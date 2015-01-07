package com.ape.onelogin.myos.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;

import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.job.AvatarUploadTask;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.myos.widget.MenuItem;
import com.ape.onelogin.myos.widget.PopupMenu;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.FileUtils;
import com.ape.onelogin.util.LogUtil;
import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.R;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject;

public class AvatarManager {
    
    private Context mContext;
    private AuthenticatorManager mAuthenticatorManager;
    private String mTempPicturePath;
    
    public static final int MENU_PICTURE_FROM_CAMERA               = 100;
    public static final int MENU_PICTURE_FROM_ALBUM                = 101;
    
    public static final int REQ_PICTURE_FROM_CAMERA                = 1000;
    public static final int REQ_PICTURE_FROM_ALBUM                 = 1001;
    
    public static final int MSG_AVATAR_DOWNLOAD_SUCCESS            =  0x1001;
    public static final int MSG_AVATAR_DOWNLOAD_FAIL               =  0x1002;
    
    private static final String PICTURE_DIRECTORY = "avatar";
    private static final int AVATAR_SIZE = 128;
    
    public AvatarManager(Context context) {
        mContext = context;
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
        mTempPicturePath = getPicturePath("tmp");
    }
    
    public void showAvatarMenu(MenuItem.OnMenuItemClickListener itemClickListener) {
        PopupMenu popupMenu = new PopupMenu(mContext, R.string.ex_picture_title);
        popupMenu.add(MENU_PICTURE_FROM_CAMERA, R.string.ex_picture_from_camera, 0)
            .setOnMenuItemClickListener(itemClickListener);
        popupMenu.add(MENU_PICTURE_FROM_ALBUM, R.string.ex_picture_from_album, 0)
            .setOnMenuItemClickListener(itemClickListener);
        popupMenu.show();
    }
    
    public static String getPicturePath(Context context, String userKey) {
        String picturePath = null;
        String pictureDir = null;
        String pictrueName = null;
        
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            pictureDir = context.getDir(PICTURE_DIRECTORY, Context.MODE_PRIVATE).getAbsolutePath();
        } else {
            pictureDir = context.getExternalCacheDir() + "/" + PICTURE_DIRECTORY;
        }
        pictrueName = getAvatarKey(userKey);
        picturePath = pictureDir + File.separator + pictrueName;
        return picturePath;
    }
    
    public static boolean isExternalStorage() {
        String sdState = Environment.getExternalStorageState();
        if (!sdState.equals(Environment.MEDIA_MOUNTED)) {
            return false;
        } else {
            return true;
        }
    }
    
    public String getPicturePath(String userKey) {
        return getPicturePath(mContext, userKey);
    }
    
    /**
     * 获取保存在服务器的头像文件名
     * 
     * @return
     */
    public static String getAvatarKey(String userKey) {
        return String.format("%s_%s", PICTURE_DIRECTORY, userKey);
    }
    
    public Bitmap getPictureFromAlbum(Intent data) {
        return getPictureFromAlbum(mTempPicturePath, data);
    }
    
    public Bitmap getPictureFromAlbum(String picturePath, Intent data) {
        Bitmap picture = null;
        Uri imageUri = data.getData();
        String[] columns = {
                MediaStore.Images.Media.DATA, 
                MediaStore.Images.Media.ORIENTATION
        };
        
        Cursor cursor = mContext.getContentResolver()
                .query(imageUri, columns, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(columns[0]));
        String orientation = cursor.getString(cursor.getColumnIndex(columns[1]));
        int angle = 0;
        if (orientation != null && !orientation.equals("")) {
            angle = Integer.parseInt(orientation);
        }
        picture = ContextUtils.scaleBitmap(path, AVATAR_SIZE, AVATAR_SIZE, angle);
        cursor.close();
        
        if (picture != null) {
            FileOutputStream fileOutputStream = null;
            File file = new File(picturePath);
            try {
                FileUtils.createNewFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                fileOutputStream = new FileOutputStream(file);
                if (path.endsWith("jpg") || path.endsWith("jpeg")) {
                    picture.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                } else {
                    picture.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return picture;
    }
    
    public Bitmap getPictureFromCamera(Intent data) {
        return getPictureFromCamera(mTempPicturePath, data);
    }
    
    public Bitmap getPictureFromCamera(String picturePath, Intent data) {
        Bitmap picture = null;
        Bundle bundle = data.getExtras();
        picture = (Bitmap) bundle.get("data");
        if (picture != null) {
            FileOutputStream fileOutputStream = null;
            File file = new File(picturePath);
            try {
                FileUtils.createNewFile(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                fileOutputStream = new FileOutputStream(file);
                picture.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException ioe) {
                   ioe.printStackTrace(); 
                }
            }
        }
        return picture;
    }
    
    public Bitmap getCooperationAvatar(String imageUri, String userKey) {
        Bitmap bitmap = null;
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        
        String picturePath = getPicturePath(userKey);
        try {
            URL myFileUrl = new URL(imageUri);
            conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            
            File file = new File(picturePath);
            FileUtils.createNewFile(file);
            inputStream = conn.getInputStream();
            outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024 * 4];
            int readLength = 0;
            while (true) {
                readLength = inputStream.read(buffer);
                if (readLength == -1) {
                    break;
                }
                outputStream.write(buffer, 0, readLength);
            }
            
            bitmap = BitmapFactory.decodeFile(picturePath);
            String avatarPath = mAuthenticatorManager.getAvatarPath();
            if (!picturePath.equals(avatarPath)) {
                mAuthenticatorManager.setAvatarPath(picturePath);
            }
            
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
    
    public void uploadAvatar(Handler handler) {
        AvatarUploadTask avatarUploadTask = new AvatarUploadTask(mContext);
        avatarUploadTask.setFile(mTempPicturePath);
        avatarUploadTask.setHandler(handler);
        avatarUploadTask.execute();
    }
    
    public static class AvatarDisplay {
        
        private Context mAvatarContext;
        private AuthenticListener mAuthenticListener;
        private LogUtil mLogUtil;
        
        public AvatarDisplay(Context context, AuthenticListener resultReListener) {
            mAvatarContext = context;
            mAuthenticListener = resultReListener;
            mLogUtil = new LogUtil("AvatarDisplay");
        }
        
        public void showAvatar(HashMap<String, String> userInfoMap) {
            if (userInfoMap == null) {
                mLogUtil.i("showAvatar", "user map is null");
                mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                        null, "user map is null!");
                return;
            }
            
            String userKey = userInfoMap.get(AbsLoginHandler.KEY_USER_KEY);
            String avatarPath = userInfoMap.get(AbsLoginHandler.KEY_AVATAR_PATH);
            String avatar = userInfoMap.get(AbsLoginHandler.KEY_AVATAR);
            String sdk = userInfoMap.get(AbsLoginHandler.KEY_SDK_TYPE);
            
            if (userKey == null || userKey.trim().length() == 0) {
                mLogUtil.i("showAvatar", "user key is empty");
                mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                        null, "user key is empty!");
                return;
            }
            
            if (AvatarManager.isExternalStorage()) {
                String tmpPath = AvatarManager.getPicturePath(mAvatarContext, userKey);
                if (!tmpPath.equals(avatarPath)) {
                    mLogUtil.i("showAvatar", "avatar storage path has been changed");
                    mLogUtil.i("showAvatar", "before:%s", avatarPath);
                    avatarPath = tmpPath;
                    OneLoginApplication.authenticatorManager.setAvatarPath(avatarPath);
                    mLogUtil.i("showAvatar", "after:%s", avatarPath);
                }
            }
            
            if (avatarPath != null && avatarPath.trim().length() != 0) {
                File avatarFile = new File(avatarPath);
                if (avatarFile.exists()) {
                    FileInputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(avatarPath);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        if (bitmap == null) {
                            avatarFile.delete();
                            loadAvatar(avatar, sdk, userKey, avatarPath);
                        } else {
                            mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS,
                                    bitmap, "avatar load success!");
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (avatar != null && avatar.trim().length() != 0) {
                        loadAvatar(avatar, sdk, userKey, avatarPath);
                    } else {
                        mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                                null, "avatar is empty!");
                    }
                }
            } else {
                if (avatar != null && avatar.trim().length() != 0) {
                    loadAvatar(avatar, sdk, userKey, avatarPath);
                } else {
                    mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                            null, "avatar and avatarPath are empty!");
                }
            }
        }
        
        private void loadAvatar(final String avatar, final String sdk,
                final String userKey, final String avatarPath) {
            new Thread() {
                
                @Override
                public void run() {
                    if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(sdk)) {
                        CloudLoginHandler testCloudService = CloudLoginHandler.getInstance(mAvatarContext);
                        Map<String, String> userMap = OneLoginApplication.authenticatorManager.getCloudFileData();
                        
                        if(userMap == null) {
                            mLogUtil.e("get cloud file data map error! the map is null!!!");
                            mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                                    null, "get cloud file data map error! the map is null!!!");
                            return;
                        }
                        testCloudService.allocFileClient(userMap);
                        MissionObject missionObject = testCloudService.initDownload(avatarPath, avatar, true);
                        int result = testCloudService.download(missionObject, null).getResultCode();
                        if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                            try {
                                InputStream is = new FileInputStream(new File(avatarPath));
                                Bitmap bitmap = BitmapFactory.decodeStream(is);
                                mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS,
                                        bitmap, "download avatar success");
                            } catch (FileNotFoundException nfe) {
                                mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                                        null, "avatarPath not found!");
                            }
                        } else {
                            mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_UNKNOWN_ERROR,
                                    null, "unknown error!");
                        }
                    } else {
                        AvatarManager avatarManager = new AvatarManager(mAvatarContext);
                        Bitmap bitmap = avatarManager.getCooperationAvatar(avatar, userKey);
                        mAuthenticListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS,
                                bitmap, "download third party avatar success");
                    }
                }
                
            }.start();
        }
    }
}

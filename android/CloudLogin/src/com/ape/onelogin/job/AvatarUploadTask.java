package com.ape.onelogin.job;

import java.io.File;
import java.util.Map;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.myos.ui.AvatarManager;
import com.ape.onelogin.util.LogUtil;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject;
import com.cloud.util.Log;

public class AvatarUploadTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "AvatarUploadTask";
    private Context mContext;
    private Handler mHandler;
    private String mTempFile;
    private String mLocalFile;
    private String mKey;
    private LogUtil mLogUtil;
    
    private MissionObject missionObject;
    private AuthenticatorManager mAuthenticatorManager;
    
    public AvatarUploadTask(Context context) {
        this.mContext = context;
        this.mLogUtil = new LogUtil("AvatarUploadTask");
        this.mAuthenticatorManager = OneLoginApplication.authenticatorManager;
    }
    
    public void setFile(String file) {
        this.mTempFile = file;
    }
    
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
    
    @Override
    protected Integer doInBackground(Void... params) {
        if(mTempFile != null && mTempFile.trim().length() != 0) {
            Map<String, String> userMap = mAuthenticatorManager.getCloudFileData();
            if(userMap == null) {
                mLogUtil.e("get cloud file data map error! the map is null!!!");
                return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
            }
            String userKey = userMap.get(AbsLoginHandler.KEY_USER_KEY);
            if (userKey == null || userKey.trim().length() == 0) {
                mLogUtil.e("userKey is empty!!!");
                return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
            }
            
            mKey = AvatarManager.getAvatarKey(userKey);
            mLocalFile = AvatarManager.getPicturePath(mContext, userKey);
            CloudLoginHandler testCloudService = CloudLoginHandler.getInstance(mContext);
            testCloudService.allocFileClient(userMap);
            testCloudService.deleteUploadMission(mKey);
            missionObject = testCloudService.initMultipartUpload(mTempFile, mKey);
            return testCloudService.multipartUploadFile(missionObject, null).getResultCode();
        }
        
        mLogUtil.e("file is empty, you must set a file!!!");
        return CloudUtil.CLOUD_FILE_FILE_NOT_EXIST;
    }
    
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if(mHandler != null) {
            final Message message = mHandler.obtainMessage();
            if(result > 0) {
                message.what = AvatarManager.MSG_AVATAR_DOWNLOAD_SUCCESS;
                
                if(missionObject != null) {
                    File tempFile = new File(mTempFile);
                    File localFile = new File(mLocalFile);
                    if (localFile.exists()) {
                        localFile.delete();
                    }
                    tempFile.renameTo(localFile);
                    final User user = mAuthenticatorManager.getUserData();
                    user.setAvatar(missionObject.getKey());
                    new Thread() {
                        public void run() {
                            Looper.prepare();
                            CloudLoginHandler.getInstance(mContext).updateUser(user, new AuthenticListener() {
                                
                                @Override
                                public void onComplete(int result, Object obj, String msg) {
                                    Log.w(TAG, "upload avatar success", Log.APP);
                                    mAuthenticatorManager.setUserData(AbsLoginHandler.KEY_AVATAR, missionObject.getKey());
                                    mAuthenticatorManager.setUserData(AbsLoginHandler.KEY_AVATAR_PATH, mTempFile);
                                    mHandler.sendMessage(message);
                                }
                                
                                @Override
                                public void onCancel() {
                                    Log.w(TAG, "upload avatar cancel", Log.APP);
                                    mHandler.sendMessage(message);
                                }
                            });
                            Looper.loop();
                        };
                    }.start();
                }
                
            } else if(result < 0) {
                message.what = AvatarManager.MSG_AVATAR_DOWNLOAD_FAIL;
                mHandler.sendMessage(message);
            }
        }
    }
}

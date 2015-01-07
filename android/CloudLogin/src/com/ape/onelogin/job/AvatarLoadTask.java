package com.ape.onelogin.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.myos.ui.AvatarManager;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.LogUtil;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject;


public class AvatarLoadTask extends AsyncTask<Void, Void, Integer> {
    
    private Context mContext;
    private Handler mHandler;
    private String avatarKey;
    private LogUtil mLogUtil;
    
    private String localAvatarFile;
    private AuthenticatorManager mAuthenticatorManager;
    
    public AvatarLoadTask(Context context) {
        this.mContext = context;
        this.mLogUtil = new LogUtil("AvatarLoadTask");
        this.mAuthenticatorManager = OneLoginApplication.authenticatorManager;
    }
    
    public void setLocalFile(String avatarKey) {
        this.avatarKey = avatarKey;
    }
    
    public void setAvatarPath(String avatarPath) {
        localAvatarFile = avatarPath;
    }
    
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }
    
    @Override
    protected Integer doInBackground(Void... params) {
//        localAvatarFile =  mContext.getExternalCacheDir() + 
//                Constants.AVATAR_LOCAL_DIR + File.separator + avatarKey;
//        localAvatarFile = ContextUtils.getAvatarPath(mContext, avatarKey);
        File file = new File(localAvatarFile);
        if(file.exists()) {
            file.delete();
        }
        
        CloudLoginHandler testCloudService = CloudLoginHandler.getInstance(mContext);
        Map<String, String> userMap = mAuthenticatorManager.getCloudFileData();
        
        if(userMap == null) {
            mLogUtil.e("get cloud file data map error! the map is null!!!");
            return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
        }
        testCloudService.allocFileClient(userMap);
        MissionObject missionObject = testCloudService.initDownload(localAvatarFile, avatarKey, true);
        return testCloudService.download(missionObject, null).getResultCode();
    }
    
    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        if(mHandler != null) {
            Message msg = mHandler.obtainMessage();
            if(result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                try {
                    InputStream is = new FileInputStream(new File(localAvatarFile));
                    Bitmap bitmap = BitmapFactory.decodeStream(is);
                    msg.obj = bitmap;
                    is.close();
                    mAuthenticatorManager.setAvatarPath(localAvatarFile);
                    msg.what = AvatarManager.MSG_AVATAR_DOWNLOAD_SUCCESS;
                } catch (Exception e) {
                    e.printStackTrace();
                    msg.what = AvatarManager.MSG_AVATAR_DOWNLOAD_FAIL;
                }
                
            } else {
                msg.what = AvatarManager.MSG_AVATAR_DOWNLOAD_FAIL;
            }
            mHandler.sendMessage(msg);
        }
    }
}

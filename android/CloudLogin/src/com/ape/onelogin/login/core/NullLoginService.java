package com.ape.onelogin.login.core;

import java.util.Map;

import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.util.Log;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/6/18
 */
public class NullLoginService implements LoginServiceInterface {
    private static final String TAG = "NullLoginService";

    @Override
    public void insertUser(User user, AuthenticListener resultListener) {
        Log.w(TAG, "<insertUser>", Log.APP);
    }

    @Override
    public void updateUser(User user, AuthenticListener apiListener) {
        Log.w(TAG, "<updateUser>", Log.APP);
    }

    @Override
    public void getUser(User user, AuthenticListener resultListener) {
        Log.w(TAG, "<getUser>", Log.APP);
    }

    @Override
    public void getGuestUser(User user, CloudSdkService service) {
        Log.w(TAG, "<getGuestUser>", Log.APP);
    }

    @Override
    public void getCooperationUser(User user, AuthenticListener resultListener) {
        Log.w(TAG, "<getCooperationUser>", Log.APP);
    }

    @Override
    public void findPassword(String address, AuthenticListener apiListener) {
        Log.w(TAG, "<findPassword>", Log.APP);
    }

    @Override
    public void sendEmail(String address, String title, String body,
            AuthenticListener apiListener) {
        Log.w(TAG, "<sendEmail>", Log.APP);
    }

    @Override
    public int allocFileClient(Map userInfoMap) {
        Log.w(TAG, "<allocFileClient>", Log.APP);
        return 0;
    }

    @Override
    public void deleteUploadMission(String key) {
        Log.w(TAG, "<deleteUploadMission>", Log.APP);
    }

    @Override
    public MissionObject initMultipartUpload(String filePath, String key) {
        Log.w(TAG, "<initMultipartUpload>", Log.APP);
        return null;
    }

    @Override
    public CloudFileResult multipartUploadFile(MissionObject missionObject,
            MissionListener listener) {
        Log.w(TAG, "<multipartUploadFile>", Log.APP);
        return null;
    }

    @Override
    public MissionObject initDownload(String localFile, String key,
            boolean overwrite) {
        Log.w(TAG, "<initDownload>", Log.APP);
        return null;
    }

    @Override
    public CloudFileResult download(MissionObject missionObject,
            MissionListener listener) {
        Log.w(TAG, "<download>", Log.APP);
        return null;
    }

    @Override
    public void clearGuestCache() {
        Log.w(TAG, "<clearGuestCache>", Log.APP);
    }
}

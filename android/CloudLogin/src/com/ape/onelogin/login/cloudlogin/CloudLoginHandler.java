package com.ape.onelogin.login.cloudlogin;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;

import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginServiceInterface;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.util.LogUtil;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/6/13
 */
public class CloudLoginHandler extends AbsLoginHandler {
    private static final String TAG = "CloudLoginHandler";
    
    public static final String SDK_TYPE = SDK_TYPE_CLOUD;
    private static CloudLoginHandler mCloudLoginHandler;
    private LogUtil mLogUtil;
    
    private CloudLoginHandler(Context context) {
        super(context);
        mLogUtil = new LogUtil(TAG);
    }
    
    public static CloudLoginHandler getInstance(Context context) {
        if(mCloudLoginHandler == null) {
            mCloudLoginHandler = new CloudLoginHandler(context);
        }
        return mCloudLoginHandler;
    }

    @Override
    public void init() {
        // nothing to do;
    }
    
    @Override
    public void login(Activity activity, User user, AuthenticListener resultListener) {
        mLogUtil.i("login", "\n%s", LogUtil.formatUserMessage(user));
        long time = System.currentTimeMillis();
        user.setExpiresIn(String.valueOf(time + AbsLoginHandler.LOGIN_CACHE_TIME));
        mLoginService.getUser(user, resultListener);
    }
    
    public void login(final CloudSdkService service) {
        final User user = mAuthenticatorManager.getUserData();
        final HashMap<String, String> userMap = new HashMap<String, String>();
        userMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
        
        login(null, user, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    onLogin(user, new AuthenticListener() {
                        
                        @Override
                        public void onComplete(int result, Object obj, String message) {
                            if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                                if (obj instanceof User) {
                                    User resultUser = (User) obj;
                                    mAuthenticatorManager.handleUser(resultUser);
                                    service.beginBroadcastForQuery(mAuthenticatorManager.getUserMapData());
                                } else {
                                    @SuppressWarnings("unchecked")
                                    HashMap<String, Object> resultMap = (HashMap<String, Object>) obj;
                                    User resultUser = (User) resultMap.get(Constants.REQ_SEND_MAIL_PARAM_2);
                                    mAuthenticatorManager.handleUser(resultUser);
                                    service.beginBroadcastForQuery(mAuthenticatorManager.getUserMapData());
                                }
                            } else {
                                service.beginBroadcastForQuery(userMap);
                            }
                        }
                        
                        @Override
                        public void onCancel() {
                            service.beginBroadcastForQuery(userMap);
                        }
                    });
                } else {
                    service.beginBroadcastForQuery(userMap);
                }
            }
            
            @Override
            public void onCancel() {
                service.beginBroadcastForQuery(userMap);
            }
        });
    }
    
    @Override
    public void onLogin(User user, AuthenticListener resultListener) {
        mLogUtil.i("onLogin", "\n%s", LogUtil.formatUserMessage(user));
        long time = System.currentTimeMillis();
        user.setExpiresIn(String.valueOf(time + AbsLoginHandler.LOGIN_CACHE_TIME));
        mLoginService.updateUser(user, resultListener);
    }
    
    public void loginByGuest(User user, CloudSdkService service) {
        mLogUtil.i("loginByGuest", "\n%s", LogUtil.formatUserMessage(user));
        long time = System.currentTimeMillis();
        user.setRegisterDate(formateDate(time));
        mLoginService.getGuestUser(user, service);
    }
    
    @Override
    public void register(User user, AuthenticListener resultListener) {
        mLogUtil.i("register", "\n%s", LogUtil.formatUserMessage(user));
        long time = System.currentTimeMillis();
        user.setRegisterDate(formateDate(time));
        user.setSdkType(AbsLoginHandler.SDK_TYPE_CLOUD);
        user.setExpiresIn(String.valueOf(time + AbsLoginHandler.LOGIN_CACHE_TIME));
        mLoginService.insertUser(user, resultListener);
    }
    
    @Override
    public void logout(Activity activity, AuthenticListener resultListener) {
        mAuthenticatorManager.removeUser();
        resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, null, null);
    }
    
    
    @Override
    public void setAccessToken() {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public String getSdkType() {
        return SDK_TYPE;
    }
    
    @Override
    public void findPassword(String address, AuthenticListener resultListener) {
        mLoginService.findPassword(address, resultListener);
    }
    
    @Override
    public void sendEmail(String address, String title, String body,
            AuthenticListener resultListener) {
        mLoginService.sendEmail(address, title, body, resultListener);
    }
    
    public void allocFileClient(Map<String, String> userInfoMap) {
        mLoginService.allocFileClient(userInfoMap);
    }
    
    public void deleteUploadMission(String key) {
        mLoginService.deleteUploadMission(key);
    }
    
    public MissionObject initMultipartUpload(String filePath, String key) {
        return mLoginService.initMultipartUpload(filePath, key);
    }
    
    public CloudFileResult multipartUploadFile(MissionObject missionObject,
            MissionListener listener) {
        return mLoginService.multipartUploadFile(missionObject, listener);
    }
    
    public MissionObject initDownload(String localFile, String key, boolean overwrite) {
        return mLoginService.initDownload(localFile, key,overwrite);
    }
    
    public CloudFileResult download(MissionObject missionObject,
            MissionListener listener) {
        return mLoginService.download(missionObject, listener);
    }
    
    public LoginServiceInterface getLoginService(){
        return mLoginService;
    }
}

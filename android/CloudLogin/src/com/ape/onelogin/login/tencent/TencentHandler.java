package com.ape.onelogin.login.tencent;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.Log;
import com.tencent.connect.UserInfo;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

public class TencentHandler extends AbsLoginHandler {
    public static final String SDK_TYPE = SDK_TYPE_TENCENT;
    private Tencent mTencent;
    private Long mLoginTime;
    
    private static TencentHandler mTencentHandler;
    
    private TencentHandler(Context context) {
        super(context);
        init();
    }
    
    public static TencentHandler getInstance(Context context) {
        if(mTencentHandler == null) {
            mTencentHandler = new TencentHandler(context);
        }
        return mTencentHandler;
    }

    @Override
    public void init() {
        mTencent = Tencent.createInstance(Constants.APP_KEY_TENCENT, context);
        setAccessToken();
    }
    
    @Override
    public void login(Activity activity,User user,AuthenticListener listener) { 
        Log.i(SDK_TYPE, "<login>", Log.APP);
        mLoginTime = System.currentTimeMillis();
        if (mTencent == null) {
            mTencent = Tencent.createInstance(Constants.APP_KEY_TENCENT, context);
        }
        
        if (!mTencent.isSessionValid()) {
            mTencent.login(activity, Constants.SCOPE_TENCENT, new BaseUiListener(listener));
        } else {
            mTencent.logout(activity);
        }
        
    }
    
    @Override
    public void register(final User user, final AuthenticListener resultListener) {
        long time = System.currentTimeMillis();
        user.setRegisterDate(formateDate(time));
        user.setSdkType(AbsLoginHandler.SDK_TYPE_TENCENT);
        mLoginService.insertUser(user, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    // 注册成功，从第三方服务器获取用户详细信息并保存到数据服务器上
                    OneLoginApplication.loginManager.clearGuestCache();
                    getUserInfomation(user, resultListener);
                } else {
                    resultListener.onComplete(result, obj, message);
                }
            }
            
            @Override
            public void onCancel() {
                resultListener.onCancel();
            }
        });
    }
    
    @Override
    public void logout(Activity activity,AuthenticListener resultListener) {
        Log.i(SDK_TYPE, "<logout> mTencent = " + mTencent, Log.APP);
        if(mTencent != null && mTencent.isSessionValid()) {
            mTencent.logout(activity);
        }
        if (resultListener != null) {
            resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, null, "logout");;
        }
        
        mAuthenticatorManager.removeUser();
    }
    
    @Override
    public void setAccessToken() {
//        HashMap<String, String> tokenMap = mAuthenticatorManager.getUserMapData();
//        Log.i(SDK_TYPE, "<setAccessToken> tokenMap = " + tokenMap, Log.APP);
//        if(tokenMap != null) {
//            String uid = tokenMap.get(KEY_UID);
//            String accessToken = tokenMap.get(KEY_ACCESS_TOKEN);
//            String expires_in = tokenMap.get(KEY_EXPIRES_IN);
//            
//            if(!"".equals(uid) 
//                    && !"".equals(accessToken) 
//                    && !"".equals(expires_in)) {
//                mTencent.setOpenId(uid);
//                mTencent.setAccessToken(accessToken, expires_in);
//            }
//        }
    }
    
    private class BaseUiListener implements IUiListener {
        private AuthenticListener resultListener;
        
        public BaseUiListener(AuthenticListener resultListener) {
            this.resultListener = resultListener;
        }

        @Override
        public void onComplete(Object response) {
            try {
                JSONObject jsonObject = (JSONObject) response;
                String uid = "";
                String accessToken = "";
                String expires_in = "";
                if (jsonObject.has("openid")) {
                    uid = jsonObject.getString("openid");
                }
                if(jsonObject.has("access_token")) {
                    accessToken = jsonObject.getString("access_token");
                }
                if(jsonObject.has("expires_in")) {
                    expires_in = jsonObject.getString("expires_in");
                }
                
                if(!"".equals(uid) 
                        && !"".equals(accessToken) 
                        && !"".equals(expires_in)) {
                    Long masterExpires = 
                            Math.min(Long.valueOf(expires_in) * 1000, LOGIN_CACHE_TIME) + mLoginTime;
                    User user = new User();
                    user.setUserName(uid);
                    user.setPassword(accessToken);
                    user.setExpiresIn(String.valueOf(masterExpires));
                    user.setSdkType(getSdkType());
                    user.setImei(ContextUtils.getImei(context));
                    resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, user, "");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(UiError e) {
            if(resultListener != null) {
                resultListener.onComplete(INVALID_USER, null, INVALID_USER_MESSAGE);
            }
            
            close();
        }

        @Override
        public void onCancel() {
            if(resultListener != null) {
                resultListener.onCancel();
            }
            
            close();
        }
    }

    @Override
    public String getSdkType() {
        return SDK_TYPE;
    }
    
    public Tencent getTencent() {
        return mTencent;
    }
    
    /**
     * 从数据服务器上获取相关用户
     * 1.用户不存在，则注册
     * 2.用户存在，则从第三方服务器获取用户信息
     * 
     * @param user
     * @param resultListener
     */
    @Override
    public void onLogin(final User user, final AuthenticListener resultListener) {
        mLoginService.getCooperationUser(user, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                if (result == LoginService.REP_LOGIN_SERVICE_USER_NOT_EXIST) {
                    // 用户不存在，注册
                    register(user, resultListener);
                } else if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    // 用户存在，从第三方服务器获取用户信息，并保存到数据服务器
                    User operateUser = (User) obj;
                    operateUser.setExpiresIn(user.getExpiresIn());
                    getUserInfomation(operateUser, resultListener);
                } else {
                    // 错误
                    resultListener.onComplete(result, obj, message);
                }
            }
            
            @Override
            public void onCancel() {
                resultListener.onCancel();
            }
        });
    }
    
    /**
     * 从第三方服务器上获取用户信息，并保存到数据服务器上
     * 
     * @param resultListener
     */
    private void getUserInfomation(final User user, final AuthenticListener resultListener) {
        if(mTencent != null) {
            UserInfo userInfo = new UserInfo(context, mTencent.getQQToken());
            IUiListener listener = new IUiListener() {
                
                @Override
                public void onComplete(final Object response) {
                    if(resultListener != null) {
                        JSONObject jsonObject = (JSONObject) response;
                        try {
                            user.setNickName(jsonObject.getString("nickname"));
                            user.setAvatar(jsonObject.getString("figureurl_qq_2"));
                            user.setSdkType(getSdkType());
                            updateUser(user, resultListener);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            resultListener.onComplete(INVALID_USER, null, null);
                        }
                    }
                }
                
                @Override
                public void onCancel() {
                    if(resultListener != null) {
                        resultListener.onCancel();
                    }
                }
                
                @Override
                public void onError(UiError e) {
                    if(resultListener != null) {
                        resultListener.onComplete(INVALID_USER, null, null);
                    }
                }
            };
            userInfo.getUserInfo(listener);
        }
    }
    
    public void close() {
        if (mTencent != null) {
            mTencent.logout(context);
            mTencent.releaseResource();
            mTencent = null;
        }
    }
}

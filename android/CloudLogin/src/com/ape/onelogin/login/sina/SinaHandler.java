package com.ape.onelogin.login.sina;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.login.sina.openapi.LogoutAPI;
import com.ape.onelogin.login.sina.openapi.UsersAPI;
import com.ape.onelogin.login.sina.openapi.models.ErrorInfo;
import com.ape.onelogin.login.sina.openapi.models.SinaUser;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.Log;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;
import com.sina.weibo.sdk.net.RequestListener;

/**
 * Sina登录
 * 
 * @author jianwen.zhu
 * @since 2014-3-4
 */
public class SinaHandler extends AbsLoginHandler {
    public static final String SDK_TYPE = SDK_TYPE_SINA;
    /** 微博 Web 授权类，提供登陆等功能  */
    private WeiboAuth mWeiboAuth;
    
    /** 注意：SsoHandler 仅当 SDK 支持 SSO 时有效 */
    private SsoHandler mSsoHandler;
    
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    private Oauth2AccessToken mAccessToken;
    
    private static SinaHandler mSinaHandler;
    private Long mLoginTime;
    
    private SinaHandler(Context context) {
        super(context);
        init();
    }
    
    public static SinaHandler getInstance(Context context) {
        if(mSinaHandler == null) {
            mSinaHandler = new SinaHandler(context);
        }
        return mSinaHandler;
    }
    
    @Override
    public void init() {
        setAccessToken();
    }
    
    @Override
    public void login(Activity activity, User user, AuthenticListener resultListener) {
        Log.i(SDK_TYPE, "<login>", Log.APP);
        // 创建微博实例
        mWeiboAuth = new WeiboAuth(activity,
                            Constants.APP_KEY_SINA, 
                            Constants.REDIRECT_URL, 
                            Constants.SCOPE_SINA);
        
        mSsoHandler = new SsoHandler(activity, mWeiboAuth);
        try {
            mLoginTime = System.currentTimeMillis();
            mSsoHandler.authorize(new AuthListener(resultListener));
        } catch (Exception e) {
            Log.w(SDK_TYPE, "<login>", Log.APP);
            e.printStackTrace();
        }
    }
    
    @Override
    public void register(final User user, final AuthenticListener resultListener) {
        long time = System.currentTimeMillis();
        user.setRegisterDate(formateDate(time));
        user.setSdkType(AbsLoginHandler.SDK_TYPE_SINA);
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
    public void logout(Activity activity, final AuthenticListener resultListener) {
        Log.i(SDK_TYPE, "<logout> mAccessToken = " + mAccessToken, Log.APP);
        if(mAccessToken != null) {
            new LogoutAPI(mAccessToken).logout(new RequestListener() {
                
                @Override
                public void onWeiboException(WeiboException e) {
                    if(resultListener != null) {
                        resultListener.onComplete(INVALID_USER, null, null);
                    }
                }
                
                @Override
                public void onComplete(String response) {
                    if (!TextUtils.isEmpty(response)) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            String value = obj.getString("result");
                            if ("true".equalsIgnoreCase(value)) {
                                if(resultListener != null) {
                                    resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, null, value);;
                                }
                                mAccessToken = null;
                                mAuthenticatorManager.removeUser();
                            }
                        } catch (JSONException jse) {
                            if(resultListener != null) {
                                resultListener.onComplete(INVALID_USER, null, null);
                            }
                            jse.printStackTrace();
                        }
                    }
                }
            });
        } else {
            if(resultListener != null) {
                resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, null, null);;
            }
            mAuthenticatorManager.removeUser();
        }
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
//                mAccessToken = new Oauth2AccessToken();
//                mAccessToken.setUid(uid);
//                mAccessToken.setToken(accessToken);
//                mAccessToken.setExpiresTime(expires_in.equals("") ? 0 : Long.valueOf(expires_in));
//            }
//        }
    }
    
    /**
     * 微博认证授权回调类。
     * 1. SSO 授权时，需要在 {@link #onActivityResult} 中调用 {@link SsoHandler#authorizeCallBack} 后，
     *    该回调才会被执行。
     * 2. 非 SSO 授权时，当授权结束后，该回调就会被执行。
     * 当授权成功后，请保存该 access_token、expires_in、uid 等信息到 SharedPreferences 中。
     */
    private class AuthListener implements WeiboAuthListener {
        public AuthenticListener resultListener;
        
        public AuthListener(AuthenticListener resultListener) {
            this.resultListener = resultListener;
        }
        
        @Override
        public void onComplete(Bundle values) {
            // 从 Bundle 中解析 Token
            mAccessToken = Oauth2AccessToken.parseAccessToken(values);
            if (mAccessToken.isSessionValid()) {
                String uid = mAccessToken.getUid();
                String accessToken = mAccessToken.getToken();
                String expires_in = String.valueOf(mAccessToken.getExpiresTime());
                
                Long masterExpires = 
                        Math.min(Long.valueOf(expires_in), LOGIN_CACHE_TIME + mLoginTime);
                
                User user = new User();
                user.setUserName(uid);
                user.setPassword(accessToken);
                user.setExpiresIn(String.valueOf(masterExpires));
                user.setSdkType(getSdkType());
                user.setImei(ContextUtils.getImei(context));
                resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, user, "");
            } else {
                // 当您注册的应用程序签名不正确时，就会收到 Code，请确保签名正确
                String code = values.getString("code");
                if(resultListener != null) {
                    Log.w(SDK_TYPE, "code is incorrect", Log.APP);
                    resultListener.onComplete(INVALID_USER, null, INVALID_USER_MESSAGE);
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
        public void onWeiboException(WeiboException e) {
            if(resultListener != null) {
                resultListener.onComplete(INVALID_USER, null, INVALID_USER_MESSAGE);
            }
        }
    }
    
    @Override
    public String getSdkType() {
        return SDK_TYPE;
    }
    
    public SsoHandler getSsoHandler() {
        return mSsoHandler;
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
                    // TODO 将user赋值给obj
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
        if (mAccessToken != null) {
            UsersAPI usersAPI = new UsersAPI(mAccessToken);
            usersAPI.show(Long.valueOf(mAccessToken.getUid()), new RequestListener() {
                
                @Override
                public void onComplete(String response) {
                    if (!TextUtils.isEmpty(response)) {
                        SinaUser sinaUser = SinaUser.parse(response);
                        if(sinaUser != null) {
                            user.setNickName(sinaUser.screen_name);
                            user.setAvatar(sinaUser.avatar_hd);
                            user.setSdkType(getSdkType());
                            updateUser(user, resultListener);
                        }
                    }
                }
                
                @Override
                public void onWeiboException(WeiboException e) {
                    ErrorInfo info = ErrorInfo.parse(e.getMessage());
                    Log.w(SDK_TYPE, info.toString(), Log.APP);
                    if(resultListener != null) {
                        resultListener.onComplete(INVALID_USER, null, info.toString());
                    }
                }
            });
        } else {
            resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_CACHE_NOT_EXIST, null, null);
        }
    }
    
    public void close() {
        if (mSsoHandler != null) {
            mSsoHandler = null;
        }
    }
}

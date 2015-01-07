package com.ape.onelogin.login.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.util.LogUtil;
import com.cloud.client.CloudUtil;
import com.cloud.util.Log;

public abstract class AbsLoginHandler implements ILoginInterface{
    protected Context context;
    
    private static final String TAG = "AbsLoginHandler";
    
    // 用户信息本地缓存时间限制,最小时间请设置大于10分钟,否则存在登录不成功的情况,此值越小,登录不成功的几率越大
    public static final long LOGIN_CACHE_TIME       = 24 * 60 * 60 * 1000; // hour * minute * second * millisecond;
    
    public static final String INVALID_USER_MESSAGE                              = "INVALID USER";
    public static final String INVALID_USER_KEY                                  = "-1";
    public static final int INVALID_USER                                         = -1;
    
    public static final String PREFERENCES_NAME = "onelogin_detial_preference";
    
    public static final String SDK_TYPE_ANONYMOUS           = "anonymous";
    public static final String SDK_TYPE_CLOUD               = "cloud";
    public static final String SDK_TYPE_SINA                = "sina";
    public static final String SDK_TYPE_TENCENT             = "tencent";
    public static final String SDK_TYPE_UNKNOWN             = "unknown";
    
    // important information
    public static final String KEY_USER_KEY                 = CloudUtil.KEY_CLOUD_PARAM_USER_KEY;        // index in database
    public static final String KEY_UID                      = "uid";            // userName
    public static final String KEY_ACCESS_TOKEN             = "access_token";   // password
    public static final String KEY_EXPIRES_IN               = "expires_in";
    public static final String KEY_MOBILE                   = "mobile";
    public static final String KEY_EMAIL                    = "email";
    public static final String KEY_IMEI                     = "imei";
    
    public static final String KEY_REGISTERDATE             = "registerDate";

    // user information to show
    public static final String KEY_NICKNAME                 = "nickname";
    public static final String KEY_GENDER                   = "gender";
    public static final String KEY_BIRTHDAY                 = "birthday";
    public static final String KEY_ADDRESS                  = "address";
    public static final String KEY_AVATAR                   = "avatar";
    public static final String KEY_AVATAR_PATH              = "avatar_path";
    
    // lesser information
    public static final String KEY_HOMETOWN                 = "hometown";
    public static final String KEY_COUNTRY                  = "country";
    public static final String KEY_PROVINCE                 = "province";
    public static final String KEY_CITY                     = "city";
    public static final String KEY_PROFESSION               = "profession";

    // server parameter, only set by login
    public static final String KEY_ACCESSID                 = CloudUtil.KEY_CLOUD_PARAM_ACCESSID;
    public static final String KEY_SECRETKEY                = CloudUtil.KEY_CLOUD_PARAM_SECRETKEY;
    public static final String KEY_BUCKETNAME               = CloudUtil.KEY_CLOUD_PARAM_BUCKETNAME;
    public static final String KEY_OSSTYPE                  = CloudUtil.KEY_CLOUD_PARAM_OSSTYPE;
    public static final String KEY_OSSLOCAL                 = CloudUtil.KEY_CLOUD_PARAM_OSSLOCAL;
    public static final String KEY_SDK_TYPE                 = "sdk_type";

    // obligate field
    public static final String KEY_FIELD1                   = "field1";
    public static final String KEY_FIELD2                   = "field2";
    public static final String KEY_FIELD3                   = "field3";
    public static final String KEY_FIELD4                   = "field4";
    public static final String KEY_FIELD5                   = "field5";
    
    // for auto login
//    public static final String KEY_LAST_LOGIN_TIME          = "last_login_time";    // not in server DB
    
    private LogUtil mLogUtil;
    protected static LoginServiceInterface mLoginService = new NullLoginService();
    protected AuthenticatorManager mAuthenticatorManager;
    
    public AbsLoginHandler(Context context) {
        this.context = context;
        mLogUtil = new LogUtil(TAG);
        mLoginService =  new LoginService(context, 
                OneLoginApplication.getInstance(context).getAskerAppkey());
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
    }
    
    @Override
    public void showUser(AuthenticListener resultListener) {
        User user = mAuthenticatorManager.getUserData();
        if (user == null) {
            resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_CACHE_NOT_EXIST,
                    null, "no cache in local");
        } else {
            String expiresInString = user.getExpiresIn();
            long expiresIn = 0l;
            if (expiresInString == null || expiresInString.trim().length() == 0) {
                expiresIn = 0l;
            } else {
                expiresIn =Long.valueOf(expiresInString);
            }
            long now = System.currentTimeMillis();
            if (now > expiresIn) {
                resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_CACHE_TIMEOUT,
                        user, "cache timeout, login again");
            } else {
                resultListener.onComplete(LoginService.REP_LOGIN_SERVICE_SUCCESS, user, null);
            }
        }
    }
    
    @Override
    public void updateUser(User user, AuthenticListener resultListener) {
        mLoginService.updateUser(user, resultListener);
    }
    
    public void clearLoginTime() {
        if (context != null) {
            SharedPreferences preferences = 
                    context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
            Editor editor = preferences.edit();
            editor.putString(KEY_EXPIRES_IN, "0");
            editor.commit();
            mLogUtil.i("clearLoginTime");
        }
    }
    
    protected HashMap<String, String> getUserMap(String uid) {
        HashMap<String, String> userMap = new HashMap<String, String>();
        userMap.put(KEY_UID, uid);
        userMap.put(KEY_SDK_TYPE, getSdkType());
        return userMap;
    }
    
    @Override
    public void findPassword(String address, AuthenticListener resultListener) {
        Log.w(getSdkType(), "implement this function first", Log.APP);
    }
    
    @Override
    public void sendEmail(String address, String title, String body,
            AuthenticListener resultListener) {
        Log.w(getSdkType(), "implement this function first", Log.APP);
    }
    
    @Override
    public void clearGuestCache() {
        Log.w(getSdkType(), "implement this function first", Log.APP);
        mLoginService.clearGuestCache();
    }
    
    public abstract String getSdkType();
    
    @SuppressLint("DefaultLocale")
    private String notNullString(String string) {
        return (string == null || string.trim().toLowerCase().equals("null")) ? "" : string;
    }
    
    @SuppressLint("SimpleDateFormat")
    protected String formateDate(long timeMillions) {
        SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeMillions);
        return formate.format(date);
    }
}

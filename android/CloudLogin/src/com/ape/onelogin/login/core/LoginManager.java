package com.ape.onelogin.login.core;

import android.app.Activity;
import android.content.Context;

import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.login.sina.SinaHandler;
import com.ape.onelogin.login.tencent.TencentHandler;
import com.ape.onelogin.util.Log;
import com.ape.onelogin.util.LogUtil;

/**
 * 
 * @author jianwen.zhu
 * @since 2014-3-3
 */
public class LoginManager {
    private String TAG = "LoginManager";
    
    private Context mContext;
    
    private static LoginManager mLoginManager;
    
    private ILoginInterface iLoginInterface = new NullLogin();
    
    private String sdkType;
    
    private LogUtil mLogUtil;
    
    /**
     * 鉴权接口
     * 
     * @author jianwen.zhu
     *
     */
    public interface AuthenticListener {
        public void onComplete(int result, Object obj, String message);
        public void onCancel();
    }
    
    private LoginManager(){
        mLogUtil = new LogUtil(TAG);
    }
    
    public static LoginManager getInstance() {
        if(mLoginManager == null) {
            mLoginManager = new LoginManager();
        }
        return mLoginManager;
    }
    
    public void initSdk(Context context) {
        Log.i(TAG, "<initSdk>", Log.APP);
        mContext = context;
        
        sdkType = AuthenticatorManager.getInstance(context).getSdkType();
        if (sdkType == null || sdkType.trim().length() == 0) {
            sdkType = AbsLoginHandler.SDK_TYPE_UNKNOWN;
        }
        switchSdk(sdkType);
    }
    
    /**
     * 
     * @param context
     * @param sdkType
     */
    public void switchSdk(String sdkType) {
        Log.i(TAG, "<switchSdk> sdkType=" + sdkType, Log.APP);
        this.sdkType = sdkType;
        if (sdkType.equals(AbsLoginHandler.SDK_TYPE_SINA)) {
            iLoginInterface = SinaHandler.getInstance(mContext);
        } else if (sdkType.equals(AbsLoginHandler.SDK_TYPE_TENCENT)) {
            iLoginInterface = TencentHandler.getInstance(mContext);
        } else {
            iLoginInterface = CloudLoginHandler.getInstance(mContext);
        }
    }
    
    public void showUser(AuthenticListener resultListener) {
        iLoginInterface.showUser(resultListener);
    }
    
    public void register(User user, AuthenticListener resultListener, String sdkType) {
        mLogUtil.i("register", "register account, sdk:%s, userName:%s",
                sdkType.toString(), user.getUserName());
        switchSdk(sdkType);
        iLoginInterface.register(user, resultListener);
    }
    
    public void login(Activity activity,
            User user, AuthenticListener resultListener, String sdkType)  {
        switchSdk(sdkType);
        iLoginInterface.login(activity, user, resultListener);
    }
    
    public void modify(User user, AuthenticListener resultListener) {
        iLoginInterface.updateUser(user, resultListener);
    }
    
    public void logout(Activity activity, AuthenticListener resultListener) {
        mLogUtil.i("logout");
        iLoginInterface.logout(activity, resultListener);
    }
    
    public void clearGuestCache() {
        iLoginInterface.clearGuestCache();
    }
    
    public String getSdkType() {
        return sdkType;
    }
    
    public void setSdkType(String sdkType) {
        this.sdkType = sdkType;
    }
    
    public void findPassword(String address, AuthenticListener resultListener) {
        iLoginInterface.findPassword(address, resultListener);
    }
}

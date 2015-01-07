package com.ape.onelogin.login.core;

import android.app.Activity;

import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.util.Log;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/6/18
 */
public class NullLogin implements ILoginInterface {
    
    private static final String TAG = "NullLogin";

    @Override
    public void init() {
        Log.w(TAG, "<init>", Log.APP);
    }

    @Override
    public void showUser(AuthenticListener resultListener) {
        Log.w(TAG, "<showUser>", Log.APP);
    }

    @Override
    public void login(Activity activity, User user, AuthenticListener resultListener) {
        Log.w(TAG, "<login>", Log.APP);
    }
    
    @Override
    public void onLogin(User user, AuthenticListener resultListener) {
        Log.w(TAG, "<onLogin>", Log.APP);
    }
    
    @Override
    public void register(User user, AuthenticListener resultListener) {
        Log.w(TAG, "<register>", Log.APP);
    }

    @Override
    public void updateUser(User user, AuthenticListener resultListener) {
        Log.w(TAG, "<updateUser>", Log.APP);
    }

    @Override
    public void logout(Activity activity, AuthenticListener apiListener) {
        Log.w(TAG, "<logout>", Log.APP);
    }

    @Override
    public void findPassword(String address, AuthenticListener resultListener) {
        Log.w(TAG, "<findPassword>", Log.APP);
    }

    @Override
    public void sendEmail(String address, String title, String body,
            AuthenticListener resultListener) {
        Log.w(TAG, "<sendEmail>", Log.APP);
    }

    @Override
    public void clearGuestCache() {
        Log.w(TAG, "<clearGuestCache>", Log.APP);
    }

    @Override
    public void setAccessToken() {
        Log.w(TAG, "<setAccessToken>", Log.APP);
    }
}

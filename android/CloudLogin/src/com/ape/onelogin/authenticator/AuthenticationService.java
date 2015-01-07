package com.ape.onelogin.authenticator;

import com.ape.onelogin.util.LogUtil;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticationService extends Service {

    private Authenticator mAuthenticator;
    private LogUtil mLogUtil;
    
    @Override
    public void onCreate() {
        mLogUtil = new LogUtil("AuthenticationService");
        mLogUtil.i("onCreate", "Authentication service create!");
        mAuthenticator = new Authenticator(this);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

    @Override
    public void onDestroy() {
        mLogUtil.i("onDestroy", "Authentication service destory!");
    }
}

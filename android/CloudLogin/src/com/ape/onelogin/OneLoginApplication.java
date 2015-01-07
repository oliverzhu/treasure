package com.ape.onelogin;

import android.app.Application;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.db.DBHelperImpl;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginManager;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.service.ICloudSdkService;
import com.ape.onelogin.util.LogUtil;

public class OneLoginApplication extends Application {
    public static DBHelperImpl dbHelper;
    public static LoginManager loginManager;
    public static AuthenticatorManager authenticatorManager;
    
    private static OneLoginApplication application;
    private String mAskerAppKey;
    private Context mContext;
    private LogUtil mLogUtil;
    
    @Override
    public void onCreate() {
        OneLoginApplication.getInstance(this).setAskerAppkey(Constants.APP_KEY_CLOUDLOGIN);
        openCloudService();
    }
    
    @Override
    public void onTerminate() {
        closeCloudService();
    }
    
    public OneLoginApplication() {
    }
    
    private OneLoginApplication(Context context) {
        mContext = context;
        authenticatorManager = AuthenticatorManager.getInstance(context);
        loginManager = LoginManager.getInstance();
        mLogUtil = new LogUtil("OneLoginApplication");
    }
    
    public static OneLoginApplication getInstance(Context context) {
        if (application == null) {
            application = new OneLoginApplication(context);
        }
        return application;
    }
    
    public void openCloudService() {
        if (sCloudSdkService == null) {
            Intent service = new Intent(this, CloudSdkService.class);
            bindService(service, cloudSdkServiceConn, Service.BIND_AUTO_CREATE);
        }
    }
    
    public void closeCloudService() {
        if(sCloudSdkService != null && cloudSdkServiceConn != null) {
            sCloudSdkService = null;
            try {
                mLogUtil.i("unbindService mCloudSdkService");
                unbindService(cloudSdkServiceConn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public String getAskerAppkey() {
        if (mAskerAppKey == null) {
            throw new IllegalArgumentException("asker app key is null");
        }
        return mAskerAppKey;
    }
    
    public void setAskerAppkey(String askerAppKey) {
        mAskerAppKey = askerAppKey;
        if (loginManager != null) {
            loginManager.initSdk(mContext);
        }
    }
    
    public static ICloudSdkService sCloudSdkService = null;
    private ServiceConnection cloudSdkServiceConn = new ServiceConnection() {
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            sCloudSdkService = (ICloudSdkService) ICloudSdkService.Stub.asInterface(service);
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (sCloudSdkService != null) {
                sCloudSdkService = null;
            }
        }
    };
}

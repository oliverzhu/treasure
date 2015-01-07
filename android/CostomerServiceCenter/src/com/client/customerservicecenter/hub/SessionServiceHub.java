package com.client.customerservicecenter.hub;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.ape.onelogin.service.ICloudSdkService;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;

/**
 * 查看当前登录状态
 * @author jianwen.zhu
 * @since 2014/11/12
 */
public class SessionServiceHub {
	private  ICloudSdkService mCloudSdkService = null;
	
	public class CloudServiceConnection implements ServiceConnection
	{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mCloudSdkService = (ICloudSdkService) ICloudSdkService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (mCloudSdkService != null) {
            	mCloudSdkService = null;
            }
		}
		
	}
	
	public synchronized boolean isSessionValid(final Context context)
	{
		boolean isSessionValid = false;
		Intent intent = ContextUtils.getActivityIntent(context, 
				Constants.LOGIN_PACKAGE_NAME, Constants.LOGIN_LAUNCHER_ACTIVITY_NAME);
		if(intent == null)
		{
			return false;
		}
		Intent service = new Intent(Constants.LOGIN_SERVICE_NAME);
		final CloudServiceConnection conn = new CloudServiceConnection();
		context.getApplicationContext().bindService(service, conn, Service.BIND_AUTO_CREATE);
		
		while(true) {
			if(mCloudSdkService != null) {
				try {
					isSessionValid = mCloudSdkService.isSessionValid();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		if(mCloudSdkService != null)
		{
			context.getApplicationContext().unbindService(conn);
			mCloudSdkService = null;
		}
		
		return isSessionValid;
		
	}
	
}

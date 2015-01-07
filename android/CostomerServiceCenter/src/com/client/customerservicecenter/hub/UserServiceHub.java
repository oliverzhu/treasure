package com.client.customerservicecenter.hub;

import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.ape.onelogin.service.ICloudSdkService;
import com.ape.onelogin.service.IQueryCompletionCallback;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;

/**
 * @author jianwen.zhu
 * @since 2014/9/4
 */
public class UserServiceHub {
	private  ICloudSdkService mCloudSdkService = null;
	private IQueryCompletionCallback mQueryCallbacks = null;
	
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
	
	public synchronized void getUserKey(final Context context,final onQueryUserInfoListener listener)
	{
		Intent intent = ContextUtils.getActivityIntent(context, 
				Constants.LOGIN_PACKAGE_NAME, Constants.LOGIN_LAUNCHER_ACTIVITY_NAME);
		if(intent == null)
		{
			listener.onQueryUserInfo(null);
			return;
		}
		Intent service = new Intent(Constants.LOGIN_SERVICE_NAME);
		final CloudServiceConnection conn = new CloudServiceConnection();
		context.getApplicationContext().bindService(service, conn, Service.BIND_AUTO_CREATE);
		
		mQueryCallbacks = new IQueryCompletionCallback.Stub() {
			
			@Override
			public void onQueryCompletion(Map userInfoMap) throws RemoteException {
				listener.onQueryUserInfo(userInfoMap);
				unRegisterQueryCallback(context,mQueryCallbacks,conn);
			}
		};
		while(true) {
			if(mCloudSdkService != null) {
				try {
					mCloudSdkService.registerQueryCallback(mQueryCallbacks);
					mCloudSdkService.getUserKey();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
	}
	
	private void unRegisterQueryCallback(Context context,
			IQueryCompletionCallback queryCallbacks,
			CloudServiceConnection conn)
	{
		if(mCloudSdkService != null && queryCallbacks != null)
		{
			try {
				mCloudSdkService.unRegisterQueryCallback(queryCallbacks);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			context.getApplicationContext().unbindService(conn);
			mCloudSdkService = null;
			mQueryCallbacks = null;
		}
	}
	
	public interface onQueryUserInfoListener
	{
		public void onQueryUserInfo(Map userInfoMap);
	}
}

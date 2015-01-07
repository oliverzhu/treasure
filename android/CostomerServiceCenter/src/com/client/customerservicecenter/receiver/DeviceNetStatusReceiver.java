package com.client.customerservicecenter.receiver;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.widget.Toast;

import com.client.customerservicecenter.MainActivity;
import com.client.customerservicecenter.R;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.job.LoadElectronicCardJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.common.upgrade.utils.Log;

/**
 * 当网络状态发生变化则处理相应的事件
 * @author jianwen.zhu
 * @since 2014/11/17
 */
public class DeviceNetStatusReceiver extends BroadcastReceiver {
	private static final String TAG = "DeviceNetStatusReceiver";
	private Context mContext;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_ELECTROINICCARD_NOT_ACTIVATED:
				Log.i(TAG, "electroiniccard not activated", Log.APP);
				showActivateNotification();
				break;
			case Constants.MSG_ELECTROINICCARD_ACTIVATED:
				Log.i(TAG, "electroiniccard activated", Log.APP);
				break;
			case Constants.MSG_ELECTROINICCARD_SERVER_ERROR:
				Log.i(TAG, "electroiniccard server error", Log.APP);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) 
		{
			Log.i(TAG, "receive net change broadcast", Log.APP);
			if(NetUtils.isNetWorkAvailable(context, null) && Preferences.getBoot(AppApplication.mPrefs))
			{
				Log.i(TAG, "net is available", Log.APP);
				Preferences.setBoot(AppApplication.mPrefs, false);
				
				LoadElectronicCardJob job = 
						new LoadElectronicCardJob(context, mHandler);
				AppApplication.threadPool.submit(job, null, ThreadPool.MODE_CPU);
			}
		}
	}

	
	public void register(Application context)
	{
		IntentFilter netFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(this, netFilter);
	}
	
	@SuppressWarnings("deprecation")
	private void showActivateNotification()
	{
		NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification();
		
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		notification.icon = R.drawable.ic_launcher;
		notification.tickerText = mContext.getResources().getString(R.string.notification_title_activate);
		notification.when = System.currentTimeMillis(); // 立即发生此通知
		Context appContext = mContext.getApplicationContext();    
		
        Intent appUpdateIntent = new Intent(mContext, MainActivity.class);
        appUpdateIntent.putExtra("currentItem", 2);
        appUpdateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        PendingIntent pendingIntent =PendingIntent.getActivity(mContext,
				3, appUpdateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(appContext, 
        		mContext.getResources().getString(R.string.notification_title_activate),
        		mContext.getResources().getString(R.string.notification_summary_activate), pendingIntent);
        
        notificationManager.notify(R.drawable.ic_launcher, notification);
	}
}

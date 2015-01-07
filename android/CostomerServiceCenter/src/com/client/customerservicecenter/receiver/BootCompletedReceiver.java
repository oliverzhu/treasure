package com.client.customerservicecenter.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Log;
import com.client.customerservicecenter.util.Preferences;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/11/17
 */
public class BootCompletedReceiver extends BroadcastReceiver {
	private static final String TAG = "BootCompletedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) 
		{
			Log.i(TAG, "send broadcast", Log.APP);
			if(!Preferences.getBoot(AppApplication.mPrefs))
			{
				Preferences.setBoot(AppApplication.mPrefs, true);
				Intent alarmIntent = new Intent();
				alarmIntent.setAction(Constants.ACTION_COUNTDOWNTIMER_RECEIVER);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
						alarmIntent, 0);
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
				
		        alarmManager.set(AlarmManager.RTC, 
		        		System.currentTimeMillis() + Constants.COUNTDOWNINTERVAL,pendingIntent);
			}
		}
	}

}

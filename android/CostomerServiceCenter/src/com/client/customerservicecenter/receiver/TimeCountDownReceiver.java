package com.client.customerservicecenter.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Log;
import com.client.customerservicecenter.util.Preferences;

/**
 * 记录开机时间
 * @author jianwen.zhu
 * @since 2014/10/14
 */
public class TimeCountDownReceiver extends BroadcastReceiver {
	private static final String TAG = "TimeCountDownReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals(Constants.ACTION_COUNTDOWNTIMER_RECEIVER))
		{
			Log.i(TAG, "receive broadcast", Log.APP);
			Preferences.setBootTime(AppApplication.mPrefs, System.currentTimeMillis());
		}
	}

}

package com.client.customerservicecenter.application;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.client.customerservicecenter.db.DBHelperImpl;
import com.client.customerservicecenter.db.ServiceOutletsDBManager;
import com.client.customerservicecenter.hub.BaiduMapHub;
import com.client.customerservicecenter.hub.BitmapHub;
import com.client.customerservicecenter.receiver.DeviceNetStatusReceiver;
import com.client.customerservicecenter.util.Log;
import com.client.customerservicecenter.util.thread.ThreadPool;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/8/29
 */
public class AppApplication extends Application {
	private static final String TAG = "AppApplication";
	public static SharedPreferences mPrefs;
	public static DBHelperImpl dbHelper;
	public static ServiceOutletsDBManager dbManager;
	public static ThreadPool threadPool;
	public static BitmapHub bitmapHub;
	public static BaiduMapHub baiduMapHub;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate", Log.APP);
		//获取配置文件工具
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		//数据库工具
		dbHelper = new DBHelperImpl(this);
		
		threadPool = new ThreadPool();
		
		bitmapHub = new BitmapHub();
		
		dbManager = new ServiceOutletsDBManager(this);
		
		baiduMapHub = new BaiduMapHub(this);
		
		new DeviceNetStatusReceiver().register(this);
	}
}

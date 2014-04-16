package com.home;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.home.hub.AppInteractionHub;
import com.home.provider.DBHelperImpl;

public class AppApplication extends Application {
	public static SharedPreferences mPrefs;
	public static DBHelperImpl dbHelper;
	public static AppInteractionHub appInteractionHub;
	@Override
	public void onCreate() {
		super.onCreate();
		//获取配置文件工具
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this); 
		//数据库工具
		dbHelper = new DBHelperImpl(this);
		
		appInteractionHub = new AppInteractionHub();
	}
}

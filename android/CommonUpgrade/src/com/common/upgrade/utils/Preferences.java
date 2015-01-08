package com.common.upgrade.utils;

import com.common.upgrade.bean.UpgradeInfo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * @author jianwen.zhu
 * 2014/11/3
 */
public class Preferences {
	public static final String PREFERENCES_NAME = "com_upgrade_manager";
	public static final String KEY_DOWNLOAD_ID = "downloadId";
	protected static final String KEY_APPKEY  = "key";
	protected static final String KEY_VERSION  = "version";
	protected static final String KEY_URL  = "url";
	protected static final String KEY_DESCRIPTION  = "description";
	protected static final String KEY_DOWNLOAD_SIZE  = "downloadSize";
	protected static final String KEY_DOWNLOAD_PATH  = "download_path";
	
	public static void setDownloadId(Context context,long downloadId)
    {
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	SharedPreferences.Editor editor=pref.edit();
    	editor.putLong(KEY_DOWNLOAD_ID, downloadId);
    	editor.commit();
    }
    
    public static long getDownloadId(Context context)
    {
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	return pref.getLong(KEY_DOWNLOAD_ID, -1);
    }
    
    public static void setUpgradeInfo(Context context,UpgradeInfo upgrade)
    {
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	SharedPreferences.Editor editor=pref.edit();
    	editor.putString(KEY_APPKEY, upgrade.getKey());
    	editor.putString(KEY_VERSION, upgrade.getVersion());
    	editor.putString(KEY_URL, upgrade.getUrl());
    	editor.putString(KEY_DESCRIPTION, upgrade.getDescription());
    	editor.putLong(KEY_DOWNLOAD_SIZE, upgrade.getDownloadSize());
    	editor.commit();
    }
    
    public static UpgradeInfo getUpgradeInfo(Context context)
    {
    	UpgradeInfo upgradeInfo = new UpgradeInfo();
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	upgradeInfo.setKey(pref.getString(KEY_APPKEY, ""));
    	upgradeInfo.setVersion(pref.getString(KEY_VERSION, ""));
    	upgradeInfo.setUrl(pref.getString(KEY_URL, ""));
    	upgradeInfo.setDescription(pref.getString(KEY_DESCRIPTION, ""));
    	upgradeInfo.setDownloadSize(pref.getLong(KEY_DOWNLOAD_SIZE, -1));
    	return upgradeInfo;
    }
    
    public static void setDownloadPath(Context context,String downloadPath)
    {
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	SharedPreferences.Editor editor=pref.edit();
    	editor.putString(KEY_DOWNLOAD_PATH, downloadPath);
    	editor.commit();
    }
    
    public static String getDownloadPath(Context context)
    {
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	return pref.getString(KEY_DOWNLOAD_PATH, "");
    }
    
    public static void removeAll(Context context)
    {
    	SharedPreferences pref = 
    			context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_APPEND);
    	SharedPreferences.Editor editor = pref.edit();
    	editor.clear();
    	editor.commit();
    }
}
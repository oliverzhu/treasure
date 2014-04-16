package com.home.preference;

import android.content.SharedPreferences;

/**
 * 
 * @author jianwen.zhu
 * 2013/4/9
 */
public class Preferences {
	public static final String PREFERENCE_DOWNLOAD_ID = "downloadId";
	
	public static void setDownloadId(SharedPreferences preferences,long downloadId)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putLong(PREFERENCE_DOWNLOAD_ID, downloadId);
    	editor.commit();
    }
    
    public static long getDownloadId(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return -1;
    	}
    	return preferences.getLong(PREFERENCE_DOWNLOAD_ID, -1);
    }
}

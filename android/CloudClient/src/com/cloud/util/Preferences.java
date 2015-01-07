package com.cloud.util;

import android.content.SharedPreferences;

/**
 * 
 * @author jianwen.zhu
 * 2014/4/2
 */
public class Preferences {
    public static final String PREFERENCE_AUTHORIZE_TIME = "authorizeTime";
    public static final String PREFERENCE_AUTHORIZE_STATE = "authorizeState";
    
    public static void setAuthorizeTime(SharedPreferences preferences,long time) {
        if(preferences == null) {
            return;
        }
        SharedPreferences.Editor editor=preferences.edit();
        editor.putLong(PREFERENCE_AUTHORIZE_TIME, time);
        editor.commit();
    }
    
    public static long getAuthorizeTime(SharedPreferences preferences) {
        if(preferences == null) {
            return -1;
        }
        return preferences.getLong(PREFERENCE_AUTHORIZE_TIME, -1);
    }
    
    public static void setAuthorizeState(SharedPreferences preferences,boolean state) {
        if(preferences == null) {
            return;
        }
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean(PREFERENCE_AUTHORIZE_TIME, state);
        editor.commit();
    }
    
    public static boolean getAuthorizeState(SharedPreferences preferences) {
        if(preferences == null) {
            return false;
        }
        return preferences.getBoolean(PREFERENCE_AUTHORIZE_TIME, false);
    }
}

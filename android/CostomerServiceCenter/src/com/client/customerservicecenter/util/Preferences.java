package com.client.customerservicecenter.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.SharedPreferences;

/**
 * 
 * @author jianwen.zhu
 * 2014/8/28
 */
public class Preferences {
    public static final String PREFERENCE_PHONE = "phone";
    public static final String PREFERENCE_EMAIL = "email";
    public static final String PREFERENCE_BOOT = "boot";
    public static final String PREFERENCE_BOOT_TIME = "boot_time";
    public static final String PREFERENCE_CHECKED_STATE = "check_state";
    public static final String PREFERENCE_LOCATED_CITY = "located_city";
    public static final String PREFERENCE_DB_VERSION = "db_version";
    public static final String PREFERENCE_OUTLET_TIME = "outlet_time";
    
    /** User Info */
    public static final String PREFERENCE_USERKEY = "userkey";
    public static final String PREFERENCE_UID = "uid";
    public static final String PREFERENCE_BIRTHDAY = "birthday";
    public static final String PREFERENCE_HOMETOWN = "hometown";
    public static final String PREFERENCE_NICKNAME = "nickname";
    public static final String PREFERENCE_PROFESSION = "profession";
    public static final String PREFERENCE_COUNTRY = "country";
    public static final String PREFERENCE_CITY = "city";
    public static final String PREFERENCE_ADDRESS = "address";
    public static final String PREFERENCE_PROVINCE = "province";
    public static final String PREFERENCE_GENDER = "gender";
    public static final String PREFERENCE_MOBILE = "mobile";
    public static final String PREFERENCE_FIELD1 = "field1";
    public static final String PREFERENCE_FIELD2 = "field2";
    public static final String PREFERENCE_FIELD3 = "field3";
    public static final String PREFERENCE_FIELD4 = "field4";
    public static final String PREFERENCE_FIELD5 = "field5";
    public static final String PREFERENCE_ACCESSID = "accessid";
    public static final String PREFERENCE_BUCKETNAME = "bucketname";
    public static final String PREFERENCE_SECRETKEY = "secretkey";
    public static final String PREFERENCE_OSSTYPE = "osstype";
    public static final String PREFERENCE_OSSLOCAL = "osslocal";
    
    public static void setPhoneNumber(SharedPreferences preferences,String phoneNumber)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putString(PREFERENCE_PHONE, phoneNumber);
    	editor.commit();
    }
    
    public static String getPhoneNumber(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return "";
    	}
    	return preferences.getString(PREFERENCE_PHONE, "");
    }
    
    public static void setEmail(SharedPreferences preferences,String email)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putString(PREFERENCE_EMAIL, email);
    	editor.commit();
    }
    
    public static String getEmail(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return "";
    	}
    	return preferences.getString(PREFERENCE_EMAIL, "");
    }
    
    public static void setBoot(SharedPreferences preferences,boolean boot)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putBoolean(PREFERENCE_BOOT, boot);
    	editor.commit();
    }
    
    public static boolean getBoot(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return false;
    	}
    	return preferences.getBoolean(PREFERENCE_BOOT, false);
    }
    
    public static void setBootTime(SharedPreferences preferences,long bootTime)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putLong(PREFERENCE_BOOT_TIME, bootTime);
    	editor.commit();
    }
    
    public static long getBootTime(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return -1;
    	}
    	return preferences.getLong(PREFERENCE_BOOT_TIME, -1);
    }
    
    public static void setOutletTime(SharedPreferences preferences,long outletTime)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putLong(PREFERENCE_OUTLET_TIME, outletTime);
    	editor.commit();
    }
    
    public static long getOutletTime(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return -1;
    	}
    	return preferences.getLong(PREFERENCE_OUTLET_TIME, -1);
    }
    
    public static void setCheckedState(SharedPreferences preferences,boolean checkedState)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putBoolean(PREFERENCE_CHECKED_STATE, checkedState);
    	editor.commit();
    }
    
    public static boolean getCheckedState(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return false;
    	}
    	return preferences.getBoolean(PREFERENCE_CHECKED_STATE, false);
    }
    
    public static void setLocatedCity(SharedPreferences preferences,String locatedCity)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putString(PREFERENCE_LOCATED_CITY, locatedCity);
    	editor.commit();
    }
    
    public static String getLocatedCity(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return "";
    	}
    	return preferences.getString(PREFERENCE_LOCATED_CITY, "");
    }
    
    public static void setDatabaseVersion(SharedPreferences preferences,int version)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.putInt(PREFERENCE_DB_VERSION, version);
    	editor.commit();
    }
    
    public static Integer getDatabaseVersion(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return 0;
    	}
    	return preferences.getInt(PREFERENCE_DB_VERSION, 0);
    }
    
    @SuppressWarnings({"rawtypes"})
    public static void setUserInfo(SharedPreferences preferences,Map userInfoMap)
    {
    	if(preferences == null)
    	{
    		return;
    	}
    	SharedPreferences.Editor editor=preferences.edit();
    	editor.clear();
    	Set set = userInfoMap.entrySet();
    	Iterator it = set.iterator();
    	while(it.hasNext())
    	{
    		Map.Entry entry = (Entry) it.next();
    		if(entry.getValue() != null)
    		{
    			String value = (String) entry.getValue();
    			if(value.trim().length() != 0 && !"null".equals(value))
    			{
    				editor.putString((String) entry.getKey(), value);
    			}
    		}
    	}
    	editor.commit();
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map getUserInfo(SharedPreferences preferences)
    {
    	if(preferences == null)
    	{
    		return null;
    	}
    	Map userInfo = new HashMap();
    	userInfo.put(PREFERENCE_USERKEY, preferences.getString(PREFERENCE_USERKEY, "-1"));
    	userInfo.put(PREFERENCE_UID, preferences.getString(PREFERENCE_UID, ""));
    	userInfo.put(PREFERENCE_BIRTHDAY, preferences.getString(PREFERENCE_BIRTHDAY, ""));
    	userInfo.put(PREFERENCE_HOMETOWN, preferences.getString(PREFERENCE_HOMETOWN, ""));
    	userInfo.put(PREFERENCE_NICKNAME, preferences.getString(PREFERENCE_NICKNAME, ""));
    	userInfo.put(PREFERENCE_PROFESSION, preferences.getString(PREFERENCE_PROFESSION, ""));
    	userInfo.put(PREFERENCE_COUNTRY, preferences.getString(PREFERENCE_COUNTRY, ""));
    	userInfo.put(PREFERENCE_CITY, preferences.getString(PREFERENCE_CITY, ""));
    	userInfo.put(PREFERENCE_ADDRESS, preferences.getString(PREFERENCE_ADDRESS, ""));
    	userInfo.put(PREFERENCE_PROVINCE, preferences.getString(PREFERENCE_PROVINCE, ""));
    	userInfo.put(PREFERENCE_GENDER, preferences.getString(PREFERENCE_GENDER, ""));
    	userInfo.put(PREFERENCE_MOBILE, preferences.getString(PREFERENCE_MOBILE, ""));
    	userInfo.put(PREFERENCE_EMAIL, preferences.getString(PREFERENCE_EMAIL, ""));
    	userInfo.put(PREFERENCE_FIELD1, preferences.getString(PREFERENCE_FIELD1, ""));
    	userInfo.put(PREFERENCE_FIELD2, preferences.getString(PREFERENCE_FIELD2, ""));
    	userInfo.put(PREFERENCE_FIELD3, preferences.getString(PREFERENCE_FIELD3, ""));
    	userInfo.put(PREFERENCE_FIELD4, preferences.getString(PREFERENCE_FIELD4, ""));
    	userInfo.put(PREFERENCE_FIELD5, preferences.getString(PREFERENCE_FIELD5, ""));
    	userInfo.put(PREFERENCE_ACCESSID, preferences.getString(PREFERENCE_ACCESSID, ""));
    	userInfo.put(PREFERENCE_BUCKETNAME, preferences.getString(PREFERENCE_BUCKETNAME, ""));
    	userInfo.put(PREFERENCE_SECRETKEY, preferences.getString(PREFERENCE_SECRETKEY, ""));
    	userInfo.put(PREFERENCE_OSSTYPE, preferences.getString(PREFERENCE_OSSTYPE, ""));
    	userInfo.put(PREFERENCE_OSSLOCAL, preferences.getString(PREFERENCE_OSSLOCAL, ""));
    	return userInfo;
    }
    
}

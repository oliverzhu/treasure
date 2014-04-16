package com.home.theme;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.home.R;

public class ThemeUtils {
	 private static int sTheme;  
	  
	    public final static int THEME_DEFAULT = 0;  
	    public final static int THEME_LIGHT = 1;  
	    public final static int THEME_DARK = 2;  
	  
	    /** 
	     * Set the theme of the Activity, and restart it by creating a new Activity 
	     * of the same type. 
	     */  
	    public static void changeToTheme(FragmentActivity activity, int theme)  
	    {  
	        sTheme = theme;  
	        
	        activity.finish();
	  
	        activity.startActivity(new Intent(activity, activity.getClass()));  
	    }  
	  
	    /** Set the theme of the activity, according to the configuration. */  
	    public static void onActivityCreateSetTheme(FragmentActivity activity)
	    {  
	        switch (sTheme)  
	        {  
	        default:  
	        case 1:  
	          activity.setTheme(R.style.Light);  
	            break;  
	        case 2:  
	            activity.setTheme(R.style.Dark);  
	            break;  
	        }  
	    }  
}

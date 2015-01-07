package com.cloud.util.log;

import android.util.Log;

import com.cloud.client.CloudUtil;

public class LogBase {
    private String mTag;
    
    public LogBase(String baseTag, String tag) {
        mTag = String.format("%s.%s", baseTag, tag);
    }
    
    public void w(String format, Object... args) {
        if (CloudUtil.DEBUG) {
            Log.w(mTag, String.format(format, args));
        }
    }
    
    public void i(String format, Object... args) {
        if (CloudUtil.DEBUG) {
            Log.i(mTag, String.format(format, args));
        }
    }
    
    public void d(String format, Object... args) {
        if (CloudUtil.DEBUG) {
            Log.d(mTag, String.format(format, args));
        }
    }
    
    public void e(String format, Object... args) {
        if (CloudUtil.DEBUG) {
            Log.e(mTag, String.format(format, args));
        }
    }
    
    public void v(String format, Object... args) {
        if (CloudUtil.DEBUG) {
            Log.v(mTag, String.format(format, args));
        }
    }
    
    public void w(String message) {
        if (CloudUtil.DEBUG) {
            Log.w(mTag, message);
        }
    }
    
    public void i(String message) {
        if (CloudUtil.DEBUG) {
            Log.i(mTag, message);
        }
    }
    
    public void d(String message) {
        if (CloudUtil.DEBUG) {
            Log.d(mTag, message);
        }
    }
    
    public void e(String message) {
        if (CloudUtil.DEBUG) {
            Log.e(mTag, message);
        }
    }
    
    public void v(String message) {
        if (CloudUtil.DEBUG) {
            Log.v(mTag, message);
        }
    }
}

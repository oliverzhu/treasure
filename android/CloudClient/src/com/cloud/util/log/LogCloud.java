package com.cloud.util.log;

import com.cloud.client.CloudUtil;

public class LogCloud extends LogBase {
    
    public LogCloud(String tag) {
        super("CloudClientService", tag);
    }
    
    public void i(String method, String format, Object... args) {
        if (CloudUtil.DEBUG) {
            super.i("[%s()]%s", method, String.format(format, args));
        }
    }

    public void d(String method, String format, Object... args) {
        if (CloudUtil.DEBUG) {
            super.d("[%s()]%s", method, String.format(format, args));
        }
    }
    
    public void e(String method, String format, Object... args) {
        if (CloudUtil.DEBUG) {
            super.e("[%s()]%s", method, String.format(format, args));
        }
    }
    
    public void v(String method, String format, Object... args) {
        if (CloudUtil.DEBUG) {
            super.v("[%s()]%s", method, String.format(format, args));
        }
    }
    
    public void w(String method, String format, Object... args) {
        if (CloudUtil.DEBUG) {
            super.w("[%s()]%s", method, String.format(format, args));
        }
    }

    public void i(String method, String message) {
        if (CloudUtil.DEBUG) {
            super.i("[%s()]%s", method, message);
        }
    }

    public void d(String method, String message) {
        if (CloudUtil.DEBUG) {
            super.d("[%s()]%s", method, message);
        }
    }
    
    public void e(String method, String message) {
        if (CloudUtil.DEBUG) {
            super.e("[%s()]%s", method, message);
        }
    }
    
    public void v(String method, String message) {
        if (CloudUtil.DEBUG) {
            super.v("[%s()]%s", method, message);
        }
    }
    
    public void w(String method, String message) {
        if (CloudUtil.DEBUG) {
            super.w("[%s()]%s", method, message);
        }
    }
    
    public void i(String message) {
        if (CloudUtil.DEBUG) {
            super.i(message);
        }
    }

    public void d(String message) {
        if (CloudUtil.DEBUG) {
            super.d(message);
        }
    }
    
    public void e(String message) {
        if (CloudUtil.DEBUG) {
            super.e(message);
        }
    }
    
    public void v(String message) {
        if (CloudUtil.DEBUG) {
            super.v(message);
        }
    }
    
    public void w(String message) {
        if (CloudUtil.DEBUG) {
            super.w(message);
        }
    }
}

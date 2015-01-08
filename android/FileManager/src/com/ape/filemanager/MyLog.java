package com.ape.filemanager;

public class MyLog
{
    public static final boolean DEBUG = true;
    public static final boolean DATA_DEBUG = true;
    public static final boolean APP_DEBUG = true;
    public static final boolean DISPLAY_DEBUG = false;

    public static final String TAG = "fileManager";

    public static void v(String tag, String msg)
    {
        if (DEBUG)
        {
            android.util.Log.v(tag, msg);
        }
    }

    public static void v(String msg)
    {
        v(TAG, msg);
    }

    public static void d(String tag, String msg)
    {
        if (DEBUG)
        {
            android.util.Log.d(tag, msg);
        }
    }

    public static void d(String msg)
    {
        d(TAG, msg);
    }

    public static void i(String tag, String msg)
    {
        if (DEBUG)
        {
            android.util.Log.i(tag, msg);
        }
    }

    public static void i(String msg)
    {
        i(TAG, msg);
    }

    public static void w(String tag, String msg)
    {
        if (DEBUG)
        {
            android.util.Log.w(tag, msg);
        }
    }

    public static void w(String msg)
    {
        w(TAG, msg);
    }

    public static void e(String tag, String msg)
    {
        if (DEBUG)
        {
            android.util.Log.e(tag, msg);
        }
    }

    public static void e(String msg)
    {
        e(TAG, msg);
    }
}

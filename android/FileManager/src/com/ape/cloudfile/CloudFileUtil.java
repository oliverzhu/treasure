package com.ape.cloudfile;

import java.io.File;

import com.ape.filemanager.Util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

public class CloudFileUtil
{
    public static final String REFER_APK = "com.ape.onelogin";
    public static final String LOGIN_SERVICER_NAME = "com.ape.onelogin.service.CloudSdkService";
    public static final String LOGIN_ACTIVITY_ACTION = "com.ape.onelogin.login.core.Action.ONELOGIN_ACCESS";
    
    public static final String CLOUD_STORAGE_ACTION = "com.ape.filemanager.cloudstorage";
    public static final String CLOUD_STORAGE_EXTRA_PATH_TAG = "cloud_path";
    public static final String CLOUD_STORAGE_FROM_OTHER_APP = "from_other_app";
    public static final String CLOUD_GALLERY_BACKUP_DIR = "/CloudGallery/";
    public static final String CLOUD_GALLERY_BACKUP_DIR_NAME = "CloudGallery";

    public static final String ASKER_APPKEY = "a785c1cc81b195d99f6eaf4a24cfa72d";
    public static final String OWNER_APPKEY = "a785c1cc81b195d99f6eaf4a24cfa72d";
    
    public static final String PACKAGE_NAME = "com.ape.filemanager";
    public static final String CLOUD_ROOT_DIR = "/";
    public static final String CLOUD_SYSTEM_DIR = "/cloud_system/";
    public static final String LOCAL_CACHE_DIR = ".sync";
    public static final String DOWNLOAD_PATH_SUFFIX = "/CloudStorage";

    public static final String USER_MAP_TYPE_KEY = "sdk_type";
    public static final String USER_MAP_TYPE_ANONYMOUS = "anonymous";
    public static final String USER_MAP_TYPE_UNKNOWN = "unknown";

    public static final int CLOUD_IMAGE_THUMB_WIDTH = 320;
    public static final int CLOUD_IMAGE_THUMB_HEIGHT = 480;
    public static final int MIN_CLOUD_IMAGE_SIZE = 50*1024;
    
    public static final int MSG_GET_USER_KEY = 1;
    public static final int MSG_GET_USER_STATUS =2;
    public static final int MSG_UPDATE_UI = 3;
    public static final int MSG_LOAD_LIST = 4;
    public static final int MSG_REFRESH_LIST = 5;
    public static final int MSG_SET_PROGRESS_STATUS = 6;
    public static final int MSG_REFRESHED_LIST = 7;
    
    public static final long CACHE_UPDATE_TIME = 2*3600*1000l;
    
    public static final int DIALOG_ID_NO_WIFI = 100;
    public static final int DIALOG_ID_VIEW_CLOUD_FILE = 101;
    
    public static final int UPLOAD_TAB_INDEX = 0;
    public static final int DOWNLOAD_TAB_INDEX = 1;
    public static final int MAX_TRANSFER_TABS = 2;
    public static final String FRAGMENT_UPLOAD = "uplaod_fragment";
    public static final String FRAGMENT_DOWNLOAD = "downlaod_fragment";

    public static final String FOCUS_TAB_INDEX = "focus_tab";
    public static final String USER_KEY_TAG= "user_key";
    public static final String TRANSFER_SRC_FILES_TAG = "src_files";
    public static final String TRANSFER_DEST_PATH_TAG = "dest_path";
    
    public static final String VIEW_CLOUD_IMAGE_TAG = "view_cloud_image_path";
    public static final String VIEW_ONE_IMAGE_MODE_TAG = "view_one_image";
    
    public static final int TRANSFER_LIST_TITLE_ID = -1;
    
    public static final int MAX_TRANSFER_TASK = 2;
    public static final long MAX_DEFAULT_USER_CLOUD_SPACE = 2*1024*1024*1024l;


    public static String getLocalCachePath(String cloudPath)
    {
        StringBuilder path = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
        path.append(File.separator).append(LOCAL_CACHE_DIR);
        path.append(cloudPath);
        
        String localPath = path.toString();
        File dir = new File(Util.getPathFromFilepath(localPath));
        if (!dir.exists())
        {
            dir.mkdirs();
        }

        return localPath;
    }

    public static String getCloudThumbPath(String cloudPath)
    {
        if (cloudPath.startsWith("/"))
        {
            return CLOUD_SYSTEM_DIR + cloudPath.substring(1);
        } else
        {
            return CLOUD_SYSTEM_DIR + cloudPath;
        }
    }
    
    public static String getDownloadPath()
    {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        return sdPath + DOWNLOAD_PATH_SUFFIX;
    }

    public static boolean isNetWorkEnable(Context context)
    {
        boolean result = false;
        ConnectivityManager connectMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo();
        if (mNetworkInfo != null)
        {
            result = mNetworkInfo.isAvailable();
        }

        return result;
    }
}

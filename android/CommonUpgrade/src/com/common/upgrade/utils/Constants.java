package com.common.upgrade.utils;



/**
 * 
 * @author jianwen.zhu
 * 2014/4/28
 */
public class Constants {
//	public static final String NET_BASE = "http://192.168.33.101:8080/newota";
	public static final String NET_BASE = "http://dev.jstinno.com:8080/uploadapk";
	
	/**查看新版本URL */
	public static final String  UPGRADE_VERSION_URL = "/APKCheckVersion";
	
	/**查看版本参数 */
	public static final String  UPGRADE_VERSION_PARAMS = "?appKey=#&versionCode=#&imei=#";
	
	public static final String DOWNLOAD_FILE_PATH = "/upgrade";
	
	public static final int MSG_HAVA_NEW_VERSION = 1;
	public static final int MSG_NO_NEW_VERSION = 2;
	public static final int MSG_START_DOWNLOAD = 3;
	public static final int MSG_VERSION_RESULT = 4;
	public static final int MSG_NET_ERROR = 5;
	
	public static final int MUST_UPDATE = 1;
	public static final int NOT_MUST_UPDATE = 0;
	
	public static final String APK_SUFFIX = ".apk";
	
	public static final Integer DOWNLOAD_STATUS_RUNNING = 1;
	
	public static final String ERROR_CODE_NET = "net_error";
}

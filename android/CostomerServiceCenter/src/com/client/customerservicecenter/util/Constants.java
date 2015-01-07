package com.client.customerservicecenter.util;

import com.client.customerservicecenter.bean.Picture;


/**
 * 
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public class Constants {
	public static final String URL_PREFIX = "http://dev.jstinno.com:8080/ADMServer";
//	public static final String URL_PREFIX = "http://HT178-WIN7.JSTINNO:8080/ADMServer";
//	public static final String URL_PREFIX = "http://dev.jstinno.com:8080/CustomerService";
//	public static final String URL_PREFIX = "http://HT178-WIN7.JSTINNO:8080/CustomerService";
	
	/**提交反馈 */
	public static final String URL_FEEDBACK_SUBMIT = URL_PREFIX + "/serverInfo!saveFeedbackData.action";
	
	/**根据用户imei获取反馈 */
	public static final String URL_FEEDBACK_DATA_BY_IMEI = URL_PREFIX + "/serverInfo!getFeedbackInfoJson.action?imei=#";
	
	/**根据用户id获取反馈 */
	public static final String URL_FEEDBACK_DATA_BY_UID = URL_PREFIX + "/serverInfo!getFeedbackInfoJsonByUid.action?uid=#";
	
	/**根据用户版本(地区)获取常见问题 */
	public static final String URL_FEEDBACK_FAQ_BY_VERSION = URL_PREFIX + "/serverInfo!getFeedbackFaqJsonByVersion.action?version=#";
	
	/**追加反馈 */
	public static final String URL_FEEDBACK_CHAT = URL_PREFIX + "/serverInfo!saveFeedbackChatData";
	
	/**服务网点 */
	public static final String URL_SERVICE_OUTLET = URL_PREFIX + "/serverInfo!getServiceOutlet";
	
	public static final String FRAGMENT_FAQ = "FRAGMENT_FAQ";
	public static final String FRAGMENT_FEEDBACK = "FRAGMENT_FEEDBACK"; 
	public static final String FRAGMENT_ELECTRONIC_WARRANTY_CARD = "FRAGMENT_ELECTRONIC_WARRANTY_CARD"; 
	public static final String FRAGMENT_MORE = "FRAGMENT_MORE";
	
	
	public static final String PREFERENCE_USER_INFO = "com_user_info";
	public static final String PREFERENCE_CUSTOMER_SERVICE = "com_customer_service";
	public static final String KEY_TABLE_EXISTS = "table_exists";
	public static final String KEY_ACTIVATE_DATE = "activated_date";
	
	public static final String ERROR_SYSTEM = "error_system";
	public static final String ERROR_BATTERY = "error_battery";
	public static final String ERROR_PHONE = "error_phone";
	public static final String ERROR_NET = "error_net";
	public static final String ERROR_APPLICATION = "error_application";
	public static final String ERROR_DATA = "error_data";
	public static final String ERROR_OTHERS = "error_others";
	public static final String SUGAR_ADVISE = "sugar_advise";
	public static final String HISTORY_FEEDBACK = "history_feedback";
	
	public static final int TYPE_SYSTEM = 1;
	public static final int TYPE_BATTERY = 2;
	public static final int TYPE_PHONE = 3;
	public static final int TYPE_NET = 4;
	public static final int TYPE_APPLICATION = 5;
	public static final int TYPE_DATA = 6;
	public static final int TYPE_OTHERS = 7;
	public static final int TYPE_SUGAR_ADVISE = 8;
	public static final int TYPE_HISTORY_FEEDBACK = 9;
	
	public static final int MSG_SHOWPROGRESSDIALOG = 1;
	public static final int MSG_CLOSEPROGRESSDIALOG = 2;
	public static final int MSG_GRIDVIEW_DATA = 3;
	public static final int MSG_NET_FEEDBACK_DATA = 4;
	public static final int MSG_NET_FEEDBACK_FAQ = 5;
	public static final int MSG_LOCAL_FEEDBACK_DATA = 6;
	public static final int MSG_LOCAL_FEEDBACK_FAQ = 7;
	public static final int MSG_UNREAD_FEEDBACK = 8;
	public static final int MSG_USER_KEY_SUCCESS = 9;
	public static final int MSG_USER_KEY_FAIL = 10;
	public static final int MSG_SUBMIT_COMMENT_FAIL_KEY = 11;
	public static final int MSG_SUBMIT_COMMENT_SUCCESS_KEY = 12;
	public static final int MSG_SUBMIT_PICTURE_SUCCESS = 13;
	public static final int MSG_SUBMIT_PICTURE_FAIL = 14;
	public static final int MSG_ACTIVATE_ELECTROINICCARD_SUCCESS = 15;
	public static final int MSG_ACTIVATE_ELECTROINICCARD_FAIL = 16;
	public static final int MSG_LOCATE_START = 17;
	public static final int MSG_LOCATE_STOP = 18;
	public static final int MSG_LOCATE_SUCCESS = 19;
	public static final int MSG_LOCATE_FAIL = 20;
	public static final int MSG_LOCATE_CITY_DATA = 21;
	public static final int MSG_LOCAL_CITY_DATA = 22;
	public static final int MSG_DELETE_UPLOAD_IMG = 23;
	public static final int MSG_ELECTROINICCARD_ACTIVATED = 24;
	public static final int MSG_ELECTROINICCARD_NOT_ACTIVATED = 25;
	public static final int MSG_SESSION_VALID = 26;
	public static final int MSG_SESSION_INVALID = 27;
	public static final int MSG_ELECTROINICCARD_SERVER_ERROR = 28;
	public static final int MSG_SERVICE_OUTLET = 29;
	public static final int MSG_RELOAD_FAIL = 30;
	
	public static final Integer SUCCESS = 0;
	public static final Integer FAIL = 1; 
	public static final Integer NET_NOT_AVAILABLE = 2; 
	
	/** image type */
	public static final int TYPE_THUMBNAIL = 1;
	public static final int TYPE_MICROTHUMBNAIL = 2;
	
	public static final int RESULT_FEEDBACK_HISTORY = 88;
	
	public static final float THRESHOLD_GRID_ITEM = 0.7f;
	public static final float THRESHOLD_PICTURE_SIZE = 10;
	public static final float THRESHOLD_LOCATE_TRYCOUNT = 20;
	
	public static final String DIR_UPLOAD_PIC_CACHE = "/pic";
	public static final String DIR_TEST_TIME_FILE = "/.csc/timeconfig.xml";
	
	/** 16:9 */
	public static final float SIZE_UPLOAD_PIC_WEIGHT = 100;//dip
	public static final float SIZE_UPLOAD_PIC_HEIGHT = 178;//dip
	
	public static final float SIZE_SHOW_THUMBNAIL_WEIGHT = 100;//dip
	public static final float SIZE_SHOW_THUMBNAIL_HEIGHT = 100;//dip
	
	public static final int SIZE_SHOW_MICROTHUMBNAIL_WEIGHT = 128;
	public static final int SIZE_SHOW_MICROTHUMBNAIL_HEIGHT = 128;
	
	public static final float SIZE_DELETE_ICON_WEIGHT = 26;//dip
	public static final float SIZE_DELETE_ICON_HEIGHT = 26;//dip
	
	public static final String ASKER_KEY = "2876de12b0378979e42ded5b32474e74";
	
	/** one year */
	public static final long EXPIRE_DATE = 31536000000L;
	/** four hour */
	public static final long COUNTDOWNINTERVAL = 14400000L;
	
	/** seven days */
	public static final long REQUEST_OUTLET_TIME = 604800000L;
	
	public static final String ACTION_COUNTDOWNTIMER_RECEIVER = "android.customerservice.action.TIME_COUNTDOWN";
	public static final String ACTION_LOGIN_USER_EDIT = "com.ape.onelogin.login.core.Action.ONELOGIN_EDIT";
	public static final String ACTION_LOGIN_USER_ACCESS = "com.ape.onelogin.login.core.Action.ONELOGIN_ACCESS";
	
	public static final String LOGIN_PACKAGE_NAME = "com.ape.onelogin";
	public static final String LOGIN_LAUNCHER_ACTIVITY_NAME = "com.ape.onelogin.myos.ui.LoginActivity";
	public static final String LOGIN_SERVICE_NAME = "com.ape.onelogin.service.CloudSdkService";
	
	public static final int ACTIVITY_CODE_REQUEST_FOR_USERID = 888;
	
	public static final int BAIDU_CODE_GPS_RESULT = 61;
	public static final int BAIDU_CODE_NET_RESULT = 161;
	
	public static final String CODE_BEIJING = "110000";
	public static final String CODE_TIANJIN = "120000";
	public static final String CODE_SHANGHAI = "310000";
	public static final String CODE_CHONGQING = "500000";
	
	
	
	public interface UnReadStateChangeListener
    {
    	void onStateChange();
    }
	
	public interface AuthenticListener {
		public void onComplete(Object obj);
		
		public void onException(int errorNo, String msg);
		
		public void onCancel();
	}
	
	public interface OnDatasourceChangeListener
    {
    	void onDatasourceChange(Picture p);
    }
}

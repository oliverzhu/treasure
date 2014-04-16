package com.home.url;

/**
 * 
 * @author Oliverzhu
 * 2013/6/18
 */
public class RequestUrlProvider {
	public static final String BASE = "http://dev.jstinno.com:8080";
	//-------用户--------
	/**推送信息 */
	public static final String  NOTIFICATION_URL = BASE + "/ADMServer/serverInfo!getOperateJson.action?cid=#&readTime=#&isDebugVersion=#";
	
}

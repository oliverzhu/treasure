package com.client.bean;

import com.cloud.client.CloudObject;

/**
 * 电子保单
 * @author jianwen.zhu
 *
 */
public class Card extends CloudObject{
	private String userKey;
	private String imei;
	/**0:not activated 1:activated */
	private int type;
	private String activateDate;
	
	public String getUserKey() {
		return userKey;
	}
	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}
	public String getImei() {
		return imei;
	}
	public void setImei(String imei) {
		this.imei = imei;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getActivateDate() {
		return activateDate;
	}
	public void setActivateDate(String activateDate) {
		this.activateDate = activateDate;
	}
}

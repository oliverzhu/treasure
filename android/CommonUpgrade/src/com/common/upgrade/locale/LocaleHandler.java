package com.common.upgrade.locale;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/29
 */ 
public abstract class LocaleHandler {
	protected String value;
	
	public abstract String getDialogTitle();
	public abstract String getProgressDialogTitle();
	public abstract String getProgressDialogMessage();
	public abstract String getToastMessage();
	
	public void setUpgradeDescription(String value)
	{
		this.value = value;
	}
	
	public String getUpgradeDescription()
	{
		return value;
	}

}

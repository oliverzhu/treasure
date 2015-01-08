package com.common.upgrade.locale;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/29
 */
public class LocaleChinese extends LocaleHandler {
	public static final String defaultLocale = "zh";
	
	@Override
	public String getDialogTitle() {
		return "发现新版本，是否升级？";
	}

	@Override
	public String getProgressDialogTitle() {
		return "检查更新";
	}

	@Override
	public String getProgressDialogMessage() {
		return "正在检查...";
	}

	@Override
	public String getToastMessage() {
		return "已是最新版本";
	}
}

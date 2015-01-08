package com.common.upgrade.core;

import com.common.upgrade.bean.UpgradeInfo;


/**
 * 
 * @author jianwen.zhu
 * @since 2014/4/28
 */
public interface UpgradeInterface {
	/**
	 * 请求服务器是否有新版本,并弹出对话框
	 * @return
	 */
	public void askForNewVersion();
	
	/**
	 * 请求服务器是否有新版本
	 * @return
	 */
	public void askForNewVersionFlag(CheckNewVersionListener checkversionListener);
	
	/**
	 * 请求服务器是否有新版本
	 * @param url
	 */
	public void askForNewVersion(String url);
	
	/**
	 * 下载新版本
	 * @return
	 */
	public void downloadNewVersion(UpgradeInfo upgradeInfo);
}

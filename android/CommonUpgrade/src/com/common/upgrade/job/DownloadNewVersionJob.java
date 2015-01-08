package com.common.upgrade.job;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.common.upgrade.bean.UpgradeInfo;
import com.common.upgrade.core.UpgradeManager;
import com.common.upgrade.utils.Constants;
import com.common.upgrade.utils.Preferences;
import com.common.upgrade.utils.thread.ThreadPool;
import com.common.upgrade.utils.thread.ThreadPool.JobContext;


@SuppressLint("NewApi")
public class DownloadNewVersionJob implements ThreadPool.Job<Void>{
	private Context mContext;
	private UpgradeInfo mUpgradeInfo;
	
	private boolean allowMobileDownload = false;
	private static final long MAX_ALLOWED_DOWNLOAD_BYTES_BY_MOBILE = 3145725;
	
	public DownloadNewVersionJob(Context context,UpgradeInfo upgradeInfo)
	{
		this.mContext = context;
		this.mUpgradeInfo = upgradeInfo;
	}
	

	@Override
	public Void run(JobContext jc) {
		try {
			if(checkApkExist())
			{
				Intent installApkIntent = new Intent();
				installApkIntent.setAction(Intent.ACTION_VIEW);
				installApkIntent.setDataAndType(Uri.parse(Preferences.getDownloadPath(mContext)),
						"application/vnd.android.package-archive");
				installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				mContext.startActivity(installApkIntent);
			}else
			{
				String apkName = mContext.getPackageName() + System.currentTimeMillis() + Constants.APK_SUFFIX;
				//系统下载程序
				final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
				
				Long recommendedMaxBytes = DownloadManager.getRecommendedMaxBytesOverMobile(mContext);
				
				//可以在移动网络下下载
				if(recommendedMaxBytes == null 
						|| recommendedMaxBytes.longValue() > MAX_ALLOWED_DOWNLOAD_BYTES_BY_MOBILE)
				{
					allowMobileDownload = true;
				}
				
				Uri uri = Uri.parse(mUpgradeInfo.getUrl());
				
				final Request request = new Request(uri);
				
				request.setNotificationVisibility(
		                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				request.setNotificationVisibility(
		                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
				
				int NETWORK_TYPE = DownloadManager.Request.NETWORK_WIFI;
				if(allowMobileDownload)
				{
					NETWORK_TYPE |= DownloadManager.Request.NETWORK_MOBILE;
				}
				request.setAllowedNetworkTypes(NETWORK_TYPE);
				request.allowScanningByMediaScanner();
				request.setShowRunningNotification(true);
				request.setVisibleInDownloadsUi(true);
				request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS + Constants.DOWNLOAD_FILE_PATH, apkName);
				request.setTitle(UpgradeManager.mAppName);
				
		    	//id 保存起来跟之后的广播接收器作对比
	        	long id = downloadManager.enqueue(request);
	        	
	        	long oldId = Preferences.getDownloadId(mContext);
	        	if(oldId != -1)
	        	{
	        		downloadManager.remove(oldId);
	        	}
	        	
	        	Preferences.removeAll(mContext);
	        	Preferences.setDownloadId(mContext, id);
	        	Preferences.setUpgradeInfo(mContext, mUpgradeInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean checkApkExist()
	{
		UpgradeInfo prefUpgradeInfo = Preferences.getUpgradeInfo(mContext);
		String version = prefUpgradeInfo.getVersion();
		String downloadPath = Preferences.getDownloadPath(mContext);
		
		if(version != null 
				&& version.trim().length() != 0 
				&& version.equals(prefUpgradeInfo.getVersion()) 
				&& downloadPath != null 
				&& downloadPath.trim().length() != 0)
		{
			String path = Uri.parse(downloadPath).getPath();
			if(path != null  && path.endsWith(Constants.APK_SUFFIX))
			{
				File file = new File(path);
				if(file.exists())
				{
					return true;
				}
			}
			
		}
		return false;
	}
}

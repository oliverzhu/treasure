package com.home.job;

import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.net.Uri;

import com.home.AppApplication;
import com.home.R;
import com.home.preference.Preferences;
import com.home.url.FileDirProvider;
import com.home.util.thread.ThreadPool;
import com.home.util.thread.ThreadPool.JobContext;

public class UpdateAPKJob implements ThreadPool.Job<Void>{
	private Context mContext;
	private String url;
	
	public UpdateAPKJob(Context context,String url)
	{
		this.mContext = context;
		this.url = url;
	}
	

	@Override
	public Void run(JobContext jc) {
		try {
			//系统下载程序
			final DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
			
			Uri uri = Uri.parse(url);
			
			final Request request = new Request(uri);
			
			request.setNotificationVisibility(
	                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			request.setNotificationVisibility(
	                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
			
			request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
			
			request.setShowRunningNotification(true);
			request.setVisibleInDownloadsUi(true);
			request.setDestinationInExternalFilesDir(
					mContext, FileDirProvider.apk, url.substring(url.lastIndexOf("/") + 1));
			request.setTitle(mContext.getResources().getString(R.string.app_name));
	    	
	    	//id 保存起来跟之后的广播接收器作对比
        	long id = downloadManager.enqueue(request);
        	Preferences.setDownloadId(AppApplication.mPrefs, id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

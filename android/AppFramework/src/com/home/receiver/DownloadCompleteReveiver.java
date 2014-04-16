package com.home.receiver;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.home.AppApplication;
import com.home.preference.Preferences;

/**
 * 下载完成通知接收器
 * @author jianwen.zhu
 *
 */
public class DownloadCompleteReveiver extends BroadcastReceiver {
	private DownloadManager downloadManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if(action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
		{
			long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
			
			if(id == Preferences.getDownloadId(AppApplication.mPrefs))
			{
				Query query = new Query();
				query.setFilterById(id);
				downloadManager = 
						(DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
				Cursor cursor = downloadManager.query(query);
				
				int columnCount = cursor.getColumnCount();
				String path = null;
				while(cursor.moveToNext())
				{
					for(int j = 0;j < columnCount;j++)
					{
						String columnName = cursor.getColumnName(j);
						String string = cursor.getString(j);
						if("local_uri".equals(columnName))
						{
							path = string;
						}
					}
				}
				cursor.close();
				
				if(path != null)
				{
					Intent installIntent = new Intent();
					installIntent.setAction(Intent.ACTION_VIEW);
					installIntent.setDataAndType(Uri.parse("file://"+path),
							"application/vnd.android.package-archive");
					installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
					context.startActivity(installIntent);	
				}
			}
		}
	}

}

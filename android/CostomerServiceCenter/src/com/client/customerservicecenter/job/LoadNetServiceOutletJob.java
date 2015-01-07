package com.client.customerservicecenter.job;

import java.util.Map;

import org.json.JSONArray;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.parser.json.NetServiceOutletParsing;
import com.client.customerservicecenter.parser.xml.ServiceOutletXmlParsing;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/12/3
 */
public  class LoadNetServiceOutletJob implements ThreadPool.Job<Void>{
	protected Context mContext;
	private Handler mHandler;
	public LoadNetServiceOutletJob(Context context,Handler handler)
	{
		this.mContext = context;
		this.mHandler = handler;
	}
	

	@Override
	public Void run(JobContext jc) {
		try {
			if(NetUtils.isNetWorkAvailable(mContext, null) && isTimeOver())
			{
				String uri = Constants.URL_SERVICE_OUTLET;
				JSONArray jsonArray = NetUtils.getJSONArrayByGet(uri);
				if(jsonArray != null && jsonArray.length() != 0)
				{
					new NetServiceOutletParsing().readJsonArray(jsonArray);
					Preferences.setOutletTime(AppApplication.mPrefs, System.currentTimeMillis());
				}
			}
			
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_SERVICE_OUTLET;
			mHandler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean isTimeOver()
	{
		long compareTime = Constants.REQUEST_OUTLET_TIME;
		long recordedTime = Preferences.getOutletTime(AppApplication.mPrefs);
		long deltaTime = System.currentTimeMillis() - recordedTime; 
		ServiceOutletXmlParsing xmlParse = ServiceOutletXmlParsing.getInstance();
		String filePath = 
				Environment.getExternalStorageDirectory().getPath() + 
				Constants.DIR_TEST_TIME_FILE;
		Map<String, String> timeIntervalMap = xmlParse.parse(filePath);
		if(timeIntervalMap != null)
		{
			compareTime = Long.parseLong(timeIntervalMap.get("interval")) * 60 * 1000;
		}
		
		if(deltaTime > compareTime)
		{
			return true;
		}
		return false;
	}
}

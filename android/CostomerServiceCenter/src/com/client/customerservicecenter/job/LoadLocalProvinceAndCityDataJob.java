package com.client.customerservicecenter.job;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.db.handler.CityHandler;
import com.client.customerservicecenter.db.handler.ProvinceHandler;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/22
 */
public  class LoadLocalProvinceAndCityDataJob implements ThreadPool.Job<Void>{
	protected Context mContext;
	private Handler mHandler;
	
	public LoadLocalProvinceAndCityDataJob(Context context,Handler handler)
	{
		this.mContext = context;
		this.mHandler = handler;
	}
	

	@Override
	public Void run(JobContext jc) {
		if(AppApplication.baiduMapHub.getProvinceMap() == null 
				|| AppApplication.baiduMapHub.getProvinceMap().size() == 0)
		{
			AppApplication.baiduMapHub.setProvinceMap(
					ProvinceHandler.getInstance().queryAllForMap());
		}
		
		if(AppApplication.baiduMapHub.getCityList() == null
				|| AppApplication.baiduMapHub.getCityList().size() == 0)
		{
			AppApplication.baiduMapHub.setCityList(CityHandler.getInstance().queryAll());
		}
		
		if(AppApplication.baiduMapHub.getCityList() != null && AppApplication.baiduMapHub.getCityList().size() != 0
				&& AppApplication.baiduMapHub.getProvinceMap() != null && AppApplication.baiduMapHub.getProvinceMap().size() != 0)
		{
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_LOCAL_CITY_DATA;
			mHandler.sendMessage(msg);
		}
		return null;
	}
}

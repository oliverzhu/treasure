package com.client.customerservicecenter.job;

import java.util.List;

import android.content.Context;

import com.client.customerservicecenter.bean.ServiceOutletInfo;
import com.client.customerservicecenter.db.handler.ServiceOutletHandler;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/21
 */
public  class LoadLocatedCityDataJob implements ThreadPool.Job<List<ServiceOutletInfo>>{
	protected Context mContext;
	protected String city;
	
	public LoadLocatedCityDataJob(Context context,String city)
	{
		this.mContext = context;
		this.city = city;
	}
	

	@Override
	public List<ServiceOutletInfo> run(JobContext jc) {
		List<ServiceOutletInfo> infos = null;
		try {
			infos = ServiceOutletHandler.getInstance().queryByCity(city);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infos;
	}
}

package com.client.customerservicecenter.job;

import java.util.List;

import android.content.Context;

import com.client.customerservicecenter.bean.CityInfo;
import com.client.customerservicecenter.bean.ServiceOutletInfo;
import com.client.customerservicecenter.db.handler.ServiceOutletHandler;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/22
 */
public  class LoadSpecificCityDataJob implements ThreadPool.Job<List<ServiceOutletInfo>>{
	protected Context mContext;
	protected CityInfo cityInfo;
	
	public LoadSpecificCityDataJob(Context context,CityInfo city)
	{
		this.mContext = context;
		this.cityInfo = city;
	}
	

	@Override
	public List<ServiceOutletInfo> run(JobContext jc) {
		List<ServiceOutletInfo> infos = null;
		try {
			if(cityInfo.getProvinceCode().equals(Constants.CODE_BEIJING) 
					|| cityInfo.getProvinceCode().equals(Constants.CODE_SHANGHAI)
					|| cityInfo.getProvinceCode().equals(Constants.CODE_TIANJIN)
				    || cityInfo.getProvinceCode().equals(Constants.CODE_CHONGQING))
			{
				infos = ServiceOutletHandler.getInstance().queryByDistinct(cityInfo.getCity());
			}else
			{
				infos = ServiceOutletHandler.getInstance().queryByCity(cityInfo.getCity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infos;
	}
}

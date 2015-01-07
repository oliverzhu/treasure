package com.client.customerservicecenter.hub;

import java.util.List;
import java.util.Map;

import android.content.Context;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.client.customerservicecenter.bean.CityInfo;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/17
 */
public class BaiduMapHub {
	/** badidu map*/
	public static LocationClient mLocationClient;
	
	/**
	 * *********定位模式*********
	 * LocationMode.Hight_Accuracy:gps,wifi，基站
	 * LocationMode.Battery_Saving:wifi,基站
	 * LocationMode.Device_Sensors:gps
	 */
	private LocationMode mLocationMode = LocationMode.Hight_Accuracy;
	
	/**
	 * **********坐标系**********
	 * gcj02
	 * bd09ll
	 * bd09
	 */
	private String mCoorType = "bd09ll";
	
	/**
	 * **********定位时间间隔******
	 */
	private int mSpan = 3000;
	
	public BDLocationListener mMyLocationListener;
	
	private Map<Long,String> mProvinceMap;
	
	private List<CityInfo> mCityList;
	
	public BaiduMapHub(Context context)
	{
		mLocationClient = new LocationClient(context.getApplicationContext());
	}
	
	public void start()
	{
		if(mMyLocationListener != null)
		{
			LocationClientOption option = new LocationClientOption();
			option.setLocationMode(mLocationMode);//设置定位模式
			option.setCoorType(mCoorType);//返回的定位结果是百度经纬度，默认值gcj02
			option.setScanSpan(mSpan);//设置发起定位请求的间隔时间为5000ms
			option.setIsNeedAddress(true);//checkGeoLocation.isChecked()
			mLocationClient.setLocOption(option);
			mLocationClient.start();
		}
	}
	
	public void stop()
	{
		mLocationClient.stop();
	}
	
	public void setLocationMode(LocationMode locationMode)
	{
		this.mLocationMode = locationMode;
	}
	
	public void setCoorType(String coorType)
	{
		this.mCoorType = coorType;
	}
	
	public void setLocationListener(BDLocationListener listener)
	{
		this.mMyLocationListener = listener;
		mLocationClient.registerLocationListener(mMyLocationListener);
	}
	
	public void setProvinceMap(Map<Long,String> provinceMap)
	{
		mProvinceMap = provinceMap;
	}
	
	public Map<Long,String> getProvinceMap()
	{
		return mProvinceMap;
	}
	
	public void setCityList(List<CityInfo> cityList)
	{
		mCityList = cityList;
	}
	
	public List<CityInfo> getCityList()
	{
		return mCityList;
	}

}

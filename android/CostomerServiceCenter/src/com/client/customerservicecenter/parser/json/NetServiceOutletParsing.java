package com.client.customerservicecenter.parser.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.client.customerservicecenter.bean.CityInfo;
import com.client.customerservicecenter.bean.ServiceOutletInfo;
import com.client.customerservicecenter.db.handler.CityHandler;
import com.client.customerservicecenter.db.handler.ServiceOutletHandler;

public class NetServiceOutletParsing extends AbstractJsonParsing<ServiceOutletInfo> {

	@Override
	protected ServiceOutletInfo readJsonItem(JSONObject item) throws JSONException {
		JSONArray listDistrictJSON = new JSONArray(item.getString("listDistrict"));
		if(listDistrictJSON != null && listDistrictJSON.length() != 0)
		{
			for(int j = 0;j < listDistrictJSON.length();j++)
			{
				CityInfo cityInfo = new CityInfo();
				JSONObject obj = listDistrictJSON.getJSONObject(j);
				cityInfo.setCode(obj.getString("cityCode"));
				cityInfo.setCity(obj.getString("cityName"));
				cityInfo.setProvinceCode(obj.getString("provinceCode"));
				CityHandler.getInstance().add(cityInfo);
			}
		}
		
		JSONArray listServiceOutletJSON = new JSONArray(item.getString("listServiceOutlet"));
		if(listServiceOutletJSON != null && listServiceOutletJSON.length() != 0)
		{
			for(int j = 0;j < listServiceOutletJSON.length();j++)
			{
				ServiceOutletInfo outletInfo = new ServiceOutletInfo();
				JSONObject obj = listServiceOutletJSON.getJSONObject(j);
				outletInfo.setId(obj.getInt("id"));
				outletInfo.setProvince(obj.getString("provinceName"));
				outletInfo.setProvinceCode(obj.getString("provinceCode"));
				outletInfo.setCity(obj.getString("cityName"));
				outletInfo.setCityCode(obj.getString("cityCode"));
				outletInfo.setDistrict(obj.getString("regionName"));
				outletInfo.setDistrictCode(obj.getString("regionCode"));
				outletInfo.setPhone(obj.getString("phone"));
				outletInfo.setOutletName(obj.getString("outlet"));
				outletInfo.setAddress(obj.getString("address"));
				outletInfo.setFlagDelete(obj.getInt("delete"));
				outletInfo.setCid(obj.getInt("cid"));
				
				ServiceOutletHandler.getInstance().add(outletInfo);
			}
		}
		return null;
	}
	
	/**
	 * 输入InputStream
	 * 
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public ArrayList<ServiceOutletInfo> readJsonArray(JSONArray jsonArray) {
		
		try {
			for(int i = 0;i < jsonArray.length();i++)
			{
				JSONObject item = jsonArray.getJSONObject(i);
				readJsonItem(item);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}

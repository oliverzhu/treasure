/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.ServiceOutletInfo;
import com.client.customerservicecenter.db.DBHelper;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/16
 */
public class ServiceOutletHandler extends IHandler<ServiceOutletInfo> {
	private static final String ID = "f_id";
	private static final String PROVINCE = "f_province_name";
	private static final String PROVINCE_CODE = "f_province_code";
	private static final String CITY = "f_city_name";
	private static final String CITY_CODE = "f_city_code";
	private static final String DISTRICT = "f_region_name";
	private static final String DISTRICT_CODE = "f_region_code";
	private static final String OUTLET_NAME = "f_outlet";
	private static final String PHONE = "f_phone";
	private static final String ADDRESS = "f_address";
	private static final String FLAGDELETE = "f_delete";
	private static final String CID = "f_cid";
	
	private static final String TABLE_NAME = "tb_serviceoutlet";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + PROVINCE + " DESC";
	private static final String SQL_QUERY_BY_CITY = "SELECT * FROM " + TABLE_NAME +  " WHERE " + CITY + "='#'";
	private static final String SQL_QUERY_BY_DISTINCT = "SELECT * FROM " + TABLE_NAME +  " WHERE " + DISTRICT + "='#'";
	
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ PROVINCE + ","
			+ PROVINCE_CODE + ","
			+ CITY + ","
			+ CITY_CODE + ","
			+ DISTRICT + ","
			+ DISTRICT_CODE + ","
			+ OUTLET_NAME + ","
			+ PHONE + ","
			+ ADDRESS + ","
			+ FLAGDELETE + ","
			+ CID + ")"
			+ " VALUES (#,'#','#','#','#','#','#','#','#','#',#,#)";
		
	private static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ PROVINCE + "='#'," 
			+ PROVINCE_CODE + "='#'," 
			+ CITY + "='#'," 
			+ CITY_CODE + "='#',"
			+ DISTRICT + "='#',"
			+ DISTRICT_CODE + "='#',"
			+ OUTLET_NAME + "='#',"
			+ PHONE + "='#',"
			+ ADDRESS + "='#',"
			+ FLAGDELETE + "=#,"
			+ CID + "=#"
			+ " WHERE " + ID + "=#";

	private ServiceOutletHandler() {
	}

	private static ServiceOutletHandler instance = new ServiceOutletHandler();

	public static ServiceOutletHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(ServiceOutletInfo obj) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return false;
		}
		ServiceOutletInfo queryBean = query(obj);
		if(queryBean == null)
		{
			List<String> params = new ArrayList<String>();
			params.add(obj.getId() + "");
			params.add(obj.getProvince());
			params.add(obj.getProvinceCode());
			params.add(obj.getCity());
			params.add(obj.getCityCode());
			params.add(obj.getDistrict());
			params.add(obj.getDistrictCode());
			params.add(obj.getOutletName());
			params.add(obj.getPhone());
			params.add(obj.getAddress());
			params.add(obj.getFlagDelete() + "");
			params.add(obj.getCid() + "");
			try {
				dbManager.executeSQL(SQL_INSERT, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			update(obj);
		}
		return true;
	}


	@Override
	public void delete(ServiceOutletInfo obj) {
		
	}

	@Override
	public void update(ServiceOutletInfo obj) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getProvince());
		params.add(obj.getProvinceCode());
		params.add(obj.getCity());
		params.add(obj.getCityCode());
		params.add(obj.getDistrict());
		params.add(obj.getDistrictCode());
		params.add(obj.getOutletName());
		params.add(obj.getPhone());
		params.add(obj.getAddress());
		params.add(obj.getFlagDelete() + "");
		params.add(obj.getCid() + "");
		params.add(obj.getId() + "");
		dbManager.executeSQL(SQL_UPDATE, params);
	}

	@Override
	public ServiceOutletInfo query(ServiceOutletInfo obj) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getId() + "");
		Cursor result = dbManager.query(SQL_QUERY, params);
		try {
			if (result != null) {
				if (result.getCount() == 0) {
					result.close();
					return null;
				}
				result.moveToFirst();
				ServiceOutletInfo app = new ServiceOutletInfo();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setProvince(result.getString(result.getColumnIndex(PROVINCE)));
				app.setProvinceCode(result.getString(result.getColumnIndex(PROVINCE_CODE)));
				app.setCity(result.getString(result.getColumnIndex(CITY)));
				app.setCityCode(result.getString(result.getColumnIndex(CITY_CODE)));
				app.setDistrict(result.getString(result.getColumnIndex(DISTRICT)));
				app.setDistrictCode(result.getString(result.getColumnIndex(DISTRICT_CODE)));
				app.setOutletName(result.getString(result.getColumnIndex(OUTLET_NAME)));
				app.setPhone(result.getString(result.getColumnIndex(PHONE)));
				app.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
				app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
				app.setCid(result.getInt(result.getColumnIndex(CID)));
				return app;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(result != null) {
				result.close();
			}
		}
		return null;
	}

	@Override
	public void batch(List<ServiceOutletInfo> addObjs,
			List<ServiceOutletInfo> updateObjs, List<ServiceOutletInfo> deleteObjs) {
		
	}

	@Override
	public List<ServiceOutletInfo> queryAll() {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<ServiceOutletInfo> beans = new ArrayList<ServiceOutletInfo>();
		Cursor result = dbManager.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			ServiceOutletInfo app = new ServiceOutletInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setProvince(result.getString(result.getColumnIndex(PROVINCE)));
			app.setProvinceCode(result.getString(result.getColumnIndex(PROVINCE_CODE)));
			app.setCity(result.getString(result.getColumnIndex(CITY)));
			app.setCityCode(result.getString(result.getColumnIndex(CITY_CODE)));
			app.setDistrict(result.getString(result.getColumnIndex(DISTRICT)));
			app.setDistrictCode(result.getString(result.getColumnIndex(DISTRICT_CODE)));
			app.setOutletName(result.getString(result.getColumnIndex(OUTLET_NAME)));
			app.setPhone(result.getString(result.getColumnIndex(PHONE)));
			app.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
			app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
			app.setCid(result.getInt(result.getColumnIndex(CID)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public List<ServiceOutletInfo> queryByCity(String city) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(city);
		Cursor result = dbManager.query(SQL_QUERY_BY_CITY, params);
		List<ServiceOutletInfo> beans = new ArrayList<ServiceOutletInfo>();
		while(result != null && result.moveToNext()){
			ServiceOutletInfo app = new ServiceOutletInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setProvince(result.getString(result.getColumnIndex(PROVINCE)));
			app.setProvinceCode(result.getString(result.getColumnIndex(PROVINCE_CODE)));
			app.setCity(result.getString(result.getColumnIndex(CITY)));
			app.setCityCode(result.getString(result.getColumnIndex(CITY_CODE)));
			app.setDistrict(result.getString(result.getColumnIndex(DISTRICT)));
			app.setDistrictCode(result.getString(result.getColumnIndex(DISTRICT_CODE)));
			app.setOutletName(result.getString(result.getColumnIndex(OUTLET_NAME)));
			app.setPhone(result.getString(result.getColumnIndex(PHONE)));
			app.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
			app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
			app.setCid(result.getInt(result.getColumnIndex(CID)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public List<ServiceOutletInfo> queryByDistinct(String distinct) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(distinct);
		Cursor result = dbManager.query(SQL_QUERY_BY_DISTINCT, params);
		List<ServiceOutletInfo> beans = new ArrayList<ServiceOutletInfo>();
		while(result != null && result.moveToNext()){
			ServiceOutletInfo app = new ServiceOutletInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setProvince(result.getString(result.getColumnIndex(PROVINCE)));
			app.setProvinceCode(result.getString(result.getColumnIndex(PROVINCE_CODE)));
			app.setCity(result.getString(result.getColumnIndex(CITY)));
			app.setCityCode(result.getString(result.getColumnIndex(CITY_CODE)));
			app.setDistrict(result.getString(result.getColumnIndex(DISTRICT)));
			app.setDistrictCode(result.getString(result.getColumnIndex(DISTRICT_CODE)));
			app.setOutletName(result.getString(result.getColumnIndex(OUTLET_NAME)));
			app.setPhone(result.getString(result.getColumnIndex(PHONE)));
			app.setAddress(result.getString(result.getColumnIndex(ADDRESS)));
			app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
			app.setCid(result.getInt(result.getColumnIndex(CID)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	

	@Override
	public void clear() {
		
	}
}

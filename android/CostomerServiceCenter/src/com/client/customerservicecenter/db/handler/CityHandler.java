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
import com.client.customerservicecenter.bean.CityInfo;
import com.client.customerservicecenter.db.DBHelper;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/22
 */
public class CityHandler extends IHandler<CityInfo> {
	private static final String ID = "id";
	private static final String CODE = "f_code";
	private static final String CITY = "f_city";
	private static final String PROVINCECODE = "f_province_code";
	
	private static final String TABLE_NAME = "tb_city";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + CODE + "='#'";
	private static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + PROVINCECODE;
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + CODE + ","
			+ CITY + ","
			+ PROVINCECODE + ")"
			+ " VALUES ('#','#','#')";

	private CityHandler() {
	}

	private static CityHandler instance = new CityHandler();

	public static CityHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(CityInfo obj) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return false;
		}
		CityInfo queryCityInfo = query(obj);
		if(queryCityInfo != null)
		{
			return false;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getCode());
		params.add(obj.getCity());
		params.add(obj.getProvinceCode());
		try {
			dbManager.executeSQL(SQL_INSERT, params);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}


	@Override
	public void delete(CityInfo obj) {
	}

	@Override
	public void update(CityInfo obj) {
	}

	@Override
	public CityInfo query(CityInfo obj) {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getCode());
		Cursor result = dbManager.query(SQL_QUERY, params);
		try {
			if (result != null) {
				if (result.getCount() == 0) {
					result.close();
					return null;
				}
				result.moveToFirst();
				CityInfo app = new CityInfo();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setCode(result.getString(result.getColumnIndex(CODE)));
				app.setCity(result.getString(result.getColumnIndex(CITY)));
				app.setProvinceCode(result.getString(result.getColumnIndex(PROVINCECODE)));
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
	public void batch(List<CityInfo> addObjs,
			List<CityInfo> updateObjs, List<CityInfo> deleteObjs) {
		
	}

	@Override
	public List<CityInfo> queryAll() {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<CityInfo> beans = new ArrayList<CityInfo>();
		Cursor result = dbManager.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			CityInfo app = new CityInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setCode(result.getString(result.getColumnIndex(CODE)));
			app.setCity(result.getString(result.getColumnIndex(CITY)));
			app.setProvinceCode(result.getString(result.getColumnIndex(PROVINCECODE)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}

	@Override
	public void clear() {
		
	}
}

/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.database.Cursor;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.ProvinceInfo;
import com.client.customerservicecenter.db.DBHelper;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/22
 */
public class ProvinceHandler extends IHandler<ProvinceInfo> {
	private static final String ID = "id";
	private static final String CODE = "f_code";
	private static final String PROVINCE = "f_province";
	
	private static final String TABLE_NAME = "tb_province";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + PROVINCE + " DESC";

	private ProvinceHandler() {
	}

	private static ProvinceHandler instance = new ProvinceHandler();

	public static ProvinceHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(ProvinceInfo obj) {
		return false;
	}


	@Override
	public void delete(ProvinceInfo obj) {
	}

	@Override
	public void update(ProvinceInfo obj) {
	}

	@Override
	public ProvinceInfo query(ProvinceInfo obj) {
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
				ProvinceInfo app = new ProvinceInfo();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setCode(result.getString(result.getColumnIndex(CODE)));
				app.setProvince(result.getString(result.getColumnIndex(PROVINCE)));
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
	public void batch(List<ProvinceInfo> addObjs,
			List<ProvinceInfo> updateObjs, List<ProvinceInfo> deleteObjs) {
		
	}

	@Override
	public List<ProvinceInfo> queryAll() {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		List<ProvinceInfo> beans = new ArrayList<ProvinceInfo>();
		Cursor result = dbManager.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			ProvinceInfo app = new ProvinceInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setCode(result.getString(result.getColumnIndex(CODE)));
			app.setProvince(result.getString(result.getColumnIndex(PROVINCE)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public Map<Long,String> queryAllForMap() {
		DBHelper dbManager = AppApplication.dbManager; 
		if (dbManager == null) {
			return null;
		}
		Map<Long,String> beans = new HashMap<Long,String>();
		Cursor result = dbManager.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			beans.put(Long.valueOf(result.getString(result.getColumnIndex(CODE))), 
					result.getString(result.getColumnIndex(PROVINCE)));
		}
		result.close(); 
		return beans;
	}

	@Override
	public void clear() {
		
	}
}

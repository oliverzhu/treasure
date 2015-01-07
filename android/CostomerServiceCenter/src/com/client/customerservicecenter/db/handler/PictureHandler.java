/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.client.customerservicecenter.bean.Picture;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class PictureHandler extends IHandler<Picture> {
	private static final String ID = "f_id";
	private static final String PATH = "f_path";
	private static final String KEY = "f_key";
	private static final String FEEDBACKID = "f_feedbackid";
	
	private static final String TABLE_NAME = "feedback_picture";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "(" + ID + " INTEGER PRIMARY KEY,"
			+ PATH + " VARCHAR(100),"
			+ KEY + " VARCHAR(100),"
			+ FEEDBACKID + " VARCHAR(100))";
	
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ PATH + ","
			+ KEY + ","
			+ FEEDBACKID + ")"
			+ " VALUES (#,'#','#','#')";
		
	private static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ PATH + "='#'," 
			+ KEY + "='#'," 
			+ FEEDBACKID + "='#'" 
			+ " WHERE " + ID + "=#";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_QUERY_ALL  = "SELECT * FROM " + TABLE_NAME;
	private static final String SQL_QUERY_BY_FEEDBACKID = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEEDBACKID +  "='#'";
	private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;

	private PictureHandler() {
	}

	private static PictureHandler instance = new PictureHandler();

	public static PictureHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(Picture obj) {
		if (dbHelper == null || obj == null) {
			return false;
		}
		
		Picture queryObj = query(obj);
		if(queryObj == null)
		{
			List<String> params = new ArrayList<String>();
			params.add(obj.getId() + "");
			params.add(obj.getPath());
			params.add(obj.getKey());
			params.add(obj.getFeedbackId());
				try {
				dbHelper.executeSQL(SQL_INSERT, params);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			update(obj);
		}
		return true;
	}

	/**
	 * 
	 */
	public  void clear() {
		if(dbHelper != null) {
			dbHelper.executeSQL(SQL_DELETE_ALL);
		}
	}

	@Override
	public void delete(Picture obj) {
		if(dbHelper ==null || obj == null){
 			return;
 		}
		Picture tmp = query(obj);
		if(tmp != null)
		{
			List<String> params = new ArrayList<String>();
			params.add(tmp.getId() + "");
			dbHelper.executeSQL(SQL_DELETE, params);
		}
	}

	@Override
	public void update(Picture obj) {
		if(dbHelper ==null || obj == null){
			return;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getPath());
		params.add(obj.getKey());
		params.add(obj.getFeedbackId());
		params.add(obj.getId() + "");
		dbHelper.executeSQL(SQL_UPDATE, params);
	}

	@Override
	public Picture query(Picture obj) {
		if (dbHelper == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getId() + "");
		Cursor result = dbHelper.query(SQL_QUERY, params);
		try {
			if (result != null) {
				if (result.getCount() == 0) {
					result.close();
					return null;
				}
				result.moveToFirst();
				Picture app = new Picture();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setPath(result.getString(result.getColumnIndex(PATH)));
				app.setKey(result.getString(result.getColumnIndex(KEY)));
				app.setFeedbackId(result.getString(result.getColumnIndex(FEEDBACKID)));
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
	public void batch(List<Picture> addObjs,
			List<Picture> updateObjs, List<Picture> deleteObjs) {
		
	}

	@Override
	public List<Picture> queryAll() {
		if(dbHelper == null){
			return null;
		}
		List<Picture> beans = new ArrayList<Picture>();
		Cursor result = dbHelper.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			Picture app = new Picture();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setPath(result.getString(result.getColumnIndex(PATH)));
			app.setKey(result.getString(result.getColumnIndex(KEY)));
			app.setFeedbackId(result.getString(result.getColumnIndex(FEEDBACKID)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public List<Picture> queryAllByFeedbackId(String feedbackId) {
		if(dbHelper == null){
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(feedbackId);
		Cursor result = dbHelper.query(SQL_QUERY_BY_FEEDBACKID, params);
		List<Picture> beans = new ArrayList<Picture>();
		while(result != null && result.moveToNext()){
			Picture app = new Picture();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setPath(result.getString(result.getColumnIndex(PATH)));
			app.setKey(result.getString(result.getColumnIndex(KEY)));
			app.setFeedbackId(result.getString(result.getColumnIndex(FEEDBACKID)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
}

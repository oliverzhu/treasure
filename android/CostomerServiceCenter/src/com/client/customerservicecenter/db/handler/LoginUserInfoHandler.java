/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.client.customerservicecenter.bean.LoginUserInfo;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class LoginUserInfoHandler extends IHandler<LoginUserInfo> {
	private static final String ID = "f_id";
	private static final String NICKNAME = "f_nickname";
	private static final String HEADIMG = "f_headimg";
	
	private static final String ACCESSID = "f_accessid";
	private static final String SECRETKEY = "f_secretkey";
	private static final String BUCKETNAME = "f_bucketname";
	private static final String OSSTYPE = "f_osstype";
	private static final String OSSLOCAL = "f_osslocal";
	
	private static final String TABLE_NAME = "feedback_user";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "(" + ID + " INTEGER PRIMARY KEY,"
			+ NICKNAME + " VARCHAR(100),"
			+ HEADIMG + " VARCHAR(100),"
			+ ACCESSID + " VARCHAR(200),"
			+ SECRETKEY + " VARCHAR(200),"
			+ BUCKETNAME + " VARCHAR(200),"
			+ OSSTYPE + " VARCHAR(50),"
			+ OSSLOCAL + " VARCHAR(50))";
	
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ NICKNAME + ","
			+ HEADIMG + ","
			+ ACCESSID + ","
			+ SECRETKEY + ","
			+ BUCKETNAME + ","
			+ OSSTYPE + ","
			+ OSSLOCAL + ")"
			+ " VALUES (#,'#','#','#','#','#','#','#')";
		
	private static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ NICKNAME + "='#'," 
			+ HEADIMG + "='#'," 
			+ ACCESSID + "='#'," 
			+ SECRETKEY + "='#'," 
			+ BUCKETNAME + "='#'," 
			+ OSSTYPE + "='#'," 
			+ OSSLOCAL + "='#'" 
			+ " WHERE " + ID + "=#";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_QUERY_ALL  = "SELECT * FROM " + TABLE_NAME;
	private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;

	private LoginUserInfoHandler() {
	}

	private static LoginUserInfoHandler instance = new LoginUserInfoHandler();

	public static LoginUserInfoHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(LoginUserInfo obj) {
		if (dbHelper == null || obj == null) {
			return false;
		}
		
		
		LoginUserInfo queryObj = query(obj);
		if(queryObj == null)
		{
			List<String> params = new ArrayList<String>();
			params.add(obj.getId() + "");
			params.add(obj.getNickName());
			params.add(obj.getHeadImg());
			params.add(obj.getAccessId());
			params.add(obj.getSecretKey());
			params.add(obj.getBucketName());
			params.add(obj.getOssType());
			params.add(obj.getOssLocal());
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
	public void delete(LoginUserInfo obj) {
		if(dbHelper ==null || obj == null){
 			return;
 		}
		LoginUserInfo tmp = query(obj);
		if(tmp != null)
		{
			List<String> params = new ArrayList<String>();
			params.add(tmp.getId() + "");
			dbHelper.executeSQL(SQL_DELETE, params);
		}
	}

	@Override
	public void update(LoginUserInfo obj) {
		if(dbHelper ==null || obj == null){
			return;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getNickName());
		params.add(obj.getHeadImg());
		params.add(obj.getAccessId());
		params.add(obj.getSecretKey());
		params.add(obj.getBucketName());
		params.add(obj.getOssType());
		params.add(obj.getOssLocal());
		params.add(obj.getId() + "");
		dbHelper.executeSQL(SQL_UPDATE, params);
	}

	@Override
	public LoginUserInfo query(LoginUserInfo obj) {
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
				LoginUserInfo app = new LoginUserInfo();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setNickName(result.getString(result.getColumnIndex(NICKNAME)));
				app.setHeadImg(result.getString(result.getColumnIndex(HEADIMG)));
				app.setAccessId(result.getString(result.getColumnIndex(ACCESSID)));
				app.setSecretKey(result.getString(result.getColumnIndex(SECRETKEY)));
				app.setBucketName(result.getString(result.getColumnIndex(BUCKETNAME)));
				app.setOssType(result.getString(result.getColumnIndex(OSSTYPE)));
				app.setOssLocal(result.getString(result.getColumnIndex(OSSLOCAL)));
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
	public void batch(List<LoginUserInfo> addObjs,
			List<LoginUserInfo> updateObjs, List<LoginUserInfo> deleteObjs) {
		
	}

	@Override
	public List<LoginUserInfo> queryAll() {
		if(dbHelper == null){
			return null;
		}
		List<LoginUserInfo> beans = new ArrayList<LoginUserInfo>();
		Cursor result = dbHelper.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			LoginUserInfo app = new LoginUserInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setNickName(result.getString(result.getColumnIndex(NICKNAME)));
			app.setHeadImg(result.getString(result.getColumnIndex(HEADIMG)));
			app.setAccessId(result.getString(result.getColumnIndex(ACCESSID)));
			app.setSecretKey(result.getString(result.getColumnIndex(SECRETKEY)));
			app.setBucketName(result.getString(result.getColumnIndex(BUCKETNAME)));
			app.setOssType(result.getString(result.getColumnIndex(OSSTYPE)));
			app.setOssLocal(result.getString(result.getColumnIndex(OSSLOCAL)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
}

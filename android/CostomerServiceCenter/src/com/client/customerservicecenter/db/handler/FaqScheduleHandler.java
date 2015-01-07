/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.client.customerservicecenter.bean.FeedbackScheduleInfo;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class FaqScheduleHandler extends IHandler<FeedbackScheduleInfo> {
	private static final String ID = "f_id";
	private static final String COMMENT = "f_comment";
	private static final String COMMENTDATE = "f_commentdate";
	private static final String FEEDBACKID = "f_feedbackid";
	private static final String IDENTITY = "f_identity";
	
	private static final String TABLE_NAME = "faq_schedule";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "(" + ID + " INTEGER PRIMARY KEY,"
			+ COMMENT + " VARCHAR(1000),"
			+ FEEDBACKID + " INTEGER,"
			+ COMMENTDATE + " VARCHAR(100),"
			+ IDENTITY + " INTEGER)";
	
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ COMMENT + ","
			+ COMMENTDATE + ","
			+ FEEDBACKID + ","
			+ IDENTITY + ")"
			+ " VALUES (#,'#','#',#,#)";
	
	private static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ COMMENT + "='#'," 
			+ COMMENTDATE + "='#'," 
			+ FEEDBACKID + "=#," 
			+ IDENTITY + "=#"
			+ " WHERE " + ID + "=#";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_QUERY_BY_FEEDBACKID  = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEEDBACKID + "='#'"+ " ORDER BY " + COMMENTDATE + " DESC";
	private static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME;
	private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;

	private FaqScheduleHandler() {
	}

	private static FaqScheduleHandler instance = new FaqScheduleHandler();

	public static FaqScheduleHandler getInstance() {
		return instance;
	}
	
	@Override
	public synchronized boolean add(FeedbackScheduleInfo obj) {
		boolean isAdd = false;
		if (dbHelper == null || obj == null) {
			return false;
		}
		
		FeedbackScheduleInfo bean = query(obj);
		if(bean == null)
		{
			List<String> params = new ArrayList<String>();
			params.add(obj.getId() + "");
			params.add(obj.getComment());
			params.add(obj.getCommentDate());
			params.add(obj.getFeedbackId() + "");
			params.add(obj.getIdentity() + "");
 			try {
				dbHelper.executeSQL(SQL_INSERT, params);
				if(obj.getIdentity() == 0)
				{
					isAdd = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else
		{
			update(obj);
		}
		
		return isAdd;
		
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
	public void delete(FeedbackScheduleInfo obj) {
		if(dbHelper ==null || obj == null){
 			return;
 		}
		FeedbackScheduleInfo tmp = query(obj);
		if(tmp != null)
		{
			List<String> params = new ArrayList<String>();
			params.add(tmp.getId() + "");
			dbHelper.executeSQL(SQL_DELETE, params);
		}
	}

	@Override
	public void update(FeedbackScheduleInfo obj) {
		if(dbHelper ==null || obj == null){
			return;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getComment());
		params.add(obj.getCommentDate());
		params.add(obj.getFeedbackId() + "");
		params.add(obj.getIdentity() + "");
		params.add(obj.getId() + "");
		dbHelper.executeSQL(SQL_UPDATE, params);
	}

	@Override
	public FeedbackScheduleInfo query(FeedbackScheduleInfo obj) {
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
				FeedbackScheduleInfo app = new FeedbackScheduleInfo();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setComment(result.getString(result.getColumnIndex(COMMENT)));
				app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
				app.setFeedbackId(result.getInt(result.getColumnIndex(FEEDBACKID)));
				app.setIdentity(result.getInt(result.getColumnIndex(IDENTITY)));
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
	public void batch(List<FeedbackScheduleInfo> addObjs,
			List<FeedbackScheduleInfo> updateObjs, List<FeedbackScheduleInfo> deleteObjs) {
		
	}

	@Override
	public List<FeedbackScheduleInfo> queryAll() {
		if(dbHelper == null){
			return null;
		}
		List<FeedbackScheduleInfo> beans = new ArrayList<FeedbackScheduleInfo>();
		Cursor result = dbHelper.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			FeedbackScheduleInfo app = new FeedbackScheduleInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setComment(result.getString(result.getColumnIndex(COMMENT)));
			app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
			app.setFeedbackId(result.getInt(result.getColumnIndex(FEEDBACKID)));
			app.setIdentity(result.getInt(result.getColumnIndex(IDENTITY)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public List<FeedbackScheduleInfo> queryAllByFeedbackId(int feedbackId) {
		if(dbHelper == null){
			return null;
		}
		List<FeedbackScheduleInfo> beans = new ArrayList<FeedbackScheduleInfo>();
		List<String> params = new ArrayList<String>();
		params.add(feedbackId + "");
		Cursor result = dbHelper.query(SQL_QUERY_BY_FEEDBACKID, params);
		while(result != null && result.moveToNext()){
			FeedbackScheduleInfo app = new FeedbackScheduleInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setComment(result.getString(result.getColumnIndex(COMMENT)));
			app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
			app.setFeedbackId(result.getInt(result.getColumnIndex(FEEDBACKID)));
			app.setIdentity(result.getInt(result.getColumnIndex(IDENTITY)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
}

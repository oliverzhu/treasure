/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.home.provider.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.home.bean.FeedbackScheduleInfo;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class FeedbackScheduleHandler extends IHandler<FeedbackScheduleInfo> {

	private FeedbackScheduleHandler() {
	}

	private static FeedbackScheduleHandler instance = new FeedbackScheduleHandler();

	public static FeedbackScheduleHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(FeedbackScheduleInfo obj) {
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
				dbHelper.executeSQL(FeedbackScheduleInfo.SQL_INSERT, params);
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
			dbHelper.executeSQL(FeedbackScheduleInfo.SQL_DELETE_ALL);
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
			dbHelper.executeSQL(FeedbackScheduleInfo.SQL_DELETE, params);
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
		dbHelper.executeSQL(FeedbackScheduleInfo.SQL_UPDATE, params);
	}

	@Override
	public FeedbackScheduleInfo query(FeedbackScheduleInfo obj) {
		if (dbHelper == null) {
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getId() + "");
		Cursor result = dbHelper.query(FeedbackScheduleInfo.SQL_QUERY, params);
		try {
			if (result != null) {
				if (result.getCount() == 0) {
					result.close();
					return null;
				}
				result.moveToFirst();
				FeedbackScheduleInfo app = new FeedbackScheduleInfo();
				app.setId(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.ID)));
				app.setComment(result.getString(result.getColumnIndex(FeedbackScheduleInfo.COMMENT)));
				app.setCommentDate(result.getString(result.getColumnIndex(FeedbackScheduleInfo.COMMENTDATE)));
				app.setFeedbackId(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.FEEDBACKID)));
				app.setIdentity(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.IDENTITY)));
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
		Cursor result = dbHelper.query(FeedbackScheduleInfo.SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			FeedbackScheduleInfo app = new FeedbackScheduleInfo();
			app.setId(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.ID)));
			app.setComment(result.getString(result.getColumnIndex(FeedbackScheduleInfo.COMMENT)));
			app.setCommentDate(result.getString(result.getColumnIndex(FeedbackScheduleInfo.COMMENTDATE)));
			app.setFeedbackId(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.FEEDBACKID)));
			app.setIdentity(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.IDENTITY)));
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
		Cursor result = dbHelper.query(FeedbackScheduleInfo.SQL_QUERY_BY_FEEDBACKID, params);
		while(result != null && result.moveToNext()){
			FeedbackScheduleInfo app = new FeedbackScheduleInfo();
			app.setId(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.ID)));
			app.setComment(result.getString(result.getColumnIndex(FeedbackScheduleInfo.COMMENT)));
			app.setCommentDate(result.getString(result.getColumnIndex(FeedbackScheduleInfo.COMMENTDATE)));
			app.setFeedbackId(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.FEEDBACKID)));
			app.setIdentity(result.getInt(result.getColumnIndex(FeedbackScheduleInfo.IDENTITY)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
}

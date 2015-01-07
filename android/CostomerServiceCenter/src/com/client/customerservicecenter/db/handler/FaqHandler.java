/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;

import com.client.customerservicecenter.bean.CommentInfo;

/**
 * 常见问题列表
 * @author jianwen.zhu
 * 2013/6/14
 */
public class FaqHandler extends IHandler<CommentInfo> {
	
	//反馈id
	private static final String ID = "f_id";
	//反馈内容
	private static final String COMMENT = "f_comment";
	//反馈类型
	private static final String TYPE = "f_type";
	//反馈时间
	private static final String COMMENTDATE = "f_commentdate";
	
	//问题解决状态
	private static final String STATUS = "f_status";
	
	//反馈查看状态
	private static final String READSTATE = "f_readstate";
	
	//用户id
	private static final String USERID = "f_uid";
	
	private static final String FLAGDELETE = "f_delete";
	
	private static final String TYPESTR = "f_typestr";
	
	//客户回复
	private static final String REPLY = "f_reply";
	
	private static final String TABLE_NAME = "faq_comment";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "(" + ID + " INTEGER PRIMARY KEY,"
			+ COMMENT + " VARCHAR(1000),"
			+ TYPE + " VARCHAR(100),"
			+ COMMENTDATE + " VARCHAR(100),"
			+ READSTATE + " INTEGER,"
			+ STATUS + " INTEGER,"
			+ USERID + " VARCHAR(45)," 
			+ FLAGDELETE +" INTEGER,"
			+ TYPESTR +" VARCHAR(45),"
			+ REPLY +" VARCHAR(1000))";
	
	private static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ COMMENT + ","
			+ TYPE + ","
			+ COMMENTDATE + ","
			+ READSTATE + ","
			+ STATUS + ","
			+ USERID + ","
			+ FLAGDELETE + ","
			+ TYPESTR + ","
			+ REPLY + ")"
			+ " VALUES (#,'#','#','#',#,#,'#',#,'#','#')";
		
	private static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ COMMENT + "='#'," 
			+ TYPE + "='#'," 
			+ COMMENTDATE + "='#'," 
			+ READSTATE + "=#,"
			+ STATUS + "=#,"
			+ FLAGDELETE + "=#,"
			+ TYPESTR + "='#',"
			+ REPLY + "='#'"
			+ " WHERE " + ID + "=#";
	
	private static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COMMENTDATE + " DESC";
	private static final String SQL_QUERY_BY_ARGS = "SELECT * FROM " + TABLE_NAME + " WHERE " + COMMENT + " LIKE '%#%'" + " OR " + TYPESTR + " LIKE '%#%'" + " OR " + REPLY + " LIKE '%#%'" + " ORDER BY " + COMMENTDATE + " DESC";
	private static final String SQL_QUERY_BY_FLAGDELETE = "SELECT * FROM " + TABLE_NAME + " WHERE " + USERID +  "='#'" + " AND " + FLAGDELETE + "='#'" + " ORDER BY " + COMMENTDATE + " DESC";
	private static final String SQL_QUERY_UNREAD_COUNT = "SELECT count(*) FROM " + TABLE_NAME + " WHERE " + READSTATE + "=#" + " AND " + USERID + "='#'"+ " AND " + FLAGDELETE + "='#'";
	private static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	private static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;

	private FaqHandler() {
	}

	private static FaqHandler instance = new FaqHandler();

	public static FaqHandler getInstance() {
		return instance;
	}
	
	@Override
	public boolean add(CommentInfo obj) {
		if (dbHelper == null || obj == null) {
			return false;
		}
		
		List<String> params = new ArrayList<String>();
		params.add(obj.getId() + "");
		params.add(obj.getComment());
		params.add(obj.getType());
		params.add(obj.getCommentDate());
		params.add(obj.getReadState() + "");
		params.add(obj.getStatus() + "");
		params.add(obj.getUserId() + "");
		params.add("0");
		params.add(obj.getTypeStr());
		params.add(obj.getReply());
			try {
			dbHelper.executeSQL(SQL_INSERT, params);
		} catch (Exception e) {
			e.printStackTrace();
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
	public void delete(CommentInfo obj) {
		if(dbHelper ==null || obj == null){
 			return;
 		}
		CommentInfo tmp = query(obj);
		if(tmp != null)
		{
			List<String> params = new ArrayList<String>();
			params.add(tmp.getId() + "");
			dbHelper.executeSQL(SQL_DELETE, params);
		}
	}

	@Override
	public void update(CommentInfo obj) {
		if(dbHelper ==null || obj == null){
			return;
		}
		List<String> params = new ArrayList<String>();
		params.add(obj.getComment());
		params.add(obj.getType());
		params.add(obj.getCommentDate());
		params.add(obj.getReadState() + "");
		params.add(obj.getStatus() + "");
		params.add(obj.getFlagDelete() + "");
		params.add(obj.getTypeStr());
		params.add(obj.getReply());
		params.add(obj.getId() + "");
		dbHelper.executeSQL(SQL_UPDATE, params);
	}

	@Override
	public CommentInfo query(CommentInfo obj) {
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
				CommentInfo app = new CommentInfo();
				app.setId(result.getInt(result.getColumnIndex(ID)));
				app.setComment(result.getString(result.getColumnIndex(COMMENT)));
				app.setType(result.getString(result.getColumnIndex(TYPE)));
				app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
				app.setReadState(result.getInt(result.getColumnIndex(READSTATE)));
				app.setStatus(result.getInt(result.getColumnIndex(STATUS)));
				app.setUserId(result.getString(result.getColumnIndex(USERID)));
				app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
				app.setTypeStr(result.getString(result.getColumnIndex(TYPESTR)));
				app.setReply(result.getString(result.getColumnIndex(REPLY)));
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
	public void batch(List<CommentInfo> addObjs,
			List<CommentInfo> updateObjs, List<CommentInfo> deleteObjs) {
		
	}

	@Override
	public List<CommentInfo> queryAll() {
		if(dbHelper == null){
			return null;
		}
		List<CommentInfo> beans = new ArrayList<CommentInfo>();
		Cursor result = dbHelper.query(SQL_QUERY_ALL);
		while(result != null && result.moveToNext()){
			CommentInfo app = new CommentInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setComment(result.getString(result.getColumnIndex(COMMENT)));
			app.setType(result.getString(result.getColumnIndex(TYPE)));
			app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
			app.setReadState(result.getInt(result.getColumnIndex(READSTATE)));
			app.setStatus(result.getInt(result.getColumnIndex(STATUS)));
			app.setUserId(result.getString(result.getColumnIndex(USERID)));
			app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
			app.setTypeStr(result.getString(result.getColumnIndex(TYPESTR)));
			app.setReply(result.getString(result.getColumnIndex(REPLY)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public List<CommentInfo> queryAllByUid(String userId) {
		if(dbHelper == null){
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(userId);
		params.add("0");
		Cursor result = dbHelper.query(SQL_QUERY_BY_FLAGDELETE, params);
		List<CommentInfo> beans = new ArrayList<CommentInfo>();
		while(result != null && result.moveToNext()){
			CommentInfo app = new CommentInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setComment(result.getString(result.getColumnIndex(COMMENT)));
			app.setType(result.getString(result.getColumnIndex(TYPE)));
			app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
			app.setReadState(result.getInt(result.getColumnIndex(READSTATE)));
			app.setStatus(result.getInt(result.getColumnIndex(STATUS)));
			app.setUserId(result.getString(result.getColumnIndex(USERID)));
			app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
			app.setTypeStr(result.getString(result.getColumnIndex(TYPESTR)));
			app.setReply(result.getString(result.getColumnIndex(REPLY)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	public List<CommentInfo> queryAllByArg(String arg) {
		if(dbHelper == null){
			return null;
		}
		List<String> params = new ArrayList<String>();
		params.add(arg);
		params.add(arg);
		params.add(arg);
		Cursor result = dbHelper.query(SQL_QUERY_BY_ARGS, params);
		List<CommentInfo> beans = new ArrayList<CommentInfo>();
		while(result != null && result.moveToNext()){
			CommentInfo app = new CommentInfo();
			app.setId(result.getInt(result.getColumnIndex(ID)));
			app.setComment(result.getString(result.getColumnIndex(COMMENT)));
			app.setType(result.getString(result.getColumnIndex(TYPE)));
			app.setCommentDate(result.getString(result.getColumnIndex(COMMENTDATE)));
			app.setReadState(result.getInt(result.getColumnIndex(READSTATE)));
			app.setStatus(result.getInt(result.getColumnIndex(STATUS)));
			app.setUserId(result.getString(result.getColumnIndex(USERID)));
			app.setFlagDelete(result.getInt(result.getColumnIndex(FLAGDELETE)));
			app.setTypeStr(result.getString(result.getColumnIndex(TYPESTR)));
			app.setReply(result.getString(result.getColumnIndex(REPLY)));
			beans.add(app);
		}
		result.close(); 
		return beans;
	}
	
	/**
	 * 查看未读反馈回执
	 * @return
	 */
	public int queryUnReadCount(String userId) {
		if(dbHelper == null || userId == null || userId.trim().length() == 0){
			return 0;
		}
		List<String> params = new ArrayList<String>();
		params.add("0");
		params.add(userId);
		params.add("0");
		Cursor result = dbHelper.query(SQL_QUERY_UNREAD_COUNT,params);
		int count = 0; 
		if(result != null){
			result.moveToFirst();
			count = result.getInt(0);
			result.close();
		}
		
		return count;
	}
}

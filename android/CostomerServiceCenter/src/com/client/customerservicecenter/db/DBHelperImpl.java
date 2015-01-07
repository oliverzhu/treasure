/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-11-7
 */
package com.client.customerservicecenter.db;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.client.customerservicecenter.db.handler.CommentInfoHandler;
import com.client.customerservicecenter.db.handler.FaqHandler;
import com.client.customerservicecenter.db.handler.FaqScheduleHandler;
import com.client.customerservicecenter.db.handler.FeedbackScheduleHandler;
import com.client.customerservicecenter.db.handler.LoginUserInfoHandler;
import com.client.customerservicecenter.db.handler.PictureHandler;
import com.client.customerservicecenter.util.Utils;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class DBHelperImpl extends SQLiteOpenHelper implements DBHelper {
	private static final int VERSION = 9;
	private static final String DB_NAME = "feedback.db";
	private SQLiteDatabase database;
	
	private Integer lock = 0;
	
	/**
	 * constructor the db helper
	 */
	public DBHelperImpl(Context context) {
		super(context, DB_NAME, null, VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CommentInfoHandler.SQL_CREATE);
		db.execSQL(FeedbackScheduleHandler.SQL_CREATE);
		db.execSQL(FaqHandler.SQL_CREATE);
		db.execSQL(FaqScheduleHandler.SQL_CREATE);
		db.execSQL(PictureHandler.SQL_CREATE);
		db.execSQL(LoginUserInfoHandler.SQL_CREATE);
	}
	
	private void cleanDB(SQLiteDatabase db) {
		db.execSQL(CommentInfoHandler.SQL_DROP);
		db.execSQL(FeedbackScheduleHandler.SQL_DROP);
		db.execSQL(FaqHandler.SQL_DROP);
		db.execSQL(FaqScheduleHandler.SQL_DROP);
		db.execSQL(FaqScheduleHandler.SQL_DROP);
		db.execSQL(PictureHandler.SQL_DROP);
		db.execSQL(LoginUserInfoHandler.SQL_DROP);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//write the log
		cleanDB(db);
		//upgrade the database
		onCreate(db);
	}


	@Override
	public void executeSQL(String sql) {
		synchronized (lock) {
			database = getWritableDatabase();
			database.execSQL(sql);
		}
	}
	
	@Override
	public void executeSQL(String sql, List<String> params) {
		executeSQL(Utils.combinaStr(sql, params));
	}
	
	@Override
	public Cursor query(String sql) {
		synchronized (lock) {
			database = getReadableDatabase();
			Cursor result = database.rawQuery(sql, null);
			return result;
		}
	}

	@Override
	public Cursor query(String sql, List<String> params) {
		return query(Utils.combinaStr(sql, params));
	}

	@Override
	public void batchExecSQL(List<String> addSQL, List<String> updateSQL, List<String> deleteSQL) {
		database = getWritableDatabase();
		database.beginTransaction();
		for(String item : addSQL) {
			database.execSQL(item);
		}
		database.setTransactionSuccessful();
		database.endTransaction();
	}

}

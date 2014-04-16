/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-11-7
 */
package com.home.provider;

import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.home.bean.FeedbackScheduleInfo;
import com.home.bean.OperatingBean;
import com.home.util.Utils;
import com.home.util.log.Log;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class DBHelperImpl extends SQLiteOpenHelper implements DBHelper {
	private static final String TAG = "DBHelperImpl";
	private static final int VERSION = 1;
	private static final String DB_NAME = "app.db";
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
		Log.i(TAG, "create db", Log.APP);
		db.execSQL(FeedbackScheduleInfo.SQL_CREATE);
		db.execSQL(OperatingBean.SQL_CREATE);
	}
	
	private void cleanDB(SQLiteDatabase db) {
		Log.i(TAG, "create db", Log.APP);
		db.execSQL(FeedbackScheduleInfo.SQL_DROP);
		db.execSQL(OperatingBean.SQL_DROP);
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

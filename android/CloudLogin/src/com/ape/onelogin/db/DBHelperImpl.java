/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-11-7
 */
package com.ape.onelogin.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ape.onelogin.util.Log;
import com.ape.onelogin.util.Utils;

/**
 * 
 * @author jianwen.zhu
 * 2013/6/14
 */
public class DBHelperImpl extends SQLiteOpenHelper implements DBHelper {
    private static final String TAG = "DBHelperImpl";
    private static final int VERSION = 8;
    private static final String DB_NAME = "cloud.db";
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
//        db.execSQL(LoginInfo.SQL_CREATE);
    }
    
    private void cleanDB(SQLiteDatabase db) {
        Log.i(TAG, "create db", Log.APP);
//        db.execSQL(LoginInfo.SQL_DROP);
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
    
    @Override
    public long insert(String table, String nullColumnHack, ContentValues values) {
        synchronized (lock) {
            database = getWritableDatabase();
            return database.insert(table, nullColumnHack, values);
        }
    }
    
    @Override
    public Cursor query(String table, String[] columns, String selection, 
            String[] selectionArgs, String groupBy, String having, String orderBy) {
        synchronized (lock) {
            database = getReadableDatabase();
            return database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        }
    }
    
    @Override
    public void delete(String table, String whereClause, String[] whereArgs) {
        synchronized (lock) {
            database = getWritableDatabase();
            database.delete(table, whereClause, whereArgs);
        }
    }
    
    @Override
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        synchronized (lock) {
            database = getWritableDatabase();
            return database.update(table, values, whereClause, whereArgs);
        }
    }
}

/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-11-7
 */
package com.ape.onelogin.db;

import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * 
 * @author jianwen.zhu
 *
 */
public interface DBHelper {

    /**
     * execute the sql,such as execute insert,update,delete sql.
     * @param sql
     * @author MichaelHuang
     */
    public void executeSQL(String sql);
    
    /**
     * execute the sql. user the params.
     * @param sql
     * @param params
     */
    public void executeSQL(String sql, List<String> params);

    /**
     * query the db for more information.
     * @param sql
     * @return cursor The result
     * @author MichaelHuang
     */
    public Cursor query(String sql);
    
    /**
     * query the db for more information.
     * @param sql
     * @param params
     * @return cursor The result
     * @author MichaelHuang
     */
    public Cursor query(String sql, List<String> params);
    
    /**
     * batch to execute sql
     * @param sql
     */
    public void batchExecSQL(List<String> addSQL, List<String> updateSQL, List<String> deleteSQL);
    
    public long insert(String table, String nullColumnHack, ContentValues values);
    
    public Cursor query(String table, String[] columns, String selection, 
            String[] selectionArgs, String groupBy, String having, String orderBy);
    
    public void delete(String table, String whereClause, String[] whereArgs);
    
    public int update(String table, ContentValues values, String whereClause, String[] whereArgs);
}

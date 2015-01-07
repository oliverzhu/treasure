/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-11-7
 */
package com.client.customerservicecenter.db;

import java.util.List;

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

}

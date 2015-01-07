/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.client.customerservicecenter.db.handler;

import java.util.List;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.db.DBHelper;

/**
 * 
 * @author jianwen.zhu
 *
 * @param <T>
 * 2013/4/24
 */
public abstract class IHandler<T> {
	protected  DBHelper dbHelper = AppApplication.dbHelper;
	/**
	 * add in database
	 * @param obj
	 */
	public abstract boolean add(T obj);
	
	/**
	 * delete from database
	 * @param obj
	 */
	public abstract void  delete(T obj);
	
	/**
	 * update database record
	 * @param obj
	 */
	public abstract void  update(T obj);
	
	/**
	 * query info from database.
	 * @param obj
	 * @return
	 */
	public abstract T  query(T obj);
	
	/**
	 * batch operations
	 * @param objs
	 */
	public abstract void  batch(List<T> addObjs, List<T> updateObjs, List<T> deleteObjs);
	
	/**
	 * delete all
	 */
	public abstract void clear();
	
	public abstract List<T> queryAll();
	
}

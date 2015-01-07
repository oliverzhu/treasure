/**
 * inforcreation.com co.Ltd
 * MichaelHuang
 * 2011-12-22
 */
package com.ape.onelogin.db;

import java.util.List;

import com.ape.onelogin.OneLoginApplication;

/**
 * 
 * @author jianwen.zhu
 *
 * @param <T>
 * 2013/4/24
 */
public abstract class IHandler<T> {
    protected  DBHelper dbHelper = OneLoginApplication.dbHelper;
    /**
     * add in database
     * @param obj
     */
    public abstract void add(T obj);
    
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

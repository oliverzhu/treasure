package com.cloud.client.sql;

import net.tsz.afinal.http.AjaxParams;


import com.cloud.client.CloudObject;

public interface ISqlGenerate {
    public String getTableName(Class<?> clazz);
    public AjaxParams createTable(Class<?> clazz);

    public AjaxParams insertObject(CloudObject entity);
    
    public AjaxParams updateObject(CloudObject entity);
    public AjaxParams updateObjectSpecifiedField(CloudObject entity);
    public AjaxParams updateCustom(String sql, String tableName);

    public AjaxParams getAll(Class<?> clazz);
    public AjaxParams getAll(Class<?> clazz, String orderBy);
    public AjaxParams getById(Class<?> clazz, Object idValue);
    public AjaxParams getByWhere(Class<?> clazz, String where);
    public AjaxParams getByCustomSql(String sql, String tableName);

    public AjaxParams deleteObject(CloudObject entity);
    public AjaxParams deleteObject(Class<?> clazz, String where);
    public AjaxParams deleteCustom(String sql, String tableName);
    
    public AjaxParams dropTable(Class<?> clazz);
    public AjaxParams insertOrUpdate(CloudObject entity, String where);
    public AjaxParams selectOrUpdate(CloudObject entity, String where);
    public AjaxParams selectOrInsert(CloudObject entity, String where);
}

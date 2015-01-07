package com.cloud.client.sql;

import com.cloud.client.CloudClientService;
import com.cloud.client.CloudObject;
import com.cloud.client.CloudUtil;
import com.cloud.client.CloudClientService.CloudParam;

import android.util.Log;

import net.tsz.afinal.db.sqlite.SqlBuilderEx;
import net.tsz.afinal.db.sqlite.SqlInfo;
import net.tsz.afinal.db.table.TableInfo;
import net.tsz.afinal.http.AjaxParams;

public class SqlGenerate implements ISqlGenerate {
    
    private static final String TAG = "SqlGenerate";
    private CloudClientService mCloudService;
    
    public SqlGenerate(CloudClientService cloudService, String prefix) {
    	mCloudService = cloudService;
        TableInfo.setPrefix(prefix);
    }

    @Override
    public String getTableName(Class<?> clazz) {
        return TableInfo.get(clazz).getTableName();
    }
    
    @Override
    public AjaxParams createTable(Class<?> clazz) {
        SqlInfo sqlInfo = SqlBuilderEx.getCreatTableSQL(clazz);
        sqlInfo.setAction(CloudUtil.SQL_ACTION_CREATE);
        return createSqlParams(sqlInfo);
    }

    @Override
    public AjaxParams insertObject(CloudObject entity) {
        AjaxParams params = null;
        
        if (entity == null) {
            Log.i(TAG, "[setCloudObject]entiy is null!!!");
        } else if (!(entity instanceof CloudObject)) {
            Log.i(TAG, "[setCloudObject]the entity must extends " + CloudObject.class);
        } else {
            SqlInfo sqlInsert = SqlBuilderEx.buildInsertSql(entity);
            sqlInsert.setAction(CloudUtil.SQL_ACTION_INSERT);
            params = createSqlParams(sqlInsert);
        }
        Log.i(TAG, "[setCloudObject]post message SUCCESS!");
        return params;
    }

    @Override
    public AjaxParams updateObject(CloudObject entity) {
        AjaxParams params = null;
        
        if (entity == null) {
            Log.i(TAG, "[updateObject]entiy is null!!!");
        } else if (!(entity instanceof CloudObject)) {
            Log.i(TAG, "[updateObject]the entity must extends " + CloudObject.class);
        } else if (entity.getId() == null){
            Log.i(TAG, "[updateObject]the entity's id has been missed!");
        }else{
            SqlInfo sqlUpdate = SqlBuilderEx.getUpdateSqlAsSqlInfo(entity);
            sqlUpdate.setAction(CloudUtil.SQL_ACTION_UPDATE);
            params = createSqlParams(sqlUpdate);
        }
        return params;
    }

    @Override
    public AjaxParams updateObjectSpecifiedField(CloudObject entity) {
        AjaxParams params = null;
        
        if (entity == null) {
            Log.i(TAG, "[updateObject]entiy is null!!!");
        } else if (!(entity instanceof CloudObject)) {
            Log.i(TAG, "[updateObject]the entity must extends " + CloudObject.class);
        } else if (entity.getId() == null){
            Log.i(TAG, "[updateObject]the entity's id has been missed!");
        }else{
            SqlInfo sqlUpdate = SqlBuilderEx.getUpdateSqlSpecifiedField(entity);
            sqlUpdate.setAction(CloudUtil.SQL_ACTION_UPDATE);
            params = createSqlParams(sqlUpdate);
        }
        return params;
    }

    @Override
    public AjaxParams updateCustom(String sql, String tableName) {
        SqlInfo sqlUpdate = new SqlInfo();
        sqlUpdate.setSql(sql);
        sqlUpdate.setTableName(tableName);
        sqlUpdate.setAction(CloudUtil.SQL_ACTION_UPDATE);
        return createSqlParams(sqlUpdate);
    }
    
    @Override
    public AjaxParams getAll(Class<?> clazz) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectSQL(clazz);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECT);
        return createSqlParams(sqlSelect);
    }

    @Override
    public AjaxParams getAll(Class<?> clazz, String orderBy) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectSQL(clazz);
        sqlSelect.setSql(sqlSelect.getSql() + " ORDER BY " + orderBy);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECT);
        return createSqlParams(sqlSelect);
    }

    @Override
    public AjaxParams getById(Class<?> clazz, Object idValue) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectSQL(clazz, idValue);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECT);
        return createSqlParams(sqlSelect);
    }

    @Override
    public AjaxParams getByWhere(Class<?> clazz, String where) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectSQLByWhere(clazz, where);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECT);
        return createSqlParams(sqlSelect);
    }

    @Override
    public AjaxParams getByCustomSql(String sql, String tableName) {
        SqlInfo sqlSelect = new SqlInfo();
        sqlSelect.setSql(sql);
        sqlSelect.setTableName(tableName);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECT);
        return createSqlParams(sqlSelect);
    }
    
    @Override
    public AjaxParams deleteObject(CloudObject entity) {
        AjaxParams params = null;

        if (entity == null) {
            Log.i(TAG, "[deleteObject]entiy is null!!!");
        } else if (!(entity instanceof CloudObject)) {
            Log.i(TAG, "[deleteObject]the entity must extends " + CloudObject.class);
        } else if (entity.getId() == null){
            Log.i(TAG, "[deleteObject]the entity's id has been missed!");
        }else{
            SqlInfo sqlDelete = SqlBuilderEx.buildDeleteSql(entity);
            sqlDelete.setAction(CloudUtil.SQL_ACTION_DELETE);
            params = createSqlParams(sqlDelete);
        }
        return params;
    }

    @Override
    public AjaxParams deleteObject(Class<?> clazz, String where) {
        SqlInfo sqlDelete = SqlBuilderEx.buildDeleteSql(clazz, where);
        sqlDelete.setAction(CloudUtil.SQL_ACTION_DELETE);
        return createSqlParams(sqlDelete);
    }
    
    @Override
    public AjaxParams deleteCustom(String sql, String tableName) {
        SqlInfo sqlDelete = new SqlInfo();
        sqlDelete.setSql(sql);
        sqlDelete.setTableName(tableName);
        sqlDelete.setAction(CloudUtil.SQL_ACTION_DELETE);
        return createSqlParams(sqlDelete);
    }
    
    public AjaxParams dropTable(Class<?> clazz) {
        AjaxParams params = null;
        TableInfo table = TableInfo.get(clazz);
        SqlInfo sqlDrop = new SqlInfo();
        sqlDrop.setSql("DROP TABLE " + table.getTableName());
        sqlDrop.setAction(CloudUtil.SQL_ACTION_DROP);
        sqlDrop.setTableName(table.getTableName());
        params = createSqlParams(sqlDrop);
        return params;
    }
    
    @Override
    public AjaxParams insertOrUpdate(CloudObject entity, String where) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectIdByWhere(entity.getClass(), where);
        SqlInfo sqlInsert = SqlBuilderEx.buildInsertSql(entity);
        SqlInfo sqlUpdate = SqlBuilderEx.getUpdateSqlAsSqlInfo(entity, where);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_INSERTORUPDATE);
        AjaxParams params = createSqlParams(sqlSelect);
        params.put(CloudUtil.SQL_PARAM_SQL_PART_INSERT, sqlInsert.getSql());
        params.put(CloudUtil.SQL_PARAM_SQL_PART_UPDATE, sqlUpdate.getSql());
        return params;
    } 

    @Override
    public AjaxParams selectOrUpdate(CloudObject entity, String where) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectIdByWhere(entity.getClass(), where);
        SqlInfo sqlUpdate = SqlBuilderEx.getUpdateSqlAsSqlInfo(entity, where);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECTORUPDATE);
        AjaxParams params = createSqlParams(sqlSelect);
        params.put(CloudUtil.SQL_PARAM_SQL_PART_UPDATE, sqlUpdate.getSql());
        return params;
    }

    @Override
    public AjaxParams selectOrInsert(CloudObject entity, String where) {
        SqlInfo sqlSelect = SqlBuilderEx.getSelectIdByWhere(entity.getClass(), where);
        SqlInfo sqlInsert = SqlBuilderEx.buildInsertSql(entity);
        sqlSelect.setAction(CloudUtil.SQL_ACTION_SELECTORINSERT);
        AjaxParams params = createSqlParams(sqlSelect);
        params.put(CloudUtil.SQL_PARAM_SQL_PART_INSERT, sqlInsert.getSql());
        return params;
    }
    
    private AjaxParams createSqlParams(SqlInfo sqlInfo) {
        AjaxParams params = new AjaxParams();
        CloudParam mCloudParam = mCloudService.getAuthorizeParms();
        params.put(CloudUtil.SQL_PARAM_SQL, sqlInfo.getSql());
        params.put(CloudUtil.SQL_PARAM_ACTION, String.valueOf(sqlInfo.getAction()));
        params.put(CloudUtil.SQL_PARAM_TABLE, sqlInfo.getTableName());
        params.put(CloudUtil.SQL_PARAM_PROVIDER_KEY_PLUS, mCloudParam.providerKey);
        params.put(CloudUtil.SQL_PARAM_PROVIDER_APK_NAME, mCloudParam.providerPackageName);
        params.put(CloudUtil.SQL_PARAM_PROVIDER_CLIENT_TIME, mCloudParam.providerTime);
        params.put(CloudUtil.SQL_PARAM_USER_KEY_PLUS, mCloudParam.userKey);
        params.put(CloudUtil.SQL_PARAM_USER_APK_NAME, mCloudParam.userPackageName);
        params.put(CloudUtil.SQL_PARAM_USER_CLIENT_TIME, mCloudParam.userTime);
        params.put(CloudUtil.SQL_PARAM_VERSION_NO, mCloudParam.version);
        return params;
    }
}

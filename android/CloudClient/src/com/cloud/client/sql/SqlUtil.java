package com.cloud.client.sql;

import com.cloud.client.CloudObject;
import com.cloud.client.CloudUtil;

import net.tsz.afinal.db.sqlite.SqlBuilderEx;
import net.tsz.afinal.db.sqlite.SqlInfo;
import net.tsz.afinal.db.table.TableInfo;

class SqlUtil {
    
    private String mAppKey = null;
    
    public SqlUtil(String appKey) {
        this.mAppKey = appKey;
        TableInfo.setPrefix(mAppKey);
    }
    /**
     * @param entity
     * @return
     */
    public SqlInfo getCreateTableSql(Class<?> clazz) {
        SqlInfo sqlInfo = SqlBuilderEx.getCreatTableSQL(clazz);
        return createSqlInfo(sqlInfo, CloudUtil.SQL_ACTION_CREATE);
    }
    
    /**
     * @param clazz
     * @return
     */
    public SqlInfo getDropTable(Class<?> clazz) {
        TableInfo table = TableInfo.get(clazz);
        String sql = "DROP TABLE " + table.getTableName();
        
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.setTableName(table.getTableName());
        sqlInfo.setSql(sql);
        
        return createSqlInfo(sqlInfo, CloudUtil.SQL_ACTION_DROP);
    }
    
    /**
     * @param entity
     * @return
     */
    public SqlInfo getInsertSql(CloudObject entity) {
        return createSqlInfo(SqlBuilderEx.buildInsertSql(entity), 
                CloudUtil.SQL_ACTION_INSERT);
    }
    
    /**
     * @param entity
     * @return
     */
    public SqlInfo getUpdateSql(CloudObject entity) {
        return createSqlInfo(SqlBuilderEx.getUpdateSqlAsSqlInfo(entity), 
                CloudUtil.SQL_ACTION_UPDATE);
    }
    
    /**
     * @param entity
     * @param strWhere
     * @return
     */
    public SqlInfo getUpdateSql(CloudObject entity, String strWhere) {
        return createSqlInfo(SqlBuilderEx.getUpdateSqlAsSqlInfo(entity, strWhere),
                CloudUtil.SQL_ACTION_UPDATE);
    }
    
    /**
     * @param entity
     * @return
     */
    public SqlInfo getDeleteSql(CloudObject entity) {
        return createSqlInfo(SqlBuilderEx.buildDeleteSql(entity),
                CloudUtil.SQL_ACTION_DELETE);
    }
    
    /**
     * @param clazz
     * @param id
     * @return
     */
    public SqlInfo getDeleteSql(Class<?> clazz, Object id) {
        return createSqlInfo(SqlBuilderEx.buildDeleteSql(clazz, id),
                CloudUtil.SQL_ACTION_DELETE);
    }
    
    /**
     * @param clazz
     * @param strWhere
     * @return
     */
    public SqlInfo getDeleteSql(Class<?> clazz, String strWhere) {
        return createSqlInfo(SqlBuilderEx.buildDeleteSql(clazz, strWhere),
                CloudUtil.SQL_ACTION_DELETE);
    }
    
    /**
     * @param clazz
     * @return
     */
    public SqlInfo getDeleteAll(Class<?> clazz) {
        return createSqlInfo(SqlBuilderEx.buildDeleteSql(clazz, null),
                CloudUtil.SQL_ACTION_DELETE);
    }
    
    /**
     * @param id
     * @param clazz
     * @return
     */
    public <T> SqlInfo getSelectSql(Object id, Class<T> clazz) {
        return createSqlInfo(SqlBuilderEx.getSelectSQL(clazz, id),
                CloudUtil.SQL_ACTION_SELECT);
    }
    
    /**
     * @param clazz
     * @return
     */
    public <T> SqlInfo getSelectAll(Class<T> clazz) {
        return createSqlInfo(SqlBuilderEx.getSelectSQL(clazz),
                CloudUtil.SQL_ACTION_SELECT);
    }
    
    /**
     * @param clazz
     * @param orderBy
     * @return
     */
    public <T> SqlInfo getSelectAll(Class<T> clazz, String orderBy) {
        SqlInfo sqlInfo = SqlBuilderEx.getSelectSQL(clazz);
        sqlInfo.setSql(sqlInfo.getSql() + " ORDER BY " + orderBy);
        sqlInfo.setAction(CloudUtil.SQL_ACTION_SELECT);
        return sqlInfo;
    }
    
    /**
     * @param clazz
     * @param strWhere
     * @return
     */
    public <T> SqlInfo getSelectAllByWhere(Class<T> clazz, String strWhere) {
        return createSqlInfo(SqlBuilderEx.getSelectSQLByWhere(clazz, strWhere),
                CloudUtil.SQL_ACTION_SELECT);
    }
    
    /**
     * @param clazz
     * @param strWhere
     * @param orderBy
     * @return
     */
    public <T> SqlInfo getSelectAllByWhere(Class<T> clazz, String strWhere, String orderBy) {
        SqlInfo sqlInfo = SqlBuilderEx.getSelectSQLByWhere(clazz, strWhere);
        sqlInfo.setSql(sqlInfo.getSql() + " ORDER BY " + orderBy);
        sqlInfo.setAction(CloudUtil.SQL_ACTION_SELECT);
        return sqlInfo;
    }
    
    private SqlInfo createSqlInfo(SqlInfo sqlInfo, int action) {
        SqlInfo result = sqlInfo;
        result.setAction(action);
        return result;
        
    }
}

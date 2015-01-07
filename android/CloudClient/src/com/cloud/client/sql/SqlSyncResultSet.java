package com.cloud.client.sql;

import java.util.List;

import com.cloud.client.CloudObject;

public class SqlSyncResultSet {
    public static class SqlSyncResult extends BaseSqlSyncResult {
        // nothing to do
    }
    
    public static class SqlSyncSingleResult<T extends CloudObject> extends BaseSqlSyncResult {
        private T entity = null;
        
        public T getEntity() {
            return entity;
        }
        
        public void setEntity(T entity) {
            this.entity = entity;
        }
    }
    
    public static class SqlSyncMultiResult<T extends CloudObject> extends BaseSqlSyncResult {
        private List<T> entity = null;
        
        public List<T> getEntity() {
            return entity;
        }
        
        public void setEntity(List<T> entity) {
            this.entity = entity;
        }
    }
}

package com.cloud.client.sql;

import com.cloud.client.CloudUtil;

public class BaseSqlSyncResult {
    private int code = CloudUtil.CLOUDCLIENT_ENTITY_INITIALIZATION;
    private String message = "entity initialization";
    
    public int getCode() {
        return code;
    }
    
    public void setCode(int code) {
        this.code = code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
}

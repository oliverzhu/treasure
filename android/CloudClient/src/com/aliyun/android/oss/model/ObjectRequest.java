package com.aliyun.android.oss.model;

public abstract class ObjectRequest {
    private String key;
    private String bucket;
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public void setBucket(String bucket) {
        this.bucket = bucket;
    }
    
    public String getBucket() {
        return this.bucket;
    }
}

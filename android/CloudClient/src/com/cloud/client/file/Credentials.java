package com.cloud.client.file;

public class Credentials {
    private String mAccessId;
    private String mSecretKey;
    private String mBucketName;
    private String mOSSType;
    private String mOSSLocal;
    
    public class OSSType {
        public static final String ALIYUN = "Aliyun";
        public static final String AMAZON = "Amazon";
    }

    public Credentials(String accessId, String secretKey, 
            String bucketName, String ossType, String ossLocal) {
        this.mAccessId = accessId;
        this.mSecretKey = secretKey;
        this.mBucketName = bucketName;
        this.mOSSType = ossType;
        this.mOSSLocal = ossLocal;
    }

    public String getAccessId() {
        return mAccessId;
    }

    public void setAccessId(String accessId) {
        this.mAccessId = accessId;
    }

    public String getSecretKey() {
        return mSecretKey;
    }

    public void setSecretKey(String secretKey) {
        this.mSecretKey = secretKey;
    }

    public String getBucketName() {
        return mBucketName;
    }

    public void setBucketName(String bucketName) {
        this.mBucketName = bucketName;
    }

    public String getOSSType() {
        return mOSSType;
    }

    public void setOSSType(String ossType) {
        this.mOSSType = ossType;
    }

    public String getOSSLocal() {
        return mOSSLocal;
    }

    public void setOSSLocal(String ossLocal) {
        this.mOSSLocal = ossLocal;
    };
    
}

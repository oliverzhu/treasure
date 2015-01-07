package com.aliyun.android.oss.model;


import com.cloud.client.file.MissionListener;

import org.apache.http.entity.mime.MultipartEntity;

public class PutObjectRequest extends ObjectRequest {
    private String filePath;
    private String md5Hash;
    private String contentType;
    private MultipartEntity entity;
    private MissionListener progressListener;

    public PutObjectRequest(String key, String filePath, String contentType) {
        super.setKey(key);
        this.filePath = filePath;
        this.contentType = contentType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContentMD5() {
        return md5Hash;
    }

    public void setContentMD5(String md5Hash) {
        this.md5Hash = md5Hash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public MultipartEntity getEntity() {
        return entity;
    }

    public void setEntity(MultipartEntity entity) {
        this.entity = entity;
    }
    
    public void setProgressListener(MissionListener listener) {
        this.progressListener = listener;
    }
    
    public MissionListener getProgressListener() {
        return progressListener;
    }
}

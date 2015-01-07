/**
4 * Copyright (c) 2012 The Wiseserc. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.aliyun.android.oss.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.InputStreamEntity;

import com.aliyun.android.oss.OSSErrorCode;
import com.aliyun.android.oss.OSSException;
import com.aliyun.android.oss.http.HttpContentType;
import com.aliyun.android.oss.http.HttpMethod;
import com.aliyun.android.oss.http.OSSHttpTool;
import com.aliyun.android.oss.model.ObjectMetaData;
import com.aliyun.android.util.Helper;
import com.cloud.client.file.MissionObject;
import com.cloud.client.file.database.DatabaseAccessManager;

/**
 * 上传Object任务
 * 
 * @author Michael
 */
public class PutObjectTask extends Task {
    /**
     * object key
     */
    private String objectKey;

    /**
     * Object 元数据
     */
    private ObjectMetaData objectMetaData;

    /**
     * 上传的文件
     */
    private File uploadFile;
    
    private InputStreamEntity entity = null;

    /**
     * 上传的文件数据
     */
    private byte[] data;

    public PutObjectTask(String bucketName, String objectKey, String contentType) {
        super(HttpMethod.PUT, bucketName);
        this.objectKey = objectKey;
        objectMetaData = new ObjectMetaData();
        objectMetaData.setContentType(contentType);
    }

    public PutObjectTask(String bucketName, String objectKey, String contentType, 
            InputStreamEntity entity) {
        super(HttpMethod.PUT, bucketName);
        this.objectKey = objectKey;
        this.entity = entity;
        objectMetaData = new ObjectMetaData();
        objectMetaData.setContentType(contentType);
    }
    
    /**
     * 参数合法性验证
     */
    @Override
    protected void checkArguments() {
        if (Helper.isEmptyString(bucketName) || Helper.isEmptyString(objectKey)) {
            throw new IllegalArgumentException(
                    "bucketName or objectKey not set");
        }
        if (objectMetaData == null
                || Helper.isEmptyString(objectMetaData.getContentType())) {
            throw new IllegalArgumentException(
                    "ObjectMetaData not properly set");
        }
    }

    /**
     * 构造HttpPut
     */
    protected HttpUriRequest generateHttpRequest() {
        // 生成Http请求
        String requestUri = this.getOSSEndPoint() + 
                httpTool.generateCanonicalizedResource("/" + OSSHttpTool.encodeUri(objectKey));
        HttpPut httpPut = new HttpPut(requestUri);

        // 构造HttpPut
        String resource = httpTool.generateCanonicalizedResource("/"
                + bucketName + "/" + objectKey);
        String dateStr = Helper.getGMTDate();
        String xossHeader = OSSHttpTool
                .generateCanonicalizedHeader(objectMetaData.getAttrs());
        String authorization = OSSHttpTool.generateAuthorization(accessId,
                accessKey, httpMethod.toString(), "",
                objectMetaData.getContentType(), dateStr, xossHeader, resource);

        httpPut.setHeader(AUTHORIZATION, authorization);
        httpPut.setHeader(DATE, dateStr);

        OSSHttpTool.addHttpRequestHeader(httpPut, CACHE_CONTROL,
                objectMetaData.getCacheControl());
        OSSHttpTool.addHttpRequestHeader(httpPut, CONTENT_DISPOSITION,
                objectMetaData.getContentDisposition());
        OSSHttpTool.addHttpRequestHeader(httpPut, CONTENT_ENCODING,
                objectMetaData.getContentEncoding());
        OSSHttpTool.addHttpRequestHeader(httpPut, CONTENT_TYPE,
                objectMetaData.getContentType());
        OSSHttpTool.addHttpRequestHeader(httpPut, EXPIRES,
                Helper.getGMTDate(objectMetaData.getExpirationTime()));

        // 加入用户自定义header
        for (Entry<String, String> entry: objectMetaData.getAttrs().entrySet()) {
            OSSHttpTool.addHttpRequestHeader(httpPut, entry.getKey(),
                    entry.getValue());
        }

        if (objectMetaData.getContentType().equals(HttpContentType.DIR.toString())) {
            data = new byte[0];
            httpPut.setEntity(new ByteArrayEntity(this.data));
        } else {
            httpPut.setEntity(entity);
        }
        
        return httpPut;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public File getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(File uploadFile) {
        this.uploadFile = uploadFile;
        if (entity == null) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(uploadFile);
                entity = new InputStreamEntity(inputStream, uploadFile.length());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                        inputStream = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public ObjectMetaData getObjectMetaData() {
        return objectMetaData;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setEntity(InputStreamEntity entity) {
        this.entity = entity;
    }
    
    public InputStreamEntity getEntity() {
        return this.entity;
    }
    /**
     * 获取Put Object的结果：OSS收到文件的MD5值，以便用户检查
     * @return OSS收到文件的MD5值
     * 
     * @throws OSSException
     */
    public String getResult() throws OSSException {
        HttpResponse r = null;
        try {
            r = this.execute();
        } catch (OSSException osse) {
            throw osse;
        } finally {
            if (entity != null) {
                try {
                    if (entity.getContent() != null) {
                        entity.getContent().close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Header etagHeader = r.getFirstHeader("ETag");
        if (etagHeader == null) {
            OSSException ossException = new OSSException("no ETag header returned from oss.");
            ossException.setErrorCode(OSSErrorCode.ETAG_HEADER_EMPTY);
            throw ossException;
        }
        String value = etagHeader.getValue();
        
        //去掉返回值首尾的"
        while (value.startsWith("\"")) {
            value = value.substring(1);
        }
        while (value.endsWith("\"")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }
}

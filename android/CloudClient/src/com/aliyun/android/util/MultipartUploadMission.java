/**
 * Copyright (c) 2012 The Wiseserc. All rights reserved. Use of this source code
 * is governed by a BSD-style license that can be found in the LICENSE file.
 */
package com.aliyun.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aliyun.android.oss.OSSErrorCode;
import com.aliyun.android.oss.OSSException;
import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.model.ObjectMetaData;
import com.aliyun.android.oss.model.Part;
import com.aliyun.android.oss.model.query.ListPartsQuery;
import com.aliyun.android.oss.task.MultipartUploadAbortTask;
import com.aliyun.android.oss.task.MultipartUploadCompleteTask;
import com.aliyun.android.oss.task.MultipartUploadInitTask;
import com.aliyun.android.oss.task.UploadPartTask;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionListener;

/**
 * 分组上传任务
 * 
 * @author Michael
 */
public class MultipartUploadMission {

    public static Integer DEFAULT_MAX_PARTS = 1000;

    private String accessId;

    private String accessKey;

    /**
     * 所属Bucket
     */
    private String bucketName;

    /**
     * 目标文件
     */
    private String targetObjectKey;

    /**
     * 本地文件
     */
    private File file;

    /**
     * 文件总大小
     */
    private long totalSize = 0L;
    
    /**
     * 文件类型
     */
    private String contentType;
    
    /**
     * 分块上传标识ID
     */
    private String uploadId;

    /**
     * 初始化时间
     */
    private Date initTime;

    /**
     * 文件块大小,以Byte为单位
     */
    private Integer partSize;

    /**
     * 文件块信息, 以partNumber作为索引 Modified by harttle, changed from HashMap, no need
     * to use hashMap(better sparseArray)
     */
    private List<Part> partList;

    /**
     * 文件总块数
     */
    private int totalPart;
    
    /**
     * Object 元数据
     */
    private ObjectMetaData objectMetaData;
    
    private MissionListener listener;
    
    /**
     * 构造新实例
     */
    public MultipartUploadMission(String accessId, String accessKey,
            String bucketName, String targetObjectKey, File file,
            Integer partSize) {
        this.accessId = accessId;
        this.accessKey = accessKey;
        this.bucketName = bucketName;
        this.targetObjectKey = targetObjectKey;
        this.file = file;
        this.partSize = partSize;
        this.totalSize = file.length();
        this.partList = new ArrayList<Part>();
        
        this.totalPart = (int) (totalSize / partSize);
        if (file.length() % partSize != 0) {
            this.totalPart++;
        }

        if (this.totalPart > 10000) {
            OSSException ossException = new OSSException("Part number exeeded!");
            ossException.setErrorCode(OSSErrorCode.PART_NUMBER_EXEEDED);
            throw ossException;
        }
    }

    /**
     * 使用MultipartUploadInitTask进行初始化
     * 然后读取文件，并根据partSize进行截断，填充数据和partNumber到parts中
     */
    public void init() {
        try {
            MultipartUploadInitTask initTask = new MultipartUploadInitTask(
                    bucketName, targetObjectKey);
            initTask.initKey(accessId, accessKey);
            if (contentType != null) {
                objectMetaData = new ObjectMetaData();
                objectMetaData.setContentType(contentType);
                initTask.setObjectMetaData(objectMetaData);
            }
            this.uploadId = initTask.getResult();
        } catch (OSSException e) {
            throw e;
        }
    }

    /**
     * 调用UploadPartTask上传每一块文件，并对parts进行etag填充。这样用户可以单独起线程去完成每一块的上传。
     */
    public void uploadPart(Part part) {
        try {
            UploadPartTask uploadTask = new UploadPartTask(bucketName,
                    targetObjectKey, uploadId, part);
            uploadTask.initKey(accessId, accessKey);
            String eTag = uploadTask.getResult();
            part.setEtag(eTag);
        } catch (OSSException e) {
            throw e;
        } finally {
            if (part.getStream() != null) {
                try {
                    part.getStream().close();
                    part.setStream(null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public List<Part> splitFile() {
        int currPartNumber = 1;
        long offset = 0L;
        long currPartSize = 0L;
        
        for (int i = 0; i < totalPart; i++) {
            currPartNumber = i + 1;
            offset = partSize * i;
            currPartSize = partSize < file.length() - offset ? partSize
                    : file.length() - offset;
            
            Part part = new Part(currPartNumber);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            try {
                inputStream.skip(offset);
            } catch (IOException e) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            
            part.setStream(inputStream);
            part.setSize(currPartSize);
            partList.add(part);
        }
        return partList;
    }
    
    /**
     * 结束分组上传
     */
    public OSSObject complete(List<Part> partList) {
        // 调用MultipartUploadCompleteTask.
        try {
            MultipartUploadCompleteTask task = new MultipartUploadCompleteTask(
                    bucketName, targetObjectKey, uploadId, partList);
            task.initKey(accessId, accessKey);
            return task.getResult();
        } catch (OSSException e) {
            throw e;
        }
    }

    /**
     * 取消上传，使用MultipartUploadAbortTask，如果其所属的某些 Part 仍然在上传， 那么这次中止操作将无法删除这些
     * Part。所以如果存在并发访问的情况,为了彻底释放OSS 上的空间,需要调用几次 Abort Multipart Upload 接口。
     */
    public boolean abort() {
        // 取消上传，
        try {
            MultipartUploadAbortTask task = new MultipartUploadAbortTask(
                    bucketName, targetObjectKey, uploadId);
            task.initKey(accessId, accessKey);
            return task.getResult();
        } catch (OSSException e) {
            throw e;
        } catch (Exception ee) {
            throw new OSSException(ee);
        }
    }

    /**
     * 罗列已经上传成功的文件块，默认每页1000个Part
     */
    public Pagination<Part> listSuccessParts() {
        return listSuccessParts(DEFAULT_MAX_PARTS);
    }

    /**
     * 罗列已经上传成功的文件块
     */
    public Pagination<Part> listSuccessParts(Integer maxParts) {
        try {
            ListPartsQuery query = new ListPartsQuery(accessId, accessKey,
                    bucketName, targetObjectKey, uploadId, maxParts);
            return query.paginate();
        } catch (OSSException e) {
            throw e;
        }
    }

    /**
     * 简单的分组上传方式
     */
    // public OSSObject quickUpload() {
    // // 自动调用init, uploadPart和complete，不启用多线程，或者用户直接为这整个过程开启一个线程。
    // init();
    // parts = uploadFile();
    // return complete();
    // }

    /**
     * 切割文件并进行上传 由于分块上传的文件往往较手机内存来说很大，所以不能全部读入到内存中，需要切割一块后就直接上传，然后释放内存
     * 
     * @throws IOException
     */
    public int uploadFile() {
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        int currPartNumber = 1;
        long start = 0L;
        long currPartSize = 0L;
        if (listener != null) {
            listener.setTotalSize(totalPart);
        }
        
        for (int i = 0; i < totalPart; i++) {
            if (listener != null && listener.isCancel()) {
                result = CloudUtil.CLOUD_FILE_MISSION_CANCEL;
                break;
            }
            start = partSize * i;
            currPartNumber = i + 1;
            currPartSize = partSize < file.length() - start ? partSize
                    : file.length() - start;
            Part part = new Part(currPartNumber);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                inputStream.skip(start);
                part.setStream(inputStream);
                part.setSize(currPartSize);
                uploadPart(part);
                partList.add(part);
                if (listener != null) {
                    listener.transferred(currPartNumber);
                }
            } catch (OSSException osse) {
                throw osse;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return result;
    }
    
    public void setMissionListener(MissionListener listener) {
        this.listener = listener;
    }

    /** * @return the bucketName */
    public String getBucketName() {
        return bucketName;
    }

    /** * @param bucketName the bucketName to set */

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    /** * @return the targetObjectKey */
    public String getTargetObjectKey() {
        return targetObjectKey;
    }

    /** * @param targetObjectKey the targetObjectKey to set */

    public void setTargetObjectKey(String targetObjectKey) {
        this.targetObjectKey = targetObjectKey;
    }

    public File getFile() {
        return this.file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    /** * @return the uploadId */
    public String getUploadId() {
        return uploadId;
    }

    /** * @param uploadId the uploadId to set */

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    /** * @return the initTime */
    public Date getInitTime() {
        return initTime;
    }

    /** * @param initTime the initTime to set */

    public void setInitTime(Date initTime) {
        this.initTime = initTime;
    }

    /** * @return the partSize */
    public Integer getPartSize() {
        return partSize;
    }

    /** * @param partSize the partSize to set */

    public void setPartSize(Integer partSize) {
        this.partSize = partSize;
    }

    /** * @return the accessId */
    public String getAccessId() {
        return accessId;
    }

    /** * @param accessId the accessId to set */

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    /** * @return the accessKey */
    public String getAccessKey() {
        return accessKey;
    }

    /** * @param accessKey the accessKey to set */

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
    
    public List<Part> getPartList() {
        return this.partList;
    }
    
    public int getTotalPart() {
        return this.totalPart;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

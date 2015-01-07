/**
 * Copyright (c) 2012 The Wiseserc. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */

package com.aliyun.android.oss.task;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import com.aliyun.android.oss.OSSErrorCode;
import com.aliyun.android.oss.OSSException;
import com.aliyun.android.oss.http.HttpMethod;
import com.aliyun.android.oss.http.OSSHttpTool;
import com.aliyun.android.oss.model.OSSObject;
import com.aliyun.android.oss.model.Range;
import com.aliyun.android.util.Helper;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;
import com.cloud.client.file.database.DatabaseAccessManager;
import com.cloud.util.log.LogMission;

/**
 * @author Michael
 */
public class GetObjectTask extends Task {
    private static final String TAG = "GetObjectTask";

    /**
     * Object名称
     */
    private String objectKey;

    /**
     * 范围
     */
    private Range range;

    /**
     * 指定在此时间后修改过
     */
    private Date modifiedSince;

    /**
     * 指定在此时间后没有修改过
     */
    private Date unModifiedSince;

    /**
     * 期望的ETag
     */
    private String expectedETag;

    /**
     * 不期望的ETag
     */
    private String unexpectedETag;
    
    private String filePath;

    /**
     * OSS返回请求的自定义header
     */
    private String responseContentType = null;

    private String responseContentLanguage = null;

    private String responseExpires = null;

    private String responseCacheControl = null;

    private String responseContentDisposition = null;

    private String responseContentEncoding = null;
    
    private MissionListener listener = null;

    private MissionObject missionObject = null;
    
    private DatabaseAccessManager mDatabaseAccessManager = null;
    
    private LogMission mLogMission;
    /**
     * 解密密钥
     */
    private byte[] decryptKey;
    
    public GetObjectTask(String bucketName, String objectKey, String filePath, MissionObject missionObject) {
        this(bucketName, objectKey);
        this.filePath = filePath;
        this.missionObject = missionObject;
    }
    /**
     * 构造新实例
     */
    public GetObjectTask(String bucketName, String objectKey) {
        super(HttpMethod.GET, bucketName);
        this.objectKey = objectKey;
        this.mLogMission = new LogMission(TAG);
    }

    /**
     * 参数合法性验证
     */
    @Override
    protected void checkArguments() {
        if (Helper.isEmptyString(bucketName) || Helper.isEmptyString(objectKey)) {
            throw new IllegalArgumentException(
                    "bucketName or objectKey not properly set");
        }
    }

    /**
     * 构造HttpGet
     */
    protected HttpUriRequest generateHttpRequest() {
        // 生成Http请求
        String requestUri = this.getOSSEndPoint()
                + httpTool.generateCanonicalizedResource("/" + OSSHttpTool.encodeUri(objectKey));
        HttpGet httpGet = new HttpGet(requestUri);

        // 构造Http请求
        String resource = httpTool.generateCanonicalizedResource("/"
                + bucketName + "/" + objectKey);
        String dateStr = Helper.getGMTDate();
        String authorization = OSSHttpTool
                .generateAuthorization(accessId, accessKey,
                        httpMethod.toString(), "", "", dateStr, "", resource);

        httpGet.setHeader(AUTHORIZATION, authorization);
        httpGet.setHeader(DATE, dateStr);

        if (range != null) {
            OSSHttpTool.addHttpRequestHeader(httpGet, RANGE,
                    "bytes=" + range.toString());
        }
        OSSHttpTool.addHttpRequestHeader(httpGet, IF_MODIFIED_SINCE,
                Helper.getGMTDate(modifiedSince));
        OSSHttpTool.addHttpRequestHeader(httpGet, IF_UNMODIFIED_SINCE,
                Helper.getGMTDate(unModifiedSince));
        OSSHttpTool.addHttpRequestHeader(httpGet, IF_MATCH, expectedETag);
        OSSHttpTool
                .addHttpRequestHeader(httpGet, IF_NONE_MATCH, unexpectedETag);

        return httpGet;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public void setProgressListener(MissionListener listener) {
        this.listener = listener;
    }
    
    public MissionListener getProgressListener() {
        return this.listener;
    }

    /**
     * 获取文件传输的范围，用{@link Range}对象表示
     * 
     * @return 返回文件传输的范围
     * @see Range
     */
    public Range getRange() {
        return range;
    }

    /**
     * 设置文件传输范围，用{@link Range}对象表示
     * 
     * @param range
     *            文件传输范围
     * @see Range
     */
    public void setRange(Range range) {
        this.range = range;
    }

    /**
     * 获取期望时间，如果该时间早于实际修改时间，则正常传送文件，否则请求返回304 not modified
     * 
     * @return 期望时间
     */
    public Date getModifiedSince() {
        return modifiedSince;
    }

    /**
     * 设置期望时间，如果该时间早于实际修改时间，则正常传送文件，否则请求返回304 Not Modified
     * 
     * @param modifiedSince
     *            期望时间
     */
    public void setModifiedSince(Date modifiedSince) {
        this.modifiedSince = modifiedSince;
    }

    /**
     * 获取非期望时间，如果该时间等于或晚于文件实际修改时间，则正常传输文件，否则请求返回412 Precondition Failed
     * 
     * @return 非期望时间
     */
    public Date getUnModifiedSince() {
        return unModifiedSince;
    }

    /**
     * 设置非期望时间，如果该时间等于或晚于文件实际修改时间，则正常传输文件，否则请求返回412 Precondition Failed
     * 
     * @param unModifiedSince
     *            非期望时间
     */
    public void setUnModifiedSince(Date unModifiedSince) {
        this.unModifiedSince = unModifiedSince;
    }

    /**
     * 获取期望Tag，如果期望Tag和object的ETag匹配，则正常返回，否则请求返回412 Precondition Failed
     * 
     * @return 期望Tag
     */
    public String getExpectedETag() {
        return expectedETag;
    }

    /**
     * 设置期望Tag，如果期望Tag和object的ETag匹配，则正常传输文件，否则请求返回412 Precondition Failed
     * 
     * @param expectedETag
     */
    public void setExpectedETag(String expectedETag) {
        this.expectedETag = expectedETag;
    }

    /**
     * 获取期望Tag，如果期望Tag和object的ETag不匹配，则正常传输文件，否则请求返回304 Not Modified
     * 
     * @return 期望Tag
     */
    public String getUnexpectedETag() {
        return unexpectedETag;
    }

    /**
     * 设置期望Tag，如果期望Tag和object的ETag不匹配，则正常传输文件，否则请求返回304 Not Modified
     * 
     * @return 期望Tag
     */
    public void setUnexpectedETag(String unexpectedETag) {
        this.unexpectedETag = unexpectedETag;
    }

    public String getResponseContentType() {
        return responseContentType;
    }

    /**
     * 设置OSS返回请求的Content-Type header
     * 
     * @param responseContentType
     */
    public void setResponseContentType(String responseContentType) {
        httpTool.setContentType(responseContentType);
        this.responseContentType = responseContentType;
    }

    public String getResponseContentLanguage() {
        return responseContentLanguage;
    }

    /**
     * 设置OSS返回请求的Content-Language header
     * 
     * @param responseContentLanguage
     */
    public void setResponseContentLanguage(String responseContentLanguage) {
        httpTool.setContentLanguage(responseContentLanguage);
        this.responseContentLanguage = responseContentLanguage;
    }

    public String getResponseExpires() {
        return responseExpires;
    }

    /**
     * 设置OSS返回请求的Expires header
     * 
     * @param responseExpires
     */
    public void setResponseExpires(String responseExpires) {
        httpTool.setExpires(responseExpires);
        this.responseExpires = responseExpires;
    }

    public String getResponseCacheControl() {
        return responseCacheControl;
    }

    /**
     * 设置OSS返回请求的Cache-Contorl header
     * 
     * @param responseCacheControl
     */
    public void setResponseCacheControl(String responseCacheControl) {
        httpTool.setCacheControl(responseCacheControl);
        this.responseCacheControl = responseCacheControl;
    }

    public String getResponseContentDisposition() {
        return responseContentDisposition;
    }

    /**
     * 设置OSS返回请求的Content-Disposition header
     * 
     * @param responseContentDisposition
     */
    public void setResponseContentDisposition(String responseContentDisposition) {
        httpTool.setContentDisposition(responseContentDisposition);
        this.responseContentDisposition = responseContentDisposition;
    }

    public String getResponseContentEncoding() {
        return responseContentEncoding;
    }

    /**
     * 设置OSS返回请求的Content-Encoding header
     * 
     * @param responseContentEncoding
     */
    public void setResponseContentEncoding(String responseContentEncoding) {
        httpTool.setContentEncoding(responseContentEncoding);
        this.responseContentEncoding = responseContentEncoding;
    }

    /**
     * 获取对象，包括对象元信息以及对象内容
     * @param {@link OSSObject}对象，包含对象元信息以及内容
     * @return {@link OSSClientUtil}
     * @throws OSSException
     */
    public int getResult(OSSObject object) throws OSSException {
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        
        HttpResponse response = null;
        RandomAccessFile randomAccessFile = null;
        InputStream inputStream = null;
        object = new OSSObject(this.bucketName, this.objectKey);
        try {
            mLogMission.i("getResult", "before network request", missionObject);
            response = this.execute();
            mLogMission.i("getResult", "network response", missionObject);
            Header rangeHeader = response.getFirstHeader("Content-Range");
            if (rangeHeader != null) {
                String range = rangeHeader.getValue();
                mLogMission.i("getResult", "range:" + range.toString(), missionObject);
            }
            object.setObjectMetaData(OSSHttpTool.getObjectMetadataFromResponse(response));
            if (response.getEntity() != null) {
                mLogMission.i("getResult", "the content length get from server:" + 
                        response.getEntity().getContentLength(), missionObject);
                if (missionObject.getFileLength() <= 0L) {
                    missionObject.setFileLength(response.getEntity().getContentLength());
                    mLogMission.i("getResult", "get content length and set file length. fileLength:" + 
                            missionObject.getFileLength(), missionObject);
                }
                missionObject.setPaused(false);
                inputStream = response.getEntity().getContent();
                if (listener != null) {
                    listener.setTotalSize(missionObject.getFileLength());
                }
            }
            result = mDatabaseAccessManager.updateDownloadFile(missionObject);
            if (result != CloudUtil.CLOUDCLIENT_RESULT_OK) {
                return result;
            }
            
            randomAccessFile = new RandomAccessFile(filePath, "rwd");
            long offset = 0;
            if (range != null) {
                offset = range.getStart();
                mLogMission.i("getResult", "set offset before write file, offset:" + offset, missionObject);
            }
            randomAccessFile.seek(offset);
            mLogMission.i("getResult", "before write to local, file offset:" + offset, missionObject);
            byte[] buffer = new byte[1024 * 4];
            long readTotal = offset;
            int readLength = 0;
            while (true) {
                if (readTotal == missionObject.getFileLength()) {
                    missionObject.setFinished(true);
                    result = CloudUtil.CLOUDCLIENT_RESULT_OK;
                    mLogMission.i("getResult", "readTotal == missionObject's fileLength, readTotal:" + readTotal, missionObject);
                    break;
                }
                if (listener != null && listener.isCancel()) {
                    missionObject.setPaused(true);
                    result = CloudUtil.CLOUD_FILE_MISSION_CANCEL;
                    mLogMission.i("getResult", "cancel download missionObject", missionObject);
                    break;
                }
                readLength = inputStream.read(buffer);
                mLogMission.i("getResult", "read buffer length:" + readLength, missionObject);
                if (readLength == -1) {
                    missionObject.setFinished(true);
                    result = CloudUtil.CLOUDCLIENT_RESULT_OK;
                    mLogMission.i("getResult", "buffer read finish, readLength:" + readLength, missionObject);
                    break;
                }
                
                mLogMission.i("getResult", "write to local, length:" + readLength, missionObject);
                randomAccessFile.write(buffer, 0, readLength);
                readTotal += readLength;
                mLogMission.i("getResult", "readTotal:" + readTotal, missionObject);
                missionObject.setTransferredLength(readTotal);
                missionObject.setLastTime(System.currentTimeMillis());
//                result = mDatabaseAccessManager.updateDownloadFile(missionObject);
//                if (result != CloudUtil.CLOUDCLIENT_RESULT_OK) {
//                    break;
//                }
                if (listener != null) {
                    listener.transferred(readTotal);
                }
            }
        } catch (OSSException osse) {
            throw osse;
        } catch (ParseException pe) {
            OSSException ossException = new OSSException(pe);
            ossException.setErrorCode(OSSErrorCode.PARSE_METADATA_ERROR);
            throw ossException;
        } catch (IOException ioe) {
            OSSException ossException = new OSSException(ioe);
            ossException.setErrorCode(OSSErrorCode.GET_ENTITY_CONTENT_ERROR);
            throw ossException;
        } finally {
            mDatabaseAccessManager.updateDownloadFile(missionObject);
            mLogMission.i("getResult", "finally, update db", missionObject);
            this.releaseHttpClient();
            if (randomAccessFile != null) {
                try {
                    inputStream.close();
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
    
    /**
     * 获取对象，包括对象元信息以及对象内容 
     * 
     * @return {@link OSSObject}对象，包含对象元信息以及内容
     * @throws Exception
     * @see OSSObject
     */
    public OSSObject getResult() throws OSSException {
        HttpResponse r = null;
        OSSObject obj = new OSSObject(this.bucketName, this.objectKey);
        FileOutputStream fileOutputStream = null;
        InputStream inputStream;
        try {
            r = this.execute();
            
            obj.setObjectMetaData(OSSHttpTool.getObjectMetadataFromResponse(r));

            if (listener != null && r.getEntity() != null) {
                listener.setTotalSize(r.getEntity().getContentLength());
            }
            inputStream = r.getEntity().getContent();
            fileOutputStream = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024 * 4];
            int readLength = 0;
            long readTotal = 0L;
            while ((readLength = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, readLength);
                readTotal += readLength;
                if (listener != null) {
                    listener.transferred(readTotal);
                }
            }
        } catch (OSSException osse) {
            throw osse;
        } catch (ParseException pe) {
            OSSException ossException = new OSSException(pe);
            ossException.setErrorCode(OSSErrorCode.PARSE_METADATA_ERROR);
            throw ossException;
        } catch (IOException ioe) {
            OSSException ossException = new OSSException(ioe);
            ossException.setErrorCode(OSSErrorCode.GET_ENTITY_CONTENT_ERROR);
            throw ossException;
        } finally {
            this.releaseHttpClient();
            if (fileOutputStream != null)
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

        
//        try {
//            // 解密
//            String algorithm = obj.getObjectMetaData().getAttrs()
//                    .get(X_OSS_META_ENCRYPT);
//            if (!Helper.isEmptyString(algorithm)) {
//                if (Helper.isEmptyString(new String(decryptKey))) {
//                    throw new IllegalArgumentException("decrypt should not be null");
//                }
//                data = CipherUtil.decrypt(data, decryptKey,
//                        CipherAlgorithm.valueOf(algorithm));
//            }
//        } catch (IllegalArgumentException ie) {
//            OSSException ossException = new OSSException(ie);
//            ossException.setErrorCode(OSSErrorCode.DECRYPT_KEY_NOT_EMPTY);
//            throw ossException;
//        } catch (Exception e) {
//            OSSException ossException = new OSSException(e);
//            ossException.setErrorCode(OSSErrorCode.DECRYPT_ERROR);
//            throw ossException;
//        } finally {
//            this.releaseHttpClient();
//        }
//
//        try {
//            // 解压缩
//            String compressMethod = obj.getObjectMetaData().getAttrs()
//                    .get(X_OSS_META_COMPRESS);
//            if (!Helper.isEmptyString(compressMethod)
//                    && compressMethod.equals("zip")) {
//                data = CompressUtils.unzipBytes(data);
//            }
//            obj.setData(data);
//        } catch (Exception e) {
//            OSSException ossException = new OSSException(e);
//            ossException.setErrorCode(OSSErrorCode.UNZIP_ERROR);
//            throw ossException;
//        } finally {
//            this.releaseHttpClient();
//        }
        
        return obj;
    }

    /** * @return the decryptKey */
    public byte[] getDecryptKey() {
        return decryptKey;
    }

    /** * @param decryptKey the decryptKey to set */
    public void setDecryptKey(byte[] decryptKey) {
        this.decryptKey = decryptKey;
    }
    public DatabaseAccessManager getDatabaseAccessManager() {
        return mDatabaseAccessManager;
    }
    public void setDatabaseAccessManager(DatabaseAccessManager mDatabaseAccessManager) {
        this.mDatabaseAccessManager = mDatabaseAccessManager;
    }
}

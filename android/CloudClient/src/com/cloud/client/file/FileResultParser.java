package com.cloud.client.file;

import com.aliyun.android.oss.OSSErrorCode;
import com.cloud.client.CloudUtil;

public class FileResultParser {
    public static int getResultCode(String codeMsg) {
        int code = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
        
        if (codeMsg == null) {
            return code;
        }

        if (codeMsg.equals(OSSErrorCode.ACCESS_DENIED)) {
            code = CloudUtil.CLOUD_FILE_ACCESS_DENIED;
        } else if (codeMsg.equals(OSSErrorCode.BUCKET_ALREADY_EXISTS)) {
            code = CloudUtil.CLOUD_FILE_BUCKET_ALREADY_EXISTS;
        } else if (codeMsg.equals(OSSErrorCode.BUCKET_NOT_EMPTY)) {
            code = CloudUtil.CLOUD_FILE_BUCKET_NOT_EMPTY;
        } else if (codeMsg.equals(OSSErrorCode.ENTITY_TOO_LARGE)) {
            code = CloudUtil.CLOUD_FILE_ENTITY_TOO_LARGE;
        } else if (codeMsg.equals(OSSErrorCode.INVALID_BUCKET_NAME)) {
            code = CloudUtil.CLOUD_FILE_INVALID_BUCKET_NAME;
        } else if (codeMsg.equals(OSSErrorCode.TOO_MANY_BUCKETS)) {
            code = CloudUtil.CLOUD_FILE_TOO_MANY_BUCKETS;
        } else if (codeMsg.equals(OSSErrorCode.INVALID_ACCESS_KEY_ID)) {
            code = CloudUtil.CLOUD_FILE_INVALID_ACCESS_KEY_ID;
        } else if (codeMsg.equals(OSSErrorCode.NO_SUCH_BUCKET)) {
            code = CloudUtil.CLOUD_FILE_NO_SUCH_BUCKET;
        } else if (codeMsg.equals(OSSErrorCode.INVALID_ARGUMENTS)) {
            code = CloudUtil.CLOUD_FILE_INVALID_ARGUMENTS;
        } else if (codeMsg.equals(OSSErrorCode.ENCRYPT_KEY_NOT_EMPTY)) {
            code = CloudUtil.CLOUD_FILE_ENCRYPT_KEY_NOT_EMPTY;
        } else if (codeMsg.equals(OSSErrorCode.ENCRYPT_ERROR)) {
            code = CloudUtil.CLOUD_FILE_ENCRYPT_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.DECRYPT_KEY_NOT_EMPTY)) {
            code = CloudUtil.CLOUD_FILE_DECRYPT_KEY_NOT_EMPTY;
        } else if (codeMsg.equals(OSSErrorCode.DECRYPT_ERROR)) {
            code = CloudUtil.CLOUD_FILE_DECRYPT_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.ZIP_ERROR)) {
            code = CloudUtil.CLOUD_FILE_ZIP_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.UNZIP_ERROR)) {
            code = CloudUtil.CLOUD_FILE_UNZIP_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.PARSE_METADATA_ERROR)) {
            code = CloudUtil.CLOUD_FILE_PARSE_METADATA_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.PARSE_XML_ERROR)) {
            code = CloudUtil.CLOUD_FILE_PARSE_XML_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.GET_ENTITY_CONTENT_ERROR)) {
            code = CloudUtil.CLOUD_FILE_GET_ENTITY_CONTENT_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.ETAG_HEADER_EMPTY)) {
            code = CloudUtil.CLOUD_FILE_ETAG_HEADER_EMPTY;
        } else if (codeMsg.equals(OSSErrorCode.NO_UPLOADID_RETURN)) {
            code = CloudUtil.CLOUD_FILE_NO_UPLOADID_RETURN;
        } else if (codeMsg.equals(OSSErrorCode.PART_NUMBER_EXEEDED)) {
            code = CloudUtil.CLOUD_FILE_PART_NUMBER_EXEEDED;
        } else if (codeMsg.equals(OSSErrorCode.IOEXCEPTION)) {
            code = CloudUtil.CLOUD_FILE_IOEXCEPTION;
        } else if (codeMsg.equals(OSSErrorCode.DATABASE_ERROR)) {
            code = CloudUtil.CLOUD_FILE_DATABASE_ERROR;
        } else if (codeMsg.equals(OSSErrorCode.NO_SUCH_KEY)) {
            code = CloudUtil.CLOUD_FILE_NO_SUCH_KEY;
        } else {
            code = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
        }
        return code;
    }
}

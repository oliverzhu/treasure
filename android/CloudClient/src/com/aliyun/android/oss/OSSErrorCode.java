/**
 * Copyright (c) 2012 The Wiseserc. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.aliyun.android.oss;

/**
 * OSS定义的错误代码
 * 
 * @author Michael
 */
public interface OSSErrorCode {
    /**
     * 未知错误 
     */
    public static final String UNKNOWN_ERROR = "UnknownError";

    /**
     * 拒绝访问
     */
    public static final String ACCESS_DENIED = "AccessDenied";

    /**
     * Bucket已经存在
     */
    public static final String BUCKET_ALREADY_EXISTS = "BucketAlreadyExists";

    /**
     * Bucket不为空
     */
    public static final String BUCKET_NOT_EMPTY = "BucketNotEmpty";

    /**
     * 实体过大
     */
    public static final String ENTITY_TOO_LARGE = "EntityTooLarge";

    /**
     * Bucket不符合命名规范
     */
    public static final String INVALID_BUCKET_NAME = "InvalidBucketName";

    /**
     * 超过bucket最大创建数
     */
    public static final String TOO_MANY_BUCKETS = "TooManyBuckets";

    /**
     * 不合法的AccessKey和AccessId
     */
    public static final String INVALID_ACCESS_KEY_ID = "InvalidAccessKeyId";

    /**
     * Bucket不存在
     */
    public static final String NO_SUCH_BUCKET = "NoSuchBucket";
    
    /**
     * Key不存在
     */
    public static final String NO_SUCH_KEY = "NoSuchKey";
    /**
     * 非法参数
     */
    public static final String INVALID_ARGUMENTS = "InvalidArguments";
    
    /**
     * 加密密钥不为空
     */
    public static final String ENCRYPT_KEY_NOT_EMPTY = "EncryptKeyNotEmpty";
    
    /**
     * 加密错误
     */
    public static final String ENCRYPT_ERROR = "EncryptError";
    
    /**
     * 解密密钥不为空
     */
    public static final String DECRYPT_KEY_NOT_EMPTY = "DecryptKeyNotEmpty";
    
    /**
     * 解密错误
     */
    public static final String DECRYPT_ERROR = "DecryptError";

    /**
     * 压缩错误
     */
    public static final String ZIP_ERROR = "ZipError";
    
    /**
     * 解压缩错误
     */
    public static final String UNZIP_ERROR = "UnzipError";
    
    /**
     * 解析对象元信息错误
     */
    public static final String PARSE_METADATA_ERROR = "ParseMetaDataError";
    
    /**
     * 解析Xml错误
     */
    public static final String PARSE_XML_ERROR = "ParseXmlError";
    
    /**
     * 获取对象内容错误
     */
    public static final String GET_ENTITY_CONTENT_ERROR = "GetEntityContentError";
    
    /**
     * ETag header为空
     */
    public static final String ETAG_HEADER_EMPTY = "ETagHeaderEmpty";
    
    /**
     * 服务器未返回upload id
     */
    public static final String NO_UPLOADID_RETURN = "NoUploadIdReturn";
    
    /**
     * 分组数据超过上限
     */
    public static final String PART_NUMBER_EXEEDED = "PartNumberExeeded";
    
    /**
     * IO异常
     */
    public static final String IOEXCEPTION = "IOException";
    
    /**
     * 数据库错误
     */
    public static final String DATABASE_ERROR = "DatabaseError";
}

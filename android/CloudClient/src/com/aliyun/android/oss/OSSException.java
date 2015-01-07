/**
 * Copyright (c) 2012 The Wiseserc. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.aliyun.android.oss;

import com.aliyun.android.oss.model.HttpResponseError;

/**
 * 阿里云错误信息
 * 
 * @author Michael
 */
public class OSSException extends RuntimeException {
    /**
     * 序列号
     */
    private static final long serialVersionUID = 9049791916458818831L;

    public static final String NON_OSS_ERROR = "Non-OSS-Error";
    
    public static final String NET_OSS_ERROR = "Net-OSS-Error";
    
    /**
     * 错误类型
     */
    private String errorType;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * Host标识
     */
    private String hostId;

    /**
     * Request标识
     */
    private String requestId;

    /**
     * 构造新实例
     */
    public OSSException() {}

    /**
     * 用给定的异常信息构造新实例
     */
    public OSSException(String message) {
        super(message);
        this.setErrorType(NON_OSS_ERROR);
    }

    public OSSException(HttpResponseError error) {
        super(error.getMessage());
        this.setErrorType(NET_OSS_ERROR);
        this.setErrorCode(error.getCode());
        this.setHostId(error.getHostId());
        this.setRequestId(error.getRequestId());
    }
    
    /**
     * 用异常信息和表示异常原因的对象构造新实例
     */
    public OSSException(String message, Throwable cause) {
        super(message, cause);
        this.setErrorType(NON_OSS_ERROR);
    }

    /**
     * 用异常信息和表示异常原因及其他信息的对象构造新实例
     */
    public OSSException(String message, Throwable cause, 
            String errorType, String errorCode, String requestId, String hostId) {
        super(message, cause);
        this.errorType = errorType;
        this.errorCode = errorCode;
        this.requestId = requestId;
        this.hostId = hostId;
    }

    /**
     * 用表示异常原因的对象构造新实例
     */
    public OSSException(Throwable cause) {
        super(cause);
        this.setErrorType(NON_OSS_ERROR);
    }

    public void setErrorType(String type) {
        this.errorType = type;
    }
    
    public String getErrorType() {
        return this.errorType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}

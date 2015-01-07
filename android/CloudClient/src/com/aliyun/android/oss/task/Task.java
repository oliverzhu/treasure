/**
 * Copyright (c) 2012 The Wiseserc. All rights reserved. Use of this source code
 * is governed by a BSD-style license that can be found in the LICENSE file.
 */
package com.aliyun.android.oss.task;

import java.io.IOException;

import net.tsz.afinal.http.RetryHandler;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParserException;

import com.aliyun.android.oss.OSSErrorCode;
import com.aliyun.android.oss.OSSException;
import com.aliyun.android.oss.http.HttpMethod;
import com.aliyun.android.oss.http.IHttpHeaders;
import com.aliyun.android.oss.http.IHttpParameters;
import com.aliyun.android.oss.http.OSSHttpTool;
import com.aliyun.android.oss.model.HttpResponseError;
import com.aliyun.android.oss.xmlparser.HttpResponseErrorParser;

/**
 * OSS请求的任务基类
 * 
 * @author Michael
 */
public abstract class Task implements IHttpParameters, IHttpHeaders {

    public static String OSS_PROTOCOL = "http://";

    /**
     * OSS HOST
     */
    public static String OSS_HOST = "oss.aliyuncs.com";

    /**
     * 用户授权ID，匿名访问可以不设置
     */
    protected String accessId;

    /**
     * 用户授权key, 匿名访问可以不设置
     */
    protected String accessKey;

    /**
     * http请求方法
     */
    protected HttpMethod httpMethod;

    /**
     * Http请求工具
     */
    protected OSSHttpTool httpTool;

    /**
     * bucket名称
     */
    protected String bucketName;

    /**
     * Http客户端，用来执行请求方法
     */

    protected DefaultHttpClient client;
    
    private static int socketTimeout = 10 * 1000; //超时时间，默认10秒
    private static int maxRetries = 3;//错误尝试次数，错误异常表请在RetryHandler添加
    
    /**
     * 构造新实例
     * 
     * @param httpMethod
     *            任务请求方法的类型
     */
    public Task(HttpMethod httpMethod) {
        this(httpMethod, null);
    }

    public Task(HttpMethod httpMethod, String bucketName) {
        BasicHttpParams httpParams = new BasicHttpParams();
        ConnManagerParams.setTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
        HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
        
        httpTool = new OSSHttpTool();
        this.httpMethod = httpMethod;
        this.client = new DefaultHttpClient(httpParams);
        this.client.setHttpRequestRetryHandler(new RetryHandler(maxRetries));;
        this.bucketName = bucketName;
    }

    protected String getOSSEndPoint() {
        if (this.bucketName == null) {
            return String.format("%s%s", OSS_PROTOCOL, OSS_HOST);
        }

        return String
                .format("%s%s.%s", OSS_PROTOCOL, this.bucketName, OSS_HOST);
    }

    protected String getOSSHost() {
        if (this.bucketName == null) {
            return OSS_HOST;
        }

        return String.format("%s.%s", this.bucketName, OSS_HOST);
    }

    /**
     * 初始化AccessKey
     * 
     * @param accessKeyId
     * @param accessKeySecret
     */
    public void initKey(String accessKeyId, String accessKeySecret) {
        this.accessId = accessKeyId;
        this.accessKey = accessKeySecret;
    }

    /**
     * 执行任务，对于派生出来的任务类型，分别执行他们实现的抽象方法 由于对响应的处理有相似之处：如果返回码为200表示请求成功，由子类自己处理响应对象；
     * 否则出现错误，统一处理服务器返回xml格式错误信息并封装为 {@link OSSException}抛出
     * 
     * @return 服务器返回的响应对象
     * @throws OSSException
     */
    protected HttpResponse execute() throws OSSException {
        try {
            checkArguments();
        } catch (IllegalArgumentException ie) {
            OSSException ossException = new OSSException(ie);
            ossException.setErrorCode(OSSErrorCode.INVALID_ARGUMENTS);
            throw ossException;
        }

        try {
            HttpResponse r = this.client.execute(this.generateHttpRequest());
            if (r.getStatusLine().getStatusCode() / 100 <= 3) {
                return r;
            } else {
                HttpResponseError error = this.getResponseError(r);
                OSSException osse = new OSSException(error);
                throw osse;
            }
        } catch (OSSException osse) {
            throw osse;
        } catch (Exception e) {
            OSSException ossException = new OSSException(e);
            ossException.setErrorCode(OSSErrorCode.UNKNOWN_ERROR);
            throw new OSSException(ossException);
        }
    }

    /**
     * 仅在Http返回码不为200时调用，获取服务器返回的错误信息
     * 
     * @param response
     * @return 返回{@link HttpResponseError}对象，由{@link HttpResponseErrorParser}
     *         解析后获得
     * @throws IllegalStateException
     * @throws XmlPullParserException
     * @throws IOException
     */
    protected HttpResponseError getResponseError(HttpResponse response)
            throws IllegalStateException, XmlPullParserException, IOException {
        HttpResponseErrorParser parser = new HttpResponseErrorParser();
        HttpResponseError error = parser.parse(response.getEntity()
                .getContent());

        return error;
    }

    /**
     * 生成Http请求对象，根据请求方法不同可能是不同Method对应的对象
     */
    protected abstract HttpUriRequest generateHttpRequest();

    /**
     * 参数合法性验证
     */
    protected abstract void checkArguments();

    /**
     * 释放HttpClient，在Task完成后必须调用
     */
    protected void releaseHttpClient() {
        this.client.getConnectionManager().shutdown();
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}

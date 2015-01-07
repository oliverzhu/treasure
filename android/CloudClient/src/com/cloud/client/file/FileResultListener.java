package com.cloud.client.file;

public interface FileResultListener<T>{
    public void onJobDone(T result);
}

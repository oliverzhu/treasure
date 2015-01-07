package com.cloud.client.sql;

public interface SqlResultListener extends ISqlResultListener{
    public void onSuccess(String msg);
    public void onFailure(int errorNo, String msg);
}

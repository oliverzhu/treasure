package com.cloud.client.sql;

import com.cloud.client.CloudObject;

public interface SqlResultSingleListener<T extends CloudObject> extends
        ISqlResultListener {
    public void onSuccess(T entity, String msg);
    public void onFailure(int errorNo, String msg);
}

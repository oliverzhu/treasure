package com.cloud.client.sql;

import java.util.List;

import com.cloud.client.CloudObject;

public interface SqlResultMultiListener<T extends CloudObject> extends
        ISqlResultListener {
    public void onSuccess(List<T> entity, String msg);
    public void onFailure(int errorNo, String msg);
}

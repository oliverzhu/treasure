package com.cloud.client.file;

public interface MissionListener {
    public void transferred(long transferred);
    public void setTotalSize(long size);
    public boolean isCancel();
}

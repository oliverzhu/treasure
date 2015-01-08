package com.ape.cloudfile.module;

import com.cloud.client.CloudObject;

public class UserCloudSpace extends CloudObject
{
    private String userKey;
    private long cloudSpace;
    private long data1;
    private long data2;
    private long data3;

    public String getUserKey()
    {
        return userKey;
    }
    public void setUserKey(String userKey)
    {
        this.userKey = userKey;
    }
    public long getCloudSpace()
    {
        return cloudSpace;
    }
    public void setCloudSpace(long cloudSpace)
    {
        this.cloudSpace = cloudSpace;
    }
    public long getData1()
    {
        return data1;
    }
    public void setData1(long data1)
    {
        this.data1 = data1;
    }
    public long getData2()
    {
        return data2;
    }
    public void setData2(long data2)
    {
        this.data2 = data2;
    }
    public long getData3()
    {
        return data3;
    }
    public void setData3(long data3)
    {
        this.data3 = data3;
    }
}

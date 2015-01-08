package com.common.upgrade.job;

import android.content.Context;

import com.common.upgrade.utils.Constants;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/4/28
 */
public class CheckNewVersionJobWithoutClientUrl extends AbstractCheckNewVersionJob{
	public CheckNewVersionJobWithoutClientUrl(Context context,String appKey)
	{
		super(context,appKey);
	}
	
	@Override
	public String getCheckVersionUrl()
	{
		return Constants.NET_BASE
				+ Constants.UPGRADE_VERSION_URL + Constants.UPGRADE_VERSION_PARAMS;
	}

}

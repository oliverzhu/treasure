package com.common.upgrade.job;

import android.content.Context;

import com.common.upgrade.utils.Constants;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/4/28
 */
public class CheckNewVersionJobWithClientUrl extends AbstractCheckNewVersionJob{
	private String url;
	public CheckNewVersionJobWithClientUrl(Context context,String appKey,String url)
	{
		super(context,appKey);
		this.url = url;
	}
	
	@Override
	public String getCheckVersionUrl()
	{
		return url + Constants.UPGRADE_VERSION_PARAMS;
	}

}

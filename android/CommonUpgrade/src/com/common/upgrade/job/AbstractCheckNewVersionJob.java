package com.common.upgrade.job;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.common.upgrade.bean.UpgradeInfo;
import com.common.upgrade.parser.json.VersionJsonParsing;
import com.common.upgrade.utils.Constants;
import com.common.upgrade.utils.ContextUtils;
import com.common.upgrade.utils.NetUtils;
import com.common.upgrade.utils.Utils;
import com.common.upgrade.utils.thread.ThreadPool;
import com.common.upgrade.utils.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/4/28
 */
public abstract class AbstractCheckNewVersionJob implements ThreadPool.Job<UpgradeInfo>{
	protected Context mContext;
	protected String mAppKey;
	
	public AbstractCheckNewVersionJob(Context context,String appKey)
	{
		this.mContext = context;
		this.mAppKey = appKey;
	}
	

	@Override
	public UpgradeInfo run(JobContext jc) {
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = "";
		try {
			imei = telephonyManager.getDeviceId();
		} catch (SecurityException e1) {
			e1.printStackTrace();
		}
		
		if(imei == null || imei.trim().length() == 0)
		{
			imei = "0000000000";
		}
		
		UpgradeInfo upgradeInfo = null;
		try {
			if(imei != null && !"".equals(imei))
			{
				List<String> netParams = new ArrayList<String>();
				netParams.add(mAppKey);
				netParams.add(ContextUtils.getVersionCode(mContext) + "");
				netParams.add(imei);
				String uri = 
						Utils.combinaStr(getCheckVersionUrl(), netParams);
				JSONArray jsonArray = NetUtils.getJSONArrayByPost(uri);
				if(jsonArray != null && jsonArray.length() != 0)
				{
					VersionJsonParsing parser = new VersionJsonParsing();
					ArrayList<UpgradeInfo> upgradeInfos = parser.readJsonArray(jsonArray);
					if(upgradeInfos != null && upgradeInfos.size() != 0)
					{
						upgradeInfo =  upgradeInfos.get(0);
					}
				}
			}
		}
		catch(ConnectTimeoutException e)
		{
			upgradeInfo = new UpgradeInfo();
			upgradeInfo.setErrorCode(Constants.ERROR_CODE_NET);
			e.printStackTrace();
		}
		catch(UnknownHostException e)
		{
			upgradeInfo = new UpgradeInfo();
			upgradeInfo.setErrorCode(Constants.ERROR_CODE_NET);
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		} 
		return upgradeInfo;
	}
	
	public abstract String getCheckVersionUrl();

}

package com.client.customerservicecenter.job;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.hub.UserServiceHub;
import com.client.customerservicecenter.hub.UserServiceHub.onQueryUserInfoListener;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 获取userKey
 * @author jianwen.zhu
 * @since 2014/9/16
 */
public  class LoadUserKeyJob implements ThreadPool.Job<Void>{
	protected Context mContext;
	private Handler mHandler;
	private UserServiceHub mServiceHub;
	private SharedPreferences mPref;
	public LoadUserKeyJob(Context context,Handler handler,UserServiceHub serviceHub)
	{
		this.mContext = context;
		this.mHandler = handler;
		this.mServiceHub = serviceHub;
		mPref = mContext.getSharedPreferences(Constants.PREFERENCE_USER_INFO, Context.MODE_APPEND);
	}
	

	@Override
	public Void run(JobContext jc) {
		if(!NetUtils.isNetWorkAvailable(mContext, null))
		{
			Map userInfoMap = Preferences.getUserInfo(mPref);
			sendUserInfoMessage(userInfoMap,true);
		}else
		{
			mServiceHub.getUserKey(mContext, new onQueryUserInfoListener() {
				
				@Override
				public void onQueryUserInfo(Map userInfoMap) {
					sendUserInfoMessage(userInfoMap,false);
				}
			});
		}
		
		return null;
	}
	
	private void sendUserInfoMessage(Map userInfoMap,boolean cache)
	{
		if(!checkUserLegal(userInfoMap))
		{
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_USER_KEY_FAIL;
			mHandler.sendMessage(msg);
		}else
		{
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_USER_KEY_SUCCESS;
			msg.obj = userInfoMap;
			mHandler.sendMessage(msg);
			if(!cache)
			{
				Preferences.setUserInfo(mPref, userInfoMap);
			}
		}
	}
	
	private boolean checkUserLegal(Map userInfoMap)
	{
		if(userInfoMap == null || userInfoMap.size() == 0 || "-1".equals(userInfoMap.get("userkey")))
		{
			return false;
		}
		return true;
	}
}

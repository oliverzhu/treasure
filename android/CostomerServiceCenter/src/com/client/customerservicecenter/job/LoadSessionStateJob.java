package com.client.customerservicecenter.job;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.hub.SessionServiceHub;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 获取用户当前登录状态
 * @author jianwen.zhu
 * @since 2014/11/13
 */
public  class LoadSessionStateJob implements ThreadPool.Job<Void>{
	protected Context mContext;
	private Handler mHandler;
	private SessionServiceHub mServiceHub;
	private int mPosition;
	
	public LoadSessionStateJob(Context context,
			Handler handler,
			SessionServiceHub serviceHub)
	{
		this.mContext = context;
		this.mHandler = handler;
		this.mServiceHub = serviceHub;
	}
	
	public LoadSessionStateJob(Context context,
			Handler handler,
			SessionServiceHub serviceHub,
			int position)
	{
		this.mContext = context;
		this.mHandler = handler;
		this.mServiceHub = serviceHub;
		this.mPosition = position;
	}
	
	@Override
	public Void run(JobContext jc) {
		boolean sessionState = mServiceHub.isSessionValid(mContext);
		Message msg = mHandler.obtainMessage();
		msg.what = Constants.MSG_SESSION_INVALID;
		if(sessionState)
		{
			msg.what = Constants.MSG_SESSION_VALID;
			msg.arg1 = mPosition;
		}
		mHandler.sendMessage(msg);
		return null;
	}
	
}

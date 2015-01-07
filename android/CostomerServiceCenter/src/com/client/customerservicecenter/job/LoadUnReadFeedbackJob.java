package com.client.customerservicecenter.job;

import com.client.customerservicecenter.db.handler.CommentInfoHandler;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/6/23
 */
public  class LoadUnReadFeedbackJob implements ThreadPool.Job<Integer>{
	private String mUserId;
	public LoadUnReadFeedbackJob(String userId)
	{
		this.mUserId = userId;
	}
	

	@Override
	public Integer run(JobContext jc) {
		try {
			int count= 
					CommentInfoHandler.getInstance().queryUnReadCount(mUserId);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}

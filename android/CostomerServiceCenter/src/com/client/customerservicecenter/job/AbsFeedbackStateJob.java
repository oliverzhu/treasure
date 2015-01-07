package com.client.customerservicecenter.job;

import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.db.handler.CommentInfoHandler;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 改变反馈状态
 * @author jianwen.zhu
 * @since 2014/9/17
 */
public  abstract class AbsFeedbackStateJob implements ThreadPool.Job<Void>{
	protected CommentInfo commentInfo;
	public AbsFeedbackStateJob(CommentInfo commentInfo)
	{
		this.commentInfo = commentInfo;
	}
	

	@Override
	public Void run(JobContext jc) {
		try {
			changeState();
			CommentInfoHandler.getInstance().update(commentInfo);
			notifyStateChange();
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	protected abstract void changeState();
	
	protected void notifyStateChange() {
		
	}
}

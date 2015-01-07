package com.client.customerservicecenter.job;

import java.util.ArrayList;

import android.content.Context;

import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.db.handler.CommentInfoHandler;
import com.client.customerservicecenter.db.handler.FeedbackScheduleHandler;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/9/15
 */
public  class LoadLocalFeedbackDataJob implements ThreadPool.Job<ArrayList<CommentInfo>>{
	protected Context mContext;
	protected String userId;
	
	public LoadLocalFeedbackDataJob(Context context,String userId)
	{
		this.mContext = context;
		this.userId = userId;
	}
	

	@Override
	public ArrayList<CommentInfo> run(JobContext jc) {
		ArrayList<CommentInfo> infos = null;
		try {
			infos = (ArrayList<CommentInfo>) CommentInfoHandler.getInstance().queryAllByUid(userId);
			if(infos != null && infos.size() != 0)
			{
				for(int i = 0;i < infos.size();i++)
				{
					CommentInfo commentInfo = infos.get(i);
					ArrayList<FeedbackScheduleInfo> schedule = 
							(ArrayList<FeedbackScheduleInfo>) FeedbackScheduleHandler.getInstance().queryAllByFeedbackId(commentInfo.getId());
					if(schedule != null && schedule.size() != 0)
					{
						commentInfo.setInfos(schedule);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infos;
	}
}

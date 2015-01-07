package com.client.customerservicecenter.job;

import java.util.ArrayList;

import android.content.Context;

import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.bean.LoginUserInfo;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.db.handler.FaqHandler;
import com.client.customerservicecenter.db.handler.FaqScheduleHandler;
import com.client.customerservicecenter.db.handler.LoginUserInfoHandler;
import com.client.customerservicecenter.db.handler.PictureHandler;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 加载本地常见问题列表
 * @author jianwen.zhu
 * @since 2014/9/15
 */
public  class LoadLocalFeedbackFaqJob implements ThreadPool.Job<ArrayList<CommentInfo>>{
	protected Context mContext;
	private String mArg;
	
	public LoadLocalFeedbackFaqJob(Context context,String arg)
	{
		this.mContext = context;
		this.mArg = arg;
	}
	

	@Override
	public ArrayList<CommentInfo> run(JobContext jc) {
		ArrayList<CommentInfo> infos = null;
		try {
			infos = (ArrayList<CommentInfo>) FaqHandler.getInstance().queryAllByArg(mArg);
			if(infos != null && infos.size() != 0)
			{
				LoginUserInfo obj = new LoginUserInfo();
				for(int i = 0;i < infos.size();i++)
				{
					CommentInfo commentInfo = infos.get(i);
					
					ArrayList<Picture> pictures = 
							(ArrayList<Picture>) PictureHandler.getInstance().queryAllByFeedbackId(commentInfo.getId() + "");
					if(pictures != null && pictures.size() != 0)
					{
						commentInfo.setPicture(pictures);
					}
					
					String userId = commentInfo.getUserId();
					if(userId != null && userId.trim().length() != 0 
							&& !"null".equals(userId) && !"NULL".equals(userId))
					{
						obj.setId(Integer.valueOf(userId));
						LoginUserInfo userInfos = LoginUserInfoHandler.getInstance().query(obj);
						commentInfo.setLoginUserInfo(userInfos);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return infos;
	}
}

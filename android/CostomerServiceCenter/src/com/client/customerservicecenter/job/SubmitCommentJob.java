package com.client.customerservicecenter.job;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 追加反馈
 * @author jianwen.zhu
 * @since 2014/9/17
 */
public  class SubmitCommentJob implements ThreadPool.Job<Boolean>{
	private FeedbackScheduleInfo commentInfo;
	public SubmitCommentJob(FeedbackScheduleInfo commentInfo)
	{
		this.commentInfo = commentInfo;
	}
	

	@Override
	public Boolean run(JobContext jc) {
		try {
			return comment(commentInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 评论
	 * @param bean
	 * @return
	 */
	private  boolean comment(FeedbackScheduleInfo bean) {
		String path =  Constants.URL_FEEDBACK_CHAT;
		// 将用户名和密码放入HashMap中
		if (bean != null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("comment", bean.getComment());
			params.put("fid", bean.getFeedbackId() + "");
			try {
				return NetUtils.uploadParamsByPost(path, params);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
}

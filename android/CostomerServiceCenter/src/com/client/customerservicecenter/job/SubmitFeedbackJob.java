package com.client.customerservicecenter.job;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 发送反馈
 * @author jianwen.zhu
 * @since 2014/9/1
 */
public  class SubmitFeedbackJob implements ThreadPool.Job<Boolean>{
	private CommentInfo commentInfo;
	public SubmitFeedbackJob(CommentInfo commentInfo)
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
	private  boolean comment(CommentInfo bean) {
		String path =  Constants.URL_FEEDBACK_SUBMIT;
		// 将用户名和密码放入HashMap中
		if (bean != null) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("comment", bean.getComment());
			params.put("imei", bean.getImei());
			params.put("version", bean.getVersion());
			params.put("customVersion", bean.getCustomVersion());
			params.put("model", bean.getModel());
			params.put("type", bean.getType());
			params.put("arg0", bean.getArg0());
			params.put("arg1", bean.getArg1());
			params.put("arg2", bean.getArg2());
			params.put("uid", bean.getUserId());
			if(bean.getPhone() != null && bean.getPhone().trim().length() != 0)
			{
				params.put("phone", bean.getPhone());
			}
			if(bean.getEmail() != null && bean.getEmail().trim().length() != 0)
			{
				params.put("email", bean.getEmail());
			}
			
			if(bean.getPicture() != null && bean.getPicture().size() > 1)
			{
				params.put("picture", getJsonPicPath(bean.getPicture()));
			}
//			String picture = "[{\"id\":0,\"key\":\"2202050114\"},{\"id\":0,\"key\":\"2202050115\"},{\"id\":0,\"key\":\"2202050116\"}]";
//			params.put("picture", picture);
			
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
	
	private String getJsonPicPath(ArrayList<Picture>  pics)
	{
		JSONArray jsonArray = new JSONArray();
		for(int i = 1;i < pics.size();i++)
		{
			JSONObject jsonItem = new JSONObject();
			String path = pics.get(i).path;
			String fileName = path.substring((path.lastIndexOf("/") + 1));
			try {
				jsonItem.put("key",fileName);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonItem);
		}
		return jsonArray.toString();
	}
}

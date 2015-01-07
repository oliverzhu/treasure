package com.client.customerservicecenter.parser.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.db.handler.CommentInfoHandler;
import com.client.customerservicecenter.db.handler.FeedbackScheduleHandler;

public class NetFeedbackDataParsing extends AbstractJsonParsing<CommentInfo> {

	@Override
	protected CommentInfo readJsonItem(JSONObject item) throws JSONException {
		boolean isNewComment = false; 
		CommentInfo commentBean = new CommentInfo();
		int id = Integer.parseInt(item.getString("id"));
		String comment = item.getString("comment"); //评论内容
		if(comment != null && comment.contains("'"))
		{
			comment.replaceAll("'", "''");
		}
		String imei = item.getString("imei");
		String version = item.getString("version");
		String customVersion = item.getString("customVersion");
		String model = item.getString("model");
		String type = item.getString("type");
		String arg0 = item.getString("arg0");
		String arg1 = item.getString("arg1");
		String arg2 = item.getString("arg2");
		String status = item.getString("status");
		String commentDate = item.getString("commentDate");
		String userId = item.getString("userId");
		JSONArray infos = new JSONArray(item.getString("infos"));
		if(infos != null && infos.length() != 0)
		{
			for(int j = 0;j < infos.length();j++)
			{
				FeedbackScheduleInfo info = new FeedbackScheduleInfo();
				JSONObject obj = infos.getJSONObject(j);
				info.setId(Integer.valueOf(obj.getString("id")));
				String scheduleComment = obj.getString("comment");
				if(scheduleComment != null && scheduleComment.contains("'"))
				{
					scheduleComment = scheduleComment.replaceAll("'", "''");
				}
				info.setComment(scheduleComment);
				info.setCommentDate(obj.getString("commentDate"));
				info.setFeedbackId(obj.getString("feedbackId") != null ? Integer.valueOf(obj.getString("feedbackId")) : 0);
				info.setIdentity(obj.getString("identity") != null ? Integer.valueOf(obj.getString("identity")) : 0);
				if(FeedbackScheduleHandler.getInstance().add(info))
				{
					isNewComment = true;
				}
			}
		}
		
		commentBean.setId(id);
		commentBean.setComment(comment);
		commentBean.setImei(imei);
		commentBean.setVersion(version);
		commentBean.setCustomVersion(customVersion);
		commentBean.setModel(model);
		commentBean.setType(type);
		commentBean.setArg0(arg0);
		commentBean.setArg1(arg1);
		commentBean.setArg2(arg2);
		commentBean.setStatus(status != null ? Integer.valueOf(status) : 0);
		commentBean.setCommentDate(commentDate);
		commentBean.setUserId(userId);
		
		CommentInfo cBean = CommentInfoHandler.getInstance().query(commentBean);
		if(cBean == null)
		{
			if(isNewComment)
			{
				commentBean.setReadState(0);
			}else
			{
				commentBean.setReadState(1);
			}
			 CommentInfoHandler.getInstance().add(commentBean);
		}else
		{
			if(isNewComment)
			{
				cBean.setReadState(0);
			}
			cBean.setStatus(commentBean.getStatus());
			CommentInfoHandler.getInstance().update(cBean);
		}
		return commentBean;
	}

}

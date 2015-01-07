package com.client.customerservicecenter.parser.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.bean.LoginUserInfo;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.db.handler.FaqHandler;
import com.client.customerservicecenter.db.handler.FaqScheduleHandler;
import com.client.customerservicecenter.db.handler.LoginUserInfoHandler;
import com.client.customerservicecenter.db.handler.PictureHandler;
import com.client.customerservicecenter.util.Constants;

/**
 * 解析常见问题json
 * @author jianwen.zhu
 * @since 2014/9/22
 */
public class NetFeedbackFaqParsing extends AbstractJsonParsing<CommentInfo> {
	private Context mContext;
	
	public NetFeedbackFaqParsing(Context context)
	{
		mContext = context;
	}

	@Override
	protected CommentInfo readJsonItem(JSONObject item) throws JSONException {
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
		String reply = "";
		
		String jsonInfos = item.getString("infos");
		if(jsonInfos != null && jsonInfos.trim().length() != 0 
				&& !"null".equals(jsonInfos))
		{
			JSONArray infos = new JSONArray(jsonInfos);
			if(infos != null && infos.length() != 0)
			{
				for(int j = 0;j < infos.length();j++)
				{
					FeedbackScheduleInfo info = new FeedbackScheduleInfo();
					JSONObject obj = infos.getJSONObject(j);
					info.setId(Integer.valueOf(obj.getString("id")));
					reply = obj.getString("comment");
					
					int identity = obj.getString("identity") != null ? Integer.valueOf(obj.getString("identity")) : -1;
					if(identity == 0)
					{
						if(reply != null && reply.contains("'"))
						{
							reply = reply.replaceAll("'", "''");
						}
						info.setComment(reply);
						info.setCommentDate(obj.getString("commentDate"));
						info.setFeedbackId(obj.getString("feedbackId") != null ? Integer.valueOf(obj.getString("feedbackId")) : 0);
						info.setIdentity(identity);
						FaqScheduleHandler.getInstance().add(info);
						break;
					}
				}
			}
		}
		
		String jsonPicture = item.getString("picture");
		if(jsonPicture != null && jsonPicture.trim().length() != 0 
				&& !"null".equals(jsonPicture))
		{
			JSONArray picutures = new JSONArray(jsonPicture);
			if(picutures != null && picutures.length() != 0)
			{
				for(int j = 0;j < picutures.length();j++)
				{
					Picture picture = new Picture();
					JSONObject obj = picutures.getJSONObject(j);
					picture.setId(Integer.valueOf(obj.getString("id")));
					picture.setKey(obj.getString("key"));
					picture.setFeedbackId(obj.getString("feedbackId"));
					PictureHandler.getInstance().add(picture);
				}
			}
		}
		
		String jsonLoginUserInfo = item.getString("loginUserInfo");
		if(jsonLoginUserInfo != null 
				&& jsonLoginUserInfo.trim().length() != 0 
				&& !"null".equals(jsonLoginUserInfo))
		{
			JSONObject loginUser = new JSONObject(jsonLoginUserInfo);
			if(loginUser != null)
			{
				LoginUserInfo loginUserInfo = new LoginUserInfo();
				loginUserInfo.setId(Integer.valueOf(loginUser.getString("id")));
				loginUserInfo.setNickName(loginUser.getString("nickName"));
				loginUserInfo.setHeadImg(loginUser.getString("headImg"));
				loginUserInfo.setAccessId(loginUser.getString("accessId"));
				loginUserInfo.setSecretKey(loginUser.getString("secretKey"));
				loginUserInfo.setBucketName(loginUser.getString("bucketName"));
				loginUserInfo.setOssType(loginUser.getString("ossType"));
				loginUserInfo.setOssLocal(loginUser.getString("ossLocal"));
				LoginUserInfoHandler.getInstance().add(loginUserInfo);
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
		commentBean.setReply(reply);
		commentBean.setTypeStr(getTypeStr(Integer.valueOf(type)));
		
		CommentInfo cBean = FaqHandler.getInstance().query(commentBean);
		if(cBean == null)
		{
			FaqHandler.getInstance().add(commentBean);
		}else
		{
			FaqHandler.getInstance().update(cBean);
		}
		return commentBean;
	}
	
	private String getTypeStr(int type)
	{
		String issueType = "";
		switch (type) {
		case Constants.TYPE_SYSTEM:
			issueType = mContext.getResources().getString(R.string.error_system);
			break;
		case Constants.TYPE_BATTERY:
			issueType = mContext.getResources().getString(R.string.error_battery);
			break;
		case Constants.TYPE_PHONE:
			issueType = mContext.getResources().getString(R.string.error_phone);
			break;
		case Constants.TYPE_NET:
			issueType = mContext.getResources().getString(R.string.error_net);
			break;
		case Constants.TYPE_APPLICATION:
			issueType = mContext.getResources().getString(R.string.error_application);
			break;
		case Constants.TYPE_DATA:
			issueType = mContext.getResources().getString(R.string.error_data);
			break;
		case Constants.TYPE_OTHERS:
			issueType = mContext.getResources().getString(R.string.error_others);
			break;
		case Constants.TYPE_SUGAR_ADVISE:
			issueType = mContext.getResources().getString(R.string.sugar_advise);
			break;

		default:
			break;
		}
		return issueType;
	}

}

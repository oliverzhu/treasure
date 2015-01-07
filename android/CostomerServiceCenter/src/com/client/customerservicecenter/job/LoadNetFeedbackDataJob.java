package com.client.customerservicecenter.job;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.client.customerservicecenter.parser.json.NetFeedbackDataParsing;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Utils;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/6/23
 */
public  class LoadNetFeedbackDataJob implements ThreadPool.Job<Integer>{
	protected Context mContext;
	private String userId;
	public LoadNetFeedbackDataJob(Context context,String userId)
	{
		this.mContext = context;
		this.userId = userId;
	}
	

	@Override
	public Integer run(JobContext jc) {
		Integer RESULT = Constants.FAIL;
		try {
			if(userId != null && !"".equals(userId) 
					&& NetUtils.isNetWorkAvailable(mContext, null))
			{
				List<String> netParams = new ArrayList<String>();
				netParams.add(userId);
				String uri = 
						Utils.combinaStr(
								Constants.URL_FEEDBACK_DATA_BY_UID, netParams);
				JSONArray jsonArray = NetUtils.getJSONArrayByPost(uri);
				if(jsonArray != null && jsonArray.length() != 0)
				{
					new NetFeedbackDataParsing().readJsonArray(jsonArray);
				}
				RESULT = Constants.SUCCESS;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return RESULT;
	}
}

package com.client.customerservicecenter.job;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.client.customerservicecenter.parser.json.NetFeedbackFaqParsing;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Utils;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * 获取常见问题
 * @author jianwen.zhu
 * @since 2014/9/22
 */
public  class LoadNetFeedbackFaqJob implements ThreadPool.Job<Integer>{
	protected Context mContext;
	public LoadNetFeedbackFaqJob(Context context)
	{
		this.mContext = context;
	}
	

	@Override
	public Integer run(JobContext jc) {
		Integer RESULT = Constants.FAIL;
		if(!NetUtils.isNetWorkAvailable(mContext, null))
		{
			RESULT = Constants.NET_NOT_AVAILABLE;
		}else
		{
			String version = ContextUtils.getInnerVersion();
			try {
				if(version != null && !"".equals(version))
				{
					List<String> netParams = new ArrayList<String>();
					netParams.add(version);
					String uri = 
							Utils.combinaStr(
									Constants.URL_FEEDBACK_FAQ_BY_VERSION, netParams);
					JSONArray jsonArray = NetUtils.getJSONArrayByPost(uri);
					if(jsonArray != null && jsonArray.length() != 0)
					{
						new NetFeedbackFaqParsing(mContext).readJsonArray(jsonArray);
					}
				}
				RESULT = Constants.SUCCESS;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return RESULT;
	}
}

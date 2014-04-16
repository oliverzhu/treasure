package com.home.job;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.home.bean.OperatingBean;
import com.home.parser.json.JSONParsingService;
import com.home.url.RequestUrlProvider;
import com.home.util.NetUtils;
import com.home.util.Utils;
import com.home.util.log.Log;
import com.home.util.thread.ThreadPool;
import com.home.util.thread.ThreadPool.JobContext;

/**
 * @author jianwen.zhu
 * 2013/12/10
 */
public class LoadMessagesJob implements ThreadPool.Job<ArrayList<OperatingBean>>{
	private static final String TAG = "LoadMessagesJob";

	@Override
	public ArrayList<OperatingBean> run(JobContext jc) {
		try {
			String readTime = "1990-3-28 00:00:00";
			String encodeReadTime= URLEncoder.encode(readTime, "UTF-8");   //这里是讲String编码成UTF-8
			encodeReadTime=encodeReadTime.replaceAll("\\+","%20");  
			List<String> params = new ArrayList<String>();
			params.add("5");
			params.add(encodeReadTime);
			//debug版本
			params.add("1");
			String uri = 
					Utils.combinaStr(RequestUrlProvider.NOTIFICATION_URL, params);
			JSONArray jsonArray = NetUtils.getJSONArrayByPost(uri);
			ArrayList<OperatingBean> beans = null;
			if(jsonArray != null && jsonArray.length() != 0)
			{
				beans = JSONParsingService.getInstance().readMessageJSONArray(jsonArray);
			}
			return beans;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "load message error", Log.APP);
		} 
		return null;
	}

}

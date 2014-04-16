package com.home.parser.json;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.home.bean.OperatingBean;
import com.home.provider.handler.OperateHandler;
import com.home.url.RequestUrlProvider;
import com.home.util.log.Log;

public class JSONParsingService {
	private static final String TAG = "JSONParsingService";
	private static JSONParsingService spf;

	public static JSONParsingService getInstance() {
		if (spf == null) {
			spf = new JSONParsingService();
		}
		return spf;
	}
	
	private JSONParsingService()
	{
		
	}

	/**
	 * 输入InputStream
	 * 
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public ArrayList<OperatingBean> readMessageJSONArray(JSONArray jsonArray) {
		
		try {
			ArrayList<OperatingBean> listOperating = new ArrayList<OperatingBean>();
			
			for(int i = 0;i < jsonArray.length();i++)
			{
				OperatingBean operatingBean = new OperatingBean();
				JSONObject item = jsonArray.getJSONObject(i);
				String odrid = "";
				try {
					odrid = item.getString("odrid");
				} catch (Exception e) {
					e.printStackTrace();
				}
				String order = item.getString("order");
				String desc = item.getString("desc");
				String msg = item.getString("msg");
				String url = item.getString("url");
				String path = "";
				if(url != null && url.trim().length() != 0)
				{
					path = RequestUrlProvider.BASE + url.substring(url.indexOf("ADMServer") - 1);
				}
				String link = item.getString("link");
				String cdate = item.getString("cdate");
				
				operatingBean.setOdrid(odrid);
				operatingBean.setOperatOrder(order.trim());
				operatingBean.setOperatDesc(desc);
				operatingBean.setOperatMsg(msg);
				operatingBean.setOperatURL(path);
				operatingBean.setOperateLink(link);
				operatingBean.setDateCreation(cdate);
				
				//保存到数据库
				OperateHandler.getInstance().add(operatingBean);
				listOperating.add(operatingBean);
			}
			return listOperating;
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "json parse error", Log.APP);
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "other exception", Log.APP);
		}
		return null;
	}
}

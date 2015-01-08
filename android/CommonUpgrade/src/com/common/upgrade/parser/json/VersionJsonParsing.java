package com.common.upgrade.parser.json;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.common.upgrade.bean.LanguageDescriptionInfo;
import com.common.upgrade.bean.LanguageVersionInfo;
import com.common.upgrade.bean.UpgradeInfo;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/4/29
 */
public class VersionJsonParsing extends AbstractJsonParsing<UpgradeInfo> {

	@Override
	protected UpgradeInfo readJsonItem(JSONObject jsonItem) throws JSONException{
		UpgradeInfo upgradeInfo = new UpgradeInfo();
		String url = jsonItem.getString("url");
		String version = jsonItem.getString("version");
		String result = jsonItem.getString("result");
		String versionName = jsonItem.getString("versionName");
		String mustUpdate = jsonItem.getString("mustUpdate");
		
		List<LanguageVersionInfo> listVersionInfos = null;
		JSONArray versionInfos = new JSONArray(jsonItem.getString("versionName"));
		if(versionInfos != null && versionInfos.length() != 0)
		{
			listVersionInfos = new ArrayList<LanguageVersionInfo>();
			for(int j = 0;j < versionInfos.length();j++)
			{
				LanguageVersionInfo info = new LanguageVersionInfo();
				JSONObject obj = versionInfos.getJSONObject(j);
				info.setRegion(obj.getString("region"));
				info.setValue(obj.getString("value"));
				listVersionInfos.add(info);
			}
			upgradeInfo.setLanguagesVersionInfos(listVersionInfos);
		}
		
		List<LanguageDescriptionInfo> listDesInfos = null;
		JSONArray desInfos = new JSONArray(jsonItem.getString("description"));
		if(desInfos != null && desInfos.length() != 0)
		{
			listDesInfos = new ArrayList<LanguageDescriptionInfo>();
			for(int j = 0;j < desInfos.length();j++)
			{
				LanguageDescriptionInfo info = new LanguageDescriptionInfo();
				JSONObject obj = desInfos.getJSONObject(j);
				info.setRegion(obj.getString("region"));
				info.setValue(obj.getString("value"));
				listDesInfos.add(info);
			}
			upgradeInfo.setLanguagesDescriptionInfos(listDesInfos);
		}
		
		upgradeInfo.setUrl(url);
		upgradeInfo.setVersion(version);
		upgradeInfo.setResult(result);
		upgradeInfo.setVersionName(versionName);
		upgradeInfo.setMustUpdate(mustUpdate);
		return upgradeInfo;
	}
}

package com.client.customerservicecenter.hub;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.client.bean.Card;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Constants.AuthenticListener;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.Utils;
import com.cloud.client.CloudClientService;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;
import com.cloud.client.sql.SqlResultListener;
import com.cloud.client.sql.SqlResultMultiListener;
import com.cloud.client.sql.SqlResultSingleListener;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/9/22
 */
public class CloudCustomerServiceHandler extends CloudClientService{
	private static CloudCustomerServiceHandler cloudLoginHandler;
	private static final String PACKAGE_NAME = "com.client.customerservicecenter";
	private static final String OWNER_KEY = "2876de12b0378979e42ded5b32474e74";
	private SharedPreferences mPref;
	
	public CloudCustomerServiceHandler(Context context, String askerAppKey) {
		super(context, askerAppKey);
		mPref = context.getSharedPreferences(Constants.PREFERENCE_CUSTOMER_SERVICE, 
				Context.MODE_APPEND);
	}
	
	public static CloudCustomerServiceHandler getInstance(Context context)
	{
		if(cloudLoginHandler == null)
		{
			cloudLoginHandler = new CloudCustomerServiceHandler(context,Constants.ASKER_KEY);
		}
		return cloudLoginHandler;
	}

	@Override
	protected String initOwnerKey() {
		return OWNER_KEY;
	}

	@Override
	protected String initPackageName() {
		return PACKAGE_NAME;
	}
	
	public void createUserTable(final AuthenticListener apiListener)
	{
		super.create(Card.class, new SqlResultListener() {
			
			@Override
			public void onSuccess(String msg) {
				Editor editor = mPref.edit();
				editor.putBoolean(Constants.KEY_TABLE_EXISTS, true);
				editor.commit();
				if(apiListener != null)
				{
					apiListener.onComplete(null);
				}
			}
			
			@Override
			public void onFailure(int errorNo, String msg) {
				if(apiListener != null)
				{
					apiListener.onException(errorNo, msg);
				}
			}
		});
	}
	
	private void getUser(final Card card,final AuthenticListener apiListener)
	{
		String where = "imei='" + card.getImei() + "'";
		super.getByWhere(Card.class, where, new SqlResultMultiListener<Card>(){

			@Override
			public void onSuccess(List<Card> entity, String msg) {
				if(entity == null || entity.size() == 0)
				{
					if(card.getType() == 1)
					{
						card.setActivateDate(getActivateDate());
					}
					insertUser(card,apiListener,card.getType());
				}else
				{
					Card queryCard = entity.get(0);
					if(card.getType() == 1 && queryCard.getType() ==1)
					{
						if(apiListener != null)
		            	{
							saveActivateDateToPref(queryCard);
							apiListener.onComplete("activate");
		            	}
						return;
					}else if(card.getType() == 1 && queryCard.getType() == 0)
					{
						queryCard.setActivateDate(getActivateDate());
						queryCard.setType(1);
						updateUser(queryCard, apiListener,1);
					}else if(card.getType() == 0 && queryCard.getType() == 0)
					{
						card.setId(queryCard.getId());
						updateUser(card, apiListener,0);
					}else if(card.getType() == 0 && queryCard.getType() == 1)
					{
						card.setId(queryCard.getId());
						card.setType(1);
						updateUser(card, apiListener,0);
					}
				}
			}

			@Override
			public void onFailure(int errorNo, String msg) {
				
			}});
	}
	
	public void getActivatedUser(final Card card,final AuthenticListener apiListener)
	{
		String where = "imei='" + card.getImei() + "'";
		super.getByWhere(Card.class, where, new SqlResultMultiListener<Card>(){

			@Override
			public void onSuccess(List<Card> entity, String msg) {
				if(apiListener == null)
				{
					return;
				}
				if(entity == null || entity.size() == 0)
				{
					apiListener.onComplete(Constants.MSG_ELECTROINICCARD_NOT_ACTIVATED);
				}else
				{
					Card queryCard = entity.get(0);
					if(queryCard.getType() == 1)
					{
						saveActivateDateToPref(queryCard);
						apiListener.onComplete(Constants.MSG_ELECTROINICCARD_ACTIVATED);
					}else 
					{
						apiListener.onComplete(Constants.MSG_ELECTROINICCARD_NOT_ACTIVATED);
					}
				}
			}

			@Override
			public void onFailure(int errorNo, String msg) {
				if(apiListener != null)
            	{
            		apiListener.onException(errorNo,msg);
            	}
			}});
	}
	
	private void insertUser(Card card,final AuthenticListener apiListener,final int type) {
		super.insert(card, new SqlResultSingleListener<Card>(){

			@Override
			public void onSuccess(Card entity, String msg) {
				if(apiListener != null)
            	{
					if(type == 0)
					{
						apiListener.onComplete("update");
					}else if(type == 1)
					{
						saveActivateDateToPref(entity);
						apiListener.onComplete("activate");
					}
            	}
			}

			@Override
			public void onFailure(int errorNo, String msg) {
				if(apiListener != null)
            	{
            		apiListener.onException(errorNo,msg);
            	}
			}});
		
	}
	
	private void updateUser(final Card card,final AuthenticListener apiListener,final int type)
	{
		super.update(card, new SqlResultListener() {
			
			@Override
			public void onSuccess(String msg) {
				if(apiListener != null)
            	{
					if(type == 0)
					{
						apiListener.onComplete("update");
					}else if(type == 1)
					{
						saveActivateDateToPref(card);
						apiListener.onComplete("activate");
					}
            	}
			}
			
			@Override
			public void onFailure(int errorNo, String msg) {
				if(apiListener != null)
            	{
            		apiListener.onException(errorNo,msg);
            	}
			}
		});
	}
	
	public void insertOrUpdateUser(Card card,
			final AuthenticListener apiListener) {
		getUser(card, apiListener);
	}
	
	private void saveActivateDateToPref(Card user)
	{
		long activateDate = Utils.getTimeMillions(user.getActivateDate());
		Editor editor = mPref.edit();
		editor.putLong(Constants.KEY_ACTIVATE_DATE, activateDate);
		editor.commit();
	}
	
	private String getActivateDate()
	{
		long activateDate = System.currentTimeMillis();
		if(Preferences.getBootTime(AppApplication.mPrefs) != -1)
		{
			activateDate = Preferences.getBootTime(AppApplication.mPrefs);
		}
		return Utils.formateDate(activateDate);
	}
	
	public int allocFileClient(Map userInfoMap) {
		return super.allocFileClient(userInfoMap); 
	}

	
	public MissionObject initMultipartUpload(String filePath, String key) {
        return super.initMultipartUpload(filePath, key);
    }
    
    public CloudFileResult multipartUploadFile(MissionObject missionObject,
            MissionListener listener) {
        return super.multipartUpload(missionObject, listener);
    }
    
    public MissionObject initDownload(String localFile, String key, boolean overwrite) {
        return super.initDownload(key, localFile, overwrite);
    }
    
    public CloudFileResult download(MissionObject missionObject,
            MissionListener listener) {
        return super.downloadFile(missionObject, listener);
    }
	
}

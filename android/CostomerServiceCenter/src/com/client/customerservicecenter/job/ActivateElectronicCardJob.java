package com.client.customerservicecenter.job;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.client.bean.Card;
import com.client.customerservicecenter.hub.CloudCustomerServiceHandler;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Constants.AuthenticListener;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;
import com.cloud.util.Log;

/**
 * 激活电子保单
 * @author jianwen.zhu
 * @since 2014/6/23
 */
public  class ActivateElectronicCardJob implements ThreadPool.Job<Void>{
	private static final String TAG = "ActivateElectronicCardJob";
	protected Context mContext;
	private Handler mHandler;
	
	private String mUserKey;
	
	public ActivateElectronicCardJob(Context context,Handler handler,String userKey)
	{
		this.mContext = context;
		this.mHandler = handler;
		this.mUserKey = userKey;
	}
	

	@Override
	public Void run(JobContext jc) {
		Looper.prepare();
		final AuthenticListener authenticListener = new AuthenticListener() {
			
			@Override
			public void onException(int errorNo, String message) {
				Message msg = mHandler.obtainMessage();
				msg.what = Constants.MSG_ACTIVATE_ELECTROINICCARD_FAIL;
				mHandler.sendMessage(msg);
			}
			
			@Override
			public void onComplete(Object obj) {
				Message msg = mHandler.obtainMessage();
				msg.what = Constants.MSG_ACTIVATE_ELECTROINICCARD_SUCCESS;
				msg.obj = obj;
				mHandler.sendMessage(msg);
				
			}
			
			@Override
			public void onCancel() {
				Message msg = mHandler.obtainMessage();
				msg.what = Constants.MSG_ACTIVATE_ELECTROINICCARD_FAIL;
				mHandler.sendMessage(msg);
			}
		};
		final CloudCustomerServiceHandler service = CloudCustomerServiceHandler.getInstance(mContext);
				//new CloudCustomerServiceHandler(mContext, Constants.ASKER_KEY);
		SharedPreferences pref = 
				mContext.getSharedPreferences(Constants.PREFERENCE_CUSTOMER_SERVICE, Context.MODE_APPEND);
		boolean isTableExist = pref.getBoolean(Constants.KEY_TABLE_EXISTS, false);
		Log.i(TAG, "isTableExist = " + isTableExist, Log.APP);
		
		final Card card = new Card();
		card.setImei(ContextUtils.getImei(mContext));
		card.setUserKey(mUserKey);
		card.setType(1);
		
		if(isTableExist)
		{
			service.insertOrUpdateUser(card, authenticListener);
		}else
		{
			service.createUserTable(new AuthenticListener() {
				
				@Override
				public void onException(int errorNo, String msg) {
					authenticListener.onException(errorNo, msg);
				}
				
				@Override
				public void onComplete(Object obj) {
					service.insertOrUpdateUser(card, authenticListener);
				}
				
				@Override
				public void onCancel() {
					authenticListener.onCancel();
					
				}
			});
		}
		Looper.loop();
		return null;
	}
}

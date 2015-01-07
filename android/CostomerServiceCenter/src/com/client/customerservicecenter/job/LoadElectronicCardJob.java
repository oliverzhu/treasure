package com.client.customerservicecenter.job;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.client.bean.Card;
import com.client.customerservicecenter.hub.CloudCustomerServiceHandler;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Constants.AuthenticListener;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;
import com.cloud.util.Log;

/**
 * 查询该imei是否被激活过
 * @author jianwen.zhu
 * @since 2014/11/3
 */
public  class LoadElectronicCardJob implements ThreadPool.Job<Void>{
	private static final String TAG = "LoadElectronicCardJob";
	protected Context mContext;
	private Handler mHandler;
	
	
	public LoadElectronicCardJob(Context context,Handler handler)
	{
		this.mContext = context;
		this.mHandler = handler;
	}
	

	@Override
	public Void run(JobContext jc) {
		if(isActivated())
		{
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_ELECTROINICCARD_ACTIVATED;
			msg.obj = getActivateDate() + Constants.EXPIRE_DATE;
			mHandler.sendMessage(msg);
		}else
		{
			if(!NetUtils.isNetWorkAvailable(mContext, null))
			{
				Message msg = mHandler.obtainMessage();
				msg.what = Constants.MSG_ELECTROINICCARD_SERVER_ERROR;
				mHandler.sendMessage(msg);
			}else
			{
				Looper.prepare();
				final AuthenticListener authenticListener = new AuthenticListener() {
					
					@Override
					public void onException(int errorNo, String message) {
						Message msg = mHandler.obtainMessage();
						msg.what = Constants.MSG_ELECTROINICCARD_SERVER_ERROR;
						mHandler.sendMessage(msg);
					}
					
					@Override
					public void onComplete(Object obj) {
						int what = (Integer) obj;
						Message msg = mHandler.obtainMessage();
						msg.what = what;
						msg.obj = getActivateDate() + Constants.EXPIRE_DATE;
						mHandler.sendMessage(msg);
					}
					
					@Override
					public void onCancel() {
						Message msg = mHandler.obtainMessage();
						msg.what = Constants.MSG_ELECTROINICCARD_SERVER_ERROR;
						mHandler.sendMessage(msg);
					}
				};
				final CloudCustomerServiceHandler service = CloudCustomerServiceHandler.getInstance(mContext);
				SharedPreferences pref = 
						mContext.getSharedPreferences(Constants.PREFERENCE_CUSTOMER_SERVICE, Context.MODE_APPEND);
				boolean isTableExist = pref.getBoolean(Constants.KEY_TABLE_EXISTS, false);
				Log.i(TAG, "isTableExist = " + isTableExist, Log.APP);
				
				final Card card = new Card();
				card.setImei(ContextUtils.getImei(mContext));
				
				if(isTableExist)
				{
					service.getActivatedUser(card, authenticListener);
				}else
				{
					service.createUserTable(new AuthenticListener() {
						
						@Override
						public void onException(int errorNo, String msg) {
							authenticListener.onException(errorNo, msg);
						}
						
						@Override
						public void onComplete(Object obj) {
							service.getActivatedUser(card, authenticListener);
						}
						
						@Override
						public void onCancel() {
							authenticListener.onCancel();
							
						}
					});
				}
				Looper.loop();
			}
		}
		return null;
	}
	
	private boolean isActivated()
	{
		if(getActivateDate() == -1)
		{
			return false;
		}
		SharedPreferences mPref = 
				mContext.getSharedPreferences(Constants.PREFERENCE_CUSTOMER_SERVICE, Context.MODE_APPEND);
		long expireDate = getActivateDate() + Constants.EXPIRE_DATE;
		long currentTime = System.currentTimeMillis();
		long delta = expireDate - currentTime;
		if(delta <= 0)
		{
			Editor editor = mPref.edit();
			editor.remove(Constants.KEY_ACTIVATE_DATE);
			editor.commit();
			return false;
		}
		return true;
	}
	
	private long getActivateDate()
	{
		SharedPreferences mPref = 
				mContext.getSharedPreferences(Constants.PREFERENCE_CUSTOMER_SERVICE, Context.MODE_APPEND);
		return mPref.getLong(Constants.KEY_ACTIVATE_DATE, -1);
	}
}

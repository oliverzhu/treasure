package com.client.customerservicecenter.fragment;

import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.FeedbackActivity;
import com.client.customerservicecenter.FeedbackListActivity;
import com.client.customerservicecenter.R;
import com.client.customerservicecenter.adapter.GridViewAdapter;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.GridViewItem;
import com.client.customerservicecenter.hub.SessionServiceHub;
import com.client.customerservicecenter.job.LoadNetFeedbackDataJob;
import com.client.customerservicecenter.job.LoadSessionStateJob;
import com.client.customerservicecenter.job.LoadUnReadFeedbackJob;
import com.client.customerservicecenter.job.LoadUserKeyJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;


/**
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public class FeedbackFragment extends BaseFragment{
	private GridView gridView;
	private GridViewAdapter gridViewAdapter;
	private ArrayList<GridViewItem> datas;
	
	private SessionServiceHub mSessionServiceHub;
	
	private FutureListener<Integer> mNetFeedbackDataListener = 
    		new FutureListener<Integer>() {
		@Override
		public synchronized void onFutureDone(Future<Integer> result) {
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_NET_FEEDBACK_DATA;
			mHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_SESSION_VALID:
				int position = msg.arg1;
				Bundle data = new Bundle();
				data.putString("operateType", datas.get(position).item_tag);
				data.putString("errorType", datas.get(position).item_type);
				setUserInfoData(data);
				Intent intent = new Intent();
				intent.putExtras(data);
				intent.setClass(mContext, FeedbackActivity.class);
				startActivity(intent);
				break;
			case Constants.MSG_SESSION_INVALID:
				Intent loginIntent = ContextUtils.getActivityIntent(mContext, 
						Constants.LOGIN_PACKAGE_NAME, Constants.LOGIN_LAUNCHER_ACTIVITY_NAME);
				if(loginIntent == null)
				{
					ContextUtils.showToast(mContext, R.string.toast_lack_login_error, Toast.LENGTH_SHORT);
					return;
				}
				Intent userActivity = new Intent();
				userActivity.setAction(Constants.ACTION_LOGIN_USER_ACCESS);
				startActivityForResult(userActivity,Constants.ACTIVITY_CODE_REQUEST_FOR_USERID);
				break;
			case Constants.MSG_GRIDVIEW_DATA:
				gridViewAdapter = new GridViewAdapter(mContext,datas);
				gridView.setAdapter(gridViewAdapter);
				
				gridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int position,
							long arg3) {
						int item_type = Integer.valueOf(datas.get(position).item_type);
						if(item_type != Constants.TYPE_HISTORY_FEEDBACK)
						{
							jumpToFeedbackActivity(position);
						}
						else
						{
							Intent intent = new Intent();
							intent.setClass(mContext, FeedbackListActivity.class);
							startActivityForResult(intent, Constants.RESULT_FEEDBACK_HISTORY);
						}
					}
				});
				
				AppApplication.threadPool.submit(new LoadUserKeyJob(getActivity(), mHandler,mServiceHub), null, ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_USER_KEY_SUCCESS:
				userInfoMap = (Map) msg.obj;
				String userId = (String) userInfoMap.get("userkey");
				AppApplication.threadPool.submit(new LoadNetFeedbackDataJob(mContext,userId), 
						mNetFeedbackDataListener, 
						ThreadPool.MODE_NETWORK);
				break;
			case Constants.MSG_NET_FEEDBACK_DATA :
				executeUnReadFeedbackJob(null);
				break;
			case Constants.MSG_UNREAD_FEEDBACK :
				onCountChange(msg.arg1);
				break;
			default:
				break;
			}
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mUserHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_USER_KEY_SUCCESS:
				userInfoMap = (Map) msg.obj;
				break;

			default:
				break;
			}
		};
	};
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mSessionServiceHub = new SessionServiceHub();
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_feedback, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		gridView = (GridView) getActivity().findViewById(R.id.gridView);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		
		getActivity().findViewById(R.id.contactUs).setOnClickListener(uiListener);
		
		new LoadGridViewDataTask().execute();
	}
	
	private OnClickListener uiListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.contactUs:
				Intent contactIntent = new Intent();
				contactIntent.setAction(Intent.ACTION_CALL);
				contactIntent.setData(Uri.parse("tel:" + getResources().getString(R.string.contactPhoneNumber)));
				startActivity(contactIntent);
				break;
			default:
				break;
			}
		}
	};
	
	
	private void initGridViewDatas()
	{
		datas = new ArrayList<GridViewItem>();
		
		GridViewItem item_net = new GridViewItem();
		item_net.item_img = getResources().getDrawable(R.drawable.img_error_net);
		item_net.item_bg = getResources().getDrawable(R.drawable.item_net_selector);
		item_net.item_name = getResources().getString(R.string.error_net);
		item_net.item_tag = Constants.ERROR_NET;
		item_net.item_type = String.valueOf(Constants.TYPE_NET);
		
		GridViewItem item_system = new GridViewItem();
		item_system.item_img = getResources().getDrawable(R.drawable.img_error_system);
		item_system.item_bg = getResources().getDrawable(R.drawable.item_system_selector);
		item_system.item_name = getResources().getString(R.string.error_system);
		item_system.item_tag = Constants.ERROR_SYSTEM;
		item_system.item_type = String.valueOf(Constants.TYPE_SYSTEM);
		
		GridViewItem item_battery = new GridViewItem();
		item_battery.item_img = getResources().getDrawable(R.drawable.img_error_battery);
		item_battery.item_bg = getResources().getDrawable(R.drawable.item_battery_selector);
		item_battery.item_name = getResources().getString(R.string.error_battery);
		item_battery.item_tag = Constants.ERROR_BATTERY;
		item_battery.item_type = String.valueOf(Constants.TYPE_BATTERY);
		
		GridViewItem item_phone = new GridViewItem();
		item_phone.item_img = getResources().getDrawable(R.drawable.img_error_phone);
		item_phone.item_bg = getResources().getDrawable(R.drawable.item_phone_selector);
		item_phone.item_name = getResources().getString(R.string.error_phone);
		item_phone.item_tag = Constants.ERROR_PHONE;
		item_phone.item_type = String.valueOf(Constants.TYPE_PHONE);
		
		GridViewItem item_application = new GridViewItem();
		item_application.item_img = getResources().getDrawable(R.drawable.img_error_application);
		item_application.item_bg = getResources().getDrawable(R.drawable.item_application_selector);
		item_application.item_name = getResources().getString(R.string.error_application);
		item_application.item_tag = Constants.ERROR_APPLICATION;
		item_application.item_type = String.valueOf(Constants.TYPE_APPLICATION);
		
		GridViewItem item_data = new GridViewItem();
		item_data.item_img = getResources().getDrawable(R.drawable.img_error_data);
		item_data.item_bg = getResources().getDrawable(R.drawable.item_data_selector);
		item_data.item_name = getResources().getString(R.string.error_data);
		item_data.item_tag = Constants.ERROR_DATA;
		item_data.item_type = String.valueOf(Constants.TYPE_DATA);
		
		GridViewItem item_others = new GridViewItem();
		item_others.item_img = getResources().getDrawable(R.drawable.img_error_others);
		item_others.item_bg = getResources().getDrawable(R.drawable.item_others_selector);
		item_others.item_name = getResources().getString(R.string.error_others);
		item_others.item_tag = Constants.ERROR_OTHERS;
		item_others.item_type = String.valueOf(Constants.TYPE_OTHERS);
		
		GridViewItem item_advise = new GridViewItem();
		item_advise.item_img = getResources().getDrawable(R.drawable.img_sugar_advise);
		item_advise.item_bg = getResources().getDrawable(R.drawable.item_advise_selector);
		item_advise.item_name = getResources().getString(R.string.sugar_advise);
		item_advise.item_tag = Constants.SUGAR_ADVISE;
		item_advise.item_type = String.valueOf(Constants.TYPE_SUGAR_ADVISE);
		
		GridViewItem item_history = new GridViewItem();
		item_history.item_img = getResources().getDrawable(R.drawable.img_history_feedback);
		item_history.item_bg = getResources().getDrawable(R.drawable.item_history_selector);
		item_history.item_name = getResources().getString(R.string.history_feedback);
		item_history.item_tag = Constants.HISTORY_FEEDBACK;
		item_history.item_type = String.valueOf(Constants.TYPE_HISTORY_FEEDBACK);
		
		datas.add(item_net);
		datas.add(item_battery);
		datas.add(item_phone);
		datas.add(item_data);
		datas.add(item_system);
		datas.add(item_application);
		datas.add(item_others);
		datas.add(item_advise);
		datas.add(item_history);
	}
	
	private  class LoadGridViewDataTask extends AsyncTask<Object, Integer, Object>
	{

		@Override
		protected Object doInBackground(Object... params) {
			initGridViewDatas();
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_GRIDVIEW_DATA;
			mHandler.sendMessage(msg);
			return null;
		}
	}

	public void onCountChange(final int count) {
		if(getActivity() != null)
		{
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					int num = count;
					if(count > 99)
					{
						num = 99;
					}
					View view = gridView.getChildAt(8);
					if(view != null)
					{
						TextView tv = (TextView) view.findViewById(R.id.unread_number);
						if(count == 0)
						{
							tv.setVisibility(View.GONE);
						}else
						{
							tv.setVisibility(View.VISIBLE);
							tv.setText(num + "");
						}
					}
				}
			});
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == Constants.RESULT_FEEDBACK_HISTORY)
		{
			executeUnReadFeedbackJob(data);
		}else if(requestCode == Constants.ACTIVITY_CODE_REQUEST_FOR_USERID)
		{
			AppApplication.threadPool.submit(
					new LoadUserKeyJob(getActivity(), mUserHandler,mServiceHub), 
					null, ThreadPool.MODE_CPU);
		}
	}
	
	private void executeUnReadFeedbackJob(Intent data)
	{
		String userId = null;
		if(data != null)
		{
			Bundle bundle = data.getExtras();
			userId = bundle.getString("userkey");
		}else if(userInfoMap != null)
		{
			userId = (String) userInfoMap.get("userkey");
		}
		
		if(userId != null)
		{
			AppApplication.threadPool.submit(new LoadUnReadFeedbackJob(userId), 
					getUnReadFeedbakListener(), 
					ThreadPool.MODE_CPU);
		}
	}

	private FutureListener<Integer> getUnReadFeedbakListener()
	{
		return new FutureListener<Integer>() {
			@Override
			public synchronized void onFutureDone(Future<Integer> count) {
				Message msg = mHandler.obtainMessage();
				msg.what = Constants.MSG_UNREAD_FEEDBACK;
				msg.arg1 = count.get();
				mHandler.sendMessage(msg);
			}};
	}
	
	/**
	 * 设置邮箱电话等信息
	 * @param data
	 */
	private void setUserInfoData(Bundle data)
	{
		if(userInfoMap != null)
		{
			if(userInfoMap.get("email") != null)
			{
				String email = (String) userInfoMap.get("email");
				if(!isStrNull(email))
				{
					data.putString("email", email);
				}
			}
			
			if(userInfoMap.get("mobile") != null)
			{
				String mobile = (String) userInfoMap.get("mobile");
				if(!isStrNull(mobile))
				{
					data.putString("mobile", mobile);
				}
			}
		}
	}
	
	private boolean isStrNull(String str)
	{
		if(str.trim().length() != 0 && !"null".equals(str) && !"NULL".equals(str))
		{
			return false;
		}
		return true;
	}
	
	private void jumpToFeedbackActivity(int position)
	{
		if(NetUtils.isNetWorkAvailable(mContext, null))
		{
			AppApplication.threadPool.submit(
					new LoadSessionStateJob(mContext, mHandler, mSessionServiceHub,position), 
					null, ThreadPool.MODE_CPU);
		}else
		{
			ContextUtils.showToast(mContext, R.string.toast_net_error, Toast.LENGTH_SHORT);
		}
	}
	
}

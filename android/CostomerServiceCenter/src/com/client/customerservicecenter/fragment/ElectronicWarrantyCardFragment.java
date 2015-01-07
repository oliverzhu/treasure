package com.client.customerservicecenter.fragment;

import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.ElectronicCardActivity;
import com.client.customerservicecenter.R;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.hub.SessionServiceHub;
import com.client.customerservicecenter.job.ActivateElectronicCardJob;
import com.client.customerservicecenter.job.LoadElectronicCardJob;
import com.client.customerservicecenter.job.LoadSessionStateJob;
import com.client.customerservicecenter.job.LoadUserKeyJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Utils;
import com.client.customerservicecenter.util.thread.ThreadPool;

/**
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public class ElectronicWarrantyCardFragment extends BaseFragment {
	private ViewGroup itemImei;
	private ViewGroup itemRepairDate;
	private ViewGroup itemUserMsg;
	private ViewGroup itemAbout;
	
	private TextView summaryImei;
	private TextView summaryRepairDate;
	private TextView activateCard;
	
	private ImageView indicator;
	private ProgressBar progressBar;
	
	private AlertDialog mAlertDialog;
	private ProgressDialog mProgressDialog;
	
	private SessionServiceHub mSessionServiceHub;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_USER_KEY_SUCCESS:
				userInfoMap = (Map) msg.obj;
				AppApplication.threadPool.submit(
						new ActivateElectronicCardJob(mContext, mHandler, (String)userInfoMap.get("userkey")), 
						null, 
						ThreadPool.MODE_NETWORK);
				break;
			case Constants.MSG_USER_KEY_FAIL:
				showActivateFailTip();
				break;
			case Constants.MSG_ACTIVATE_ELECTROINICCARD_FAIL:
				showActivateFailTip();
				break;
			case Constants.MSG_ACTIVATE_ELECTROINICCARD_SUCCESS:
				ContextUtils.closeProgressDialog(mProgressDialog);
				String type = (String) msg.obj;
				if("activate".equals(type))
				{
					ContextUtils.showToast(mContext, R.string.toast_activate_success, Toast.LENGTH_SHORT);
					setSummaryDate(getActivateDate() + Constants.EXPIRE_DATE); 
				}
				break;
			case Constants.MSG_ELECTROINICCARD_ACTIVATED:
				long activatedTime = (Long) msg.obj;
				setSummaryDate(activatedTime);
				break;
			case Constants.MSG_ELECTROINICCARD_NOT_ACTIVATED:
				activateCard.setEnabled(true);
				break;
			case Constants.MSG_ELECTROINICCARD_SERVER_ERROR:
				activateCard.setEnabled(true);
				break;
			case Constants.MSG_SESSION_VALID:
				AppApplication.threadPool.submit(
						new LoadUserKeyJob(mContext, mModifyUserHandler,mServiceHub), 
						null, ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_SESSION_INVALID:
				hideProgressBar();
				itemUserMsg.setEnabled(true);
				Intent userActivity = new Intent();
				userActivity.setAction(Constants.ACTION_LOGIN_USER_ACCESS);
				startActivityForResult(userActivity,Constants.ACTIVITY_CODE_REQUEST_FOR_USERID);
				break;
			default:
				break;
			}
		};
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mModifyUserHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			hideProgressBar();
			itemUserMsg.setEnabled(true);
			switch (msg.what) {
			case Constants.MSG_USER_KEY_SUCCESS:
				Intent editIntent = new Intent();
				editIntent.setAction(Constants.ACTION_LOGIN_USER_EDIT);
				Map infoMap = (Map) msg.obj;
				String uid = "";
				if(infoMap.get("uid") != null)
				{
					uid = (String) infoMap.get("uid");
					if(uid.trim().length() != 0)
					{
						Bundle data = new Bundle();
						data.putString("uid", uid);
//						data.putString("source_intent", mContext.getPackageName());
						editIntent.putExtras(data);
						mContext.startActivity(editIntent);
					}
				}
				break;
			case Constants.MSG_USER_KEY_FAIL:
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
		return inflater.inflate(R.layout.fragment_electronic_warranty, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPref = mContext.getSharedPreferences(Constants.PREFERENCE_CUSTOMER_SERVICE, Context.MODE_APPEND);
		
		itemImei = (ViewGroup) getActivity().findViewById(R.id.item_imei);
		itemRepairDate = (ViewGroup) getActivity().findViewById(R.id.item_repair_date);
		itemUserMsg = (ViewGroup) getActivity().findViewById(R.id.item_user_msg);
		itemAbout = (ViewGroup) getActivity().findViewById(R.id.item_about);
		
		summaryImei = (TextView) itemImei.findViewById(R.id.summary_imei);
		summaryImei.setText(ContextUtils.getImei(mContext));
		
		indicator = (ImageView) itemUserMsg.findViewById(R.id.indicator);
		progressBar = (ProgressBar) itemUserMsg.findViewById(R.id.loading_progressbar);
		hideProgressBar();
		
		activateCard = (TextView) itemRepairDate.findViewById(R.id.activateCard);
		activateCard.setEnabled(false);
		activateCard.setText(mContext.getResources().getString(R.string.activate));
		
		summaryRepairDate =  (TextView) itemRepairDate.findViewById(R.id.summary_repair_date);
		
		activateCard.setOnClickListener(onClickListener);
		itemUserMsg.setOnClickListener(onClickListener);
		itemAbout.setOnClickListener(onClickListener);
		
		AppApplication.threadPool.submit(new LoadElectronicCardJob(mContext, mHandler), null, ThreadPool.MODE_CPU);
	}
	
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.item_user_msg:
				showProgressBar();
				itemUserMsg.setEnabled(false);
				AppApplication.threadPool.submit(
						new LoadSessionStateJob(mContext, mHandler, mSessionServiceHub),
						null, ThreadPool.MODE_CPU);
				break;
			case R.id.item_about:
//				Intent intent = new Intent(mContext,EwcActivity.class);
				Intent intent = new Intent(mContext,ElectronicCardActivity.class);
				mContext.startActivity(intent);
				break;
			case R.id.activateCard:
				if(!NetUtils.isNetWorkAvailable(mContext, null))
				{
					showAlertDialog();
				}else
				{
					activateCard.setEnabled(false);
					mProgressDialog = 
							ContextUtils.showProgressDialog(mContext, 
									R.string.progressdialog_title_activate, 
									R.string.progressdialog_message_activate);
					AppApplication.threadPool.submit(new LoadUserKeyJob(mContext, mHandler,mServiceHub), null, ThreadPool.MODE_CPU);
				}
				break;
			default:
				break;
			}
		}
	};
	
	private void showAlertDialog()
	{
		mAlertDialog = ContextUtils.showAlertDialog(mContext, 
				R.string.dialog_title_activate, 
				R.string.dialog_message_activate, 
				new int[]{R.string.btn_ok,R.string.btn_cancel}, 
				new DialogInterface.OnClickListener[]{
				new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						ContextUtils.startNetworkSettingActivity(mContext);
					}
					
				},
				new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which) {
						mAlertDialog.dismiss();
					}
					
				}});
		mAlertDialog.show();
	}
	
	private long getActivateDate()
	{
		return mPref.getLong(Constants.KEY_ACTIVATE_DATE, -1);
	}
	
	private void showProgressBar()
	{
		indicator.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
	}
	private void hideProgressBar()
	{
		indicator.setVisibility(View.VISIBLE);
		progressBar.setVisibility(View.GONE);
		
	}
	
	private void setSummaryDate(long activateDate)
	{
		String time = Utils.formateSimpleDate(activateDate);
		activateCard.setEnabled(false);
		summaryRepairDate.setText(time);
		activateCard.setText(mContext.getResources().getString(R.string.activated));
	}
	
	private void showActivateFailTip()
	{
		ContextUtils.closeProgressDialog(mProgressDialog);
		activateCard.setEnabled(true);
		ContextUtils.showToast(mContext, R.string.toast_activate_fail, Toast.LENGTH_SHORT);
	}
}

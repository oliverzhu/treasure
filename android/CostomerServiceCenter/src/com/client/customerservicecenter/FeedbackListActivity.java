package com.client.customerservicecenter;

import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.job.LoadLocalFeedbackDataJob;
import com.client.customerservicecenter.job.LoadNetFeedbackDataJob;
import com.client.customerservicecenter.job.LoadUserKeyJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.view.FeedbackLayout;
import com.client.customerservicecenter.widget.ptr.PullToRefreshBase;
import com.client.customerservicecenter.widget.ptr.PullToRefreshBase.OnRefreshListener;
import com.client.customerservicecenter.widget.ptr.PullToRefreshScrollView;

public class FeedbackListActivity extends BaseActivity {
	private PullToRefreshScrollView mPullRefreshScrollView;
	private ScrollView mListContainer;
	private LinearLayout mLoadingContainer;
	private RelativeLayout mEmptyContainer;
	private TextView barTitle;
	
	private ProgressDialog mProgressDialog;
	
	private FutureListener<Integer> mNetFeedbackDataListener = 
    		new FutureListener<Integer>() {
		@Override
		public synchronized void onFutureDone(Future<Integer> result) {
			Integer flag = result.get();
			Message msg = mHandler.obtainMessage();
			if(flag == Constants.SUCCESS && userInfoMap != null)
			{
				msg.what = Constants.MSG_CLOSEPROGRESSDIALOG;
				String uid = (String) userInfoMap.get("userkey");
				AppApplication.threadPool.submit(new LoadLocalFeedbackDataJob(mContext,uid),
						mLocalFeedbackDataListener, ThreadPool.MODE_CPU);
			}else
			{
				msg.what = Constants.MSG_SUBMIT_COMMENT_FAIL_KEY;
			}
			mHandler.sendMessage(msg);
		}
	};
	
	private FutureListener<Integer> mReloadNetFeedbackDataListener = 
    		new FutureListener<Integer>() {
		@Override
		public synchronized void onFutureDone(Future<Integer> result) {
			Integer flag = result.get();
			if(flag == Constants.SUCCESS && userInfoMap != null)
			{
				String uid = (String) userInfoMap.get("userkey");
				AppApplication.threadPool.submit(new LoadLocalFeedbackDataJob(mContext,uid),
						mLocalFeedbackDataListener, ThreadPool.MODE_CPU);
			}else
			{
				Message msg = mReloadHandler.obtainMessage();
				msg.what = Constants.MSG_RELOAD_FAIL;
				mReloadHandler.sendMessage(msg);
			}
		}
	};
	
	private FutureListener<ArrayList<CommentInfo>> mLocalFeedbackDataListener = 
    		new FutureListener<ArrayList<CommentInfo>>() {
		@Override
		public synchronized void onFutureDone(Future<ArrayList<CommentInfo>> infos) {
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_LOCAL_FEEDBACK_DATA;
			msg.obj = infos.get();
			mHandler.sendMessage(msg);
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_LOCAL_FEEDBACK_DATA:
				ContextUtils.closeProgressDialog(mProgressDialog);
				showListContainer();
				if(msg.obj != null)
				{
					@SuppressWarnings("unchecked")
					ArrayList<CommentInfo> infos = (ArrayList<CommentInfo>) msg.obj;
					if(infos.size() != 0)
					{
						mListContainer.removeAllViews();
						FeedbackLayout feedbackLayout = new FeedbackLayout(mContext, infos,mHandler);
						mListContainer.addView(feedbackLayout,
								new LayoutParams(LayoutParams.MATCH_PARENT,
										LayoutParams.WRAP_CONTENT));
					}else
					{
						showReloadContainer();
					}
				}else
				{
					showReloadContainer();
					
				}
				break;
			case Constants.MSG_USER_KEY_FAIL:
				showReloadContainer();
				break;
			case Constants.MSG_USER_KEY_SUCCESS:
				userInfoMap = (Map) msg.obj;
				String uid = (String) userInfoMap.get("userkey");
				AppApplication.threadPool.submit(new LoadLocalFeedbackDataJob(mContext,uid),
						mLocalFeedbackDataListener, ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_SHOWPROGRESSDIALOG:
				Bundle data = msg.getData();
				int titleResId = data.getInt("title");
				int messageResId = data.getInt("message");
				mProgressDialog = ContextUtils.showProgressDialog(mContext, titleResId, messageResId);
				break;
			case Constants.MSG_CLOSEPROGRESSDIALOG:
				submitSuccessTip();
				break;
			case Constants.MSG_SUBMIT_COMMENT_SUCCESS_KEY:
				if(userInfoMap != null)
				{
					AppApplication.threadPool.submit(new LoadNetFeedbackDataJob(mContext, 
							(String)userInfoMap.get("userkey")), 
							mNetFeedbackDataListener, ThreadPool.MODE_NETWORK);
				}else
				{
					submitFailTip();
				}
				break;
			case Constants.MSG_SUBMIT_COMMENT_FAIL_KEY:
				submitFailTip();
				break;
			default:
				break;
			}
		};
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mReloadHandler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_USER_KEY_SUCCESS:
				userInfoMap = (Map) msg.obj;
				String uid = (String) userInfoMap.get("userkey");
				AppApplication.threadPool.submit(new LoadNetFeedbackDataJob(mContext, uid), 
						mReloadNetFeedbackDataListener, ThreadPool.MODE_NETWORK);
				break;
			case Constants.MSG_USER_KEY_FAIL:
				ContextUtils.showToast(mContext, R.string.toast_server_error, Toast.LENGTH_SHORT);
			case Constants.MSG_RELOAD_FAIL:
				showReloadContainer();
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_feedback_list);
		
		initUi();
		initData(mHandler,false);
	}
	
	private void initUi()
	{
		barTitle = (TextView) findViewById(R.id.bar_title);
		barTitle.setText(getResources().getString(R.string.history_feedback));
		findViewById(R.id.back).setOnClickListener(uiListener);
		
		mPullRefreshScrollView = (PullToRefreshScrollView) findViewById(R.id.dismissable_container);
		mListContainer = mPullRefreshScrollView.getRefreshableView();
		mLoadingContainer = (LinearLayout) findViewById(R.id.loading_container);
		mEmptyContainer = (RelativeLayout) findViewById(R.id.reload);
		mEmptyContainer.setOnClickListener(uiListener);
		
		mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

			@Override
			public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
				initData(mReloadHandler,true);
			}
		});
	}
	
	private void initData(Handler handler,boolean pullToRefresh)
	{
		if(!pullToRefresh) showLoadingContainer();
		
		AppApplication.threadPool.submit(new LoadUserKeyJob(mContext, handler,mServiceHub), 
				null, ThreadPool.MODE_CPU);
	}
	
	private OnClickListener uiListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.back:
				if(userInfoMap != null)
				{
					Bundle bundle = new Bundle();
			        bundle.putString("userkey", (String) userInfoMap.get("userkey"));
			        Intent intent = new Intent();
			        intent.putExtras(bundle);
			        setResult(Activity.RESULT_OK, intent);
				}
				finish();
				break;
			case R.id.reload:
				initData(mReloadHandler,false);
				break;
			default:
				break;
			}
		}
	};
	
	private void showListContainer() {
        if (View.VISIBLE == mLoadingContainer.getVisibility()) {
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_in));
        }
        mListContainer.setVisibility(View.VISIBLE);
        mLoadingContainer.setVisibility(View.INVISIBLE);
        mEmptyContainer.setVisibility(View.INVISIBLE);
        
        // Call onRefreshComplete when the list has been refreshed.
		mPullRefreshScrollView.onRefreshComplete();
    }
	
	private void showLoadingContainer() {
        if (View.INVISIBLE == mLoadingContainer.getVisibility()) {
            mLoadingContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
            mEmptyContainer.setVisibility(View.INVISIBLE);
        }
    }
	
	private void showReloadContainer() {
		if (View.INVISIBLE == mLoadingContainer.getVisibility()) {
			mEmptyContainer.startAnimation(AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_in));
        }
		mLoadingContainer.setVisibility(View.INVISIBLE);
        mListContainer.setVisibility(View.INVISIBLE);
        mEmptyContainer.setVisibility(View.VISIBLE);
    }
	
	private void submitSuccessTip()
	{
		ContextUtils.closeProgressDialog(mProgressDialog);
		ContextUtils.showToast(mContext, R.string.submitSuccess, Toast.LENGTH_SHORT);
	}
	
	private void submitFailTip()
	{
		ContextUtils.closeProgressDialog(mProgressDialog);
		ContextUtils.showToast(mContext, R.string.submitFail, Toast.LENGTH_SHORT);
	}
}

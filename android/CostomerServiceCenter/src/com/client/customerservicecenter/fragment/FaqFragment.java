package com.client.customerservicecenter.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.adapter.FaqListAdapter;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.job.LoadLocalFeedbackFaqJob;
import com.client.customerservicecenter.job.LoadNetFeedbackFaqJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;


/**
 * 常见问题
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public class FaqFragment extends BaseFragment {
	private View mLoadingContainer;
	private TextView mEmpty;
	private EditText searchEt;
	private ImageView deleteIndicator;
    private View mListContainer;
    private ListView lv;
    
    private FaqListAdapter adapter;
	private FutureListener<Integer> mNetFeedbackFaqListener = 
    		new FutureListener<Integer>() {
		@Override
		public synchronized void onFutureDone(Future<Integer> result) {
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_NET_FEEDBACK_FAQ;
			mHandler.sendMessage(msg);
		}
	};
	
	private FutureListener<ArrayList<CommentInfo>> mLocalFeedbackFaqListener = 
    		new FutureListener<ArrayList<CommentInfo>>() {
		@Override
		public synchronized void onFutureDone(Future<ArrayList<CommentInfo>> infos) {
			sendLoadLocalFaqMessage(false,infos.get());
		}
	};
	
	private FutureListener<ArrayList<CommentInfo>> mSearchLocalFeedbackFaqListener = 
    		new FutureListener<ArrayList<CommentInfo>>() {
		@Override
		public synchronized void onFutureDone(Future<ArrayList<CommentInfo>> infos) {
			sendLoadLocalFaqMessage(true,infos.get());
		}
	};
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_NET_FEEDBACK_FAQ:
				AppApplication.threadPool.submit(new LoadLocalFeedbackFaqJob(mContext,""),
						mLocalFeedbackFaqListener, 
						ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_LOCAL_FEEDBACK_FAQ:
				showListContainer();
				Bundle data = msg.getData();
				boolean clear = data.getBoolean("clear");
				if(!clear)
				{
					searchEt.setInputType(InputType.TYPE_CLASS_TEXT);
					searchEt.addTextChangedListener(getTextWatcher());
				}
				if(msg.obj != null)
				{
					
					ArrayList<CommentInfo> infos = (ArrayList<CommentInfo>) msg.obj;
					if(infos.size() != 0)
					{
						mEmpty.setVisibility(View.GONE);
					}else
					{
						showEmpty(clear);
					}
					adapter.setData(infos,clear);
				}else
				{
					showEmpty(clear);
				}
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_faq, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
		
		mLoadingContainer = getActivity().findViewById(R.id.loading_container);
        mListContainer = getActivity().findViewById(R.id.list_container);
		mEmpty=(TextView)getActivity().findViewById(R.id.empty);
		deleteIndicator = (ImageView) getActivity().findViewById(R.id.delete_indicator);
		deleteIndicator.setOnClickListener(viewListener);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mListContainer.setVisibility(View.INVISIBLE);
        lv = (ListView) getActivity().findViewById(R.id.list);
        
        searchEt = (EditText) getActivity().findViewById(R.id.search_feedback);
        searchEt.setInputType(InputType.TYPE_NULL);
        adapter = new FaqListAdapter(mContext,lv);
        lv.setAdapter(adapter);
		
        showLoadingContainer();
		AppApplication.threadPool.submit(new LoadNetFeedbackFaqJob(mContext),
				mNetFeedbackFaqListener, 
				ThreadPool.MODE_NETWORK);
	}
	
	private void showListContainer() {
        if (View.VISIBLE == mLoadingContainer.getVisibility()) {
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_in));
        }
        mListContainer.setVisibility(View.VISIBLE);
        mLoadingContainer.setVisibility(View.INVISIBLE);
    }
	
	private void showLoadingContainer() {
        if (View.INVISIBLE == mLoadingContainer.getVisibility()) {
            mLoadingContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        }
    }
	
	private View.OnClickListener viewListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.delete_indicator:
				searchEt.setText("");
				break;

			default:
				break;
			}
		}
	};
	
	private TextWatcher getTextWatcher()
	{
		return new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				String arg = "";
				if(arg0.length() > 0)
				{
					deleteIndicator.setVisibility(View.VISIBLE);
					arg = arg0.toString();
				}else
				{
					deleteIndicator.setVisibility(View.GONE);
				}
				AppApplication.threadPool.submit(new LoadLocalFeedbackFaqJob(mContext,arg),
						mSearchLocalFeedbackFaqListener, 
						ThreadPool.MODE_CPU);
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
			}
		};
	}
	
	/**
	 * @param clear 是否清楚之前数据
	 */
	private void sendLoadLocalFaqMessage(boolean clear,ArrayList<CommentInfo> infos)
	{
		Bundle data = new Bundle();
		data.putBoolean("clear", clear);
		Message msg = mHandler.obtainMessage();
		msg.what = Constants.MSG_LOCAL_FEEDBACK_FAQ;
		msg.obj = infos;
		msg.setData(data);
		mHandler.sendMessage(msg);
	}
	
	private void showEmpty(boolean localSearch)
	{
		if(!localSearch)
		{
			if(NetUtils.isNetWorkAvailable(mContext, null))
			{
				ContextUtils.showToast(mContext, R.string.toast_server_error, Toast.LENGTH_SHORT);
			}else
			{
				ContextUtils.showToast(mContext, R.string.toast_net_error, Toast.LENGTH_SHORT);
			}
		}
		mEmpty.setVisibility(View.VISIBLE);
	}

}

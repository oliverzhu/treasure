package com.client.customerservicecenter.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.job.ChangeUnReadStateJob;
import com.client.customerservicecenter.job.DeleteFeedbackJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Constants.UnReadStateChangeListener;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.thread.ThreadPool;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/9/16
 */
public class FeedbackLayout extends LinearLayout {
	private Context mContext;
	private ArrayList<CommentInfo> mDataSource;
	private Handler mHandler;
	public FeedbackLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FeedbackLayout(Context context, ArrayList<CommentInfo> datasource,Handler handler) {
		super(context);
		this.mContext = context;
		this.mDataSource = datasource;
		this.mHandler = handler;
		init();
	}
	
	private void init()
	{
		if(mDataSource == null || mDataSource.size() == 0) {
			return;
		}
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(LinearLayout.VERTICAL);
		for(int i = 0;i < mDataSource.size();i++)
		{
			final CommentInfo bean =  mDataSource.get(i);
			
			final RelativeLayout itemLayoutParent = 
					(RelativeLayout) View.inflate(mContext, R.layout.item_feedback, null);
			
			LinearLayout feedbackChildContainer = 
					(LinearLayout) itemLayoutParent.findViewById(R.id.feedbackChildContainer);
			final RelativeLayout feedbackContainer = 
					(RelativeLayout) itemLayoutParent.findViewById(R.id.feedbackContainer);
			
			final FeedbackChildLayout detailLayout = 
					new FeedbackChildLayout(mContext, bean,mHandler);
			detailLayout.setVisibility(View.GONE);
			feedbackChildContainer.addView(detailLayout);
			
			feedbackContainer.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(detailLayout.getVisibility() == View.GONE)
					{
						if(checkUnReadState(arg0,detailLayout))
						{
							detailLayout.setVisibility(View.VISIBLE);
						}
					}else if(detailLayout.getVisibility() == View.VISIBLE)
					{
						detailLayout.setVisibility(View.GONE);
					}
				}
			});
			
			initFeedbackContainer(feedbackContainer,bean,itemLayoutParent);
			
			
			addView(itemLayoutParent,
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		}	
	}

	private void initFeedbackContainer(RelativeLayout feedbackContainer,
			final CommentInfo bean,final RelativeLayout itemLayoutParent) {
		feedbackContainer.setTag(bean);
		
		TextView feedbackPrimary = (TextView) feedbackContainer.findViewById(R.id.feedback_primary);
		TextView feedbackSecondary = (TextView) feedbackContainer.findViewById(R.id.feedback_secondary);
		TextView commentDate = (TextView) feedbackContainer.findViewById(R.id.commentDate);
		ImageView unreadTip = (ImageView) feedbackContainer.findViewById(R.id.unread_tip);
		TextView feedbackStatus = (TextView) feedbackContainer.findViewById(R.id.feedback_status);
		
		int type = Integer.valueOf(bean.getType());
		String issueType = "";
		switch (type) {
		case Constants.TYPE_SYSTEM:
			issueType = mContext.getResources().getString(R.string.error_system);
			break;
		case Constants.TYPE_BATTERY:
			issueType = mContext.getResources().getString(R.string.error_battery);
			break;
		case Constants.TYPE_PHONE:
			issueType = mContext.getResources().getString(R.string.error_phone);
			break;
		case Constants.TYPE_NET:
			issueType = mContext.getResources().getString(R.string.error_net);
			break;
		case Constants.TYPE_APPLICATION:
			issueType = mContext.getResources().getString(R.string.error_application);
			break;
		case Constants.TYPE_DATA:
			issueType = mContext.getResources().getString(R.string.error_data);
			break;
		case Constants.TYPE_OTHERS:
			issueType = mContext.getResources().getString(R.string.error_others);
			break;
		case Constants.TYPE_SUGAR_ADVISE:
			issueType = mContext.getResources().getString(R.string.sugar_advise);
			break;

		default:
			break;
		}
		
		int readState = bean.getReadState();
		if(readState == 0)
		{
			unreadTip.setVisibility(View.VISIBLE);
		}else
		{
			unreadTip.setVisibility(View.INVISIBLE);
		}
		
		int status = bean.getStatus();
		
		if(status == 1 || status ==2)
		{
			boolean isReplied = false;
			if(bean.getInfos() != null && bean.getInfos().size() != 0)
			{
				for(int i = 0;i < bean.getInfos().size();i++)
				{
					FeedbackScheduleInfo info = bean.getInfos().get(i);
					if(info.getIdentity() == 0)
					{
						isReplied = true;
						break;
					}
				}
			}
			if(isReplied)
			{
				feedbackStatus.setText(mContext.getResources().getText(R.string.replied));
			}else
			{
				feedbackStatus.setText(mContext.getResources().getText(R.string.resolving));
			}
			
			feedbackContainer.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					ContextUtils.showToast(mContext, R.string.toast_delete_warn, Toast.LENGTH_SHORT);
					return true;
				}
			});
		}else if(status == 3)
		{
			feedbackStatus.setText(mContext.getResources().getText(R.string.resolved));
			// Create a generic swipe-to-dismiss touch listener.
			/**
			feedbackContainer.setOnTouchListener(new SwipeDismissTouchListener(
					feedbackContainer,
                    null,
                    new SwipeDismissTouchListener.OnDismissCallback() {
                        @Override
                        public void onDismiss(View view, Object token) {
                        	deleteFeedback(bean);
                            removeView(itemLayoutParent);
                        }
                    }));*/
			feedbackContainer.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View arg0) {
					CustomAlertDialog dialog = new CustomAlertDialog(mContext);
	                dialog.setTitle(mContext.getResources().getString(R.string.dialog_title_feedback_delete))
	                    .setMessage(mContext.getResources().getString(R.string.dialog_message_feedback_delete))
	                    .setPositiveButton(mContext.getResources().getString(R.string.btn_ok), new View.OnClickListener() {
	                        @Override
	                        public void onClick(View v) {
	                        	deleteFeedback(bean);
	                            removeView(itemLayoutParent);
	                        }})
	                    .setNegativeButton(mContext.getResources().getString(R.string.btn_cancel), null)
	                    .show();
					return true;
				}
			});
             
		}
		
		
		
		feedbackPrimary.setText(issueType);
		feedbackSecondary.setText(bean.getComment());
		commentDate.setText(ContextUtils.getFormatString(bean.getCommentDate(), mContext));
	}
	
	private boolean checkUnReadState(View view,final FeedbackChildLayout detailLayout)
	{
		final ProgressBar unReadProgressBar = (ProgressBar) view.findViewById(R.id.unread_progressbar);
		ImageView unReadTip = (ImageView) view.findViewById(R.id.unread_tip);
		CommentInfo commentInfo = (CommentInfo) view.getTag();
		if(commentInfo.getReadState() == 1)
		{
			return true;
		}
		
		unReadProgressBar.setVisibility(View.VISIBLE);
		unReadTip.setVisibility(View.INVISIBLE);
		ChangeUnReadStateJob changeUnReadStateJob = new ChangeUnReadStateJob(commentInfo, new UnReadStateChangeListener() {
			
			@Override
			public void onStateChange() {
				Activity activity = (Activity) mContext;
				activity.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						unReadProgressBar.setVisibility(View.INVISIBLE);
						detailLayout.setVisibility(View.VISIBLE);
					}
				});
			}
		});
		AppApplication.threadPool.submit(changeUnReadStateJob, null, ThreadPool.MODE_CPU);
		return false;
	}
	
	private void deleteFeedback(CommentInfo commentInfo)
	{
		DeleteFeedbackJob deleteFeedbackJob = new DeleteFeedbackJob(commentInfo);
		AppApplication.threadPool.submit(deleteFeedbackJob, null, ThreadPool.MODE_CPU);
	}

}

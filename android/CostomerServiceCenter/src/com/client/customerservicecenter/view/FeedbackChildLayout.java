package com.client.customerservicecenter.view;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.FeedbackScheduleInfo;
import com.client.customerservicecenter.job.SubmitCommentJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/9/16
 */
public class FeedbackChildLayout extends LinearLayout {
	private Context mContext;
	private CommentInfo commentInfo;
	private Handler mHandler;
	
	private FutureListener<Boolean> mSubmitCommentListener = 
    		new FutureListener<Boolean>() {
		@Override
		public synchronized void onFutureDone(Future<Boolean> result) {
			Boolean flag = result.get();
			Message msg = mHandler.obtainMessage();
			if(flag)
			{
				msg.what = Constants.MSG_SUBMIT_COMMENT_SUCCESS_KEY;
			}else
			{
				msg.what = Constants.MSG_SUBMIT_COMMENT_FAIL_KEY;
			}
			mHandler.sendMessage(msg);
		}
	};
	public FeedbackChildLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FeedbackChildLayout(Context context, CommentInfo commentInfo,Handler handler) {
		super(context);
		this.mContext = context;
		this.commentInfo = commentInfo;
		this.mHandler = handler;
		init();
	}
	
	private void init()
	{
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(LinearLayout.VERTICAL);
		if(commentInfo.getInfos() != null && commentInfo.getInfos().size() != 0) {
			for(int i = 0;i < commentInfo.getInfos().size();i++)
			{
				FeedbackScheduleInfo bean =  commentInfo.getInfos().get(i);
				
				final RelativeLayout itemLayoutParent = 
						(RelativeLayout) View.inflate(mContext, R.layout.item_feedback_child, null);
				
				initFeedbackChildContainer(itemLayoutParent,bean);
				
				addView(itemLayoutParent,
						new LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT));
			}
		}
		
		if(commentInfo.getStatus() != 3)
		{
			RelativeLayout sendCommentContainer = 
					(RelativeLayout) View.inflate(mContext, R.layout.item_feedback_send, null);
			
			setCommentListener(sendCommentContainer);
			
			addView(sendCommentContainer,
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		}
	}
	
	private void initFeedbackChildContainer(RelativeLayout feedbackContainer,
			FeedbackScheduleInfo bean) {
		TextView feedbackPrimary = (TextView) feedbackContainer.findViewById(R.id.feedback_primary_child);
		TextView feedbackSecondary = (TextView) feedbackContainer.findViewById(R.id.feedback_secondary_child);
		TextView commentDate = (TextView) feedbackContainer.findViewById(R.id.commentDate_child);
		
		String issueType = "";
		switch (bean.getIdentity()) {
		case 0:
			issueType = mContext.getResources().getString(R.string.label_customerservice);
			break;
		case 1:
			issueType = mContext.getResources().getString(R.string.label_user);
			break;
		default:
			break;
		}
		
		feedbackPrimary.setText(issueType);
		feedbackSecondary.setText(bean.getComment());
		commentDate.setText(ContextUtils.getFormatString(bean.getCommentDate(), mContext));
	}
	
	private void setCommentListener(RelativeLayout sendCommentContainer)
	{
		ImageView sendCommentImg = 
				(ImageView) sendCommentContainer.findViewById(R.id.sendComment);
		final EditText commentEt = 
				(EditText) sendCommentContainer.findViewById(R.id.editText);
		sendCommentImg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String comment = commentEt.getEditableText().toString();
				if(comment == null || "".equals(comment))
				{
					ContextUtils.showToast(mContext, R.string.commentHint, Toast.LENGTH_SHORT);
					return;
				}
				
				final FeedbackScheduleInfo bean = new FeedbackScheduleInfo();
				bean.setFeedbackId(commentInfo.getId());
				bean.setComment(comment);
				bean.setIdentity(1);
				
				Message message = mHandler.obtainMessage();
				Bundle data = new Bundle();
				message.what = Constants.MSG_SHOWPROGRESSDIALOG;
				data.putInt("title", R.string.comment);
				data.putInt("message", R.string.submitComment);
				message.setData(data);
				mHandler.sendMessage(message);
				
				SubmitCommentJob submitCommentJob = new SubmitCommentJob(bean);
				AppApplication.threadPool.submit(submitCommentJob, 
						mSubmitCommentListener, ThreadPool.MODE_NETWORK);
			}
		});
	}


}

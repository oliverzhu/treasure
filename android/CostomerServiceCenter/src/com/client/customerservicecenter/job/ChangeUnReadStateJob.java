package com.client.customerservicecenter.job;

import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.util.Constants.UnReadStateChangeListener;

/**
 * 追加反馈
 * @author jianwen.zhu
 * @since 2014/9/17
 */
public  class ChangeUnReadStateJob extends AbsFeedbackStateJob{
	private UnReadStateChangeListener listener;
	public ChangeUnReadStateJob(CommentInfo commentInfo,UnReadStateChangeListener listener)
	{
		super(commentInfo);
		this.listener = listener;
	}
	
	@Override
	protected void changeState() {
		commentInfo.setReadState(1);
	}
	
	@Override
	protected void notifyStateChange() {
		if(listener != null)
		{
			listener.onStateChange();
		}
	}
}

package com.client.customerservicecenter.job;

import com.client.customerservicecenter.bean.CommentInfo;

/**
 * 追加反馈
 * @author jianwen.zhu
 * @since 2014/9/17
 */
public  class DeleteFeedbackJob extends AbsFeedbackStateJob{
	public DeleteFeedbackJob(CommentInfo commentInfo) {
		super(commentInfo);
	}

	@Override
	protected void changeState() {
		commentInfo.setFlagDelete(1);
	}
}

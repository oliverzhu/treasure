package com.client.customerservicecenter.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 反馈处理
 * @author jianwen.zhu
 * @since 2014/8/28
 */
public class FeedbackScheduleInfo implements Parcelable{
	private Integer id;
	private String comment;
	private String commentDate;
	private Integer feedbackId;
	/** 0:客服  1:用户 */
	private Integer identity; 
	
	public FeedbackScheduleInfo(){}
	public static final Parcelable.Creator<FeedbackScheduleInfo> CREATOR = new Creator<FeedbackScheduleInfo>() {

		@Override
		public FeedbackScheduleInfo[] newArray(int size) {
			return new FeedbackScheduleInfo[size];
		}

		@Override
		public FeedbackScheduleInfo createFromParcel(Parcel source) {
			return new FeedbackScheduleInfo(source);
		}
	};
	
	public FeedbackScheduleInfo(Parcel in) {
		id = in.readInt();
		comment = in.readString();
		commentDate = in.readString();
		feedbackId = in.readInt();
		identity = in.readInt();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(comment);
		dest.writeString(commentDate);
		dest.writeInt(feedbackId);
		dest.writeInt(identity);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}

	public Integer getFeedbackId() {
		return feedbackId;
	}

	public void setFeedbackId(Integer feedbackId) {
		this.feedbackId = feedbackId;
	}

	public Integer getIdentity() {
		return identity;
	}

	public void setIdentity(Integer identity) {
		this.identity = identity;
	}
}

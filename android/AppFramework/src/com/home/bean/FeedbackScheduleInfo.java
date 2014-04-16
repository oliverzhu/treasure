package com.home.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 反馈处理
 * @author jianwen.zhu
 *
 */
public class FeedbackScheduleInfo implements Parcelable{
	private Integer id;
	private String comment;
	private String commentDate;
	private Integer feedbackId;
	/** 0:客服  1:用户 */
	private Integer identity; 
	
	public static final String ID = "f_id";
	public static final String COMMENT = "f_comment";
	public static final String COMMENTDATE = "f_commentdate";
	public static final String FEEDBACKID = "f_feedbackid";
	public static final String IDENTITY = "f_identity";
	
	private static final String TABLE_NAME = "feedback_schedule";
	
	public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
	public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME
			+ "(" + ID + " INTEGER PRIMARY KEY,"
			+ COMMENT + " VARCHAR(1000),"
			+ FEEDBACKID + " INTEGER,"
			+ COMMENTDATE + " VARCHAR(100),"
			+ IDENTITY + " INTEGER)";
	
	public static final String SQL_INSERT = "INSERT INTO " + TABLE_NAME 
			+ " (" + ID + ","
			+ COMMENT + ","
			+ COMMENTDATE + ","
			+ FEEDBACKID + ","
			+ IDENTITY + ")"
			+ " VALUES (#,'#','#',#,#)";
	
	public static final String SQL_UPDATE  ="UPDATE " + TABLE_NAME + " SET " 
			+ COMMENT + "='#'," 
			+ COMMENTDATE + "='#'," 
			+ FEEDBACKID + "=#," 
			+ IDENTITY + "=#"
			+ " WHERE " + ID + "=#";
	
	public static final String SQL_QUERY  = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	public static final String SQL_QUERY_BY_FEEDBACKID  = "SELECT * FROM " + TABLE_NAME + " WHERE " + FEEDBACKID + "='#'";
	public static final String SQL_QUERY_ALL = "SELECT * FROM " + TABLE_NAME;
	public static final String SQL_QUERY_COUNT = "SELECT count(*) FROM " + TABLE_NAME;
	public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME + " WHERE " + ID + "='#'";
	public static final String SQL_DELETE_ALL = "DELETE FROM " + TABLE_NAME;
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

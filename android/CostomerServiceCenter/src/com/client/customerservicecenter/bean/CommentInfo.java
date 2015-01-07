package com.client.customerservicecenter.bean;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 反馈
 * @author jianwen.zhu
 * @since 2014/8/28
 */
public class CommentInfo implements Parcelable{
	private Integer id;
	private String comment;
	private String imei;
	private String version;
	private String customVersion;
	private String model;
	private String type;
	private String arg0;
	private String arg1;
	private String arg2;
	private int status;
	private String phone;
	private String email;
	private String commentDate;
	/** 0:未读 1：已读 */
	private int readState;
	private ArrayList<FeedbackScheduleInfo> infos;
	private ArrayList<Picture> picture;
	private String userId;
	private String typeStr;
	private String reply;
	private LoginUserInfo loginUserInfo;
	/** 0:未删除 1：已删除 */
	private int flagDelete;
	
	public CommentInfo(){}
	public static final Parcelable.Creator<CommentInfo> CREATOR = new Creator<CommentInfo>() {

		@Override
		public CommentInfo[] newArray(int size) {
			return new CommentInfo[size];
		}

		@Override
		public CommentInfo createFromParcel(Parcel source) {
			return new CommentInfo(source);
		}
	};
	
	public CommentInfo(Parcel in) {
		id = in.readInt();
		comment = in.readString();
		imei = in.readString();
		version = in.readString();
		customVersion = in.readString();
		model = in.readString();
		type = in.readString();
		arg0 = in.readString();
		arg1 = in.readString();
		arg2 = in.readString();
		status = in.readInt();
		phone = in.readString();
		email = in.readString();
		commentDate = in.readString();
		readState = in.readInt();
		infos = (ArrayList<FeedbackScheduleInfo>)in.readValue(FeedbackScheduleInfo.class.getClassLoader());
		picture = (ArrayList<Picture>)in.readValue(Picture.class.getClassLoader());
		userId = in.readString();
		flagDelete = in.readInt();
		typeStr = in.readString();
		reply = in.readString();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(comment);
		dest.writeString(imei);
		dest.writeString(version);
		dest.writeString(customVersion);
		dest.writeString(model);
		dest.writeString(type);
		dest.writeString(arg0);
		dest.writeString(arg1);
		dest.writeString(arg2);
		dest.writeInt(status);
		dest.writeString(phone);
		dest.writeString(email);
		dest.writeString(commentDate);
		dest.writeInt(readState);
		dest.writeValue(infos);
		dest.writeValue(picture);
		dest.writeString(userId);
		dest.writeInt(flagDelete);
		dest.writeString(typeStr);
		dest.writeString(reply);
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getArg0() {
		return arg0;
	}

	public void setArg0(String arg0) {
		this.arg0 = arg0;
	}

	public String getArg1() {
		return arg1;
	}

	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

	public String getArg2() {
		return arg2;
	}

	public void setArg2(String arg2) {
		this.arg2 = arg2;
	}
	
	public String getCustomVersion() {
		return customVersion;
	}

	public void setCustomVersion(String customVersion) {
		this.customVersion = customVersion;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ArrayList<FeedbackScheduleInfo> getInfos() {
		return infos;
	}

	public void setInfos(ArrayList<FeedbackScheduleInfo> infos) {
		this.infos = infos;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCommentDate() {
		return commentDate;
	}

	public void setCommentDate(String commentDate) {
		this.commentDate = commentDate;
	}

	public Integer getReadState() {
		return readState;
	}

	public void setReadState(Integer readState) {
		this.readState = readState;
	}

	public ArrayList<Picture> getPicture() {
		return picture;
	}

	public void setPicture(ArrayList<Picture> picture) {
		this.picture = picture;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getFlagDelete() {
		return flagDelete;
	}

	public void setFlagDelete(int flagDelete) {
		this.flagDelete = flagDelete;
	}
	
	public LoginUserInfo getLoginUserInfo() {
		return loginUserInfo;
	}

	public void setLoginUserInfo(LoginUserInfo loginUserInfo) {
		this.loginUserInfo = loginUserInfo;
	}
	
	

	public String getTypeStr() {
		return typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

	public String getReply() {
		return reply;
	}

	public void setReply(String reply) {
		this.reply = reply;
	}

	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this.getClass() == o.getClass())
		{
			CommentInfo op = (CommentInfo) o;
			return this.id.equals(op.id);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int type = this.getClass().hashCode();
		int code = type*31 + Integer.valueOf(id);
		return code;
	}
}

package com.client.customerservicecenter.bean;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Picture implements Parcelable{
	public Integer id;
	public Bitmap bitmap;
	public String path;
	public String key;
	public String feedbackId;
	/**0:普通图片 1:加载更多*/
	public int type;
	
	public Picture(){}
	public static final Parcelable.Creator<Picture> CREATOR = new Creator<Picture>() {

		@Override
		public Picture[] newArray(int size) {
			return new Picture[size];
		}

		@Override
		public Picture createFromParcel(Parcel source) {
			return new Picture(source);
		}
	};
	
	public Picture(Parcel in) {
		id = in.readInt();
		path = in.readString();
		key = in.readString();
		feedbackId = in.readString();
	}
	

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(path);
		dest.writeString(key);
		dest.writeString(feedbackId);
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFeedbackId() {
		return feedbackId;
	}

	public void setFeedbackId(String feedbackId) {
		this.feedbackId = feedbackId;
	}
}

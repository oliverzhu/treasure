package com.home.saveInstance;

import android.os.Parcel;
import android.os.Parcelable;

public class People implements Parcelable{
	public int id;
	public String name;
	@Override
	public int describeContents() {
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		
	}

}

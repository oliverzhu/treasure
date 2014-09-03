package com.home.srcolllayout;

import android.graphics.Bitmap;

public class Image {
	public String id;
	public Bitmap bitmap;
	public String key;
	/**0:普通图片 1:加载更多*/
	public int type;
	
	@Override
	public boolean equals(Object o) {
		if(o == null) return false;
		if(this.getClass() == o.getClass())
		{
			Image op = (Image) o;
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

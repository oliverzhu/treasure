package com.client.customerservicecenter.adapter;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.bitmap.BitmapLoader;
import com.client.customerservicecenter.util.bitmap.FeedbackPictureLoader;
import com.client.customerservicecenter.util.bitmap.ThumbNailLoader.ImageCallback;
import com.client.customerservicecenter.view.HSVLayout;

public class HSVAdapter extends BaseAdapter {
	private Context mContext; 
	private ArrayList<Picture> list; 
	private Map<String, String> userInfoMap;
	
	private HSVLayout hsvLayout;
	
	private ImageCallback imageCallback = new ImageCallback() {
		@Override
		public void imageLoaded(Bitmap bitmap, String itemId, int pos) {
			ImageView imageViewByTag = (ImageView) hsvLayout
					.findViewWithTag(itemId);
			if(imageViewByTag != null 
					&& bitmap != null)
			{
				synchronized (imageViewByTag) {
					boolean isLoaded = 
							(Boolean) imageViewByTag.getTag(R.id.isLoaded);
					if(!isLoaded)
					{
						setImageBitmap(imageViewByTag,bitmap);
						imageViewByTag.setTag(R.id.isLoaded, true);
					}
				}
			}
		}
	};
    public HSVAdapter(Context context,HSVLayout hsvLayout){
        this.mContext=context; 
        this.hsvLayout = hsvLayout;
        this.list=new ArrayList<Picture>();
    }
    
    public void setDatas(ArrayList<Picture> dataSource)
    {
    	list.clear();
    	list.addAll(dataSource);
    }
    
    public void setUserInfoMap(Map<String, String> userInfoMap)
    {
    	this.userInfoMap = userInfoMap;
    }

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Picture getItem(int arg0) {
		if(getCount() == 0)
		{
			return null;
		}
		return list.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
		View view = LayoutInflater.from(mContext).inflate(R.layout.item_hsv,null);  
        ImageView image=(ImageView)view.findViewById(R.id.image);
        
        Picture picture = list.get(arg0);
        image.setImageResource(R.drawable.default_feedback_icon);
        if(picture != null)
        {
        	BitmapLoader bitmapLoader = 
					new FeedbackPictureLoader(
							mContext, 
							String.valueOf(picture.getId()),
							userInfoMap,
							picture.key, 
							Constants.TYPE_MICROTHUMBNAIL, 
							ContextUtils.dip2px(mContext, Constants.SIZE_SHOW_THUMBNAIL_WEIGHT), 
							ContextUtils.dip2px(mContext, Constants.SIZE_SHOW_THUMBNAIL_HEIGHT), 
							arg0, 
							imageCallback);
			bitmapLoader.startLoad();
			image.setTag(String.valueOf(picture.getId()));
			image.setTag(R.id.isLoaded, false);
        }
        return view; 
	}
	
	/**
	 * 图片加载动画
	 * @param imageView
	 * @param bitmap
	 */
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {         
		// Use TransitionDrawable to fade in.         
		final TransitionDrawable td = 
				new TransitionDrawable(
						new Drawable[] { new ColorDrawable(android.R.color.transparent), 
								new BitmapDrawable(mContext.getResources(), bitmap) });         
		imageView.setImageDrawable(td);
		td.startTransition(400);
	}
}

package com.home.adapter;

import java.util.ArrayList;
import java.util.HashSet;

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
import android.widget.ListView;
import android.widget.TextView;

import com.home.R;
import com.home.bean.OperatingBean;
import com.home.util.Constants;
import com.home.util.bitmap.BitmapLoader;
import com.home.util.bitmap.ThumbNailLoader;
import com.home.util.bitmap.ThumbNailLoader.ImageCallback;

/**
 * @author jianwen.zhu
 * 2013/12/11
 */
public class MessageListFragmentAdapter extends BaseAdapter{
	private Context context;
	public ArrayList<OperatingBean> listOperating = new ArrayList<OperatingBean>();
	public HashSet<OperatingBean> setOperating = new HashSet<OperatingBean>();
	private ListView newsLv;
	private ImageCallback imageCallback = new ImageCallback() {
		@Override
		public void imageLoaded(Bitmap bitmap, String imageUrl, int pos) {
			ImageView imageViewByTag = (ImageView) newsLv
					.findViewWithTag(imageUrl);
			if(imageViewByTag != null && isActiveSlot(pos) 
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
	public MessageListFragmentAdapter(Context context,ListView newsLv){
		this.context = context;
		this.newsLv = newsLv;
	}
	
	public void setData(ArrayList<OperatingBean> data) {
        if (data != null) {
            for (OperatingBean operateEntry : data) {
            	if(setOperating.add(operateEntry))
            	{
            		listOperating.add(operateEntry);
            	}
            }
        }
    }

	
	public Context getContext() {
		return context;
	}


	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		if(listOperating!=null && listOperating.size() > 0){
			return listOperating.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(listOperating!=null && listOperating.size() > position){
			return listOperating.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(context).inflate(R.layout.message_item, null);
			holder = new ViewHolder();
			holder.titleNews = (TextView) convertView.findViewById(R.id.titleNews);
			holder.titleTime = (TextView) convertView.findViewById(R.id.titleTime);
			holder.newsDescription = (TextView) convertView.findViewById(R.id.newsDescription);
			
			holder.newsImg = (ImageView) convertView.findViewById(R.id.newsImg); 
			convertView.setTag(holder); 
			
			
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		final OperatingBean item = (OperatingBean)getItem(position);
		if(item != null){
			String titleNewsStr = item.getOperatDesc();
			String newsDescriptionStr = item.getOperatMsg();
			String newsImgStr = item.getOperatURL();
			String order = item.getOperatOrder();
			String date = item.getDateCreation();
			
			holder.titleNews.setText(titleNewsStr);
			holder.newsDescription.setText(newsDescriptionStr);
			holder.titleTime.setText(date);
			
			if("IMG".equals(order))
			{
				holder.newsImg.setImageResource(R.drawable.default_news_icon);
				BitmapLoader bitmapLoader = 
						new ThumbNailLoader(
								context, 
								newsImgStr, 
								Constants.TYPE_MICROTHUMBNAIL, 
								150, 120, 
								position, 
								imageCallback);
				bitmapLoader.startLoad();
				holder.newsImg.setTag(newsImgStr);
				holder.newsImg.setTag(R.id.isLoaded, false);
			}else if("AUD".equals(order))
			{
				holder.newsImg.setImageResource(R.drawable.default_audio_icon);
			}else if("VID".equals(order))
			{
				holder.newsImg.setImageResource(R.drawable.default_audio_icon);
			}
		}
		
		return convertView;
	}
	
	
	public class ViewHolder{
		private TextView titleTime;
		/** 新闻标题 */
		private TextView titleNews;
		/** 新闻概要 */
		private TextView newsDescription;
		/** 新闻缩略图 */
		private ImageView newsImg;
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
								new BitmapDrawable(context.getResources(), bitmap) });         
		imageView.setImageDrawable(td);
		td.startTransition(400);
	}
	
	public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= newsLv.getFirstVisiblePosition() && slotIndex <= newsLv.getLastVisiblePosition();
    }
}

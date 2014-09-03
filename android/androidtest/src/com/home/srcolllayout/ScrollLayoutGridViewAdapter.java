package com.home.srcolllayout;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.home.R;

public class ScrollLayoutGridViewAdapter extends BaseAdapter {
	private Context context;
	private int layoutResId;
	
	protected List<Object> datas;
	protected GridView list;
	
	protected int itemWeight;
	protected int itemHeight;
	
	private  GridViweItemHolder viewHolder;
	
	public ScrollLayoutGridViewAdapter(Context context,
			int layoutResId) {
		this.context = context;
		this.layoutResId = layoutResId;
	}
	
	public void removeItem(int position)
	{
		datas.remove(position);
	}
	public void setItemWeight(int itemWeight) {
		this.itemWeight = itemWeight;
	}

	public void setItemHeight(int itemHeight) {
		this.itemHeight = itemHeight;
	}
	public  void setDatas(List<Object> datas)
	{
		this.datas = datas;
	}
	
	public void setView(GridView view)
	{
		this.list = view;
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(layoutResId,
					null);
			viewHolder = new GridViweItemHolder();
			viewHolder.itemImg = (ImageView) convertView
					.findViewById(R.id.item_img);
			viewHolder.itemImg.setLayoutParams(new LinearLayout.LayoutParams(itemWeight,itemHeight));
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (GridViweItemHolder) convertView.getTag();
			
		}

		final Image p = (Image) getItem(position);
		
		viewHolder.itemImg.setImageBitmap(p.bitmap);
		
		if(p.type == 1)
		{
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(context instanceof Activity)
					{
						Activity activity = (Activity) context;
						Intent picture = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						activity.startActivityForResult(picture, GridViewHelper.PICTURE);
					}
				}
			});
		}
		return convertView;
	}
	
	public  class GridViweItemHolder{
		public ImageView itemImg;
	}


}

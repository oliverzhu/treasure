package com.client.customerservicecenter.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.Constants.OnDatasourceChangeListener;
import com.client.customerservicecenter.widget.scroll.GridViewHelper;

public class ScrollLayoutGridViewAdapter extends BaseAdapter {
	private Context context;
	private int layoutResId;
	
	protected List<Object> datas;
	protected GridView list;
	
	protected int itemWeight;
	protected int itemHeight;
	protected int totalDataSize;
	
	private  GridViweItemHolder viewHolder;
	
	private int imgDeleteWeight;
	private int imgDeleteHeight;
	
	private OnDatasourceChangeListener mDatasourceChangeListener; 
	
	public ScrollLayoutGridViewAdapter(Context context,
			int layoutResId) {
		this.context = context;
		this.layoutResId = layoutResId;
		imgDeleteWeight = ContextUtils.dip2px(context, Constants.SIZE_DELETE_ICON_WEIGHT);
		imgDeleteHeight = ContextUtils.dip2px(context, Constants.SIZE_DELETE_ICON_HEIGHT);
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
	
	public void setTotalDataSize(int totalDataSize) {
		this.totalDataSize = totalDataSize;
	}
	
	public void setDatasourceChangeListener(OnDatasourceChangeListener datasourceChangeListener)
	{
		mDatasourceChangeListener = datasourceChangeListener;
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
			convertView.setLayoutParams(
					new AbsListView.LayoutParams(itemWeight + imgDeleteWeight / 2,
							itemHeight + imgDeleteHeight / 2));
			RelativeLayout.LayoutParams itemImgParams = 
					new RelativeLayout.LayoutParams(
							itemWeight, 
							itemHeight);
			itemImgParams.addRule(RelativeLayout.CENTER_IN_PARENT);
			viewHolder.itemImg.setLayoutParams(itemImgParams);
			
			viewHolder.itemDelete = (ImageView) convertView.findViewById(R.id.delete_img);
			RelativeLayout.LayoutParams deleteParams = 
					new RelativeLayout.LayoutParams(
							imgDeleteWeight, 
							imgDeleteHeight);
			deleteParams.addRule(RelativeLayout.ABOVE,R.id.item_img);
			deleteParams.addRule(RelativeLayout.RIGHT_OF,R.id.item_img);
			deleteParams.bottomMargin = -(imgDeleteWeight / 2);
			deleteParams.leftMargin = -(imgDeleteHeight / 2);
			viewHolder.itemDelete.setLayoutParams(deleteParams);
			
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (GridViweItemHolder) convertView.getTag();
			
		}

		final Picture p = (Picture) getItem(position);
		viewHolder.itemDelete.setTag(p);
		
		viewHolder.itemImg.setImageBitmap(p.bitmap);
		
		if(p.type == 1)
		{
			viewHolder.itemDelete.setVisibility(View.GONE);
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(context instanceof Activity)
					{
						if((totalDataSize - 1) >= Constants.THRESHOLD_PICTURE_SIZE)
						{
							ContextUtils.showToast(context, R.string.toast_picutre_size, Toast.LENGTH_SHORT);
							return;
						}
						
						Activity activity = (Activity) context;
						Intent picture = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						activity.startActivityForResult(picture, GridViewHelper.PICTURE);
					}
				}
			});
		}
		
		viewHolder.itemDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Picture p = (Picture) arg0.getTag();
				mDatasourceChangeListener.onDatasourceChange(p);
			}
		});
		return convertView;
	}
	
	public  class GridViweItemHolder{
		public ImageView itemImg;
		public ImageView itemDelete;
	}


}

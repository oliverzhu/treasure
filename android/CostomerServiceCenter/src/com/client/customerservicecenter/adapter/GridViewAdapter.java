package com.client.customerservicecenter.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.bean.GridViewItem;

public class GridViewAdapter extends BaseAdapter {
	private Context context;
	protected ArrayList<GridViewItem> datas;

	private  GridItemHolder viewHolder;
	
	public GridViewAdapter(Context context,ArrayList<GridViewItem> datas) {
		this.context = context;
		this.datas = datas;
	}
	

	@Override
	public int getCount() {
		int count = 0;
		if(datas != null)
		{
			count = datas.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position) {
		if(datas != null)
		{
			return datas.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.item_grid,
					null);
			viewHolder = new GridItemHolder();
			viewHolder.itemImg = (ImageView) convertView
					.findViewById(R.id.image_item);
			
			viewHolder.itemName = (TextView) convertView.findViewById(R.id.text_item);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (GridItemHolder) convertView.getTag();
			
		}

		final GridViewItem p = (GridViewItem) getItem(position);
		convertView.setBackgroundDrawable(p.item_bg);
		viewHolder.itemImg.setImageDrawable(p.item_img);
		viewHolder.itemName.setText(p.item_name);
		
		return convertView;
	}
	
	public  class GridItemHolder{
		public ImageView itemImg;
		public TextView itemName;
	}


}

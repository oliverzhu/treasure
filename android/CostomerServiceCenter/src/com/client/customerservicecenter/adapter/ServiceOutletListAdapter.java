package com.client.customerservicecenter.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.bean.ServiceOutletInfo;

/**
 * @author jianwen.zhu
 * 2013/6/6
 */
public class ServiceOutletListAdapter extends BaseAdapter{
	private Context mContext;
	public ArrayList<ServiceOutletInfo> dataSource = new ArrayList<ServiceOutletInfo>();
	public ServiceOutletListAdapter(Context context){
		this.mContext = context;
	}
	
	public void setData(List<ServiceOutletInfo> data) {
		dataSource.clear();
		dataSource.addAll(data);
		notifyDataSetChanged();
    }

	public Context getContext() {
		return mContext;
	}


	public void setContext(Context context) {
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return dataSource.size();
	}

	@Override
	public Object getItem(int position) {
		return dataSource.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_serviceoutlet, null);
			holder = new ViewHolder();
			holder.primaryServiceOutlet = (TextView) convertView.findViewById(R.id.primary_serviceoutlet);
			holder.summaryPhone = (TextView) convertView.findViewById(R.id.summary_phone);
			holder.summaryAddress = (TextView) convertView.findViewById(R.id.summary_address);
			convertView.setTag(holder); 
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		ServiceOutletInfo bean = (ServiceOutletInfo) getItem(position);
		
		if(bean != null)
		{
			holder.primaryServiceOutlet.setText(bean.getOutletName());
			holder.summaryPhone.setText(bean.getPhone());
			holder.summaryAddress.setText(bean.getAddress());
		}
		return convertView;
	}
	
	
	public class ViewHolder{
		private TextView primaryServiceOutlet;
		private TextView summaryPhone;
		private TextView summaryAddress;
	}
}

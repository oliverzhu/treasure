package com.client.costomerservicecenter.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.client.costomerservicecenter.R;
import com.client.costomerservicecenter.adapter.GridViewAdapter;
import com.client.costomerservicecenter.bean.GridViewItem;
import com.client.costomerservicecenter.util.Constants;


/**
 * ”√ªß∑¥¿°
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public class FeedbackFragment extends BaseFragment {
	private GridView gridView;
	private GridViewAdapter gridViewAdapter;
	private ArrayList<GridViewItem> datas;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_GRIDVIEW_DATA:
				gridViewAdapter = new GridViewAdapter(mContext,datas);
				gridView.setAdapter(gridViewAdapter);
				
				gridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
							long arg3) {
					}
				});
				break;
			default:
				break;
			}
		}
	};
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_feedback, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		gridView = (GridView) getActivity().findViewById(R.id.gridView);
		
		new LoadGridViewDataTask().execute();
	}
	
	private void initGridViewDatas()
	{
		datas = new ArrayList<GridViewItem>();
		
		GridViewItem item_net = new GridViewItem();
		item_net.item_img = getResources().getDrawable(R.drawable.img_error_net);
		item_net.item_bg = getResources().getDrawable(R.drawable.item_net_selector);
		item_net.item_name = getResources().getString(R.string.error_net);
		item_net.item_tag = Constants.ERROR_NET;
		item_net.item_type = String.valueOf(Constants.TYPE_NET);
		
		GridViewItem item_system = new GridViewItem();
		item_system.item_img = getResources().getDrawable(R.drawable.img_error_system);
		item_system.item_bg = getResources().getDrawable(R.drawable.item_system_selector);
		item_system.item_name = getResources().getString(R.string.error_system);
		item_system.item_tag = Constants.ERROR_SYSTEM;
		item_system.item_type = String.valueOf(Constants.TYPE_SYSTEM);
		
		GridViewItem item_battery = new GridViewItem();
		item_battery.item_img = getResources().getDrawable(R.drawable.img_error_battery);
		item_battery.item_bg = getResources().getDrawable(R.drawable.item_battery_selector);
		item_battery.item_name = getResources().getString(R.string.error_battery);
		item_battery.item_tag = Constants.ERROR_BATTERY;
		item_battery.item_type = String.valueOf(Constants.TYPE_BATTERY);
		
		GridViewItem item_phone = new GridViewItem();
		item_phone.item_img = getResources().getDrawable(R.drawable.img_error_phone);
		item_phone.item_bg = getResources().getDrawable(R.drawable.item_phone_selector);
		item_phone.item_name = getResources().getString(R.string.error_phone);
		item_phone.item_tag = Constants.ERROR_PHONE;
		item_phone.item_type = String.valueOf(Constants.TYPE_PHONE);
		
		GridViewItem item_application = new GridViewItem();
		item_application.item_img = getResources().getDrawable(R.drawable.img_error_application);
		item_application.item_bg = getResources().getDrawable(R.drawable.item_application_selector);
		item_application.item_name = getResources().getString(R.string.error_application);
		item_application.item_tag = Constants.ERROR_APPLICATION;
		item_application.item_type = String.valueOf(Constants.TYPE_APPLICATION);
		
		GridViewItem item_data = new GridViewItem();
		item_data.item_img = getResources().getDrawable(R.drawable.img_error_data);
		item_data.item_bg = getResources().getDrawable(R.drawable.item_data_selector);
		item_data.item_name = getResources().getString(R.string.error_data);
		item_data.item_tag = Constants.ERROR_DATA;
		item_data.item_type = String.valueOf(Constants.TYPE_DATA);
		
		GridViewItem item_others = new GridViewItem();
		item_others.item_img = getResources().getDrawable(R.drawable.img_error_others);
		item_others.item_bg = getResources().getDrawable(R.drawable.item_others_selector);
		item_others.item_name = getResources().getString(R.string.error_others);
		item_others.item_tag = Constants.ERROR_OTHERS;
		item_others.item_type = String.valueOf(Constants.TYPE_OTHERS);
		
		GridViewItem item_advise = new GridViewItem();
		item_advise.item_img = getResources().getDrawable(R.drawable.img_sugar_advise);
		item_advise.item_bg = getResources().getDrawable(R.drawable.item_advise_selector);
		item_advise.item_name = getResources().getString(R.string.sugar_advise);
		item_advise.item_tag = Constants.SUGAR_ADVISE;
		item_advise.item_type = String.valueOf(Constants.TYPE_SUGAR_ADVISE);
		
		GridViewItem item_history = new GridViewItem();
		item_history.item_img = getResources().getDrawable(R.drawable.img_history_feedback);
		item_history.item_bg = getResources().getDrawable(R.drawable.item_history_selector);
		item_history.item_name = getResources().getString(R.string.history_feedback);
		item_history.item_tag = Constants.HISTORY_FEEDBACK;
		item_history.item_type = String.valueOf(Constants.TYPE_HISTORY_FEEDBACK);
		
		datas.add(item_net);
		datas.add(item_battery);
		datas.add(item_phone);
		datas.add(item_data);
		datas.add(item_system);
		datas.add(item_application);
		datas.add(item_others);
		datas.add(item_advise);
		datas.add(item_history);
	}
	
	private  class LoadGridViewDataTask extends AsyncTask<Object, Integer, Object>
	{

		@Override
		protected Object doInBackground(Object... params) {
			initGridViewDatas();
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_GRIDVIEW_DATA;
			mHandler.sendMessage(msg);
			return null;
		}
	}
	
}

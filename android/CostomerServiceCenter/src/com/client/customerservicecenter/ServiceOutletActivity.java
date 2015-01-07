package com.client.customerservicecenter;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.client.customerservicecenter.adapter.CityHeadersAdapter;
import com.client.customerservicecenter.adapter.ServiceOutletListAdapter;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CityInfo;
import com.client.customerservicecenter.bean.ServiceOutletInfo;
import com.client.customerservicecenter.job.LoadLocalProvinceAndCityDataJob;
import com.client.customerservicecenter.job.LoadLocatedCityDataJob;
import com.client.customerservicecenter.job.LoadNetServiceOutletJob;
import com.client.customerservicecenter.job.LoadSpecificCityDataJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.Log;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.widget.sup.SlidingUpPanelLayout;
import com.client.customerservicecenter.widget.sup.SlidingUpPanelLayout.PanelSlideListener;

/**
 * 服务网点
 * @author jianwen.zhu
 * @since 2014/10/17
 */
public class ServiceOutletActivity extends Activity {
	private static final String TAG = "ServiceOutletActivity";
	private Context mContext;
	private SlidingUpPanelLayout mLayout;
	private Switch switch_;
	
	private TextView barTitle;
	
	private GridView mGridView;
	private ImageView mProgressImageView;
	private AnimationDrawable mAnimationDrawable;
	
	private ListView lv;
	private ServiceOutletListAdapter adapter;
	private List<ServiceOutletInfo> infos;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		@SuppressWarnings("unchecked")
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_SERVICE_OUTLET:
				AppApplication.threadPool.submit(new LoadLocalProvinceAndCityDataJob(mContext, mHandler), null, ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_LOCATE_START:
				if(NetUtils.isNetWorkAvailable(mContext, null))
				{
					ContextUtils.showToast(mContext, R.string.toast_locating, Toast.LENGTH_SHORT);
					AppApplication.baiduMapHub.start();
				}else
				{
					ContextUtils.showToast(mContext, R.string.toast_net_error, Toast.LENGTH_SHORT);
				}
				break;
			case Constants.MSG_LOCATE_STOP:
				AppApplication.baiduMapHub.stop();
				break;
			case Constants.MSG_LOCATE_SUCCESS:
				AppApplication.baiduMapHub.stop();
				Bundle data = msg.getData();
				String city = data.getString("city");
				LoadLocatedCityDataJob  loadLocalCityData = new LoadLocatedCityDataJob(mContext, city);
				AppApplication.threadPool.submit(
						loadLocalCityData, 
						mLocalCityDataListener, 
						ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_LOCATE_FAIL:
				AppApplication.baiduMapHub.stop();
				String cityStr = Preferences.getLocatedCity(AppApplication.mPrefs);
				if(cityStr != null && cityStr.trim().length() != 0)
				{
					sendMessageForLocateSuccess(cityStr);
				}
				break;
			case Constants.MSG_LOCATE_CITY_DATA:
				infos = (List<ServiceOutletInfo>) msg.obj;
				bindLocalCityDataTouUi();
				break;
				
			case Constants.MSG_LOCAL_CITY_DATA:
				stopDrawableAnimation();
				mGridView.setAdapter(new CityHeadersAdapter(getApplicationContext(), AppApplication.baiduMapHub.getCityList(),
						R.layout.sgh_header, R.layout.sgh_item));
				mGridView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						CityInfo cityInfo = AppApplication.baiduMapHub.getCityList().get(position);
						LoadSpecificCityDataJob  loadLocalCityData = new LoadSpecificCityDataJob(mContext, cityInfo);
						AppApplication.threadPool.submit(
								loadLocalCityData, 
								mSpecificCityDataListener, 
								ThreadPool.MODE_CPU);
						mLayout.collapsePanel();
					}
					
				});
				break;
			default:
				break;
			}
		};
	};
	
	private FutureListener<List<ServiceOutletInfo>> mLocalCityDataListener = 
    		new FutureListener<List<ServiceOutletInfo>>() {
		@Override
		public synchronized void onFutureDone(Future<List<ServiceOutletInfo>> infos) {
			mHandler.removeMessages(Constants.MSG_LOCATE_CITY_DATA);
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_LOCATE_CITY_DATA;
			msg.obj = infos.get();
			mHandler.sendMessage(msg);
		}
	};
	
	private FutureListener<List<ServiceOutletInfo>> mSpecificCityDataListener = 
    		new FutureListener<List<ServiceOutletInfo>>() {
		@Override
		public synchronized void onFutureDone(Future<List<ServiceOutletInfo>> infos) {
			mHandler.removeMessages(Constants.MSG_LOCATE_CITY_DATA);
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_LOCATE_CITY_DATA;
			msg.obj = infos.get();
			mHandler.sendMessage(msg);
		}
	};
	
	private BDLocationListener locationListener = new BDLocationListener() {
		private int tryCount = 0;
		
		@Override
		public void onReceiveLocation(BDLocation location) {
			tryCount ++;
			if(location.getLocType() == Constants.BAIDU_CODE_NET_RESULT)
			{
				String province = location.getProvince();
				String city = location.getCity();
				if(province != null && city != null)
				{
					Preferences.setLocatedCity(AppApplication.mPrefs, city);
					sendMessageForLocateSuccess(city);
					return;
				}
			}
			
			if(tryCount >= Constants.THRESHOLD_LOCATE_TRYCOUNT)
			{
				Message msg = mHandler.obtainMessage();
				msg.what = Constants.MSG_LOCATE_FAIL;
				mHandler.sendMessage(msg);
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mContext = this;
		setContentView(R.layout.activity_serviceoutlet);
		AppApplication.baiduMapHub.setLocationListener(locationListener);
		initUi();
		initData();
	}

	private void initUi() {
		barTitle = (TextView) findViewById(R.id.bar_title);
		switch_ = (Switch) findViewById(R.id.switch_);
		lv = (ListView) findViewById(R.id.city_list);
		mGridView = (GridView)findViewById(R.id.asset_grid);
		mProgressImageView = (ImageView) findViewById(R.id.anim);
        mAnimationDrawable = (AnimationDrawable) mProgressImageView.getDrawable();
		adapter = new ServiceOutletListAdapter(mContext);
		lv.setAdapter(adapter);
		findViewById(R.id.back).setOnClickListener(uiListener);
		findViewById(R.id.contactUs).setOnClickListener(uiListener);
		
		mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.setPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset,Log.APP);
            }

            @Override
            public void onPanelExpanded(View panel) {
                Log.i(TAG, "onPanelExpanded",Log.APP);

            }

            @Override
            public void onPanelCollapsed(View panel) {
                Log.i(TAG, "onPanelCollapsed",Log.APP);

            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored",Log.APP);
            }

            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden",Log.APP);
            }
        });
        
        switch_.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checkedState) {
				Preferences.setCheckedState(AppApplication.mPrefs, checkedState);
				int isStart = Constants.MSG_LOCATE_STOP;
				if(checkedState)
				{
					isStart = Constants.MSG_LOCATE_START;
				}
				sendMessageForLocate(isStart);
			}
		});
		
		barTitle.setText(R.string.title_service_outlets);
		
		startDrawableAnimation();
	}
	
	private void initData()
	{
		if(Preferences.getCheckedState(AppApplication.mPrefs))
		{
			switch_.setChecked(true);
			sendMessageForLocate(Constants.MSG_LOCATE_START);
		}else
		{
			String locateCity = 
					Preferences.getLocatedCity(AppApplication.mPrefs);
			if(locateCity != null && locateCity.trim().length() != 0)
			{
				sendMessageForLocateSuccess(locateCity);
			}
		}
		
		AppApplication.threadPool.submit(new LoadNetServiceOutletJob(mContext,mHandler), null, ThreadPool.MODE_NETWORK);
	}
	
	private void bindLocalCityDataTouUi()
	{
		if(infos != null && infos.size() != 0)
		{
			if(lv.getVisibility() == View.INVISIBLE)
			{
				lv.startAnimation(AnimationUtils.loadAnimation(mContext,
	                    android.R.anim.fade_in));
				lv.setVisibility(View.VISIBLE);
			}
			adapter.setData(infos);
		}
	}
	
	private void bindLocalServiceOutletTouUi()
	{
		if(mGridView.getVisibility() == View.INVISIBLE)
		{
			mGridView.startAnimation(AnimationUtils.loadAnimation(mContext,
                    android.R.anim.fade_in));
			mGridView.setVisibility(View.VISIBLE);
		}
	}
	
	private OnClickListener uiListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.back:
				if(mLayout.isPanelExpanded())
				{
					mLayout.collapsePanel();
				}else
				{
					finish();
				}
				break;
			case R.id.contactUs:
				Intent contactIntent = new Intent();
				contactIntent.setAction(Intent.ACTION_CALL);
				contactIntent.setData(Uri.parse("tel:" + getResources().getString(R.string.contactPhoneNumber)));
				startActivity(contactIntent);
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onStop() {
		super.onStop();
		AppApplication.baiduMapHub.stop();
		stopDrawableAnimation();
	}
	
	private void sendMessageForLocate(int isStart)
	{
		Message msg = mHandler.obtainMessage();
		msg.what = isStart;
		mHandler.sendMessage(msg);
	}
	
	private void sendMessageForLocateSuccess(String city)
	{
		if(city != null && city.trim().length() != 0)
		{
			Bundle data = new Bundle();
			data.putString("city", city);
			Message msg = mHandler.obtainMessage();
			msg.what = Constants.MSG_LOCATE_SUCCESS;
			msg.setData(data);
			mHandler.sendMessage(msg);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if(mLayout.isPanelExpanded())
			{
				mLayout.collapsePanel();
				return true;
			}
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void startDrawableAnimation()
	{
		mProgressImageView.setVisibility(View.VISIBLE);
		mAnimationDrawable.start();
	}
	
	private void stopDrawableAnimation()
	{
		bindLocalServiceOutletTouUi();
		mProgressImageView.setVisibility(View.GONE);
		mAnimationDrawable.stop();
	}
}

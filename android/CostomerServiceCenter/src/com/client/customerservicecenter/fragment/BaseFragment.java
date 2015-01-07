package com.client.customerservicecenter.fragment;

import java.util.Map;

import com.client.customerservicecenter.hub.UserServiceHub;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

/**
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public abstract class BaseFragment extends Fragment {
	protected Context mContext;
	protected Map userInfoMap;
	protected SharedPreferences mPref;
	protected UserServiceHub mServiceHub;
	public static BaseFragment createFaqFragment()
	{
		return new FaqFragment();
	}
	
	public static BaseFragment createElectronicWarrantyCardFragment()
	{
		return new ElectronicWarrantyCardFragment();
	}
	
	public static BaseFragment createFeedbackFragment()
	{
		return new FeedbackFragment();
	}
	
	public static BaseFragment createMoreFragment()
	{
		return new MoreFragment();
	}
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mServiceHub = new UserServiceHub();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		TextView text = new TextView(getActivity());
        text.setGravity(Gravity.CENTER);
        text.setText("content");
        text.setTextSize(20 * getResources().getDisplayMetrics().density);
        text.setPadding(20, 20, 20, 20);

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);
        layout.addView(text);
		return layout;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mContext = getActivity();
	}
	
	 @Override
	public void onStart() {
		super.onStart();
	}
	 
	 @Override
	public void onResume() {
		super.onResume();
	}
	 
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onStop() {
		super.onStop();
	}
	 
	 @Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	 
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}

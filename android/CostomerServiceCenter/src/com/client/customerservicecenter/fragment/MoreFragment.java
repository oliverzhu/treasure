package com.client.customerservicecenter.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.ServiceOutletActivity;
import com.client.customerservicecenter.WarrantyPolicyActivity;
import com.common.upgrade.core.CheckNewVersionListener;
import com.common.upgrade.core.UpgradeManager;


/**
 * @author jianwen.zhu
 * @since 2014/8/25
 */
public class MoreFragment extends BaseFragment{
	private ViewGroup itemWarrantyPolicy;
	private ViewGroup itemServiceOutlets;
	private ViewGroup itemVersionUpdate;
	
	private UpgradeManager upgradeMangeer;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_more, container,false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		itemWarrantyPolicy = (ViewGroup) getActivity().findViewById(R.id.item_warranty_policy);
		itemServiceOutlets = (ViewGroup) getActivity().findViewById(R.id.item_service_outlets);
		itemVersionUpdate = (ViewGroup) getActivity().findViewById(R.id.item_version_update);
		
		itemServiceOutlets.setOnClickListener(onClickListener);
		itemWarrantyPolicy.setOnClickListener(onClickListener);
		itemVersionUpdate.setOnClickListener(onClickListener);
		
		initVersionState();
	}
	
	private OnClickListener onClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.item_service_outlets:
				Intent serviceOutletIntent = new Intent(mContext,ServiceOutletActivity.class);
				mContext.startActivity(serviceOutletIntent);
				break;
			case R.id.item_warranty_policy:
				Intent warrantyPolicyIntent = new Intent(mContext,WarrantyPolicyActivity.class);
				mContext.startActivity(warrantyPolicyIntent);
				break;
			case R.id.item_version_update:
				upgradeMangeer.askForNewVersion();
				break;
			default:
				break;
			}
		}
	};
	
	private void initVersionState()
	{
		upgradeMangeer = UpgradeManager.newInstance(
						mContext, mContext.getApplicationInfo().packageName, 
						mContext.getResources().getString(R.string.app_name));
		
		upgradeMangeer.askForNewVersionFlag(new CheckNewVersionListener() {
			
			@Override
			public void checkNewVersion(boolean result) {
				ImageView newVersionIndicator = 
						(ImageView) itemVersionUpdate.findViewById(R.id.new_version_indicator);
				ImageView indicator = 
						(ImageView) itemVersionUpdate.findViewById(R.id.indicator);
				if(result)
				{
					newVersionIndicator.setVisibility(View.VISIBLE);
					indicator.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
}

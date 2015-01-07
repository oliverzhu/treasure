package com.client.customerservicecenter;

import java.util.Map;

import com.client.customerservicecenter.hub.UserServiceHub;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/16
 */
public class BaseActivity extends Activity {
	protected Context mContext;
	protected UserServiceHub mServiceHub;
	protected Map userInfoMap;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		mServiceHub = new UserServiceHub();
	}

}

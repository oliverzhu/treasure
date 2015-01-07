package com.client.customerservicecenter;

import android.app.Activity;
import android.os.Bundle;

/**
 * 电子保单说明
 * @author jianwen.zhu
 *
 */
public class EwcActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_ewc);
	}

}

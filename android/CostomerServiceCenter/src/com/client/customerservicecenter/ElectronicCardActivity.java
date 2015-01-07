package com.client.customerservicecenter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/11/5
 */
public class ElectronicCardActivity extends Activity {
	private TextView barTitle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_electronic_card);
		
		initUi();
	}
	
	private void initUi()
	{
		barTitle = (TextView) findViewById(R.id.bar_title);
		barTitle.setText(R.string.fragment_title_electronic_warranty_card);
		
		findViewById(R.id.back).setOnClickListener(uiListener);
		findViewById(R.id.contactUs).setOnClickListener(uiListener);
	}
	
	private OnClickListener uiListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.back:
				finish();
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

}

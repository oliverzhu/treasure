package com.home.looper;

import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.home.R;

public class LooperTestActivity extends Activity {
	private TextView tv;
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			tv.setText("in other thread");
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		tv = (TextView) findViewById(R.id.tv);
		
		new Thread()
		{
			public void run() 
			{
				try {
					TimeUnit.SECONDS.sleep(2);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				handler.sendMessage(handler.obtainMessage());
			}
		}.start();
		
		
		tv.setText("in main thread");
	}
	

}

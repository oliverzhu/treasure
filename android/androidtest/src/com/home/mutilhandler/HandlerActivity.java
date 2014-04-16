package com.home.mutilhandler;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import com.home.R;

public class HandlerActivity extends Activity {
	
	private TextView tv1;
	private TextView tv2;
	
	class MainThreadHandler extends Handler
	{
		public MainThreadHandler(Looper looper)
		{
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what)
			{
				case 1:
					tv1.setText("AAA");
					break;
					
			}
			
			super.handleMessage(msg);
		}
	}
	
	private Handler handler1 = new MainThreadHandler(Looper.getMainLooper());
	
	private Handler handler2 = new Handler()
	{
		public void handleMessage(Message msg) 
		{
			switch (msg.what) {
			case 1:
				tv2.setText("BBB");
				break;

			default:
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mutil_handler);
		
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		
		new Thread()
		{
			public void run() 
			{
				Message msg = handler1.obtainMessage();
				msg.what = 1;
				handler1.sendMessage(msg);
			};
		}.start();
		
		new Thread()
		{
			public void run() 
			{
				Message msg = handler1.obtainMessage();
				msg.what = 1;
				handler2.sendMessage(msg);
			};
		}.start();
	}

}

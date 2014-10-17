package com.home.countdowntimer;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.home.R;
import com.home.util.Log;

/**
 * 倒计时器
 * @author jianwen.zhu
 *
 */
public class CountDownTimerActivity extends Activity {
	private static final String TAG = "CountDownTimerActivity";
	private Button btn;
	private TextView tv;
	
	private MyCount mc;
	// 倒计时总时长
	private long millisUntilFinished = 10000;
	// 时间间隔
	private long countDownInterval = 1000;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_countdowntimer);
		
		btn = (Button) findViewById(R.id.btn);
		tv = (TextView) findViewById(R.id.tv);
		
		tv.setText(millisUntilFinished / 1000 + "");
		
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				millisUntilFinished = 10000;
				countDownInterval = 10000;
				mc = new MyCount(millisUntilFinished, countDownInterval);
				mc.start();
			}
		});
	}
	
	/* 定义一个倒计时的内部类 */
	class MyCount extends CountDownTimer {
		public MyCount(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			Log.i(TAG, "DONE!", Log.APP);
			tv.setText("Done!");
		}

		@Override
		public void onTick(long millisUntilFinished) {
			Log.i(TAG, "millisUntilFinished = " + millisUntilFinished/1000, Log.APP);
			CountDownTimerActivity.this.millisUntilFinished = millisUntilFinished;
			tv.setText(millisUntilFinished / 1000 + "");
		}
	}

}

package com.home.transitiondrawable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.home.R;

public class TransitionDrawableActivity extends Activity {
	private ImageView image;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case 1:
				setImageBitmap(image,getResources().getDrawable(R.drawable.default_audio_icon));
				break;

			default:
				break;
			}
		};
	};
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_transitiondrawable);
		
		image = (ImageView) findViewById(R.id.image);
		image.setBackground(getResources().getDrawable(R.drawable.default_news_icon));
		
		new Thread()
		{
			public void run() 
			{
				try {
					Thread.sleep(1500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Message msg = mHandler.obtainMessage();
				msg.what = 1;
				mHandler.sendMessage(msg);
			};
		}.start();
	}
	
	private void setImageBitmap(ImageView imageView, Drawable drawable) {         
		// Use TransitionDrawable to fade in.         
		final TransitionDrawable td = 
				new TransitionDrawable(
						new Drawable[] { new ColorDrawable(android.R.color.transparent), 
								drawable });         
		imageView.setImageDrawable(td);
		td.startTransition(1000);
	}

}

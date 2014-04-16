package com.home.anim.rotate;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.home.R;

public class RotateActivity extends Activity implements OnClickListener{
	private ImageView imageView1;
	private ImageView imageView2;
	private Button btn1;
	private Button btn2;
	private AnimationSet animation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rotate);
		
		imageView1 = (ImageView) findViewById(R.id.imageview1);
		imageView2 = (ImageView) findViewById(R.id.imageview2);
		
		animation = 
				(AnimationSet) AnimationUtils.loadAnimation(this, R.anim.animation_rotate);
		
		animation.setFillEnabled(true);
		animation.setFillAfter(true);
		imageView1.startAnimation(animation);
		
		btn1 = (Button) findViewById(R.id.btn1);
		btn2 = (Button) findViewById(R.id.btn2);
		
		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);
		
		animation.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				imageView1.setImageResource(R.drawable.rotate_active);
				imageView2.setImageResource(R.drawable.arrow_active);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				imageView1.setImageResource(R.drawable.rotate_normal);
				imageView2.setImageResource(R.drawable.arrow_normal);
				imageView1.clearAnimation();
			}
		});
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn1:
			animation.cancel();
			break;
		case R.id.btn2:
			animation.reset();
			imageView1.startAnimation(animation);
			break;

		default:
			break;
		}
	}

}

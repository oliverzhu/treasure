package com.home.theme;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.home.R;

public class ThemeActivity extends FragmentActivity{
	private Button light;
	private Button dark;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		ThemeUtils.onActivityCreateSetTheme(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.themeactivity_main);
		
		light = (Button) findViewById(R.id.light);
		
		dark = (Button) findViewById(R.id.dark);
		
		light.setOnClickListener(listener);
		
		dark.setOnClickListener(listener);
	}
	
	private OnClickListener listener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.light:
				ThemeUtils.changeToTheme(ThemeActivity.this, 1);
				break;
			case R.id.dark:
				ThemeUtils.changeToTheme(ThemeActivity.this, 2);
				break;
			default:
				break;
			}
			
		}
	};
}

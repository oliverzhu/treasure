package com.home.imgEffect;

import com.home.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

public class ImgEffectActivity extends Activity {
	private LinearLayout parent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.commonimgeffect_main);
		
		parent = (LinearLayout) findViewById(R.id.parent);
		
		CommonImgEffectView effectView = new CommonImgEffectView(this);
		
		parent.addView(effectView);
	}

}

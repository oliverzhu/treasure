package com.home.density;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.TextView;

import com.home.R;

/**
 * 得到屏幕的密度
 * @author jianwen.zhu
 *
 */
public class DensityActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_density);
		
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		
		StringBuffer sb = new StringBuffer();
		sb.append("desity = " + dm.density + "\n");
		sb.append("densityDpi = " + dm.densityDpi + "\n");
		sb.append("heightPixels = " + dm.heightPixels + "\n");
		sb.append("widthPixels = " + dm.widthPixels + "\n");
		sb.append("scaledDensity = " + dm.scaledDensity + "\n");
		sb.append("xdpi = " + dm.xdpi + "\n");
		sb.append("ydpi = " + dm.ydpi + "\n");
		
		sb.append("1dip = " + dip2px(this,1) + "px \n");
		TextView tv = (TextView) findViewById(R.id.textView);
		tv.setText(sb.toString());
		
		
	}
	
	public static int dip2px(Context context, float dipValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(dipValue * scale +0.5f); 
	}
	
	public static int px2dip(Context context, float pxValue)
	{
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int)(pxValue / scale +0.5f); 
	}
}

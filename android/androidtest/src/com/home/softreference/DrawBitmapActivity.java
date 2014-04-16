package com.home.softreference;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.home.R;

public class DrawBitmapActivity extends Activity {
	private ImageView iv;
	private ImageView iv2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.softreference);
        
        iv = (ImageView) findViewById(R.id.iv);
        
        iv2 = (ImageView) findViewById(R.id.iv2);
        
        InputStream in = null;
		try {
			in = getResources().getAssets().open("help1.png");
		} catch (IOException e) {
			e.printStackTrace();
		}
		SoftReference<Drawable> sdrawable = new SoftReference<Drawable>(new BitmapDrawable(in));
		Drawable drawable = sdrawable.get();
		
		iv.setBackgroundDrawable(drawable);
    }
}
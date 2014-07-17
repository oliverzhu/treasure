package com.home.progressbar;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.home.R;

public class ProgessBarDemoActivity extends Activity {
	private ProgressBar progressBar;
	private Button button;
	
	private int delta = 10;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_progressbar);
		
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		button = (Button) findViewById(R.id.button);
		
		ShapeDrawable shape = new ShapeDrawable();
		shape.setShape(new RectShape());
		shape.getPaint().setColor(Color.BLUE);
		ClipDrawable clipDrawable = new ClipDrawable(shape, Gravity.CENTER, ClipDrawable.HORIZONTAL);

	    progressBar.setProgressDrawable(clipDrawable);
	    progressBar.setProgress(delta);
	    
	    button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(delta >=100)
				{
					delta = 0;
				}
				delta += 10;
				progressBar.setProgress(delta);
			}
		});
	    
	    
	}

}

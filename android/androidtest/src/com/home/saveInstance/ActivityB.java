package com.home.saveInstance;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.home.R;

public class ActivityB extends Activity {
	private Button toA;
	
	private TextView tv;
	
	private People p;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.saveinstance_main);
		
		String title = null;
		if(savedInstanceState != null)
		{
			p = savedInstanceState.getParcelable("people");
			title = savedInstanceState.getString("title");
		}
		
		toA = (Button) findViewById(R.id.directTo_B);
		
		toA.setText("B");
		
		tv = (TextView) findViewById(R.id.tv);
		
		toA.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		if(p != null)
		{
			tv.setText(p.name);
		}
	}
	
	
	//
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		People p = new People();
		p.id = 1;
		p.name = "yoyo";
		outState.putString("title","yoyo");
		
		outState.putParcelable("people", p);
	}

}

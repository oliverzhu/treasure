package com.home.keytag;

import com.home.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeyTagActivity extends Activity {
	
	private TextView tv1;
	private TextView tv2;
	
	private LinearLayout tvParent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mutil_handler);
		
		String A = "a";
		String B = "b";
		
		tvParent = (LinearLayout) findViewById(R.id.tvParent);
		
		tv1 = (TextView) findViewById(R.id.tv1);
		tv2 = (TextView) findViewById(R.id.tv2);
		
		tv1.setTag(R.id.paperId, "1");
		tv1.setTag(R.id.paperName, A);
		tv1.setTag(A);
		
		tv2.setTag(R.id.paperId, "2");
		tv2.setTag(R.id.paperName, B);
		tv2.setTag(B);
		
		tv1.setText(tv2.getTag(R.id.paperName) + "");
		tv2.setText(tv1.getTag(R.id.paperName) + "");
		
		TextView textView = (TextView) tvParent.findViewWithTag(A);
		
		Toast.makeText(this, textView.getText(), Toast.LENGTH_SHORT).show();
		
	}

}

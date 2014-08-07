package com.home.directory;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.home.R;

public class DirectoryActivity extends Activity{
	private Context mContext;
	private TextView tv;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_directory);
		
		mContext = this;
		
		tv = (TextView) findViewById(R.id.directory);
		
		String externalStoragePath = mContext.getExternalCacheDir().getPath();
		
		String dataPath = mContext.getCacheDir().getPath();
		
		tv.setText(externalStoragePath + "/n" + dataPath);
		
	}

}

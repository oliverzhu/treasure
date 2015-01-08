package com.common.upgrade;

import com.common.upgrade.core.UpgradeManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button update;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		final UpgradeManager upgradeMangeer = 
				UpgradeManager.newInstance(this, "key", getResources().getString(R.string.app_name));
		
		update = (Button) findViewById(R.id.update);
		update.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				upgradeMangeer.askForNewVersion();
			}
		});
	}
}

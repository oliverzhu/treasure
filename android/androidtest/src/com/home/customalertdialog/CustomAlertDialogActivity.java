package com.home.customalertdialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.home.R;

public class CustomAlertDialogActivity extends Activity {
	private Context mContext;
	private Button btn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alertdialog);
		mContext = this;
		btn = (Button) findViewById(R.id.button);
		
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog dialog = new AlertDialog(mContext);
                dialog.setTitle(getString(R.string.hello_world))
                    .setMessage(getString(R.string.hello_world))
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mContext, "ok", Toast.LENGTH_SHORT).show();
                        }})
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
			}
		});
	}
}

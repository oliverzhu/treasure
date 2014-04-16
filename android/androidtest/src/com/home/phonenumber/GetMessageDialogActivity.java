package com.home.phonenumber;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.home.R;

public class GetMessageDialogActivity extends Activity {
	private EditText inputPhoneNumber;
	private TextView currentNumber;
	private Button save;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_get);
		
		inputPhoneNumber = (EditText) findViewById(R.id.inputPhoneNumber);
		currentNumber = (TextView) findViewById(R.id.currentPhoneNumber);
		save = (Button) findViewById(R.id.save);
	}

}

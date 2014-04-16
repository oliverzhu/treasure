package com.home.phonenumber;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.home.R;

public class PhoneNumberTest extends Activity {
	private TextView phoneNumber;
	private TextView link;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_phonenumber);
		phoneNumber = (TextView) findViewById(R.id.phoneNumber);
		link = (TextView) findViewById(R.id.link);
		TelephonyManager phoneMgr = 
				(TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		phoneNumber.setText(phoneMgr.getLine1Number());
		
		String linkStr = "http://www.google.com";
		link.setText(
	            Html.fromHtml(
	                "<b>text3:</b>  Text with a " +
	                "<a href="+ linkStr +">" + linkStr + "</a>" +
	                "created in the Java source code using HTML."));
		link.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, 1, "1111111111111");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case 0:
			Intent messageGetIntent = new Intent();
			messageGetIntent.setClass(PhoneNumberTest.this, GetMessageDialogActivity.class);
			startActivity(messageGetIntent);
			break;

		default:
			break;
		}
		return true;
	}

}

package com.home.datepicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.home.R;

public class DatePickerActivity extends Activity {
	private Button btn;
	private TextView tv;
	private DatePickerSetListener mDatePickerSetListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_datepicker);
		btn = (Button) findViewById(R.id.btn);
		tv = (TextView) findViewById(R.id.tv);
		
		mDatePickerSetListener = new DatePickerSetListener();
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

                String birthday = "2014-10-13";
                if (birthday == null || birthday.trim().length() == 0 || birthday.equals("null")) {
                    birthday = "";
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                Calendar calendar = Calendar.getInstance();
                try {
                    date = format.parse(birthday);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date != null) {
                    calendar.setTime(date);
                }
                
                MyOSDatePickerDialog datePicker = new MyOSDatePickerDialog(
                		DatePickerActivity.this,
                        mDatePickerSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                configureDatePicker(datePicker.getDatePicker());
                datePicker.show();
            
			}
		});
	}
	
	static void configureDatePicker(DatePicker datePicker) {
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1949, Calendar.OCTOBER, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }
	
	private class DatePickerSetListener implements MyOSDatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            StringBuffer birthday = new StringBuffer();
            birthday.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
            tv.setText(birthday);
        }
    }

}

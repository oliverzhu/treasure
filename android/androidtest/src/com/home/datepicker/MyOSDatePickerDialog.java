package com.home.datepicker;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.home.R;
import com.home.datepicker.DatePicker.OnDateChangedListener;

public class MyOSDatePickerDialog extends Dialog implements OnDateChangedListener {
    
    private DatePicker mDatePicker;
    private Button mNegative;
    private Button mPositive;
    private MyOSDatePickerDialog mDatePickerDialog;
    
    private ViewOnClickListener mViewOnClickListener;
    private OnDateSetListener mCallBack;
    
    public MyOSDatePickerDialog(
            Context context, 
            OnDateSetListener listener,
            int year, int monthOfYear, int dayOfMonth) {
        super(context, R.style.Theme_MyOS_PopupMenu);
        
        setContentView(R.layout.myos_date_picker_dialog);
        
        mCallBack = listener;
        
        mDatePicker = (DatePicker) findViewById(R.id.datapicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
        
        mNegative = (Button) findViewById(R.id.negative);
        mPositive = (Button) findViewById(R.id.positive);
        mViewOnClickListener = new ViewOnClickListener();
        
        mNegative.setOnClickListener(mViewOnClickListener);
        mPositive.setOnClickListener(mViewOnClickListener);
        
        mDatePickerDialog = this;
    }
    
    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear,
            int dayOfMonth) {
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);
    }
    
    private class ViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == mNegative) {
                mDatePickerDialog.dismiss();
            } else if (v == mPositive) {
                mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                        mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
                mDatePickerDialog.dismiss();
            }
        }
    }
    
    public interface OnDateSetListener {
        void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }
}

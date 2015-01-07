package com.ape.onelogin.myos.ui;

import com.ape.onelogin.myos.widget.MyOSActionBar;
import com.ape.onelogin.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class SmartEditActivity extends Activity {
    
    private MyOSActionBar mActionBar;
    private EditText mEditText;
    
    private boolean mSaveEnable = false;
    
    private int mMode;
    
    public static final String EDIT_MODE                = "edit_mode";
    public static final String EDIT_RESULT              = "edit_result";
    public static final String INITIALIZE_DATA          = "initialize_data";
    
    public static final int MODE_NICKNAME_EDIT          = 1000;
    public static final int MODE_MOBILE_EDIT            = 1001;
    public static final int MODE_ADDRESS_EDIT           = 1002;
    public static final int MODE_EMAIL_EDIT             = 1003;
    public static final int MODE_EDIT_ALL               = 1004;
    
    public static final int REQ_NICKNAME_MODE           = 2000;
    public static final int REQ_MOBILE_MODE             = 2001;
    public static final int REQ_ADDRESS_MODE            = 2002;
    public static final int REQ_EMAIL_MODE              = 2003;
    public static final int REQ_MODE_ALL                = 2004;
    
    private TextWatcher mTextWatcher = new TextWatcher() {
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mSaveEnable = true;
            mActionBar.setSimpleEnable(mSaveEnable);
        }
        
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // TODO Auto-generated method stub
            
        }
        
        @Override
        public void afterTextChanged(Editable s) {
            // TODO Auto-generated method stub
            
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smart_edit_activity);
        
        mActionBar = (MyOSActionBar) findViewById(R.id.actionbar);
        mActionBar.setHomeOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mActionBar.setSimpleEnable(mSaveEnable);
        
        mEditText = (EditText) findViewById(R.id.edit);
        mEditText.addTextChangedListener(mTextWatcher);
        
        mMode = getIntent().getIntExtra(EDIT_MODE, MODE_EDIT_ALL);
        String data = getIntent().getStringExtra(INITIALIZE_DATA);
        mEditText.setText(data);
        changeMode(mMode);
    }
    
    private void changeMode(int mode) {
        if (mode >= MODE_NICKNAME_EDIT && mode < MODE_EDIT_ALL) {
            String title = null;
            int maxLength = 1024;
            int inputType = 0;
            switch (mode) {
                case MODE_NICKNAME_EDIT:
                    title = getString(R.string.ex_smart_edit_nickname);
                    maxLength = getResources().getInteger(R.integer.max_nickname_length);
                    break;
                case MODE_MOBILE_EDIT:
                    title = getString(R.string.ex_smart_edit_mobile);
                    maxLength = getResources().getInteger(R.integer.max_mobile_length);
                    inputType = InputType.TYPE_CLASS_PHONE;
                    break;
                case MODE_ADDRESS_EDIT:
                    title = getString(R.string.ex_smart_edit_address);
                    maxLength = getResources().getInteger(R.integer.max_address_length);
                    break;
                case MODE_EMAIL_EDIT:
                    title = getString(R.string.ex_smart_edit_email);
                    maxLength = getResources().getInteger(R.integer.max_email_length);
                    inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
                    break;
            }
            
            mActionBar.setActionTitle(title);
            
            mEditText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
            if (inputType > 0) {
                mEditText.setInputType(inputType);
            }
        }
    }
}

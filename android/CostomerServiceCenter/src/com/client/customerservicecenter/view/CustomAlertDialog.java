package com.client.customerservicecenter.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.client.customerservicecenter.R;

public class CustomAlertDialog extends Dialog {
    private TextView mTitleView;
    private TextView mMessageView;
    private Button mNegativeButton;
    private Button mPositiveButton;
    
    private String mTitle;
    private String mMessage;
    private String mNegativeName;
    private String mPositiveName;
    private View.OnClickListener mNegativeListener;
    private View.OnClickListener mPositiveListener;
    
    private CustomAlertDialog mAlertDialog;
    
    public CustomAlertDialog(Context context) {
        super(context, R.style.Theme_MyDialog);
        mAlertDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_alert_dialog);
        mTitleView = (TextView) findViewById(R.id.title);
        mMessageView = (TextView) findViewById(R.id.message);
        mNegativeButton = (Button) findViewById(R.id.negative);
        mPositiveButton = (Button) findViewById(R.id.positive);
        
        mTitleView.setText(mTitle);
        mMessageView.setText(mMessage);
        
        mNegativeButton.setText(mNegativeName);
        if (mNegativeListener == null) {
            mNegativeButton.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View v) {
                    mAlertDialog.dismiss();
                }
            });
        } else {
            mNegativeButton.setOnClickListener(mNegativeListener);
        }
        
        mPositiveButton.setText(mPositiveName);
        mPositiveButton.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mPositiveListener.onClick(v);
                if (mAlertDialog != null && mAlertDialog.isShowing()) {
                    mAlertDialog.dismiss();
                }
            }
        });
    }
    
    public CustomAlertDialog setTitle(String title) {
        mTitle = title;
        return mAlertDialog;
    }
    
    public CustomAlertDialog setMessage(String message) {
        mMessage = message;
        return mAlertDialog;
    }
    
    public CustomAlertDialog setNegativeButton(String name, View.OnClickListener l) {
        mNegativeName = name;
        mNegativeListener = l;
        return mAlertDialog;
    }
    
    public CustomAlertDialog setPositiveButton(String name, View.OnClickListener l) {
        mPositiveName = name;
        mPositiveListener = l;
        return mAlertDialog;
    }
}

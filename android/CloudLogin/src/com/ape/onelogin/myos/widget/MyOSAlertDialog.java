package com.ape.onelogin.myos.widget;

import com.ape.onelogin.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MyOSAlertDialog extends Dialog {
    
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
    
    private MyOSAlertDialog mAlertDialog;
    
    public MyOSAlertDialog(Context context) {
        super(context, R.style.Theme_MyOS_PopupMenu);
        mAlertDialog = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myos_alert_dialog);
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
    
    public MyOSAlertDialog setTitle(String title) {
        mTitle = title;
        return mAlertDialog;
    }
    
    public MyOSAlertDialog setMessage(String message) {
        mMessage = message;
        return mAlertDialog;
    }
    
    public MyOSAlertDialog setNegativeButton(String name, View.OnClickListener l) {
        mNegativeName = name;
        mNegativeListener = l;
        return mAlertDialog;
    }
    
    public MyOSAlertDialog setPositiveButton(String name, View.OnClickListener l) {
        mPositiveName = name;
        mPositiveListener = l;
        return mAlertDialog;
    }
}

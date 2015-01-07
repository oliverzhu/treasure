package com.ape.onelogin.myos.widget;

import com.ape.onelogin.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class ProgressDialog extends Dialog {
    
    private TextView mMessageView;
    private ImageView mProgressImageView;
    private AnimationDrawable mAnimationDrawable;
    
    private String mMessage;
    private int mId;
    
    public ProgressDialog(Context context) {
        super(context, R.style.Theme_MyOS_AlertDialog);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);
        
        mMessageView = (TextView) findViewById(R.id.message);
        mMessageView.setText(mMessage);
        
        mProgressImageView = (ImageView) findViewById(R.id.anim);
        mAnimationDrawable = (AnimationDrawable) mProgressImageView.getDrawable();
        mAnimationDrawable.start();
    }
    
    public void setMessage(String message) {
        mMessage = message;
    }
    
    public void setProgressId(int id) {
        mId = id;
    }
    
    public int getProgressId() {
        return mId;
    }
    
    @Override
    public void dismiss() {
        mAnimationDrawable.stop();
        super.dismiss();
    }
}

package com.home.owntextView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class MTextView extends View {
	private String text;
	
	public MTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setText(String text) {
		this.text = text;
	}
}

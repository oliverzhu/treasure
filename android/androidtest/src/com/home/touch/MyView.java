package com.home.touch;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

public class MyView extends Button {
	private static final String TAG = "MyView";

	public MyView(Context context) {
		super(context);
	}

	public MyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 返回true表示愿意接受后续事件进行消费
	 * 返回false表示不接受后续事件，交由它的父节点的onTouchEvent处理完整的事件
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.i(TAG, "onTouchEvent default return = " + super.onTouchEvent(event));
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "onTouchEvent ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
			
			break;
		case MotionEvent.ACTION_UP:
			Log.i(TAG, "onTouchEvent ACTION_UP");
			break;
		default:
			break;
		}
		return true;
	}
	
	

}

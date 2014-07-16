package com.home.touch;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MyLayout extends FrameLayout {
	private static final String TAG = "MyLaout";
	public MyLayout(Context context) {
		super(context);
	}

	public MyLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * 返回false表示事件继续往孩子节点传递
	 * 返回true表示只有本类的onTouchEvent来处理事件
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		Log.i(TAG, "onInterceptTouchEvent default return = " + super.onInterceptTouchEvent(ev));
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "onTouchEvent ACTION_DOWN");
			break;
		case MotionEvent.ACTION_MOVE:
			Log.i(TAG, "onTouchEvent ACTION_MOVE");
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

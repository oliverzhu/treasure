package com.home.touch;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

public class MyView extends Button {
	private static final String TAG = "MyView";
	private final int[] mViewLocationResult = new int[2];
	private final Rect mRect = new Rect();

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
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			Log.i(TAG, "onTouchEvent ACTION_DOWN");
			getLocationOnScreen(mViewLocationResult);
            final int viewLeft = mViewLocationResult[0], viewTop = mViewLocationResult[1];
            mRect.set(viewLeft, viewTop, viewLeft + getWidth(), viewTop + getHeight());

             Log.d(TAG, "isViewBeingDragged. View Rect: " + mRect.toString());
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

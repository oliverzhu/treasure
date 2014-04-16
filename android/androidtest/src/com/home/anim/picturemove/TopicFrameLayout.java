package com.home.anim.picturemove;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AbsoluteLayout;
import android.widget.Toast;

public class TopicFrameLayout extends AbsoluteLayout implements OnGestureListener{
	private Context context;
	
	private static final int FLING_MIN_DISTANCE = 140;
    private static final int FLING_MIN_VELOCITY = 100;
    
    private float mLastMotionX;
	
	private float mLastMotionY;
	
	private GestureDetector mGestureDetector;
	
	private Animation fadeOutAnim;
	
	public TopicFrameLayout(Context context) {
		super(context);
		this.context = context;
	}
	
	public TopicFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.context = context;
		
		mGestureDetector = new GestureDetector(this);
		
		fadeOutAnim = AnimationUtils.loadAnimation(context,
                android.R.anim.fade_out);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
//		final int action = event.getAction();
//		
//		final int x = (int) event.getX();
//		
//		final int y = (int) event.getY();
//		
//		switch (action) {
//		case MotionEvent.ACTION_DOWN:
//			mLastMotionX = x;
//			mLastMotionY = y;
//			break;
//		case MotionEvent.ACTION_MOVE:
//			break;
//		case MotionEvent.ACTION_UP:
//			if(x == mLastMotionX && y == mLastMotionY)
//			{
//				for(int i = 0;i < getChildCount();i++)
//				{
//					View child = getChildAt(i);
//					if(containPoint(child,event.getX(),event.getY()))
//					{
//						child.onTouchEvent(event);
//					}
//				}
//			}
//			
//			break;
//
//		default:
//			break;
//		}
		return super.dispatchTouchEvent(event);
	}
	
	/**
	 * return true:事件分发给ViewGroup  false:事件分发给childview
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		
		Toast.makeText(context, "onInterceptTouchEvent" + action, Toast.LENGTH_SHORT).show();
		if(action == MotionEvent.ACTION_MOVE)
		{
			Toast.makeText(context, "onInterceptTouchEvent:action" + action, Toast.LENGTH_SHORT).show();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}
	
	/**
	 * 判断动画完成之后view所响应的区域
	 * @param view
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean containPoint(View view, float x, float y) {
        Transformation trans = new Transformation();
        Animation anim = view.getAnimation();
        anim.getTransformation(view.getDrawingTime(), trans);
        Matrix matrix = trans.getMatrix();
        
        int dx = view.getLeft();
        int dy = view.getTop();
        
        x -= dx;
        y -= dy;
        
        // Invert Matrix
        Matrix mat = new Matrix();
        if (matrix.invert(mat)) {
            float[] pointsSrc = new float[] { x, y };
            float[] pointsEnd = new float[] { 0, 0 };
            
            // Get the point in inverted matrix.
            mat.mapPoints(pointsEnd, pointsSrc);
            
            // Offset the point because we translate matrix which dx and dy before.
            x = pointsEnd[0] + dx;
            y = pointsEnd[1] + dy;
        }
        
        Rect rect = new Rect();
        view.getHitRect(rect);
        return rect.contains((int) x, (int) y);
    }


	@Override
	public boolean onDown(MotionEvent e) {
		Toast.makeText(context, "onDown", Toast.LENGTH_SHORT).show();
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		Toast.makeText(context, "onFling", Toast.LENGTH_SHORT).show();
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE    
              && Math.abs(velocityX) > FLING_MIN_VELOCITY) {    
         // Fling left
			for(int i = 0;i < getChildCount();i++)
			{
				View child = getChildAt(i);
				child.startAnimation(fadeOutAnim);
			}
			
			removeAllViews();
          Toast.makeText(context, "Fling Left", Toast.LENGTH_SHORT).show();    
      } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE    
              && Math.abs(velocityX) > FLING_MIN_VELOCITY) {

          // Fling right 
          Toast.makeText(context, "Fling Right", Toast.LENGTH_SHORT).show();
      }  
     
     return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}

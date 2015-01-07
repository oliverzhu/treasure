package com.client.customerservicecenter.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**'
 * 避免横向滑动出现布局错误
 * @author jianwen.zhu
 * @since 2014/10/27
 */
public class CustomScrollView extends ScrollView {
	private Context mContext;
	private GestureDetector mGestureDetector;

	public CustomScrollView(Context context, AttributeSet attrs) {   
        super(context, attrs); 
        mContext = context;
        mGestureDetector = new GestureDetector(mContext,new YScrollDetector());
        setFadingEdgeLength(0);   
    }   
    
    @Override  
    public boolean onInterceptTouchEvent(MotionEvent ev) {   
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }   
    
    // Return false if we're scrolling in the x direction     
    class YScrollDetector extends SimpleOnGestureListener {   
        @Override  
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {   
            if(Math.abs(distanceY) > Math.abs(distanceX)) {   
                return true;   
            }   
            return false;   
        }   
    } 

}

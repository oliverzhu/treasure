package com.home.anim.picturemove;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 
 * @author Oliverzhu
 * 2012/8/14
 *
 */
public class AnimationImageView extends ImageView {
	private Context context;
	
	/** 出入场动画 */
	private Animation enterAnimation,exitAnimation;

	public AnimationImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public AnimationImageView(Context context) {
		super(context);
		this.context = context;
	}
	
	public Animation getEnterAnimation() {
		return enterAnimation;
	}

	public void setEnterAnimation(Animation enterAnimation) {
		this.enterAnimation = enterAnimation;
		enterAnimation.setDuration(1500);
		enterAnimation.setRepeatMode(Animation.RESTART);
		enterAnimation.setFillEnabled(true);
		enterAnimation.setFillAfter(true);
		enterAnimation.setRepeatCount(0);
		
		enterAnimation.setInterpolator(AnimationUtils.loadInterpolator(context,
                android.R.anim.overshoot_interpolator));
	}

	public Animation getExitAnimation() {
		return exitAnimation;
	}

	public void setExitAnimation(Animation exitAnimation) {
		this.exitAnimation = exitAnimation;
	}
	
	/**
	 * return false:屏蔽后续事件
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		Toast.makeText(context, "child-dispatchTouchEvent", Toast.LENGTH_SHORT).show();
		return super.dispatchTouchEvent(event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		String tag = (String) this.getTag();
		ViewGroup parent = (ViewGroup) this.getParent();
		parent.bringChildToFront(this);
		Toast.makeText(context, tag, Toast.LENGTH_SHORT).show();
		return true;
	}
}

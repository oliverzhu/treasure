package com.home.anim.animatorset;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.home.R;

public class AnimatorSetDemoActivity extends Activity{
	private View mHeaderView;
	private ViewGroup mContentLayout;
	
	private Button button;
	
	private long mAnimationDuration = 200;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_animatorset);
		
		mHeaderView = findViewById(R.id.headerView);
		mContentLayout = (ViewGroup) findViewById(R.id.ptr_content);
		
		button = (Button) findViewById(R.id.button);
		
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(mHeaderView.getVisibility() == View.VISIBLE)
				{
					hideHeaderView();
				}else if(mHeaderView.getVisibility() == View.GONE)
				{
					showHeaderView();
				}
			}
		});
	}
	
	public boolean showHeaderView() {
        final boolean changeVis = mHeaderView.getVisibility() != View.VISIBLE;

        if (changeVis) {
            mHeaderView.setVisibility(View.VISIBLE);
            AnimatorSet animSet = new AnimatorSet();
            ObjectAnimator transAnim = ObjectAnimator.ofFloat(mContentLayout, "translationY",
                    -mContentLayout.getHeight(), 0f);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mHeaderView, "alpha", 0f, 1f);
            animSet.playTogether(transAnim, alphaAnim);
            animSet.setDuration(mAnimationDuration);
            animSet.start();
        }

        return changeVis;
    }
	
	public boolean hideHeaderView() {
        final boolean changeVis = mHeaderView.getVisibility() != View.GONE;

        if (changeVis) {
            Animator animator;
            if (mContentLayout.getAlpha() >= 0.5f) {
                // If the content layout is showing, translate and fade out
                animator = new AnimatorSet();
                ObjectAnimator transAnim = ObjectAnimator.ofFloat(mContentLayout, "translationY",
                        0f, -mContentLayout.getHeight());
                ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(mHeaderView, "alpha", 1f, 0f);
                ((AnimatorSet) animator).playTogether(transAnim, alphaAnim);
            } else {
                // If the content layout isn't showing (minimized), just fade out
                animator = ObjectAnimator.ofFloat(mHeaderView, "alpha", 1f, 0f);
            }
            animator.setDuration(mAnimationDuration);
            animator.addListener(new HideAnimationCallback());
            animator.start();
        }

        return changeVis;
    }
	
	class HideAnimationCallback extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(Animator animation) {
            View headerView = mHeaderView;
            if (headerView != null) {
                headerView.setVisibility(View.GONE);
            }
        }
    }
}

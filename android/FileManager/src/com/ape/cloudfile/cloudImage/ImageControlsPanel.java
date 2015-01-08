package com.ape.cloudfile.cloudImage;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

public class ImageControlsPanel
{
    private static final int HIDE_TIME_INTERVAL = 5000;
    private Activity mContext;
    private Runnable mHideRunnable;

    private ViewGroup mTopContainer;
    private ViewGroup mBottomContainer;
    private static final int CONTAINER_ANIM_DURATION_MS = 200;

    private boolean mContainerVisible = false;

    public ImageControlsPanel(Activity context, ViewGroup topContrainer, ViewGroup bottomContrainer)
    {
        mContext = context;
        mTopContainer = topContrainer;
        mBottomContainer = bottomContrainer;

        mHideRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                mContainerVisible = false;
                hide();
            }
        };

    }

    private void hide()
    {
        TranslateAnimation moveUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1);
        TranslateAnimation moveDown = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        moveUp.setDuration(CONTAINER_ANIM_DURATION_MS);
        moveDown.setDuration(CONTAINER_ANIM_DURATION_MS);
        moveDown.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                
            }
            
            @Override
            public void onAnimationRepeat(Animation animation)
            {
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation)
            {
                mTopContainer.setVisibility(View.GONE);
                mBottomContainer.setVisibility(View.GONE);
            }
        });

        mTopContainer.clearAnimation();
        mTopContainer.startAnimation(moveUp);
        mBottomContainer.clearAnimation();
        mBottomContainer.startAnimation(moveDown);
        setLightsOutMode(true);
    }

    private void show()
    {
        TranslateAnimation moveDown = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0);
        TranslateAnimation moveUp = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
        moveUp.setAnimationListener(new AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                mTopContainer.setVisibility(View.VISIBLE);
                mBottomContainer.setVisibility(View.VISIBLE);
            }
            
            @Override
            public void onAnimationRepeat(Animation animation)
            {
                
            }
            
            @Override
            public void onAnimationEnd(Animation animation)
            {
                
            }
        });

        mTopContainer.clearAnimation();
        mTopContainer.startAnimation(moveDown);
        mBottomContainer.clearAnimation();
        mBottomContainer.startAnimation(moveUp);
        setLightsOutMode(false);
    }

    public void refresh()
    {
        mContainerVisible = !mContainerVisible;
        if (mContainerVisible)
        {
            show();
            mBottomContainer.postDelayed(mHideRunnable, HIDE_TIME_INTERVAL);
        } else
        {
            hide();
            mBottomContainer.removeCallbacks(mHideRunnable);
        }
    }
    
    public boolean resetHideTime()
    {
        if (mContainerVisible)
        {
            mBottomContainer.removeCallbacks(mHideRunnable);
            mBottomContainer.postDelayed(mHideRunnable, HIDE_TIME_INTERVAL);
        }

        return mContainerVisible;
    }

    public void setLightsOutMode(boolean enabled)
    {
//        if (enabled)
//        {
//            mContext.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN); 
//        } else
//        {
//            mContext.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN, 
//                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); 
//
//        }
    }
}

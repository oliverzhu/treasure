package com.ape.filemanager.myosUI;

import com.ape.filemanager.R;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class MyOSActionMode extends ActionMode
{
    private Context mContext;
    private ActionMode.Callback mModeCallback;
    private View mCustomView;

    public MyOSActionMode(Context context, ActionMode.Callback callback)
    {
        mContext = context;
        mModeCallback = callback;
    }

    @Override
    public void setTitle(CharSequence title)
    {

    }

    @Override
    public void setTitle(int resId)
    {

    }

    @Override
    public void setSubtitle(CharSequence subtitle)
    {

    }

    @Override
    public void setSubtitle(int resId)
    {

    }

    @Override
    public void setCustomView(View view)
    {
        if (view != null)
        {
            mCustomView = view;
            Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.move_in_from_top);
            mCustomView.setVisibility(View.VISIBLE);
            mCustomView.startAnimation(anim);
        }
    }

    public void start()
    {
        if (mModeCallback != null)
        {
            mModeCallback.onCreateActionMode(this, null);
            mModeCallback.onPrepareActionMode(this, null);
        }
    }

    @Override
    public void invalidate()
    {
        if (mModeCallback != null)
        {
            mModeCallback.onPrepareActionMode(this, null);
        }
    }

    @Override
    public void finish()
    {
        if (mCustomView != null)
        {
            Animation anim = AnimationUtils.loadAnimation(mContext, R.anim.move_out_to_top);
            anim.setAnimationListener(new AnimationListener()
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
                    mCustomView.setVisibility(View.GONE);
                }
            });
            mCustomView.startAnimation(anim);
        }
        if (mModeCallback != null)
        {
            mModeCallback.onDestroyActionMode(this);
        }
    }

    @Override
    public Menu getMenu()
    {
        return null;
    }

    @Override
    public CharSequence getTitle()
    {
        return null;
    }

    @Override
    public CharSequence getSubtitle()
    {
        return null;
    }

    @Override
    public View getCustomView()
    {
        return null;
    }

    @Override
    public MenuInflater getMenuInflater()
    {
        return null;
    }

}

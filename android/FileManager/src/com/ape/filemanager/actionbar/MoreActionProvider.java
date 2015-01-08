package com.ape.filemanager.actionbar;

import android.content.Context;
import android.view.ActionProvider;
import android.view.SubMenu;
import android.view.View;

public class MoreActionProvider extends ActionProvider
{
    public interface MoreActionListener
    {
        public void onPrepareSubMenu(SubMenu subMenu);
        public boolean hasSubMenu();
    }

    private MoreActionListener mListener;

    public MoreActionProvider(Context context)
    {
        super(context);
    }

    public MoreActionProvider(Context context, MoreActionListener listener)
    {
        super(context);
        mListener = listener;
    }

    @Override
    public View onCreateActionView()
    {
        return null;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu)
    {
        if (mListener != null)
        {
            mListener.onPrepareSubMenu(subMenu);
            
        } else
        {
            super.onPrepareSubMenu(subMenu);
        }
    }

    @Override
    public boolean hasSubMenu()
    {
        if (mListener != null)
        {
            return mListener.hasSubMenu();
        } else
        {
            return super.hasSubMenu();
        }
    }
}

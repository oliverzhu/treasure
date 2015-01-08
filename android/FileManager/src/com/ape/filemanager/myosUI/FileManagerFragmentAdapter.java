package com.ape.filemanager.myosUI;

import java.util.ArrayList;

import com.ape.cloudfile.widget.vpi.IconPagerAdapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.support.v13.app.FragmentPagerAdapter;

public class FileManagerFragmentAdapter extends FragmentPagerAdapter
    implements IconPagerAdapter
{
    private Context mContext;
    private ArrayList<Fragment> mFragments;
    private ArrayList<String> mTitles;
    private ArrayList<Integer> mIconsId;
    
    private int mCount;

    public FileManagerFragmentAdapter(FragmentManager fm, Context context)
    {
        super(fm);
        mContext = context;

        mTitles = new ArrayList<String>();
        mIconsId = new ArrayList<Integer>();
        mFragments = new ArrayList<Fragment>();
    }

    @Override
    public Fragment getItem(int arg0)
    {
        return mFragments.get(arg0 % mCount);
    }

    @Override
    public int getCount()
    {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        return mTitles.get(position % mCount);
    }

    @Override
    public int getIconResId(int index)
    {
        return mIconsId.get(index % mCount);
    }

    public void addFragment(Fragment f, String title, int iconId)
    {
        mFragments.add(f);
        mTitles.add(title);
        mIconsId.add(iconId);
        mCount = mFragments.size();
    }
    
    public void clearFragment()
    {
        mFragments.clear();
        mTitles.clear();
        mIconsId.clear();
        mCount = 0;
    }
}

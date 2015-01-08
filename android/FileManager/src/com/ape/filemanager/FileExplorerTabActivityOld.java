/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ape.filemanager;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.ActionMode.Callback;

import com.ape.cloudfile.CloudFileActivity;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.filemanager.search.FileManagerSearchActivity;

public class FileExplorerTabActivityOld extends Activity implements IActionTabListener {
    private static final String INSTANCESTATE_TAB = "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES = 2;
    private static final String TAG = "FileExplorerTabActivity";

    ViewPager mViewPager;
    TabsAdapter mTabsAdapter;
    ActionMode mActionMode;
    ActionMode.Callback mActionCallback;
    MountPointManager mMountPointManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_pager);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);

        mIsMyOsOptionMenuStyle = getResources().getBoolean(R.bool.myos_option_menu_style);

        final ActionBar bar = getActionBar();
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        bar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);

        mTabsAdapter = new TabsAdapter(this, mViewPager);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_category),
                FileCategoryActivityMyOS.class, null);
        mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_sd),
                FileViewActivity.class, null);
        if (getResources().getBoolean(R.bool.have_cloud_file))
        {
            mTabsAdapter.addTab(bar.newTab().setText(R.string.cloud_storage),
                    CloudFileActivity.class, null);
        } else
        {
            mTabsAdapter.addTab(bar.newTab().setText(R.string.tab_remote),
                    ServerControlActivity.class, null);
        }

        int tabIndex;
        Intent intent = getIntent();
        String action = intent.getAction();
        if (getIntent().getData() != null)
        {
            tabIndex = Util.SDCARD_TAB_INDEX;
        } else if (!TextUtils.isEmpty(action)) {
            if (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)
                        || action.equals("com.mediatek.filemanager.ADD_FILE")) {
                tabIndex = Util.CATEGORY_TAB_INDEX;
            } else if (action.equals(CloudFileUtil.CLOUD_STORAGE_ACTION)) {
                tabIndex = Util.REMOTE_TAB_INDEX;
            } else
            {
                tabIndex = Util.CATEGORY_TAB_INDEX;
            }
        } else{
            tabIndex = Util.CATEGORY_TAB_INDEX; //For market require.
//          tabIndex = PreferenceManager.getDefaultSharedPreferences(this)
//                  .getInt(INSTANCESTATE_TAB, Util.CATEGORY_TAB_INDEX);
        }
        bar.setSelectedNavigationItem(tabIndex);
        
        mMountPointManager = MountPointManager.getInstance();
        mMountPointManager.init(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(INSTANCESTATE_TAB, getActionBar().getSelectedNavigationIndex());
        editor.commit();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (getActionBar().getSelectedNavigationIndex() == Util.CATEGORY_TAB_INDEX) {
            FileCategoryActivityMyOS categoryFragement =(FileCategoryActivityMyOS) mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) {
                reInstantiateCategoryTab();
            } else {
                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void reInstantiateCategoryTab() {
        mTabsAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX,
                mTabsAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mTabsAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mTabsAdapter
                .getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    public interface IBackPressedListener {
        /**
         * 处理back事件。
         * @return True: 表示已经处理; False: 没有处理，让基类处理。
         */
        boolean onBack();
    }
    
    @Override
    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback)
    {
        mActionCallback = callback;
        return super.startActionMode(callback);
    }

    @Override
    public ActionMode getActionMode() {
        return mActionMode;
    }

    @Override
    public Callback getActionModeCallback()
    {
        return mActionCallback;
    }

    @Override
    public void onTabChanged() {
        Fragment fragment = getFragment(mViewPager.getCurrentItem());
        
        if (fragment instanceof CloudFileActivity) {
            ((CloudFileActivity)fragment).onFragementResume();
        }
    }

    @Override
    public View getActionModeCustomView()
    {
        return null;
    }

    public Fragment getFragment(int tabIndex) {
        return mTabsAdapter.getItem(tabIndex);
    }

    /**
     * This is a helper class that implements the management of tabs and all
     * details of connecting a ViewPager with associated TabHost.  It relies on a
     * trick.  Normally a tab host has a simple API for supplying a View or
     * Intent that each tab will show.  This is not sufficient for switching
     * between pages.  So instead we make the content part of the tab host
     * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
     * view to show as the tab content.  It listens to changes in tabs, and takes
     * care of switch to the correct paged in the ViewPager whenever the selected
     * tab changes.
     */
    public static class TabsAdapter extends FragmentPagerAdapter
            implements ActionBar.TabListener, ViewPager.OnPageChangeListener {
        private final Context mContext;
        private final ActionBar mActionBar;
        private final ViewPager mViewPager;
        private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

        static final class TabInfo {
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(Class<?> _class, Bundle _args) {
                clss = _class;
                args = _args;
            }
        }

        public TabsAdapter(Activity activity, ViewPager pager) {
            super(activity.getFragmentManager());
            mContext = activity;
            mActionBar = activity.getActionBar();
            mViewPager = pager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
            TabInfo info = new TabInfo(clss, args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabs.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabs.get(position);
            if (info.fragment == null) {
                info.fragment = Fragment.instantiate(mContext, info.clss.getName(), info.args);
            }
            return info.fragment;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tag = tab.getTag();
            for (int i=0; i<mTabs.size(); i++) {
                if (mTabs.get(i) == tag) {
                    mViewPager.setCurrentItem(i);
                }
            }

            //if(!tab.getText().equals(mContext.getString(R.string.tab_sd))) {
            ActionMode actionMode = ((IActionTabListener) mContext).getActionMode();
            if (actionMode != null) {
                actionMode.finish();
            }
            //}
            
            ((IActionTabListener) mContext).onTabChanged();
        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }
    }
    
    public void startSearchActivity(String path)
    {
        Intent intent = new Intent();
        intent.setClass(this, FileManagerSearchActivity.class);
        intent.putExtra(FileManagerSearchActivity.CURRENT_PATH, path);
        intent.putExtra(FileManagerSearchActivity.IS_FROM_FILEMANAGER, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public static boolean mIsFromSearch = false;
    public static String mSearchPath = null;
    private void setDefaultTabView()
    {
        if (mIsFromSearch && mSearchPath != null)
        {
            ActionBar bar = getActionBar();

            bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    
            Log.i(TAG, "setDefaultTabIndex, fromSearch:" + mIsFromSearch + ", path: " + mSearchPath);
            FileViewActivity fileView = (FileViewActivity) getFragment(Util.SDCARD_TAB_INDEX);
            if (fileView.getContext() != null)
            {
                fileView.setPath(mSearchPath);
            }
            mIsFromSearch = false;
            mSearchPath = null;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        
        setDefaultTabView();
        Log.i(TAG, "onResume");
    }
    
    public MountPointManager getMountManager()
    {
        return mMountPointManager;
    }

    public void sendFilesToCloud(ArrayList<FileInfo> filesList)
    {
        CloudFileActivity cloudFile = (CloudFileActivity) getFragment(Util.REMOTE_TAB_INDEX);

        if (cloudFile.selectFolderToUploadFiles(filesList))
        {
            switchToPage(Util.REMOTE_TAB_INDEX);
        }
    }
    
    public void switchToPage(int whichTab)
    {
        ActionBar bar = getActionBar();
        if (bar != null && whichTab < bar.getTabCount())
        {
            bar.setSelectedNavigationItem(whichTab);
        }
    }
    
    private boolean mIsMyOsOptionMenuStyle = false;
    public boolean isMyOsOptionMenuStyle()
    {
        return mIsMyOsOptionMenuStyle;
    }
}

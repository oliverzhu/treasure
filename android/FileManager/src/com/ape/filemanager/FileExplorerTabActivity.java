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

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.ActionMode.Callback;

import com.ape.cloudfile.CloudFileActivity;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.widget.vpi.TabPageIndicator;
import com.ape.filemanager.myosUI.FileManagerFragmentAdapter;
import com.ape.filemanager.myosUI.MyOSActionMode;
import com.ape.filemanager.search.FileManagerSearchActivity;
import com.common.upgrade.core.CheckNewVersionListener;
import com.common.upgrade.core.UpgradeManager;

@SuppressLint("HandlerLeak")
public class FileExplorerTabActivity extends Activity implements IActionTabListener {
    private static final String INSTANCESTATE_TAB = "tab";
    private static final int DEFAULT_OFFSCREEN_PAGES = 3;
    private static final String TAG = "FileExplorerTabActivity";

    private ViewPager mViewPager;
    private TabPageIndicator mTagPagerIndicator;
    private FileManagerFragmentAdapter mFragmentAdapter;
    
    private String[] mTabTitles;
    private int [] mTabIcons;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionCallback;
    private MountPointManager mMountPointManager;

    private UpgradeManager upgradeManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.hide();

        setContentView(R.layout.myos_activity_main);

        mMountPointManager = MountPointManager.getInstance();
        mMountPointManager.init(this);

        initTabTitles();
        mFragmentAdapter = new FileManagerFragmentAdapter(getFragmentManager(), this);
        restoreOrCreateFragment(savedInstanceState);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);

        mTagPagerIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mTagPagerIndicator.setViewPager(mViewPager);
        mTagPagerIndicator.setOnPageChangeListener(mPageChangeListener);
        
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
        mTagPagerIndicator.setCurrentItem(tabIndex);

        upgradeManger = UpgradeManager.newInstance(this, getApplicationInfo().packageName, 
                getString(R.string.app_name));
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_VERSION, 3000);
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrollStateChanged(int arg0)
        {
            MyLog.d(TAG, "onPageScrollStateChanged, arg0:" + arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2)
        {
        }

        @Override
        public void onPageSelected(int arg0)
        {
            MyLog.d(TAG, "onPageSelected, arg0:" + arg0);
            if (mActionMode != null)
            {
                mActionMode.finish();
            }
        }
    };

    private void initTabTitles()
    {
        mTabIcons = new int[Util.FM_MAX_TAB_COUNT];
        mTabIcons[Util.CATEGORY_TAB_INDEX] = 0;
        mTabIcons[Util.SDCARD_TAB_INDEX] = 0;
        mTabIcons[Util.REMOTE_TAB_INDEX] = 0;

        mTabTitles = new String[Util.FM_MAX_TAB_COUNT];
        mTabTitles[Util.CATEGORY_TAB_INDEX] = getString(R.string.tab_category);
        mTabTitles[Util.SDCARD_TAB_INDEX] = getString(R.string.tab_sd);
        if (getResources().getBoolean(R.bool.have_cloud_file))
        {
            mTabTitles[Util.REMOTE_TAB_INDEX] = getString(R.string.cloud_storage);
        } else
        {
            mTabTitles[Util.REMOTE_TAB_INDEX] = getString(R.string.tab_remote);
        }
    }

    private void restoreOrCreateFragment(Bundle savedInstanceState)
    {
        Fragment tmpFragment = null;
        Fragment categoryFragment = null;
        Fragment sdCardFragment = null;
        Fragment remoteFragment = null;
        FragmentManager fm = getFragmentManager();

        if (savedInstanceState != null)
        {
            tmpFragment = fm.getFragment(savedInstanceState, Util.FRAGMENT_CATEGORY);
            if (tmpFragment != null)
                categoryFragment = tmpFragment;

            tmpFragment = fm.getFragment(savedInstanceState, Util.FRAGMENT_SDCARD);
            if (tmpFragment != null)
                sdCardFragment = tmpFragment;

            tmpFragment = fm.getFragment(savedInstanceState, Util.FRAGMENT_REMOTE);
            if (tmpFragment != null)
                remoteFragment = tmpFragment;
        }

        if (categoryFragment == null)
        {
            categoryFragment = new FileCategoryActivityMyOS();
        }

        if (sdCardFragment == null)
        {
            sdCardFragment = new FileViewActivity();
        }

        if (remoteFragment == null)
        {
            remoteFragment = new CloudFileActivity();
        }

        mFragmentAdapter.addFragment(categoryFragment, mTabTitles[Util.CATEGORY_TAB_INDEX], mTabIcons[Util.CATEGORY_TAB_INDEX]);
        mFragmentAdapter.addFragment(sdCardFragment, mTabTitles[Util.SDCARD_TAB_INDEX], mTabIcons[Util.SDCARD_TAB_INDEX]);
        mFragmentAdapter.addFragment(remoteFragment, mTabTitles[Util.REMOTE_TAB_INDEX], mTabIcons[Util.REMOTE_TAB_INDEX]);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putInt(INSTANCESTATE_TAB, mViewPager.getCurrentItem());
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        FragmentManager fm = getFragmentManager();
        fm.putFragment(outState, Util.FRAGMENT_CATEGORY, mFragmentAdapter.getItem(0));
        fm.putFragment(outState, Util.FRAGMENT_SDCARD, mFragmentAdapter.getItem(1));
        fm.putFragment(outState, Util.FRAGMENT_REMOTE, mFragmentAdapter.getItem(2));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mViewPager.getCurrentItem() == Util.CATEGORY_TAB_INDEX) {
            FileCategoryActivityMyOS categoryFragement =(FileCategoryActivityMyOS) mFragmentAdapter.getItem(Util.CATEGORY_TAB_INDEX);
            if (categoryFragement.isHomePage()) {
                reInstantiateCategoryTab();
            } else {
                categoryFragement.setConfigurationChanged(true);
            }
        }
        super.onConfigurationChanged(newConfig);
    }

    public void reInstantiateCategoryTab() {
        mFragmentAdapter.destroyItem(mViewPager, Util.CATEGORY_TAB_INDEX,
                mFragmentAdapter.getItem(Util.CATEGORY_TAB_INDEX));
        mFragmentAdapter.instantiateItem(mViewPager, Util.CATEGORY_TAB_INDEX);
    }

    @Override
    public void onBackPressed() {
        IBackPressedListener backPressedListener = (IBackPressedListener) mFragmentAdapter
                .getItem(mViewPager.getCurrentItem());
        if (!backPressedListener.onBack()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy()
    {
        mHandler.removeMessages(MSG_UPDATE_VERSION);
        super.onDestroy();
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
        MyOSActionMode mode = new MyOSActionMode(this, callback);
        mode.start();

        return mode;
        //return super.startActionMode(callback);
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
        return findViewById(R.id.myos_action_mode_view);
    }

    public Fragment getFragment(int tabIndex) {
        return mFragmentAdapter.getItem(tabIndex);
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
//            ActionBar bar = getActionBar();
//            bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
            if (mViewPager != null)
            {
                mTagPagerIndicator.setCurrentItem(Util.SDCARD_TAB_INDEX);
            }
    
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
//        ActionBar bar = getActionBar();
//        if (bar != null && whichTab < bar.getTabCount())
//        {
//            bar.setSelectedNavigationItem(whichTab);
//        }
        if (whichTab >= 0 && whichTab < Util.FM_MAX_TAB_COUNT)
            mTagPagerIndicator.setCurrentItem(whichTab);
    }
    
    public int getSelectedPageIndex()
    {
        return mViewPager.getCurrentItem();
    }
    
    public boolean isMyOsOptionMenuStyle()
    {
        return getResources().getBoolean(R.bool.myos_option_menu_style);
    }

    private static final int MSG_UPDATE_VERSION = 1;
    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case MSG_UPDATE_VERSION:
                    upgradeManger.askForNewVersionFlag(new CheckNewVersionListener() {

                        @Override
                        public void checkNewVersion(boolean result)
                        {
                            if (result)
                            {
                                upgradeManger.askForNewVersion();
                            }
                        }});
                    break;

                default:
                    break;
            }
        }
    };
}

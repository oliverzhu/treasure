package com.ape.cloudfile.transfer;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.widget.Toast;

import com.ape.cloudfile.CloudFileActivity.ErrorStatusType;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.transfer.TransferService.ServiceInforListener;
import com.ape.cloudfile.transfer.TransferService.TransferType;
import com.ape.cloudfile.widget.vpi.TabPageIndicator;
import com.ape.cloudfile.widget.vpi.TabPageIndicator.TabActivityListener;
import com.ape.filemanager.IActionTabListener;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.ape.filemanager.myosUI.FileManagerFragmentAdapter;
import com.ape.filemanager.myosUI.MyOSActionMode;

@SuppressLint("HandlerLeak")
public class TransferFilesActivity extends Activity implements IActionTabListener
{
    private static final String TAG = "TransferFilesActivity";
    private static final int MSG_USER_INFO_FINISH = 0;
    private static final int MSG_USER_INFO_ERROR = 1;

    private static final int DEFAULT_OFFSCREEN_PAGES = 2;

    private ViewPager mViewPager;
    private TabPageIndicator mTagPagerIndicator;
    private FileManagerFragmentAdapter mFragmentAdapter;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback;
    private String mUserKey;

    private String[] mTabTitles;
    private int [] mTabIcons;

    private boolean mNeedParseIntent;

    private TransferService mTransferService;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final ActionBar bar = getActionBar();
        if (bar != null)
            bar.hide();

        MyLog.i(TAG, "onCreate");
        if (!Util.isSDCardReady())
        {
            finish();
        }
        setContentView(R.layout.myos_activity_main);

        initTabTitles();
        mFragmentAdapter = new FileManagerFragmentAdapter(getFragmentManager(), this);
        restoreOrCreateFragment(savedInstanceState);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mFragmentAdapter);
        mViewPager.setOffscreenPageLimit(DEFAULT_OFFSCREEN_PAGES);

        mTagPagerIndicator = (TabPageIndicator) findViewById(R.id.indicator);
        mTagPagerIndicator.setViewPager(mViewPager);
        mTagPagerIndicator.setOnPageChangeListener(mPageChangeListener);
        mTagPagerIndicator.setTabActivityListerner(mTabActivityListener);

        if (getResources().getBoolean(R.bool.cloud_file_can_upload_file))
        {
            Intent intent = getIntent();
            int tabIndex = intent.getIntExtra(CloudFileUtil.FOCUS_TAB_INDEX, 0);
            mTagPagerIndicator.setCurrentItem(tabIndex);
        }

        mNeedParseIntent = (savedInstanceState == null);

        bindService();
        //startService();
    }

    private ViewPager.OnPageChangeListener mPageChangeListener = new ViewPager.OnPageChangeListener()
    {
        @Override
        public void onPageScrollStateChanged(int arg0)
        {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2)
        {
        }

        @Override
        public void onPageSelected(int arg0)
        {
            if (mActionMode != null)
            {
                mActionMode.finish();
            }
        }
    };

    private void initTabTitles()
    {
        mTabIcons = new int[CloudFileUtil.MAX_TRANSFER_TABS];
        mTabIcons[CloudFileUtil.UPLOAD_TAB_INDEX] = 0;
        mTabIcons[CloudFileUtil.DOWNLOAD_TAB_INDEX] = 0;

        mTabTitles = new String[CloudFileUtil.MAX_TRANSFER_TABS];
        mTabTitles[CloudFileUtil.UPLOAD_TAB_INDEX] = getString(R.string.upload_list);
        mTabTitles[CloudFileUtil.DOWNLOAD_TAB_INDEX] = getString(R.string.download_list);
    }

    private void restoreOrCreateFragment(Bundle savedInstanceState)
    {
        Fragment tmpFragment = null;
        Fragment uploadFragment = null;
        Fragment downloadFragment = null;
        FragmentManager fm = getFragmentManager();

        if (savedInstanceState != null)
        {
            tmpFragment = fm.getFragment(savedInstanceState, CloudFileUtil.FRAGMENT_UPLOAD);
            if (tmpFragment != null)
                uploadFragment = tmpFragment;
            
            tmpFragment = fm.getFragment(savedInstanceState, CloudFileUtil.FRAGMENT_DOWNLOAD);
            if (tmpFragment != null)
                downloadFragment = tmpFragment;
        }

        if (uploadFragment == null)
        {
            uploadFragment = new TransferUploadFragment();
        }
        if (downloadFragment == null)
        {
            downloadFragment = new TransferDownloadFragment();
        }

        if (getResources().getBoolean(R.bool.cloud_file_can_upload_file))
        {
        mFragmentAdapter.addFragment(uploadFragment, mTabTitles[CloudFileUtil.UPLOAD_TAB_INDEX], mTabIcons[CloudFileUtil.UPLOAD_TAB_INDEX]);
        }
        mFragmentAdapter.addFragment(downloadFragment, mTabTitles[CloudFileUtil.DOWNLOAD_TAB_INDEX], mTabIcons[CloudFileUtil.DOWNLOAD_TAB_INDEX]);
    }

    private void parseIntent()
    {
        Intent intent = getIntent();
        String destPath = intent.getStringExtra(CloudFileUtil.TRANSFER_DEST_PATH_TAG);
        ArrayList<String> srcFiles = intent.getStringArrayListExtra(CloudFileUtil.TRANSFER_SRC_FILES_TAG);
        
        TransferType type;
        if (intent.getIntExtra(CloudFileUtil.FOCUS_TAB_INDEX, 0) == CloudFileUtil.DOWNLOAD_TAB_INDEX)
        {
            type = TransferType.download;
        } else
        {
            type = TransferType.upload;
        }
        
        if (srcFiles != null && destPath != null)
        {
            boolean result = mTransferService.addTransferMission(srcFiles, destPath, type);
            String prompt = null;

            if (!result)
            {
                prompt = getString(R.string.insufficient_memory);
            } else if (type == TransferType.download)
            {
                prompt = getString(R.string.download_path_prompt, destPath);
            }
            
            if (prompt != null)
                Toast.makeText(getApplicationContext(), prompt, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        FragmentManager fm = getFragmentManager();
        if (getResources().getBoolean(R.bool.cloud_file_can_upload_file))
        {
            fm.putFragment(outState, CloudFileUtil.FRAGMENT_UPLOAD, mFragmentAdapter.getItem(CloudFileUtil.UPLOAD_TAB_INDEX));
            fm.putFragment(outState, CloudFileUtil.FRAGMENT_DOWNLOAD, mFragmentAdapter.getItem(CloudFileUtil.DOWNLOAD_TAB_INDEX));
        } else
        {
            fm.putFragment(outState, CloudFileUtil.FRAGMENT_DOWNLOAD, mFragmentAdapter.getItem(0));
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        MyLog.i(TAG, "onStart");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        MyLog.i(TAG, "onResume");
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        MyLog.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        MyLog.i(TAG, "onDestroy");
        
        if (mTransferService != null)
        {
            mTransferService.unregisterTransferUiListener();
            mTransferService.unregisterServerInforListerner(mServiceListener);
            mTransferService.activityExit();
//            if (!mTransferService.isUnFinished())
//            {
//                stopService();
//            } else
//            {
//                mTransferService.activityExit();
//            }
        }

        unBindService();
    }

    @Override
    public ActionMode startActionMode(ActionMode.Callback callback)
    {
        mActionModeCallback = callback;

        MyOSActionMode mode = new MyOSActionMode(this, callback);
        mode.start();

        return mode;
    }

    @Override
    public void setActionMode(ActionMode actionMode)
    {
        mActionMode = actionMode;
    }

    @Override
    public ActionMode getActionMode()
    {
        return mActionMode;
    }

    @Override
    public Callback getActionModeCallback()
    {
        return mActionModeCallback;
    }

    @Override
    public void onTabChanged() {
        
    }
    
    @Override
    public View getActionModeCustomView()
    {
        return findViewById(R.id.myos_action_mode_view);
    }

    @Override
    public void onBackPressed()
    {
        if (mActionMode != null)
        {
            mActionMode.finish();
            return;
        }

        super.onBackPressed();
    }

    public Fragment getFragment(int tabIndex) {
        return mFragmentAdapter.getItem(tabIndex);
    }

//    protected void startService()
//    {
//        startService(new Intent(this, TransferService.class));
//    }
//
//    protected void stopService()
//    {
//        if (mTransferService != null)
//        {
//            mTransferService.reset();
//        }
//
//        stopService(new Intent(this, TransferService.class));
//    }

    private void bindService()
    {
        Intent intent = new Intent(this, TransferService.class);
        getApplicationContext().bindService(intent, mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void unBindService()
    {
        if (mTransferService != null)
        {
            
        }
        getApplicationContext().unbindService(mServiceCon);
    }
    
    private ServiceConnection mServiceCon = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mTransferService = ((TransferService.ServiceBinder) service).getService();
            if (mTransferService == null)
            {
                MyLog.e(TAG, "bind TransferService error, service is null");
                finish();
            }

            mTransferService.initDataForActivity();
            mTransferService.registerServerInforListerner(mServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mTransferService = null;
        }
        
    };
    
    public TransferService getTransferService()
    {
        return mTransferService;
    }
    
    public interface ITranserFragmentListener
    {
        void TranserServiceStarted();
    }
    
    private ServiceInforListener mServiceListener = new ServiceInforListener()
    {

        @Override
        public void userInformationFinish()
        {
            if (mTransferService.isUserKeyValid())
            {
                if (mNeedParseIntent)
                {
                    parseIntent();
                    mNeedParseIntent = false;
                }
    
                mUserKey = mTransferService.getUserKey();
                mHandler.sendEmptyMessage(MSG_USER_INFO_FINISH);
            } else
            {
                Toast.makeText(getApplicationContext(), R.string.get_user_info_error, Toast.LENGTH_LONG).show();
                mHandler.sendEmptyMessage(MSG_USER_INFO_ERROR);
            }
        }

        @Override
        public void userInformationError(int errorCode)
        {
            Toast.makeText(getApplicationContext(), R.string.get_user_info_error, Toast.LENGTH_LONG).show();
            mHandler.sendEmptyMessage(MSG_USER_INFO_ERROR);
            
            MyLog.e(TAG, "userInformationError, errorCode:" + errorCode);
        }

        @Override
        public void userInformationChanged()
        {
            
        }

        @Override
        public void storageUnmount()
        {
            TransferFilesActivity.this.finish();
        }

        @Override
        public void userKeyFinish()
        {
            
        }

        @Override
        public boolean userLogout()
        {
            TransferFilesActivity.this.finish();
            return false;
        }

        @Override
        public void networkConnect()
        {
            
        }

        @Override
        public void networkDisconnect()
        {
            
        }
        
    };

    private Handler mHandler = new Handler()
    {
        public void dispatchMessage(android.os.Message msg)
        {
            TransferFragment downloadFragment = (TransferFragment) getFragment(CloudFileUtil.DOWNLOAD_TAB_INDEX);
            TransferFragment uploadFragment = (TransferFragment) getFragment(CloudFileUtil.UPLOAD_TAB_INDEX);
            if (downloadFragment == null || uploadFragment == null)
            {
                return;
            }

            switch (msg.what)
            {
                case MSG_USER_INFO_FINISH:
                    downloadFragment.TranserServiceStarted();
                    uploadFragment.TranserServiceStarted();
                    break;
                    
                case MSG_USER_INFO_ERROR:
                    downloadFragment.showErrorInformation(ErrorStatusType.no_user_id);
                    uploadFragment.showErrorInformation(ErrorStatusType.no_user_id);
                default:
                    break;
            }
        }
    };

    private TabActivityListener mTabActivityListener = new TabActivityListener()
    {
        @Override
        public void goBack()
        {
            finish();
        }
    };
}

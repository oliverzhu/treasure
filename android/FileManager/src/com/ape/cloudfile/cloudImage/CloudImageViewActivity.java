package com.ape.cloudfile.cloudImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.cloudfile.CloudFileOperationActivity;
import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.transfer.TransferService;
import com.ape.cloudfile.transfer.TransferService.ServiceInforListener;
import com.ape.filemanager.FileCategoryHelper;
import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;

@SuppressWarnings("deprecation")
public class CloudImageViewActivity extends CloudFileOperationActivity
{
    private static final String TAG = "CloudImageViewActivity";
    private static final int GET_CLOUD_FILE_LIST_FINISH_MSG = 0;
    private static final int GET_CLOUD_FILE_LIST_ERROR_MSG = 1;
    private static final int GET_ONE_FILE_THUMB_FINISH_MSG = 2;
    private static final int GET_ALL_FILES_THUMB_FINISH_MSG = 3;

    private String mCurrentCloudPath;
    private String mImageCloudDir;

    private Gallery mGallery;
    private GalleryImageAdapter mAdapter;
    private ArrayList<CloudFile> mCloudFileList;
    private int mCurrentIndex;

    private ViewGroup mTopControlsView;
    private ViewGroup mBottomControlsView;
    private ImageControlsPanel mBottomControlsPanel;

    private CloudFileService mCloudFileService;
    private boolean mIsLoadingImage = false;
    private boolean mIsViewOneImage = false;
    private boolean mNeedCancelLoading = false;

    private TransferService mTransferService;

    private enum ErrorType
    {
        cloudFileServiceError,
        getCloudFileListError
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        MyLog.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_image_view);

        parseIntent(savedInstanceState);
        initControlsPanel();

        mCloudFileList = new ArrayList<CloudFile>();
        mAdapter = new GalleryImageAdapter(this, mCloudFileList);
        mGallery = (Gallery) findViewById(R.id.cloud_image_gallery);
        mGallery.setAdapter(mAdapter);
        setGallery();
        mNeedCancelLoading = false;

        bindService();
    }

    @Override
    protected void onResume()
    {
        MyLog.i(TAG, "onResume");
        super.onResume();
        mCloudFileService = CloudFileService.getInstance(this);
        if (!mCloudFileService.isFileClientValid())
        {
            showErrorInformation(ErrorType.cloudFileServiceError);
        } else
        {
            if (!mIsLoadingImage)
            {
                new LoadImageThread(mImageCloudDir).start();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);

        outState.putString(CloudFileUtil.VIEW_CLOUD_IMAGE_TAG, mCurrentCloudPath);
        MyLog.i(TAG, "onSaveInstanceState, currentCloudPath:" + mCurrentCloudPath);
    }

    @Override
    protected void onDestroy()
    {
        MyLog.i(TAG, "onDestroy");
        super.onDestroy();

        mNeedCancelLoading = true;
        if (mAdapter != null)
        {
            mAdapter.exit();
        }

        unBindService();
    }

    private boolean parseIntent(Bundle savedInstanceState)
    {
        boolean retVal = false;
        Intent intent = getIntent();

        mIsViewOneImage = intent.getBooleanExtra(CloudFileUtil.VIEW_ONE_IMAGE_MODE_TAG, false);
        if (savedInstanceState == null)
        {
            mCurrentCloudPath = intent.getStringExtra(CloudFileUtil.VIEW_CLOUD_IMAGE_TAG);
        } else 
        {
            mCurrentCloudPath = savedInstanceState.getString(CloudFileUtil.VIEW_CLOUD_IMAGE_TAG);
        }

        if (mCurrentCloudPath != null)
        {
            mImageCloudDir = Util.getCloudPathFromFilepath(mCurrentCloudPath);
            if (mImageCloudDir != null && mImageCloudDir.length() > 0)
            {
                if (!mImageCloudDir.endsWith("/"))
                {
                    mImageCloudDir = mImageCloudDir + "/";
                }
                retVal = true;
            } else
            {
                finish();
            }
        }

        MyLog.i(TAG, "In parseIntent, mImageCloudDir:" + mImageCloudDir + ", mCurrentCloudPath:" + mCurrentCloudPath);
        return retVal;
    }
    
    private void initControlsPanel()
    {
        mTopControlsView = (ViewGroup) findViewById(R.id.cloudphoto_top_controls);
        mBottomControlsView = (ViewGroup) findViewById(R.id.cloudphoto_bottom_controls);
        mBottomControlsPanel = new ImageControlsPanel(this, mTopControlsView, mBottomControlsView);

        TextView title = (TextView) mTopControlsView.findViewById(R.id.cloudphoto_top_title);
        TextView count = (TextView) mTopControlsView.findViewById(R.id.cloudphoto_top_count);
        ImageButton delBtn = (ImageButton) mBottomControlsView.findViewById(R.id.cloudphoto_bottom_control_delete_img);
        ImageButton downloadBtn = (ImageButton) mBottomControlsView.findViewById(R.id.cloudphoto_bottom_control_download_img);
        title.setOnClickListener(mBtnListener);
        count.setOnClickListener(mBtnListener);
        delBtn.setOnClickListener(mBtnListener);
        downloadBtn.setOnClickListener(mBtnListener);
    }
    
    private void setTitleAndCount()
    {
        String title = Util.getNameFromFilepath(mCurrentCloudPath);
        TextView titleView = (TextView) findViewById(R.id.cloudphoto_top_title);
        titleView.setText(title);
        
        String count = String.format("%d/%d", mCurrentIndex + 1, mCloudFileList.size());
        TextView countView = (TextView) findViewById(R.id.cloudphoto_top_count);
        countView.setText(count);
    }
    
    private OnClickListener mBtnListener = new OnClickListener()
    {

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.cloudphoto_top_title:
                    finish();
                    break;

                case R.id.cloudphoto_top_count:
                    mBottomControlsPanel.resetHideTime();
                    break;

                case R.id.cloudphoto_bottom_control_download_img:
                    if (CloudFileUtil.isNetWorkEnable(CloudImageViewActivity.this))
                    {
                        downloadCloudFile(mCloudFileList.get(mCurrentIndex));
                    } else {
                        Toast.makeText(CloudImageViewActivity.this, R.string.no_network_eror, Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.cloudphoto_bottom_control_delete_img:
                    if (CloudFileUtil.isNetWorkEnable(CloudImageViewActivity.this))
                    {
                        deleteCloudFile(mCloudFileList.get(mCurrentIndex));
                    } else {
                        Toast.makeText(CloudImageViewActivity.this, R.string.no_network_eror, Toast.LENGTH_SHORT).show();
                    }
                    break;

                default:
                    break;
            }
            
        }
    };

    private int calculateCurrentIndex()
    {
        int index;
        mCurrentIndex = 0;

        for (index = 0; index < mCloudFileList.size(); index++)
        {
            if (mCloudFileList.get(index).getKey().equals(mCurrentCloudPath))
            {
                mCurrentIndex = index;
                break;
            }
        }

        mGallery.setSelection(mCurrentIndex);
        MyLog.d(TAG, "calculateCurrentIndex, currentIndex:" + mCurrentIndex
                + ", size:" + mCloudFileList.size());

        return mCurrentIndex;
    }

    Handler mMainHandler = new Handler()
    {
        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case GET_CLOUD_FILE_LIST_FINISH_MSG:
                    ArrayList<CloudFile> list = (ArrayList<CloudFile>) msg.obj;
                    mCloudFileList.clear();
                    mCloudFileList.addAll(list);
                    mAdapter.notifyDataSetChanged();
                    calculateCurrentIndex();
                    break;

                case GET_CLOUD_FILE_LIST_ERROR_MSG:
                    showErrorInformation(ErrorType.getCloudFileListError);
                    break;

                case GET_ONE_FILE_THUMB_FINISH_MSG:
                    String cloudPath = (String) msg.obj;
                    if (cloudPath.equals(mCloudFileList.get(mCurrentIndex).getKey()))
                    {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;

                case GET_ALL_FILES_THUMB_FINISH_MSG:
                    mAdapter.notifyDataSetChanged();
                    mBottomControlsPanel.refresh();
                    break;

                default:
                    break;
            }
        }
    };

    private void setGallery()
    {
        mGallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id)
            {
                if (position < mCloudFileList.size())
                {
                    mCurrentIndex = position;
                    mCurrentCloudPath = mCloudFileList.get(mCurrentIndex).getKey();
                    setTitleAndCount();

                    mBottomControlsPanel.resetHideTime();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                
            }});

        mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                // To show top and bottom buttons.
                mBottomControlsPanel.refresh();
            }});
    }
    
    private void showErrorInformation(ErrorType errorType)
    {
        switch (errorType)
        {
            case cloudFileServiceError:
                Toast.makeText(this, R.string.cloud_file_service_error, Toast.LENGTH_SHORT).show();
                finish();
                break;

            case getCloudFileListError:
                Toast.makeText(this, R.string.get_file_list_fail, Toast.LENGTH_SHORT).show();
                finish();
                break;
        }
    }

    private class LoadImageThread extends Thread
    {
        private String mCloudDir;
        private ArrayList<CloudFile> mCloudLists;

        public LoadImageThread(String dir)
        {
            mCloudDir = dir;
            mCloudLists = new ArrayList<CloudFile>();
        }

        @Override
        public void run()
        {
            mIsLoadingImage = true;

            getCloudImageList();
            getCloudImageThumbnails();

            mIsLoadingImage = false;
        }

        private void getCloudImageList()
        {
            if (mIsViewOneImage)
            {
                String dir = Util.getCloudPathFromFilepath(mCurrentCloudPath);
                String name = Util.getNameFromFilename(mCurrentCloudPath);
                CloudFile cloudFile = new CloudFile(mCurrentCloudPath, dir, name, 0, 0, true);
                mCloudLists.add(cloudFile);
            } else
            {
                CloudFileResult result = mCloudFileService.listDirectory(mCloudDir);
                if (result.getResultCode() != CloudUtil.CLOUDCLIENT_RESULT_OK)
                {
                    mMainHandler.sendEmptyMessage(GET_CLOUD_FILE_LIST_ERROR_MSG);
                    return;
                }

                mCloudLists.clear();
                List<CloudFile> files = result.getFileList();
                for (int index = 0; index < files.size() && !mNeedCancelLoading; index++)
                {
                    CloudFile file = files.get(index);
                    FileCategory fc = FileCategoryHelper.getCategoryFromPath(file.getKey());
                    if (fc == FileCategory.Picture)
                    {
                        mCloudLists.add(file);
                    }
                }
            }

            Message msg = Message.obtain();
            msg.what = GET_CLOUD_FILE_LIST_FINISH_MSG;
            msg.obj = mCloudLists;
            mMainHandler.sendMessage(msg);
        }

        private void getCloudImageThumbnails()
        {
            GalleryImageTool tool = new GalleryImageTool(mCloudFileService);

            for (int index = 0; index < mCloudLists.size() && !mNeedCancelLoading; index++)
            {
                CloudFile file = mCloudLists.get(index);
                String cloudPath = file.getKey();
                String cachePath = CloudFileUtil.getLocalCachePath(cloudPath);
                File cacheFile = new File(cachePath);

                if (!cacheFile.exists())
                {
                    if (tool.getCloudThumbnailFile(cloudPath) != null)
                    {
                        Message msg = Message.obtain();
                        msg.what = GET_ONE_FILE_THUMB_FINISH_MSG;
                        msg.obj = cloudPath;
                        mMainHandler.sendMessage(msg);
                    }
                }
            }

            Message msg = Message.obtain();
            msg.what = GET_ALL_FILES_THUMB_FINISH_MSG;
            msg.obj = mCloudLists;
            mMainHandler.sendMessage(msg);
        }
    }

    @Override
    public void onTaskResult(int result)
    {
        super.onTaskResult(result);

        mCloudFileList.remove(mCurrentIndex);
        if (mCloudFileList.size() == 0)
        {
            finish();
            return;
        }

        if (mCloudFileList.size() == mCurrentIndex)
        {
            mCurrentIndex = mCurrentIndex -1;
        }
        mCurrentCloudPath = mCloudFileList.get(mCurrentIndex).getKey();
        mAdapter.notifyDataSetChanged();
        mGallery.setSelection(mCurrentIndex);
        setTitleAndCount();
    }

    private void bindService()
    {
        Intent intent = new Intent(this, TransferService.class);
        getApplicationContext().bindService(intent, mServiceConn, Service.BIND_AUTO_CREATE);
    }

    private void unBindService()
    {
        if (mTransferService != null)
        {
            
        }
        getApplicationContext().unbindService(mServiceConn);
    }

    private ServiceConnection mServiceConn = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mTransferService = ((TransferService.ServiceBinder) service).getService();
            if (mTransferService == null)
            {
                finish();
                Toast.makeText(CloudImageViewActivity.this, "Cannot start TransferService.", Toast.LENGTH_LONG).show();
            } else
            {
                mTransferService.registerServerInforListerner(mServiceListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
        }
        
    };

    private ServiceInforListener mServiceListener = new ServiceInforListener()
    {
        @Override
        public void userInformationFinish()
        {
            
        }

        @Override
        public void userKeyFinish()
        {
            
        }

        @Override
        public void userInformationChanged()
        {
            
        }

        @Override
        public void userInformationError(int errorCode)
        {
            
        }

        @Override
        public boolean userLogout()
        {
            return false;
        }

        @Override
        public void networkConnect()
        {
            
        }

        @Override
        public void networkDisconnect()
        {
            finish();
            Toast.makeText(CloudImageViewActivity.this, R.string.no_network_eror, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void storageUnmount()
        {
            finish();
        }
    };
}

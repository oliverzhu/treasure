package com.ape.cloudfile.transfer;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.ape.cloudfile.CloudFileActivity.ErrorStatusType;
import com.ape.cloudfile.CloudFileListAdapter;
import com.ape.cloudfile.CloudFileListItem;
import com.ape.cloudfile.CloudFileListItem.ModeCallback;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.transfer.TransferFilesActivity.ITranserFragmentListener;
import com.ape.cloudfile.transfer.TransferService.TransferType;
import com.ape.cloudfile.transfer.TransferService.TransferUiListener;
import com.ape.cloudfile.widget.BottomActionBar;
import com.ape.cloudfile.widget.CloudListView;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.R;
import com.cloud.client.file.MissionObject;

public abstract class TransferFragment extends Fragment 
    implements ITranserFragmentListener, CloudFileListAdapter.ListItemOperationListener
{
    private static final String TAG = "TransferFragment";
    protected TransferFilesActivity mActivity;
    protected View mRootView;
    
    protected View mErrorView;
    protected View mTransferContentView;

    protected TransferService mTransferService;
    private boolean mServiceBindNeedRefreshList;
    
    private CloudListView mTransferListView;
    private CloudFileListAdapter<MissionObject> mTransferAdapter;
    protected ArrayList<MissionObject> mTransferList= new ArrayList<MissionObject>();
    protected int mLlistTitleCount = 0;

    // transferringlist and transferredlist get data from service
    protected ArrayList<MissionObject> mTransferringList = null;
    protected ArrayList<MissionObject> mTransferredList = null;

    private ModeCallback<MissionObject> mActionModeCallback;
    private BottomActionBar mBottomActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        mActivity = (TransferFilesActivity) getActivity();
        mRootView = inflater.inflate(R.layout.cloud_file_transfer_fragment,
                container, false);
        
        mErrorView = mRootView.findViewById(R.id.error_information_view);
        mTransferContentView = mRootView.findViewById(R.id.transfer_list_content);

        mTransferAdapter = new CloudFileListAdapter<MissionObject>(mActivity, mTransferList);
        mTransferAdapter.setOperationListener(this);
        mTransferListView = (CloudListView) mRootView.findViewById(R.id.transfer_list);
        mTransferListView.setAdapter(mTransferAdapter);
        mTransferListView.setEmptyView(mRootView.findViewById(R.id.transfer_empty_view));
        mTransferListView.setOnItemLongClickListener(new onListItemLongClickListener());
        mTransferListView.setOnItemClickListener(new OnListItemClickListener());
        mTransferAdapter.setListView(mTransferListView);

        mBottomActionBar = (BottomActionBar) mRootView.findViewById(R.id.bottom_aciton_bar);
        mActionModeCallback = new ModeCallback<MissionObject>(mActivity,
                mTransferAdapter, mBottomActionBar);

        return mRootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        MyLog.i(TAG, "onStart");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MyLog.i(TAG, "onResume");
        
        if (mServiceBindNeedRefreshList)
        {
            mServiceBindNeedRefreshList = false;
            TranserServiceStarted();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        MyLog.i(TAG, "onPause");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        MyLog.i(TAG, "onStop");
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        MyLog.i(TAG, "onSaveInstanceState");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (mTransferAdapter != null)
        {
            mTransferAdapter.exit();
        }
        if (mTransferService != null)
        {
            mTransferService.unregisterTransferUiListener(mTransferListener, getTransferType());
        }
        MyLog.i(TAG, "onDestroy");
    }

    private class onListItemLongClickListener implements OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                int position, long id)
        {
            ActionMode actionMode = mActivity.getActionMode();
            if (actionMode == null)
            {
                CloudFileListItem.onClickCheckedFileItem(view, mTransferAdapter);
                actionMode = mActivity.startActionMode(mActionModeCallback);
                mActivity.setActionMode(actionMode);
//                Util.updateActionModeTitle(actionMode, mActivity,
//                        mTransferAdapter.getCheckedFileCount());
                mActionModeCallback.updateActionModeUI();
                mTransferAdapter.notifyDataSetChanged();
            }
            return true;
        }
    }
    
    private class OnListItemClickListener implements OnItemClickListener
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                int position, long id)
        {
            ActionMode actionMode = mActivity.getActionMode();
            if (actionMode != null)
            {
                CloudFileListItem.onClickCheckedFileItem(view, mTransferAdapter);
                if (mTransferAdapter.getCheckedFileCount() > 0)
                {
                    actionMode.invalidate();
                }
//                    Util.updateActionModeTitle(actionMode, mActivity,
//                            mTransferAdapter.getCheckedFileCount());
                mActionModeCallback.updateActionModeUI();
            }
        }
    }
    
    protected Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            if (!isAdded())
                return;

            switch (msg.what)
            {
                case CloudFileUtil.MSG_LOAD_LIST:
                    uploadList();
                    break;
                    
                case CloudFileUtil.MSG_REFRESH_LIST:
                    refreshList();
                    break;

                default:
                    break;
            }
        };
    };
    
    public void uploadList()
    {
        new Thread(new Runnable()
        {
            
            @Override
            public void run()
            {
                loadTransferList();
                mHandler.sendEmptyMessage(CloudFileUtil.MSG_REFRESH_LIST);
            }
        }).start();
    }
    
    protected boolean loadTransferList()
    {
        boolean retVal = false;
        if (mTransferService == null)
        {
            return false;
        }

        mTransferringList = mTransferService.getTransferringList(getTransferType());
        mTransferredList = mTransferService.getTransferredList(getTransferType());

        return retVal;
    }
    
    public void refreshList()
    {
        if (mTransferService == null)
        {
            return;
        }

        int listTitleCount = 0;
        mTransferList.clear();
        if (mTransferringList != null && mTransferringList.size() > 0)
        {
            MissionObject listTitle = new MissionObject(null, getTransferringPrompt(), null);
            listTitle.setId(CloudFileUtil.TRANSFER_LIST_TITLE_ID);
            mTransferList.add(listTitle);
            mTransferList.addAll(mTransferringList);
            ++listTitleCount;
        }
        if (mTransferredList != null && mTransferredList.size() > 0)
        {
            MissionObject listTitle = new MissionObject(null, getTransferredPrompt(), null);
            listTitle.setId(CloudFileUtil.TRANSFER_LIST_TITLE_ID);
            mTransferList.add(listTitle);
            mTransferList.addAll(mTransferredList);
            ++listTitleCount;
        }

        mTransferAdapter.setListTitleCount(listTitleCount);
        mTransferAdapter.notifyDataSetChanged();

        mErrorView.setVisibility(View.GONE);
        mTransferContentView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void TranserServiceStarted()
    {
        MyLog.i(TAG, "TranserServiceStarted, mActivity:" + mActivity);
        if (mActivity == null)
        {
            mServiceBindNeedRefreshList = true;
        } else
        {
            mTransferService = mActivity.getTransferService();
            mTransferService.registerTransferUiListener(mTransferListener, getTransferType());
            mHandler.sendEmptyMessage(CloudFileUtil.MSG_LOAD_LIST);
        }
    }
    
    public void showErrorInformation(ErrorStatusType type)
    {
        TextView errorText = (TextView)mErrorView.findViewById(R.id.error_information_text);

        switch (type)
        {
            case ok:
                break;
                
            case no_network:
                errorText.setText(R.string.no_network_eror);
                break;

            case no_service_sdk:
                errorText.setText(R.string.no_service_sdk);
                break;

            case no_user_id:
                errorText.setText(R.string.no_user_id);
                break;

            case sdcard_busy:
                errorText.setText(R.string.no_user_id);
                break;

            case get_list_fail:
                break;
        }
        
        if (type == ErrorStatusType.ok)
        {
            mErrorView.setVisibility(View.GONE);
            mTransferContentView.setVisibility(View.VISIBLE);
        } else
        {
            mErrorView.setVisibility(View.VISIBLE);
            mTransferContentView.setVisibility(View.GONE);
        }
    }
    
    @Override
    public boolean downloadFiles()
    {
        return true;
    }
    
    @Override
    public boolean isCheckedMode()
    {
        return (mActivity.getActionMode() != null);//false;
    }
    
    protected ArrayList<MissionObject> getCheckedList()
    {
        return mTransferAdapter.getCheckedFiles();
    }
    
    @Override
    public void setProgressBarInfo(RoundProgressBar bar, MissionObject mission)
    {
        if (mTransferService != null)
        {
            mTransferService.addOrUpdateTransfer(mission, bar, getTransferType());
        }
    }
    
    @Override
    public void removeProgressBarInfo(RoundProgressBar bar, MissionObject mission)
    {
        if (mTransferService != null)
        {
            mTransferService.removeTransferProgressBar(bar, mission, getTransferType());
        }
    }
    
    @Override
    public void onActionModeFinished()
    {
        if (mBottomActionBar != null)
        {
            mBottomActionBar.setVisibility(View.GONE);
        }
    }
    
    private TransferUiListener mTransferListener = new TransferUiListener()
    {

        @Override
        public void transferListChanged()
        {
            if (mHandler != null)
            {
                mHandler.sendEmptyMessage(CloudFileUtil.MSG_REFRESH_LIST);
            }
        }

    };
    
    protected class DeleteListener implements OperationEventListener
    {
        ProgressDialog progressDialog;

        @Override
        public void onTaskPrepare()
        {
            progressDialog = new ProgressDialog(mActivity);
            progressDialog.setTitle(R.string.operation_delete);
            progressDialog.setMessage(mActivity.getString(R.string.wait));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
        }

        @Override
        public void onTaskResult(int result)
        {
            if (progressDialog != null)
            {
                try
                {
                    progressDialog.dismiss();
                } catch (Exception e)
                {
                }
                progressDialog = null;
            }
            //mHandler.sendEmptyMessage(CloudFileUtil.MSG_REFRESH_LIST);
            //refreshList(); //update list by TransferUiListener;
            ActionMode mode = mActivity.getActionMode();
            if (mode != null)
            {
                mode.finish();
            }
        }
        
    }

    abstract protected String getTransferringPrompt();
    
    abstract protected String getTransferredPrompt();
    
    abstract public TransferType getTransferType();
}

package com.ape.cloudfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.cloudfile.CloudFileListItem.ModeCallback;
import com.ape.cloudfile.CloudFileSortHelper.SortMethod;
import com.ape.cloudfile.cloudSearch.CloudFileSearchActivity;
import com.ape.cloudfile.transfer.TransferFilesActivity;
import com.ape.cloudfile.transfer.TransferService;
import com.ape.cloudfile.transfer.TransferService.ServiceInforListener;
import com.ape.cloudfile.widget.ApeProgressDialog;
import com.ape.cloudfile.widget.BottomActionBar;
import com.ape.cloudfile.widget.BottomActionBar.BottomActionBarListener;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.FileExplorerTabActivity;
import com.ape.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.FileViewActivity;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.R;
import com.ape.filemanager.TextInputDialog;
import com.ape.filemanager.TextInputDialog.OnFinishListener;
import com.ape.filemanager.Util;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.MissionObject;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@SuppressLint("HandlerLeak")
public class CloudFileActivity extends Fragment implements IBackPressedListener
    , CloudFileListAdapter.ListItemOperationListener
{
    private static final String TAG = "CloudFileActivity";
    private static final String USER_KEY = "user_key";

    private FileExplorerTabActivity mActivity;
    private View mRootView;
    private PullToRefreshListView mFileListView;
    private ListView mActualListView;
    private CloudFileListAdapter<CloudFile> mAdapter;
    
    private ModeCallback<CloudFile> mActionModeCallback;

    private View mNavigationBar;
    private TextView mNavigationBarText;
    private View mUploadBtnView;
    private View mUpLevelView;

    private ArrayList<CloudFile> mFileNameList = new ArrayList<CloudFile>();
    private ArrayList<CloudFile> mWorkFileList = new ArrayList<CloudFile>();
    CloudFileService mCloudFileService;

    private String mCurrentPath;
    private String mRoot = CloudFileUtil.CLOUD_ROOT_DIR;
    private boolean mIsEntryFromOtherApp = false;
    private boolean mNeedRefreshWhenEntry = false;

    private CloudFileOperator mCloudFileOperator;
    private CloudFileSortHelper mSortHelper;
    private ArrayList<FileInfo> mUploadFiles = new ArrayList<FileInfo>();

    private static ApeProgressDialog mProgressDialog;

    private static final int MENU_OPERATION_SEARCH = 200;
    private static final int MENU_OPERATION_REFRESH = 201;
    private static final int MENU_OPERATION_CREATE_DIR = 202;

    private static final int MENU_SORT = 203;
    private static final int MENU_SORT_NAME = 204;
    private static final int MENU_SORT_SIZE = 205;
    private static final int MENU_SORT_DATE = 206;
    private static final int MENU_SORT_TYPE = 207;
    
    private static final int MENU_TRANSFER_LIST = 208;
    
    private static final int MENU_OPERATION_UPLOAD = 209;
    private static final int MENU_OPERATION_CANCEL = 210;

    public static final int LOGIN_REQUEST_CODE = 1;
    public static final int UPLOAD_REQUEST_CODE = 2;
    public static final int DOWNLOAD_REQUEST_CODE = 3;

    private String mDownLoadPath = null;

    private boolean mIsHasReferSDK = false;
    private boolean mIsMultiTransfer = false;
    
    private BottomActionBar mBottomActionBar;
    private ListDisplayMode mListDisplayMode = ListDisplayMode.normalMode;

    private TransferService mTransferService;
    private String mUserKey = null;
    private String mUserName = null;

    private enum ListDisplayMode
    {
        normalMode, uploadMode
    }

    public enum ErrorStatusType
    {
        ok, sdcard_busy, no_network, no_service_sdk, no_user_id, user_logout,
        get_list_fail, user_infor_error
    }

    private OnCreateContextMenuListener mListMenuCreateListener = new CloudFileListOnCreateContextMenuListener();

    private void init()
    {
        //mCurrentPath = mRoot; // Move to onCreateView
        mCloudFileService = CloudFileService.getInstance(mActivity.getApplicationContext());

        mDownLoadPath = CloudFileUtil.getDownloadPath();
        File downFile = new File(mDownLoadPath);
        if (!downFile.exists())
        {
            downFile.mkdirs();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String oldKey = pref.getString(USER_KEY, "-1");
        if (!oldKey.equals(mUserKey))
        {
            mCloudFileOperator.doClearCache();

            SharedPreferences.Editor editor = pref.edit();
            editor.putString(USER_KEY, mUserKey);
            editor.commit();
        }
    }

    private class OnCloudFileClick implements OnItemClickListener
    {

        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                int position, long id)
        {
            ActionMode mode = mActivity.getActionMode();
            if (mode != null)
            {
                CloudFileListItem.onClickCheckedFileItem(view, mAdapter);
                mode.invalidate();
                mActionModeCallback.updateActionModeUI();
//                    Util.updateActionModeTitle(mode, mActivity,
//                            mAdapter.getCheckedFileCount());
                return;
            }

            if (mAdapter.isPullToRefreshList() && position > 0)
                position--;
            CloudFile file = (CloudFile) mAdapter.getItem(position);
            if (file.isFile())
            {
                if (!mCloudFileOperator.viewCloudFile(file, true)
                        && !mActivity.isMyOsOptionMenuStyle())
                {
                    view.showContextMenu();
                }
            } else
            {
                mCurrentPath = file.getKey();
                listFile(mCurrentPath);
            }
        }

    }
    
    private class OnCloudFileLongClick implements OnItemLongClickListener
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                int position, long id)
        {
            if (mListDisplayMode == ListDisplayMode.uploadMode || mIsLoadingFileList)
                return false;

            ActionMode mode = mActivity.getActionMode();
            if (mode == null)
            {
                CloudFileListItem.onClickCheckedFileItem(view, mAdapter);
                mode = mActivity.startActionMode(mActionModeCallback);
                mActivity.setActionMode(mode);
                mAdapter.notifyDataSetChanged();
                mActionModeCallback.updateActionModeUI();
                //Util.updateActionModeTitle(mode, mActivity, mAdapter.getCheckedFileCount());
                return true;
            }
            return false;
        }
        
    }

    private String initFirstPath()
    {
        mCurrentPath = mRoot; // Default path.
        mIsEntryFromOtherApp = false;

        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action) && action.equals(CloudFileUtil.CLOUD_STORAGE_ACTION))
        {
            String cloudPath = intent.getStringExtra(CloudFileUtil.CLOUD_STORAGE_EXTRA_PATH_TAG);
            if (cloudPath != null)
            {
                mCurrentPath = cloudPath;
                mNeedRefreshWhenEntry = true;
                if (intent.getBooleanExtra(CloudFileUtil.CLOUD_STORAGE_FROM_OTHER_APP, false))
                {
                    mIsEntryFromOtherApp = true;
                }
            }
        }

        return mCurrentPath;
    }
    
    private void initSearchBar()
    {
        LinearLayout searchView;
        searchView = (LinearLayout) mRootView.findViewById(R.id.search_view_frame);
        TextView prompt = (TextView) searchView.findViewById(R.id.search_file_view);
        prompt.setText(R.string.search_cloud_file);

        searchView.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                gotoCloudSearch();
            }
        });
        
        if (mActivity.isMyOsOptionMenuStyle())
        {
            searchView.setVisibility(View.GONE);
        } else
        {
            searchView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        mActivity = (FileExplorerTabActivity) getActivity();
        mCloudFileOperator = new CloudFileOperator(mActivity);
        mSortHelper = new CloudFileSortHelper(mActivity);

        mRootView = inflater.inflate(R.layout.cloud_file_activity,
                container, false);

        mFileListView = (PullToRefreshListView) mRootView.findViewById(R.id.file_path_list);
        mFileListView.setOnRefreshListener(mPullRefreshListener);
        mFileListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
        mActualListView = mFileListView.getRefreshableView();
        mActualListView.setOnItemClickListener(new OnCloudFileClick());
        if (mActivity.isMyOsOptionMenuStyle())
        {
            mActualListView.setOnItemLongClickListener(new OnCloudFileLongClick());
        } else
        {
            mActualListView.setOnCreateContextMenuListener(mListMenuCreateListener);
        }
        mAdapter = new CloudFileListAdapter<CloudFile>(mActivity, mFileNameList);
        mAdapter.setOperationListener(this);
        mAdapter.setIsPullToRefreshList(true);
        mActualListView.setAdapter(mAdapter);

        mUploadBtnView = mRootView.findViewById(R.id.upload_operation_bar);
        mUploadBtnView.findViewById(R.id.button_upload_confirm).setOnClickListener(mButtonClick);
        mUploadBtnView.findViewById(R.id.button_upload_cancel).setOnClickListener(mButtonClick);
        
        mNavigationBar = mRootView.findViewById(R.id.navigation_bar);
        mNavigationBarText = (TextView) mNavigationBar.findViewById(R.id.current_path_view);
        mUpLevelView = mNavigationBar.findViewById(R.id.path_pane_up_level);
        mUpLevelView.setOnClickListener(mButtonClick);

        mIsMultiTransfer = getResources().getBoolean(R.bool.cloud_file_multi_transfer);

        mBottomActionBar = (BottomActionBar) mRootView.findViewById(R.id.bottom_aciton_bar);
        if (mBottomActionBar != null)
        {
            createOptionMenu(null);
        } else
        {
            setHasOptionsMenu(true);
        }
        mActionModeCallback = new ModeCallback<CloudFile>(mActivity, mAdapter, mBottomActionBar);

        //mIsHasReferSDK = checkReferSDK();
        
        initFirstPath();
        
        initSearchBar();

        startAndBindService();
        return mRootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        mIsHasReferSDK = checkReferSDK();

        if (mTransferService != null && mTransferService.isNetWorkConnect())
        {
            CloudUserInfomation userInfo = mTransferService.getUserInfomation();
            if (userInfo != null && !userInfo.isLoading())
            {
                if (userInfo.isInvalidUserKey() || !mTransferService.isUserLogin())
                {
                    userInfo.goToGetUserKey();
                } else
                {
                    userInfo.goToGetUserCloudSpace(CloudFileService.getInstance(mActivity.getApplicationContext()));
                }
            } else
            {
                updateUI();
            }
        } else
        {
            updateUI();
        }

        if (mAdapter != null)
        {
            mAdapter.resumeIconLoader();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mAdapter != null)
        {
            mAdapter.pauseIconLoader();
        }
    }

    public void onFragementResume()
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        stopAndUnbindService();
        
        if (mAdapter != null)
        {
            mAdapter.exit();
        }
    }

    @Override
    public boolean onBack()
    {
        if (mAdapter == null || mIsEntryFromOtherApp)
        {
            return false;
        }

        ActionMode mode = mActivity.getActionMode();
        if (mode != null)
        {
            mode.finish();
            return true;
        }

        return onOperationUpLevel();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        AdapterView.AdapterContextMenuInfo info;
        try
        {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException exception)
        {
            MyLog.e(TAG, "Bad menuInfo." + exception);
            return false;
        }
        CloudFile file = (CloudFile) mAdapter
                .getItem(info.position);
        mAdapter.clearCheckedFiles();
        mAdapter.addCheckedFile(file);

        switch (item.getItemId())
        {
            case CloudFileListOnCreateContextMenuListener.MENU_ITEM_DELETE:
                deleteFiles();
                break;
            case CloudFileListOnCreateContextMenuListener.MENU_ITEM_DOWNLOAD:
                downloadFiles();
                break;
            default:
                break;
        }
        mAdapter.clearCheckedFiles();
        return super.onContextItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        createOptionMenu(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void addMenuItem(Menu menu, int itemId, int order, int string,
            int iconRes)
    {
        if (mBottomActionBar != null)
        {
            Drawable iconDrawable = null;
            if (iconRes > 0)
            {
                iconDrawable = mActivity.getResources().getDrawable(iconRes);
            }
            mBottomActionBar.addMenuItem(mActivity.getString(string),
                    iconDrawable, itemId);
        } else
        {
            MenuItem item = menu.add(0, itemId, order, string)
                    .setOnMenuItemClickListener(mMenuItemClickListener);
            if (iconRes > 0)
            {
                item.setIcon(iconRes);
            }
        }
    }

    private void addMenuItem(Menu menu, int itemId, int order, int string)
    {
        addMenuItem(menu, itemId, order, string, -1);
    }

    private OnMenuItemClickListener mMenuItemClickListener = new OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            int itemId = item.getItemId();

            return onMyItemClicked(item, itemId);
        }
    };

    private boolean onMyItemClicked(MenuItem item, int itemId)
    {
        switch (itemId)
        {
            case MENU_OPERATION_SEARCH:
                gotoCloudSearch();
                break;

            case MENU_OPERATION_REFRESH:
                refreshFileList(mCurrentPath);
                break;

            case MENU_OPERATION_CREATE_DIR:
                createFolder();
                break;

            case MENU_OPERATION_UPLOAD:
                uploadFiles();
                break;
                
            case MENU_OPERATION_CANCEL:
                endUploadFiles();
                break;

            case MENU_SORT:
                onClickMenuSort();
                break;

            case MENU_SORT_NAME:
                item.setChecked(true);
                onSortChanged(SortMethod.name);
                break;

            case MENU_SORT_SIZE:
                item.setChecked(true);
                onSortChanged(SortMethod.size);
                break;

            case MENU_SORT_DATE:
                item.setChecked(true);
                onSortChanged(SortMethod.date);
                break;

            case MENU_SORT_TYPE:
                item.setChecked(true);
                onSortChanged(SortMethod.type);
                break;
                
            case MENU_TRANSFER_LIST:
                gotoTransferList();
                break;

            default:
                return false;
        }
        return true;
    }

    private void onClickMenuSort()
    {
        PopupMenu popMenu = new PopupMenu(mActivity, mBottomActionBar.getPopupMenuAncherView());

        Menu sortMenu = popMenu.getMenu();
        sortMenu.add(0, MENU_SORT_NAME, 0, R.string.menu_item_sort_name).setOnMenuItemClickListener(mMenuItemClickListener);
        sortMenu.add(0, MENU_SORT_SIZE, 1, R.string.menu_item_sort_size).setOnMenuItemClickListener(mMenuItemClickListener);
        sortMenu.add(0, MENU_SORT_DATE, 2, R.string.menu_item_sort_date).setOnMenuItemClickListener(mMenuItemClickListener);
        sortMenu.add(0, MENU_SORT_TYPE, 3, R.string.menu_item_sort_type).setOnMenuItemClickListener(mMenuItemClickListener);
        sortMenu.setGroupCheckable(0, true, true);
        sortMenu.getItem(mSortHelper.getSortIndex()).setChecked(true);
        popMenu.show();
    }

    private BottomActionBarListener mBottomActionBarListener = new BottomActionBarListener()
    {
        @Override
        public boolean onActionItemClick(int itemId)
        {
            return onMyItemClicked(null, itemId);
        }
    };

    public void onSortChanged(SortMethod s)
    {
        if (mSortHelper.getSortMethod() != s)
        {
            mSortHelper.setSortMethod(s);
            Collections.sort(mFileNameList, mSortHelper.getComparator());
            mAdapter.notifyDataSetChanged();
        }
    }

    private boolean createOptionMenu(Menu menu)
    {
        if (mBottomActionBar != null)
        {
            mBottomActionBar.clearAllButtons();
            mBottomActionBar.setNormalDisplayCount(getResources().getInteger(R.integer.bottom_bar_normal_item_count));
            mBottomActionBar.setBottomActionBarListerner(mBottomActionBarListener);
        }

        addMenuItem(menu, MENU_OPERATION_SEARCH, 0, R.string.menu_item_search, R.drawable.operation_button_search);
        addMenuItem(menu, MENU_OPERATION_UPLOAD, 0, R.string.operation_send_to_cloud, R.drawable.operation_button_upload);
        addMenuItem(menu, MENU_OPERATION_CANCEL, 0, R.string.operation_cancel, R.drawable.operation_button_cancel);
        addMenuItem(menu, MENU_OPERATION_REFRESH, 1, R.string.menu_refresh_str);
        addMenuItem(menu, MENU_OPERATION_CREATE_DIR, 2, R.string.operation_create_folder);

        if (mBottomActionBar != null)
        {
            addMenuItem(menu, MENU_SORT, 3, R.string.menu_item_sort);
        } else
        {
            SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 3, R.string.menu_item_sort);
            addMenuItem(sortMenu, MENU_SORT_NAME, 0, R.string.menu_item_sort_name);
            addMenuItem(sortMenu, MENU_SORT_SIZE, 1, R.string.menu_item_sort_size);
            addMenuItem(sortMenu, MENU_SORT_DATE, 2, R.string.menu_item_sort_date);
            addMenuItem(sortMenu, MENU_SORT_TYPE, 3, R.string.menu_item_sort_type);
            sortMenu.setGroupCheckable(0, true, true);
            sortMenu.getItem(mSortHelper.getSortIndex()).setChecked(true);
        }

        if (mIsMultiTransfer)
        {
            addMenuItem(menu, MENU_TRANSFER_LIST, 3, R.string.transfer_list_title);
        }

        return true;
    }
    
    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        boolean showMenu = Util.isSDCardReady();
        if (mBottomActionBar != null)
        {
            return;
        }

        menu.findItem(MENU_OPERATION_REFRESH).setEnabled(showMenu);
        menu.findItem(MENU_OPERATION_CREATE_DIR).setEnabled(showMenu);
        menu.findItem(MENU_SORT).setEnabled(showMenu);
        if (mIsMultiTransfer)
        {
            menu.findItem(MENU_TRANSFER_LIST).setEnabled(showMenu);
        }
    }
    
    private void onPrepareButtomActionBar()
    {
        if (mBottomActionBar != null && isAdded())
        {
            if (mBottomActionBar.findItem(MENU_OPERATION_REFRESH) == null)
                return;
            boolean showMenu = Util.isSDCardReady();

            mBottomActionBar.findItem(MENU_OPERATION_REFRESH).setVisible(false);
            if (getResources().getBoolean(R.bool.cloud_file_can_upload_file))
            {
                mBottomActionBar.findItem(MENU_OPERATION_CREATE_DIR).setEnable(showMenu);
            } else
            {
                mBottomActionBar.findItem(MENU_OPERATION_CREATE_DIR).setVisible(false);
            }
            mBottomActionBar.findItem(MENU_SORT).setEnable(showMenu);
            if (mIsMultiTransfer)
            {
                mBottomActionBar.findItem(MENU_TRANSFER_LIST).setEnable(showMenu);
            }

            if (mListDisplayMode == ListDisplayMode.normalMode)
            {
                mBottomActionBar.findItem(MENU_OPERATION_UPLOAD).setVisible(false);
                mBottomActionBar.findItem(MENU_OPERATION_CANCEL).setVisible(false);
                mBottomActionBar.setNormalDisplayCount(getResources().getInteger(R.integer.bottom_bar_normal_item_count));
            } else if (mListDisplayMode == ListDisplayMode.uploadMode)
            {
                mBottomActionBar.findItem(MENU_OPERATION_UPLOAD).setVisible(true);
                mBottomActionBar.findItem(MENU_OPERATION_CANCEL).setVisible(true);
                mBottomActionBar.setNormalDisplayCount(4);
            }

            mBottomActionBar.setVisibility(View.VISIBLE);
            mBottomActionBar.refresh();
        }
    }

    interface DialogListener
    {
        void onDialogConfirm();
        void onDialogCancal();
    }

    protected void createDialog(int id, final DialogListener listener)
    {
        String title = null;
        String message = null;
        switch (id)
        {
            case CloudFileUtil.DIALOG_ID_NO_WIFI:
                title = getString(R.string.cloud_kindly_prompt_title);
                message = getString(R.string.no_wifi_prompt);
                break;
            case CloudFileUtil.DIALOG_ID_VIEW_CLOUD_FILE:
                title = getString(R.string.cloud_kindly_prompt_title);
                message = getString(R.string.view_cloud_file_prompt);
                break;

            default:
                message = null;
                break;
        }

        if (message != null)
        {
            Dialog dialog = new AlertDialog.Builder(mActivity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (listener != null)
                        {
                            listener.onDialogConfirm();
                        }
                        
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if (listener != null)
                        {
                            listener.onDialogCancal();
                        }
                    }
                }).create();
            dialog.show();
        }
    }

    private Handler mHandler = new Handler()
    {
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case CloudFileUtil.MSG_UPDATE_UI:
                    updateUI();
                    break;
                case CloudFileUtil.MSG_REFRESHED_LIST:
                    mFileListView.onRefreshComplete();
                    break;
            }
        }
    };
    
    private View.OnClickListener mButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v)
        {
            switch (v.getId())
            {
                case R.id.user_information_view:
                    gotoLogin();
                    break;

                case R.id.cloud_login_btn:
                    gotoLogin();
                    break;

                case R.id.navigation_bar:
                    if (mCurrentPath == null || mCurrentPath.equals(mRoot))
                    {
                        gotoLogin();
                    }
                    break;

                case R.id.path_pane_up_level:
                    onOperationUpLevel();
                    break;

                case R.id.button_upload_confirm:
                    uploadFiles();
                    break;

                case R.id.button_upload_cancel:
                    endUploadFiles();
                    break;

                default:
                    break;
            }
            
        }
        
    };

    public boolean selectFolderToUploadFiles(ArrayList<FileInfo> filesList)
    {
        MyLog.i(TAG, "filesList.size: " + filesList.size() + ", mUserKey:" + mUserKey);

        if (checkAndPromptBeforeOperation())
        {
            mUploadFiles.clear();
            for (FileInfo info : filesList)
            {
                mUploadFiles.add(info);
                if (info.IsDir)
                {
                    mUploadFiles.addAll(Util.getAllFileInfosUnderPath(info.filePath));
                }
            }

            if (mBottomActionBar != null)
            {
                mListDisplayMode = ListDisplayMode.uploadMode;
            } else
            {
                mUploadBtnView.setVisibility(View.VISIBLE);
            }
            updateUI();
            return true;
        }

        return false;
    }

    @Override
    public boolean isCheckedMode()
    {
        if (mActivity != null && mActivity.isMyOsOptionMenuStyle())
        {
            return (mActivity.getActionMode() != null && mActivity.getActionModeCallback() == mActionModeCallback);
        } else
        {
            return (mUploadBtnView.getVisibility() != View.VISIBLE);
        }
    }

    @Override
    public void setProgressBarInfo(RoundProgressBar bar, MissionObject mission)
    {
    }
    
    @Override
    public void removeProgressBarInfo(RoundProgressBar bar, MissionObject mission)
    {
    }

    @Override
    public void onActionModeFinished()
    {
        createOptionMenu(null);
        onPrepareButtomActionBar();
    }

    private ErrorStatusType checkUserInforError()
    {
        boolean sdCardReady = Util.isSDCardReady();
        ErrorStatusType type = ErrorStatusType.ok;

        if (!sdCardReady)
            type = ErrorStatusType.sdcard_busy;
        else if (!mTransferService.isNetWorkConnect())
            type = ErrorStatusType.no_network;
        else if (!mIsHasReferSDK)
            type = ErrorStatusType.no_service_sdk;
        else if (!mTransferService.isUserLogin())
            type = ErrorStatusType.user_logout;
        else if (!mTransferService.isUserInfoInited())
            type = ErrorStatusType.no_user_id;
        else if (!mTransferService.isUserKeyValid())
            type = ErrorStatusType.user_logout;
        else if (!mTransferService.isUserInfomationValid())
            type = ErrorStatusType.user_infor_error;
        
        return type;
    }

    private void updateUI()
    {
        if (!isAdded() || mTransferService == null)
        {
            return;
        }

        ErrorStatusType type = checkUserInforError();
        showErrorInformation(type);
        //updateNavigationPane();

        if (type == ErrorStatusType.ok)
        {
            MyLog.i(TAG, "updateUI, mNeedRefreshWhenEntry:" + mNeedRefreshWhenEntry);
            if (mNeedRefreshWhenEntry)
            {
                refreshFileList(mCurrentPath);
                mNeedRefreshWhenEntry = false;
            } else if (!mIsLoadingFileList)
            {
                listFile(mCurrentPath);
            }
            //onPrepareButtomActionBar();
        } else
        {
            mFileListView.onRefreshComplete();
            mBottomActionBar.setVisibility(View.GONE);
        }

        showUserInformation();
    }
    
    private void showUserInformation()
    {
        if (!isAdded() || mTransferService == null)
        {
            return;
        }
        if (mCurrentPath != null && !mCurrentPath.equals(mRoot))
        {
            updateNavigationPane();
            return;
        } else {
            mUpLevelView.setVisibility(View.GONE);
        }

        View userView = mNavigationBar; //mRootView.findViewById(R.id.user_information_view);
        TextView helloUser = mNavigationBarText; //(TextView) userView.findViewById(R.id.user_info_text);
        StringBuilder information = new StringBuilder();
        CloudUserInfomation userInfo = mTransferService.getUserInfomation();

        if (mTransferService.isNetWorkConnect())
        {
            if (mTransferService.isUserLogin())
            {
                information.append(getString(R.string.hello_user, mUserName));
            } else
            {
                information.append(getString(R.string.user_logout));
            }
            if (userInfo.getUsedSpace() >= 0)
            {
                String usedString = null;
                if (userInfo.getUsedSpace() > userInfo.getMaxSpace() - CloudFileUtil.MIN_ROUND_OFF)
                {
                    usedString = Util.convertStorage(userInfo.getUsedSpace() - CloudFileUtil.MIN_ROUND_OFF);
                } else {
                    usedString = Util.convertStorage(userInfo.getUsedSpace());
                }
                information.append("   ").append(usedString);
                information.append("/");
                information.append(Util.convertStorage(CloudFileUtil.MAX_DEFAULT_USER_CLOUD_SPACE));
            }
        } else
        {
            information.append(getString(R.string.no_network_eror));
        }

        helloUser.setText(information);
        userView.setOnClickListener(mButtonClick);
    }

    private void showErrorInformation(ErrorStatusType type)
    {
        View errorView = mRootView.findViewById(R.id.error_information_page);
        ImageView errorImage = (ImageView) errorView.findViewById(R.id.error_image);
        TextView prompt = (TextView) errorView.findViewById(R.id.error_prompt);
        Button loginBtn = (Button) errorView.findViewById(R.id.cloud_login_btn);
        TextView loginText = (TextView) errorView.findViewById(R.id.login_prompt);

        boolean needShowLogin = false;

        switch (type)
        {
            case ok:
                break;

            case no_service_sdk:
                prompt.setText(R.string.no_service_sdk);
                needShowLogin = false;
                break;

            case get_list_fail:
                prompt.setText(R.string.get_file_list_fail);
                needShowLogin = false;
                break;

            case sdcard_busy:
                errorImage.setImageResource(R.drawable.sd_not_available);
                prompt.setText(R.string.enable_sd_card);
                needShowLogin = false;
                break;

            case no_user_id: // Userinformation is initing...
                loginText.setText(R.string.no_user_id);
                errorImage.setImageResource(R.drawable.cloud_storage_icon);
                needShowLogin = true;
                break;

            case no_network:
                loginText.setText(R.string.no_network_eror);
                errorImage.setImageResource(R.drawable.cloud_storage_icon);
                needShowLogin = true;
                break;

            case user_logout:
                loginText.setText(R.string.cloud_login_prompt);
                errorImage.setImageResource(R.drawable.cloud_storage_icon);
                needShowLogin = true;
                break;

            case user_infor_error:
                loginText.setText(R.string.get_user_info_error);
                errorImage.setImageResource(R.drawable.cloud_storage_icon);
                needShowLogin = true;
                break;
        }

        if (needShowLogin)
        {
            prompt.setVisibility(View.GONE);
            errorImage.setVisibility(View.VISIBLE);
            loginText.setVisibility(View.VISIBLE);
            loginBtn.setVisibility(View.VISIBLE);
            loginBtn.setOnClickListener(mButtonClick);
        } else
        {
            prompt.setVisibility(View.VISIBLE);
            if (type == ErrorStatusType.sdcard_busy)
            {
                errorImage.setVisibility(View.VISIBLE);
            } else {
                errorImage.setVisibility(View.GONE);
            }
            loginText.setVisibility(View.GONE);
            loginBtn.setVisibility(View.GONE);
        }

        if (type == ErrorStatusType.ok)
        {
            errorView.setVisibility(View.GONE);
            mNavigationBar.setVisibility(View.VISIBLE);
            mFileListView.setVisibility(View.VISIBLE);
        } else
        {
            errorView.setVisibility(View.VISIBLE);
            mNavigationBar.setVisibility(View.GONE);
            mFileListView.setVisibility(View.GONE);
            showEmptyView(false);

            ActionMode mode = mActivity.getActionMode();
            if (mode != null && mActivity.getActionModeCallback() == mActionModeCallback)
            {
                mode.finish();
            }
        }
    }

    private void showProgressDialog(String title)
    {
        mProgressDialog = new ApeProgressDialog(mActivity);
        mProgressDialog.setTitle(title);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setButton(
                DialogInterface.BUTTON_NEUTRAL,
                getString(R.string.cancel),
                new CancelListener(mProgressDialog));
        mProgressDialog.show();
    }

    private void dismissProgressDialog()
    {
        if (mProgressDialog != null)
        {
            try
            {
                mProgressDialog.dismiss();
            } catch (Exception e)
            {
                MyLog.e(TAG, "dismissProgressDialog, error: " + e);
            }
            mProgressDialog = null;
        }
    }

    public void refreshFileList(String dir)
    {
        MyLog.i(TAG, "refreshFileList, dir:" + dir);
        if (mCloudFileService != null && mTransferService != null && mTransferService.isNetWorkConnect())
        {
            mCloudFileOperator.doListFiles(new ListFileListener(), dir, false);
        }
    }

    private void listFile(String dir)
    {
        MyLog.i(TAG, "listFile, dir:" + dir);
        if (mCloudFileService != null && mTransferService != null && mTransferService.isNetWorkConnect())
        {
            mCloudFileOperator.doListFiles(new ListFileListener(), dir, true);
        }
    }

    private void uploadFiles()
    {
        MyLog.i(TAG, "uploadFiles, userKey:" + mUserKey + ", size:" + mUploadFiles.size());
        if (mTransferService != null && mTransferService.isUserKeyValid()
                && mUploadFiles.size() > 0)
        {
            if (mTransferService.isWifiConnetion())
            {
                startToUpload();
            } else
            {
                createDialog(CloudFileUtil.DIALOG_ID_NO_WIFI, new DialogListener()
                {
                    @Override
                    public void onDialogConfirm()
                    {
                        startToUpload();
                    }

                    @Override
                    public void onDialogCancal()
                    {
                        endUploadFiles();
                    }
                });
            }
        }
    }
    
    private void startToUpload()
    {
        if (mIsMultiTransfer)
        {
            Intent intent = new Intent(mActivity, TransferFilesActivity.class);
            intent.putExtra(CloudFileUtil.FOCUS_TAB_INDEX, CloudFileUtil.UPLOAD_TAB_INDEX);
            intent.putExtra(CloudFileUtil.USER_KEY_TAG, mUserKey);
            intent.putExtra(CloudFileUtil.TRANSFER_DEST_PATH_TAG, mCurrentPath);
            ArrayList<String> uploadList = new ArrayList<String>();
            for (FileInfo file : mUploadFiles)
            {
                uploadList.add(file.filePath);
            }
            intent.putStringArrayListExtra(CloudFileUtil.TRANSFER_SRC_FILES_TAG, uploadList);
            startActivityForResult(intent, UPLOAD_REQUEST_CODE);
        } else
        {
            mCloudFileOperator.doUploadFiles(
                    new TransferFilesListener(R.string.operation_send_to_cloud),
                    mUploadFiles, mCurrentPath);
        }
    }
    
    private void endUploadFiles()
    {
        mUploadFiles.clear();
        mListDisplayMode = ListDisplayMode.normalMode;
        mUploadBtnView.setVisibility(View.GONE);
        updateUI();
    }
    
    @Override
    public boolean downloadFiles()
    {
        if (mAdapter.getCheckedFileCount() > 0)
        {
            ArrayList<CloudFile> list = mAdapter.getCheckedFiles();
            for (CloudFile file : list)
            {
                if (!file.isFile())
                {
                    AlertDialog dialog = new AlertDialog.Builder(mActivity)
                            .setMessage(R.string.nonsupport_download_folder)
                            .setPositiveButton(R.string.confirm, null)
                            .create();
                    dialog.show();
                    return false;
                }
            }
            mWorkFileList.clear();
            mWorkFileList.addAll(list);

            downloadFromWorkFileList();
            return true;
        }
        return false;
    }

    private void downloadFromWorkFileList()
    {
        if(mTransferService != null && mTransferService.isWifiConnetion())
        {
            startToDownload();
        } else
        {
            createDialog(CloudFileUtil.DIALOG_ID_NO_WIFI, new DialogListener()
            {
                @Override
                public void onDialogConfirm()
                {
                    startToDownload();
                }

                @Override
                public void onDialogCancal()
                {
                    mWorkFileList.clear();
                }
            });
        }
    }

    private void startToDownload()
    {
        if (mIsMultiTransfer)
        {
            Intent intent = new Intent(mActivity, TransferFilesActivity.class);
            intent.putExtra(CloudFileUtil.FOCUS_TAB_INDEX, CloudFileUtil.DOWNLOAD_TAB_INDEX);
            intent.putExtra(CloudFileUtil.USER_KEY_TAG, mUserKey);
            intent.putExtra(CloudFileUtil.TRANSFER_DEST_PATH_TAG, mDownLoadPath);
            ArrayList<String> downloadList = new ArrayList<String>();
            for (CloudFile file : mWorkFileList)
            {
                downloadList.add(file.getKey());
            }
            intent.putStringArrayListExtra(CloudFileUtil.TRANSFER_SRC_FILES_TAG, downloadList);
            startActivityForResult(intent, DOWNLOAD_REQUEST_CODE);
        } else
        {
            mCloudFileOperator.doDownLoadFiles(
                    new TransferFilesListener(R.string.menu_download_str),
                    mWorkFileList, mDownLoadPath);
        }
    }
    
    @Override
    public void deleteFiles()
    {
        if (mAdapter.getCheckedFileCount() > 0)
        {
            mWorkFileList.clear();
            mWorkFileList.addAll(mAdapter.getCheckedFiles());
            for (int index = 0; index < mWorkFileList.size(); index++)
            {
                if (mWorkFileList.get(index).getKey().equals(CloudFileUtil.CLOUD_GALLERY_BACKUP_DIR))
                {
                    String prompt = getString(R.string.system_folder_delete_fail, CloudFileUtil.CLOUD_GALLERY_BACKUP_DIR_NAME);
                    Toast.makeText(mActivity, prompt, Toast.LENGTH_SHORT).show();
                    mWorkFileList.remove(index);
                    break;
                }
            }

            if (mWorkFileList.size() > 0)
            {
                mCloudFileOperator.doDeleteFiles(new DeleteFilesListener(),
                        mWorkFileList);
            }
        }

        ActionMode mode = mActivity.getActionMode();
        if (mode != null)
        {
            mode.finish();
        }
    }

    private void createFolder()
    {
        TextInputDialog dialog = new TextInputDialog(mActivity,
                //mActivity.getString(R.string.operation_create_folder),
                mActivity.getString(R.string.operation_create_folder_message),
                null, //mActivity.getString(R.string.operation_create_folder_message),
                mActivity.getString(R.string.new_folder_name),
                new OnFinishListener()
                {
                    @Override
                    public boolean onFinish(String text)
                    {
                        if (mCloudFileOperator.isFileExist(mCurrentPath+text+File.separator))
                        {
                            mCloudFileOperator.showResult(OperationEventListener.ERROR_CODE_FILE_EXIST);
                        } else
                        {
                            mCloudFileOperator.doCreateDir(new SimpleListener(),
                                    mCurrentPath, text);
                        }
                        return true;
                    }
                });
        dialog.setCloudInput(true);
        dialog.show();
    }
    
    private void gotoTransferList()
    {
        Intent intent = new Intent();
        intent.setClass(mActivity, TransferFilesActivity.class);
        intent.putExtra(CloudFileUtil.FOCUS_TAB_INDEX, CloudFileUtil.UPLOAD_TAB_INDEX);
        intent.putExtra(CloudFileUtil.USER_KEY_TAG, mUserKey);
        startActivity(intent);
    }
    
    private void gotoLogin()
    {
        Intent intent = new Intent(CloudFileUtil.LOGIN_ACTIVITY_ACTION);
        try
        {
            if (mIsHasReferSDK)
            {
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
            }
        } catch (Exception e)
        {
            MyLog.e(TAG, "gotoLogin error, e:" + e);
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case LOGIN_REQUEST_CODE:
                getUserStatus();
                showUserInformation();
                break;

            case UPLOAD_REQUEST_CODE:
                endUploadFiles();
                break;

            case DOWNLOAD_REQUEST_CODE:
                mWorkFileList.clear();
                break;

            default:
                break;
        }
    }

    private boolean onOperationUpLevel()
    {
        if (mCurrentPath != null)
        {
            if (mCurrentPath.equals(mRoot))
            {
                return false;
            } else
            {
                mCurrentPath = new File(mCurrentPath).getParent();
                if (mCurrentPath == null || mCurrentPath.equals(File.separator))
                {
                    mCurrentPath = mRoot;
                } else if (!mCurrentPath.endsWith(File.separator))
                {
                    mCurrentPath += File.separator;
                }
                listFile(mCurrentPath);
                return true;
            }
        } else
        {
            return false;
        }
    }

    private boolean updateNavigationPane()
    {
        boolean isRoot = (mCurrentPath == null || mCurrentPath.equals(mRoot));

        View arrow = mNavigationBar.findViewById(R.id.path_pane_arrow);
        arrow.setVisibility(View.GONE); //(isRoot ? View.GONE : View.VISIBLE);
        //mUpLevelView.setVisibility(isRoot ?  View.GONE : View.VISIBLE);
        mUpLevelView.setVisibility(View.GONE);

        String cloudDir = mCurrentPath;
        if (mCurrentPath.endsWith("/"))
        {
            cloudDir = mCurrentPath.substring(0, mCurrentPath.length()-1);
        }
        mNavigationBarText.setText(cloudDir);

        return isRoot;
    }

    private void showEmptyView(boolean show)
    {
        View emptyView = mRootView.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    
//    private boolean isFragmentFocus()
//    {
//        int tabFocus = mActivity.getActionBar().getSelectedNavigationIndex();
//        return (tabFocus == Util.REMOTE_TAB_INDEX);
//    }

    private boolean mIsTaskRunning = false;
    private boolean mIsLoadingFileList = false;
    abstract class CloudFileListener implements OperationEventListener
    {

        @Override
        public void onTaskPrepare()
        {
//            if (isFragmentFocus())
//            {
//                setHasOptionsMenu(false);
//            }
            mIsTaskRunning = true;
            mUploadBtnView.findViewById(R.id.button_upload_confirm).setEnabled(false);
            mUploadBtnView.findViewById(R.id.button_upload_cancel).setEnabled(false);
        }

        @Override
        public void onTaskResult(int result)
        {
//            if (isFragmentFocus())
//            {
//                setHasOptionsMenu(true);
//            }
            mIsTaskRunning = false;
            mUploadBtnView.findViewById(R.id.button_upload_confirm).setEnabled(true);
            mUploadBtnView.findViewById(R.id.button_upload_cancel).setEnabled(true);
            
            if ((result != OperationEventListener.ERROR_CODE_SUCCESS)
                    || !(this instanceof ListFileListener))
            {
                mCloudFileOperator.showResult(result);
            }
        }
        
    }

    private class ListFileListener extends CloudFileListener
    {
        private static final int NEED_PROGRESS_TIME = 500;
        List<CloudFile> mList;
        View mProgress;
        Runnable mDelayDisplay;

        public ListFileListener()
        {
            mDelayDisplay = new Runnable()
            {
                
                @Override
                public void run()
                {
                    //mProgress.setVisibility(View.VISIBLE);
                    //mFileListView.setVisibility(View.GONE);
                    mFileListView.setRefreshing(false);
                }
            };
        }

        @Override
        public void onTaskPrepare()
        {
            super.onTaskPrepare();

            mIsLoadingFileList = true;
            showErrorInformation(ErrorStatusType.ok);
            mProgress =  mRootView.findViewById(R.id.load_file_list_progress);
            mProgress.postDelayed(mDelayDisplay, NEED_PROGRESS_TIME);
            //mFileNameList.clear();
            
            showEmptyView(false);
            //updateNavigationPane();
            showUserInformation();

            //mAdapter.clearCheckedFiles();
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
//            mFileNameList.clear();
//
//            List<CloudFile> list = progress.getCloudFiles();
//            if (list != null && list.size() > 0)
//            {
//                mFileNameList.addAll(list);
//            }
            mList = progress.getCloudFiles();
        }

        @Override
        public void onTaskResult(int result)
        {
            super.onTaskResult(result);

            mProgress.removeCallbacks(mDelayDisplay);
            mProgress.setVisibility(View.GONE);
            if (checkUserInforError() != ErrorStatusType.ok)
            {
                mFileListView.setVisibility(View.GONE);
            } else if (result == OperationEventListener.ERROR_CODE_SUCCESS)
            {
                mFileNameList.clear();
                if (mList != null)
                {
                    mFileNameList.addAll(mList);
                }

                Collections.sort(mFileNameList, mSortHelper.getComparator());
                mFileListView.setVisibility(View.VISIBLE);
                mAdapter.notifyDataSetChanged();
                
                showEmptyView(Util.isSDCardReady() && mFileNameList.size()==0);
            } else 
            {
                showErrorInformation(ErrorStatusType.get_list_fail);
                mCloudFileOperator.showResult(result);
            }

            mIsLoadingFileList = false;
            mFileListView.onRefreshComplete();
            onPrepareButtomActionBar();
        }
        
    }
    
    private boolean updateApeProgress(ProgressInfo progress)
    {
        if (progress != null && mProgressDialog != null)
        {
            mProgressDialog.setMax((int) progress.getTotal());
            mProgressDialog.setProgress(progress.getProgeress());
            mProgressDialog.setCurrentNumber(progress.getCurrentNumber());
            mProgressDialog.setTotalNumber(progress.getTotalNumber());
            String msg = progress.getUpdateInfo();
            if (msg != null)
            {
                mProgressDialog.setMessage(msg);
            }
            
            MyLog.i(TAG, "UploadFilesListener, max:" + progress.getTotal()
                    + ", progress: " + progress.getProgeress());
            return true;
        }
        
        return false;
    }

    private class TransferFilesListener extends CloudFileListener
    {
        private int titleId;

        public TransferFilesListener(int titleId)
        {
            this.titleId = titleId;
        }

        @Override
        public void onTaskPrepare()
        {
            super.onTaskPrepare();
            showProgressDialog(getString(titleId));
            mProgressDialog.setMessage(getString(R.string.create_transfer_connection));
            mProgressDialog.setTotalNumber(mUploadFiles.size());
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
            updateApeProgress(progress);
        }

        @Override
        public void onTaskResult(int result)
        {
            super.onTaskResult(result);

            dismissProgressDialog();
            if (titleId == R.string.menu_download_str)
            {
                mActivity.switchToPage(Util.SDCARD_TAB_INDEX);
                FileViewActivity fv = (FileViewActivity) mActivity.getFragment(Util.SDCARD_TAB_INDEX);
                fv.setPath(mDownLoadPath);
            } else
            {
                endUploadFiles();
            }
        }
        
    }

    private class DeleteFilesListener extends CloudFileListener
    {
        @Override
        public void onTaskPrepare()
        {
            super.onTaskPrepare();
            
            showProgressDialog(getString(R.string.operation_deleting));
            mProgressDialog.setTotalNumber(mWorkFileList.size());
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
            updateApeProgress(progress);
        }

        @Override
        public void onTaskResult(int result)
        {
            super.onTaskResult(result);
            
            mWorkFileList.clear();
            mAdapter.clearCheckedFiles();
            dismissProgressDialog();
            sendMsgToUpdateUi();
        }

    }
    
    private class SimpleListener extends CloudFileListener
    {
        ProgressDialog myProgress;

        @Override
        public void onTaskPrepare()
        {
            super.onTaskPrepare();

            myProgress = new ProgressDialog(mActivity);
            myProgress.setMessage(getString(R.string.wait));
            myProgress.setCancelable(false);
            myProgress.setIndeterminate(true);
            myProgress.setButton(
                    DialogInterface.BUTTON_NEUTRAL,
                    getString(R.string.cancel),
                    new CancelListener(myProgress));
            myProgress.show();
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
            //updateApeProgress(progress);
        }

        @Override
        public void onTaskResult(int result)
        {
            super.onTaskResult(result);
            
            if (myProgress != null)
            {
                try
                {
                    myProgress.dismiss();
                } catch (Exception e)
                {
                    MyLog.e(TAG, "myProgress.dismiss, e:" + e);
                }
                myProgress = null;
            }
            sendMsgToUpdateUi();

            mWorkFileList.clear();
            mAdapter.clearCheckedFiles();
        }

    }
    
    private class CancelListener implements DialogInterface.OnClickListener
    {
        private AlertDialog mDialog;

        public CancelListener(AlertDialog dialog)
        {
            mDialog = dialog;
        }

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            if (which == DialogInterface.BUTTON_NEUTRAL)
            {
                mDialog.setMessage(getString(R.string.wait));
                mDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.INVISIBLE);
                mCloudFileOperator.cancelCurrentTask();
            }
        }
        
    }

    private boolean checkAndPromptBeforeOperation()
    {
        boolean result = true;
        String errorString = null;

        if (mTransferService == null)
            return result;

        if (!mTransferService.isNetWorkConnect())
        {
            errorString = getString(R.string.network_connect_error);
        } else if (!mIsHasReferSDK)
        {
            errorString = getString(R.string.no_service_sdk);
        } else if (!mTransferService.isUserLogin())
        {
            errorString = getString(R.string.user_logout);
        } else if (!mTransferService.isUserInfomationValid())
        {
            errorString = getString(R.string.get_user_info_error);
        }

        if (errorString != null)
        {
            Toast.makeText(mActivity, errorString, Toast.LENGTH_SHORT).show();
            result = false;
        }

        return result;
    }

    private void gotoCloudSearch()
    {
        if (checkAndPromptBeforeOperation())
        {
            Intent intent = new Intent(mActivity, CloudFileSearchActivity.class);
            intent.putExtra(CloudFileSearchActivity.CURRENT_PATH, mCurrentPath);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private OnRefreshListener<ListView> mPullRefreshListener = new OnRefreshListener<ListView>()
    {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> arg0)
        {
            if (checkAndPromptBeforeOperation())
            {
                MyLog.i(TAG, "mPullRefreshListener, before refreshFileList");
                if (!mIsLoadingFileList)
                    refreshFileList(mCurrentPath);
            } else
            {
                mHandler.sendEmptyMessage(CloudFileUtil.MSG_REFRESHED_LIST);
            }
        }
    };
    
    private void startAndBindService()
    {
        Intent intent = new Intent(mActivity, TransferService.class);
        mActivity.startService(intent);
        mActivity.bindService(intent, mServiceCon, Service.BIND_AUTO_CREATE);
    }

    private void stopAndUnbindService()
    {
        mActivity.unbindService(mServiceCon);
        
        if (mTransferService != null)
        {
            mTransferService.unregisterServerInforListerner(mServiceListener);
            if (!mTransferService.isUnFinished())
            {
                mTransferService.reset();
                mActivity.stopService(new Intent(mActivity, TransferService.class));
            }
        }
    }
    
    private ServiceConnection mServiceCon = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mTransferService = ((TransferService.ServiceBinder) service).getService();
            if (mTransferService == null)
            {
                mActivity.finish();
                Toast.makeText(mActivity, "Cannot start TransferService.", Toast.LENGTH_LONG).show();
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
            mUserKey = mTransferService.getUserKey();
            mUserName = mTransferService.getUserName();
            init();
            updateUI();
        }

        @Override
        public void userKeyFinish()
        {
            mUserKey = mTransferService.getUserKey();
            mUserName = mTransferService.getUserName();
            init();
            updateUI();
        }

        @Override
        public void userInformationChanged()
        {
            mUserKey = mTransferService.getUserKey();
            mUserName = mTransferService.getUserName();
            mCloudFileOperator.doClearCache();
            init();
            updateUI();
        }

        @Override
        public void userInformationError(int errorCode)
        {
            updateUI();
        }

        @Override
        public boolean userLogout()
        {
            updateUI();
            return false;
        }

        @Override
        public void networkConnect()
        {
            sendMsgToUpdateUi();
        }

        @Override
        public void networkDisconnect()
        {
            sendMsgToUpdateUi();
        }

        @Override
        public void storageUnmount()
        {
            sendMsgToUpdateUi();
        }

        @Override
        public void storageMount()
        {
            sendMsgToUpdateUi();
        }
    };
    
    private void getUserStatus()
    {
        if (mTransferService != null)
        {
            CloudUserInfomation userInfo = mTransferService.getUserInfomation();
            if (mTransferService != null && userInfo != null && !userInfo.isLoading())
            {
                userInfo.goToGetUserKey();
            }
        }
    }

    public boolean checkReferSDK()
    {
        PackageManager pm = mActivity.getPackageManager();
        List<ApplicationInfo> allAppInfoList = pm.getInstalledApplications(0);

        for (ApplicationInfo appInfo : allAppInfoList)
        {
            if (appInfo.packageName.equalsIgnoreCase(CloudFileUtil.REFER_APK))
            {
                return true;
            }
        }
        return false;
    }

    private void sendMsgToUpdateUi()
    {
        Message msg = mHandler.obtainMessage();
        msg.what = CloudFileUtil.MSG_UPDATE_UI;
        mHandler.sendMessage(msg);
    }
}

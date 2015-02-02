
package com.ape.filemanager;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ape.cloudfile.widget.BottomActionBar;
import com.ape.filemanager.FavoriteDatabaseHelper.FavoriteDatabaseListener;
import com.ape.filemanager.FileCategoryHelper.CategoryInfo;
import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.ape.filemanager.FileExplorerTabActivity.IBackPressedListener;
import com.ape.filemanager.FileViewInteractionHub.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

@SuppressLint({ "UseSparseArrays", "HandlerLeak" })
public class FileCategoryActivityMyOS extends Fragment implements IFileInteractionListener,
        FavoriteDatabaseListener, IBackPressedListener {

    public static final String EXT_FILETER_KEY = "ext_filter";

    private static final String LOG_TAG = "FileCategoryActivity";

    private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();

    private FileListCursorAdapter mAdapter;

    private FileViewInteractionHub mFileViewInteractionHub;

    private FileCategoryHelper mFileCagetoryHelper;

    private FileIconHelper mFileIconHelper;

    private ScannerReceiver mScannerReceiver;

    private FavoriteList mFavoriteList;

    private ViewPage curViewPage = ViewPage.Invalid;

    private ViewPage preViewPage = ViewPage.Invalid;

    private FileExplorerTabActivity mActivity;

    private View mRootView;
    private LinearLayout mSearchView;

    private FileViewActivity mFileViewActivity;
    
    private MountPointManager mMountPointManager;
    
    private boolean mConfigurationChanged = false;

    private PullToRefreshListView mPullRefreshListView;
    private ListView mFileListView;
    private BottomActionBar mBottomActionBar;

    public void setConfigurationChanged(boolean changed) {
        mConfigurationChanged = changed;
    }

    static {
        button2Category.put(R.id.category_music, FileCategory.Music);
        button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_picture, FileCategory.Picture);
        //button2Category.put(R.id.category_theme, FileCategory.Theme);
        button2Category.put(R.id.category_document, FileCategory.Doc);
        button2Category.put(R.id.category_zip, FileCategory.Zip);
        button2Category.put(R.id.category_apk, FileCategory.Apk);
        button2Category.put(R.id.category_favorite, FileCategory.Favorite);
    }

    private FileCategory mListCategories[] = {
            FileCategory.Picture, FileCategory.Music,
            FileCategory.Video, FileCategory.Doc,
            FileCategory.Apk, FileCategory.Zip};
    private GridView mCategoryList;
    private CategoryGridAdapter mCategoryAdapter;
    
    private ListView mSdCapacityList;
    private SdCapacityListAdapter mSdCapacityListAdapter;
    private List<MountFileInfo> mListSdData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = (FileExplorerTabActivity) getActivity();
        mMountPointManager = mActivity.getMountManager();
        mFileViewActivity = (FileViewActivity) mActivity.getFragment(Util.SDCARD_TAB_INDEX);
        mRootView = inflater.inflate(R.layout.file_explorer_category_myos, container, false);
        setFavoriteView();

        mPullRefreshListView = (PullToRefreshListView) mRootView.findViewById(R.id.file_path_list);
        mPullRefreshListView.setOnRefreshListener(mPullRefreshListener);
        mPullRefreshListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
//        ListView fileListView = (ListView) mRootView.findViewById(R.id.file_path_list);
//        fileListView.setAdapter(mAdapter);
        mFileListView = mPullRefreshListView.getRefreshableView();

        curViewPage = ViewPage.Invalid;
        mFileViewInteractionHub = new FileViewInteractionHub(this);
        mFileViewInteractionHub.setIsPullToRefreshList(true);
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)
                && (action.equals(Intent.ACTION_PICK) || action.equals(Intent.ACTION_GET_CONTENT)
                        || action.equals("com.mediatek.filemanager.ADD_FILE")))
        {
            mFileViewInteractionHub.setMode(Mode.Pick);
        } else {
            mFileViewInteractionHub.setMode(Mode.View);
        }
        mFileViewInteractionHub.setRootPath("/");
        mFileIconHelper = new FileIconHelper(mActivity);
        mFavoriteList = new FavoriteList(mActivity, (ListView) mRootView.findViewById(R.id.favorite_list), this, mFileIconHelper);
        //mFavoriteList.initList();

        mAdapter = new FileListCursorAdapter(mActivity, null, mFileViewInteractionHub, mFileIconHelper);
        mFileListView.setAdapter(mAdapter);

        setSearchUI();

        mBottomActionBar = (BottomActionBar) mRootView.findViewById(R.id.bottom_aciton_bar);
        if (mBottomActionBar != null)
        {
            mFileViewInteractionHub.onCreateOptionsMenu(null);
        }

        mFileCagetoryHelper = new FileCategoryHelper(mActivity);
        initCategoryListView();
        initSdCapacityListView();
        //updateUI();
        registerScannerReceiver();

        return mRootView;
    }

    OnRefreshListener<ListView> mPullRefreshListener = new OnRefreshListener<ListView>()
    {
        @Override
        public void onRefresh(PullToRefreshBase<ListView> refreshView)
        {
            mHandler.removeMessages(MSG_PULL_TO_REFRESH);
            mHandler.sendEmptyMessage(MSG_PULL_TO_REFRESH);
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();

        if (!isAdded() || mFileViewInteractionHub.isInProgress())
        {
            return;
        }

        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) {
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
            refreshCategoryInfo();
            // refresh file list
            mFileViewInteractionHub.refreshCheckFileList();
        } else {
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }

        //mSearchView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
    }

    private void setSearchUI()
    {
        mSearchView = (LinearLayout) mRootView.findViewById(R.id.search_view_frame);
        mSearchView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (v.getId() == R.id.search_view_frame)
                {
                    gotoSearchActivity();
                }
            }
        });
        mSearchView.setVisibility(View.GONE);
    }
    
    private void setFavoriteView()
    {
        View extras = mRootView.findViewById(R.id.extra_categories);
        View favorite = mRootView.findViewById(R.id.category_favorite);
        int visibility = (OptionsUtils.isHaveFavorite(mActivity)) ? View.VISIBLE : View.GONE;

    	if (extras != null)
    	{
    		extras.setVisibility(visibility);
    	}
    	if (favorite != null)
    	{
    		favorite.setVisibility(visibility);
    	}
    }

    private void registerScannerReceiver() {
        mScannerReceiver = new ScannerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(GlobalConsts.ACTION_SCAN_FILES_COMPLETED);
        intentFilter.addDataScheme("file");
        mActivity.registerReceiver(mScannerReceiver, intentFilter);
    }

    public void refreshCategoryInfo()
    {
        new Thread()
        {
            @Override
            public void run()
            {
                mFileCagetoryHelper.refreshCategoryInfo();
                mHandler.sendEmptyMessage(MSG_CATEGORY_INFO_UPDATE);
            };
        }.start();
        //mCategoryAdapter.notifyDataSetChanged();

        mListSdData = mMountPointManager.getMountPointFileInfo();
        mSdCapacityListAdapter.notifyDataSetChanged();
    }

    public enum ViewPage {
        Home, Favorite, Category, NoSD, Invalid
    }

    private void showPage(ViewPage p) {
        if (curViewPage == p) return;

        curViewPage = p;

        showView(R.id.file_path_list, false);
        showView(R.id.navigation_bar, false);
        showView(R.id.category_page, false);
        showView(R.id.operation_bar, false);
        showView(R.id.sd_not_available_page, false);
        mFavoriteList.show(false);
        showEmptyView(false);
        if (mActivity.isMyOsOptionMenuStyle())
        {
            mSearchView.setVisibility(View.GONE);
        }
        if (mBottomActionBar != null)
        {
            if (p == ViewPage.Home || p == ViewPage.NoSD)
            {
                mBottomActionBar.setVisibility(View.GONE);
            } else
            {
                mBottomActionBar.setVisibility(View.VISIBLE);
                mHandler.removeMessages(MSG_BUTTOM_BAR_UPDATE);
                mHandler.sendEmptyMessage(MSG_BUTTOM_BAR_UPDATE);
            }
        }

        switch (p) {
            case Home:
                showView(R.id.category_page, true);
                mFileCagetoryHelper.setCurCategory(FileCategory.All);
                if (mConfigurationChanged) {
                    ((FileExplorerTabActivity) mActivity).reInstantiateCategoryTab();
                    mConfigurationChanged = false;
                }
                if (!mActivity.isMyOsOptionMenuStyle())
                {
                    mSearchView.setVisibility(View.GONE);
                }
                break;
            case Favorite:
                showView(R.id.navigation_bar, true);
                mFavoriteList.show(true);
                showEmptyView(mFavoriteList.getCount() == 0);
                if (!mActivity.isMyOsOptionMenuStyle())
                {
                    mSearchView.setVisibility(View.VISIBLE);
                }
                break;
            case Category:
                showView(R.id.navigation_bar, true);
                showView(R.id.file_path_list, true);
                showEmptyView(mAdapter.getCount() == 0);
                if (!mActivity.isMyOsOptionMenuStyle())
                {
                    mSearchView.setVisibility(View.VISIBLE);
                }
                break;
            case NoSD:
                showView(R.id.sd_not_available_page, true);
                if (!mActivity.isMyOsOptionMenuStyle())
                {
                    mSearchView.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void showEmptyView(boolean show) {
        View emptyView = mActivity.findViewById(R.id.empty_view);
        if (emptyView != null)
            emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showView(int id, boolean show) {
        View view = mRootView.findViewById(id);
        if (view != null) {
            view.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());
            if (f != null) {
                onCategorySelected(f);
                if (f != FileCategory.Favorite) {
                    setHasOptionsMenu(true);
                }
            }
        }

    };

    private void onCategorySelected(FileCategory f) {
        if (mFileCagetoryHelper.getCurCategory() != f) {
            mFileCagetoryHelper.setCurCategory(f);
            mFileViewInteractionHub.setCurrentPath(mFileViewInteractionHub.getRootPath()
                    + getString(mFileCagetoryHelper.getCurCategoryNameResId()));
            mFileViewInteractionHub.refreshFileList();
        }

        if (f == FileCategory.Favorite) {
            showPage(ViewPage.Favorite);
        } else {
            showPage(ViewPage.Category);
        }
    }

    @Override
    public boolean onBack() {
        if (isHomePage() || curViewPage == ViewPage.NoSD || mFileViewInteractionHub == null) {
            return false;
        }

        return mFileViewInteractionHub.onBackPressed();
    }

    public boolean isHomePage() {
        return curViewPage == ViewPage.Home;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (curViewPage != ViewPage.Category && curViewPage != ViewPage.Favorite) {
            return;
        }

        mFileViewInteractionHub.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu == null)
            return;

        if (!isHomePage() && mFileCagetoryHelper.getCurCategory() != FileCategory.Favorite) {
            mFileViewInteractionHub.onPrepareOptionsMenu(menu);
        }
    }

    public boolean onRefreshFileList(String path, FileSortHelper sort) {
        int oldItemCount = getItemCount();
        FileCategory curCategory = mFileCagetoryHelper.getCurCategory();
        if (curCategory == FileCategory.Favorite || curCategory == FileCategory.All)
            return false;

        Cursor c = mFileCagetoryHelper.query(curCategory, sort.getSortMethod());
        showEmptyView(c == null || c.getCount() == 0);
        mAdapter.changeCursor(c);

        if (oldItemCount != getItemCount())
        {
            if (mBottomActionBar != null)
            {
                mHandler.removeMessages(MSG_BUTTOM_BAR_UPDATE);
                mHandler.sendEmptyMessage(MSG_BUTTOM_BAR_UPDATE);
            }
            else
            {
                mActivity.invalidateOptionsMenu();
            }
        }
        mPullRefreshListView.onRefreshComplete();
        return true;
    }

    @Override
    public View getViewById(int id) {
        return mRootView.findViewById(id);
    }

    @Override
    public Context getContext() {
        return mActivity;
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mFavoriteList.getArrayAdapter().notifyDataSetChanged();
                showEmptyView(mAdapter.getCount() == 0);
            }

        });
    }

    @Override
    public void onPick(FileInfo f) {
        try {
            Intent intent = Intent.parseUri(Uri.fromFile(new File(f.filePath)).toString(), 0);
            mActivity.setResult(Activity.RESULT_OK, intent);
            mActivity.finish();
            return;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean shouldShowOperationPane() {
        return true;
    }

    @Override
    public boolean onOperation(int id) {
        switch (id) {
            case R.id.button_operation_copy:
            case GlobalConsts.MENU_COPY:
                mFileViewInteractionHub.addContextMenuSelectedItem();
                copyFileInFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case R.id.button_operation_move:
            case GlobalConsts.MENU_MOVE:
                mFileViewInteractionHub.addContextMenuSelectedItem();
                startMoveToFileView(mFileViewInteractionHub.getSelectedFileList());
                mFileViewInteractionHub.clearSelection();
                break;
            case GlobalConsts.OPERATION_UP_LEVEL:
                setHasOptionsMenu(false);
                showPage(ViewPage.Home);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public String getDisplayPath(String path) {
        return getString(R.string.tab_category) + path;
    }

    @Override
    public String getRealPath(String displayPath) {
        return "";
    }

    @Override
    public boolean onNavigation(String path) {
        showPage(ViewPage.Home);
        setHasOptionsMenu(false); // MOMO-92
        return true;
    }

    @Override
    public boolean shouldHideMenu(int menu) {
        return (menu == GlobalConsts.MENU_NEW_FOLDER || menu == GlobalConsts.MENU_FAVORITE
                || menu == GlobalConsts.MENU_PASTE || menu == GlobalConsts.MENU_SHOWHIDE);
    }

    @Override
    public void addSingleFile(FileInfo file) {
        refreshList();
    }

    @Override
    public Collection<FileInfo> getAllFiles() {
        return mAdapter.getAllFiles();
    }

    @Override
    public FileInfo getItem(int pos) {
        return mAdapter.getFileItem(pos);
    }

    @Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

    @Override
    public void sortCurrentList(FileSortHelper sort) {
        refreshList();
    }

    private void refreshList() {
        mFileViewInteractionHub.refreshFileList();
    }

    private void copyFileInFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.copyFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    private void startMoveToFileView(ArrayList<FileInfo> files) {
        if (files.size() == 0) return;
        mFileViewActivity.moveToFile(files);
        mActivity.getActionBar().setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
    }

    @Override
    public FileIconHelper getFileIconHelper() {
        return mFileIconHelper;
    }

    public void onDestroy() {
        if (mAdapter != null && mAdapter.getCursor() != null) {
            mAdapter.getCursor().close();
        }

        if (mActivity != null) {
            mActivity.unregisterReceiver(mScannerReceiver);
        }
        
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    private class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(LOG_TAG, "received broadcast: " + action.toString() + ", data:"+ intent.getData());
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED))
            {
                mMountPointManager.init(mActivity);
                if (mBottomActionBar != null)
                    mFileViewInteractionHub.onPrepareButtomActionBar();
                else
                    mActivity.invalidateOptionsMenu();

                ActionMode mode = mActivity.getActionMode();
                if (mode != null)
                {
                    mode.finish();
                }
            }

            // handle intents related to external storage
            if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)
                    || action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)
                    || action.equals(GlobalConsts.ACTION_SCAN_FILES_COMPLETED)) {
                notifyFileChanged();
            }
        }
    }

    private void updateUI() {
        if (!isAdded() || mFileViewInteractionHub.isInProgress())
        {
            return;
        }

        boolean sdCardReady = Util.isSDCardReady();
        if (sdCardReady) {
            if (preViewPage != ViewPage.Invalid) {
                showPage(preViewPage);
                preViewPage = ViewPage.Invalid;
            } else if (curViewPage == ViewPage.Invalid || curViewPage == ViewPage.NoSD) {
                showPage(ViewPage.Home);
            }
            refreshCategoryInfo();
            // refresh file list
            mFileViewInteractionHub.refreshCheckFileList();
            // refresh file list view in another tab
            mFileViewActivity.refresh();

        } else {
            preViewPage = curViewPage;
            showPage(ViewPage.NoSD);
        }

        //mSearchView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
    }

    // process file changed notification, using a timer to avoid frequent
    // refreshing due to batch changing on file system
    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                mHandler.sendMessage(message);
            }

        }, 1000);
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;
    private static final int MSG_CATEGORY_INFO_UPDATE = 101;
    private static final int MSG_BUTTOM_BAR_UPDATE = 102;
    private static final int MSG_PULL_TO_REFRESH = 103;

    private Timer timer;

    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
                    
                case MSG_CATEGORY_INFO_UPDATE:
                    mCategoryAdapter.notifyDataSetChanged();
                    break;
                    
                case MSG_BUTTOM_BAR_UPDATE:
                    if (mBottomActionBar != null)
                    {
                        mFileViewInteractionHub.onPrepareButtomActionBar();
                    }
                case MSG_PULL_TO_REFRESH:
                    mFileViewInteractionHub.onOperationReferesh();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    // update the count of favorite
    @Override
    public void onFavoriteDatabaseChanged() {
        //setCategoryCount(FileCategory.Favorite, mFavoriteList.getCount());
    }

    @Override
    public void runOnUiThread(Runnable r) {
        mActivity.runOnUiThread(r);
    }

    private void initCategoryListView()
    {
        mCategoryAdapter = new CategoryGridAdapter(mActivity);
        mCategoryList = (GridView) mRootView.findViewById(R.id.category_buttons_grid);
        mCategoryList.setAdapter(mCategoryAdapter);
        mCategoryList.setOnItemClickListener(new OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id)
            {
                FileCategory f = mListCategories[position];
                if (f != null)
                {
                    onCategorySelected(f);
                    if (f != FileCategory.Favorite)
                    {
                        setHasOptionsMenu(true);
                    }
                }
            }
        });
    }

    private class CategoryGridAdapter extends BaseAdapter
    {
        private int mColumnCount;
        private LayoutInflater mInflater;

        public CategoryGridAdapter(Context context)
        {
            mInflater = LayoutInflater.from(context);
            mColumnCount = context.getResources().getInteger(R.integer.category_numColumns);
        }

        @Override
        public int getCount()
        {
            return mListCategories.length;
        }

        @Override
        public Object getItem(int position)
        {
            return mListCategories[position];
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            FileCategory category = mListCategories[position];

            View view = null;
            if (convertView == null)
            {
                view = mInflater.inflate(R.layout.category_item_layout, parent, false);
            } else
            {
                view = convertView;
            }

            ImageView icon = (ImageView) view.findViewById(R.id.category_icon);
            icon.setImageResource(mFileCagetoryHelper.getCategoryIconId(category));
            TextView categoryText = (TextView) view.findViewById(R.id.category_text);
            categoryText.setText(mFileCagetoryHelper.getCategoryNameId(category));

            CategoryInfo categoryInfo = mFileCagetoryHelper.getCategoryInfo(category);
            TextView categoryCount = (TextView) view.findViewById(R.id.category_count);
            categoryCount.setText("(" + categoryInfo.count + ")");
            TextView categorySize = (TextView) view.findViewById(R.id.category_size);
            categorySize.setText(Util.convertStorage(categoryInfo.size));

            // To show line
            int maxLine = ((getCount()+1) / mColumnCount);
            View horizontalLine = view.findViewById(R.id.bottom_horizontal_line);
            View verticalLine = view.findViewById(R.id.right_vertical_line);
            if (((position + 1) % mColumnCount) == 0)
            {
                verticalLine.setVisibility(View.GONE);
            } else
            {
                if (position < mColumnCount) // first row
                {
                    verticalLine.setBackgroundResource(R.drawable.grid_line_vertical_up);
                } else if (position >= (maxLine - 1) * mColumnCount)
                {
                    verticalLine.setBackgroundResource(R.drawable.grid_line_vertical_down);
                } else
                {
                    verticalLine.setBackgroundResource(R.drawable.grid_line_vertical_middle);
                }
                verticalLine.setVisibility(View.VISIBLE);
            }
            if (position >= (maxLine - 1) * mColumnCount)
            {
                horizontalLine.setVisibility(View.GONE);
            } else
            {
                if ((position % 2) == 0)
                {
                    horizontalLine.setBackgroundResource(R.drawable.grid_line_horizontal_left);
                } else
                {
                    horizontalLine.setBackgroundResource(R.drawable.grid_line_horizontal_right);
                }
                horizontalLine.setVisibility(View.VISIBLE);
            }
            return view;
        }
    }

    private void initSdCapacityListView()
    {
        mListSdData = mMountPointManager.getMountPointFileInfo();
        mSdCapacityListAdapter = new SdCapacityListAdapter(mActivity);
        mSdCapacityList = (ListView)  mRootView.findViewById(R.id.sd_capacity_info_list);
        mSdCapacityList.setAdapter(mSdCapacityListAdapter);
    }
    
    private class SdCapacityListAdapter extends BaseAdapter
    {
        private LayoutInflater mInflater;

        public SdCapacityListAdapter(Context context)
        {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount()
        {
            return mListSdData.size();
        }

        @Override
        public Object getItem(int position)
        {
            return mListSdData.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            MountFileInfo sdInfo = mListSdData.get(position);

            View view = null;
            if (convertView == null)
            {
                view = mInflater.inflate(R.layout.category_sd_capacity_item, parent, false);
            } else
            {
                view = convertView;
            }

            CategoryBar bar = (CategoryBar) view.findViewById(R.id.category_bar);
            bar.setFullValue(sdInfo.totalSpace);
            long otherSize = sdInfo.totalSpace - sdInfo.freeSpace;
            bar.addCategory(R.drawable.category_bar_other);
            bar.setCategoryValue(0, otherSize);
            bar.startAnimation();

            StringBuilder capacityStr = new StringBuilder(sdInfo.displayName);
            capacityStr.append(": ");
            capacityStr.append(getString(R.string.sd_card_size, Util.convertStorage(sdInfo.totalSpace)));
            capacityStr.append("  ");
            capacityStr.append(getString(R.string.sd_card_available, Util.convertStorage(sdInfo.freeSpace)));
            TextView tv = (TextView) view.findViewById(R.id.sd_card_capacity);
            tv.setText(capacityStr.toString());

            return view;
        }
    }

    @Override
    public void setHasOptionsMenu(boolean hasMenu)
    {
        if (mBottomActionBar != null)
        {
            super.setHasOptionsMenu(false);
            mBottomActionBar.setVisibility(hasMenu ? View.VISIBLE : View.GONE);
        } else
        {
            super.setHasOptionsMenu(hasMenu);
        }
    }

    @Override
    public void gotoSearchActivity()
    {
        if (Util.isSDCardReady())
        {
            mActivity.startSearchActivity(Util.getTinnoRootPath());
        }
    }

    @Override
    public ListView getFileListView()
    {
        return mFileListView;
    }
}

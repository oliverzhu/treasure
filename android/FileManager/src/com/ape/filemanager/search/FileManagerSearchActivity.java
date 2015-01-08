package com.ape.filemanager.search;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.FileExplorerTabActivity;
import com.ape.filemanager.FileIconHelper;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.FileListItem;
import com.ape.filemanager.IntentBuilder;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;

public class FileManagerSearchActivity extends Activity implements
        OnItemClickListener
{
    private static final String TAG = "FileManagerSearchActivity";
    private TextView mResultView = null;
    private String mSearchPath = null;
    private SearchView mSearchView = null;
    private ImageView mSearchBg = null;

    private MenuItem mSearchItem;
    private long mTotal = 0;
    private long mSearchTotal = 0;
    private String mSearchText = null;
    private boolean mIsFromFileManger;

    public static final String CURRENT_PATH = "current_path";
    public static final String SEARCH_TEXT = "search_text";
    public static final String SEARCH_TOTAL = "search_total";
    public static final String IS_FROM_FILEMANAGER = "is_from_fileManger";

    private ListView mListView;
    private SearchListAdapter mAdapter;
    private ArrayList<FileInfo> mFileInfoList;
    private FileIconHelper mFileIconHelper;

    private boolean mIsReturnBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTheme(R.style.FileManagerOperTheme);
        setContentView(R.layout.search_main);
        mResultView = (TextView) findViewById(R.id.search_result);
        mSearchBg = (ImageView) findViewById(R.id.search_bg);

        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mFileInfoList = new ArrayList<FileInfo>();
        mFileIconHelper = new FileIconHelper(this);
        mAdapter = new SearchListAdapter(this, R.layout.file_browser_item,
                mFileInfoList, mFileIconHelper);
        mListView.setAdapter(mAdapter);

        initSearchView();
        parseIntent();
    }
    
    private void parseIntent()
    {
        Intent intent = getIntent();
        
        mIsFromFileManger = getIntent().getBooleanExtra(IS_FROM_FILEMANAGER, false);
        mSearchPath = intent.getStringExtra(CURRENT_PATH);
        if (mSearchPath == null)
        {
            mSearchPath = Util.getTinnoRootPath();
        }
        
        handleIntent(intent);
        Log.i(TAG, "onCreate, intent:" + intent);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (mIsReturnBack && !TextUtils.isEmpty(mSearchText))
        {
            requestSearch(mSearchText);
        }
        mIsReturnBack = false;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        handleIntent(intent);
    }

    private void initSearchView()
    {
        ActionBar bar = getActionBar();
        if (bar != null)
            bar.hide();

        ImageView titleIcon = (ImageView) findViewById(R.id.title_icon);
        try
        {
            PackageManager pm = getPackageManager();
            PackageInfo info = pm.getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
            titleIcon.setImageDrawable(pm.getApplicationIcon(info.applicationInfo));
        } catch (Exception e)
        {
            titleIcon.setImageResource(R.drawable.app_filemanager);
        }
        mSearchView = (SearchView) findViewById(R.id.title_search_view);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null)
        {
            mSearchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
        }
        mSearchView.onActionViewExpanded();

        if (!TextUtils.isEmpty(mSearchText))
        {
            mSearchView.setQuery(mSearchText, false);
            mSearchView.clearFocus();
        }
        
        View titleBackView = findViewById(R.id.title_back_view);
        titleBackView.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.search_options_menu, menu);
//        mSearchItem = menu.findItem(R.id.search);
//        mSearchView = (SearchView) menu.findItem(R.id.search).getActionView();
//
//        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
//                LayoutParams.WRAP_CONTENT);
//        mSearchView.setLayoutParams(layoutParams);
//
//        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        if (searchManager != null)
//        {
//            mSearchView.setSearchableInfo(searchManager
//                    .getSearchableInfo(getComponentName()));
//        }
//
//        mSearchItem.expandActionView();
//        mSearchItem.setOnActionExpandListener(new OnActionExpandListener()
//        {
//
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item)
//            {
//                return true;
//            }
//
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item)
//            {
//                finish();
//                return false;
//            }
//        });
//        if (!TextUtils.isEmpty(mSearchText))
//        {
//            mSearchView.setQuery(mSearchText, false);
//            mSearchView.clearFocus();
//        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (mSearchText != null)
        {
            outState.putString(SEARCH_TEXT, mSearchText);
            outState.putLong(SEARCH_TOTAL, mTotal);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null && mResultView != null)
        {
            mSearchText = savedInstanceState.getString(SEARCH_TEXT);
            if (!TextUtils.isEmpty(mSearchText))
            {
                mTotal = savedInstanceState.getLong(SEARCH_TOTAL);
                mResultView.setVisibility(View.VISIBLE);
                mResultView.setText(getResources().getString(
                        R.string.search_result, mSearchText, mTotal));
            }
        }

        Intent intent = getIntent();
        handleIntent(intent);
    }

    /**
     * The method handles received intent.
     * 
     * @param intent
     *            the intent FileManagerSearchActivity received.
     */
    private void handleIntent(Intent intent)
    {

        if (Intent.ACTION_VIEW.equals(intent.getAction()))
        {
            String path = null;
            if (intent.getData() != null)
            {
                path = intent.getData().toString();
            }
            if (TextUtils.isEmpty(path))
            {
                Log.w(TAG, "handleIntent intent uri path == null");
                return;
            }
            onItemClick(Util.GetFileInfo(path));
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            // handles a search query
            String query = intent.getStringExtra(SearchManager.QUERY);
            requestSearch(query);
        }
    }

    /**
     * The method start search task.
     * 
     * @param query
     *            the search target.
     */
    private void requestSearch(String query)
    {
        Log.i(TAG, "requestSearch, query:" + query);
        if (query != null && !query.isEmpty())
        {
            SearchTask task = new SearchTask(new SearchListener(query), query,
                    mSearchPath, getContentResolver());
            task.execute();
            if (mSearchView != null)
            {
                mSearchView.setQuery(query, false);
                mSearchView.clearFocus();
            }
        } else
        {
            Toast.makeText(this, R.string.search_text_empty, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    protected class SearchListener implements OperationEventListener
    {
        private static final int FRIST_UPDATE_COUNT = 20;
        private static final int NEED_UPDATE_LIST = 6;
        private boolean mIsResultSet = false;
        private int mCount = 0;

        /**
         * Constructor of SearchListener.
         * 
         * @param text
         *            the search target(String), which will be shown on
         *            searchResult TextView..
         */
        public SearchListener(String text)
        {
            if (text == null)
            {
                throw new IllegalArgumentException();
            }
            mSearchText = text;
        }

        @Override
        public void onTaskResult(int result)
        {
            Log.i(TAG, "onTaskResult, size:" + mFileInfoList.size());
            mAdapter.notifyDataSetChanged();
            updateSearchResult();
        }

        @Override
        public void onTaskPrepare()
        {
            mAdapter.clear();
            // mAdapter.changeMode(FileInfoAdapter.MODE_SEARCH);
        }

        @Override
        public void onTaskProgress(ProgressInfo progressInfo)
        {
            if (!progressInfo.isFailInfo())
            {
                if (mResultView != null && !mIsResultSet)
                {
                    mSearchTotal = progressInfo.getTotal();
                    mIsResultSet = true;
                }

                FileInfo fileInfo = progressInfo.getFileInfo();
                if (fileInfo != null)
                {
                    mAdapter.add(fileInfo);
                }
                mCount++;
                if (mCount > FRIST_UPDATE_COUNT)
                {
                    if (mListView.getLastVisiblePosition() + NEED_UPDATE_LIST > mAdapter
                            .getCount())
                    {
                        mAdapter.notifyDataSetChanged();
                        updateSearchResult();
                        mCount = 0;
                    }
                }
            }
        }
        
        private void updateSearchResult()
        {
            mTotal = mAdapter.getCount();
            mResultView.setVisibility(View.VISIBLE);
            mResultView.setText(getResources().getString(
                    R.string.search_result, mSearchText, mTotal));
            mSearchBg.setVisibility(View.GONE);
        }
    }

    public class SearchListAdapter extends ArrayAdapter<FileInfo>
    {
        private LayoutInflater mInflater;
        private FileIconHelper mFileIcon;
        private Context mContext;
        private int mLayout;
        List<FileInfo> mListData;

        public SearchListAdapter(Context context, int resource,
                List<FileInfo> fileInfoList, FileIconHelper fileIcon)
        {
            super(context, resource, fileInfoList);
            mInflater = LayoutInflater.from(context);
            mFileIcon = fileIcon;
            mContext = context;
            mLayout = resource;
            mListData = fileInfoList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = null;
            if (convertView != null)
            {
                view = convertView;
            } else
            {
                view = mInflater.inflate(mLayout, parent, false);
            }

            FileInfo lFileInfo = mListData.get(position);
            FileListItem.setupFileListItemInfo(mContext, view, lFileInfo,
                    mFileIcon, null);
            return view;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position,
            long arg3)
    {
        if (position >= mAdapter.getCount() || position < 0)
        {
            Log.e(TAG, "click events error");
            Log.e(TAG, "mFileInfoList.size(): " + mAdapter.getCount());
            return;
        }
        FileInfo selectedFileInfo = (FileInfo) mAdapter.getItem(position);
        onItemClick(selectedFileInfo);
    }

    /**
     * The method deal with the event that a certain childView of listView in
     * searchActivity is clicked.
     * 
     * @param selectedFileInfo
     *            The FileInfo associate with the selected childView of
     *            listView.
     */
    private void onItemClick(FileInfo selectedFileInfo)
    {
        if (selectedFileInfo.IsDir)
        {
            if (!mIsFromFileManger)
            {
                Intent intent = new Intent(this, FileExplorerTabActivity.class);
                intent.setData(Uri.parse("file://"+selectedFileInfo.filePath));
                startActivity(intent);
            } else
            {
                FileExplorerTabActivity.mIsFromSearch = true;
                FileExplorerTabActivity.mSearchPath = selectedFileInfo.filePath;
            }
            finish();
        } else
        {
            try
            {
                IntentBuilder.viewFile(this, selectedFileInfo.filePath, true);
                mIsReturnBack = true;
            } catch (ActivityNotFoundException e)
            {
                Log.e(TAG, "fail to view file: " + e.toString());
            }
        }
    }
}

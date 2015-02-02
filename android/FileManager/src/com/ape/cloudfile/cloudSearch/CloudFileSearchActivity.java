package com.ape.cloudfile.cloudSearch;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.cloudfile.CloudFileListAdapter;
import com.ape.cloudfile.CloudFileListOnCreateContextMenuListener;
import com.ape.cloudfile.CloudFileOperationActivity;
import com.ape.cloudfile.CloudFileOperator;
import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.accessDAO.CloudFileAccessDAO;
import com.ape.cloudfile.task.TaskSearchCloudFile;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.R;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.MissionObject;


public class CloudFileSearchActivity extends CloudFileOperationActivity implements
        OnItemClickListener
{
    private static final String TAG = "CloudFileSearchActivity";

    public static final String CURRENT_PATH = "current_path";
    public static final String RETURN_PATH = "return_path";

    public static final String SEARCH_TEXT = "search_text";
    public static final String SEARCH_TOTAL = "search_total";

    private MenuItem mSearchItem;
    private TextView mResultView = null;
    private String mSearchPath = null;
    private SearchView mSearchView = null;
    private ImageView mSearchBg = null;
    
    private String mSearchText = null;
    private long mTotal = 0;
    
    private ListView mListView;
    private CloudFileListAdapter<CloudFile> mAdapter;
    private ArrayList<CloudFile> mCloudFileList;
    
    private CloudFile mDeleteCloudFile;

    private boolean mIsReturnBack = false;

    private OnCreateContextMenuListener mListMenuCreateListener = new CloudFileListOnCreateContextMenuListener();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //setTheme(R.style.FileManagerOperTheme);
        setContentView(R.layout.search_main);
        
        mResultView = (TextView) findViewById(R.id.search_result);
        mSearchBg = (ImageView) findViewById(R.id.search_bg);

        mCloudFileList = new ArrayList<CloudFile>();
        mAdapter = new CloudFileListAdapter<CloudFile>(this, mCloudFileList);
        mAdapter.setOperationListener(mAdapterListener);
        mListView = (ListView) findViewById(R.id.list_view);
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnCreateContextMenuListener(mListMenuCreateListener);

        initSearchView();
        TaskSearchCloudFile.beginInitCacheDB(this);
        parseIntent();
    }
    
    private void parseIntent()
    {
        Intent intent = getIntent();
        
        mSearchPath = intent.getStringExtra(CURRENT_PATH);
        if (mSearchPath == null)
        {
            mSearchPath = CloudFileUtil.CLOUD_ROOT_DIR;
        }

        handleIntent(intent);
        MyLog.i(TAG, "parseIntent, intent:" + intent);
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
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
        
        CloudFile file = (CloudFile) mAdapter.getItem(info.position);
        switch (item.getItemId())
        {
            case CloudFileListOnCreateContextMenuListener.MENU_ITEM_DELETE:
                mDeleteCloudFile = file;
                deleteCloudFile(file);
                break;
            case CloudFileListOnCreateContextMenuListener.MENU_ITEM_DOWNLOAD:
                downloadCloudFile(file);
                break;
        }
        return super.onContextItemSelected(item);
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
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id)
    {
        if (position >= mAdapter.getCount() || position < 0)
        {
            MyLog.e(TAG, "click events error");
            MyLog.e(TAG, "mFileInfoList.size(): " + mAdapter.getCount());
            return;
        }
        
        onItemClick(mCloudFileList.get(position));
    }
    
    public void onItemClick(CloudFile fileInfo)
    {
        MyLog.i(TAG, "onItemClick, file:" + fileInfo.getKey());
        if (fileInfo.isFile())
        {
            CloudFileOperator tool = new CloudFileOperator(this);
            if (tool.viewCloudFile(fileInfo, false))
            {
                mIsReturnBack = true;
            }
        } else
        {
            Intent intent = new Intent(CloudFileUtil.CLOUD_STORAGE_ACTION);
            intent.putExtra(CloudFileUtil.CLOUD_STORAGE_EXTRA_PATH_TAG, fileInfo.getKey());
            intent.putExtra(CloudFileUtil.CLOUD_STORAGE_FROM_OTHER_APP, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
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
                MyLog.w(TAG, "handleIntent intent uri path == null");
                return;
            }

            List<CloudFile> list = CloudFileAccessDAO.getInstance(this).getCloudFiles(path, false);
            if (list != null && list.size() > 0)
            {
                onItemClick(list.get(0));
            }
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
        MyLog.i(TAG, "requestSearch, query:" + query);
        if (!CloudFileService.getInstance(this).isFileClientValid())
        {
            Toast.makeText(this, R.string.cloud_file_service_error, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(query))
        {
            Toast.makeText(this, R.string.search_text_empty, Toast.LENGTH_SHORT).show();
            
        } else
        {
            TaskSearchCloudFile task = new TaskSearchCloudFile(
                    new SearchListener(query),
                    query,
                    mSearchPath, this);
            task.execute();
            if (mSearchView != null)
            {
                mSearchView.setQuery(query, false);
                mSearchView.clearFocus();
            }
        }
    }
    
    private class SearchListener implements OperationEventListener
    {
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
        public void onTaskPrepare()
        {
            mCloudFileList.clear();
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
            List<CloudFile> files = progress.getCloudFiles();
            if (files != null)
            {
                mCloudFileList.addAll(files);
                mAdapter.notifyDataSetChanged();
                updateSearchResult();
            }
        }

        @Override
        public void onTaskResult(int result)
        {
            if (result == OperationEventListener.ERROR_CODE_SUCCESS)
            {
                updateSearchResult();
            } else
            {
                Toast.makeText(CloudFileSearchActivity.this, R.string.cloud_file_service_error, Toast.LENGTH_SHORT).show();
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
    
    private CloudFileListAdapter.ListItemOperationListener mAdapterListener
        = new CloudFileListAdapter.ListItemOperationListener()
    {

        @Override
        public boolean downloadFiles()
        {
            return false;
        }

        @Override
        public void deleteFiles()
        {
            
        }

        @Override
        public boolean isCheckedMode()
        {
            return false;
        }

        @Override
        public void setProgressBarInfo(RoundProgressBar bar,
                MissionObject mission)
        {
            
        }

        @Override
        public void removeProgressBarInfo(RoundProgressBar bar, MissionObject mission)
        {
            
        }

        @Override
        public void onActionModeFinished()
        {
            
        }
    };

    @Override
    public void onTaskResult(int result)
    {
        super.onTaskResult(result);
        
        if (mDeleteCloudFile != null)
        {
            mCloudFileList.remove(mDeleteCloudFile);
            mAdapter.notifyDataSetChanged();
            mDeleteCloudFile = null;
        }
    };
}

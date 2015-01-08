package com.ape.cloudfile.task;

import java.util.List;

import android.content.Context;

import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.accessDAO.CloudFileAccessDAO;
import com.ape.cloudfile.accessDAO.CloudFileDBHelper;
import com.ape.filemanager.BaseAsyncTask;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.MyLog;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;

public class TaskSearchCloudFile extends BaseAsyncTask
{
    private static final String TAG = "TaskSearchCloudFile";
    private static Boolean mIsCacheDBInited = false;
    private static Boolean mCacheDBIniting = false;
    private static Object mLock = new Object();

    private final String mSearchName;
    private final String mPath;
    private final Context mContext;
    protected OperationEventListener mListener = null;

    public TaskSearchCloudFile(OperationEventListener listener, String searchName,
            String path, Context context)
    {
        super(listener);
        mListener = listener;
        mContext = context;
        mPath = path;
        mSearchName = searchName;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        
        synchronized (mLock)
        {
            while (mCacheDBIniting)
            {
                try
                {
                    mLock.wait();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

        if (!mIsCacheDBInited)
        {
            if (initCacheDatabase(mContext))
            {
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(CloudFileDBHelper.FIELD_NAME).append(" LIKE ?");
        sb.append(" AND ");
        sb.append(CloudFileDBHelper.FIELD_PARENT).append(" LIKE ?");
        
        String [] selectionArgs = {"%" + mSearchName + "%", mPath + "%"};

        CloudFileAccessDAO myDao = CloudFileAccessDAO.getInstance(mContext);
        List<CloudFile> fileList = myDao.query(sb.toString(), selectionArgs, null);
        if (fileList == null)
        {
            MyLog.e(TAG, "get file list is null.");
            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        
        publishProgress(new ProgressInfo(fileList, 0, 0, 0, 0));

        return ret;
    }
    
    public static boolean initCacheDatabase(Context context)
    {
        synchronized (mLock)
        {
            mCacheDBIniting = true;

            CloudFileAccessDAO myDao = CloudFileAccessDAO.getInstance(context);
            CloudFileService tool = CloudFileService.getInstance(context);
            if (!tool.isFileClientValid())
            {
                return false;
            }
            
            myDao.deleteAll();
            CloudFileResult result = tool.listDirectory(CloudFileUtil.CLOUD_GALLERY_BACKUP_DIR);
            if (result.getResultCode() == CloudUtil.CLOUDCLIENT_RESULT_OK)
            {
                List<CloudFile> list = result.getFileList();
                int listSize = list.size();
                MyLog.d(TAG, "initCacheDatabase, listSize:" + listSize);
                for (CloudFile file : list)
                {
                    if (!file.getKey().startsWith(CloudFileUtil.CLOUD_SYSTEM_DIR))
                    {
                        myDao.insert(file);
                    }
                }
                
                mIsCacheDBInited = true;
            }

            mCacheDBIniting = false;
            mLock.notifyAll();
        }

        return mIsCacheDBInited;
    }
    
    public static void beginInitCacheDB(final Context context)
    {
        mIsCacheDBInited = false;
        new Thread(){
            @Override
            public void run()
            {
                initCacheDatabase(context);
            }            
        }.start();
    }
    
    public static boolean isCacheDBInit()
    {
        return mIsCacheDBInited;
    }
}

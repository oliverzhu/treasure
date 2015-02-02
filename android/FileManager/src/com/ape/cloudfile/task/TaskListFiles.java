package com.ape.cloudfile.task;

import java.util.List;

import android.content.Context;

import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.ProgressInfo;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;

public class TaskListFiles extends TaskCloudFileBase
{
    private static final String TAG = "TaskListFiles";
    private String mCloudPath;

    private boolean mNeedCache = true;

    public TaskListFiles(Context context, OperationEventListener listener,
            String path)
    {
        super(context, listener);

        mCloudPath = path;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;

        if (mCloudPath == null)
        {
            return OperationEventListener.ERROR_CODE_NAME_VALID;
        }

        if (mNeedCache && getFileListFromCache())
        {
            return OperationEventListener.ERROR_CODE_SUCCESS;
        }

        MyLog.i(TAG, "doInBackground, before listDirectory, mCloudPath:" + mCloudPath);
        CloudFileResult result = CloudFileService.getInstance(mContext).listDirectory(mCloudPath);
        ret = handleServerResult(result);
        
        if (ret == OperationEventListener.ERROR_CODE_SUCCESS)
        {
            List<CloudFile> list = result.getFileList();
            filterFiles(list);
            publishProgress(new ProgressInfo(list, 0, 0, 0, 0));

            mFileDao.deleteUnderParent(mCloudPath);
            mFileDao.insertAll(list);
        } else
        {
            StringBuffer logText = new StringBuffer("listfile failed, resultCode:");
            logText.append(result.getResultCode());
            logText.append(", path:").append(mCloudPath);
            logText.append(", message:").append(result.getMessage());
            MyLog.e(TAG, logText.toString());
        }

        return ret;
    }

    protected boolean getFileListFromCache()
    {
        boolean retValue = false;

        List<CloudFile> list = mFileDao.getCloudFiles(mCloudPath, true);
        if (list != null && list.size() > 0)
        {
            long lastAccessTime = Long.MAX_VALUE;
            for (CloudFile file : list)
            {
                lastAccessTime = (file.getAccessTime() < lastAccessTime) ? 
                        file.getAccessTime() : lastAccessTime;
            }

            if (System.currentTimeMillis() - lastAccessTime > CloudFileUtil.CACHE_UPDATE_TIME)
            {
                mFileDao.deleteUnderParent(mCloudPath);
                retValue = false;
            } else
            {
                publishProgress(new ProgressInfo(list, 0, 0, 0, 0));
                //mFileDao.updatePathAccessTime(mCloudPath);
                retValue = true;
            }
        }

        MyLog.i(TAG, "getFileListFromCache, ret:" + retValue);
        return retValue;
    }

    public boolean isNeedCache()
    {
        return mNeedCache;
    }

    public void setNeedCache(boolean needCache)
    {
        mNeedCache = needCache;
    }
    
    public void filterFiles(List<CloudFile> list)
    {
        if (mCloudPath.equals(CloudFileUtil.CLOUD_ROOT_DIR))
        {
            int index = 0;
            while (index < list.size())
            {
                if (list.get(index).getKey().equals(CloudFileUtil.CLOUD_SYSTEM_DIR))
                {
                    list.remove(index);
                }
                index++;
            }
        }
    }
}
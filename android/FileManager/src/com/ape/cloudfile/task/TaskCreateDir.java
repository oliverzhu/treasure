package com.ape.cloudfile.task;

import java.io.File;

import android.content.Context;

import com.ape.cloudfile.CloudFileService;
import com.ape.filemanager.MyLog;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;

public class TaskCreateDir extends TaskCloudFileBase
{
    private static final String TAG = "TaskCreateDir";
    private String mCloudPath;
    private String mDirName;
    
    public TaskCreateDir(Context context, OperationEventListener listener,
            String path, String dirName)
    {
        super(context, listener);
        mCloudPath = path;
        mDirName = dirName;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;

        if (mCloudPath == null)
        {
            return OperationEventListener.ERROR_CODE_NAME_VALID;
        }

        String dirCloudPath = mCloudPath + mDirName;
        if (!dirCloudPath.endsWith(File.separator))
        {
            dirCloudPath = dirCloudPath + File.separator;
        }
        CloudFileResult result = CloudFileService.getInstance(mContext)
                .createDir(dirCloudPath);
        ret = handleServerResult(result);

        if (ret == OperationEventListener.ERROR_CODE_SUCCESS)
        {
            CloudFile myFile = new CloudFile(dirCloudPath,
                    mCloudPath,
                    mDirName,
                    0,
                    System.currentTimeMillis(),
                    false);

            mFileDao.insert(myFile);
        } else
        {
            StringBuffer logText = new StringBuffer("Create dir failed, resultCode:");
            logText.append(result.getResultCode());
            logText.append(", dir:").append(mDirName);
            logText.append(", message:").append(result.getMessage());
            MyLog.e(TAG, logText.toString());
        }

        return ret;
    }

}
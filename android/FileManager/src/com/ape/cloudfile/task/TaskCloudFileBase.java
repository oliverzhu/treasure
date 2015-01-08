package com.ape.cloudfile.task;

import android.content.Context;
import android.text.TextUtils;

import com.ape.cloudfile.accessDAO.CloudFileAccessDAO;
import com.ape.filemanager.BaseAsyncTask;
import com.ape.filemanager.ProgressInfo;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.CloudFileResult;

abstract public class TaskCloudFileBase extends BaseAsyncTask
{
    protected static final int TOTAL = 100;

    protected Context mContext;
    protected CloudFileAccessDAO mFileDao;
    
    protected int mCurrentNumber;
    protected int mTotalNumber;

    public TaskCloudFileBase(Context context, OperationEventListener listener)
    {
        super(listener);

        mContext = context;
        mFileDao = CloudFileAccessDAO.getInstance(mContext);
    }

    static private int parseErrorMsg(String msg)
    {
        if (TextUtils.isEmpty(msg))
        {
            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
        if (msg.contains("UnknownHostException"))
        {
            return OperationEventListener.ERROR_CODE_CANNOT_CONNECT_HOST;
        }
        if (msg.contains("timed out"))
        {
            return OperationEventListener.ERROR_CODE_NETWORK_TIMEOUT;
        }
            
        return OperationEventListener.ERROR_CODE_UNSUCCESS;
    }

    static public int handleServerResult(CloudFileResult result)
    {
        switch (result.getResultCode())
        {
            case CloudUtil.CLOUDCLIENT_RESULT_OK:
                return OperationEventListener.ERROR_CODE_SUCCESS;
                
            case CloudUtil.CLOUD_FILE_NETWORK_ERROR:
                return OperationEventListener.ERROR_CODE_NETWORK_ERROR;

            case CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR:
                return parseErrorMsg(result.getMessage());
                
            case CloudUtil.CLOUD_FILE_CREATE_BUCKER_ERROR:
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
                
            case CloudUtil.CLOUD_FILE_MISSION_CANCEL:
                return OperationEventListener.ERROR_CODE_USER_CANCEL;

            case CloudUtil.CLOUD_FILE_NO_SUCH_KEY:
                return OperationEventListener.ERROR_CODE_FILE_NOT_EXIST;

            default:
                return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }
    }

    protected void publishBeginProgress(String msg)
    {
        ProgressInfo progress = new ProgressInfo(msg, 0, TOTAL,
                1, mTotalNumber);
        publishProgress(progress);
    }
    
    protected void publishMyProgress(String msg, int progeress, long total)
    {
        ProgressInfo progress = new ProgressInfo(msg, progeress, total,
                mCurrentNumber + 1, mTotalNumber);
        publishProgress(progress);
    }
    
    protected void publishFinishedProgress(String msg)
    {
        ProgressInfo progress = new ProgressInfo(msg, TOTAL, TOTAL,
                mTotalNumber, mTotalNumber);
        publishProgress(progress);
    }
}

package com.ape.filemanager;

import android.os.AsyncTask;
import android.util.Log;

abstract public class BaseAsyncTask extends AsyncTask<Void, ProgressInfo, Integer>
{
    private static final String TAG = "BaseAsyncTask";

    protected OperationEventListener mListener = null;
    protected boolean mIsTaskFinished = true;
    protected boolean mIsCancel = false;

    /**
     * Constructor of BaseAsyncTask
     * 
     * @param fileInfoManager
     *            a instance of FileInfoManager, which manages information of
     *            files in FileManager.
     * @param listener
     *            a instance of OperationEventListener, which is a interface
     *            doing things before/in/after the task.
     */
    public BaseAsyncTask(OperationEventListener listener)
    {
        mListener = listener;
    }

    @Override
    protected void onPreExecute()
    {
        mIsCancel = false;
        mIsTaskFinished = false;
        if (mListener != null)
        {
            Log.d(TAG, "onPreExecute");
            mListener.onTaskPrepare();
        }
    }

    @Override
    protected void onPostExecute(Integer result)
    {
        if (mListener != null)
        {
            Log.d(TAG, "onPostExecute");
            mListener.onTaskResult(result);
            mListener = null;
        }
        mIsTaskFinished = true;
    }

    @Override
    protected void onCancelled()
    {
        if (mListener != null)
        {
            Log.d(TAG, "onCancelled()");
            mListener
                    .onTaskResult(OperationEventListener.ERROR_CODE_USER_CANCEL);
            mListener = null;
        }
        mIsTaskFinished = true;
        mIsCancel = true;
    };
    
    public boolean isTaskCancelled()
    {
        return mIsCancel;
    }
    
    public void cancelTask()
    {
        mIsCancel = true;
    }

    @Override
    protected void onProgressUpdate(ProgressInfo... values)
    {
        if (mListener != null && values != null && values[0] != null)
        {
            Log.v(TAG, "onProgressUpdate");
            mListener.onTaskProgress(values[0]);
        }
    }

    /**
     * This method remove listener from task. Set listener associate with task
     * to be null.
     */
    protected void removeListener()
    {
        if (mListener != null)
        {
            Log.d(TAG, "removeListener");
            mListener = null;
        }
    }

    /**
     * This method set mListener with certain listener.
     * 
     * @param listener
     *            the certain listener, which will be set to be mListener.
     */
    public void setListener(OperationEventListener listener)
    {
        mListener = listener;
    }

    public boolean isTaskBusy()
    {
        Log.d(TAG, "isTaskBusy,task status = " + getStatus());
        if (mIsTaskFinished || getStatus() == Status.FINISHED)
        {
            Log.d(TAG, "isTaskBusy,retuen false.");
            return false;
        }
        Log.d(TAG, "isTaskBusy,retuen true.");
        return true;
    }

    public interface OperationEventListener
    {
        int ERROR_CODE_NAME_VALID = 100;
        int ERROR_CODE_SUCCESS = 0;

        int ERROR_CODE_UNSUCCESS = -1;
        int ERROR_CODE_NAME_EMPTY = -2;
        int ERROR_CODE_NAME_TOO_LONG = -3;
        int ERROR_CODE_FILE_EXIST = -4;
        int ERROR_CODE_NOT_ENOUGH_SPACE = -5;
        int ERROR_CODE_DELETE_FAILS = -6;
        int ERROR_CODE_USER_CANCEL = -7;
        int ERROR_CODE_PASTE_TO_SUB = -8;
        int ERROR_CODE_UNKOWN = -9;
        int ERROR_CODE_COPY_NO_PERMISSION = -10;
        int ERROR_CODE_MKDIR_UNSUCCESS = -11;
        int ERROR_CODE_CUT_SAME_PATH = -12;
        int ERROR_CODE_BUSY = -100;
        int ERROR_CODE_DELETE_UNSUCCESS = -13;
        int ERROR_CODE_PASTE_UNSUCCESS = -14;
        int ERROR_CODE_DELETE_NO_PERMISSION = -15;
        int ERROR_CODE_COPY_GREATER_4G_TO_FAT32 = -16;
        int ERROR_CODE_NO_PERMISSION = -17;
        
        int ERROR_CODE_NETWORK_ERROR = -18;
        int ERROR_CODE_NETWORK_TIMEOUT = -19;
        int ERROR_CODE_CANNOT_CONNECT_HOST = -20;
        int ERROR_CODE_CANNOT_UPLOAD = -21;
        int ERROR_CODE_FILE_NOT_EXIST = -22;
        int ERROR_CODE_WAIT_NETWORK = -23;
        int ERROR_CODE_TASK_QUEUING = -24;

        /**
         * This method will be implemented, and called in onPreExecute of
         * asynctask
         */
        void onTaskPrepare();

        /**
         * This method will be implemented, and called in onProgressUpdate
         * function of asynctask
         * 
         * @param fileInfo
         *            information of fileInfo, which will be updated on UI
         */
        void onTaskProgress(ProgressInfo progress);

        /**
         * This method will be implemented, and called in onPostExecute of
         * asynctask
         * 
         * @param result
         *            the result of asynctask's doInBackground()
         */
        void onTaskResult(int result);
    }
}

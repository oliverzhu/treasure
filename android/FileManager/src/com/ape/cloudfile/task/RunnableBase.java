package com.ape.cloudfile.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

public class RunnableBase implements Runnable
{
    protected RunableListener mListener;
    protected boolean mIsCancel = false;
    protected boolean mIsDelete = false;
    protected Context mContext;
    
    protected String mLocalPath;
    protected String mCloudPath;
    protected MissionObject mMission;

    protected MissionListener missionListener = new MissionListener() {
        protected static final int NEED_UPDATE_TIME = 100;
        private long mLastOperationTime = 0;
        private long totalSize = 0l;

        @Override
        public void setTotalSize(long size) {
            totalSize = size;
            mLastOperationTime = System.currentTimeMillis();
            
//            if (mListener != null) {
//                mListener.publishProgress(getCurrentFilePath(), 0, totalSize);
//            }
            publishProgress(mMission.getTransferredLength(), totalSize);
        }

        @Override
        public void transferred(long transferred) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - mLastOperationTime > NEED_UPDATE_TIME
                    || transferred > (totalSize -2) )
            {
//                String msg = getCurrentFilePath();
//
//                if (mListener != null) {
//                    mListener.publishProgress(msg, transferred, totalSize);
//                }
                publishProgress(transferred, totalSize);
                mLastOperationTime = currentTime;
            }
        }

        @Override
        public boolean isCancel()
        {
            return mIsCancel;
        }
    };
    
    public RunnableBase(Context context, RunableListener listener,
            MissionObject mission)
    {
        mListener = listener;
        mContext = context;
        mMission = mission;
        mLocalPath = mission.getLocalFile();
        mCloudPath = mission.getKey();
    }

    @Override
    public void run()
    {
        mStatus = Status.RUNNING;
    }
    
    public void cancelTask()
    {
        mIsCancel = true;
    }
    
    public boolean isTaskCancel()
    {
        return mIsCancel;
    }
    
    public void setDelete()
    {
        mIsDelete = true;
    }
    
    public boolean isDelete()
    {
        return mIsDelete;
    }

    public String getCurrentFilePath()
    {
        return mLocalPath;
    }
    
    static public interface RunableListener
    {
        void publishProgress(String filePath, long progress, long total);
        
        void onTaskResult(String filePath, int result);
    }
    
    protected static final int MESSAGE_POST_RESULT = 0x1;
    protected static final int MESSAGE_POST_PROGRESS = 0x2;
    
    @SuppressLint("HandlerLeak")
    protected class InternalHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    if (mListener != null)
                    {
                        mListener.onTaskResult(mLocalPath, msg.arg1);
                    }
                    break;
                case MESSAGE_POST_PROGRESS:
                    if (mListener != null)
                    {
                        ProgressResult result = (ProgressResult) msg.obj;
                        mListener.publishProgress(mLocalPath, result.progress, result.total);
                    }
                    break;
            }
        }
    }
    
    protected InternalHandler mHandler = new InternalHandler();
    
    static public class ProgressResult
    {
        public long progress;
        public long total;
        public ProgressResult(long progress, long total)
        {
            this.progress = progress;
            this.total = total;
        }
    }
    
    protected void postResult(int result)
    {
        mStatus = Status.FINISHED;
        Message message = mHandler.obtainMessage(MESSAGE_POST_RESULT, result, 0);
        message.sendToTarget();
    }
    
    protected void publishProgress(long progress, long total)
    {
        ProgressResult result = new ProgressResult(progress, total);
        Message message = mHandler.obtainMessage(MESSAGE_POST_PROGRESS, result);
        message.sendToTarget();
    }
    
    public enum Status {
        /**
         * Indicates that the task has not been executed yet.
         */
        PENDING,
        /**
         * Indicates that the task is running.
         */
        RUNNING,
        /**
         * Indicates that {@link AsyncTask#onPostExecute} has finished.
         */
        FINISHED,
    }
    
    protected volatile Status mStatus = Status.PENDING;

    public final Status getStatus() {
        return mStatus;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((mCloudPath == null) ? 0 : mCloudPath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        RunnableBase other = (RunnableBase) obj;
        if (mCloudPath == null)
        {
            if (other.mCloudPath != null)
                return false;
        } else if (!mCloudPath.equals(other.mCloudPath))
            return false;
        return true;
    }

    public RunableListener getListener()
    {
        return mListener;
    }

    public void setListener(RunableListener listener)
    {
        mListener = listener;
    }
}

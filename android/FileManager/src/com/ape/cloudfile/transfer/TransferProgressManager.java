package com.ape.cloudfile.transfer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.task.RunnableBase;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.filemanager.MyLog;
import com.cloud.client.file.MissionObject;

public class TransferProgressManager
{
    private static final String TAG = "TransferProgressManager";

    class TransferHolder
    {
        MissionObject mission;
        RoundProgressBar progressBar;
        RunnableBase task;
        
        public TransferHolder(MissionObject mission, RoundProgressBar progressBar, RunnableBase task)
        {
            this.mission = mission;
            this.progressBar = progressBar;
            this.task = task;
        }
        
        @Override
        public String toString()
        {
            StringBuilder logStr = new StringBuilder("LocalFile:");
            logStr.append(mission.getLocalFile()).append(", progreesBar:" + progressBar);
            logStr.append(", task:" + task);
            return logStr.toString();
        }
    }

    private ConcurrentHashMap<String, TransferHolder> mTaskMap = new ConcurrentHashMap<String, TransferHolder>();
    private ThreadPoolExecutor mTaskPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(CloudFileUtil.MAX_TRANSFER_TASK);
    private static final ThreadFactory  sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            Thread tread = new Thread(r, "TransferThreadPool #" + mCount.getAndIncrement());
            tread.setPriority(Thread.NORM_PRIORITY - 1);
            return tread;
        }
    };

    public TransferProgressManager()
    {
        mTaskPool.setThreadFactory(sThreadFactory);
    }
   
    public void addOrUpdateMission(MissionObject mission,
            RoundProgressBar progressBar, RunnableBase task)
    {
        TransferHolder holder = mTaskMap.get(mission.getLocalFile());
        if (holder != null)
        {
            holder.progressBar = progressBar;
            //holder.task = task;
        } else
        {
            holder = new TransferHolder(mission, progressBar, task);
            mTaskMap.put(mission.getLocalFile(), holder);
        }
        
        if (task != null)
        {
            mTaskPool.execute(task);
        }

        MyLog.i(TAG, "addOrUpdateMission, taskMap:" + mTaskMap);
    }
    
    public void setProgressBar(MissionObject mission, RoundProgressBar progressBar)
    {
        TransferHolder holder = mTaskMap.get(mission.getLocalFile());
        if (holder != null)
        {
            holder.progressBar = progressBar;
        }
    }
    
    public RunnableBase getRunnable(String filePath)
    {
        TransferHolder holder = mTaskMap.get(filePath);
        if (holder != null)
        {
            return holder.task;
        }
        
        return null;
    }
    
    public MissionObject getMission(String filePath)
    {
        TransferHolder holder = mTaskMap.get(filePath);
        if (holder != null)
        {
            return holder.mission;
        }
        
        return null;
    }
    
    public RoundProgressBar getProgressBar(String filePath)
    {
        TransferHolder holder = mTaskMap.get(filePath);
        if (holder != null)
        {
            return holder.progressBar;
        }
        
        return null;
    }
    
    public boolean dismissProgressBar(String filePath)
    {
        boolean retVal = false;
        TransferHolder holder = mTaskMap.get(filePath);
        if (holder != null)
        {
            holder.progressBar = null;
            retVal = true;
        }
        
        return retVal;
    }
    
    public boolean cancelRunnable(String filePath)
    {
        boolean retVal = false;
        TransferHolder holder = mTaskMap.get(filePath);
        if (holder != null)
        {
            holder.task.cancelTask();
            retVal = true;
        }
        
        return retVal;
    }
    
    public boolean removeTaskFromPool(String filePath)
    {
        RunnableBase task = getRunnable(filePath);
        return mTaskPool.remove(task);
    }
    
    public void removeMission(String filePath)
    {
        mTaskMap.remove(filePath);
    }
    
    public void clearAll()
    {
        mTaskMap.clear();
    }
    
    public void exit()
    {
        mTaskMap.clear();
        mTaskPool.shutdown();
    }
    
    @Override
    public String toString()
    {
        return mTaskMap.toString();
    }
}

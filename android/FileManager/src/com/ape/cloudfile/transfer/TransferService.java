package com.ape.cloudfile.transfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.ape.cloudfile.CloudFileOperator;
import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudUserInfomation;
import com.ape.cloudfile.CloudUserInfomation.UserInformationListener;
import com.ape.cloudfile.accessDAO.CloudFileAccessDAO;
import com.ape.cloudfile.accessDAO.CloudTransferListDAO;
import com.ape.cloudfile.task.RunnableBase;
import com.ape.cloudfile.task.RunnableBase.RunableListener;
import com.ape.cloudfile.task.RunnableBase.Status;
import com.ape.cloudfile.task.RunnableDownload;
import com.ape.cloudfile.task.RunnableUpload;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.cloudfile.widget.RoundProgressBar.ProgressStatus;
import com.ape.filemanager.BaseAsyncTask;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.ape.filemanager.updatehelper.MediaStoreHelper;
import com.ape.filemanager.updatehelper.MultiMediaStoreHelper.DeleteMediaStoreHelper;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.MissionObject;


public class TransferService extends Service
{
    private static final String TAG = "TransferService";

    private final IBinder mBinder = new ServiceBinder();
    
    private String mUserKey;
    private Context mContext;
    private boolean mInit = false;
    
    private CloudFileOperator mHelper;
    private CloudUserInfomation mUserInfomation;
    
    private ArrayList<MissionObject> mUploadingList= new ArrayList<MissionObject>();
    private ArrayList<MissionObject> mUploadedList= new ArrayList<MissionObject>();

    private ArrayList<MissionObject> mDownloadingList= new ArrayList<MissionObject>();
    private ArrayList<MissionObject> mDownloadedList= new ArrayList<MissionObject>();
    
    private TransferProgressManager mUploadManager = new TransferProgressManager();
    private TransferProgressManager mDownloadManager = new TransferProgressManager();
    
    public enum TransferType
    {
        upload, download
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO Auto-generated method stub
        return mBinder;
    }

    public class ServiceBinder extends Binder {
        /**
         * @return service instance
         */
        public TransferService getService() {
            return TransferService.this;
        }
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        MyLog.i(TAG, "onCreate");
        
        mContext = getApplicationContext();
        mUserInfomation = new CloudUserInfomation(mContext, new UserInformationListener()
        {
            @Override
            public void userInformationFinish()
            {
                mInit = false;
                initData();
                notifyServerInfoListener(InformationType.userInformationFinish, null);
            }

            @Override
            public void userInformationChanged(String userKey)
            {
                MyLog.i(TAG, "userInformationChanged");
                setUserKey(mUserInfomation.getUserKey());
                userKeyChanged();
                notifyServerInfoListener(InformationType.userInformationChanged, null);
            }

            @Override
            public void userInformationError(int errorCode)
            {
                notifyServerInfoListener(InformationType.userInformationError, errorCode);
            }

            @Override
            public void userKeyFinish(String userKey)
            {
                String oldUserKey = mUserKey;
                setUserKey(mUserInfomation.getUserKey());
                if (!mUserKey.equals(oldUserKey)) // The same user key send to ui only once.
                {
                    notifyServerInfoListener(InformationType.userKeyFinish, null);
                }
            }

            @Override
            public void userLogout()
            {
                MyLog.i(TAG, "userLogout");
                pauseAllTask(TransferType.upload);
                pauseAllTask(TransferType.download);
                setUserKey(mUserInfomation.getUserKey());
            }
            
        });
        mUserInfomation.start();
        
        mHelper = new CloudFileOperator(mContext);
        
        registerCloudReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        MyLog.i(TAG, "onStartCommand, intent:" + intent);
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy()
    {
        mUploadManager.exit();
        mDownloadManager.exit();
        
        mUserInfomation.exit();
        unregisterCloudReceiver();
        MyLog.i(TAG, "On TransferService.onDestroy");
    }

    public String getUserKey()
    {
        return mUserKey;
    }

    private void setUserKey(String userKey)
    {
        mUserKey = userKey;
        CloudFileService.getInstance(mContext).setUserId(mUserKey);
    }

    public String getUserName()
    {
        return mUserInfomation.getUserName();
    }

    public boolean isUserLogin()
    {
        return mUserInfomation.isSessionValid();
    }

    public boolean isUserInfomationValid()
    {
        return !mUserInfomation.isInvalidUserKey() && mUserInfomation.isSpaceInformationValid();
    }

    public CloudUserInfomation getUserInfomation()
    {
        return mUserInfomation;
    }

    public boolean isUserInfoInited()
    {
        return mUserInfomation.isInforComplete();
    }

    public boolean isUserKeyValid()
    {
        return !mUserInfomation.isInvalidUserKey();
    }

    public boolean isNetWorkConnect()
    {
        return mIsNetworkConnect;
    }

    public boolean isWifiConnetion()
    {
        return mIsWifiConnect;
    }

    public boolean initDataForActivity()
    {
        boolean retVal = false;
        if (isUserKeyValid())
        {
            mInit = false;
            initData();
            retVal = true;
        }

        return retVal;
    }

    private synchronized void initData()
    {
        MyLog.i(TAG, "initData, mInit: " + mInit);
        if (!mInit)
        {
            loadTransferList(TransferType.upload);
            loadTransferList(TransferType.download);
        }
        mInit = true;
    }
    
    public void reset()
    {
        mUserKey = "";
    }
    
    private void userKeyChanged()
    {
        for (MissionObject mission : mUploadingList)
        {
            mUploadManager.cancelRunnable(mission.getLocalFile());
            mUploadManager.removeTaskFromPool(mission.getLocalFile());
        }

        for (MissionObject mission : mDownloadingList)
        {
            RunnableBase task = mDownloadManager.getRunnable(mission.getLocalFile());
            if (task != null)
            {
                task.cancelTask();
            }
        }
        mUploadManager.clearAll();
        mDownloadManager.clearAll();
    }
    
    public boolean isUnFinished()
    {
        return (mUploadingList.size() > 0 || mDownloadingList.size() > 0);
    }
    
    public void activityExit()
    {
        for (MissionObject mission : mUploadingList)
        {
            mUploadManager.dismissProgressBar(mission.getLocalFile());
        }

        for (MissionObject mission : mDownloadingList)
        {
            mDownloadManager.dismissProgressBar(mission.getLocalFile());
        }
    }

    public boolean addTransferMission(ArrayList<String> srcList, String destPath, TransferType type)
    {
        boolean retVal = true;
        CloudFileService tools = CloudFileService.getInstance(mContext);

        long beginTime = System.currentTimeMillis();

        if (type == TransferType.download)
        {
            int repeatCount = 0;
            File destDir = new File(destPath);
            if (!destDir.exists())
            {
                destDir.mkdirs();
            }
            long freeSpace = destDir.getFreeSpace();
            CloudFileAccessDAO cacher = CloudFileAccessDAO.getInstance(mContext);
            CloudTransferListDAO transferDao = CloudTransferListDAO.getInstance(mContext);

            for (String srcFile : srcList)
            {
                if (transferDao.isDownloadRecordExist(mUserKey, srcFile))
                {
                    continue;
                }

                List<CloudFile> cloudFiles = cacher.getCloudFiles(srcFile, false);
                if (cloudFiles != null && cloudFiles.size() > 0)
                {
                    freeSpace = freeSpace - cloudFiles.get(0).getLength();
                }
                if (freeSpace <= 0)
                {
                    return false;
                }

                String dir = destPath;
                String destFile;
                if (dir.endsWith(File.separator))
                {
                    dir = dir.substring(0, dir.length()-1);
                }
                if (srcFile.startsWith(File.separator))
                {
                    destFile = dir + srcFile;
                } else
                {
                    destFile = dir + File.separator + srcFile;
                }
                if (new File(destFile).exists())
                {
                    repeatCount++;
                    continue;
                }

                MissionObject mission = tools.initDownload(srcFile, destFile);
                if (mInit && mission != null)
                {
                    mDownloadingList.add(mission);
                }
            }
            if (repeatCount > 0)
            {
                String prompt = mContext.getString(R.string.add_to_download_repeat, repeatCount, destPath);
                Toast.makeText(mContext, prompt, Toast.LENGTH_LONG).show();
            }
        } else //upload
        {
            long freeSpace = mUserInfomation.getMaxSpace() - mUserInfomation.getUsedSpace();
            for (String srcFile : srcList)
            {
                String fileName = Util.getNameFromFilepath(srcFile);
                File file = new File(srcFile);
                if (freeSpace < file.length())
                {
                    return false;
                } else
                {
                    freeSpace = freeSpace - file.length();
                }

                if (!TextUtils.isEmpty(fileName))
                {
                    String destFile;
                    if (destPath.endsWith(File.separator))
                    {
                        destFile = destPath + fileName;
                    } else
                    {
                        destFile = destPath + File.separator + fileName;
                    }

                    MissionObject mission = tools.initMultipartUpload(srcFile, destFile);
                    if (mInit && mission != null && !mission.isFinished())
                    {
                        mUploadingList.add(mission);
                    }
                }
            }
        }

        long dur = System.currentTimeMillis() - beginTime;
        MyLog.d(TAG, "addTransferMission, dur:" + dur + ", type:" + type);
        return retVal;
    }

    public boolean loadTransferList(TransferType type)
    {
        List<MissionObject> transferringList;
        List<MissionObject> transferredList;
        List<MissionObject> list;

        if (type == TransferType.upload)
        {
            transferringList = mUploadingList;
            transferredList = mUploadedList;
            list = CloudFileService.getInstance(mContext).listUploadMissionObject();
        } else
        {
            transferringList = mDownloadingList;
            transferredList = mDownloadedList;
            list = CloudFileService.getInstance(mContext).listDownloadMissionObject();
        }

        MyLog.i(TAG, "loadTransferList, type:" + type + ", list.size: " + list.size());

        transferringList.clear();
        transferredList.clear();
        
        if (list != null)
        {
            for (MissionObject mission : list)
            {
                if (mission.isFinished())
                    transferredList.add(mission);
                else
                    transferringList.add(mission);
            }
        }
        return (list != null) && (list.size() > 0);
    }
    
    public ArrayList<MissionObject> getTransferringList(TransferType type)
    {
        if (!mInit)
        {
            initData();
        }

        if (type == TransferType.upload)
            return mUploadingList;
        else
            return mDownloadingList;
    }
    
    public ArrayList<MissionObject> getTransferredList(TransferType type)
    {
        if (!mInit)
        {
            initData();
        }
        if (type == TransferType.upload)
            return mUploadedList;
        else
            return mDownloadedList;
    }
    
    private RunnableBase createRunnable(MissionObject mission, TransferType type)
    {
        if (type == TransferType.upload)
        {
            return new RunnableUpload(mContext, new TransferListener(type),
                    mission);
        } else
        {
            return new RunnableDownload(mContext, new TransferListener(type),
                    mission);
        }
    }

    public void addOrUpdateTransfer(MissionObject mission, 
            RoundProgressBar progressBar, TransferType type)
    {
        if (mission.isFinished())
        {
            return;
        }

        MyLog.d(TAG, "addOrUpdateTransfer, file:" + mission.getLocalFile()+", progressBar:" + progressBar);

        TransferProgressManager manager;
        if (type == TransferType.upload)
        {
            manager = mUploadManager;
            progressBar.setUploadOrDownloadType(true);
        } else
        {
            manager = mDownloadManager;
            progressBar.setUploadOrDownloadType(false);
        }

        if (mission.isPaused())
        {
            progressBar.setProgressStatus(ProgressStatus.pause);
        } else
        {
            RunnableBase runnable = manager.getRunnable(mission.getLocalFile());

            MyLog.i(TAG, "addOrUpdateTransfer, runnable:" + runnable);
            if (runnable == null)
            {
                runnable = createRunnable(mission, type);
                manager.addOrUpdateMission(mission, progressBar, runnable);
                progressBar.setProgressStatus(ProgressStatus.wait);
            } else
            {
                manager.setProgressBar(mission, progressBar);
                if (runnable.getStatus() == Status.PENDING)
                {
                    progressBar.setProgressStatus(ProgressStatus.wait);
                } else if (runnable.getStatus() == Status.RUNNING)
                {
                    progressBar.setProgressStatus(ProgressStatus.progress);
                }
            }
        }

        setProgressFromTag(progressBar);
        progressBar.setOnClickListener(new OnProgressBarClick(type));
    }
    
    private class OnProgressBarClick implements OnClickListener
    {
        private TransferType type;
        private TransferProgressManager manager;

        public OnProgressBarClick(TransferType type)
        {
            this.type = type;
            if (type == TransferType.upload)
            {
                manager = mUploadManager;
            } else
            {
                manager = mDownloadManager;
            }
        }

        @Override
        public void onClick(View v)
        {
            Object obj = v.getTag();
            if (obj == null)
            {
                return;
            }

            if (obj.getClass() == MissionObject.class && v.getClass() == RoundProgressBar.class)
            {
                RoundProgressBar bar = (RoundProgressBar) v;
                MissionObject mission = (MissionObject) obj;
                MyLog.d(TAG, "RoundProgressBar click, mission.isPaused:" + mission.isPaused()
                        + ", progressStatus:" + bar.getProgressStatus());
                if (mission.isPaused() || bar.getProgressStatus() == ProgressStatus.error)
                {
                 // to resume.
                    mission.setPaused(false);
                    RunnableBase runnable = createRunnable(mission, type);
                    manager.addOrUpdateMission(mission, bar, runnable);
                    bar.setProgressStatus(ProgressStatus.wait);
                } else if (bar.getProgressStatus() != ProgressStatus.wait)
                {
                    // to pause.
                    mission.setPaused(true);
                    manager.cancelRunnable(mission.getLocalFile());
                    //manager.removeMission(mission.getLocalFile()); // delete when task paused.
                    bar.setProgressStatus(ProgressStatus.wait);
                } else // ProgressStatus.wait
                {
                    RunnableBase runnable = manager.getRunnable(mission.getLocalFile());
                    if (runnable == null)
                    {
                        mission.setPaused(false);
                        RunnableBase runnable2 = createRunnable(mission, type);
                        manager.addOrUpdateMission(mission, bar, runnable2);
                        bar.setProgressStatus(ProgressStatus.wait);
                    } else
                    {
                        if (runnable.isTaskCancel())
                        {
                            mHelper.showResult(OperationEventListener.ERROR_CODE_WAIT_NETWORK);
                        } else
                        {
                            mHelper.showResult(OperationEventListener.ERROR_CODE_TASK_QUEUING);
                        }
                    }
                }
            }
        }
    }
    
    public boolean removeTransferProgressBar(RoundProgressBar progressBar, MissionObject mission, TransferType type)
    {
        boolean retVal = false;
        Object obj = progressBar.getTag();
        TransferProgressManager manager;

        progressBar.setProgress(0);
        progressBar.setProgressStatus(ProgressStatus.wait);

        if (type == TransferType.upload)
        {
            manager = mUploadManager;
        } else
        {
            manager = mDownloadManager;
        }

        if (obj != null && obj.getClass() == MissionObject.class)
        {
            String path = mission.getLocalFile();
            MyLog.i(TAG, "removeTransferProgressBar, before, dismissProgressBar path:" + path);
            retVal = manager.dismissProgressBar(path);
        }

        return retVal;
    }
    
    // flag is true delete local file, false only delete record.
    public void deleteTransferList(List<MissionObject> missions, boolean flag,
            TransferType type, OperationEventListener listener)
    {
        DeleteMissionTask deleteTask = new DeleteMissionTask(missions, flag, type, listener);
        deleteTask.execute();
    }
    
    private Vector<MissionObject> mDeletingVector = new Vector<MissionObject>();
    private class DeleteMissionTask extends BaseAsyncTask
    {
        private List<MissionObject> missions;
        private TransferType type;
        private boolean flag;

        private DeleteMediaStoreHelper mDeleteMediaStoreHelper;

        public DeleteMissionTask(List<MissionObject> missions, 
                boolean flag, TransferType type, OperationEventListener listener)
        {
            super(listener);
            this.missions = missions;
            this.flag = flag;
            this.type = type;
            
            MediaStoreHelper helper = new MediaStoreHelper(mContext);
            mDeleteMediaStoreHelper = new DeleteMediaStoreHelper(helper);
        }

        @Override
        protected void onPostExecute(Integer result)
        {
            super.onPostExecute(result);
            notifyTransferUi(type, TransferUiNotifyType.transferListChanged);
        }

        @Override
        protected Integer doInBackground(Void... params)
        {
            ArrayList<MissionObject> transferringList;
            ArrayList<MissionObject> transferredList;
            TransferProgressManager manager;

            if (type == TransferType.download)
            {
                transferringList = mDownloadingList;
                transferredList = mDownloadedList;
                manager = mDownloadManager;
            } else
            {
                transferringList = mUploadingList;
                transferredList = mUploadedList;
                manager = mUploadManager;
            }

            for (MissionObject mission : missions)
            {
                boolean needDelete = false;
                if (mission.isFinished())
                {
                    transferredList.remove(mission);
                    needDelete = true;
                } else
                {
                    RunnableBase task = manager.getRunnable(mission.getLocalFile());
                    MyLog.i("DeleteMissionTask, file:"+mission.getLocalFile()+", task:" + task);
                    if (task != null)
                    {
                        task.setDelete();
                        task.cancelTask();
                        mDeletingVector.add(mission);
                    } else
                    {
                        needDelete = true;
                        manager.removeMission(mission.getLocalFile());
                    }
                    //transferringList.remove(mission);
                    removeFromTransferring(transferringList, mission);
                }
                
                CloudFileService tool = CloudFileService.getInstance(mContext);
                if (needDelete)
                {
                    try {
                        if (type == TransferType.download)
                        {
                            tool.deleteDownloadOneMission(mission.getKey(), flag);
                            if (flag)
                                mDeleteMediaStoreHelper.addRecord(mission.getLocalFile());
                        } else
                        {
                            tool.deleteUploadOneMission(mission.getKey());
                        }
                    } catch (Exception e) {
                        MyLog.e(TAG, "Delete fail, e:" + e);
                    }
                }
            }

            synchronized (mDeletingVector)
            {
                while (mDeletingVector.size() > 0)
                {
                    try
                    {
                        mDeletingVector.wait();
                    } catch (Exception e)
                    {
                        MyLog.e("wait exception:" + e);
                    }
                }
            }

            if (type == TransferType.download && flag)
            {
                mDeleteMediaStoreHelper.updateRecords();
            }
            return 1;
        }
    }
    
    private void deleteUnfinishedMission(final MissionObject mission, final TransferType type)
    {
        Thread myThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                MyLog.i("deleteUnfinishedMission, threadName:" + Thread.currentThread().getName());
                try
                {
                    if (type == TransferType.download)
                    {
                        CloudFileService.getInstance(mContext)
                            .deleteDownloadOneMission(mission.getKey(), true);
                    } else
                    {
                        CloudFileService.getInstance(mContext)
                            .deleteUploadOneMission(mission.getKey());
                    }
                } catch (Exception e) {
                    MyLog.e("deleteUnfinishedMission, Delete failed, Exception:" + e);

                    if (type == TransferType.upload)
                    {
                        CloudTransferListDAO dao = CloudTransferListDAO.getInstance(mContext);
                        dao.deleteUploadRecordByKey(mUserKey, mission.getKey());
                    }
                } finally
                {
                    MyLog.i("deleteUnfinishedMission, task delete, file:" + mission.getLocalFile());
                    mDeletingVector.remove(mission);
                    synchronized (mDeletingVector)
                    {
                        mDeletingVector.notifyAll();
                    }
                }
            }
        });
        myThread.start();
    }

    private class TransferListener implements RunableListener
    {
        private TransferType type;
        TransferProgressManager manager;

        public TransferListener(TransferType type)
        {
            this.type = type;
            if (type == TransferType.download)
            {
                manager = mDownloadManager;
            } else
            {
                manager = mUploadManager;
            }
        }

        @Override
        public void publishProgress(String filePath, long progress, long total)
        {
            MissionObject mission = manager.getMission(filePath);
            RoundProgressBar bar = manager.getProgressBar(filePath);

            if (mission != null)
            {
                mission.setTransferredLength(progress);
                mission.setFileLength(total);

                if (bar != null && mission.equals(bar.getTag()))
                {
                    if (bar.getProgressStatus() != ProgressStatus.progress && !mission.isPaused())
                    {
                        bar.setProgressStatus(ProgressStatus.progress);
                    }
                    setProgressFromTag(bar);
                }
            }

            MyLog.d(TAG, "publishProgress, file:" + filePath + ", progressBar:" + bar);
        }

        @Override
        public void onTaskResult(String filePath, int result)
        {
            MissionObject mission = manager.getMission(filePath);
            RoundProgressBar bar = manager.getProgressBar(filePath);

            MyLog.d(TAG, "onTaskResult, filePath:" + filePath + ", result:" + result);
            if (mission == null)
                return;
            
            RunnableBase task = manager.getRunnable(filePath);
            if (task != null && task.isDelete())
            {
                deleteUnfinishedMission(mission, type);
                manager.removeMission(filePath);
                return;
            }

            switch (result)
            {
                case OperationEventListener.ERROR_CODE_USER_CANCEL:
                    if (bar != null)
                    {
                        bar.setProgressStatus(ProgressStatus.pause);
                    }
                    break;

                case OperationEventListener.ERROR_CODE_FILE_NOT_EXIST:
                {
                    ArrayList<MissionObject> transferring = getTransferringList(type);
                    int index = transferring.indexOf(mission);
                    removeFromTransferring(transferring, index);
                    deleteUnfinishedMission(mission, type);

                    notifyTransferUi(type, TransferUiNotifyType.transferListChanged);

                    promptSourceFileNotExist(filePath);
                    if (bar != null)
                    {
                        bar.clearAll();
                    }
                }
                    break;

                case OperationEventListener.ERROR_CODE_FILE_EXIST:
                case OperationEventListener.ERROR_CODE_SUCCESS:
                    ArrayList<MissionObject> transferring = getTransferringList(type);
                    ArrayList<MissionObject> transferred = getTransferredList(type);

                    int index;
                    MissionObject myMission = null;
                    for (index = 0; index < transferring.size(); index++)
                    {
                        if (transferring.get(index).equals(mission))
                        {
                            myMission = transferring.get(index);
                            break;
                        }
                    }
                    if (myMission != null && index < transferring.size())
                    {
                        //transferring.remove(index);
                        removeFromTransferring(transferring, index);
                        myMission.setFinished(true);
                        //transferred.add(0, myMission);
                        addToTransferred(transferred, 0, myMission);
                        notifyTransferUi(type, TransferUiNotifyType.transferListChanged);
                    }
                    if (bar != null)
                    {
                        bar.clearAll();
                    }
                    break;

                default:
                    if (bar != null)
                    {
                        bar.setProgressStatus(ProgressStatus.error);
                        //mission.setPaused(true); // Do not set pause, so network is ok, continue upload or download.
                    }
                    mHelper.showResult(result);
                    MyLog.e("onTaskResult, error:" + result + ", file:" + filePath);
                    break;
            }

            manager.removeMission(filePath);
//            if (bar != null)
//            {
//                bar.clearAll();
//            }
        }
        
        private void promptSourceFileNotExist(String filePath)
        {
            String fileName = Util.getNameFromFilepath(filePath);
            String prompt = (type == TransferType.download)
                    ? mContext.getString(R.string.cannot_download_file, fileName)
                    : mContext.getString(R.string.cannot_upload_file, fileName);
            mHelper.showResult(OperationEventListener.ERROR_CODE_FILE_NOT_EXIST, " " + prompt);
        }
    }
    
    private MissionObject removeFromTransferring(ArrayList<MissionObject> transferring, int index)
    {
        synchronized (transferring)
        {
            if (index >= 0 && index < transferring.size())
                return transferring.remove(index);
            else
                return null;
        }
    }

    private boolean removeFromTransferring(ArrayList<MissionObject> transferring, MissionObject mission)
    {
        synchronized (transferring)
        {
            return transferring.remove(mission);
        }
    }
    
    private void addToTransferred(ArrayList<MissionObject> transferred, int index, MissionObject mission)
    {
        synchronized (transferred)
        {
            transferred.add(index, mission);
        }
    }

    private boolean setProgressFromTag(RoundProgressBar progressBar)
    {
        boolean retVal = false;
        Object objMission = progressBar.getTag(R.id.tag_mission);
        Object objSizeView = progressBar.getTag(R.id.tag_sizeview);
        
        if (objMission != null && objMission.getClass() == MissionObject.class)
        {
            MissionObject mission = (MissionObject) objMission;
            if (mission.getFileLength() > 0)
            {
                progressBar.setMax(mission.getFileLength());
                progressBar.setProgress(mission.getTransferredLength());
            }
            
            if (objSizeView != null && objSizeView.getClass() == TextView.class)
            {
                final TextView sizeView = (TextView) objSizeView;
                final StringBuffer fileSize = new StringBuffer();
                fileSize.append(Util.convertStorage(mission.getTransferredLength()));
                fileSize.append("/");
                fileSize.append(Util.convertStorage(mission.getFileLength()));
                sizeView.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        sizeView.setText(fileSize.toString());
                    }
                });
            }
            retVal = true;
        }
        
        return retVal;
    }

    // to notified ui update interface, begin.
    public interface TransferUiListener
    {
        void transferListChanged();
    }

    private enum TransferUiNotifyType
    {
        transferListChanged
    }
    
    private ArrayList<TransferUiListener> mUploadUiListenerList = new ArrayList<TransferService.TransferUiListener>();
    private ArrayList<TransferUiListener> mDownloadUiListenerList = new ArrayList<TransferService.TransferUiListener>();
    
    public void registerTransferUiListener(TransferUiListener listener, TransferType type)
    {
        if (type == TransferType.download)
        {
            mDownloadUiListenerList.add(listener);
        } else
        {
            mUploadUiListenerList.add(listener);
        }
    }
    
    public void unregisterTransferUiListener(TransferUiListener listener, TransferType type)
    {
        if (type == TransferType.download)
        {
            mDownloadUiListenerList.remove(listener);
        } else
        {
            mUploadUiListenerList.remove(listener);
        }
    }
    
    public void notifyTransferUi(TransferType type, TransferUiNotifyType notifyType)
    {
        ArrayList<TransferUiListener> listenerList;
        if (type == TransferType.download)
        {
            listenerList = mDownloadUiListenerList;
        } else
        {
            listenerList = mUploadUiListenerList;
        }
        for (TransferUiListener listener : listenerList)
        {
            if (listener == null)
            {
                continue;
            }
            switch (notifyType)
            {
                case transferListChanged:
                    listener.transferListChanged();
                    break;

                default:
                    break;
            }
        }
    }
    // to notified ui update interface, end.

    // ServerInforListener is used to communication with activities, begin
    private enum InformationType
    {
        userInformationFinish,
        userKeyFinish,
        userInformationChanged,
        userInformationError,
        userLogout,
        networkConnect,
        networkDisconnect,
        storageUnmount,
        storageMount
    }

    public interface ServiceInforListener
    {
        public void userInformationFinish();

        public void userKeyFinish();

        public void userInformationChanged();

        public void userInformationError(int errorCode);

        public boolean userLogout();

        public void networkConnect();

        public void networkDisconnect();

        public void storageUnmount();

        public void storageMount();
    }
    
    private ArrayList<ServiceInforListener> mServiceInforListenerList = new ArrayList<ServiceInforListener>();
    public void registerServerInforListerner(ServiceInforListener listener)
    {
        if ((listener != null) && !mServiceInforListenerList.contains(listener))
        {
            mServiceInforListenerList.add(listener);
            if (mUserInfomation.isInforComplete())
            {
                listener.userInformationFinish();
            }

            if (mIsNetworkConnect)
            {
                listener.networkConnect();
            } else
            {
                listener.networkDisconnect();
            }
        }
    }
    public void unregisterServerInforListerner(ServiceInforListener listener)
    {
        mServiceInforListenerList.remove(listener);
    }

    private boolean notifyServerInfoListener(InformationType type, Object parameter)
    {
        boolean result = false;

        for (ServiceInforListener listener : mServiceInforListenerList)
        {
            if (listener == null)
            {
                continue;
            }
            switch (type)
            {
                case userInformationFinish:
                    listener.userInformationFinish();
                    result = true;
                    break;

                case userKeyFinish:
                    listener.userKeyFinish();
                    result = true;
                    break;

                case userInformationChanged:
                    listener.userInformationChanged();
                    result = true;
                    break;

                case userInformationError:
                    listener.userInformationError((Integer) parameter);
                    result = true;
                    break;

                case userLogout:
                    listener.userLogout();
                    break;

                case networkConnect:
                    listener.networkConnect();
                    result = true;
                    break;

                case networkDisconnect:
                    listener.networkDisconnect();
                    result = true;
                    break;

                case storageUnmount:
                    listener.storageUnmount();
                    result = true;
                    break;

                case storageMount:
                    listener.storageMount();
                    result = true;
                    break;
            }
        }

        return result;
    }

    private boolean mIsNetworkConnect = true;
    private boolean mIsWifiConnect = false;
    private void registerCloudReceiver()
    {
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter2.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter2.addDataScheme("file");

        mContext.registerReceiver(mCloudReceiver, intentFilter1);
        mContext.registerReceiver(mStorageReceiver, intentFilter2);
    }
    private void unregisterCloudReceiver()
    {
        mContext.unregisterReceiver(mCloudReceiver);
        mContext.unregisterReceiver(mStorageReceiver);
    }

    private BroadcastReceiver mCloudReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
            {
                ConnectivityManager connectMgr = (ConnectivityManager) mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                boolean oldStatus = mIsNetworkConnect;
                mIsWifiConnect = false;
                mIsNetworkConnect = false;
                NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo();
                if (mNetworkInfo != null)
                {
                    mIsNetworkConnect = mNetworkInfo.isAvailable();
                    if (mIsNetworkConnect)
                    {
                        if (!mUserInfomation.isLoading())
                        {
                            mUserInfomation.goToGetUserKey();
                        }
                        NetworkInfo wifiInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (wifiInfo != null)
                        {
                            mIsWifiConnect = wifiInfo.isAvailable();
                        }
                    }
                }

                if (mIsNetworkConnect)
                {
                    notifyServerInfoListener(InformationType.networkConnect, null);
                    resumeAllTask();
                } else
                {
                    //Toast.makeText(mContext, R.string.no_network_eror, Toast.LENGTH_SHORT).show();
                    notifyServerInfoListener(InformationType.networkDisconnect, null);
                }
                MyLog.i(TAG, "mCloudReceiver, CONNECTIVITY_ACTION, connect:"
                        + mIsNetworkConnect + ", wifi:" + mIsWifiConnect);
            }
        }

    };
    
    private BroadcastReceiver mStorageReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            MyLog.i(TAG, "mStorageReceiver, action:" + action);
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED))
            {
                if (Util.isSDCardReady())
                {
                    resumeAllTask();
                }
                notifyServerInfoListener(InformationType.storageMount, null);
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED))
            {
                notifyServerInfoListener(InformationType.storageUnmount, null);
            }
        }
    };
    
    public void resumeAllTask()
    {
        resumeTasks(TransferType.upload);
        resumeTasks(TransferType.download);
    }
    
    public void resumeTasks(TransferType type)
    {
        ArrayList<TransferUiListener> listenerList;
        ArrayList<MissionObject> transferringList;
        TransferProgressManager manager;
        
        if (type == TransferType.download)
        {
            listenerList = mDownloadUiListenerList;
            transferringList = mDownloadingList;
            manager = mDownloadManager;
        } else
        {
            listenerList = mUploadUiListenerList;
            transferringList = mUploadingList;
            manager = mUploadManager;
        }
        
        if (listenerList.size() > 0)
        {
            if (transferringList.size() > 0)
                notifyTransferUi(type, TransferUiNotifyType.transferListChanged);
        } else
        {
            for (MissionObject mission : transferringList)
            {
                if (!mission.isFinished() && !mission.isPaused())
                {
                    RunnableBase runnable = manager.getRunnable(mission.getLocalFile());
                    RoundProgressBar progressBar = manager.getProgressBar(mission.getLocalFile());
                    if (runnable == null)
                    {
                        runnable = createRunnable(mission, type);
                        manager.addOrUpdateMission(mission, progressBar, runnable);
                    }
                }
            }
        }
    }

    public void pauseAllTask(TransferType type)
    {
        ArrayList<MissionObject> transferringList;
        TransferProgressManager manager;

        if (type == TransferType.download)
        {
            transferringList = mDownloadingList;
            manager = mDownloadManager;
        } else
        {
            transferringList = mUploadingList;
            manager = mUploadManager;
        }

        for (MissionObject mission : transferringList)
        {
            if (!mission.isFinished() && !mission.isPaused())
            {
                mission.setPaused(true);
                manager.cancelRunnable(mission.getLocalFile());
            }
        }
    }
}

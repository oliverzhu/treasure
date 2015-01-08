package com.ape.cloudfile.task;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;

import com.ape.cloudfile.CloudFileService;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.Util;
import com.ape.filemanager.updatehelper.MediaStoreHelper;
import com.ape.filemanager.updatehelper.MultiMediaStoreHelper.PasteMediaStoreHelper;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

public class TaskDownloadFiles extends TaskCloudFileBase
{
    private static final String TAG = "TaskDownloadFiles";
    private static final int LEAST_DOWNLOAD_SPACE = 50*1024;

    private ArrayList<CloudFile> mDownFiles;
    private String mLocalPath;

    private MediaStoreHelper mMediaProviderHelper;
    private PasteMediaStoreHelper mAddMediaHelper;
    
    private MissionListener missionListener = new MissionListener() {
        protected static final int NEED_UPDATE_TIME = 100;
        private long mLastOperationTime = 0;
        private long totalSize = 0l;

        @Override
        public void setTotalSize(long size) {
            totalSize = size;
            mLastOperationTime = System.currentTimeMillis();

            publishMyProgress(getCurrentFileName(), 0, TOTAL);
        }

        @Override
        public void transferred(long transferred) {
            long currentTime = System.currentTimeMillis();

            if (currentTime - mLastOperationTime > NEED_UPDATE_TIME
                    || transferred > (totalSize -2) )
            {
                String msg = getCurrentFileName();

                publishMyProgress(msg, (int) (transferred * TOTAL / totalSize), TOTAL);
                mLastOperationTime = currentTime;
                
                MyLog.i(TAG, "DownLoad file, transferred:" + transferred + ", totalSize:" + totalSize);
            }
            MyLog.i(TAG, "DownLoad file, transferred:" + transferred);
        }

        @Override
        public boolean isCancel()
        {
            return mIsCancel;
        }
    };

    public TaskDownloadFiles(Context context, OperationEventListener listener,
            ArrayList<CloudFile> fileInfos, String localFile)
    {
        super(context, listener);
        mDownFiles = fileInfos;
        mLocalPath = localFile;
        
        mMediaProviderHelper = new MediaStoreHelper(context);
        mAddMediaHelper = new PasteMediaStoreHelper(mMediaProviderHelper);
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        CloudFileResult result;
        CloudFileService service = CloudFileService.getInstance(mContext);
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        
        if (mDownFiles == null || mDownFiles.size() < 1)
        {
            return OperationEventListener.ERROR_CODE_NAME_EMPTY;
        }

        mCurrentNumber = 0;
        mTotalNumber = mDownFiles.size();

        for (CloudFile file : mDownFiles)
        {
            if (mIsCancel) {
                break;
            }
            publishMyProgress(file.getName(), 0, TOTAL);

            String downLoadPath =  getDownloadPath(file);
            
            if (!isEnoughSpace(file, mLocalPath))
            {
                return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
            }

            createLocalFolder(Util.getPathFromFilepath(downLoadPath));

            if (file.isFile())
            {
                MissionObject mission = service.initDownload(file.getKey(), downLoadPath);
                result = service.downloadfile(mission, missionListener);
    
                ret = handleServerResult(result);
                if (ret != OperationEventListener.ERROR_CODE_SUCCESS)
                {
                    StringBuffer logText = new StringBuffer("DownLoad file failed, resultCode:");
                    logText.append(result.getResultCode());
                    logText.append(", file: ").append(file.getKey());
                    logText.append(", message:").append(result.getMessage());
                    MyLog.e(TAG, logText.toString());
                    break;
                }
            } else
            {
                createLocalFolder(downLoadPath);
            }

            mAddMediaHelper.addRecord(downLoadPath);
            publishMyProgress(file.getName(), TOTAL, TOTAL);
            mCurrentNumber++;
        }

        mAddMediaHelper.updateRecords();

        return ret;
    }

    private String getDownloadPath(CloudFile file)
    {
        String path;
 
        path = mLocalPath;
        if (path.endsWith(File.separator))
        {
            path = path.substring(0, path.length()-1);
        }
        if (file.getKey().startsWith(File.separator))
        {
            path = path + file.getKey();
        } else
        {
            path = path + File.separator + file.getKey();
        }
        if (path.endsWith(File.separator))
        {
            path = path.substring(0, path.length()-1);
        }

        return path;
    }
    
    private boolean isEnoughSpace(CloudFile file, String dstFolder)
    {
        long needSize = file.getLength();
        
        if (needSize <= 0)
        {
            needSize = LEAST_DOWNLOAD_SPACE;
        }
        
        File lFile = new File(dstFolder);
        return (lFile.getFreeSpace() > needSize);
    }
    
    private boolean createLocalFolder(String path)
    {
        boolean retValue = true;

        File lFile = new File(path);
        if (!lFile.exists())
        {
            retValue = lFile.mkdirs();
        }
        
        return retValue;
    }
    
    private String getCurrentFileName()
    {
        String name = null;
        if (mCurrentNumber < mDownFiles.size())
        {
            name = mDownFiles.get(mCurrentNumber).getName();
        }
        return name;
    }
}

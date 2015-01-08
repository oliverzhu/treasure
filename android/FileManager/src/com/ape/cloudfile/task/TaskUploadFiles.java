package com.ape.cloudfile.task;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;

import com.ape.cloudfile.CloudFileService;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.MyLog;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

public class TaskUploadFiles extends TaskCloudFileBase
{
    private static final String TAG = "TaskUploadFiles";
    private ArrayList<FileInfo> mUploadFiles;
    private String mCloudPath;


    private MissionListener missionListener = new MissionListener() {
        protected static final int TOTAL = 100;
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
            }
        }

        @Override
        public boolean isCancel()
        {
            return mIsCancel;
        }
    };

    public TaskUploadFiles(Context context, OperationEventListener listener,
            ArrayList<FileInfo> fileInfos, String cloudPath)
    {
        super(context, listener);

        mUploadFiles = fileInfos;
        mCloudPath = cloudPath;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        CloudFileResult result;
        CloudFileService service = CloudFileService.getInstance(mContext);
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;

        if (mUploadFiles == null || mUploadFiles.size() < 1)
        {
            return OperationEventListener.ERROR_CODE_NAME_EMPTY;
        }

        mCurrentNumber = 0;
        mTotalNumber = mUploadFiles.size();

        String localPath = getLocalPath();

        for (FileInfo fileInfo : mUploadFiles)
        {
            publishMyProgress(fileInfo.fileName, 0, TOTAL);

            String cloudName = null;
            if (localPath != null
                    && fileInfo.filePath.startsWith(localPath)
                    && localPath.length() < fileInfo.filePath.length())
            {
                cloudName = mCloudPath + 
                        fileInfo.filePath.substring(localPath.length());
            } else
            {
                cloudName = mCloudPath + fileInfo.fileName;
            }

            if (fileInfo.IsDir)
            {
                if (!cloudName.endsWith(File.separator))
                    cloudName = cloudName + File.separator;
                result = service.createDir(cloudName);
            } else
            {
                MissionObject mission = service.initMultipartUpload(fileInfo.filePath, cloudName);
                result = service.multipartUploadFile(mission, missionListener);
            }

            ret = handleServerResult(result);
            if (ret != OperationEventListener.ERROR_CODE_SUCCESS)
            {
                StringBuffer logText = new StringBuffer("Uploadfile failed, resultCode:");
                logText.append(result.getResultCode());
                logText.append(", file:").append(fileInfo.filePath);
                logText.append(", message:").append(result.getMessage());
                MyLog.e(TAG, logText.toString());
                break;
            }

            CloudFile myFile = new CloudFile(cloudName,
                    getParentPathWithSeparator(cloudName),
                    fileInfo.fileName,
                    fileInfo.fileSize,
                    System.currentTimeMillis(),
                    !fileInfo.IsDir);
            mFileDao.insert(myFile);
            
            publishMyProgress(fileInfo.fileName, TOTAL, TOTAL);
            mCurrentNumber++;
        }

        return ret;
    }

    private String getLocalPath()
    {
        if (mUploadFiles.size() <= 1)
        {
            return null;
        }
        
        FileInfo file = mUploadFiles.get(0);
        if (file.IsDir)
        {
            int pos = file.filePath.lastIndexOf(File.separatorChar);
            if (pos != -1)
            {
                return file.filePath.substring(0, pos + 1);
            }
        }
        
        return null;
    }

    private String getParentPathWithSeparator(String fileName)
    {
        String myFileName;

        if (fileName.endsWith(File.separator))
        {
            myFileName = fileName.substring(0, fileName.length()-1);
        } else
        {
            myFileName = fileName;
        }
        
        int pos = myFileName.lastIndexOf(File.separator);
        if (pos != -1)
        {
            return myFileName.substring(0, pos + 1);
        }

        return File.separator;
    }
    
    private String getCurrentFileName()
    {
        String name = null;
        if (mCurrentNumber < mUploadFiles.size())
        {
            name = mUploadFiles.get(mCurrentNumber).fileName;
        }
        return name;
    }
}

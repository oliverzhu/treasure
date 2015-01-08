package com.ape.cloudfile.task;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;

import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.accessDAO.CloudTransferListDAO;
import com.ape.filemanager.FileCategoryHelper;
import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.ape.filemanager.MyLog;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;

public class TaskDeleteFiles extends TaskCloudFileBase
{
    private static final String TAG = "TaskDeleteFiles";
    private ArrayList<CloudFile> mDeleteFiles = new ArrayList<CloudFile>();
    
    public TaskDeleteFiles(Context context, OperationEventListener listener,
            ArrayList<CloudFile> deleteFiles)
    {
        super(context, listener);

        mDeleteFiles.addAll(deleteFiles);
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        CloudFileResult result;
        CloudFileService service = CloudFileService.getInstance(mContext);
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;

        if (mDeleteFiles == null || mDeleteFiles.size() < 1)
        {
            return OperationEventListener.ERROR_CODE_NAME_EMPTY;
        }

        mCurrentNumber = 0;
        mTotalNumber = mDeleteFiles.size();

        for (CloudFile file : mDeleteFiles)
        {
            if (mIsCancel) {
                break;
            }
            publishMyProgress(file.getName(), mCurrentNumber, mTotalNumber);

            if (file.isFile())
            {
                deleteThumbnailFile(file.getKey(), service);
                result = service.deleteFile(file.getKey());
                deleteTransferRecord(service, file.getKey(), false);
            } else
            {
                result = service.deleteDir(file.getKey());
                deleteTransferRecord(service, file.getKey(), true);
            }
            mCurrentNumber++;

            ret = handleServerResult(result);
            if (ret != OperationEventListener.ERROR_CODE_SUCCESS)
            {
                StringBuffer logText = new StringBuffer("Delete file failed, resultCode:");
                logText.append(result.getResultCode());
                logText.append(", file:").append(file.getKey());
                MyLog.e(TAG, logText.toString());
                break;
            }

            mFileDao.delete(file);
        }

        return ret;
    }

    private boolean deleteThumbnailFile(String cloudPath, CloudFileService service)
    {
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(cloudPath);
        if (fc != FileCategory.Picture)
            return true;

        String cloudThumb = CloudFileUtil.getCloudThumbPath(cloudPath);
        String localThumb = CloudFileUtil.getLocalCachePath(cloudPath);

        service.deleteFile(cloudThumb);
        File thumbFile = new File(localThumb);
        if (thumbFile.exists())
        {
            thumbFile.delete();
        }

        return true;
    }

    private void deleteTransferRecord(CloudFileService service, String key, boolean isDir)
    {
        try
        {
            if (isDir)
            {
                CloudTransferListDAO dao = CloudTransferListDAO.getInstance(mContext);
                dao.deleteUploadRecordByDir(service.getUserId(), key);
            } else
            {
                service.deleteUploadOneMission(key);
            }
        } catch (Exception e)
        {
            MyLog.e(TAG, "deleteTransferRecord, failed, key:" + key);
        }
    }
}

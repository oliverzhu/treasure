package com.ape.cloudfile.task;

import java.io.File;

import android.content.Context;
import android.os.Environment;

import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.cloudfile.accessDAO.CloudFileAccessDAO;
import com.ape.cloudfile.cloudImage.GalleryImageTool;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.FileCategoryHelper;
import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.Util;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionObject;

public class RunnableUpload extends RunnableBase
{
    private static final String TAG = "RunnableUpload";
    private GalleryImageTool imageTool;

    public RunnableUpload(Context context, RunableListener listener,
            MissionObject mission)
    {
        super(context, listener, mission);
        imageTool = new GalleryImageTool(CloudFileService.getInstance(mContext));
    }

    @Override
    public void run()
    {
        super.run();

        CloudFileResult result;
        CloudFileService service = CloudFileService.getInstance(mContext);
        File localFile = new File(mLocalPath);
        boolean isFile = localFile.isFile();
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;

        if (!localFile.exists())
        {
            postResult(OperationEventListener.ERROR_CODE_FILE_NOT_EXIST);
            return;
        }

        if (!createThumbnailFile())
        {
            postResult(OperationEventListener.ERROR_CODE_UNSUCCESS);
            return;
        }

        result = service.multipartUploadFile(mMission, missionListener);
        ret = TaskCloudFileBase.handleServerResult(result);

        if (ret != OperationEventListener.ERROR_CODE_SUCCESS)
        {
            StringBuffer logText = new StringBuffer("RunnableUpload failed, resultCode:");
            logText.append(result.getResultCode());
            logText.append(", file:").append(mLocalPath);
            logText.append(", message:").append(result.getMessage());
            MyLog.e(TAG, logText.toString());
        } else
        {
            
            CloudFile myFile = new CloudFile(mCloudPath,
                    getParentPathWithSeparator(mCloudPath),
                    Util.getNameFromFilepath(mLocalPath),
                    localFile.length(),
                    System.currentTimeMillis(),
                    isFile);
            CloudFileAccessDAO.getInstance(mContext).insert(myFile);
            
            if (mListener != null)
            {
                mListener.publishProgress(mLocalPath, mMission.getFileLength(), mMission.getFileLength());
            }
        }
        
//        if (mListener != null)
//        {
//            mListener.onTaskResult(mLocalPath, ret);
//        }
        postResult(ret);
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

    private boolean createThumbnailFile()
    {
        if (mMission.getTransferredLength() > 0)
        {
            return true;
        }

        boolean retVal = true;
        String cloudPath = mMission.getKey();
        String localPath = mMission.getLocalFile();
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(cloudPath);

        String localThumbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CloudFileUtil.LOCAL_CACHE_DIR;
        if (localPath.startsWith(localThumbPath))
        {
            return true;
        }

        switch (fc)
        {
            case Picture:
                retVal = imageTool.createCloudCacheThumbnail(localPath, cloudPath);
                break;

            default:
                break;
        }

        return retVal;
    }
}

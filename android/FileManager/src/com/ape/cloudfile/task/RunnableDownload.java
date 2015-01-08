package com.ape.cloudfile.task;

import java.io.File;

import android.content.Context;
import android.os.Looper;

import com.ape.cloudfile.CloudFileService;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.updatehelper.MediaStoreHelper;
import com.ape.filemanager.updatehelper.MultiMediaStoreHelper.PasteMediaStoreHelper;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.Util;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionObject;

public class RunnableDownload extends RunnableBase
{
    private static final String TAG = "RunnableDownload";

    public RunnableDownload(Context context, RunableListener listener,
            MissionObject mission)
    {
        super(context, listener, mission);
    }

    @Override
    public void run()
    {
        super.run();

        if (Looper.myLooper() == null)
            Looper.prepare();

        CloudFileResult result;
        CloudFileService service = CloudFileService.getInstance(mContext);
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        
        createFolder();
        
        result = service.downloadfile(mMission, missionListener);
        ret = TaskCloudFileBase.handleServerResult(result);

        if (ret == OperationEventListener.ERROR_CODE_SUCCESS)
        {
            MediaStoreHelper mediaProviderHelper = new MediaStoreHelper(mContext);
            PasteMediaStoreHelper addMediaHelper = new PasteMediaStoreHelper(mediaProviderHelper);
            addMediaHelper.addRecord(mLocalPath);
            addMediaHelper.updateRecords();
        } else
        {
            StringBuffer logText = new StringBuffer("RunnableDownload failed, resultCode:");
            logText.append(result.getResultCode());
            logText.append(", file:").append(mLocalPath);
            logText.append(", message:").append(result.getMessage());
            MyLog.e(logText.toString());
        }

//        if (mListener != null)
//        {
//            mListener.onTaskResult(mLocalPath, ret);
//        }
        postResult(ret);
        
        Looper looper = Looper.myLooper();
        if (looper != null && Thread.currentThread().isAlive())
        {
            looper.quit();
        }
    }
    
    private void createFolder()
    {
        String folder = Util.getPathFromFilepath(mMission.getLocalFile());
        File folderFile = new File(folder);
        if (!folderFile.exists())
        {
            folderFile.mkdirs();
        }
    }
}

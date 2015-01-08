package com.ape.cloudfile;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ape.cloudfile.accessDAO.CloudFileAccessDAO;
import com.ape.cloudfile.cloudImage.CloudImageViewActivity;
import com.ape.cloudfile.task.TaskCreateDir;
import com.ape.cloudfile.task.TaskDeleteFiles;
import com.ape.cloudfile.task.TaskDownloadFiles;
import com.ape.cloudfile.task.TaskListFiles;
import com.ape.cloudfile.task.TaskUploadFiles;
import com.ape.cloudfile.transfer.TransferFilesActivity;
import com.ape.filemanager.BaseAsyncTask;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.ape.filemanager.FileCategoryHelper;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.IntentBuilder;
import com.ape.filemanager.R;
import com.cloud.client.file.CloudFile;

public class CloudFileOperator
{
    private Context mContext;
    private Context mFirstContext;
    private BaseAsyncTask mCurrentTask;

    public CloudFileOperator(Context context)
    {
        mContext = context.getApplicationContext();
        mFirstContext = context;
    }

    public boolean isNetworkConnected()
    { 
        if (mContext != null)
        { 
            ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext 
                .getSystemService(Context.CONNECTIVITY_SERVICE); 
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
            if (mNetworkInfo != null)
            { 
                return mNetworkInfo.isAvailable(); 
            } 
        } 
        return false; 
    }

    public void showResult(int result)
    {
        showResult(result, null);
    }

    public void showResult(int result, String prompt)
    {
        boolean longShow = false;
        boolean needShowDialog = false;
        String resultStr = null;
        switch (result)
        {
            case OperationEventListener.ERROR_CODE_SUCCESS:
                resultStr = mContext.getString(R.string.operation_success);
                break;

            case OperationEventListener.ERROR_CODE_NETWORK_TIMEOUT:
            case OperationEventListener.ERROR_CODE_NETWORK_ERROR:
                resultStr = mContext.getString(R.string.network_connect_error);
                break;

            case OperationEventListener.ERROR_CODE_UNSUCCESS:
                resultStr = "Failed";
                break;

            case OperationEventListener.ERROR_CODE_USER_CANCEL:
                resultStr = mContext.getString(R.string.operation_cancel);
                break;

            case OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE:
                resultStr = mContext.getString(R.string.insufficient_memory);
                break;

            case OperationEventListener.ERROR_CODE_FILE_NOT_EXIST:
                resultStr = mContext.getString(R.string.source_file_not_exist) + prompt;
                longShow = true;
                break;

            case OperationEventListener.ERROR_CODE_FILE_EXIST:
                resultStr = mContext.getString(R.string.fail_to_create_folder,
                        mContext.getString(R.string.file_exist));
                needShowDialog = true;
                break;

            case OperationEventListener.ERROR_CODE_WAIT_NETWORK:
                resultStr = mContext.getString(R.string.wait_network_response);
                break;

            case OperationEventListener.ERROR_CODE_TASK_QUEUING:
                resultStr = mContext.getString(R.string.task_queueing);
                break;

            case OperationEventListener.ERROR_CODE_CANNOT_UPLOAD:
                resultStr = "User key invalid";

        }

        if (resultStr != null)
        {
            if (needShowDialog)
            {
                AlertDialog dialog = new AlertDialog.Builder(mFirstContext)
                    .setMessage(resultStr)
                    .setPositiveButton(R.string.confirm, null).create();
                dialog.show();
            } else
            {
                int showTime = longShow ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
                Toast.makeText(mContext, resultStr, showTime).show();
            }
        }
    }

    public void doUploadFiles(OperationEventListener listener,
            ArrayList<FileInfo> filesList, String cloudPath)
    {
        TaskUploadFiles uploadTask = new TaskUploadFiles(mContext,
                listener, filesList, cloudPath);
        uploadTask.execute();
        mCurrentTask = uploadTask;
    }
    
    public void doDownLoadFiles(OperationEventListener listener,
            ArrayList<CloudFile> filesList, String localPath)
    {
        TaskDownloadFiles downloadTask = new TaskDownloadFiles(mContext,
                listener, filesList, localPath);
        downloadTask.execute();
        mCurrentTask = downloadTask;
    }

    public void doListFiles(OperationEventListener listener, String path, boolean needCache)
    {
        TaskListFiles listTask = new TaskListFiles(mContext, listener, path);
        listTask.setNeedCache(needCache);
        //listTask.execute();
        listTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mCurrentTask = listTask;
    }
    
    public void doDeleteFiles(OperationEventListener listener,
            ArrayList<CloudFile> filesList)
    {
        TaskDeleteFiles deleteTask = new TaskDeleteFiles(mContext,
                listener, filesList);
        deleteTask.execute();
        mCurrentTask = deleteTask;
    }
    
    public void doCreateDir(OperationEventListener listener,
            String path, String dirName)
    {
        TaskCreateDir createDir = new TaskCreateDir(mContext, listener,
                path, dirName);
        createDir.execute();
        mCurrentTask = createDir;
    }

    public boolean cancelCurrentTask()
    {
        boolean ret = false;

        if (mCurrentTask != null && !mCurrentTask.isTaskCancelled())
        {
            mCurrentTask.cancelTask();
        }
        
        return ret;
    }
    
    public void doClearCache()
    {
        CloudFileAccessDAO myDao = CloudFileAccessDAO.getInstance(mContext);

        myDao.deleteAll();
    }

    public boolean isFileExist(String filePath)
    {
        CloudFileAccessDAO myDao = CloudFileAccessDAO.getInstance(mContext);
        CloudFile cloudFile = new CloudFile(filePath, null, 0, 0, 0);
        return myDao.isExist(cloudFile);
    }

    public boolean viewCloudFile(CloudFile file, boolean associatedView)
    {
        boolean result = false;
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(file.getKey());
        File localFile = new File(CloudFileUtil.getDownloadPath() + file.getKey());

        if (fc == FileCategory.Picture)
        {
            Intent intent = new Intent(mContext, CloudImageViewActivity.class);
            intent.putExtra(CloudFileUtil.VIEW_CLOUD_IMAGE_TAG, file.getKey());
            intent.putExtra(CloudFileUtil.VIEW_ONE_IMAGE_MODE_TAG, !associatedView);
            mFirstContext.startActivity(intent);
            result = true;
        } else if (localFile.exists())
        {
            IntentBuilder.viewFile(mFirstContext, localFile.getAbsolutePath(), true);
        } else
        {
            final String filePath = file.getKey();
            Dialog dialog = new AlertDialog.Builder(mFirstContext)
                    .setTitle(mContext.getString(R.string.cloud_kindly_prompt_title))
                    .setMessage(mContext.getString(R.string.view_cloud_file_prompt))
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            downloadFileToView(filePath);
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            return;
                        }
                    }).create();
            dialog.show();
            result = false;
        }

        return result;
    }

    private void downloadFileToView(final String filePath)
    {
        boolean isWifiConnent = false;
        ConnectivityManager connectMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo mNetworkInfo = connectMgr.getActiveNetworkInfo(); 
        if (mNetworkInfo != null && mNetworkInfo.isAvailable())
        { 
            NetworkInfo wifiInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiInfo != null)
            {
                isWifiConnent = wifiInfo.isAvailable();
            }
        } else
        {
            Toast.makeText(mContext, R.string.no_network_eror, Toast.LENGTH_SHORT).show();
            return;
        }
 
        if (isWifiConnent)
        {
            gotoTransferListToDownload(filePath);
        } else
        {
            Dialog dialog = new AlertDialog.Builder(mFirstContext)
                .setTitle(mContext.getString(R.string.cloud_kindly_prompt_title))
                .setMessage(mContext.getString(R.string.no_wifi_prompt))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        gotoTransferListToDownload(filePath);
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        return;
                    }
                }).create();
            dialog.show();
        }
    }

    private void gotoTransferListToDownload(String filePath)
    {
        Intent intent = new Intent(mContext, TransferFilesActivity.class);
        intent.putExtra(CloudFileUtil.FOCUS_TAB_INDEX, CloudFileUtil.DOWNLOAD_TAB_INDEX);
        intent.putExtra(CloudFileUtil.TRANSFER_DEST_PATH_TAG, CloudFileUtil.getDownloadPath());
        ArrayList<String> downloadList = new ArrayList<String>();
        downloadList.add(filePath);
        intent.putStringArrayListExtra(CloudFileUtil.TRANSFER_SRC_FILES_TAG, downloadList);
        mFirstContext.startActivity(intent);
    }
}

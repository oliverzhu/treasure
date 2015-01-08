package com.ape.cloudfile;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;

import com.ape.cloudfile.task.TaskDeleteFiles;
import com.ape.cloudfile.transfer.TransferFilesActivity;
import com.ape.cloudfile.widget.ApeProgressDialog;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.R;
import com.cloud.client.file.CloudFile;

public abstract class CloudFileOperationActivity extends Activity implements OperationEventListener
{
    private ApeProgressDialog mProgressDialog;

    protected void downloadCloudFile(CloudFile cloudFile)
    {
        Intent intent = new Intent(this, TransferFilesActivity.class);
        intent.putExtra(CloudFileUtil.FOCUS_TAB_INDEX, CloudFileUtil.DOWNLOAD_TAB_INDEX);
        intent.putExtra(CloudFileUtil.TRANSFER_DEST_PATH_TAG, CloudFileUtil.getDownloadPath());
        ArrayList<String> downloadList = new ArrayList<String>();
        downloadList.add(cloudFile.getKey());
        intent.putStringArrayListExtra(CloudFileUtil.TRANSFER_SRC_FILES_TAG, downloadList);
        startActivity(intent);
    }

    protected void deleteCloudFile(CloudFile cloudFile)
    {
        ArrayList<CloudFile> deleteList = new ArrayList<CloudFile>();
        deleteList.add(cloudFile);

        TaskDeleteFiles deleteTask = new TaskDeleteFiles(this, this, deleteList);
        deleteTask.execute();
    }

    private void showProgressDialog(String title)
    {
        mProgressDialog = new ApeProgressDialog(this);
        mProgressDialog.setTitle(title);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void dismissProgressDialog()
    {
        if (mProgressDialog != null)
        {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    //For OperationEventListener, begin.
    @Override
    public void onTaskPrepare()
    {
        showProgressDialog(getString(R.string.operation_deleting));
    }

    @Override
    public void onTaskProgress(ProgressInfo progress)
    {
        
    }

    @Override
    public void onTaskResult(int result)
    {
        dismissProgressDialog();
        
    }
    //For OperationEventListener, end.
}

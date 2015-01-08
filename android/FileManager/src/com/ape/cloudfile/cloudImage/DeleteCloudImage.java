package com.ape.cloudfile.cloudImage;

import com.ape.cloudfile.widget.ApeProgressDialog;
import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.ProgressInfo;

import android.content.Context;

public class DeleteCloudImage
{
    private Context mContext;

    public DeleteCloudImage(Context context)
    {
        mContext = context;
    }
    
    public void delete(String cloudPath)
    {
        
    }
    
    private class DeleteFileListener implements OperationEventListener
    {
        private ApeProgressDialog mProgressDialog;

        @Override
        public void onTaskPrepare()
        {
            mProgressDialog = new ApeProgressDialog(mContext);
            
        }

        @Override
        public void onTaskProgress(ProgressInfo progress)
        {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onTaskResult(int result)
        {
            // TODO Auto-generated method stub
            
        }
        
    }
}

package com.client.customerservicecenter.job;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.hub.CloudCustomerServiceHandler;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;
import com.cloud.client.file.MissionObject;

/**
 * 上传图片
 * @author jianwen.zhu
 * @since 2014/9/17
 */
public  class SubmitPictureJob implements ThreadPool.Job<Void>{
	private Context mContext;
    private Handler mHandler;
    private ArrayList<Picture> mFiles;
    private Map mUserInfoMap;
    private MissionObject missionObject;
	public SubmitPictureJob(Context context,ArrayList<Picture> files,Handler handler,Map userInfoMap)
	{
		this.mContext = context;
		this.mHandler = handler;
		this.mFiles = files;
		this.mUserInfoMap = userInfoMap;
	}
	

	@Override
	public Void run(JobContext jc) {
		try {
			for(int i = 1;i < mFiles.size();i++)
			{
				String file = mFiles.get(i).path;
				
				if(file != null && file.trim().length() != 0)
		    	{
		    		String fileName = file.substring((file.lastIndexOf("/") + 1));
		        	CloudCustomerServiceHandler testCloudService = CloudCustomerServiceHandler.getInstance(mContext);
		        	testCloudService.allocFileClient(mUserInfoMap);
		            missionObject = testCloudService.initMultipartUpload(file, fileName);
		            int resultCode = testCloudService.multipartUploadFile(missionObject, null).getResultCode();
		            if(resultCode < 0)
		            {
		            	Message msg = mHandler.obtainMessage();
		            	msg.what = Constants.MSG_SUBMIT_PICTURE_FAIL;
		            	mHandler.sendMessage(msg);
		            	return null;
		            }
		            
		    	}
			}
			
			Message msg = mHandler.obtainMessage();
        	msg.what = Constants.MSG_SUBMIT_PICTURE_SUCCESS;
        	mHandler.sendMessage(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}

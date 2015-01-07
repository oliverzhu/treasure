package com.client.customerservicecenter.job;

import java.io.File;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;

/**
 * @author jianwen.zhu
 * @since 2014/10/31
 */
public  class DeleteUploadImgJob implements ThreadPool.Job<Void>{
	private Context mContext;
	private Handler mHandler;
	private Picture mPicture;
	public DeleteUploadImgJob(Context context,Handler handler,Picture p) {
		mContext = context;
		mPicture = p;
		mHandler = handler;
	}

	@Override
	public Void run(JobContext jc) {
		String path = mContext.getExternalCacheDir() 
				+ Constants.DIR_UPLOAD_PIC_CACHE 
				+ File.separator + mPicture.key;
		File file = new File(path);
		if(file.exists())
		{
			file.delete();
		}
		Message msg = mHandler.obtainMessage();
		msg.what = Constants.MSG_DELETE_UPLOAD_IMG;
		msg.obj = mPicture;
		mHandler.sendMessage(msg);
		return null;
	}

	
}

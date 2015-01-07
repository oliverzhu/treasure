package com.client.customerservicecenter.util.bitmap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.job.LoadNetImageJob;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;

/**
 * 加载普通图片
 * @author jianwen.zhu
 * 2014/9/29
 */
public class ThumbNailLoader extends BitmapLoader {
	protected Context mContext;
	protected String mItemId;
	protected String mfileName;
	protected int mType;
	protected int mTargetWidth;
	protected int mTargetHeight;
	protected int mPos;
	protected ImageCallback mImageCallback;
	
	
	
	@SuppressLint("HandlerLeak")
	final Handler handler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			mImageCallback.imageLoaded((Bitmap) msg.obj, mItemId,mPos);
		}
	};
	
	/**
	 * 回调接口，用于图片下载完成之后修改ui的
	 */
	public interface ImageCallback {

		public void imageLoaded(Bitmap bitmap, String itemId,int pos);

	}

	public ThumbNailLoader(Context context,String itemId,String fileName,int type,int targetWidth,int targetHeight,int pos,ImageCallback imageCallback) {
		mContext = context;
		mItemId = itemId;
		mfileName = fileName;
		mPos = pos;
		mType = type;
		mTargetWidth = targetWidth;
		mTargetHeight = targetHeight;
		mImageCallback = imageCallback;
	}

	@Override
	protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
		return AppApplication.threadPool.submit(new LoadNetImageJob(mContext, mfileName, mType, mTargetWidth, mTargetHeight), l, ThreadPool.MODE_NETWORK);
	}

	@Override
	protected void recycleBitmap(Bitmap bitmap) {
		BitmapPool pool = AppApplication.bitmapHub.getMicroThumbPool();
		if (pool != null)
			pool.recycle(bitmap);
	}

	@Override
	protected void onLoadComplete(Bitmap bitmap) {
		Message msg=new Message();
		msg.obj = bitmap;
		handler.sendMessage(msg);
	}
}

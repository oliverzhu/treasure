package com.home.hub;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.home.bean.OperatingBean;
import com.home.job.LoadMessagesJob;
import com.home.job.NetImageRequestJob;
import com.home.job.UpdateAPKJob;
import com.home.util.Constants;
import com.home.util.bitmap.BitmapPool;
import com.home.util.cache.BytesBufferPool;
import com.home.util.cache.ImageCacheService;
import com.home.util.thread.Future;
import com.home.util.thread.FutureListener;
import com.home.util.thread.ThreadPool;

/**
 * @author jianwen.zhu
 * 2013/12/9
 */
public class AppInteractionHub {
	private Object mLock = new Object();
	private ThreadPool mThreadPool;
	private ImageCacheService mImageCacheService;
	private BitmapPool sMicroThumbPool;
	private static final BitmapPool sThumbPool = new BitmapPool(4);
	private static int sMicrothumbnailTargetWidthSize = 200;
	private static int sMicrothumbnailTargetHeightSize = 200;
	private static int sThumbnailTargetSize = 640;
	private static final int BYTESBUFFE_POOL_SIZE = 4;
	private static final int BYTESBUFFER_SIZE = 200 * 1024;
	private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(
			BYTESBUFFE_POOL_SIZE, BYTESBUFFER_SIZE);
	
	public AppInteractionHub()
	{
		mThreadPool = new ThreadPool();
	}
	
	/**
	 * update apk
	 * @param context
	 * @param url
	 */
	public Future<Void> submitUpgradeAPKJob(Context context,String url)
	{
		return mThreadPool.submit(new UpdateAPKJob(context, url), null, ThreadPool.MODE_NETWORK);
	}
	
	/**
	 * load message
	 * @param listener
	 * @return
	 */
	public Future<ArrayList<OperatingBean>> submitLoadMessagesJob(FutureListener<ArrayList<OperatingBean>> listener)
	{
		return mThreadPool.submit(new LoadMessagesJob(), listener, ThreadPool.MODE_NETWORK);
	}
	
	/**
	 * load thumbnail img
	 * @param listener
	 * @return
	 */
	public Future<Bitmap> submitLoadThumbnailImageJob(
			Context context,String file,int type,int targetWidth,int targetHeight,FutureListener<Bitmap> listener)
	{
		return mThreadPool.submit(new NetImageRequestJob(context, file, type, targetWidth, targetHeight), listener, ThreadPool.MODE_NETWORK);
	}
	
	
	
	public ImageCacheService getImageCacheService(Context context) {
		// This method may block on file I/O so a dedicated lock is needed here.
		synchronized (mLock) {
			if (mImageCacheService == null) {
				mImageCacheService = new ImageCacheService(context);
			}
			return mImageCacheService;
		}
	}
	
	public  BytesBufferPool getBytesBufferPool() {
		return sMicroThumbBufferPool;
	}
	
	public  BitmapPool getMicroThumbPool() {
		if (sMicroThumbPool == null) {
			initializeMicroThumbPool();
		}
		return sMicroThumbPool;
	}
	
	public BitmapPool getThumbPool() {
		return sThumbPool;
	}
	
	private void initializeMicroThumbPool() {
		sMicroThumbPool = new BitmapPool(sMicrothumbnailTargetWidthSize,
				sMicrothumbnailTargetHeightSize, 16);
	}
	
	public int getTargetSize(int type) {
		switch (type) {
		case Constants.TYPE_THUMBNAIL:
			return sThumbnailTargetSize;
		case Constants.TYPE_MICROTHUMBNAIL:
			return sMicrothumbnailTargetWidthSize < sMicrothumbnailTargetHeightSize ? sMicrothumbnailTargetWidthSize : sMicrothumbnailTargetHeightSize;

		default:
			throw new RuntimeException(
					"should only request thumb/microthumb from cache");
		}
	}
	
}

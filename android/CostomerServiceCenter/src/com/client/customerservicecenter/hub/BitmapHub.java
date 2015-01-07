package com.client.customerservicecenter.hub;

import android.content.Context;

import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.bitmap.BitmapPool;
import com.client.customerservicecenter.util.cache.BytesBufferPool;
import com.client.customerservicecenter.util.cache.ImageCacheService;

/**
 * 加载bitmap
 * @author jianwen.zhu
 * 2014/9/29
 */
public class BitmapHub {
	private Object mLock = new Object();
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
	
	public BitmapHub()
	{
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

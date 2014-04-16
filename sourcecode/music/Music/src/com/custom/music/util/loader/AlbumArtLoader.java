package com.custom.music.util.loader;

import java.io.FileDescriptor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import com.custom.music.util.MusicUtils;
import com.custom.music.util.bitmap.BitmapLoader;
import com.custom.music.util.bitmap.BitmapPool;
import com.custom.music.util.thread.Future;
import com.custom.music.util.thread.FutureListener;

/**
 * @author jianwen.zhu
 * 2013/12/11
 */
public class AlbumArtLoader extends BitmapLoader {
	private Context mContext;
	private FileDescriptor mFileDescriptor;
	private String mfileName;
	private int mType;
	private int mTargetWidth;
	private int mTargetHeight;
	private int mPos;
	private ImageCallback mImageCallback;
	
	
	
	@SuppressLint("HandlerLeak")
	final Handler handler=new Handler()
	{
		@Override
		public void handleMessage(Message msg) 
		{
			mImageCallback.imageLoaded((Bitmap) msg.obj, mfileName,mPos);
		}
	};
	
	/**
	 * 回调接口，用于图片下载完成之后修改ui的
	 */
	public interface ImageCallback {

		public void imageLoaded(Bitmap bitmap, String imageUrl,int pos);

	}

	public AlbumArtLoader(Context context,FileDescriptor fileDescriptor,String fileName,int type,int targetWidth,int targetHeight,int pos,ImageCallback imageCallback) {
		mContext = context;
		mFileDescriptor = fileDescriptor;
		mfileName = fileName;
		mPos = pos;
		mType = type;
		mTargetWidth = targetWidth;
		mTargetHeight = targetHeight;
		mImageCallback = imageCallback;
	}

	@Override
	protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
		return MusicUtils.submitAlbumArtJob(mContext, mFileDescriptor,mfileName, mType, mTargetWidth, mTargetHeight, l);
	}

	@Override
	protected void recycleBitmap(Bitmap bitmap) {
		BitmapPool pool = MusicUtils.getMicroThumbPool();
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

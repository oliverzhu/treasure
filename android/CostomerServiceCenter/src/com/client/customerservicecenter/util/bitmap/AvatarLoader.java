package com.client.customerservicecenter.util.bitmap;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.job.LoadNetAvatarJob;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;

/**
 * 加载头像
 * @author jianwen.zhu
 *
 */
public class AvatarLoader extends ThumbNailLoader {
	private Map<String,String> userInfoMap;

	public AvatarLoader(Context context, String itemId,Map<String,String> userInfoMap,String fileName, int type,
			int targetWidth, int targetHeight, int pos,
			ImageCallback imageCallback) {
		super(context, itemId,fileName, type, targetWidth, targetHeight, pos, imageCallback);
		this.userInfoMap = userInfoMap;
	}
	
	@Override
	protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l) {
		return AppApplication.threadPool.submit(new LoadNetAvatarJob(mContext, userInfoMap,mfileName, mType, mTargetWidth, mTargetHeight), l, ThreadPool.MODE_NETWORK);
	}

}

package com.client.customerservicecenter.job;

import java.io.File;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.hub.CloudCustomerServiceHandler;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Log;
import com.client.customerservicecenter.util.bitmap.DecodeUtils;
import com.client.customerservicecenter.util.thread.ThreadPool.JobContext;
import com.cloud.client.file.MissionObject;

/**
 * 加载头像
 * @author jianwen.zhu
 * @since 2014/9/29
 */
public class LoadNetFeedbackPictureJob extends ImageCacheRequestJob {
	private static final String TAG = "LocalImageRequestJob";
    private String avatarKey;
    private Map<String,String> userInfoMap;
	public LoadNetFeedbackPictureJob(Context context, Map<String,String> userInfoMap,
			String path, int type,
			int targetWidthSize, int targetHeightSize) {
		super(context, path, type, targetWidthSize, targetHeightSize, 0);
		avatarKey = path;
		this.userInfoMap = userInfoMap;
	}

	@Override
	public Bitmap onDecodeOriginal(JobContext jc, final int type) {
    	Log.i(TAG, "<onDecodeOriginal> type:" + type, Log.APP);
        if (null == avatarKey) {
            return null;
        }
        String mLocalFilePath = 
        		context.getExternalCacheDir() + Constants.DIR_UPLOAD_PIC_CACHE + File.separator + avatarKey;
		File f = new File(mLocalFilePath);
		if(!f.exists())
		{
			CloudCustomerServiceHandler testCloudService = CloudCustomerServiceHandler.getInstance(context);
	    	testCloudService.allocFileClient(userInfoMap);
	        MissionObject missionObject = testCloudService.initDownload(mLocalFilePath, avatarKey, true);
	        int downloadResult = testCloudService.download(missionObject, null).getResultCode();
	        
	        if(downloadResult < 0)
	        {
	        	return null;
	        }
		}
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int targetSize = AppApplication.bitmapHub.getTargetSize(type);

        // try to decode from JPEG EXIF
        if (type == Constants.TYPE_MICROTHUMBNAIL) {
            ExifInterface exif = null;
            byte [] thumbData = null;
            try {
                exif = new ExifInterface(mLocalFilePath);
                if (exif != null) {
                    thumbData = exif.getThumbnail();
                }
            } catch (Throwable t) {
            }
            if (thumbData != null) {
                Bitmap bitmap = DecodeUtils.decodeIfBigEnough(
                        jc, thumbData, options, targetSize);
                if (bitmap != null) return bitmap;
            }
        }

        Bitmap bitmap = DecodeUtils.decodeThumbnail(
              jc, mLocalFilePath, options, targetSize, type);

        return DecodeUtils.ensureGLCompatibleBitmap(bitmap);
    }

}

package com.custom.music.job;

import java.io.FileDescriptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.custom.music.util.Log;
import com.custom.music.util.MusicUtils;
import com.custom.music.util.bitmap.DecodeUtils;
import com.custom.music.util.thread.ThreadPool.JobContext;

/**
 * load local img
 * @author jianwen.zhu
 * 2014/1/2
 */
public class LocalAlbumArtJob extends ImageCacheRequestJob {
	private static final String TAG = "LocalImageRequestJob";
    private FileDescriptor mFileDescriptor;
	public LocalAlbumArtJob(Context context, FileDescriptor fd,String fileName, int type,
			int targetWidthSize, int targetHeightSize) {
		super(context, fileName, type, targetWidthSize, targetHeightSize, 0);
		mFileDescriptor = fd;
	}

	@Override
	public Bitmap onDecodeOriginal(JobContext jc, final int type) {
    	Log.i(TAG, "<onDecodeOriginal> type:" + type, Log.APP);
        if (null == mFileDescriptor) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int targetSize = MusicUtils.getTargetSize(type);
        Bitmap bitmap = DecodeUtils.decodeThumbnail(
              jc, mFileDescriptor, options, targetSize, type);

        return DecodeUtils.ensureGLCompatibleBitmap(bitmap);
    }

}

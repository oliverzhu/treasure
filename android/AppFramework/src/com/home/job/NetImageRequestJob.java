package com.home.job;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileLock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;

import com.home.AppApplication;
import com.home.util.Constants;
import com.home.util.FileUtils;
import com.home.util.bitmap.DecodeUtils;
import com.home.util.log.Log;
import com.home.util.thread.ThreadPool.JobContext;

public class NetImageRequestJob extends ImageCacheRequestJob {
	private static final String TAG = "LocalImageRequestJob";
    private String mNetFilePath;
	public NetImageRequestJob(Context context, String path, int type,
			int targetWidthSize, int targetHeightSize) {
		super(context, path, type, targetWidthSize, targetHeightSize, 0);
		mNetFilePath = path;
	}

	@Override
	public Bitmap onDecodeOriginal(JobContext jc, final int type) {
    	Log.i(TAG, "<onDecodeOriginal> type:" + type, Log.APP);
        if (null == mNetFilePath) {
            return null;
        }
        String mLocalFilePath = context.getExternalCacheDir() + "/img" + mNetFilePath.substring(mNetFilePath.lastIndexOf("/"));
		File f = new File(mLocalFilePath);
		FileLock fl = null;
		try {
			if(!f.exists()) {
				File dest = new File(f.getPath());
				FileUtils.createNewFile(dest);
				FileOutputStream out = new FileOutputStream(dest);
				URL connUrl = new URL(mNetFilePath);
				fl = out.getChannel().tryLock();
				if(fl != null)
				{
					HttpURLConnection conn = (HttpURLConnection) connUrl.openConnection();
					conn.setDoInput(true);
					conn.setConnectTimeout(10 * 1000);
					conn.connect();
					InputStream in = conn.getInputStream();

					// inputStream在一次使用以后就会变空
					if(f.exists() && in != null) {
						byte[] buf = new byte[1024];
						int i = 0;
						while((i = in.read(buf)) > 0) {
							out.write(buf, 0, i);
						}
						
						if(fl.isValid())
						{
							Log.i(TAG, "release file lock", Log.APP);
							fl.release();
						}
					}
					if(in != null) {
						in.close();
					}
					if(out != null) {
						out.close();
					}
					if (conn != null) {
						conn.disconnect();
					}
				}else
				{
					Log.i(TAG, "the file lock is used by another thread", Log.APP);
					if(out != null)
					{
						out.close();
					}
					return null;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "get net img fail", Log.APP);
			if(f.exists())
			{
				f.delete();
			}
			if(fl != null && fl.isValid())
			{
				try {
					fl.release();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
			return null;
		}
		
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        int targetSize = AppApplication.appInteractionHub.getTargetSize(type);

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

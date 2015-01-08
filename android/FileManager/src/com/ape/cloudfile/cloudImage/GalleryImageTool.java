package com.ape.cloudfile.cloudImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;

import com.ape.cloudfile.CloudFileService;
import com.ape.cloudfile.CloudFileUtil;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.Util;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionObject;

public class GalleryImageTool
{
    private static final String TAG = "GalleryImageTool";
    private int mErrorCode;
    private CloudFileService mCloudTool;

    public GalleryImageTool(CloudFileService tool)
    {
        mCloudTool = tool;
    }

    public int getErrorCode()
    {
        return mErrorCode;
    }

    public boolean uploadImage(String localPath, String cloudPath)
    {
        boolean retVal = false;
        
        File localImage = new File(localPath);
        if (!localImage.exists())
            return false;

        if (localImage.length() > CloudFileUtil.MIN_CLOUD_IMAGE_SIZE)
        {
            //1.Create thumnail in local "SDCard/.sync/" + "cloudPath"
            //2.Upload thumnail to cloudPath : "/cloud_system/" + "cloudPath"
            if (!createCloudCacheThumbnail(localPath, cloudPath))
                return false;
        } else
        {
            String localThumbPath = CloudFileUtil.getLocalCachePath(cloudPath);
            
            Util.copyFile(localPath, localThumbPath);
        }

        //3.Upload image to cloudPath
        MissionObject  mission = mCloudTool.initMultipartUpload(localPath, cloudPath);
        CloudFileResult result = mCloudTool.multipartUploadFile(mission, null);
        mErrorCode = result.getResultCode();
        retVal = (mErrorCode == CloudUtil.CLOUDCLIENT_RESULT_OK);

        return retVal;
    }
    
    public boolean createCloudCacheThumbnail(String localPath, String cloudPath)
    {
        //1.Create thumnail in local "SDCard/.sync/" + "cloudPath"
        String localThumbPath = createThumbnailFile(localPath, cloudPath);

        if (localThumbPath == null)
            return false;

        //2.Upload thumnail to cloudPath : "/cloud_system/" + "cloudPath"
        String cloudThumbPath = CloudFileUtil.getCloudThumbPath(cloudPath);
        MissionObject  missionThumb = mCloudTool.initMultipartUpload(localThumbPath, cloudThumbPath);
        CloudFileResult thumbResult = mCloudTool.multipartUploadFile(missionThumb, null);
        mErrorCode = thumbResult.getResultCode();
        if (mErrorCode != CloudUtil.CLOUDCLIENT_RESULT_OK && mErrorCode != CloudUtil.CLOUD_FILE_FILE_EXIST)
        {
            return false;
        }
        try
        {
            mCloudTool.deleteUploadOneMission(missionThumb.getKey());
        } catch (Exception e)
        {
        }

        return true;
    }
    
    private String createThumbnailFile(String localPath, String cloudPath)
    {
        boolean result = false;
        String localThumbPath = CloudFileUtil.getLocalCachePath(cloudPath);
        Bitmap thumbnail = scaleBitmap(localPath, CloudFileUtil.CLOUD_IMAGE_THUMB_WIDTH, CloudFileUtil.CLOUD_IMAGE_THUMB_HEIGHT, 0);
        FileOutputStream fOut = null;

        try
        {
            fOut = new FileOutputStream(localThumbPath);
            result = thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } finally
        {
            thumbnail.recycle();
            if (fOut != null)
            {
                try
                {
                    fOut.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        
        return (result) ? localThumbPath : null;
    }

    public String getCloudThumbnailFile(String cloudPath)
    {
        String cachePath = CloudFileUtil.getLocalCachePath(cloudPath);
        File cacheFile = new File(cachePath);
        MyLog.d(TAG, "getCloudThumbnailFile, cloudPath:" + cloudPath + ", cachePath:" + cachePath);
        if (!cacheFile.exists())
        {
            String cloudThumbPath = CloudFileUtil.getCloudThumbPath(cloudPath);
            MissionObject mission = mCloudTool.initDownload(cloudThumbPath, cachePath);
            MyLog.d(TAG, "getCloudThumbnailFile, cloudThumbPath:" + cloudThumbPath + ", mission:" + mission);
            if (mission != null)
            {
                CloudFileResult result = mCloudTool.downloadFile(mission, null);

                int errorCode = result.getResultCode();
                if (errorCode == CloudUtil.CLOUD_FILE_NO_SUCH_KEY)
                {
                    MissionObject mission2 = mCloudTool.initDownload(cloudPath, cachePath);
                    CloudFileResult result2 = mCloudTool.downloadFile(mission2, null);
                    mCloudTool.deleteDownloadOneMission(cloudPath, false);
                    errorCode = result2.getResultCode();
                }
                if (errorCode != CloudUtil.CLOUDCLIENT_RESULT_OK)
                {
                    cachePath = null;
                }
            }
            mCloudTool.deleteDownloadOneMission(cloudThumbPath, false);
        }

        return cachePath;
    }
    
    public boolean deleteCloudImage(String cloudPath)
    {
        String cloudThumb = CloudFileUtil.getCloudThumbPath(cloudPath);
        String localThumb = CloudFileUtil.getLocalCachePath(cloudPath);
        
        mCloudTool.deleteFile(cloudThumb);
        File thumbFile = new File(localThumb);
        if (thumbFile.exists())
        {
            thumbFile.delete();
        }
        
        CloudFileResult result = mCloudTool.deleteFile(cloudPath);
        return (result.getResultCode() == CloudUtil.CLOUDCLIENT_RESULT_OK);
    }

    /**
     * 
     * 
     * @param in
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static Bitmap scaleBitmap(String filePath, int targetWidth,
            int targetHeight, int angle)
    {
        if (targetHeight == 0 || targetHeight == 0)
            return null;

        // get sampleBitmap
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);
        if ((opts.outWidth - opts.outHeight) * (targetWidth - targetHeight) < 0)
        {
            int tmp = targetWidth;
            targetWidth = targetHeight;
            targetHeight = tmp;
        }

        int scaleWidth = opts.outWidth / targetWidth;
        int scaleHeight = opts.outHeight / targetHeight;
        int scale = scaleWidth < scaleHeight ? scaleWidth : scaleHeight;
        if (scale < 1)
        {
            scale = 1;
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap sampleBitmap = BitmapFactory.decodeFile(filePath, opts);

        if (opts.outWidth == -1 || opts.outHeight == -1)
        {
            return null;
        }
        // get scalebitmap
        float fScaleWidth = targetWidth / ((float) opts.outWidth);
        float fScaleHeight = targetHeight / ((float) opts.outHeight);
        float fScale = fScaleWidth > fScaleHeight ? fScaleWidth : fScaleHeight;
        if (fScale > 1)
            fScale = 1;
        Matrix matrix = new Matrix();
        if (angle != 0)
        {
            matrix.setRotate(angle);
        }
        matrix.postScale(fScale, fScale);
        Bitmap scaleBitmap = Bitmap.createBitmap(sampleBitmap, 0, 0,
                opts.outWidth, opts.outHeight, matrix, true);

        // get targetBitmap
        int bitmapX = (scaleBitmap.getWidth() - targetWidth) / 2;
        bitmapX = bitmapX > 0 ? bitmapX : 0;
        int bitmapY = (scaleBitmap.getHeight() - targetHeight) / 2;
        bitmapY = bitmapY > 0 ? bitmapY : 0;
        targetWidth = targetWidth < (scaleBitmap.getWidth()) ? targetWidth
                : (scaleBitmap.getWidth());
        targetHeight = targetHeight < (scaleBitmap.getHeight()) ? targetHeight
                : (scaleBitmap.getHeight());
        Bitmap targetBitmap = Bitmap.createBitmap(scaleBitmap, bitmapX,
                bitmapY, targetWidth, targetHeight);

        // scaleBitmap.recycle();
        //sampleBitmap.recycle();
        return targetBitmap;
    }
}

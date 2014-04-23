/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.home.job;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.home.AppApplication;
import com.home.util.Constants;
import com.home.util.bitmap.BitmapUtils;
import com.home.util.bitmap.DecodeUtils;
import com.home.util.cache.BytesBufferPool.BytesBuffer;
import com.home.util.cache.ImageCacheService;
import com.home.util.log.Log;
import com.home.util.thread.ThreadPool.Job;
import com.home.util.thread.ThreadPool.JobContext;


abstract class ImageCacheRequestJob implements Job<Bitmap> {
    private static final String TAG = "ImageCacheRequestJob";
    protected Context context;
    protected String mPath;
    private int mType;
    private int mTargetWidthSize;
    private int mTargetHeightSize;
    private long mDateModifiedInSec;

    public ImageCacheRequestJob(Context context,
            String path, int type, int targetWidthSize,int targetHeightSize, long dateModifiedInSec)
    {
    	this.context = context;
        mPath = path;
        mType = type;
        mTargetWidthSize = targetWidthSize;
        mTargetHeightSize = targetHeightSize;
        mDateModifiedInSec = dateModifiedInSec;
    }
    
    @Override
    public Bitmap run(JobContext jc) {
    	Log.i(TAG, "<run>", Log.APP);
        ImageCacheService cacheService = 
        		AppApplication.appInteractionHub.getImageCacheService(context);

        BytesBuffer buffer = AppApplication.appInteractionHub.getBytesBufferPool().get();
        try {
            boolean found = cacheService.getImageData(mPath, mType, buffer, mDateModifiedInSec);
            // if support picture quality tuning, we decode bitmap from origin image
            // in order to apply picture quality every time
            if (jc.isCancelled()) return null;
            if (found) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap;
                if (mType == Constants.TYPE_MICROTHUMBNAIL) {
                    bitmap = DecodeUtils.decode(jc,
                            buffer.data, buffer.offset, buffer.length, options,
                            AppApplication.appInteractionHub.getMicroThumbPool());
                } else {
                    bitmap = DecodeUtils.decode(jc,
                            buffer.data, buffer.offset, buffer.length, options,
                            AppApplication.appInteractionHub.getThumbPool());
                }
                if (bitmap == null && !jc.isCancelled()) {
                }
                return bitmap;
            }
        } finally {
        	AppApplication.appInteractionHub.getBytesBufferPool().recycle(buffer);
        }
        Bitmap bitmap = onDecodeOriginal(jc, mType);
        if (jc.isCancelled()) return null;

        if (bitmap == null) {
            return null;
        }

        if (mType == Constants.TYPE_MICROTHUMBNAIL) {
            bitmap = BitmapUtils.resizeAndCropCenter(bitmap, mTargetWidthSize,mTargetHeightSize, true);
        } else {
            bitmap = BitmapUtils.resizeDownBySideLength(bitmap, mTargetWidthSize, true);
        }
        if (jc.isCancelled()) return null;

        byte[] array = BitmapUtils.compressToBytes(bitmap);
        if (jc.isCancelled()) return null;

        cacheService.putImageData(mPath, mType, array);
        // if support picture quality tuning, we don't write data to cache in order to improve performance
        return bitmap;
    }

    public abstract Bitmap onDecodeOriginal(JobContext jc, int type);
}
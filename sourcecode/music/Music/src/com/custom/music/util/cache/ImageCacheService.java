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

package com.custom.music.util.cache;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.content.Context;

import com.custom.music.util.Log;
import com.custom.music.util.Utils;
import com.custom.music.util.cache.BlobCache.LookupRequest;
import com.custom.music.util.cache.BytesBufferPool.BytesBuffer;


public class ImageCacheService {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageCacheService";

    private static final String IMAGE_CACHE_FILE = "imgcache";
    private static final int IMAGE_CACHE_MAX_ENTRIES = 5000;
    private static final int IMAGE_CACHE_MAX_BYTES = 200 * 1024 * 1024;
    private static final int IMAGE_CACHE_VERSION = 7;

    private BlobCache mCache;

    public ImageCacheService(Context context) {
        mContext = context;
        mCache = CacheManager.getCache(context, IMAGE_CACHE_FILE,
                IMAGE_CACHE_MAX_ENTRIES, IMAGE_CACHE_MAX_BYTES,
                IMAGE_CACHE_VERSION);
    }

    /**
     * Gets the cached image data for the given <code>path</code> and <code>type</code>.
     *
     * The image data will be stored in <code>buffer.data</code>, started from
     * <code>buffer.offset</code> for <code>buffer.length</code> bytes. If the
     * buffer.data is not big enough, a new byte array will be allocated and returned.
     *
     * @return true if the image data is found; false if not found.
     */
    public boolean getImageData(String filePath, int type, BytesBuffer buffer) {
    	Log.i(TAG, "<getImageData> start filePath:" + filePath + " type:" + type,Log.APP);
        if (mCache == null) {
        	Log.w(TAG, "<getImageData> mChache is null",Log.APP);
            return false;
        }

        byte[] key = makeKey(filePath, type);
        long cacheKey = Utils.crc64Long(key);
        try {
            LookupRequest request = new LookupRequest();
            request.key = cacheKey;
            request.buffer = buffer.data;
            synchronized (mCacheLock) {
                if (mCache == null) {
                	Log.w(TAG, "<getImageData> mChache is null",Log.APP);
                    return false;
                }
                if (!mCache.lookup(request)) 
                {
                	Log.w(TAG, "<getImageData> there is no blob cache",Log.APP);
                	return false;
                }
            }
            if (isSameKey(key, request.buffer)) {
            	Log.i(TAG, "<getImageData> end get image date success",Log.APP);
                buffer.data = request.buffer;
                buffer.offset = key.length;
                buffer.length = request.length - buffer.offset;
                return true;
            }
        } catch (IOException ex) {
            // ignore.
        }
        return false;
    }

    public void putImageData(String filepath, int type, byte[] value) {
    	Log.i(TAG, "<putImageData> start filepath:" + filepath,Log.APP);
        if (mCache == null) {
        	Log.w(TAG, "<putImageData> mChache is null",Log.APP);
            return;
        }
        byte[] key = makeKey(filepath, type);
        long cacheKey = Utils.crc64Long(key);
        ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
        buffer.put(key);
        buffer.put(value);
        synchronized (mCacheLock) {
            try {
                if (mCache != null) {
                    mCache.insert(cacheKey, buffer.array());
                }
            } catch (IOException ex) {
                // ignore.
            }
        }
        Log.i(TAG, "<putImageData> end",Log.APP);
    }

    private static byte[] makeKey(String filePath, int type) {
    	Log.i(TAG, "<makeKey> filePath:" + filePath,Log.APP);
        return getBytes(filePath.toString() + "+" + type);
    }

    private static boolean isSameKey(byte[] key, byte[] buffer) {
        int n = key.length;
        if (buffer.length < n) {
        	Log.i(TAG, "<isSameKey> not the same",Log.APP);
            return false;
        }
        for (int i = 0; i < n; ++i) {
            if (key[i] != buffer[i]) {
            	Log.i(TAG, "<isSameKey> not the same",Log.APP);
                return false;
            }
        }
        Log.i(TAG, "<isSameKey> same",Log.APP);
        return true;
    }

    //check dateTaken api
    public boolean getImageData(String filePath, int type, BytesBuffer buffer,
                                  long dateModifiedInSec) {
    	Log.i(TAG, "<getImageData> start filePath:" + filePath,Log.APP);
        if (mCache == null) {
        	Log.w(TAG, "<getImageData> mCache is null",Log.APP);
            return false;
        }
        byte[] key = makeKey(filePath, type);
        long cacheKey = Utils.crc64Long(key);
        try {
            LookupRequest request = new LookupRequest();
            request.key = cacheKey;
            request.buffer = buffer.data;
            synchronized (mCacheLock) {
                if (mCache == null) {
                	Log.w(TAG, "<getImageData> mCache is null",Log.APP);
                    return false;
                }
                if (!mCache.lookup(request)) return false;
            }
            if (isSameKey(key, request.buffer)) {
                buffer.data = request.buffer;
                buffer.offset = key.length;
                buffer.length = request.length - buffer.offset;
                Log.i(TAG, "<getImageData> end",Log.APP);
                return true;
            }
        } catch (IOException ex) {
            // ignore.
        }
        return false;
    }
       
    public void putImageData(String filePath, int type, byte[] value, long dateModifiedInSec) {
    	Log.i(TAG, "<putImageData> filePath:" + filePath,Log.APP);
        if (mCache == null) {
        	Log.w(TAG, "mCache is null",Log.APP);
            return;
        }
        byte[] key = makeKey(filePath, type, dateModifiedInSec);
        long cacheKey = Utils.crc64Long(key);
        ByteBuffer buffer = ByteBuffer.allocate(key.length + value.length);
        buffer.put(key);
        buffer.put(value);
        synchronized (mCacheLock) {
            try {
                if (mCache != null) {
                    mCache.insert(cacheKey, buffer.array());
                }
            } catch (IOException ex) {
                // ignore.
            }
        }
    }
    
    public void clearImageData(String filePath, int type) {
    	Log.i(TAG, "<clearImageData> filePath:" + filePath,Log.APP);
        byte[] key = makeKey(filePath, type);
        long cacheKey = Utils.crc64Long(key);
        synchronized (mCache) {
            try {
                mCache.clearEntry(cacheKey);
            } catch (IOException ex) {
                // ignore.
            }
        }
    }

    private static byte[] makeKey(String filePath, int type, long dateModifiedInSec) {
    	Log.i(TAG, "<makeKey> filePath:" + filePath,Log.APP);
        return getBytes(filePath + "+" + type + "+" + dateModifiedInSec);
    }

    // M: for closing/re-opening cache
    private Context mContext;
    private Object mCacheLock = new Object();

    // M: for closing/re-opening cache
    public void closeCache() {
        synchronized (mCacheLock) {
            // simply clear the reference,
            // since the BlobCache should already be closed in CacheManager
            mCache = null;
        }
    }
    
    public void openCache() {
        synchronized (mCacheLock) {
            if (mCache == null) {
            	Log.i(TAG, "<openCache>",Log.APP);
                // re-open the cache
                mCache = CacheManager.getCache(mContext, IMAGE_CACHE_FILE,
                        IMAGE_CACHE_MAX_ENTRIES, IMAGE_CACHE_MAX_BYTES,
                        IMAGE_CACHE_VERSION);
            }
        }
    }
    
    public static byte[] getBytes(String in) {
        byte[] result = new byte[in.length() * 2];
        int output = 0;
        for (char ch : in.toCharArray()) {
            result[output++] = (byte) (ch & 0xFF);
            result[output++] = (byte) (ch >> 8);
        }
        return result;
    }
}

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

package com.client.customerservicecenter.util.cache;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.client.customerservicecenter.util.Log;


public class CacheManager {
	private static final String TAG = "CacheManager";
    private static final String KEY_CACHE_UP_TO_DATE = "cache-up-to-date";
    private static HashMap<String, BlobCache> sCacheMap =
            new HashMap<String, BlobCache>();
    private static boolean sOldCheckDone = false;

    // Return null when we cannot instantiate a BlobCache, e.g.:
    // there is no SD card found.
    // This can only be called from data thread.
    public static BlobCache getCache(Context context, String filename,
            int maxEntries, int maxBytes, int version) {
    	Log.i(TAG, "<getCache> filename:" + filename,Log.APP);
        synchronized (sCacheMap) {
            if (sNoStorage) {
                return null;
            }
            if (!sOldCheckDone) {
                removeOldFilesIfNecessary(context);
                sOldCheckDone = true;
            }
            BlobCache cache = sCacheMap.get(filename);
            if (cache == null) {
                File cacheDir = context.getExternalCacheDir();
                if (cacheDir == null) {
                    return null;
                }
                String path = cacheDir.getAbsolutePath() + "/" + filename;
                try {
                    cache = new BlobCache(path, maxEntries, maxBytes, false,
                            version);
                    sCacheMap.put(filename, cache);
                } catch (IOException e) {
                	Log.e("~~~~~~~~~~~~~~~~~~ getCache ~~~~~~~~~~~~~~~~~~", Log.DATA);
                	e.printStackTrace();
                }
            }
            return cache;
        }
    }

    // Removes the old files if the data is wiped.
    private static void removeOldFilesIfNecessary(Context context) {
    	Log.i(TAG, "<removeOldFilesIfNecessary>",Log.APP);
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(context);
        int n = 0;
        try {
            n = pref.getInt(KEY_CACHE_UP_TO_DATE, 0);
        } catch (Throwable t) {
            // ignore.
        }
        if (n != 0) return;
        pref.edit().putInt(KEY_CACHE_UP_TO_DATE, 1).commit();

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null) {
            return;
        }
        String prefix = cacheDir.getAbsolutePath() + "/";

        BlobCache.deleteFiles(prefix + "imgcache");
        BlobCache.deleteFiles(prefix + "rev_geocoding");
        BlobCache.deleteFiles(prefix + "bookmark");
    }

    // M: for closing cache after card unmounted
    private static boolean sNoStorage = false;

    // M: disable cache when card got unmounted
    public static void storageStateChanged(boolean mounted) {
    	Log.i(TAG, "<storageStateChanged>",Log.APP);
        synchronized (sCacheMap) {
            if (mounted) {
                // this is lazy initialization: we do NOT re-open all cache files until they are needed again
                sNoStorage = false;
            } else {
                // clear cache map and disable cache access
                sNoStorage = true;
                for (BlobCache cache : sCacheMap.values()) {
                    // close all entry's "value", which is a BlobCache
                    cache.close();
                }
                sCacheMap.clear();
            }
        }
    }
}

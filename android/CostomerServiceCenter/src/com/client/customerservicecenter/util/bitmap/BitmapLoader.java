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

package com.client.customerservicecenter.util.bitmap;

import android.graphics.Bitmap;

import com.client.customerservicecenter.util.Log;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;


// We use this class to
//     1.) load bitmaps in background.
//     2.) as a place holder for the loaded bitmap
public abstract class BitmapLoader implements FutureListener<Bitmap> {
    private static final String TAG = "BitmapLoader";

    /* Transition Map:
     *   INIT -> REQUESTED, RECYCLED
     *   REQUESTED -> INIT (cancel), LOADED, ERROR, RECYCLED
     *   LOADED, ERROR -> RECYCLED
     */
    private static final int STATE_INIT = 0;
    private static final int STATE_REQUESTED = 1;
    private static final int STATE_LOADED = 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_RECYCLED = 4;

    private int mState = STATE_INIT;
    // mTask is not null only when a task is on the way
    private Future<Bitmap> mTask;
    private Bitmap mBitmap;

    @Override
    public void onFutureDone(Future<Bitmap> future) {
    	Log.i(TAG, "<onFutureDone>",Log.APP);
        synchronized (this) {
            mTask = null;
            mBitmap = future.get();
            if (mState == STATE_RECYCLED) {
                if (mBitmap != null) {
                    recycleBitmap(mBitmap);
                    mBitmap = null;
                }
                return; // don't call callback
            }
            if (future.isCancelled() && mBitmap == null) {
                if (mState == STATE_REQUESTED) mTask = submitBitmapTask(this);
                return; // don't call callback
            } else {
                mState = mBitmap == null ? STATE_ERROR : STATE_LOADED;
            }
        }
        onLoadComplete(mBitmap);
    }

    public synchronized void startLoad() {
    	Log.i(TAG, "<startLoad>",Log.APP);
        if (mState == STATE_INIT) {
            mState = STATE_REQUESTED;
            if (mTask == null) mTask = submitBitmapTask(this);
        }
    }

    public synchronized void cancelLoad() {
    	Log.i(TAG, "<cancelLoad>",Log.APP);
        if (mState == STATE_REQUESTED) {
            mState = STATE_INIT;
            if (mTask != null) mTask.cancel();
        }
    }

    // Recycle the loader and the bitmap
    public synchronized void recycle() {
    	Log.i(TAG, "<recycle>",Log.APP);
        mState = STATE_RECYCLED;
        if (mBitmap != null) {
            recycleBitmap(mBitmap);
            mBitmap = null;
        }
        if (mTask != null) mTask.cancel();
    }

    public synchronized boolean isRequestInProgress() {
        return mState == STATE_REQUESTED;
    }

    public synchronized boolean isRecycled() {
        return mState == STATE_RECYCLED;
    }

    public synchronized Bitmap getBitmap() {
        return mBitmap;
    }

    abstract protected Future<Bitmap> submitBitmapTask(FutureListener<Bitmap> l);
    abstract protected void recycleBitmap(Bitmap bitmap);
    abstract protected void onLoadComplete(Bitmap bitmap);
    
    // M: added for performance auto test
    public boolean mBitmapLoaded = false;

    /// M: Video thumbnail play @{
    public synchronized boolean isLoadingCompleted() {
        return ((mState == STATE_LOADED) || (mState == STATE_ERROR));
    }
    /// @}
}
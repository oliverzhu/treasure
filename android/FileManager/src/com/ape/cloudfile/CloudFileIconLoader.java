package com.ape.cloudfile;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.widget.ImageView;

import com.ape.cloudfile.cloudImage.GalleryImageTool;
import com.ape.filemanager.FileCategoryHelper.FileCategory;

public class CloudFileIconLoader implements Callback
{
    private static final String LOADER_THREAD_NAME = "CloudFileIconLoader";

    /**
     * Type of message sent by the UI thread to itself to indicate that some
     * photos need to be loaded.
     */
    private static final int MESSAGE_REQUEST_LOADING = 1;

    /**
     * Type of message sent by the loader thread to indicate that some photos
     * have been loaded.
     */
    private static final int MESSAGE_ICON_LOADED = 2;

    private static abstract class ImageHolder
    {
        public static final int NEEDED = 0;
        public static final int LOADING = 1;
        public static final int LOADED = 2;

        int state;

        public static ImageHolder create(FileCategory cate)
        {
            switch (cate)
            {
                case Picture:
                case Video:
                    return new BitmapHolder();
            }

            return null;
        };

        public abstract boolean setImageView(ImageView v);

        public abstract boolean isNull();

        public abstract void setImage(Object image);
    }

    private static class BitmapHolder extends ImageHolder
    {
        SoftReference<Bitmap> bitmapRef;

        @Override
        public boolean setImageView(ImageView v)
        {
            if (bitmapRef.get() == null)
                return false;

            v.setImageBitmap(bitmapRef.get());
            return true;
        }

        @Override
        public boolean isNull()
        {
            return (bitmapRef == null);
        }

        @Override
        public void setImage(Object image)
        {
            bitmapRef = (image == null) ? null : new SoftReference<Bitmap>((Bitmap) image);
        }
    }

    public static class FileReqData
    {
        public String mPath;
        public FileCategory mCategory;
        public ImageView mView;
        
        public FileReqData(String path, FileCategory cate, ImageView view)
        {
            mPath = path;
            mCategory = cate;
            mView = view;
        }
    }
    
    /**
     * A soft cache for image thumbnails. the key is file path
     */
    private final static ConcurrentHashMap<String, ImageHolder> mImageCache = new ConcurrentHashMap<String, ImageHolder>();

    /**
     * A map from ImageView to the corresponding photo ID. Please note that this
     * photo ID may change before the photo loading request is started.
     */
    private final ConcurrentHashMap<ImageView, FileReqData> mPendingRequests = new ConcurrentHashMap<ImageView, FileReqData>();

    /**
     * Handler for messages sent to the UI thread.
     */
    private final Handler mMainThreadHandler = new Handler(this);
    /**
     * Thread responsible for loading photos from the database. Created upon the
     * first request.
     */
    private LoaderThread mLoaderThread;

    /**
     * Flag indicating if the image loading is paused.
     */
    private boolean mPaused;
    
    private Context mContext;
    private CloudFileService mCloudFileService;
    private int mThumbWidth;
    private int mThumbHeight;

    public CloudFileIconLoader(Context context, int width, int height)
    {
        mContext = context;
        mThumbWidth = width;
        mThumbHeight = height;
    }

    public boolean loadIcon(ImageView view, String path, FileCategory cate)
    {
        boolean loaded = loadCachedIcon(view, path, cate);
        
        if (loaded)
        {
            mPendingRequests.remove(view);
        } else
        {
            FileReqData reqData = new FileReqData(path, cate, view);
            mPendingRequests.put(view, reqData);
            
            if (!mPaused)
            {
                requestLoading();
            }
        }

        return loaded;
    }

    public void cancelRequest(ImageView view)
    {
        mPendingRequests.remove(view);
    }

    /**
     * Checks if the photo is present in cache. If so, sets the photo on the
     * view, otherwise sets the state of the photo to
     * {@link BitmapHolder#NEEDED}
     */
    private boolean loadCachedIcon(ImageView view, String path, FileCategory cate)
    {
        ImageHolder holder = mImageCache.get(path);

        if (holder == null)
        {
            holder = ImageHolder.create(cate);
            if (holder == null)
                return false;

            mImageCache.put(path, holder);
        } else if (holder.state == ImageHolder.LOADED)
        {
            if (holder.isNull())
                return true;

            // failing to set imageview means that the soft reference was
            // released by the GC, we need to reload the photo.
            if (holder.setImageView(view))
                return true;
        }

        holder.state = ImageHolder.NEEDED;
        return false;
    }

    /**
     * Sends a message to this thread itself to start loading images. If the
     * current view contains multiple image views, all of those image views will
     * get a chance to request their respective photos before any of those
     * requests are executed. This allows us to load images in bulk.
     */
    private boolean mLoadingRequested;
    private void requestLoading()
    {
        if (!mLoadingRequested)
        {
            mLoadingRequested = true;
            mMainThreadHandler.sendEmptyMessage(MESSAGE_REQUEST_LOADING);
        }
    }

    /**
     * Main thread handler
     */
    @Override
    public boolean handleMessage(Message msg)
    {
        switch (msg.what)
        {
            case MESSAGE_REQUEST_LOADING:
                mLoadingRequested = false;
                if (!mPaused)
                {
                    if (mLoaderThread == null)
                    {
                        mLoaderThread = new LoaderThread();
                        mLoaderThread.start();
                    }
                    mLoaderThread.requestLoading();
                }
                break;

            case MESSAGE_ICON_LOADED:
                if (!mPaused)
                {
                    processLoadedIcons();
                }
                break;
        }

        return false;
    }
    
    /**
     * Goes over pending loading requests and displays loaded photos. If some of
     * the photos still haven't been loaded, sends another request for image
     * loading.
     */
    private void processLoadedIcons()
    {
        Iterator<ImageView> iterator = mPendingRequests.keySet().iterator();
        while (iterator.hasNext())
        {
            ImageView view = iterator.next();
            FileReqData reqData = mPendingRequests.get(view);
            boolean loaded = loadCachedIcon(view, reqData.mPath, reqData.mCategory);
            if (loaded)
            {
                iterator.remove();
            }
        }

        if (!mPendingRequests.isEmpty())
        {
            requestLoading();
        }
    }

    /**
     * The thread that performs loading of photos.
     */
    private class LoaderThread extends HandlerThread implements Callback
    {
        private Handler mLoaderThreadHandler;
        private GalleryImageTool mImageTool;
        
        public LoaderThread()
        {
            super(LOADER_THREAD_NAME);
        }

        /**
         * Sends a message to this thread to load requested photos.
         */
        public void requestLoading()
        {
            mCloudFileService = CloudFileService.getInstance(mContext);
            String userKey = mCloudFileService.getUserId();
            if (TextUtils.isEmpty(userKey) || userKey.equals("-1"))
            {
                return;
            }

            if (mLoaderThreadHandler == null)
            {
                mLoaderThreadHandler = new Handler(getLooper(), this);
            }
            mLoaderThreadHandler.sendEmptyMessage(0);
        }

        @Override
        public boolean handleMessage(Message msg)
        {
            if (mImageTool == null)
            {
                mImageTool = new GalleryImageTool(mCloudFileService);
            }

            Iterator<FileReqData> iterator = mPendingRequests.values().iterator();
            while (iterator.hasNext() && !mPaused)
            {
                FileReqData reqData = iterator.next();
                ImageHolder holder = mImageCache.get(reqData.mPath);

                if (holder.state == ImageHolder.NEEDED)
                {
                    holder.state = ImageHolder.LOADING;

                    switch (reqData.mCategory)
                    {
                        case Picture:
                            String cachePath = mImageTool.getCloudThumbnailFile(reqData.mPath);
                            if (cachePath != null)
                            {
                                Bitmap bitmap = GalleryImageTool.scaleBitmap(cachePath,
                                        mThumbWidth, mThumbHeight, 0);
                                holder.setImage(bitmap);
                            }
                            holder.state = ImageHolder.LOADED;
                            break;

                        default:
                            holder.state = ImageHolder.LOADED;
                            break;
                    }
                }
            }

            mMainThreadHandler.sendEmptyMessage(MESSAGE_ICON_LOADED);
            return true;
        }
    }

    /**
     * Stops loading images, kills the image loader thread and clears all
     * caches.
     */
    public void stop()
    {
        pause();

        if (mLoaderThread != null)
        {
            mLoaderThread.quit();
            mLoaderThread = null;
        }
    }

    public void clear()
    {
        mPendingRequests.clear();
        mImageCache.clear();
    }

    public void pause()
    {
        mPaused = true;
    }

    public void resume()
    {
        mPaused = false;
        if (!mPendingRequests.isEmpty())
        {
            requestLoading();
        }
    }
}

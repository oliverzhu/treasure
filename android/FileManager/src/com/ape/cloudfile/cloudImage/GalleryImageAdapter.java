package com.ape.cloudfile.cloudImage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.ape.cloudfile.CloudFileUtil;
import com.ape.filemanager.MyLog;
import com.ape.filemanager.R;
import com.cloud.client.file.CloudFile;

public class GalleryImageAdapter extends BaseAdapter
{
    private static final String TAG = "GalleryImageAdapter";

    private ArrayList<CloudFile> mImageList;
    private Context mContext;
//    private Animation mRotateAnim;

    public GalleryImageAdapter(Context context, ArrayList<CloudFile> list)
    {
        mImageList = list;
        mContext = context;
    }

    @Override
    public int getCount()
    {
        return mImageList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mImageList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        String cloudPath = mImageList.get(position).getKey();
        String cachePath = CloudFileUtil.getLocalCachePath(cloudPath);
        CloudImageCache imageCache = null;
        ImageView imageView;

        if (convertView == null)
        {
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            imageCache = new CloudImageCache(cloudPath, null);
            imageView.setTag(imageCache);
        } else
        {
            imageView = (ImageView) convertView;
            imageCache = (CloudImageCache) imageView.getTag();
        }

        File cacheFile = new File(cachePath);
        if (cacheFile.exists())
        {
//          imageView.clearAnimation();
//          mRotateAnim.cancel();

            imageCache.cacheBmp = getFileBitmap(cachePath);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageBitmap(imageCache.cacheBmp);
            MyLog.d(TAG, "getView, cacheFile:" + cacheFile);
        } else
        {
            // Loading...
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(R.drawable.spinner_outer_holo);
            
//          LinearInterpolator lin = new LinearInterpolator();
//          mRotateAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate_self);
//          mRotateAnim.setInterpolator(lin);
//          imageView.startAnimation(mRotateAnim);
        }

        return imageView;
    }

    public float getScale(boolean focused,int offset)
    {
        return Math.max(0, 1.0f/(float)Math.pow(2, Math.abs(offset)));
    }

    public static class CloudImageCache
    {
        public String cloudPath;
        public Bitmap cacheBmp;
        
        public CloudImageCache(String cloudPath, Bitmap cacheBmp)
        {
            this.cloudPath = cloudPath;
            this.cacheBmp = cacheBmp;
        }
    }

    private HashMap<String, Bitmap> mBitmapCacheMap = new HashMap<String, Bitmap>();
    private static final int MAX_CACHE_SIZE = 100;
    private Bitmap getFileBitmap(String path)
    {
        Bitmap retBitmap;
        retBitmap = mBitmapCacheMap.get(path);
        MyLog.i(TAG, "getFileBitmap, getFromCacheMap:" + retBitmap);

        if (retBitmap == null)
        {
            if (mBitmapCacheMap.size() > MAX_CACHE_SIZE)
            {
                mBitmapCacheMap.clear(); // to forbit memeory error.
            }

            retBitmap = BitmapFactory.decodeFile(path);
            mBitmapCacheMap.put(path, retBitmap);
        }

        return retBitmap;
    }
    
    public void exit()
    {
        Iterator<Bitmap> iterator = mBitmapCacheMap.values().iterator();
        while (iterator.hasNext())
        {
            Bitmap btiBitmap = iterator.next();
            if (btiBitmap != null)
                btiBitmap.recycle();
        }
        mBitmapCacheMap.clear();
    }
}

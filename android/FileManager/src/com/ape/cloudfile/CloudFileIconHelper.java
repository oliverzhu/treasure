package com.ape.cloudfile;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.ape.filemanager.FileCategoryHelper;
import com.ape.filemanager.FileIconHelper;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.cloud.client.file.CloudFile;

public class CloudFileIconHelper
{
    private Context mContext;
    private CloudFileIconLoader mIconLoader;

    public CloudFileIconHelper(Context context)
    {
        mContext = context;
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.file_icon_picture);
        mIconLoader = new CloudFileIconLoader(mContext,
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight());        
    }

    public void setIcon(CloudFile fileInfo, ImageView fileImage)
    {
        mIconLoader.cancelRequest(fileImage);
        if (!fileInfo.isFile()) // is dir
        {
            fileImage.setImageResource(R.drawable.folder);
            return;
        }

        String ext = Util.getExtFromFilename(fileInfo.getName());
        int iconId = FileIconHelper.getFileIcon(ext);
        fileImage.setImageResource(iconId);
        
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(fileInfo.getKey());
        
        switch (fc)
        {
            case Picture:
                mIconLoader.loadIcon(fileImage, fileInfo.getKey(), fc);
                break;

            default:
                break;
        }
    }
    
    public void exit()
    {
        mIconLoader.stop();
    }

    public void pauseIconLoader()
    {
        mIconLoader.pause();
    }

    public void resumeIconLoader()
    {
        mIconLoader.resume();
    }
}

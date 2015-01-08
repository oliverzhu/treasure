package com.ape.filemanager.updatehelper;

import java.util.ArrayList;

public class MultiMediaStoreHelper
{
    protected final ArrayList<String> mPathList = new ArrayList<String>();
    private static final int NEED_UPDATE = 100;
    protected final MediaStoreHelper mMediaStoreHelper;

    public MultiMediaStoreHelper(MediaStoreHelper mediaStoreHelper)
    {
        if (mediaStoreHelper == null)
        {
            throw new IllegalArgumentException(
                    "mediaStoreHelper has not been initialized.");
        }
        mMediaStoreHelper = mediaStoreHelper;
    }

    public void addRecord(String path)
    {
        mPathList.add(path);
        if (mPathList.size() > NEED_UPDATE)
        {
            updateRecords();
        }
    }

    public void updateRecords()
    {
        mPathList.clear();
    }
    
    /**
     * Set dstfolder to scan with folder.
     * 
     * @param dstFolder
     */
    public void setDstFolder(String dstFolder) {
        mMediaStoreHelper.setDstFolder(dstFolder);
    }

    public static class PasteMediaStoreHelper extends MultiMediaStoreHelper
    {
        private static final int PASTE_NEED_UPDATE = 20;

        public PasteMediaStoreHelper(MediaStoreHelper mediaStoreHelper)
        {
            super(mediaStoreHelper);
        }

        @Override
        public void addRecord(String path)
        {
            mPathList.add(path);
            if (mPathList.size() > PASTE_NEED_UPDATE)
            {
                updateRecords();
            }
        }

        @Override
        public void updateRecords()
        {
            mMediaStoreHelper.scanPathforMediaStore(mPathList);
            super.updateRecords();
        }
    }

    public static class DeleteMediaStoreHelper extends MultiMediaStoreHelper
    {
        public DeleteMediaStoreHelper(MediaStoreHelper mediaStoreHelper)
        {
            super(mediaStoreHelper);
        }

        @Override
        public void updateRecords()
        {
            mMediaStoreHelper.deleteFileInMediaStore(mPathList);
            super.updateRecords();
        }
    }
}
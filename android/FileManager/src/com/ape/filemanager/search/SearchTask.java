package com.ape.filemanager.search;

import java.io.File;

import com.ape.filemanager.BaseAsyncTask;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.ProgressInfo;
import com.ape.filemanager.Settings;
import com.ape.filemanager.Util;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class SearchTask extends BaseAsyncTask
{
    private static final String TAG = "SearchTask";
    private final String mSearchName;
    private final String mPath;
    private final ContentResolver mContentResolver;
    protected OperationEventListener mListener = null;

    public SearchTask(OperationEventListener operationEvent, String searchName,
            String path, ContentResolver contentResolver)
    {
        super(operationEvent);
        mListener = operationEvent;
        mContentResolver = contentResolver;
        mPath = path;
        mSearchName = searchName;
    }

    @Override
    protected Integer doInBackground(Void... params)
    {
        Uri uri = MediaStore.Files.getContentUri("external");
        int ret = OperationEventListener.ERROR_CODE_SUCCESS;
        String[] projection = { MediaStore.Files.FileColumns.DATA, };
        StringBuilder sb = new StringBuilder();

        String nameField = isHaveFileName(mContentResolver) ? FILE_NAME : MediaStore.Files.FileColumns.TITLE;
        sb.append(nameField + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%" + mSearchName + "%");
        sb.append(" and ").append(MediaStore.Files.FileColumns.DATA + " like ");
        DatabaseUtils.appendEscapedSQLString(sb, "%" + mPath + "%");

        String selection = sb.toString();
        Cursor cursor = mContentResolver.query(uri, projection, selection,
                null, null);
        if (cursor == null)
        {
            Log.d(TAG, "doInBackground, cursor is null.");
            return OperationEventListener.ERROR_CODE_UNSUCCESS;
        }

        int total = cursor.getCount();
        publishProgress(new ProgressInfo("", 0, total, 0, total));
        int progress = 0;
        cursor.moveToFirst();
        try
        {
            boolean isShowHidden = Settings.instance().getShowDotAndHiddenFiles();
            while (!cursor.isAfterLast())
            {
                if (isCancelled())
                {
                    Log.d(TAG, "doInBackground,cancel.");
                    ret = OperationEventListener.ERROR_CODE_USER_CANCEL;
                    break;
                }
                String name = (String) cursor.getString(cursor
                        .getColumnIndex(MediaStore.Files.FileColumns.DATA));
                cursor.moveToNext();
                progress++;
                FileInfo fileInfo = Util.GetFileInfo(new File(name), null, isShowHidden);
                if (fileInfo.isHidden && !isShowHidden)
                    continue;
                publishProgress(new ProgressInfo(fileInfo, progress, total, progress, total));
            }
        } finally
        {
            cursor.close();
        }

        return ret;
    }
    
    private static final String FILE_NAME = "file_name";
    private boolean isHaveFileName(ContentResolver resolver)
    {
        Cursor cursor = null;
        try
        {
            Uri uri = MediaStore.Files.getContentUri("external");
            String[] projection = {FILE_NAME};
            cursor = resolver.query(uri, projection, null, null, null);
        } catch (Exception e)
        {
            return false;
        } finally
        {
            if (cursor != null)
                cursor.close();
        }
        
        return true;
    }
}

package com.ape.filemanager.updatehelper;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteFullException;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import com.ape.filemanager.GlobalConsts;

public final class MediaStoreHelper
{

    private static final String TAG = "MediaStoreHelper";
    private final Context mContext;
    private final Handler mHandler;
    // private BaseAsyncTask mBaseAsyncTask;
    private AsyncTask mBaseAsyncTask;
    private String mDstFolder;
    private static final int SCAN_FOLDER_NUM = 20;

    /**
     * Constructor of MediaStoreHelper
     * 
     * @param context
     *            the Application context
     */
    public MediaStoreHelper(Context context)
    {
        mContext = context;
        mHandler = new Handler();
    }
    // public MediaStoreHelper(Context context, BaseAsyncTask baseAsyncTask) {
    // mContext = context;
    // mBaseAsyncTask = baseAsyncTask;
    // }

    public MediaStoreHelper(Context context, AsyncTask baseAsyncTask)
    {
        this(context);
        mBaseAsyncTask = baseAsyncTask;
    }

    public void updateInMediaStore(String newPath, String oldPath)
    {
        Log.d(TAG, "updateInMediaStore,newPath =" + newPath + ",oldPath = "
                + oldPath);
        if (mContext != null && !TextUtils.isEmpty(newPath) && !TextUtils.isEmpty(oldPath)) {
            Uri uri = MediaStore.Files.getContentUri("external");
            uri = uri.buildUpon().appendQueryParameter("need_update_media_values", "true").build();

            String where = MediaStore.Files.FileColumns.DATA + "=?";
            String[] whereArgs = new String[] { oldPath };

            ContentResolver cr = mContext.getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.Files.FileColumns.DATA, newPath);
            try {
                Log.d(TAG, "updateInMediaStore,update.");
                cr.update(uri, values, where, whereArgs);

                // mediaProvicer.update() only update data columns of
                // database, it is need to other fields of the database, so scan the
                // new path after update(). see ALPS00416588
                scanPathforMediaStore(newPath);
            } catch (NullPointerException e) {
                Log.e(TAG, "Error, NullPointerException:" + e + ",update db may failed!!!");
            } catch (SQLiteFullException e) {
                Log.e(TAG, "Error, database or disk is full!!!" + e);
                if (mBaseAsyncTask != null) {
                    mBaseAsyncTask.cancel(true);
                }
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "Error, database is closed!!!");
            } catch (Exception e) {
                Log.e(TAG, "Error, other database exception!!!"+e);
            }
        }
    }
    
    private boolean isMediaScannerScanning()
    {
        boolean result = false;
        final Cursor cursor = mContext.getContentResolver().query(MediaStore.getMediaScannerUri(),
                new String[] {MediaStore.MEDIA_SCANNER_VOLUME},
                null, null, null);
        if (cursor != null)
        {
            if (cursor.getCount() == 1)
            {
                cursor.moveToFirst();
                result = "external".equals(cursor.getString(0));
            }
            cursor.close();
        }
        return result;
    }

    private Runnable mPendingMediaScan;
    public void handleMediaScan()
    {
        if (mHandler == null)
        {
            return;
        }

        if (mPendingMediaScan != null)
        {
            mHandler.removeCallbacks(mPendingMediaScan);
        }
        
        if (mPendingMediaScan == null)
        {
            mPendingMediaScan = new Runnable() {
                public void run() {
                    if (isMediaScannerScanning()) {
                        Log.d(TAG, "MediaScanner is scanning, wait for a moment");
                        mHandler.postDelayed(this, 1000);
                    } else {
                        doMediaScan();
                    }
                }
            };
        }
        
        mHandler.postDelayed(mPendingMediaScan, 100);
    }
    
    private synchronized void doMediaScan() {
        Log.d(TAG, "doMediaScan: " + (mPendingMediaScan != null ? "yes" : "no"));

        if (mPendingMediaScan != null && mContext != null) {
            Bundle args = new Bundle();
            args.putString("volume", "external");
            mContext.startService(new Intent("android.media.IMediaScannerService").putExtras(args));

            mHandler.removeCallbacks(mPendingMediaScan);
            mPendingMediaScan = null;
        }
    }

    /**
     * scan Path for new file or folder in MediaStore
     * 
     * @param path
     *            the scan path
     */
    public void scanPathforMediaStore(String path)
    {
        Log.d(TAG, "scanPathforMediaStore.path =" + path);
        if (mContext != null && !TextUtils.isEmpty(path))
        {
            String[] paths =
            { path };
            Log.d(TAG, "scanPathforMediaStore,scan file .");
            MediaScannerConnection.scanFile(mContext, paths, null, new FileScanCommletedCallBack(paths.length));
        }
    }

    public void scanPathforMediaStore(List<String> scanPaths)
    {
        Log.d(TAG, "scanPathforMediaStore,scanPaths.");
        int length = scanPaths.size();
        if (mContext != null && length > 0)
        {
            String[] paths = new String[scanPaths.size()];
//            if (mDstFolder != null && length > SCAN_FOLDER_NUM)
//            {
//                paths = new String[] { mDstFolder };
//            } else
            {
                paths = new String[length];
                scanPaths.toArray(paths);
            }
            Log.d(TAG, "scanPathforMediaStore,scan file.");
            MediaScannerConnection.scanFile(mContext, paths, null, new FileScanCommletedCallBack(paths.length));
        }
    }

    /**
     * delete the record in MediaStore
     * 
     * @param paths
     *            the delete file or folder in MediaStore
     */
    public void deleteFileInMediaStore(List<String> paths)
    {
        Log.d(TAG, "deleteFileInMediaStore.");
        Uri uri = MediaStore.Files.getContentUri("external");
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("?");
        for (int i = 0; i < paths.size() - 1; i++)
        {
            whereClause.append(",?");
        }
        String where = MediaStore.Files.FileColumns.DATA + " IN("
                + whereClause.toString() + ")";
        // notice that there is a blank before "IN(".
        if (mContext != null && !paths.isEmpty())
        {
            ContentResolver cr = mContext.getContentResolver();
            String[] whereArgs = new String[paths.size()];
            paths.toArray(whereArgs);
            Log.d(TAG, "deleteFileInMediaStore,delete.");
            try {
                cr.delete(uri, where, whereArgs);
                sendScanCompleted(whereArgs[0]);
            } catch (SQLiteFullException e) {
                Log.e(TAG, "Error, database or disk is full!!!" + e);
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "Error, database is closed!!!");
            } catch (Exception e) {
                Log.e(TAG, "Error, other database exception!!!"+e);
            }
        }
    }

    /**
     * delete the record in MediaStore
     * 
     * @param path
     *            the delete file or folder in MediaStore
     */
    public void deleteFileInMediaStore(String path)
    {
        Log.d(TAG, "deleteFileInMediaStore,path =" + path);
        if (TextUtils.isEmpty(path))
        {
            return;
        }
        Uri uri = MediaStore.Files.getContentUri("external");
        String where = MediaStore.Files.FileColumns.DATA + "=?";
        String[] whereArgs = new String[]{ path };
        if (mContext != null)
        {
            ContentResolver cr = mContext.getContentResolver();
            Log.d(TAG, "deleteFileInMediaStore,delete.");
            try {
                cr.delete(uri, where, whereArgs);
                sendScanCompleted(whereArgs[0]);
            } catch (SQLiteFullException e) {
                Log.e(TAG, "Error, database or disk is full!!!" + e);
            } catch (UnsupportedOperationException e) {
                Log.e(TAG, "Error, database is closed!!!");
            } catch (Exception e) {
                Log.e(TAG, "Error, other database exception!!!"+e);
            }
        }
    }
    
    /**
     * Set dstfolder so when scan files size more than SCAN_FOLDER_NUM use folder
     * path to make scanner scan this folder directly.
     * 
     * @param dstFolder
     */
    public void setDstFolder(String dstFolder) {
        mDstFolder = dstFolder;
    }
    
    private class FileScanCommletedCallBack implements MediaScannerConnection.OnScanCompletedListener
    {
        int mCurrentCount;
        int mTotalCount;
        public FileScanCommletedCallBack(int totalCount)
        {
            mTotalCount = totalCount;
            mCurrentCount = 0;
        }

        @Override
        public void onScanCompleted(String path, Uri uri)
        {
            mCurrentCount++;
            if (mCurrentCount >= mTotalCount)
            {
                sendScanCompleted(path);
            }
        }
    }
    
    private void sendScanCompleted(String path)
    {
        Uri uri = Uri.parse("file://" + path);
        mContext.sendBroadcast(new Intent(GlobalConsts.ACTION_SCAN_FILES_COMPLETED, uri));
    }
}

/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ape.filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.ape.filemanager.BaseAsyncTask.OperationEventListener;
import com.ape.filemanager.Util.SDCardInfo;
import com.ape.filemanager.updatehelper.MediaStoreHelper;
import com.ape.filemanager.updatehelper.MultiMediaStoreHelper.DeleteMediaStoreHelper;
import com.ape.filemanager.updatehelper.MultiMediaStoreHelper.PasteMediaStoreHelper;

public class FileOperationHelper {
    private static final String LOG_TAG = "FileOperation";

    private ArrayList<FileInfo> mCurFileNameList = new ArrayList<FileInfo>();

    private boolean mMoving;

    private IOperationProgressListener mOperationListener;

    private FilenameFilter mFilter;
    
    private MediaStoreHelper mMediaProviderHelper;
    private PasteMediaStoreHelper mPasteMediaStoreHelper;
    private DeleteMediaStoreHelper mDeleteMediaStoreHelper;

    public interface IOperationProgressListener {
        Context getContext();

        void onFinish();

        void onFileChanged(String path);
        
        void onTaskResult(int errorCode, int operatorStrId);
    }

    public FileOperationHelper(IOperationProgressListener l) {
        mOperationListener = l;
        mMediaProviderHelper = new MediaStoreHelper(mOperationListener.getContext());
        mPasteMediaStoreHelper = new PasteMediaStoreHelper(mMediaProviderHelper);
        mDeleteMediaStoreHelper = new DeleteMediaStoreHelper(mMediaProviderHelper);
    }

    public void setFilenameFilter(FilenameFilter f) {
        mFilter = f;
    }

    public int CreateFolder(String path, String name) {
        Log.v(LOG_TAG, "CreateFolder >>> " + path + "," + name);

        File pathFile = new File(path);
        if (pathFile.getFreeSpace() <= 0) {
            return OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE;
        }

        File f = new File(Util.makePath(path, name));
        if (f.exists()) {
            return OperationEventListener.ERROR_CODE_FILE_EXIST;
        }

        if (f.mkdir()) {
            mMediaProviderHelper.scanPathforMediaStore(f.getAbsolutePath());
            //mMediaProviderHelper.handleMediaScan();
            return OperationEventListener.ERROR_CODE_SUCCESS;
        } else {
            return OperationEventListener.ERROR_CODE_NO_PERMISSION;
        }
    }

    public void Copy(ArrayList<FileInfo> files) {
        copyFileList(files);
    }

    public boolean Paste(String path) {
        if (mCurFileNameList.size() == 0)
            return false;

        if (!isEnoughSpace(mCurFileNameList, path))
        {
            clear();
            if (mOperationListener != null) {
                mOperationListener.onTaskResult(
                        OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE,
                        R.string.fail_to_paste_file);
                mOperationListener.onFinish();
            }
            return false;
        }
        
        final String _path = path;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                mPasteMediaStoreHelper.setDstFolder(_path);
                for (FileInfo f : mCurFileNameList) {
                    CopyFile(f, _path);
                }

                mPasteMediaStoreHelper.updateRecords();
                mPasteMediaStoreHelper.setDstFolder(null);
//                mOperationListener.onFileChanged(Environment
//                        .getExternalStorageDirectory()
//                        .getAbsolutePath());

                clear();
            }
        });

        return true;
    }

    public boolean canPaste() {
        return mCurFileNameList.size() != 0;
    }

    public void StartMove(ArrayList<FileInfo> files) {
        if (mMoving)
            return;

        mMoving = true;
        copyFileList(files);
    }

    public boolean isMoveState() {
        return mMoving;
    }

    public boolean canMove(String path) {
        for (FileInfo f : mCurFileNameList) {
            if (!f.IsDir)
                continue;

            if (Util.containsPath(f.filePath, path))
                return false;
        }

        return true;
    }

    public void clear() {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
        }
    }

    public boolean EndMove(String path) {
        if (!mMoving)
            return false;
        mMoving = false;

        if (TextUtils.isEmpty(path))
            return false;
        
        if (mCurFileNameList.size() > 0)
        {
            String dstDriver = Util.getRootPathFromFilePath(path);
            String srcDriver = Util.getRootPathFromFilePath(mCurFileNameList.get(0).filePath);
            if (!srcDriver.equals(dstDriver))
            {
                if (!isEnoughSpace(mCurFileNameList, path))
                {
                    clear();
                    if (mOperationListener != null) {
                        mOperationListener.onTaskResult(
                                OperationEventListener.ERROR_CODE_NOT_ENOUGH_SPACE,
                                R.string.fail_to_paste_file);
                        mOperationListener.onFinish();
                    }
                    return false;
                }
            }
        }

        final String _path = path;
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                    mPasteMediaStoreHelper.setDstFolder(_path);
                    for (FileInfo f : mCurFileNameList) {
                        MoveFile(f, _path);
                    }

                    mPasteMediaStoreHelper.updateRecords();
                    mDeleteMediaStoreHelper.updateRecords();
                    mPasteMediaStoreHelper.setDstFolder(null);
//                    mOperationListener.onFileChanged(Environment
//                            .getExternalStorageDirectory()
//                            .getAbsolutePath());

                    clear();
                }
        });

        return true;
    }

    public ArrayList<FileInfo> getFileList() {
        return mCurFileNameList;
    }

    @SuppressWarnings("unchecked")
    private void asnycExecute(Runnable r) {
        final Runnable _r = r;
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                synchronized(mCurFileNameList) {
                    _r.run();
                }

                return null;
            }

            @Override
            protected void onPreExecute()
            {
                takeWakeLock();
            }

            @Override
            protected void onPostExecute(Object result)
            {
                super.onPostExecute(result);
                Log.i(LOG_TAG, "asnycExecute, OperationListener:" + mOperationListener);
                if (mOperationListener != null) {
                    mOperationListener.onFinish();
                }
                releaseWakeLock();
            }
            
        }.execute();
    }

    public boolean isFileSelected(String path) {
        synchronized(mCurFileNameList) {
            for (FileInfo f : mCurFileNameList) {
                if (f.filePath.equalsIgnoreCase(path))
                    return true;
            }
        }
        return false;
    }

    public int Rename(FileInfo f, String newName) {
        if (f == null || newName == null) {
            Log.e(LOG_TAG, "Rename: null parameter");
            return OperationEventListener.ERROR_CODE_NAME_EMPTY;
        }

        File file = new File(f.filePath);
        String newPath = Util.makePath(Util.getPathFromFilepath(f.filePath), newName);
        
        File newFile = new File(newPath);
        if (newFile.exists()) {
            return OperationEventListener.ERROR_CODE_FILE_EXIST;
        }
//        final boolean needScan = file.isFile();
        try {
            boolean ret = file.renameTo(new File(newPath));
            if (ret) {
                //mMediaProviderHelper.updateInMediaStore(newPath, f.filePath);
                mMediaProviderHelper.handleMediaScan();
                return OperationEventListener.ERROR_CODE_SUCCESS;
//                if (needScan) {
//                    mOperationListener.onFileChanged(f.filePath);
//                }
//                mOperationListener.onFileChanged(newPath);
            }
        } catch (SecurityException e) {
            Log.e(LOG_TAG, "Fail to rename file," + e.toString());
        }
        return OperationEventListener.ERROR_CODE_NO_PERMISSION;
    }

    public boolean Delete(ArrayList<FileInfo> files) {
        copyFileList(files);
        asnycExecute(new Runnable() {
            @Override
            public void run() {
                for (FileInfo f : mCurFileNameList) {
                    DeleteFile(f);
                }

                mDeleteMediaStoreHelper.updateRecords();
//                mOperationListener.onFileChanged(Environment
//                        .getExternalStorageDirectory()
//                        .getAbsolutePath()); 

                clear();
            }
        });
        return true;
    }

    protected void DeleteFile(FileInfo f) {
        if (f == null) {
            Log.e(LOG_TAG, "DeleteFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        boolean directory = file.isDirectory();
        if (directory) {
            for (File child : file.listFiles(mFilter)) {
                if (Util.isNormalFile(child.getAbsolutePath())) {
                    DeleteFile(Util.GetFileInfo(child, mFilter, true));
                }
            }
        }

        mDeleteMediaStoreHelper.addRecord(file.getAbsolutePath());
        file.delete();

        Log.v(LOG_TAG, "DeleteFile >>> " + f.filePath);
    }

    private void CopyFile(FileInfo f, String dest) {
        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter");
            return;
        }

        File file = new File(f.filePath);
        if (file.isDirectory()) {

            // directory exists in destination, rename it
            String destPath = Util.makePath(dest, f.fileName);
            File destFile = new File(destPath);
            int i = 1;
            while (destFile.exists()) {
                destPath = Util.makePath(dest, f.fileName + " " + i++);
                destFile = new File(destPath);
            }

            destFile.mkdir(); //STGOO-465

            for (File child : file.listFiles(mFilter)) {
                if (!child.isHidden() && Util.isNormalFile(child.getAbsolutePath())) {
                    CopyFile(Util.GetFileInfo(child, mFilter, Settings.instance().getShowDotAndHiddenFiles()), destPath);
                }
            }
        } else {
            String destFile = Util.copyFile(f.filePath, dest);
            if (destFile != null)
                mPasteMediaStoreHelper.addRecord(destFile);
        }
        Log.v(LOG_TAG, "CopyFile >>> " + f.filePath + "," + dest);
    }

    private boolean MoveFile(FileInfo f, String dest) {
        Log.v(LOG_TAG, "MoveFile >>> " + f.filePath + "," + dest);

        if (f == null || dest == null) {
            Log.e(LOG_TAG, "CopyFile: null parameter");
            return false;
        }
        
        String srcDriver = Util.getRootPathFromFilePath(f.filePath);
        String dstDriver = Util.getRootPathFromFilePath(dest);

        if (srcDriver.equals(dstDriver))
        {
            File file = new File(f.filePath);
            String newPath = Util.makePath(dest, f.fileName);
            boolean renameResult = false;
            if (f.filePath.equals(newPath))
            {
                return renameResult;
            }
            try {
                renameResult = file.renameTo(new File(newPath));
                if (renameResult) {
                    mPasteMediaStoreHelper.addRecord(newPath);
                    mDeleteMediaStoreHelper.addRecord(f.filePath);
                }
            } catch (SecurityException e) {
                Log.e(LOG_TAG, "Fail to move file," + e.toString());
            }

            return renameResult;
        } else
        {
            CopyFile(f,dest);
            DeleteFile(f);
        }
        return false;
    }

    private void copyFileList(ArrayList<FileInfo> files) {
        synchronized(mCurFileNameList) {
            mCurFileNameList.clear();
            for (FileInfo f : files) {
                mCurFileNameList.add(f);
            }
        }
    }
    
    public boolean isEnoughSpace(ArrayList<FileInfo> files, String dstFolder)
    {
        //File file = new File(dstFolder);

        SDCardInfo spaceInfo = Util.getSDCardInfoByPath(dstFolder);
        if (spaceInfo != null)
        {
            long freeSpace = spaceInfo.free; //file.getFreeSpace();
            long needSpace = getFileListSpaceSize(files);

            Log.i(LOG_TAG, "isEnoughSpace, freeSpace:" + freeSpace + ", needSpace" + needSpace);
            return (freeSpace > needSpace);
        } else {
            return false;
        }
    }

    private long getFileListSpaceSize(ArrayList<FileInfo> files)
    {
        long totalSize = 0;
        
        for (FileInfo f : files)
        {
            totalSize += getFileSize(new File(f.filePath));
        }
        return totalSize;
    }
    
    private long getFileSize(File srcFile)
    {
        long size = srcFile.length();
        
        if (srcFile.isDirectory() && srcFile.canRead())
        {
            File[] files = srcFile.listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    size += getFileSize(file);
                }
            }
        }
        
        return size;
    }

    public static final String WAKE_LOCK_TAG = "FileManager";
    private PowerManager.WakeLock mWakeLock;

    @SuppressLint("Wakelock")
    private void takeWakeLock()
    {
        Context context = mOperationListener.getContext();
        if (context != null)
        {
            if (mWakeLock == null)
            {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
                mWakeLock.setReferenceCounted(false);
            }
            mWakeLock.acquire();
            Log.i(LOG_TAG, "takeWakeLock, mWakeLock.acquire");
        }
    }

    private void releaseWakeLock()
    {
        if (mWakeLock != null)
        {
            mWakeLock.release();
            mWakeLock = null;
            Log.i(LOG_TAG, "releaseWakeLock, mWakeLock.release");
        }
    }
}

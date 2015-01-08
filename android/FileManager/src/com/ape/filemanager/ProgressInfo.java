package com.ape.filemanager;

import java.util.List;

import com.cloud.client.file.CloudFile;

public class ProgressInfo
{
    private static final String TAG = "ProgressInfo";
    private String mUpdateInfo = null;
    private final int mProgress;
    private int mErrorCode = 0;
    private final long mTotal;
    private final boolean mIsFailInfo;
    private FileInfo mFileInfo = null;
    private final long mTotalNumber;
    private final int mCurrentNumber;
    
    // For cloud file
    private List<CloudFile> mCloudFiles;

    /**
     * Constructor to construct a ProgressInfo
     * 
     * @param update the string which will be shown on ProgressDialogFragment
     * @param progeress current progress number
     * @param total total number
     */
    public ProgressInfo(String update, int progeress, long total, int currentNumber,
            long totalNumber) {
       // LogUtils.d(TAG, "ProgressInfo1,currentNumber=" + currentNumber + ",totalNumber = " + totalNumber);
        mUpdateInfo = update;
        mProgress = progeress;
        mTotal = total;
        mIsFailInfo = false;
        mCurrentNumber = currentNumber;
        mTotalNumber = totalNumber;
    }

    /**
     * Constructor to construct a ProgressInfo
     * 
     * @param fileInfo the fileInfo which will be associated with Dialog
     * @param progeress current progress number
     * @param total total number
     */
    public ProgressInfo(FileInfo fileInfo, int progeress, long total, int currentNumber,
            long totalNumber) {
       // LogUtils.d(TAG, "ProgressInfo2,currentNumber=" + currentNumber + ",totalNumber = " + totalNumber);
        mFileInfo = fileInfo;
        mProgress = progeress;
        mTotal = total;
        mIsFailInfo = false;
        mCurrentNumber = currentNumber;
        mTotalNumber = totalNumber;
    }

    /**
     * Constructor to construct a ProgressInfo
     * 
     * @param errorCode An int represents ERROR_CODE
     * @param isFailInfo status of task associated with certain progressDialog
     */
    public ProgressInfo(int errorCode, boolean isFailInfo) {
        mErrorCode = errorCode;
        mProgress = 0;
        mTotal = 0;
        mIsFailInfo = isFailInfo;
        mCurrentNumber = 0;
        mTotalNumber = 0;
    }

    /**
     * Constructor to construct a ProgressInfo for Cloud files
     * 
     * @param files CloudFile list
     * @param 
     */
    public ProgressInfo(List<CloudFile> files, int progeress, long total, int currentNumber,
            long totalNumber) {
        mCloudFiles = files;
        mProgress = progeress;
        mTotal = total;
        mIsFailInfo = false;
        mCurrentNumber = currentNumber;
        mTotalNumber = totalNumber;
    }

    /**
     * This method gets cloud file list of task doing in background
     *
     * @return mCloudFiles, which contains file's information(name, size, and so on)
     */
    public List<CloudFile> getCloudFiles(){
        return mCloudFiles;
    }

    /**
     * This method gets status of task doing in background
     *
     * @return true for failed, false for no fail occurs in task
     */
    public boolean isFailInfo() {
        return mIsFailInfo;
    }

    /**
     * This method gets fileInfo, which will be updated on DetaiDialog
     * 
     * @return fileInfo, which contains file's information(name, size, and so on)
     */
    public FileInfo getFileInfo() {
        return mFileInfo;
    }

    /**
     * This method gets ERROR_CODE for certain task, which is doing in background.
     * 
     * @return ERROR_CODE for certain task
     */
    public int getErrorCode() {
        return mErrorCode;
    }

    /**
     * This method gets the content, which will be updated on ProgressDialog
     * 
     * @return content, which need update
     */
    public String getUpdateInfo() {
        return mUpdateInfo;
    }

    /**
     * This method gets current progress number
     * 
     * @return current progress number of progressDialog
     */
    public int getProgeress() {
        return mProgress;
    }

    /**
     * This method gets total number of progressDialog
     * 
     * @return total number
     */
    public long getTotal() {
        return mTotal;
    }

    /**
     * This method gets current number of files
     *
     * @return current number
     */
    public int getCurrentNumber() {
        return mCurrentNumber;
    }

    /**
     * This method gets total number of files
     *
     * @return total number
     */
    public long getTotalNumber() {
        return mTotalNumber;
    }
}

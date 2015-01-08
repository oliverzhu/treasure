/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 * 
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

package com.ape.filemanager;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ape.filemanager.R;
import com.ape.filemanager.Util.SDCardInfo;

public final class MountPointManager
{
	private static final String TAG = "MountPointManager";

	public static final String SEPARATOR = "/";

	private static MountPointManager sInstance = new MountPointManager();

	private Context mContext;
	private StorageManager mStorageManager = null;
	private final CopyOnWriteArrayList<MountPoint> mMountPathList = new CopyOnWriteArrayList<MountPoint>();

	// for reflect StorageManager
	private static final String GET_VOLUME_PATHS_NAME = "getVolumePaths";
	private static final String GET_VOLUME_STATE_NAME = "getVolumeState";
	private Method mMethodGetPaths;
	private Method mMethodGetState;

	private MountPointManager()
	{
	}

	/**
	 * This method initializes MountPointManager.
	 * 
	 * @param context
	 *            Context to use
	 */
	public void init(Context context)
	{
		mContext = context;
		mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
		mMountPathList.clear();

		try
		{
			mMethodGetPaths = mStorageManager.getClass().getMethod(GET_VOLUME_PATHS_NAME);
			mMethodGetState = mStorageManager.getClass().getMethod(GET_VOLUME_STATE_NAME, new Class[]{String.class});
		} catch (NoSuchMethodException e)
		{
			e.printStackTrace();
			return;
		}
		
		long start = System.currentTimeMillis();
		//String internalSDPath = Util.getInternalStorageDirectory();

		// check media availability to init mMountPathList
		String[] volumePathList = getVolumePaths();
		if (volumePathList != null)
		{
		    boolean isHasOneExternalSD = false;
			for (int i = 0; i < volumePathList.length; i++)
			{
				MountPoint mountPoint = new MountPoint();
				mountPoint.mPath = volumePathList[i];
				mountPoint.mIsMounted = isMounted(volumePathList[i]);

				if (TextUtils.isEmpty(mountPoint.mPath))
				    continue;

				if (mountPoint.mPath.contains("usb"))
				{
                    mountPoint.mIsExternal = true;
                    mountPoint.mDescription = mContext.getString(R.string.usb_storage);
                    mountPoint.mMountIcon = mContext.getResources().getDrawable(R.drawable.usb_otg_storage);
				} else
				{
    			    if (Util.getSdDirectory().equals(mountPoint.mPath))
    			    {
    			        mountPoint.mIsExternal = Environment.isExternalStorageRemovable();
                    } else
                    {
                        mountPoint.mIsExternal = !isHasOneExternalSD;
                    }
    			    if (mountPoint.mIsExternal)
    			        isHasOneExternalSD = true;

    			    if (mountPoint.mIsExternal)
    			    {
    			        mountPoint.mDescription = mContext.getString(R.string.storage_sd_card);
    			        mountPoint.mMountIcon = mContext.getResources().getDrawable(R.drawable.sd_card_storage);
    			    } else
    			    {
    			        mountPoint.mDescription = mContext.getString(R.string.phone_storage);
    			        mountPoint.mMountIcon = mContext.getResources().getDrawable(R.drawable.phone_storage);
                    }
				}
				mountPoint.mMaxFileSize = 0;

				mMountPathList.add(mountPoint);
			}
		}

		sortMountPoint();
        long dur = System.currentTimeMillis() - start;
        Log.i(TAG, "MountPointManager.init dur:" + dur);
	}

	private void sortMountPoint()
	{
	    int count = mMountPathList.size();
	    for (int index = 0; index < count/2; index++)
	    {
	        int invert = count - 1 - index;
	        MountPoint tmp = mMountPathList.get(index);
	        mMountPathList.set(index, mMountPathList.get(invert));
	        mMountPathList.set(invert, tmp);
	    }
	}

	private String[] getVolumePaths()
	{
		String[] paths = null;
		try
		{
			if (mMethodGetPaths != null)
			{
				paths = (String[]) mMethodGetPaths.invoke(mStorageManager);
			}
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return paths;
	}
	
	private String getVolumeState(String volumePath)
	{
		String state = null;
		try
		{
			if (mMethodGetState != null && volumePath != null)
			{
			state = (String) mMethodGetState.invoke(mStorageManager, new Object[]{volumePath});
			}
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return state;
	}

	/**
	 * This method gets instance of MountPointManager. Before calling this
	 * method, must call init().
	 * 
	 * @return instance of MountPointManager
	 */
	public static MountPointManager getInstance()
	{
		return sInstance;
	}

	private static class MountPoint
	{
		String mDescription;
		String mPath;
		Drawable mMountIcon;
		boolean mIsExternal;
		boolean mIsMounted;
		long mMaxFileSize;
	}

	/**
	 * This method gets informations of file of mount point path
	 * 
	 * @return fileInfos of mount point path
	 */
	public List<MountFileInfo> getMountPointFileInfo()
	{
		List<MountFileInfo> fileInfos = new ArrayList<MountFileInfo>(3);
		for (MountPoint mp : mMountPathList)
		{
			if (mp.mIsMounted)
			{
			    MountFileInfo info = new MountFileInfo();
			    info.fileName = Util.getNameFromFilepath(mp.mPath);
			    info.filePath = mp.mPath;
			    info.canRead = true;
			    info.canWrite = false;
			    info.isHidden = false;
			    info.IsDir = true;
			    
			    info.displayName = mp.mDescription;
			    info.mountIcon = mp.mMountIcon;
			    info.isExternal = mp.mIsExternal;

			    SDCardInfo storageInfo = Util.getSDCardInfoByPath(mp.mPath);
			    if (storageInfo != null)
			    {
	                info.freeSpace = storageInfo.free;
	                info.totalSpace = storageInfo.total;
	                fileInfos.add(info);
			    }
			}
		}
		return fileInfos;
	}
	
    /**
     * This method gets paths of file of mount point path
     * 
     * @return fileInfos of mount point path
     */
    public List<String> getMountPointPaths()
    {
        List<String> mountPaths = new ArrayList<String>(3);
        for (MountPoint mp : mMountPathList)
        {
            if (mp.mIsMounted)
            {
                mountPaths.add(mp.mPath);
            }
        }
        return mountPaths;
    }

	/**
	 * This method gets count of mount, number of mount point(s)
	 * 
	 * @return number of mount point(s)
	 */
	public int getMountCount()
	{
		int count = 0;
		for (MountPoint mPoint : mMountPathList)
		{
			if (mPoint.mIsMounted)
			{
				count++;
			}
		}
		Log.d(TAG, "getMountCount,count = " + count);
		return count;
	}

	/**
	 * This method checks whether SDcard is mounted or not
	 * 
	 * @param mountPoint
	 *            the mount point that should be checked
	 * @return true if SDcard is mounted, false otherwise
	 */
	protected boolean isMounted(String mountPoint)
	{
		if (TextUtils.isEmpty(mountPoint))
		{
			return false;
		}
		String state = null;

		state = getVolumeState(mountPoint);
		Log.d(TAG, "isMounted, mountPoint = " + mountPoint + ", state = " + state);
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	/**
	 * This method checks whether SDcard is mounted or not
	 * 
	 * @param path
	 *            the path that should be checked
	 * @return true if SDcard is mounted, false otherwise
	 */
	protected boolean isRootPathMount(String path)
	{
		Log.d(TAG, "isRootPathMount,  path = " + path);
		boolean ret = false;
		if (path == null)
		{
			return ret;
		}
		ret = isMounted(getRealMountPointPath(path));
		Log.d(TAG, "isRootPathMount,  ret = " + ret);
		return ret;
	}

	/**
	 * This method gets real mount point path for certain path.
	 * 
	 * @param path
	 *            certain path to be checked
	 * @return real mount point path for certain path, "" for path is not
	 *         mounted
	 */
	public String getRealMountPointPath(String path)
	{
		Log.d(TAG, "getRealMountPointPath ,path =" + path);
		for (MountPoint mountPoint : mMountPathList)
		{
			if (path.startsWith(mountPoint.mPath))
			{
				Log.d(TAG, "getRealMountPointPath = " + mountPoint.mPath);
				return mountPoint.mPath;
			}
		}
		Log.d(TAG, "getRealMountPointPath = \"\" ");
		return "";
	}

	/**
	 * This method checks weather certain path is a FAT32 disk.
	 * 
	 * @param path
	 *            certain path to be checked
	 * @return true for FAT32, and false for not.
	 */
	public boolean isFat32Disk(String path)
	{
		Log.d(TAG, "isFat32Disk ,path =" + path);
		for (MountPoint mountPoint : mMountPathList)
		{
			if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR))
			{
				Log.d(TAG, "isFat32Disk = " + mountPoint.mPath);
				if (mountPoint.mMaxFileSize > 0)
				{
					Log.d(TAG, "isFat32Disk = true.");
					return true;
				}
				Log.d(TAG, "isFat32Disk = false.");
				return false;
			}
		}

		Log.d(TAG, "isFat32Disk = false.");
		return false;
	}

	/**
	 * This method changes mount state of mount point, if parameter path is
	 * mount point.
	 * 
	 * @param path
	 *            certain path to be checked
	 * @param isMounted
	 *            flag to mark weather certain mount point is under mounted
	 *            state
	 * @return true for change success, and false for fail
	 */
	public boolean changeMountState(String path, Boolean isMounted)
	{
		boolean ret = false;
		for (MountPoint mountPoint : mMountPathList)
		{
			if (mountPoint.mPath.equals(path))
			{
				if (mountPoint.mIsMounted == isMounted)
				{
					break;
				} else
				{
					mountPoint.mIsMounted = isMounted;
					ret = true;
					break;
				}
			}
		}
		Log.d(TAG, "changeMountState ,path =" + path + ",ret = " + ret);

		return ret;
	}

	/**
	 * This method checks weather certain path is mount point.
	 * 
	 * @param path
	 *            certain path, which needs to be checked
	 * @return true for mount point, and false for not mount piont
	 */
	public boolean isMountPoint(String path)
	{
		boolean ret = false;
		Log.d(TAG, "isMountPoint ,path =" + path);
		if (path == null)
		{
			return ret;
		}
		for (MountPoint mountPoint : mMountPathList)
		{
			if (path.equals(mountPoint.mPath))
			{
				ret = true;
				break;
			}
		}
		Log.d(TAG, "isMountPoint ,ret =" + ret);
		return ret;
	}

	/**
	 * This method checks weather certain path is internal mount path.
	 * 
	 * @param path
	 *            path which needs to be checked
	 * @return true for internal mount path, and false for not internal mount
	 *         path
	 */
	public boolean isInternalMountPath(String path)
	{
		Log.d(TAG, "isInternalMountPath ,path =" + path);
		if (path == null)
		{
			return false;
		}
		for (MountPoint mountPoint : mMountPathList)
		{
			if (!mountPoint.mIsExternal && mountPoint.mPath.equals(path))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks weather certain path is external mount path.
	 * 
	 * @param path
	 *            path which needs to be checked
	 * @return true for external mount path, and false for not external mount
	 *         path
	 */
	public boolean isExternalMountPath(String path)
	{
		Log.d(TAG, "isExternalMountPath ,path =" + path);
		if (path == null)
		{
			return false;
		}
		for (MountPoint mountPoint : mMountPathList)
		{
			if (mountPoint.mIsExternal && mountPoint.mPath.equals(path))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * This method checks weather certain file is External File.
	 * 
	 * @param fileInfo
	 *            certain file needs to be checked
	 * @return true for external file, and false for not external file
	 */
	public boolean isExternalFile(FileInfo fileInfo)
	{
		boolean ret = false;
		if (fileInfo != null)
		{
			String mountPath = getRealMountPointPath(fileInfo.filePath);
			if (mountPath.equals(fileInfo.filePath))
			{
				Log.d(TAG, "isExternalFile,return false .mountPath = " + mountPath);
				ret = false;
			}
			if (isExternalMountPath(mountPath))
			{
				ret = true;
			}
		}

		Log.d(TAG, "isExternalFile,ret = " + ret);

		return ret;
	}

	/**
	 * This method gets description of certain path
	 * 
	 * @param path
	 *            certain path
	 * @return description of the path
	 */
	public String getDescriptionPath(String path)
	{
		Log.d(TAG, "getDescriptionPath ,path =" + path);
		if (mMountPathList != null)
		{
			for (MountPoint mountPoint : mMountPathList)
			{
				if ((path + SEPARATOR).startsWith(mountPoint.mPath + SEPARATOR))
				{
					return path.length() > mountPoint.mPath.length() + 1 ? mountPoint.mDescription + SEPARATOR + path.substring(mountPoint.mPath.length() + 1)
							: mountPoint.mDescription;
				}
			}
		}
		return path;
	}

	public boolean isPrimaryVolume(String path)
	{
		Log.d(TAG, "isPrimaryVolume ,path =" + path);
		if (mMountPathList.size() > 0)
		{
			return mMountPathList.get(0).mPath.equals(path);
		} else
		{
			Log.w(TAG, "mMountPathList null!");
			return false;
		}
	}
}

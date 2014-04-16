package com.custom.music.util;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;

//import com.mediatek.drm.OmaDrmClient;
//import com.mediatek.drm.OmaDrmStore;
//import com.mediatek.drm.OmaDrmUiUtils;

public class DrmUtils {
//	private static OmaDrmClient mDrmClient = null;
	
	public static final int DRMMETHOD_METHOD_FL = 1;
			//OmaDrmStore.DrmMethod.METHOD_FL;
	
	public static final int RIGHTSSTATUS_RIGHTS_VALID = 0x00;
			//OmaDrmStore.RightsStatus.RIGHTS_VALID;
	
	public static final int RIGHTSSTATUS_RIGHTS_INVALID = 0x01;
			//OmaDrmStore.RightsStatus.RIGHTS_INVALID
	
	public static final int RIGHTSSTATUS_SECURE_TIMER_INVALID = 0x04;
			//OmaDrmStore.RightsStatus.SECURE_TIMER_INVALID;
	
	public static boolean isSupportDrm()
	{
		return false;
//		return MusicFeatureOption.IS_SUPPORT_DRM;
	}
	
	public static void showProtectionInfoDialog(Context context,long mSelectedId)
	{
//		OmaDrmUiUtils.showProtectionInfoDialog(context, 
//                ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mSelectedId));
	}
	
	public static int checkRightsStatusForTap(Context context,Uri uri){
		return 0;
//		if(mDrmClient == null)
//		{
//			mDrmClient = new OmaDrmClient(context);
//		}
//		return mDrmClient.mDrmClient.checkRightsStatusForTap(uri, OmaDrmStore.Action.PLAY);
	}
	
	public static int checkRightsStatus(Context context,Uri uri){
		return 0;
//		if(mDrmClient == null)
//		{
//			mDrmClient = new OmaDrmClient(context);
//		}
//		return mDrmClient.mDrmClient.checkRightsStatus(uri, OmaDrmStore.Action.PLAY);
	}
	
	public static void showConsumeDialog(Context context)
	{
//		OmaDrmUiUtils.showConsumeDialog(context, context, null);
	}
	
	public static void showRefreshLicenseDialog(Context context,Uri uri)
	{
//		if(mDrmClient == null)
//		{
//			mDrmClient = new OmaDrmClient(context);
//		}
//		OmaDrmUiUtils.showRefreshLicenseDialog(mDrmClient, context, 
//                uri, null);
	}
	
	public static void showSecureTimerInvalidDialog(Context context)
	{
//		OmaDrmUiUtils.showSecureTimerInvalidDialog(context, null, null);
	}

}

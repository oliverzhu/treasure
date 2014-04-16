package com.custom.music;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.custom.music.util.DrmUtils;
import com.custom.music.util.Log;
/**
 * M: AudioPreviewStarter is an Activity which is used to check the DRM file
 * and decide launch the AudioPreview or not.
 */
public class AudioPreviewStarter extends Activity
        implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    
    private static final String TAG = "AudioPreStarter";
    private Intent mIntent;
    /// M: Use member variable to show toast to avoid show the toast on screen for a long time if user click many time.
    private Toast mToast;
    
    private String[] mCursorCols;
    /**
        * M: onCreate to check the DRM file
        * and decide launch the AudioPreview or show DRM dialog.
        *
        * @param icicle If the activity is being re-initialized after
        *     previously being shut down then this Bundle contains the data it most
        *     recently supplied in 
        */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.v(TAG, ">>> onCreate",Log.APP);
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }

        Log.v(TAG, "uri=" + uri,Log.APP);
        mIntent = new Intent(getIntent());
        mIntent.setClass(this, AudioPreview.class);
        if (!DrmUtils.isSupportDrm()) {
            Log.v(TAG, "DRM is off",Log.APP);
            startActivity(mIntent);
            finish();
            return;
        }
        
        initCursorColumns();
        processForDrm(uri);
        Log.v(TAG, "onCreate >>>",Log.APP);
    }

    /**
     * M: handle the DRM dialog click event.
     * 
     * @param dialog
     *            The dialog that was dismissed will be passed into the method.
     * @param which
     *            The button that was clicked.
     */
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            /// M: continue to play
            Log.v(TAG, "onClick: BUTTON_POSITIVE",Log.APP);
            startActivity(mIntent);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
            /// M: do nothing but finish itself
            Log.v(TAG, "onClick: BUTTON_NEGATIVE",Log.APP);
        } else {
            Log.w(TAG, "undefined button on DRM consume dialog!",Log.APP);
        }
    }

    /**
     * M: finish itself when dialog dismiss.
     * 
     * @param dialog
     *            The dialog that was dismissed will be passed into the method.
     */
    public void onDismiss(DialogInterface dialog) {
        Log.v(TAG, "onDismiss",Log.APP);
        finish();
    }

    /**
     * M: the method is to do DRM process by uri.
     * 
     * @param uri
     *            the uri of the playing file
     */
    private void processForDrm(Uri uri) {
        final String schemeContent = "content";
        final String schemeFile = "file";
        final String hostMedia = "media";
        final String drmFileSuffix = ".dcf";
        final int isDrmIndex = 1;
        final int drmMethonIndex = 2;
        String scheme = uri.getScheme();
        String host = uri.getHost();
        Log.v(TAG, "scheme=" + scheme + ", host=" + host,Log.APP);
        /// M: to resolve the bug when modify suffix of drm file
        /// ALPS00677354 @{ 
        ContentResolver resolver = getContentResolver();
        Cursor c = null;
        if (schemeContent.equals(scheme) && hostMedia.equals(host)) {
            /// M: query DB for drm info
            c = resolver.query(uri, mCursorCols, null, null, null);
        } else if (schemeFile.equals(scheme)) {
            /// M: a file opened from FileManager/ other app
            String path = uri.getPath();
            path = path.replaceAll("'", "''");
            Log.v(TAG, "file path=" + path,Log.APP);
            if (path == null) {
                finish();
                return;
            }
            Uri contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            StringBuilder where = new StringBuilder();
            where.append(MediaStore.Audio.Media.DATA + "='" + path + "'");
            c = resolver.query(contentUri, mCursorCols, where.toString(), null, null);
        }
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    /// M: cursor is valid
                    int isDrm = c.getInt(c.getColumnIndexOrThrow("is_drm"));
                    Log.v(TAG, "isDrm=" + isDrm,Log.APP);
                    if (isDrm == 1) {
                        /// M: is a DRM file
                        checkDrmRightStatus(uri, c.getInt(drmMethonIndex));
                        return;
                    }
                }
            } finally {
                c.close();
            }
        }
        /// @}
        startActivity(mIntent);
        finish();
    }

    /**
     * M: the method is to check the drm right of the playing file.
     * 
     * @param uri
     *            the uri of the playing file
     * @param drmMethod
     *            the drm method of the playing file, it will retrive by drm client if the value is -1
     */
    private void checkDrmRightStatus(Uri uri, int drmMethod) {
        int rightsStatus = -1;
        int method = drmMethod;
        /// M: when modify the suffix of drm file ,drmMedthod in db is -1 in JB edtion
        if (method == -1) {
            showToast(getString(R.string.playback_failed));
            finish();
            return;
        }
        Log.v(TAG, "drmMethod=" + method,Log.APP);

        try {
            rightsStatus = DrmUtils.checkRightsStatusForTap(this, uri);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "checkRightsStatusForTap throw IllegalArgumentException " + e,Log.APP);
        }
        Log.v(TAG, "checkDrmRightStatus: rightsStatus=" + rightsStatus,Log.APP);
        switch (rightsStatus) {
            case DrmUtils.RIGHTSSTATUS_RIGHTS_VALID:
                if (method == DrmUtils.DRMMETHOD_METHOD_FL) {
                    /// M: FL does not have constraints
                    startActivity(mIntent);
                    finish();
                    return;
                }
                DrmUtils.showConsumeDialog(this);
                break;
            case DrmUtils.RIGHTSSTATUS_RIGHTS_INVALID:
                if (method == DrmUtils.DRMMETHOD_METHOD_FL) {
                    /// M: FL does not have constraints
                    showToast(getString(R.string.fl_invalid));
                    finish();
                    return;
                }          
                DrmUtils.showRefreshLicenseDialog(this,uri);
//                if (method == OmaDrmStore.DrmMethod.METHOD_CD) {
//                    finish();
//                }
                break;
            case DrmUtils.RIGHTSSTATUS_SECURE_TIMER_INVALID:
            	DrmUtils.showSecureTimerInvalidDialog(this);
                break;
            default:
                break;
        }
    }
    /**
     * M: Show the given text to screen.
     * 
     * @param toastText Need show text.
     */
    private void showToast(CharSequence toastText) {
        if (mToast == null) {
            mToast = Toast.makeText(getApplicationContext(), toastText, Toast.LENGTH_SHORT);
        }
        mToast.setText(toastText);
        mToast.show();
    }
    
    private void initCursorColumns()
    {
    	ArrayList<String> mCursorColsList = new ArrayList<String>();
        mCursorColsList.add(MediaStore.Audio.Media._ID);
        
        if(DrmUtils.isSupportDrm())
        {
        	mCursorColsList.add("is_drm");
        	mCursorColsList.add("drm_method");
        }
        mCursorCols = (String[])mCursorColsList.toArray(new String[1]);
    }
}

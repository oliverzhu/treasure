package com.ape.onelogin.util;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.ape.onelogin.login.core.Constants;

/**
 * 上下文操作，如创建对话框，进度条,版本信息...
 * 
 * @author jianwen.zhu
 * @since 2013-12-6
 */
public class ContextUtils {
    private static final String TAG = "";
    
    /**
     * Show a Toast(Toast.LENGTH_SHORT).
     * 
     * @param text the content shown on the Toast.
     */
    public static void showToast(Context context,String text,int length) {
        Toast.makeText(context, text, length).show();
    }
    
    /**
     * Show a Toast(Toast.LENGTH_SHORT).
     * 
     * @param text the content shown on the Toast.
     */
    public static void showToast(Context context,int resId,int length) {
        Toast.makeText(context, resId, length).show();
    }
    
    /**
     * show progress dialog
     * 
     * @param context
     * @param title
     *            Dialog title
     * @param message
     *            Dialog message
     * @return
     */
    public static ProgressDialog showProgressDialog(Context context, int title,
            int message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setTitle(title);
        dialog.setMessage(context.getResources().getString(message));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
        return dialog;
    }
    
    public static ProgressDialog createProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        return dialog;
    }
    
    /**
     * close progress dialog
     * 
     * @param progressDialog
     */
    public static void closeProgressDialog(Dialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.cancel();
        }
    }
    
    /**
     * 
     * @param context
     * @param title
     * @param message
     * @param buttonTexts
     * @param listeners
     * @return
     */
    public static AlertDialog showAlertDialog(Context context, int title,
            int message, int[] buttonTexts, OnClickListener[] listeners) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        if (buttonTexts.length == 1) {
            dialog.setNeutralButton(buttonTexts[0], listeners[0]);
        } else if (buttonTexts.length == 2) {
            dialog.setPositiveButton(buttonTexts[0], listeners[0]);
            dialog.setNegativeButton(buttonTexts[1], listeners[1]);
        } else if (buttonTexts.length == 3) {
            dialog.setPositiveButton(buttonTexts[0], listeners[0]);
            dialog.setNeutralButton(buttonTexts[1], listeners[1]);
            dialog.setNegativeButton(buttonTexts[2], listeners[2]);
        }
        return dialog.create();
    }
    
    /**
     * 获取版本号
     * 
     * @param context
     * @return
     */
    public static float getVersionCode(Context context) {
        float versionCode = -1;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return versionCode;
    }

    /**
     * 获取版本信息
     * @param mContext
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            versionName = info.versionName;
            // int versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        return versionName;
    }
    
    public static String getUserAgent(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            throw new IllegalStateException("getPackageInfo failed");
        }
        return String.format("%s/%s; %s/%s/%s/%s; %s/%s/%s",
                packageInfo.packageName,
                packageInfo.versionName,
                Build.BRAND,
                Build.DEVICE,
                Build.MODEL,
                Build.ID,
                Build.VERSION.SDK_INT,
                Build.VERSION.RELEASE,
                Build.VERSION.INCREMENTAL);
    }
    
    public static String getImei(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
        if(imei == null){
            imei = Constants.PASSWORD_MASK;
        }
        return imei;
    }
    
    /**
     * @param activity
     */
    public static void showSoftKeyboard(Activity activity, View view) {
        ((InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
    }

    /**
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        if (activity == null) {
            return;
        }
        try {
            View focus = activity.getCurrentFocus();
            if (focus == null) {
                return;
            }
            InputMethodManager imm = ((InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE));
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 根据大小缩放图片,并旋转角度
     * 
     * @param in
     * @param targetWidth
     * @param targetHeight
     * @return
     */
    public static Bitmap scaleBitmap(String filePath, int targetWidth, int targetHeight,int angle) {
        // get sampleBitmap
        Options opts = new Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, opts);

        int scaleWidth = opts.outWidth / targetWidth;
        int scaleHeight = opts.outHeight / targetHeight;
        int scale = scaleWidth < scaleHeight ? scaleWidth : scaleHeight;
        if (scale < 1) {
            scale = 1;
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap sampleBitmap = BitmapFactory.decodeFile(filePath, opts);

        if (opts.outWidth == -1 || opts.outHeight == -1) {
            return null;
        }
        // get scalebitmap
        float fScaleWidth = targetWidth / ((float) opts.outWidth);
        float fScaleHeight = targetHeight / ((float) opts.outHeight);
        float fScale = fScaleWidth > fScaleHeight ? fScaleWidth : fScaleHeight;
        if (fScale > 1)
            fScale = 1;
        Matrix matrix = new Matrix();
        if(angle != 0)
        {
            matrix.setRotate(angle);
        }
        matrix.postScale(fScale, fScale);
        Bitmap scaleBitmap = Bitmap.createBitmap(sampleBitmap, 0, 0, opts.outWidth, opts.outHeight, matrix, true);

        // get targetBitmap
        int bitmapX = (scaleBitmap.getWidth() - targetWidth) / 2;
        bitmapX = bitmapX > 0 ? bitmapX : 0;
        int bitmapY = (scaleBitmap.getHeight() - targetHeight) / 2;
        bitmapY = bitmapY > 0 ? bitmapY : 0;
        targetWidth = targetWidth < (scaleBitmap.getWidth()) ? targetWidth : (scaleBitmap.getWidth());
        targetHeight = targetHeight < (scaleBitmap.getHeight()) ? targetHeight : (scaleBitmap.getHeight());
        Bitmap targetBitmap = Bitmap.createBitmap(scaleBitmap, bitmapX, bitmapY, targetWidth, targetHeight);

        // scaleBitmap.recycle();
        // sampleBitmap.recycle();
        return targetBitmap;
    }
    
    public static Intent getActivityIntent(Context context,String packageName,String className) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent();
        ComponentName compoentName = 
                new ComponentName(packageName,className);
        intent.setComponent(compoentName);
        ResolveInfo ri = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if(ri != null) {
            return intent;
        }
        return null;
    }
    
//    public static String getAvatarPath(Context context, String avatar) {
//        return context.getExternalCacheDir() + Constants.AVATAR_LOCAL_DIR + File.separator + avatar;
//    }
}

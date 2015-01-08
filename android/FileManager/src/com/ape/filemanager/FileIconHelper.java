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


import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.View;
import android.widget.ImageView;

import com.ape.filemanager.FileCategoryHelper.FileCategory;
import com.ape.filemanager.FileIconLoader.IconLoadFinishListener;

public class FileIconHelper implements IconLoadFinishListener {

    private static final String LOG_TAG = "FileIconHelper";

    private static HashMap<ImageView, ImageView> imageFrames = new HashMap<ImageView, ImageView>();

    private static HashMap<String, Integer> fileExtToIcons = new HashMap<String, Integer>();

    private FileIconLoader mIconLoader;
    private Context mContext;

    static {
        addItem(new String[] {
            "mp3", "mpga", "wav", "imy", "aac", "ape", "flac"
        }, R.drawable.file_icon_mp3);
//        addItem(new String[] {
//            "wma"
//        }, R.drawable.file_icon_wma);
        addItem(new String[] {
            "mid", "midi", "amr", "awb", "ogg", "m4a", "3gpp"
        }, R.drawable.file_icon_mid);
        addItem(new String[] {
                "mp4", "avi", /*"wmv", */"mpeg", "mov", "m4v", "3gp", "3g2", "3gpp2", /*"asf", */"flv", "f4v", "fla"
        }, R.drawable.file_icon_video);
        addItem(new String[] {
                "jpg", "jpeg", "gif", "png", "bmp", "wbmp"
        }, R.drawable.file_icon_picture);
        addItem(new String[] {
                "txt", "log", "xml", "ini", "lrc", "vcf", "vcs"
        }, R.drawable.file_icon_txt);
//        addItem(new String[] {
//                "doc", "ppt", "docx", "pptx", "xsl", "xslx",
//        }, R.drawable.file_icon_office);
//        addItem(new String[] {
//            "pdf"
//        }, R.drawable.file_icon_pdf);
        addItem(new String[] {
            "zip"
        }, R.drawable.file_icon_zip);
        addItem(new String[] {
            "mtz"
        }, R.drawable.file_icon_theme);
        addItem(new String[] {
            "rar"
        }, R.drawable.file_icon_rar);
        addItem(new String[] {
                "apk"
            }, R.drawable.file_icon_apk);
    }
    
    public static boolean isSupportedByCurrentSystem(Context context, String mimeType) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setType(mimeType);
        ResolveInfo info = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return info != null;
    }

    public FileIconHelper(Context context) {
        mContext = context;
        mIconLoader = new FileIconLoader(context, this);
    }

    private static void addItem(String[] exts, int resId) {
        if (exts != null) {
            for (String ext : exts) {
                fileExtToIcons.put(ext.toLowerCase(), resId);
            }
        }
    }

    public static int getFileIcon(String ext) {
        Integer i = fileExtToIcons.get(ext.toLowerCase());
        if (i != null) {
            return i.intValue();
        } else {
            //return R.drawable.file_icon_default;
            return FileCustomExtension.getIconIdFromExt(ext);
        }

    }
    
    public void cancelLoadFileIcon(FileInfo fileInfo, ImageView fileImage) {
        mIconLoader.cancelRequest(fileImage);
    }

    public void setIcon(FileInfo fileInfo, ImageView fileImage, ImageView fileImageFrame) {
        String filePath = fileInfo.filePath;
        long fileId = fileInfo.dbId;
        String extFromFilename = Util.getExtFromFilename(filePath);
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(filePath);
        fileImageFrame.setVisibility(View.GONE);
        boolean set = false;
        
        if (extFromFilename.toLowerCase().equals("3gp")
                || extFromFilename.toLowerCase().equals("3gpp")) {
            String mime = IntentBuilder.loadMimetypeFromDB(filePath, mContext);
            if (mime != null) {
                if (mime.startsWith("audio/")) {
                    fileImage.setImageResource(R.drawable.file_icon_mid);
                    return;
                }
                if (mime.startsWith("video/")) {
                    fileImage.setImageResource(R.drawable.file_icon_video);
                    mIconLoader.loadIcon(fileImage, filePath, fileId, FileCategory.Video);
                    fileImageFrame.setVisibility(View.GONE);
                    return;
                }
            }
        }

        int id = getFileIcon(extFromFilename);
        fileImage.setImageResource(id);

        mIconLoader.cancelRequest(fileImage);
        switch (fc) {
            case Apk:
                mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                set = true;
                break;
            case Picture:
            case Video:
                set = mIconLoader.loadIcon(fileImage, filePath, fileId, fc);
                fileImageFrame.setVisibility(View.GONE);
                if (set){
                    //fileImageFrame.setVisibility(View.VISIBLE);
                }
                else {
                    fileImage.setImageResource(fc == FileCategory.Picture ? R.drawable.file_icon_picture
                            : R.drawable.file_icon_video);
                    imageFrames.put(fileImage, null);
                    set = true;
                }
                break;
            default:
                set = true;
                break;
        }

        if (!set)
            fileImage.setImageResource(R.drawable.file_icon_default);
    }

    @Override
    public void onIconLoadFinished(ImageView view) {
        ImageView frame = imageFrames.get(view);
        if (frame != null) {
            frame.setVisibility(View.VISIBLE);
            imageFrames.remove(view);
        }
    }

}

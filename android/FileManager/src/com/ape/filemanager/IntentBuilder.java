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
import java.util.ArrayList;

import com.ape.filemanager.R;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

public class IntentBuilder {

    public static void viewFile(final Context context, final String filePath, boolean needUnknowSelect) {
        String type = getMimeType(filePath);

        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            if (type.equals("video/3gpp") || type.equals("audio/3gpp")) {
                String type2 = loadMimetypeFromDB(filePath, context);
                if (!TextUtils.isEmpty(type2)) {
                    type = type2;
                }
            }
            /* 设置intent的file与MimeType */
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (type.equals("application/vnd.android.package-archive"))
            {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            try
            {
                context.startActivity(intent);
            } catch (Exception e)
            {
                AlertDialog dialog = new AlertDialog.Builder(context)
                .setMessage(R.string.msg_unable_open_file)
                .setPositiveButton(R.string.confirm, null).create();
                dialog.show();
            }
        } else if (needUnknowSelect) {
            // unknown MimeType
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.dialog_select_type);

            CharSequence[] menuItemArray = new CharSequence[] {
                    context.getString(R.string.dialog_type_text),
                    context.getString(R.string.dialog_type_audio),
                    context.getString(R.string.dialog_type_video),
                    context.getString(R.string.dialog_type_image) };
            dialogBuilder.setItems(menuItemArray,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selectType = "*/*";
                            switch (which) {
                            case 0:
                                selectType = "text/plain";
                                break;
                            case 1:
                                selectType = "audio/*";
                                break;
                            case 2:
                                selectType = "video/*";
                                break;
                            case 3:
                                selectType = "image/*";
                                break;
                            }
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
                            try {
                                context.startActivity(intent);
                            } catch (Exception e) {
                                AlertDialog dialog2 = new AlertDialog.Builder(context)
                                .setMessage(R.string.msg_unable_open_file)
                                .setPositiveButton(R.string.confirm, null).create();
                                dialog2.show();
                            }
                        }
                    });
            dialogBuilder.show();
        } else {
            Toast.makeText(context, R.string.msg_unable_open_file, Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent buildSendFile(ArrayList<FileInfo> files) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String mimeType = "*/*";
        for (FileInfo file : files) {
            if (file.IsDir)
                continue;

            File fileIn = new File(file.filePath);
            mimeType = getMimeType(file.fileName);
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0)
            return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? android.content.Intent.ACTION_SEND_MULTIPLE
                : android.content.Intent.ACTION_SEND);

        if (multiple) {
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
            if (mimeType.startsWith("application") && !mimeType.equals("application/ogg"))
            {
                mimeType = "application/zip";
            }
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }
        MyLog.i("buildSendFile, mimeType:" + mimeType + ", multiple:" + multiple);

        return intent;
    }

    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        if (dotPosition == -1)
            return "*/*";

        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }

        return mimeType != null ? mimeType : "*/*";
    }
    
    /**
     * some file could be video type or audio type. The method try to find out its real MIME type
     * from database of MediaStore.
     * 
     * @param filePath path of a file
     * @return the file's real MIME type
     */
    public static String loadMimetypeFromDB(String filePath, Context context) {
        String mimeType = null;
        ContentResolver resolver = context.getContentResolver();
        if (resolver != null && filePath != null) {
            final Uri uri = MediaStore.Files.getContentUri("external");
            final String[] projection = new String[] { MediaStore.MediaColumns.MIME_TYPE };
            final String selection = MediaStore.MediaColumns.DATA + "=?";
            final String[] selectionArgs = new String[] { filePath };
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, projection, selection,
                        selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                     mimeType = cursor.getString(0);
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return mimeType;
    }
}

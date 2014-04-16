/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.custom.music;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.Toast;

import com.custom.music.util.Log;
import com.custom.music.util.MusicUtils;
import com.custom.music.view.MusicDialog;

public class DeleteItems extends Activity
{
    /// M: add for debug log
    private static final String TAG = "DeleteItems";
    /// M: add dialog key @{
    private static final int PROGRESS_DIALOG_KEY = 0;
    private static final int ALERT_DIALOG_KEY = 1;
    /// @}
    /// M: Status of deleting @{
    private static final int START_DELETING = 0;
    private static final int FINISH = 1;
    /// @}
    private long [] mItemList;

    /// M: Description show in dialog
    private String mDialogDesc = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Bundle b = getIntent().getExtras();

        /// M: Get delete description by res id to show different language when switch language
        mDialogDesc = String.format(getString(b.getInt(MusicUtils.DELETE_DESC_STRING_ID)),
                b.getString(MusicUtils.DELETE_DESC_TRACK_INFO));
        mItemList = b.getLongArray("items");

        /// M: register broadcast for the status change of SD Card @{
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_EJECT);
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
        /// @}

        /// M: Only show dialog when activity first onCreate(), because if activity is restarted
        /// (Such as change language cause activity restart), system will restore the dialog. {@
        if (icicle == null) {
            showDialog(ALERT_DIALOG_KEY);
        }
        /// @}
    }
    
    private DialogInterface.OnClickListener mButtonClicked = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogInterface, int button) {
            if (button == DialogInterface.BUTTON_POSITIVE) {
                /// M: show dialog and start to delete
                showDialog(PROGRESS_DIALOG_KEY);
                Message msg = mHandler.obtainMessage(START_DELETING);
                mHandler.sendMessage(msg);
            } else if (button == DialogInterface.BUTTON_NEUTRAL) {
                finish();
            }
        }
    };

    /**
     * M: use handler to start and finish the operation.
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == START_DELETING) {
                /// M: Do the time-consuming job in its own thread to avoid blocking anyone
                new Thread(new Runnable() {
                    public void run() {
                        doDeleteItems();
                    }
                }).start();
            } else if (msg.what == FINISH) {
                /// M: Notify user the deletion failed or the success deleted track number
                int deleteNum = msg.arg1;
                String message = null;
                if (deleteNum == 0) {
                    /// Delete fail
                    message = getString(R.string.delete_track_failed);
                } else {
                    /// Delete success
                    message = getResources().getQuantityString(R.plurals.NNNtracksdeleted,
                            deleteNum, Integer.valueOf(deleteNum));
                }
                Toast.makeText(DeleteItems.this, message, Toast.LENGTH_SHORT).show();

                finish();
            }
        }
    };

    /**
     * M: to create dialogs for deleting and alert
     * 
     * @param id The create delete dialog id
     * @return Return a new delete dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG_KEY:
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle(R.string.delete_progress_title);
                progressDialog.setMessage(getResources().getString(
                        R.string.delete_progress_message));
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    /// M: Don't respond quick search request when progress dialog is showing.
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (KeyEvent.KEYCODE_SEARCH == keyCode) {
                            Log.i(TAG, "OnKeyListener",Log.APP);
                            return true;
                        }
                        return false;
                    }
                });
                return progressDialog;
                
            case ALERT_DIALOG_KEY:
                MusicDialog musicDialog = new MusicDialog(this,mButtonClicked,null);
                musicDialog.setTitle(R.string.delete_item);
                musicDialog.setPositiveButton(getResources().getString(
                        R.string.delete_confirm_button_text));
                musicDialog.setNeutralButton(getResources().getString(R.string.cancel));
                musicDialog.setMessage(mDialogDesc);
                musicDialog.setCanceledOnTouchOutside(true);
                musicDialog.setCancelable(true);
                musicDialog.setSearchKeyListener();
                musicDialog.setIcon(android.R.drawable.ic_dialog_alert);
                return musicDialog;
                
            default:
                Log.e(TAG, "onCreateDialog with undefined id!",Log.APP);
                return null;
        }
    }

    /**
     * M:  Do the deletion and Tell them we are done
     */
    private void doDeleteItems() {
        int deleteNum = MusicUtils.deleteTracks(DeleteItems.this, mItemList);
        Message msg = mHandler.obtainMessage(FINISH, deleteNum, -1);
        mHandler.sendMessage(msg);
    }

    /**
     * M: Enable status bar expand in onPause.
     */
    @Override
    public void onPause() {
        /// M: Enable status bar expand when activity has been in background or been finished. @{
        /*StatusBarManager statusBar = (StatusBarManager) getSystemService(
                Context.STATUS_BAR_SERVICE);
        statusBar.disable(StatusBarManager.DISABLE_NONE);*/
        /// @}
        super.onPause();
    }

    /**
     * M: Disable status bar expand in onResume.
     */
    @Override
    public void onResume() {
        super.onResume();
        /// M: Disable status bar expand, so when Delete activity in foreground, user can not expand
        /// status bar to interrupt delete tracks. @{
        /*StatusBarManager statusBar = (StatusBarManager) getSystemService(
                Context.STATUS_BAR_SERVICE);
        statusBar.disable(StatusBarManager.DISABLE_EXPAND);*/
        /// @}
    }

    /**
     * M: Unregister a receiver when activity destroy
     */
    @Override
    public void onDestroy() {
        unregisterReceiver(mScanListener);
        super.onDestroy();
    }

    /**
     * M: Finish delete activity when sdcard has been unmounted.
     */
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /// M: When SD card is unmounted, finish the delete activity
            finish();
            Log.i(TAG, "SD card is ejected, finish delete activity!",Log.APP);
        }
    };
}

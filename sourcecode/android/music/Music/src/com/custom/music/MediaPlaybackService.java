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
 * Copyright (C) 2007 The Android Open Source Project
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

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.custom.music.bluetooth.avcrp.IBTAvrcpMusic;
import com.custom.music.bluetooth.avcrp.ServiceAvrcpStub;
import com.custom.music.util.DrmUtils;
import com.custom.music.util.Log;
import com.custom.music.util.MusicUtils;
import com.custom.music.util.SharedPreferencesCompat;
/// M: BT AVRCP Start {@
/// BT AVRCP End @}

/**
 * Provides "background" audio playback capabilities, allowing the
 * user to switch between activities without stopping playback.
 */
public class MediaPlaybackService extends Service {
    /** used to specify whether enqueue() should start playing
     * the new list of files right away, next or once all the currently
     * queued files have been played
     */
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int PLAYBACKSERVICE_STATUS = 1;
    
    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;
    
    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;

    public static final String PLAYSTATE_CHANGED = "com.custom.music.playstatechanged";
    public static final String META_CHANGED = "com.custom.music.metachanged";
    public static final String QUEUE_CHANGED = "com.custom.music.queuechanged";

    public static final String SERVICECMD = "com.custom.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";

    public static final String TOGGLEPAUSE_ACTION = "com.custom.music.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.custom.music.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.custom.music.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.custom.music.musicservicecommand.next";
    
    private static final String ACTION_SHUTDOWN_IPO = "android.intent.action.ACTION_SHUTDOWN_IPO";
    /// M: BT AVRCP, AVRCP14
    public static final String PLAYBACK_COMPLETE = "com.custom.music.playbackcomplete";
    public static final String QUIT_PLAYBACK = "com.custom.music.quitplayback";

    /// M: AVRCP and Android Music AP supports the FF/REWIND
    public static final String CMDFORWARD = "forward";
    public static final String CMDREWIND = "rewind";
    public static final String DELTATIME = "deltatime";

    /// M: open reverb for MusicFX
    public static final String ATTACH_AUX_AUDIO_EFFECT = "com.custom.music.attachauxaudioeffect";
    public static final String DETACH_AUX_AUDIO_EFFECT = "com.custom.music.detachauxaudioeffect";
    private static final String AUX_AUDIO_EFFECT_ID = "auxaudioeffectid";
    /// M: Add for handle media player error when attachAuxEffect an effect id is not exist because MusicFX
    /// M: has died and these effect source already have been released
    public static final int MEDIA_ERROR_MUSICFX_DIED = 0x800000FF;

    private static final int TRACK_ENDED = 1;
    /// M: Mark for bug fixed
    //private static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FOCUSCHANGE = 4;
    private static final int FADEDOWN = 5;
    private static final int FADEUP = 6;
    private static final int TRACK_WENT_TO_NEXT = 7;
    private static final int MAX_HISTORY_SIZE = 100;
    /// M: add for open media fail message, next to TRACK_WENT_TO_NEXT
    private static final int OPEN_FAILED = 8;
    /// M: When open media failed, we will re try for OPEN_FAILED_MAX_COUNT
    private static final int OPEN_FAILED_MAX_COUNT = 2;
    /// M: BT AVRCP, AVRCP14, change setting mode for repeat and shuffle.
    private static final int CHANGE_SETTING_MODE = 0x65;
    private static final int SETTING_MODE_SHUFFLE = 0x02;
    private static final int SETTING_MODE_REPEAT = 0x03;
    /// M: AVRCP and Android Music AP supports the FF/REWIND
    /// M: means 16X  maybe 10X 32X
    private static final int SPEED_NORMAL = 16;
    /// M: means 40X 
    private static final int SPEED_FAST = 40;
    private static final int POSITION_FOR_SPEED_FAST = 5000;
    /// M: modify log tag
    private static final String TAG = "MediaPlaybackService";

    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mMediaMountedCount = 0;
    private long [] mAutoShuffleList = null;
    private long [] mPlayList = null;
    private int mPlayListLen = 0;
    private Vector<Integer> mHistory = new Vector<Integer>(MAX_HISTORY_SIZE);
    private Cursor mCursor;
    private int mPlayPos = -1;
    private int mNextPlayPos = -1;
    private final Shuffler mRand = new Shuffler();
    private int mOpenFailedCounter = 0;
    //update by zjw
    String[] mCursorCols;
    private final static int IDCOLIDX = 0;
    private final static int PODCASTCOLIDX = 8;
    private final static int BOOKMARKCOLIDX = 9;
    private BroadcastReceiver mUnmountReceiver = null;
    private WakeLock mWakeLock;
    private int mServiceStartId = -1;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mQuietMode = false;
    private AudioManager mAudioManager;
    private boolean mQueueIsSaveable = true;
    // used to track what type of audio focus loss caused the playback to pause
    private boolean mPausedByTransientLossOfFocus = false;

    private SharedPreferences mPreferences;
    
    private MediaAppWidgetProviderNew mAppWidgetProvider = MediaAppWidgetProviderNew.getInstance();
    
    // interval after which we stop the service when idle
    private static final int IDLE_DELAY = 60000;

    private RemoteControlClient mRemoteControlClient;

    /// M: mediatek add variable @{
    private boolean mIsPlayerReady = false;
    private boolean mDoSeekWhenPrepared = false;
    private boolean mMediaSeekable = true;
    private boolean mNextMediaSeekable = true;
    private boolean mIsPlaylistCompleted = false;
    private boolean mReceiverUnregistered = false;
    private boolean mIsPrev = false;
    /// M:mark for loading from SDCard
    private boolean mIsReloadSuccess = false;
    /// M: For DRM completion detection
    static boolean mTrackCompleted = false;
    
    private long mPreAudioId = -1;
    private long mDurationOverride = -1;
    /// M:  AVRCP and Android Music AP supports the FF/REWIND
    private long mSeekPositionForAnotherSong = 0;
    /// M: Auxiliary audio effect id to attach MediaPlayer on
    private int mAuxEffectId = 0;
    /// M: If receive broadcast to attach reverb effect when music pause, we need delay to attach it when start play again.
    private boolean mWhetherAttachWhenPause = false;
    private StorageManager mStorageManager = null;
    /// M: async album art to get current song's album and replace it on notification
    private AlbumArtWorker mAsyncAlbumArtWorker = null;
    /// M: For album art backup for notification when skin changed
    private Bitmap mAlbumArt = null;

    /// M: Eject card path for multi-SD card support
    private String mEjectingCardPath = null;

    /// M: store current player volume, so that when install next player we can set volume with it
    private float mCurrentVolume = 1.0f;

    /// M: Use member variable to show toast to avoid show the toast on screen for a long time if user click many time.
    private Toast mToast;
    

    /// M: add for notify query at the end of 100ms when notify happen
    private static final int REMOVE_TRACK = 10;

    // M: let onChange() of ContentObserver run in the mContentObserverThreadHandler of mContentObserverThread,not it the main thread
    HandlerThread mContentObserverThread = null;
    private Handler mContentObserverThreadHandler = null;
    
    //add by zjw
    public static final String PLAY_MODE = "com.android.music.playmode";
    
    private Handler mMediaplayerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MusicUtils.debugLog("mMediaplayerHandler.handleMessage " + msg.what);
            switch (msg.what) {
                case FADEDOWN:
                	Log.i(TAG, "<mMediaplayerHandler> FADEDOWN",Log.APP);
                    mCurrentVolume -= .05f;
                    if (mCurrentVolume > .2f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case FADEUP:
                	Log.i(TAG, "<mMediaplayerHandler> FADEUP",Log.APP);
                    /// M: change volume step from 0.01 to 0.05
                    mCurrentVolume += .05f;
                    if (mCurrentVolume < 1.0f) {
                        /// M: speed up step from 10 to 50
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 50);
                    } else {
                        mCurrentVolume = 1.0f;
                    }
                    mPlayer.setVolume(mCurrentVolume);
                    break;
                case SERVER_DIED:
                    Log.i(TAG, "<mMediaplayerHandler> SERVER_DIED: mPlayPos = " + mPlayPos,Log.APP);
                    /// M: notify musicfx when service died
                    sendSessionIdToAudioEffect(false);
                    if (mIsSupposedToBePlaying) {
                        gotoNext(true);
                    /// M: Only when the position is valid we need to reopen the same song, because when open failed
                    /// we will clear the play list and position.
                    } else if (mPlayPos >= 0) {
                        // the server died when we were idle, so just
                        // reopen the same song (it will start again
                        // from the beginning though when the user
                        // restarts)
                        Log.i(TAG, "SERVER_DIED: -> openCurrentAndNext",Log.APP);
                        /// M: backup and restored seek status when reopen the current song @{
                        boolean doSeek = mDoSeekWhenPrepared;
                        mQuietMode = true;
                        openCurrentAndNext();
                        mDoSeekWhenPrepared = doSeek;
                        /// @}
                        Log.i(TAG, "SERVER_DIED: doseek restored to:" + mDoSeekWhenPrepared,Log.APP);
                        Log.i(TAG, "SERVER_DIED: <- openCurrentAndNext",Log.APP);
                    }
                    break;
                case TRACK_WENT_TO_NEXT:
                	Log.i(TAG, "<mMediaplayerHandler> TRACK_WENT_TO_NEXT",Log.APP);
                    mPlayPos = msg.arg1;
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                    if (mPlayListLen > mPlayPos) {
                        mCursor = getCursorForId(mPlayList[mPlayPos]);
                        Log.i(TAG, "switch to next song",Log.APP);
                        /// M: get seek information for the current song
                        mMediaSeekable = mNextMediaSeekable;
                        mNextMediaSeekable = true;
                        notifyChange(META_CHANGED);
                        updateNotification(MediaPlaybackService.this, null);
                        /// M: start AlbumArtWorker to get album art for notification @{
                        if (mPreAudioId != getAudioId()) {
                            mAsyncAlbumArtWorker = new AlbumArtWorker();
                            mAsyncAlbumArtWorker.execute(Long.valueOf(getAlbumId()));
                        }
                        /// @}
                        /// Add current playing track to history, so that it can not set current track to be next player
                        addPlayedTrackToHistory(false);
                        setNextTrack();
                    }
                    break;
                case TRACK_ENDED:
                	Log.i(TAG, "<mMediaplayerHandler> TRACK_ENDED",Log.APP);
                    if (mRepeatMode == REPEAT_CURRENT) {
                        /// M: reset the track complete status
                        mTrackCompleted = false;
                        seek(0);
                        /// M: check the playing status before play, call start instead play to avoid request
                        /// focus again, because call play will request new focus and fade out, then it will stop
                        /// notification.
                        if (isPlaying()) {
                            //play();
                            mPlayer.start();
                        }
                    } else {
                        gotoNext(false);
                    }
                    /// M: AVRCP14
                    notifyChange(PLAYBACK_COMPLETE); 
                    break;
                /// M: Mark for bug fixed @{
                //case RELEASE_WAKELOCK:
                //    mWakeLock.release();
                //    break;                   
                /// @}
                case FOCUSCHANGE:
                	Log.i(TAG, "<mMediaplayerHandler> FOCUSCHANGE",Log.APP);
                    // This code is here so we can better synchronize it with the code that
                    // handles fade-in
                     switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                        	Log.i(TAG, "<mMediaplayerHandler.FOCUSCHANGE> AUDIOFOCUS_LOSS",Log.APP);
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }
                            pause();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        	Log.i(TAG, "<mMediaplayerHandler.FOCUSCHANGE> AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK",Log.APP);
                            mMediaplayerHandler.removeMessages(FADEUP);
                            mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        	Log.i(TAG, "<mMediaplayerHandler.FOCUSCHANGE> AUDIOFOCUS_LOSS_TRANSIENT",Log.APP);
                            if(isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }
                            pause();
                            /**
                             * M: Added to resolve the issue of 
                             * 1. LCA low memory kill,when we find the music is paused by TransientLossOfFocus,
                             *    we still let service startForeground.
                             * 2. Dismiss the notification while the music is paused by TransientLossOfFocus
                             * @{
                             */
                            if (mPausedByTransientLossOfFocus) {
                                Notification status = new Notification();
                                startForeground(PLAYBACKSERVICE_STATUS, status);
                            }
                            /**
                             * @}
                             */
                            break;
                        case AudioManager.AUDIOFOCUS_GAIN:
                        	Log.i(TAG, "<mMediaplayerHandler.FOCUSCHANGE> AUDIOFOCUS_GAIN",Log.APP);
                            if(!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mPlayer.setVolume(mCurrentVolume);
                                play(); // also queues a fade-in
                            } else {
                                mMediaplayerHandler.removeMessages(FADEDOWN);
                                mMediaplayerHandler.sendEmptyMessage(FADEUP);
                            }
                            break;
                        default:
                            Log.e(TAG, "Unknown audio focus change code",Log.APP);
                            break;
                    }
                    break;
                /// M: add open failed message handle @{
                case OPEN_FAILED:
                	Log.i(TAG, "<mMediaplayerHandler> OPEN_FAILED",Log.APP);
                    showToast(getString(R.string.playback_failed));
                    notifyChange(QUIT_PLAYBACK);
                    break;
                /// @}
                /// M: AVRCP to change setting mode(repeat and shuffle). @{
                case CHANGE_SETTING_MODE:
                	Log.i(TAG, "<mMediaplayerHandler> CHANGE_SETTING_MODE",Log.APP);
                    handleSettingModeChange(msg.arg1, msg.arg2) ;
                    break;
                /// @}
                /// M: avoid threads not sync,do query in main thread. @{
                case REMOVE_TRACK:
                    int songId = msg.arg1;
                    Log.i(TAG, "<REMOVE_TRACK> songId:" + songId,Log.APP);
                    removeTrack(songId);
                    break;
                  /// @}
                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /// M: check the receiver is alive @{
            if (mReceiverUnregistered) {
                return;
            }
            /// @}
            String action = intent.getAction();
            String cmd = intent.getStringExtra(CMDNAME);
            
            /// M: For IPO shutdown feature, and it firstly  @{
            if (Intent.ACTION_SHUTDOWN.equals(action) || ACTION_SHUTDOWN_IPO.equals(action)) {
            	Log.i(TAG, "<mIntentReceiver> ACTION_SHUTDOWN || ACTION_SHUTDOWN_IPO",Log.APP);
                /// M: When shutting down, saveQueue first then stop the player
                getApplicationContext().getContentResolver().unregisterContentObserver(
                        mContentObserver);
                /// M: we will receive ACTION_SHUTDOWN and ACTION_SHUTDOWN_IPO,
                // but we just need unregisterReceiver once @{
                if (mUnmountReceiver != null) {
                    unregisterReceiver(mUnmountReceiver);
                    mUnmountReceiver = null;
                }
                /// @}
                saveQueue(true);
                pause();
                /// when IPO,sdcard eject,the mediaserver continue visit sdcard ,will cause mediaserver died.
                // we should release datasource,if we don't,the fd of datasource will not close.ALPS00814513@{
                if (mPlayer.isInitialized()) {
                    mPlayer.setNextDataSource(null);
                }
                /// @}
                stop();
                /// M: when shut down the phone ,if shared sdcard option is
                // opened,and has external sdcard ,clear the queue @{
                /*boolean needClearQueue = MusicFeatureOption.IS_SUPPORT_SHARED_SDCARD
                        && MusicUtils.hasMountedExternalSDcard(getApplicationContext());
                clearQueue(needClearQueue);*/
                /// @}
                /// @}
            } else if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                /// M:  Don't start playing when there is no Sdcard available  @{
                boolean hasCard = MusicUtils.hasMountedSDcard(getApplicationContext());
                Log.i(TAG, "<mIntentReceiver> CMDNEXT || NEXT_ACTION hasCard:" + hasCard,Log.APP);
                if (hasCard) {
                    gotoNext(true);
                } else {
                    notifyChange(QUIT_PLAYBACK);
                } 
                /// @}
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
            	Log.i(TAG, "<mIntentReceiver> CMDPREVIOUS || PREVIOUS_ACTION",Log.APP);
                prev();
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
            	Log.i(TAG, "<mIntentReceiver> CMDTOGGLEPAUSE || TOGGLEPAUSE_ACTION",Log.APP);
                if (isPlaying()) {
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)
                    || AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            	Log.i(TAG, "<mIntentReceiver> CMDPAUSE || PAUSE_ACTION || ACTION_AUDIO_BECOMING_NOISY",Log.APP);
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDPLAY.equals(cmd)) {
            	Log.i(TAG, "<mIntentReceiver> CMDPLAY",Log.APP);
                play();
            } else if (CMDSTOP.equals(cmd)) {
            	Log.i(TAG, "<mIntentReceiver> CMDSTOP",Log.APP);
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            } else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
            	Log.i(TAG, "<mIntentReceiver> CMDAPPWIDGETUPDATE",Log.APP);
                // Someone asked us to refresh a set of specific widgets, probably
                // because they were just added.
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetProvider.performUpdate(MediaPlaybackService.this, appWidgetIds);
            /// M: attach and detach audio effect handle  @{
            } else if (ATTACH_AUX_AUDIO_EFFECT.equals(action)) {
            	Log.i(TAG, "<mIntentReceiver> ATTACH_AUX_AUDIO_EFFECT",Log.APP);
                /// M:  This means that user has selected an auxiliary audio effect, which requires
                /// a MediaPlayer instance to operate on
                mAuxEffectId = intent.getIntExtra(AUX_AUDIO_EFFECT_ID, 0); 
                Log.i(TAG, "ATTACH_AUX_AUDIO_EFFECT with EffectId = " + mAuxEffectId,Log.APP);
                if (mPlayer != null && mPlayer.isInitialized() && mAuxEffectId > 0) {
                    /// M: when play has been pause, we need not to attach revert to player, because when bluetooth
                    /// disconnect from phone, music will pause and the audio track in A2DP will invalid, if we attach
                    /// reverb effect to player at this time, mediaplayer will happen error 32. So we attach the reverb
                    /// when we began to play it again.
                    if (mPlayer.isPlaying()) {
                        mPlayer.attachAuxEffect(mAuxEffectId);
                        mPlayer.setAuxEffectSendLevel(1.0f);
                        mWhetherAttachWhenPause = false;
                    } else {
                        mWhetherAttachWhenPause = true;
                        Log.i(TAG, "Need attach reverb effect when play music again!",Log.APP);
                    }
                }
            } else if (DETACH_AUX_AUDIO_EFFECT.equals(action)) {
                /// M:  User has switched to other audio effect, so detach current auxiliary effect from MediaPlayer
                int auxEffectId = intent.getIntExtra(AUX_AUDIO_EFFECT_ID, 0);
                Log.i(TAG, "<mIntentReceiver> DETACH_AUX_AUDIO_EFFECT auxEffectId:" + auxEffectId,Log.APP);
                if (mAuxEffectId == auxEffectId) {
                    mAuxEffectId = 0;
                    if (mPlayer != null && mPlayer.isInitialized()) {
                        if (mPlayer.isPlaying()) {
                            mPlayer.attachAuxEffect(0);
                            mWhetherAttachWhenPause = false;
                        } else {
                            mWhetherAttachWhenPause = true;
                            Log.i(TAG, "Need detach reverb effect when play music again!",Log.APP);
                        }
                    }
                }
            }
            /// @}
        }
    };

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
        	Log.i(TAG, "<mAudioFocusListener.onAudioFocusChange>",Log.APP);
            mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    /// M: add content observer to monitor media file change  @{
    private final ContentObserver mContentObserver = new ContentObserver(
            mContentObserverThreadHandler) {
        @Override
        public void onChange(boolean selfChange) {
            Log.i(TAG, "<onChange>",Log.APP);
            super.onChange(selfChange);
            if (mPlayPos >= 0 && mPlayPos <= mPlayList.length - 1) {
                long id = mPlayList[mPlayPos];
                Cursor c = MusicUtils.query(MediaPlaybackService.this,
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new String[] {
                            "_id"
                        }, "_id=" + id, null, null);
                /// M: Only when cursor count is 0 we need to remove the current song from queue,
                /// because when cursor is null, it means database isn't ready.
                if (null == c) {
                    Log.i(TAG, "mContentObserver: cursor is null, db not ready!",Log.APP);
                } else {
                    Log.i(TAG, "mContentObserver: cursor count is " + c.getCount(),Log.APP);
                    if (0 == c.getCount()) {
                        Message removeTrackMsg = mMediaplayerHandler.obtainMessage(REMOVE_TRACK);
                        removeTrackMsg.arg1 = (int) id;
                        mMediaplayerHandler.sendMessage(removeTrackMsg);
                    }
                    c.close();
                    c = null;
                }
            }
        }
    };
    /// @}

    /// M: the receiver update the notification by skin change info  @{
    private final BroadcastReceiver mSkinChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isPlaying()) {
            	Log.i(TAG, "<mSkinChangedReceiver.onReceive> isPlaying():true",Log.APP);
                updateNotification(MediaPlaybackService.this, mAlbumArt);
            }
        }
    };
    /// @}

    public MediaPlaybackService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, ">>> onCreate",Log.APP);
        
        if(DrmUtils.isSupportDrm())
        {
        	mCursorCols = new String[] {
                    "audio._id AS _id",             // index must match IDCOLIDX below
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX below
                    MediaStore.Audio.Media.BOOKMARK,    // index must match BOOKMARKCOLIDX below
                    /// M: get media duraton from database
                    MediaStore.Audio.Media.DURATION,
                    /// M: drm support
                    "is_drm",
                    "drm_method",
            };
        }else
        {
        	mCursorCols = new String[] {
                    "audio._id AS _id",             // index must match IDCOLIDX below
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.MIME_TYPE,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ARTIST_ID,
                    MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX below
                    MediaStore.Audio.Media.BOOKMARK,    // index must match BOOKMARKCOLIDX below
                    /// M: get media duraton from database
                    MediaStore.Audio.Media.DURATION,
            };
        }
        /// M:  add for clear the notification when the service restart
        stopForeground(true);
        
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        ComponentName rec = new ComponentName(getPackageName(),
                MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(rec);
        // TODO update to new constructor
//        mRemoteControlClient = new RemoteControlClient(rec);
//        mAudioManager.registerRemoteControlClient(mRemoteControlClient);
//
//        int flags = RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
//                | RemoteControlClient.FLAG_KEY_MEDIA_NEXT
//                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY
//                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
//                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
//                | RemoteControlClient.FLAG_KEY_MEDIA_STOP;
//        mRemoteControlClient.setTransportControlFlags(flags);
        
        mPreferences = getSharedPreferences("Music", MODE_WORLD_READABLE | MODE_WORLD_WRITEABLE);
        /// M:  get Sdcard status
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE); 
        boolean hasCard = MusicUtils.hasMountedSDcard(getApplicationContext());
        Log.i(TAG, "onCreate: hasCard = " + hasCard,Log.APP);
        registerExternalStorageListener();

        // Needs to be done in this thread, since otherwise ApplicationContext.getPowerManager() crashes.
        mPlayer = new MultiPlayer();
        mPlayer.setHandler(mMediaplayerHandler);
        
        /// M:  restore audio effects
        sendSessionIdToAudioEffect(false);

        reloadQueue();
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
        
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SERVICECMD);
        commandFilter.addAction(TOGGLEPAUSE_ACTION);
        commandFilter.addAction(PAUSE_ACTION);
        commandFilter.addAction(NEXT_ACTION);
        commandFilter.addAction(PREVIOUS_ACTION);
        /// M:  For IPO shutdonw and Audio effect
        commandFilter.addAction(Intent.ACTION_SHUTDOWN);
        commandFilter.addAction(ACTION_SHUTDOWN_IPO);
        commandFilter.addAction(ATTACH_AUX_AUDIO_EFFECT);
        commandFilter.addAction(DETACH_AUX_AUDIO_EFFECT);
        /// M:register ACTION_AUDIO_BECOMING_NOISY broadcast,to reduce the receive time
        commandFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mIntentReceiver, commandFilter);
        
        /// M:let the callback onChange() run in this thread,not in main thread @{
        mContentObserverThread  = new HandlerThread("ListenToContentObserver");
        mContentObserverThread.start();
        mContentObserverThreadHandler = new Handler(mContentObserverThread.getLooper()) {};
        /// @}
        
        /// M: register observer
        this.getContentResolver().registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, false, mContentObserver);
        /// M: register skin change receiver
        IntentFilter iFilter = new IntentFilter();
        //ACTION_SKIN_CHANGED --> "android.intent.action.SKIN_CHANGED"
        iFilter.addAction("android.intent.action.SKIN_CHANGED");
        registerReceiver(mSkinChangedReceiver, iFilter);
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
        mWakeLock.setReferenceCounted(false);

        // If the service was idle, but got killed before it stopped itself, the
        // system will relaunch it. Make sure it gets stopped again in that case.
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        Log.i(TAG, "onCreate >>>",Log.APP);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, ">>> onDestroy",Log.APP);
        // Check that we're not being destroyed while something is still playing.
        if (isPlaying()) {
            Log.e(TAG, "Service being destroyed while still playing.",Log.APP);
            /// M: pause the audio play
            pause();
        }

        /// M: Restet the duration of current song
        mDurationOverride = -1;

        /// M: Cancel album art worker  @{
        if (mAsyncAlbumArtWorker != null) {
            mAsyncAlbumArtWorker.cancel(true);
        }
        /// @}

        /// M: reset MusicUtile static service instance to be null when service destroy. {@
        MusicUtils.resetStaticService();
        /// @}

        // release all MediaPlayer resources, including the native player and wakelocks
        Intent i = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        sendBroadcast(i);
        mPlayer.release();
        mPlayer = null;

        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        //mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        
        // make sure there aren't any other messages coming
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mMediaplayerHandler.removeCallbacksAndMessages(null);

        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }

        unregisterReceiver(mIntentReceiver);
        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }
        /// M: unregister skin change Receiver
        unregisterReceiver(mSkinChangedReceiver);
        mReceiverUnregistered = true;
        this.getContentResolver().unregisterContentObserver(mContentObserver);
        /// M: quitContentObserverThread when service is destoried
        quitContentObserverThread();
        mWakeLock.release();
        Log.i(TAG, "onDestroy >>>",Log.APP);
        super.onDestroy();
    }
    
    private final char hexdigits [] = new char [] {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'
    };

    private void saveQueue(boolean full) {
        Log.i(TAG, "<saveQueue> full:" + full,Log.APP);
        if (!mQueueIsSaveable) {
            Log.e(TAG, "saveQueue: queue NOT savable!!",Log.APP);
            return;
        }
        Editor ed = mPreferences.edit();
        //long start = System.currentTimeMillis();
        if (full) {
            StringBuilder q = new StringBuilder();
            
            // The current playlist is saved as a list of "reverse hexadecimal"
            // numbers, which we can generate faster than normal decimal or
            // hexadecimal numbers, which in turn allows us to save the playlist
            // more often without worrying too much about performance.
            // (saving the full state takes about 40 ms under no-load conditions
            // on the phone)
            int len = mPlayListLen;
            for (int i = 0; i < len; i++) {
                long n = mPlayList[i];
                if (n < 0) {
                    continue;
                } else if (n == 0) {
                    q.append("0;");
                } else {
                    while (n != 0) {
                        int digit = (int)(n & 0xf);
                        n >>>= 4;
                        q.append(hexdigits[digit]);
                    }
                    q.append(";");
                }
            }
            //Log.i("@@@@ service", "created queue string in " + (System.currentTimeMillis() - start) + " ms");
            ed.putString("queue", q.toString());
            Log.i(TAG, "saveQueue: queue=" + q.toString(),Log.APP);
            if (mShuffleMode != SHUFFLE_NONE) {
                // In shuffle mode we need to save the history too
                len = mHistory.size();
                q.setLength(0);
                for (int i = 0; i < len; i++) {
                    int n = mHistory.get(i);
                    if (n == 0) {
                        q.append("0;");
                    } else {
                        while (n != 0) {
                            int digit = (n & 0xf);
                            n >>>= 4;
                            q.append(hexdigits[digit]);
                        }
                        q.append(";");
                    }
                }
                ed.putString("history", q.toString());
            }
        }
        ed.putInt("curpos", mPlayPos);
        Log.i(TAG, "saveQueue: mPlayPos=" + mPlayPos + ", mPlayListLen=" + mPlayListLen,Log.APP);
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            ed.putLong("seekpos", mPlayer.position());
        }
        ed.putInt("repeatmode", mRepeatMode);
        ed.putInt("shufflemode", mShuffleMode);
        SharedPreferencesCompat.apply(ed);

        //Log.i("@@@@ service", "saved state in " + (System.currentTimeMillis() - start) + " ms");
    }

    private void reloadQueue() {
        Log.i(TAG, ">>> reloadQueue",Log.APP);
        String q = null;

        /// M: No SD card mounted, should not do normal operations, ALPS00121756 @{
        boolean hasCard = MusicUtils.hasMountedSDcard(getApplicationContext());
        Log.i(TAG, "reloadQueue: hasCard = " + hasCard,Log.APP);
        if (!hasCard) {
            Log.w(TAG, "reloadQueue: no sd card!",Log.APP);
            return;
        }
        /// @}
        q = mPreferences.getString("queue", "");
        int qlen = q != null ? q.length() : 0;
        if (qlen > 1) {
            //Log.i("@@@@ service", "loaded queue: " + q);
            int plen = 0;
            int n = 0;
            int shift = 0;
            for (int i = 0; i < qlen; i++) {
                char c = q.charAt(i);
                if (c == ';') {
                    ensurePlayListCapacity(plen + 1);
                    mPlayList[plen] = n;
                    plen++;
                    n = 0;
                    shift = 0;
                } else {
                    if (c >= '0' && c <= '9') {
                        n += ((c - '0') << shift);
                    } else if (c >= 'a' && c <= 'f') {
                        n += ((10 + c - 'a') << shift);
                    } else {
                        // bogus playlist data
                        plen = 0;
                        break;
                    }
                    shift += 4;
                }
            }
            mPlayListLen = plen;

            int pos = mPreferences.getInt("curpos", 0);
            Log.i(TAG, "reloadQueue: mPlayListLen=" + mPlayListLen + ", curpos=" + pos,Log.APP);
            if (pos < 0 || pos >= mPlayListLen) {
                // The saved playlist is bogus, discard it
                mPlayListLen = 0;
                return;
            }
            mPlayPos = pos;
            Log.i(TAG, "reloadQueue: mPlayPos=" + pos,Log.APP);

            mOpenFailedCounter = 20;

            Cursor cursor = getCursorForId(mPlayList[mPlayPos]);
            /// M: Add check for DRM to decide whether to open current track
            if (cursor != null) {
            	//update by zjw
            	//MusicFeatureOption.IS_SUPPORT_DRM
                if (DrmUtils.isSupportDrm()) {
                    checkDrmWhenOpenTrack(cursor);
                } else {
                    mQuietMode = true;
                    openCurrentAndNext();
                    mIsReloadSuccess = true;
                }
                cursor.close();
                cursor = null;
            }
            /// @}
            
            if (!mPlayer.isInitialized()) {
                // couldn't restore the saved state
                Log.e(TAG, "reloadQueue: open failed! not inited!",Log.APP);
                mPlayListLen = 0;
                /// M: Set mPlayPos to be -1 when we set mPlayListLen to be 0 to avoid array index out bound
                mPlayPos = -1;
                return;
            }

            mDoSeekWhenPrepared = true;

            int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);
            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }
            mRepeatMode = repmode;

            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }
            if (shufmode != SHUFFLE_NONE) {
                // in shuffle mode we need to restore the history too
                q = mPreferences.getString("history", "");
                qlen = q != null ? q.length() : 0;
                if (qlen > 1) {
                    plen = 0;
                    n = 0;
                    shift = 0;
                    mHistory.clear();
                    for (int i = 0; i < qlen; i++) {
                        char c = q.charAt(i);
                        if (c == ';') {
                            if (n >= mPlayListLen) {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            mHistory.add(n);
                            n = 0;
                            shift = 0;
                        } else {
                            if (c >= '0' && c <= '9') {
                                n += ((c - '0') << shift);
                            } else if (c >= 'a' && c <= 'f') {
                                n += ((10 + c - 'a') << shift);
                            } else {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }
                            shift += 4;
                        }
                    }
                }
            }
            if (shufmode == SHUFFLE_AUTO) {
                if (! makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }
            mShuffleMode = shufmode;
            
        } else {
            /// M: Reset mPlayListLen so that we have a chance
            // to enter party shuffle mode if user clicks play on widgets
            mPlayListLen = 0;
        }
        Log.i(TAG, "reloadQueue >>>",Log.APP);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        /// M: BT AVRCP Start; need check with bt guy. {@
        Log.i(TAG,
                String.format("intent %s stubname:%s", intent.getAction(), ServiceAvrcpStub.class.getName()),Log.APP);
        if (IBTAvrcpMusic.class.getName().equals(intent.getAction())) {
            Log.i("MISC_AVRCP", "MediaPlayer returns IBTAvrcpMusic",Log.APP);
            return mBinderAvrcp;
        } else if ("com.custom.music.IMediaPlaybackService".equals(intent.getAction())) {
            Log.i("MISC_AVRCP", "MediaPlayer returns ServiceAvrcp inetrface",Log.APP);
            return mBinderAvrcp;
        }
        /// BT AVRCP End @}

        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        /// M: if the key event come from monkey, don't handle this event to avoid ANR in CTS monkey test
        if (intent != null && !isEventFromMonkey()) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
            	Log.i(TAG, "<onStartCommand> CMDNEXT || NEXT_ACTION",Log.APP);
                /// M:  Don't start playing when there is no Sdcard available  @{
                boolean hasCard = MusicUtils.hasMountedSDcard(getApplicationContext());
                Log.i(TAG, "onStartCommand hasCard = " + hasCard,Log.APP);
                if (hasCard) {
                    mQuietMode = false;
                    gotoNext(true);
                } else {
                    notifyChange(QUIT_PLAYBACK);
                } 
                /// @}
            } else if (CMDPREVIOUS.equals(cmd) || PREVIOUS_ACTION.equals(action)) {
            	Log.i(TAG, "<onStartCommand> CMDPREVIOUS || PREVIOUS_ACTION",Log.APP);
                /// M: Call prev directly when previous action coming.
                //if (position() < 2000) {
                    mQuietMode = false;
                    prev();
                //} else {
                //    seek(0);
                //    play();
                //}
                /// @}
            } else if (CMDTOGGLEPAUSE.equals(cmd) || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                	Log.i(TAG, "<onStartCommand> CMDTOGGLEPAUSE || TOGGLEPAUSE_ACTION --> pause",Log.APP);
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                	Log.i(TAG, "<onStartCommand> CMDTOGGLEPAUSE || TOGGLEPAUSE_ACTION --> play",Log.APP);
                    play();
                    /// M:  if don't start to play, reset the  mQuietMode  @{
                    if (!isPlaying()) {
                        mQuietMode = false;
                    }
                    /// @}
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
            	Log.i(TAG, "<onStartCommand> CMDPAUSE || PAUSE_ACTION",Log.APP);
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDPLAY.equals(cmd)) {
            	Log.i(TAG, "<onStartCommand> CMDPLAY",Log.APP);
                play();
            } else if (CMDSTOP.equals(cmd)) {
            	Log.i(TAG, "<onStartCommand> CMDSTOP",Log.APP);
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
            /// M:  AVRCP and Android Music AP supports the FF/REWIND  @{
            } else if (CMDFORWARD.equals(cmd)) {
            	Log.i(TAG, "<onStartCommand> CMDFORWARD",Log.APP);
                long deltaTime = intent.getLongExtra(DELTATIME, 0);
                scanForward(deltaTime);
            } else if (CMDREWIND.equals(cmd)) {
            	Log.i(TAG, "<onStartCommand> CMDREWIND",Log.APP);
                long deltaTime = intent.getLongExtra(DELTATIME, 0);
                scanBackward(deltaTime);
            /// @}
            } else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
            	Log.i(TAG, "<onStartCommand> CMDAPPWIDGETUPDATE",Log.APP);
                // Someone asked us to refresh a set of specific widgets, probably
                // because they were just added.
                int[] appWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetProvider.performUpdate(MediaPlaybackService.this, appWidgetIds);
            }

        }
        
        // make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        return START_NOT_STICKY;
    }
    
    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;

        // Take a snapshot of the current playlist
        saveQueue(true);

        /// M: If there are clients connectting to service, we should not stop service.
        if (isPlaying() || mPausedByTransientLossOfFocus || MusicUtils.hasBoundClient()) {
            // something is currently playing, or will be playing once 
            // an in-progress action requesting audio focus ends, so don't stop the service now.
            return true;
        }
        
        // If there is a playlist but playback is paused, then wait a while
        // before stopping the service, so that pause/resume isn't slow.
        // Also delay stopping the service if we're transitioning between tracks.
        if (mPlayListLen > 0  || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }
        
        // No active playlist, OK to stop the service right now
        stopSelf(mServiceStartId);
        return true;
    }
    
    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	Log.i(TAG, "<mDelayedStopHandler.handleMessage>",Log.APP);
            /// M: If there are clients connectting to service, we should not stop service.
            // Check again to make sure nothing is playing right now
            if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
                    || MusicUtils.hasBoundClient() || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }
            // save the queue again, because it might have changed
            // since the user exited the music app (because of
            // party-shuffle or because the play-position changed)
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    };

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     *
     * @param storagePath path to mount point for the removed media
     */
    public void closeExternalStorageFiles(String storagePath) {
    	Log.i(TAG, "<closeExternalStorageFiles> storagePath:" + storagePath,Log.APP);
        // stop playback and clean up if the SD card is going to be unmounted.
        stop(true);
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
        /// M:  Because MediaPlayer has been stopped, it does not has any meaning
        // to update the meta data
        // UPDATE: we still need this message since Music can call updateTrackInfo 
        // to decide whether to finish itself after a card is plugged out
        // Instead, tell'em player state is changed
        notifyChange(PLAYSTATE_CHANGED);
    }

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications.
     * The intent will call closeExternalStorageFiles() if the external media
     * is going to be ejected, so applications can clean up any files they have open.
     */
    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    /// M: check the receiver is alive @{
                    if (mReceiverUnregistered) {
                        return;
                    }
                    /// @}
                    
                    String action = intent.getAction();
                    /// M: modify for Multi-SD card support @{
                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        Log.i(TAG, "<mUnmountReceiver.onReceive> ACTION_MEDIA_EJECT",Log.APP);
                        /// M: For multi-SD card, we should decide whether the internal SD card is ejected
                        mEjectingCardPath = intent.getData().getPath();
                        mMediaMountedCount--;
                        Log.i(TAG, "ejected card path=" + mEjectingCardPath,Log.APP);
                        
                        /// M: Release next player to avoid switch to next song causing by media
                        /// server died. because next player may visit the eject sdcard.We should
                        /// set next media player only when current player has been initialized,
                        /// because if we set it when player not initialized(such as we has called
                        /// reset), the native media player will throw a IllegalArgumentException
                        if (mPlayer.isInitialized()) {
                            mPlayer.setNextDataSource(null);
                        }
                        
                        if (mEjectingCardPath.equals(Environment.getExternalStorageDirectory().getPath())) {
                            /// M: internal card is being ejected
                            saveQueue(true);
                            mQueueIsSaveable = false;
                            closeExternalStorageFiles(mEjectingCardPath);
                            Log.i(TAG, "card eject",Log.APP);
                        } else {
                            /// M: see if the currently playing track is on the removed card...
                            if (mCursor != null && !mCursor.isAfterLast()) {
                                String curTrackPath = mCursor.getString(mCursor.getColumnIndexOrThrow(
                                        MediaStore.Audio.Media.DATA));
                                if (curTrackPath != null && curTrackPath.contains(mEjectingCardPath)) {
                                    Log.i(TAG, "MEDIA_EJECT: current track on an unmounting external card",Log.APP);
                                    closeExternalStorageFiles(mEjectingCardPath);
                                }
                            }
                        }

                        mEjectingCardPath = null;
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED) && mMediaMountedCount < 0) {
                    	Log.i(TAG, "<mUnmountReceiver.onReceive> ACTION_MEDIA_MOUNTED",Log.APP);
                        /// M: This service assumes that these two intents come as a pair. But some 
                        /// third party application sends only mounted Intent, which causes
                        /// chaos here. It can only perform resume playing if SD card is ejected
                        /// before, which is indicated by 'mMediaMountedCount < 0'.
                        /// M: TODO: decide carefully whether mount count should include non-internal card re-mount
                        mMediaMountedCount++;

                        String mountedCardPath = intent.getData().getPath();
                        Log.i(TAG, "mounted card path=" + mountedCardPath,Log.APP);
                        if (mountedCardPath.equals(Environment.getExternalStorageDirectory().getPath())) {
                            /// M: internal card mounted
                            Log.i(TAG, "card mounted",Log.APP);
                            reloadQueue();
                            mQueueIsSaveable = true;
                            notifyChange(QUEUE_CHANGED);
                            notifyChange(META_CHANGED);
                        }
                    } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                        reloadQueueAfterScan();
                    }
                    /// @}
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    /**
     * Notify the change-receivers that something has changed.
     * The intent that is sent contains the following data
     * for the currently playing track:
     * "id" - Integer: the database row ID
     * "artist" - String: the name of the artist
     * "album" - String: the name of the album
     * "track" - String: the name of the track
     * The intent has an action that is one of
     * "com.custom.music.metachanged"
     * "com.custom.music.queuechanged",
     * "com.custom.music.playbackcomplete"
     * "com.custom.music.playstatechanged"
     * respectively indicating that a new track has
     * started playing, that the playback queue has
     * changed, that playback has stopped because
     * the last file in the list has been played,
     * or that the play-state changed (paused/resumed).
     */
    private void notifyChange(String what) {
        Log.i(TAG, "<notifyChange> what:" + what,Log.APP);
        Intent i = new Intent(what);
        i.putExtra("id", Long.valueOf(getAudioId()));
        i.putExtra("artist", getArtistName());
        i.putExtra("album",getAlbumName());
        i.putExtra("track", getTrackName());
        i.putExtra("playing", isPlaying());
        /// M: for QUIT_PLAYBACK, do NOT use sticky broadcast @{
        if (QUIT_PLAYBACK.equals(what)) {
            /// M: when quit playback, we need to reset music to be default status.
            mPlayPos = -1;
            mPlayListLen = 0;
            mShuffleMode = SHUFFLE_NONE;
            mRepeatMode = REPEAT_NONE;
            sendBroadcast(i);
        } else {
        /// @}
            sendStickyBroadcast(i);

            if (what.equals(PLAYSTATE_CHANGED)) {
//            mRemoteControlClient.setPlaybackState(isPlaying() ?
//                    RemoteControlClient.PLAYSTATE_PLAYING : RemoteControlClient.PLAYSTATE_PAUSED);
            } else if (what.equals(META_CHANGED)) {
//            RemoteControlClient.MetadataEditor ed = mRemoteControlClient.editMetadata(true);
//            ed.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, getTrackName());
//            ed.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, getAlbumName());
//            ed.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, getArtistName());
//            ed.putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, duration());
//            Bitmap b = MusicUtils.getArtwork(this, getAudioId(), getAlbumId(), false);
//            if (b != null) {
//                ed.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, b);
//            }
//            ed.apply();
            }
        }

        if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
        } else {
            saveQueue(false);
        }
        
        // Share this notification directly with our widgets
        mAppWidgetProvider.notifyChange(this, what);

        /// M: AVRCP14
        notifyBTAvrcp(what);
    }

    private void ensurePlayListCapacity(int size) {
        if (mPlayList == null || size > mPlayList.length) {
            // reallocate at 2x requested size so we don't
            // need to grow and copy the array for every
            // insert
            long [] newlist = new long[size * 2];
            int len = mPlayList != null ? mPlayList.length : mPlayListLen;
            for (int i = 0; i < len; i++) {
                newlist[i] = mPlayList[i];
            }
            mPlayList = newlist;
        }
        // FIXME: shrink the array when the needed size is much smaller
        // than the allocated size
    }
    
    // insert the list of songs at the specified position in the playlist
    private void addToPlayList(long [] list, int position) {
        int addlen = list.length;
        if (position < 0) { // overwrite
            mPlayListLen = 0;
            position = 0;
        }
        ensurePlayListCapacity(mPlayListLen + addlen);
        if (position > mPlayListLen) {
            position = mPlayListLen;
        }
        
        // move part of list after insertion point
        int tailsize = mPlayListLen - position;
        for (int i = tailsize ; i > 0 ; i--) {
            mPlayList[position + i] = mPlayList[position + i - addlen];
        }
        
        // copy list into playlist
        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }
        mPlayListLen += addlen;
        if (mPlayListLen == 0) {
            /// M: if the playlist be empty,clear the history to avoid these files can not
            // be played when added to playlist nexttime.
            mHistory.clear();
            mCursor.close();
            mCursor = null;
            notifyChange(META_CHANGED);
        }
    }
    
    /**
     * Appends a list of tracks to the current playlist.
     * If nothing is playing currently, playback will be started at
     * the first track.
     * If the action is NOW, playback will switch to the first of
     * the new tracks immediately.
     * @param list The list of tracks to append.
     * @param action NOW, NEXT or LAST
     */
    public void enqueue(long [] list, int action) {
    	Log.i(TAG, "<enqueue> action:" + action,Log.APP);
        synchronized(this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                // action == LAST || action == NOW || mPlayPos + 1 == mPlayListLen
                ///M: if add Music file to Nowplaying,and select repeate all,then setnextPlayer be null to 
                // avoid do not play the added Music. @{
                if (mPlayer.isInitialized() && getRepeatMode() == REPEAT_ALL
                        && mPlayPos == (mPlayListLen - 1)) {
                    mPlayer.setNextDataSource(null);
                }
                /// @}
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);
                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.length;
                    openCurrentAndNext();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }
            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrentAndNext();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Replaces the current playlist with a new list,
     * and prepares for starting playback at the specified
     * position in the list, or a random position if the
     * specified position is 0.
     * @param list The new list of tracks.
     */
    public void open(long [] list, int position) {
    	Log.i(TAG, "<open> position:" + position + " list:" + list,Log.APP);
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            long oldId = getAudioId();
            int listlength = list.length;
            boolean newlist = true;
            if (mPlayListLen == listlength) {
                // possible fast path: list might be the same
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlayList[i]) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }
            int oldpos = mPlayPos;
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mRand.nextInt(mPlayListLen);
            }
            mHistory.clear();

            saveBookmarkIfNeeded();
            openCurrentAndNext();
            //temp update by zjw
//            if (oldId != getAudioId()) {
//                notifyChange(META_CHANGED);
//            }
        }
    }
    
    /**
     * Moves the item at index1 to index2.
     * @param index1
     * @param index2
     */
    public void moveQueueItem(int index1, int index2) {
    	Log.i(TAG, "<moveQueueItem> index1:" + index1 + " index2:" + index2,Log.APP);
        synchronized (this) {
            /// M: if the playlist changed,set the nextPlayer be null to 
            // avoid it will be Played but the MediaPlaybackActivity show other file's information.@{
            if (mPlayer.isInitialized()) {
                mPlayer.setNextDataSource(null);
            }
            /// @}
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }
            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }
            if (index1 < index2) {
                long tmp = mPlayList[index1];
                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i+1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                        mPlayPos--;
                }
            } else if (index2 < index1) {
                long tmp = mPlayList[index1];
                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i-1];
                }
                mPlayList[index2] = tmp;
                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                        mPlayPos++;
                }
            }
            notifyChange(QUEUE_CHANGED);
        }
    }

    /**
     * Returns the current play list
     * @return An array of integers containing the IDs of the tracks in the play list
     */
    public long [] getQueue() {
    	Log.i(TAG, "<getQueue>",Log.APP);
        synchronized (this) {
            int len = mPlayListLen;
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                list[i] = mPlayList[i];
            }
            return list;
        }
    }

    private Cursor getCursorForId(long lid) {
    	Log.i(TAG, "<getCursorForId> lid:" + lid,Log.APP);
        String id = String.valueOf(lid);

        Cursor c = getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                mCursorCols, "_id=" + id , null, null);
        /// M: check the content of cursor
        if (c != null) {
            if (!c.moveToFirst()) {
                c.close();
                c = null;
            }
        }
        Log.i(TAG, "getCursorForId is " + c,Log.APP);
        return c;
    }

    private void openCurrentAndNext() {
    	Log.i(TAG, ">>> openCurrentAndNext",Log.APP);
        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            /// M: reset the duration of current song
            mDurationOverride = -1;
            if (mPlayListLen == 0) {
                /// M: if the playlist be empty,clear the history to avoid these files can not
                // be played when added to playlist nexttime.
                mHistory.clear();
                return;
            }
            stop(false);

            mCursor = getCursorForId(mPlayList[mPlayPos]);
            /// M: check cursor is not null firstly.
            while ((mCursor == null) ||
                !checkDrm(mCursor) || !open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + mCursor.getLong(IDCOLIDX))) {
                // if we get here then opening the file failed. We can close the cursor now, because
                // we're either going to create a new one next, or stop trying
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
                if (mOpenFailedCounter++ < OPEN_FAILED_MAX_COUNT && mPlayListLen > 1) {
                    /// M: When user send the prev command, if fail, we go on with prev and break from
                    /// this while after prev return. @{
                    if (mIsPrev) {
                        prev();
                        break;
                    } else {
                    /// @}
                        int pos = getNextPosition(false);
                        if (pos < 0) {
                            gotoIdleState();
                            if (mIsSupposedToBePlaying) {
                                mIsSupposedToBePlaying = false;
                                notifyChange(PLAYSTATE_CHANGED);
                            }
                            return;
                        }
                        mPlayPos = pos;
                        stop(false);
                        mPlayPos = pos;
                        mCursor = getCursorForId(mPlayList[mPlayPos]);
                    }
                } else {
                    if (!mQuietMode) {
                        /// M: send message to avoid backgroung toast
                        mMediaplayerHandler.sendMessage(mMediaplayerHandler.obtainMessage(OPEN_FAILED));
                    }
                    /// M: Clear playlist and history when open failed times more than OPEN_FAILED_MAX_COUNT
                    /// or all the playlist songs have opened failed. {@
                    Log.i(TAG, "clear palylist and position when open failed: mPlayListLen = " + mPlayListLen,Log.APP);
                    mHistory.clear();
                    mPlayPos = -1;
                    mNextPlayPos = -1;
                    /// @}
                    gotoIdleState();
                    if (mIsSupposedToBePlaying) {
                        mIsSupposedToBePlaying = false;
                        notifyChange(PLAYSTATE_CHANGED);
                    }
                    mOpenFailedCounter = 0;
                    Log.i(TAG, "Failed to open file for playback",Log.APP);
                    return;
                }
            }

            /// M: Move this to preparelistner to seek after finish prepared. {@
            /*// go to bookmark if needed
            if (isPodcast()) {
                long bookmark = getBookmark();
                // Start playing a little bit before the bookmark,
                // so it's easier to get back in to the narrative.
                seek(bookmark - 5000);
            }*/
            /// @}
        }
        Log.i(TAG, "openCurrentAndNext >>>",Log.APP);
    }

    private void setNextTrack() {
    	Log.i(TAG, ">>> setNextTrack",Log.APP);
        /// M: Only when current player has been initialized we can set next media player to current
        /// player.
        if (!mPlayer.isInitialized()) {
            Log.e(TAG, "setNextTrack with player not initialized!",Log.APP);
            return;
        }
       /// M: playlist's length is 1 with shuffle nomal mode,need not set the nextplayer 
       //     just return, to avoid it will be play twice.
        if (mShuffleMode == SHUFFLE_NORMAL && mPlayListLen == 1) {
            Log.i(TAG, "playlist's length is 1 with shuffle nomal mode,need not set nextplayer.",Log.APP);
            return;
        }
        
        mNextPlayPos = getNextPosition(false);
        /// M: we don't set the next player in repeat one mode
        if (mNextPlayPos >= 0 && mRepeatMode != REPEAT_CURRENT) {
            long id = mPlayList[mNextPlayPos];
            mPlayer.setNextDataSource(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);
        /// M: clear next player when there is no song for next  @{
        } else {
            mPlayer.setNextDataSource(null);
        }
        /// @}
        Log.i(TAG, "setNextTrack >>>",Log.APP);
    }

    /**
     * Opens the specified file and readies it for playback.
     *
     * @param path The full path of the file to be opened.
     */
    public boolean open(String path) {
        synchronized (this) {
            Log.i(TAG, "<open> path:" + path,Log.APP);
            if (path == null) {
                return false;
            }
            
            // if mCursor is null, try to associate path with a database cursor
            if (mCursor == null) {

                ContentResolver resolver = getContentResolver();
                Uri uri;
                String where;
                String selectionArgs[];
                if (path.startsWith("content://media/")) {
                    uri = Uri.parse(path);
                    where = null;
                    selectionArgs = null;
                } else {
                   uri = MediaStore.Audio.Media.getContentUriForPath(path);
                   where = MediaStore.Audio.Media.DATA + "=?";
                   selectionArgs = new String[] { path };
                }
                
                try {
                    mCursor = resolver.query(uri, mCursorCols, where, selectionArgs, null);
                    if  (mCursor != null) {
                        if (mCursor.getCount() == 0) {
                            mCursor.close();
                            mCursor = null;
                        } else {
                            mCursor.moveToNext();
                            ensurePlayListCapacity(1);
                            mPlayListLen = 1;
                            mPlayList[0] = mCursor.getLong(IDCOLIDX);
                            mPlayPos = 0;
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                }
            }
            mFileToPlay = path;
            /// M: seset the status about player, track and playlist  @{
            mIsPlayerReady = false;
            mMediaSeekable = true;
            mIsPlaylistCompleted = false;            
            mTrackCompleted = false;
            /// @}
            
            /// M: Open Asynchronously
            mPlayer.setDataSourceAsync(mFileToPlay);
            if (mPlayer.isInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }
            stop(true);
            return false;
        }
    }

    /**
     * Starts playback of a previously opened file.
     */
    public void play() {
        synchronized(this) {
           final long short_songe_length = 10000;
            Log.i(TAG, ">>> play: init=" + mPlayer.isInitialized() + ", ready=" + mIsPlayerReady
                    + ", listlen=" + mPlayListLen,Log.APP);
            /// M: When request AudioFocus failed, we should not play music and need toast user maybe the
            /// phone call is ongoing. @{
            if (AudioManager.AUDIOFOCUS_REQUEST_FAILED == mAudioManager.requestAudioFocus(
                    mAudioFocusListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)) {
                showToast(getString(R.string.audiofocus_request_failed_message));
                Log.w(TAG, "<< play: phone call is ongoing, can not play music!",Log.APP);
                return;
            }
            /// @}
            mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this.getPackageName(),
                MediaButtonIntentReceiver.class.getName()));
            /// M:  If play() gets called, onPrepare() should call play()
            // and Music should not be silent any more, if error occurred
            mQuietMode = false;
            /// M: also check the player ready
            if (mPlayer.isInitialized() && mIsPlayerReady) {
                /// M: reset the playlist complete
                mIsPlaylistCompleted = false;
                // if we are at the end of the song, go to the next song first
                long duration = mPlayer.duration();
                /// M: change the condition to next song and when duration is 0, we switch the next song.
                if (canGoToNext(duration, short_songe_length)) {
                    gotoNext(true);
                    /// M: gotoNext(true) will call open() and play() again. This play() should end here.
                    notifyChange(PLAYSTATE_CHANGED);
                    Log.i(TAG, "play>>> go to next song first",Log.APP);
                    return;
                }


                mPlayer.start();
                Log.i(TAG, "MediaPlayer start done.",Log.APP);
                // make sure we fade in, in case a previous fadein was stopped because
                // of another focus loss
                mMediaplayerHandler.removeMessages(FADEDOWN);
                mMediaplayerHandler.sendEmptyMessage(FADEUP);
                
                updateNotification(this, null);
                if (!mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = true;
                    notifyChange(PLAYSTATE_CHANGED);
                }

                /// M: If music receive attach reverb effect at pause, we need to attach it when start play again. {@
                if (mWhetherAttachWhenPause) {
                    if (mAuxEffectId > 0) {
                        mPlayer.attachAuxEffect(mAuxEffectId);
                        mPlayer.setAuxEffectSendLevel(1.0f);
                    } else {
                        mPlayer.attachAuxEffect(0);
                    }
                    mWhetherAttachWhenPause = false;
                    Log.i(TAG, "Attach reverb when user start play again with mAuxEffectId = " + mAuxEffectId,Log.APP);
                }
                /// @}
                /// M: start AlbumArtWorker to get album art for notification @{
                if (mPreAudioId != getAudioId()) {
                    mAsyncAlbumArtWorker = new AlbumArtWorker();
                    mAsyncAlbumArtWorker.execute(Long.valueOf(getAlbumId()));
                }
                /// @}
            } else if (mPlayListLen <= 0 && !(mPlayer.isInitialized() || mIsPlayerReady)) {
                // This is mostly so that if you press 'play' on a bluetooth headset
                // without every having played anything before, it will still play
                // something.
                setShuffleMode(SHUFFLE_AUTO);
                
            }/* else {
                /// M: If call play() with player not ready, maybe we not support this audio and onError at setDataSource.
                showToast(getString(R.string.fail_to_start_stream));
                MusicLogUtils.e(TAG, "play a not support audio, toast to user: mPlayPos = " + mPlayPos);
            }*/
            /// M: reset Prev Audio Id
            mPreAudioId = -1;
            Log.i(TAG, "play >>>",Log.APP);
        }
    }

    /**
     * M : enhance the notification with Album, next,prev and stop button
     * 
     * @param context The context
     * @param bitmap The album art bitmap
     */
    private void updateNotification(Context context, Bitmap bitmap) {
    	Log.i(TAG, ">>> updateNotification",Log.APP);
        RemoteViews views = new RemoteViews(getPackageName(), R.layout.newstatusbar);
        String trackinfo = getTrackName();
        String artist = getArtistName();
        if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
            artist = getString(R.string.unknown_artist_name);
        }
        /// M: change the text color along theme change @{
        //update by zjw
//        if (MusicFeatureOption.IS_SUPPORT_THEMEMANAGER) {
//            Resources res = getResources();
//            int textColor = res.getThemeMainColor();
//            if (textColor != 0) {
//                views.setTextColor(R.id.txt_trackinfo, textColor);
//            }
//        }
        /// @}
        trackinfo += " - " + artist;
        views.setTextViewText(R.id.txt_trackinfo, trackinfo);
        Intent intent;
        PendingIntent pIntent;
        
        intent = new Intent("com.custom.music.PLAYBACK_VIEWER");
        intent.putExtra("collapse_statusbar", true);
        pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.iv_cover, pIntent);
        
        intent = new Intent(PREVIOUS_ACTION);
        intent.setClass(context, MediaPlaybackService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.btn_prev, pIntent);
        
        intent = new Intent(PAUSE_ACTION);
        intent.setClass(context, MediaPlaybackService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.btn_pause, pIntent);
        
        intent = new Intent(NEXT_ACTION);
        intent.setClass(context, MediaPlaybackService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.btn_next, pIntent);
          
        intent = new Intent("my.nullaction");
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        views.setOnClickPendingIntent(R.id.rl_newstatus, pIntent);
        if (bitmap != null) {
            views.setImageViewBitmap(R.id.iv_cover, bitmap);
            mAlbumArt = bitmap;
        }  
        Notification status = new Notification();
        status.contentView = views;
        status.flags |= Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.stat_notify_musicplayer;
        status.contentIntent = PendingIntent.getService(context, 0, intent, 0);
        startForeground(PLAYBACKSERVICE_STATUS, status);
        
        Log.i(TAG, "updateNotification >>>",Log.APP);
    }

    private void stop(boolean remove_status_icon) {
        /// M: add synchronized
        synchronized (this) {
            Log.i(TAG, "<stop>" + remove_status_icon,Log.APP);
            if (mPlayer.isInitialized()) {
                mPlayer.stop();
            }
            /// M: seset the status about player and track  @{
            mIsPlayerReady = false;
            mDoSeekWhenPrepared = false;
            mMediaSeekable = true;
            /// @}
            mFileToPlay = null;
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
            /// M: Reset the duration of current song
            mDurationOverride = -1;
            if (remove_status_icon) {
                gotoIdleState();
            }
            /// Need not to stop foreground every time call stop(false), because it will make music proirty become
            /// low and may be retrieved by ActivityManager. Such as when Music is playing background and user 
            /// remove Music from app list, then user press next button in music notification bar, music will be
            /// retrieved. {@
            /*else {
                stopForeground(false);
            }*/
            /// @}
            if (remove_status_icon) {
                mIsSupposedToBePlaying = false;
            }
        }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        stop(true);
    }

    /**
     * Pauses playback (call play() to resume)
     */
    public void pause() {
        synchronized(this) {
            Log.i(TAG, "<pause>",Log.APP);
            mMediaplayerHandler.removeMessages(FADEUP);
            /// M: If Music is fading in, force it to stop
            mPlayer.setVolume(1.0f);
            /// M: add player status check
            if (isPlaying() && mPlayer.isInitialized()) {
                if (mPlayer.isPlaying()) {
                    mPlayer.pause();
                }
                /// M: If next player has init, when current player will complete, native will auto switch to
                /// next player and notify APP current player has completed. So if we pause music before the
                /// notify and after current player complete, native will not stop nextplayer, because next
                /// player has auto switch to play. we clear next player if user pause the music which has been
                /// near complete. {@
                final int nearCompleteTime = 2000; /// ms
                if (duration() - position() < nearCompleteTime) {
                    mPlayer.setNextDataSource(null);
                }
                /// @}
                gotoIdleState();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
                saveBookmarkIfNeeded();
            }
        }
    }

    /** Returns whether something is currently playing
     *
     * @return true if something is playing (or will be playing shortly, in case
     * we're currently transitioning between tracks), false if not.
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /*
      Desired behavior for prev/next/shuffle:

      - NEXT will move to the next track in the list when not shuffling, and to
        a track randomly picked from the not-yet-played tracks when shuffling.
        If all tracks have already been played, pick from the full set, but
        avoid picking the previously played track if possible.
      - when shuffling, PREV will go to the previously played track. Hitting PREV
        again will go to the track played before that, etc. When the start of the
        history has been reached, PREV is a no-op.
        When not shuffling, PREV will go to the sequentially previous track (the
        difference with the shuffle-case is mainly that when not shuffling, the
        user can back up to tracks that are not in the history).

        Example:
        When playing an album with 10 tracks from the start, and enabling shuffle
        while playing track 5, the remaining tracks (6-10) will be shuffled, e.g.
        the final play order might be 1-2-3-4-5-8-10-6-9-7.
        When hitting 'prev' 8 times while playing track 7 in this example, the
        user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next',
        a random track will be picked again. If at any time user disables shuffling
        the next/previous track will be picked in sequential order again.
     */

    public void prev() {
        synchronized (this) {
            Log.i(TAG, "<prev>",Log.APP);
            if (mShuffleMode == SHUFFLE_NORMAL) {
                // go to previously-played track and remove it from the history
                int histsize = mHistory.size();
                if (histsize == 0) {
                    // prev is a no-op
                    return;
                }
                /// M: We should check the position saved in history, if it was big than the play length, we need to
                /// pop out next value, and if there are no valid value in history, just return. {@
                boolean hasValidValue = false;
                for (int i = histsize - 1; i >= 0; i--) {
                    Integer pos = mHistory.remove(i);
                    if (pos < mPlayListLen) {
                        mPlayPos = pos.intValue();
                        hasValidValue = true;
                        break;
                    }
                }
                Log.i(TAG,"prev: mPlayPos = " + mPlayPos + ", mHistory = " + mHistory,Log.APP);
                if (!hasValidValue) {
                    Log.w(TAG, "prev with no valid position in history at shuffle mode!",Log.APP);
                    return;
                }
                /// @}
            } else {
                if (mPlayPos > 0) {
                    mPlayPos--;
                } else {
                    mPlayPos = mPlayListLen - 1;
                }
            }
            /// M: set prev status for prev fail
            mIsPrev = true;
            saveBookmarkIfNeeded();
            stop(false);
            openCurrentAndNext();
            /// M: remove paly(), it will be called in the prepare callback
            //play();
            //temp update by zjw
//            notifyChange(META_CHANGED);
            /// M: reset prev status
            mIsPrev = false;
        }
    }


    /**
         * Get the next position to play. Note that this may actually modify mPlayPos
         * if playback is in SHUFFLE_AUTO mode and the shuffle list window needed to
         * be adjusted. Either way, the return value is the next value that should be
         * assigned to mPlayPos;
         */
    private int getNextPosition(boolean force) {
        Log.i(TAG, "<getNextPosition> force" + force,Log.APP);
        /// M: marked for use can get next song in repeat one mode @{
        //if (mRepeatMode == REPEAT_CURRENT) {
        //    if (mPlayPos < 0) return 0;
        //    return mPlayPos;
        //} else 
        /// @}
        if (mShuffleMode == SHUFFLE_NORMAL) {
            // Pick random next track from the not-yet-played ones
            // TODO: make it work right after adding/removing items in the queue.

            /// M: move to gotoNext() for only add to history when gotoNext @{
            // Store the current file in the history, but keep the history at a
            // reasonable size
            //if (mPlayPos >= 0) {
            //    mHistory.add(mPlayPos);
            //}
            //if (mHistory.size() > MAX_HISTORY_SIZE) {
            //    mHistory.removeElementAt(0);
            //}
            /// @}
            int numTracks = mPlayListLen;
            int indexFilterStart = 0;
            int[] tracks = new int[numTracks];
            int numHistory = mHistory.size();

            // Find the start index of the latest repeat
            for (int i = 0; i < numHistory; i++) {
                int index = mHistory.get(i).intValue();
                if (index >= tracks.length) {
                    mHistory.clear();
                    numHistory = 0;
                    indexFilterStart = 0;
                    break;
                }
                if (tracks[index] == -1) {
                    tracks = new int[numTracks];
                    indexFilterStart = i;
                } else {
                    tracks[index] = -1;
                }
            }

            for (int i=0;i < numTracks; i++) {
                tracks[i] = i;
            }

            int numUnplayed = numTracks;
            for (int i = indexFilterStart; i < numHistory; i++) {
                int idx = mHistory.get(i).intValue();
                if (idx < numTracks && tracks[idx] >= 0) {
                    numUnplayed--;
                    tracks[idx] = -1;
                }
            }

            // 'numUnplayed' now indicates how many tracks have not yet
            // been played, and 'tracks' contains the indices of those
            // tracks.
            if (numUnplayed <=0) {
                // everything's already been played
                if (mRepeatMode == REPEAT_ALL || force) {
                    //pick from full set
                    numUnplayed = numTracks;
                    for (int i=0;i < numTracks; i++) {
                        tracks[i] = i;
                    }
                } else {
                    // all done
                    return -1;
                }
            }
            int skip = mRand.nextInt(numUnplayed);
            int cnt = -1;
            while (true) {
                while (tracks[++cnt] < 0)
                    ;
                skip--;
                if (skip < 0) {
                    break;
                }
            }
            return cnt;
        } else if (mShuffleMode == SHUFFLE_AUTO) {
            doAutoShuffleUpdate();
            return mPlayPos + 1;
        } else {
            if (mPlayPos >= mPlayListLen - 1) {
                // we're at the end of the list
                Log.i(TAG, "next: end of list...",Log.APP);
                if (mRepeatMode == REPEAT_NONE && !force) {
                    // all done
                    return -1;
                } else if (mRepeatMode == REPEAT_ALL || force) {
                    return 0;
                }
                return -1;
            } else {
                return mPlayPos + 1;
            }
        }
    }

    public void gotoNext(boolean force) {
        synchronized (this) {
            Log.i(TAG, ">>> gotoNext force:" + force,Log.APP);
            if (mPlayListLen <= 0) {
                Log.w(TAG, "No play queue",Log.APP);
                return;
            }
            /// M: Move from getNextPosition() to add played track to history only when call gotoNext()
            /// or current play complete. When all track has shuffle played and user call
            /// gotoNext(true) to switch to next track, we need add played same track to history.
            addPlayedTrackToHistory(force && (mHistory.size() >= mPlayListLen));

            int pos = getNextPosition(force);
            if (pos < 0) {
                gotoIdleState();
                if (mIsSupposedToBePlaying) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                }
                /// M: set playlist complete @{
                if (!mIsPlaylistCompleted) {
                    mIsPlaylistCompleted = true;
                    /// M: AVRCP14
                    notifyChange(PLAYBACK_COMPLETE);
                }
                /// @}

                /// M: Force fading up to stop to avoid Music keep calling play() if current status is pause
                mMediaplayerHandler.removeMessages(FADEUP);
                mPlayer.setVolume(1.0f);
                return;
            }
            mPlayPos = pos;
            saveBookmarkIfNeeded();
            stop(false);
            mPlayPos = pos;
            openCurrentAndNext();
            /// M: remove paly(), it will be called in the prepare callback
            //play();
            //temp update by zjw
//            notifyChange(META_CHANGED);
            Log.i(TAG, "gotoNext >>> " + force,Log.APP);
        }
    }
    
    private void gotoIdleState() {
        Log.i(TAG, "<gotoIdleState>",Log.APP);
        /// M: clear the album art
        mAlbumArt = null;
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        stopForeground(true);
    }
    
    private void saveBookmarkIfNeeded() {
    	Log.i(TAG, "<saveBookmarkIfNeeded>",Log.APP);
        try {
            if (isPodcast()) {
                long pos = position();
                long bookmark = getBookmark();
                long duration = duration();
                if ((pos < bookmark && (pos + 10000) > bookmark) ||
                        (pos > bookmark && (pos - 10000) < bookmark)) {
                    // The existing bookmark is close to the current
                    // position, so don't update it.
                    return;
                }
                if (pos < 15000 || (pos + 10000) > duration) {
                    // if we're near the start or end, clear the bookmark
                    pos = 0;
                }
                
                // write 'pos' to the bookmark field
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.BOOKMARK, pos);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursor.getLong(IDCOLIDX));
                getContentResolver().update(uri, values, null, null);
            }
        } catch (SQLiteException ex) {
        }
    }

    // Make sure there are at least 5 items after the currently playing item
    // and no more than 10 items before.
    private void doAutoShuffleUpdate() {
    	Log.i(TAG, ">>> doAutoShuffleUpdate",Log.APP);
        boolean notify = false;

        // remove old entries
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }
        // add new entries if needed
        int to_add = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));
        for (int i = 0; i < to_add; i++) {
            // pick something at random from the list

            int lookback = mHistory.size();
            int idx = -1;
            while(true) {
                idx = mRand.nextInt(mAutoShuffleList.length);
                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }
                lookback /= 2;
            }
            mHistory.add(idx);
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }
            ensurePlayListCapacity(mPlayListLen + 1);
            mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
            notify = true;
        }
        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    // check that the specified idx is not in the history (but only look at at
    // most lookbacksize entries in the history)
    private boolean wasRecentlyUsed(int idx, int lookbacksize) {
    	Log.i(TAG, "<wasRecentlyUsed> idx:" + idx + " lookbacksize:" + lookbacksize,Log.APP);
        // early exit to prevent infinite loops in case idx == mPlayPos
        if (lookbacksize == 0) {
            return false;
        }

        int histsize = mHistory.size();
        if (histsize < lookbacksize) {
            Log.i(TAG, "lookback too big",Log.APP);
            lookbacksize = histsize;
        }
        int maxidx = histsize - 1;
        for (int i = 0; i < lookbacksize; i++) {
            long entry = mHistory.get(maxidx - i);
            if (entry == idx) {
                return true;
            }
        }
        return false;
    }

    // A simple variation of Random that makes sure that the
    // value it returns is not equal to the value it returned
    // previously, unless the interval is 1.
    private static class Shuffler {
        private int mPrevious;
        private Random mRandom = new Random();
        public int nextInt(int interval) {
            int ret;
            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);
            mPrevious = ret;
            return ret;
        }
    };

    private boolean makeAutoShuffleList() {
    	Log.i(TAG, "<makeAutoShuffleList>",Log.APP);
        ContentResolver res = getContentResolver();
        Cursor c = null;
        try {
            c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Media._ID}, MediaStore.Audio.Media.IS_MUSIC + "=1",
                    null, null);
            if (c == null || c.getCount() == 0) {
                return false;
            }
            int len = c.getCount();
            long [] list = new long[len];
            for (int i = 0; i < len; i++) {
                c.moveToNext();
                list[i] = c.getLong(0);
            }
            mAutoShuffleList = list;
            return true;
        } catch (RuntimeException ex) {
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return false;
    }
    
    /**
     * Removes the range of tracks specified from the play list. If a file within the range is
     * the file currently being played, playback will move to the next file after the
     * range. 
     * @param first The first file to be removed
     * @param last The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(int first, int last) {
    	Log.i(TAG, "<removeTracks> first:" + first + " last:" + last,Log.APP);
        /// M: if the removed music is the file that will played next,set the 
        // nextPlayer be null to avoid it will be played.@{
        if (mPlayer.isInitialized()) {
            mPlayer.setNextDataSource(null);
        }
        /// @}
        int numremoved = removeTracksInternal(first, last);
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        return numremoved;
    }
    
    private int removeTracksInternal(int first, int last) {
    	Log.i(TAG, "<removeTracksInternal> first:" + first + " last:" + last,Log.APP);
        synchronized (this) {
            if (last < first) return 0;
            if (first < 0) first = 0;
            if (last >= mPlayListLen) last = mPlayListLen - 1;

            boolean gotonext = false;
            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                gotonext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= (last - first + 1);
            }
            int num = mPlayListLen - last - 1;
            for (int i = 0; i < num; i++) {
                mPlayList[first + i] = mPlayList[last + 1 + i];
            }

            removeInShuffleMode(first, last);

            mPlayListLen -= last - first + 1;
            Log.i(TAG, "removeTracksInternal need goto gotoNext(" + gotonext + ")",Log.APP);
            if (gotonext) {
                if (mPlayListLen == 0) {
                    /// M: if the playlist be empty,clear the history to avoid these files can not
                    // be played when added to playlist nexttime.
                    mHistory.clear();
                    stop(true);
                    mPlayPos = -1;
                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                } else {
                    if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }
                    /// M: When delete the current play track, if Music is not playing, we should
                    /// set mQuietMode to be true to avoid Music began to play after prepare finish.
                    mQuietMode = !isPlaying();
                    stop(false);
                    openCurrentAndNext();
                }
                notifyChange(META_CHANGED);
            }
            Log.i(TAG, "removeTracksInternal end: mPlayListLen = " + mPlayListLen
                    + ", mPlayPos = " + mPlayPos,Log.APP);
            return last - first + 1;
        }
    }
    
    private void removeInShuffleMode(int first, int last) {
    	Log.i(TAG, "<removeInShuffleMode> first:" + first + " last:" + last,Log.APP);
        for (int i = last; i >= first; i--) {
            for (int j = mHistory.size() - 1; j >= 0; j--) {
                Integer value = mHistory.get(j);
                if (value > i) {
                    mHistory.set(j, value - 1);
                } else if (value == i) {
                    mHistory.remove(j);
                }
            }
        }
    }

    /**
     * Removes all instances of the track with the given id
     * from the playlist.
     * 
     * M: Remvoe these tracks by two steps, first remvoe these after current play position and then
     * remove these before current play position, so that can avoid music ANR causing by calling
     * too much setDataSourceAsync().
     * 
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    public int removeTrack(long id) {
    	Log.i(TAG, "<removeTrack> id:" + id,Log.APP);
        int numremoved = 0;
        /// M: if the deleted music is the file that will played next,set the 
        // nextPlayer be null to avoid it will be played.@{
        if (mPlayer.isInitialized()) {
            mPlayer.setNextDataSource(null);
        }
        /// @}
        synchronized (this) {
            /// M: 1. First remove all songs with given id after mPlayPos @{
            for (int i = mPlayListLen - 1; i > mPlayPos; i--) {
                if (mPlayList[i] == id) {
                    Log.i(TAG, "remove rewind(" + i + "),Play position is " + mPlayPos
                            + ",removed num = " + numremoved,Log.APP);
                    numremoved += removeTracksInternal(i, i);
                }
            }
            /// @}
            
            /// M: 2. Second remove others before mPlayPos. use length to keep circle times
            /// and the circle times must be not bigger than mPlayListLen
            int length = (mPlayPos < mPlayListLen) ? (mPlayPos + 1) : mPlayListLen;
            /// M: Current delete songs position, if don't remove, move to next
            int currentPos = 0;
            for (int i = 0; i < length; i++) {
                if (mPlayList[currentPos] == id) {
                    Log.i(TAG, "remove forward(" + currentPos + "),Play position is "
                            + mPlayPos + ",removed num = " + numremoved,Log.APP);
                    numremoved += removeTracksInternal(currentPos, currentPos);
                } else {
                    currentPos++;
                } 
            }
            /// @}
        }
        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }
        Log.i(TAG, "removeTrack<<<: removed num = " + numremoved,Log.APP);
        return numremoved;
    }
    
    public void setShuffleMode(int shufflemode) {
    	Log.i(TAG, "<setShuffleMode> shufflemode:" + shufflemode,Log.APP);
        synchronized(this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }
            mShuffleMode = shufflemode;
            /// M: when set party shuffle or shuffle all at repeat current, we should set repeat mode to 
            /// repeat all to avoid repeat current exist same with party shuffle and shuffle mode. {@
            if ((mRepeatMode == REPEAT_CURRENT) && (mShuffleMode != SHUFFLE_NONE)) {
                mRepeatMode = REPEAT_ALL;
            }
            /// @}
            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrentAndNext();
                    /// M: remove paly(), it will be called in the prepare callback
                    //play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    // failed to build a list of files to shuffle
                    mShuffleMode = SHUFFLE_NONE;
                }
            }
            saveQueue(false);
        }
        notifyChange(PLAY_MODE);
    }
    public int getShuffleMode() {
        return mShuffleMode;
    }
    
    public void setRepeatMode(int repeatmode) {
    	Log.i(TAG, "<setRepeatMode> repeatmode:" + repeatmode,Log.APP);
        synchronized(this) {
            mRepeatMode = repeatmode;
            setNextTrack();
            saveQueue(false);
        }
        notifyChange(PLAY_MODE);
    }
    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    /**
     * Returns the path of the currently playing file, or null if
     * no file is currently playing.
     */
    public String getPath() {
        return mFileToPlay;
    }
    
    /**
     * Returns the rowid of the currently playing file, or -1 if
     * no file is currently playing.
     */
    public long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0 && mPlayer.isInitialized()) {
                return mPlayList[mPlayPos];
            }
        }
        return -1;
    }
    
    /**
     * Returns the position in the queue 
     * @return the position in the queue
     */
    public int getQueuePosition() {
        synchronized(this) {
            return mPlayPos;
        }
    }
    
    /**
     * Starts playing the track at the given position in the queue.
     * @param pos The position in the queue of the track that will be played.
     */
    public void setQueuePosition(int pos) {
    	Log.i(TAG, "<setQueuePosition> pos:" + pos,Log.APP);
        synchronized(this) {
            stop(false);
            mPlayPos = pos;
            openCurrentAndNext();
            play();
            notifyChange(META_CHANGED);
            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    public String getArtistName() {
        synchronized(this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
        }
    }
    
    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
        }
    }

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }
            return mCursor.getLong(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }

    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        }
    }

    private boolean isPodcast() {
        synchronized (this) {
            if (mCursor == null) {
                return false;
            }
            return (mCursor.getInt(PODCASTCOLIDX) > 0);
        }
    }
    
    private long getBookmark() {
        synchronized (this) {
            if (mCursor == null) {
                return 0;
            }
            return mCursor.getLong(BOOKMARKCOLIDX);
        }
    }
    
    /**
     * Returns the duration of the file in milliseconds.
     * Currently this method returns -1 for the duration of MIDI files.
     * M: record the duraton for performance
     *     get the duraton from database
     */
    public long duration() {
        if (mDurationOverride != -1) {
            Log.i(TAG, "<duration> duration from override is " + mDurationOverride,Log.APP);
            return mDurationOverride;
        }
        synchronized(this) {
            if (mCursor != null) {
                int durationColIdx = mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                if (!mCursor.isNull(durationColIdx)) {
                    Log.i(TAG, "<duration> duration from database is " + mCursor.getLong(durationColIdx),Log.APP);
                    return mCursor.getLong(durationColIdx);
                }
            }
        }
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            mDurationOverride = mPlayer.duration();
            Log.i(TAG, "<duration> duration from MediaPlayer is " + mDurationOverride,Log.APP);
            return mDurationOverride;
        }
        return 0;
    }

    /**
     * Returns the current playback position in milliseconds
     */
    public long position() {
        /// M: add player ready check
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            long position = mPlayer.position();
            Log.i(TAG, "<position> Position:" + position,Log.APP);
            return position;
        }
        return -1;
    }

    /**
     * Seeks to the position specified.
     *
     * @param pos The position to seek to, in milliseconds
     *  M: add player ready check and seekable check 
     */
    public long seek(long pos) {
        Log.i(TAG, "<seek> pos" + pos,Log.APP);
        if (mPlayer.isInitialized() && mIsPlayerReady) {
            if (pos != 0 && !(mMediaSeekable && mediaCanSeek())) {
                Log.e(TAG, "seek, sorry, seek is not supported",Log.APP);
                return -1;
            }
            if (pos < 0) pos = 0;
            
            final long d = mPlayer.duration();
            if (pos >= d) {
                pos = d;
            } else {
                /// M: Avoid confusion of seeking after playlist completed
                mIsPlaylistCompleted = false;
            }
            
            return mPlayer.seek(pos);
        }
        return -1;
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId: the audio session ID.
     */
    public void setAudioSessionId(int sessionId) {
    	Log.i(TAG, "<setAudioSessionId> sessionId:" + sessionId,Log.APP);
        synchronized (this) {
            mPlayer.setAudioSessionId(sessionId);
        }
    }

    /**
     * Returns the audio session ID.
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }
    
    public Bitmap getPlayingPicture(){
    	return MusicUtils.getListArtwork(this, getAudioId(), getAlbumId(),true);
    }
    
    /**
     * Provides a unified interface for dealing with midi files and
     * other media files.
     */
    private class MultiPlayer {
        private CompatMediaPlayer mCurrentMediaPlayer = new CompatMediaPlayer();
        private CompatMediaPlayer mNextMediaPlayer;
        private Handler mHandler;
        private boolean mIsInitialized = false;

        public MultiPlayer() {
            mCurrentMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSource(String path) {
            Log.i(TAG, "<setDataSource> path:" + path,Log.APP);
            /// M:  default setDataSource don't use async mode
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path, false);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }
        
        /**
         * M: add async prepare to aviod ANR, add information and duration update listener
         * 
         * @param player The mediaplayer
         * @param path The data source path
         * @param async M: use async prepare if it's set true .
         * @return If set data source success, return true, otherwise false.
         */
        private boolean setDataSourceImpl(MediaPlayer player, String path, boolean async) {
            Log.i(TAG, "<setDataSourceImpl> path:" + path + " async:" + async,Log.APP);
            try {
                player.reset();
                if (async) {
                    player.setOnPreparedListener(preparedlistener);
                } else {
                    player.setOnPreparedListener(null);
                }
                if (path.startsWith("content://")) {
                    player.setDataSource(MediaPlaybackService.this, Uri.parse(path));
                } else {
					player.setDataSource(path);
                }
                /// M:  Attach auxiliary audio effect only with valid effect id
                if (mAuxEffectId > 0) {
                    player.attachAuxEffect(mAuxEffectId);
                    player.setAuxEffectSendLevel(1.0f);
                    mWhetherAttachWhenPause = false;
                    Log.i(TAG, "setDataSourceImpl: attachAuxEffect mAuxEffectId = " + mAuxEffectId,Log.APP);
                }
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (async) {
                    player.prepareAsync();
                } else {
                    player.prepare();
                }
            } catch (IOException ex) {
                // TODO: notify the user why the file couldn't be opened
                Log.e(TAG, "setDataSourceImpl: " + ex,Log.APP);
                return false;
            } catch (IllegalArgumentException ex) {
                // TODO: notify the user why the file couldn't be opened
                Log.e(TAG, "setDataSourceImpl: " + ex,Log.APP);
                return false;
            } catch (IllegalStateException ex) {
                Log.e(TAG, "setDataSourceImpl: " + ex,Log.APP);
                return false;
            }
            player.setOnCompletionListener(listener);
            player.setOnErrorListener(errorListener);
            player.setOnInfoListener(infoListener);
            //update by zjw
//            player.setOnDurationUpdateListener(durationListener);
            sendSessionIdToAudioEffect(false);
            return true;
        }

        @SuppressLint("NewApi")
		public void setNextDataSource(String path) {
            Log.i(TAG, "<setNextDataSource> path:" + path + ", mNextPlayPos:" + mNextPlayPos,Log.APP);
            mCurrentMediaPlayer.setNextMediaPlayer(null);
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
            if (path == null) {
                return;
            }

            /// M: Check the next play audio duration and type to avoid some problem, first too short (<5s) audio should 
            /// not set to be next player, second too large AMR audio (>1hour) should not set to be next player, third drm
            /// audio should not to set to be next player too. {@
            Cursor cursor = getCursorForId(mPlayList[mNextPlayPos]);
            if (cursor == null) {
                Log.w(TAG, "setNextDataSource with null cursor!",Log.APP);
                return;
            }
            /// M: Get duration from database instead get from mediaplayer to avoid ANR because some track will spell long
            /// time to parse and only when prepare finished the native can return the duration to music.
            long nextPlayerDuration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            //update by zjw
            int isDrm = 0;
            if(DrmUtils.isSupportDrm())
            {
            	isDrm = cursor.getInt(cursor.getColumnIndexOrThrow("is_drm"));
            }
            Log.i(TAG, "setNextDataSource: database duration = " + nextPlayerDuration
                    + ", data = " + data + ", isDrm = " + isDrm,Log.APP);
            cursor.close();
            cursor = null;

            /// M: Discard to set next player when duration is less than 5s
            final int discardNextPlayerTimeForShortAudio = 5000;
            if (nextPlayerDuration < discardNextPlayerTimeForShortAudio) {
                Log.w(TAG, "Discard setNextDataSource because the audio is so short.",Log.APP);
                return;
            }
            
            /// M: Discard to set next player when current song duration is less than 1s
            if (mCurrentMediaPlayer.getDuration() < 1000) {
                Log.w(TAG, "Discard setNextDataSource because the current audio is so short.",Log.APP);
                return;
            }

            /// M: Discard to set next player when the duration is too long with AMR type, because AMR type audio
            /// will set data source with same thread in native, when the AMR audio is very long (such as 3 hours
            /// long), it will spend 3 second to set data source and will block the main thread, then the playback ui
            /// can not refresh in time. So the sound will output fisrt and play audio info update to screen late.
            final int discardNextPlayerTimeForLongAmr = 3600000; /// 1 hour
            final String amrSuffix = ".amr";
            if (data != null && data.endsWith(amrSuffix) && nextPlayerDuration > discardNextPlayerTimeForLongAmr) {
                Log.w(TAG, "Discard setNextDataSource because the amr file is too long.",Log.APP);
                return;
            }

            /// M: DRM audio should not set to be next player, because it may consume user right.
            if (isDrm == 1) {
                Log.w(TAG, "Discard setNextDataSource because the audio is drm.",Log.APP);
                return;
            }
            /// @}

            mNextMediaPlayer = new CompatMediaPlayer();
            mNextMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            /// M: setNextDataSource use async mode too to avoid block prepare method in native
            if (setDataSourceImpl(mNextMediaPlayer, path, true)) {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
                /// M: if the current volume is min fade down volume, set next player with this volume
                final float minFadeDownVolume = .2f;
                if (Math.abs(mCurrentVolume - minFadeDownVolume) < 0.001) {
                    mNextMediaPlayer.setVolume(mCurrentVolume, mCurrentVolume);
                    Log.e(TAG, "set next player volume to " + mCurrentVolume,Log.APP);
                }
            } else {
                // failed to open next, we'll transition the old fashioned way,
                // which will skip over the faulty file
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
        }
        
        public boolean isInitialized() {
            return mIsInitialized;
        }

        public void start() {
        	Log.i(TAG, "<start>",Log.APP);
            mCurrentMediaPlayer.start();
        }

        public void stop() {
        	Log.i(TAG, "<stop>",Log.APP);
            mCurrentMediaPlayer.reset();
            mIsInitialized = false;
        }

        /**
         * You CANNOT use this player anymore after calling release()
         */
        public void release() {
        	Log.i(TAG, "<release>",Log.APP);
            stop();
            mCurrentMediaPlayer.release();
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
        }
        
        public void pause() {
        	Log.i(TAG, "<pause>",Log.APP);
            mCurrentMediaPlayer.pause();
        }
        
        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
                    Log.i(TAG, "<onCompletion>: Auto switch to Next MediaPlayer and add played track to history.",Log.APP);
                    /// M: move from getNextPosition() to add played track to history only when call
                    /// gotoNext or current play complete and auto switch to next player.
                    addPlayedTrackToHistory(false);
                    /**M: Delay the mCurrentMediaPlayer to release, to avoid the mCurrentMediaPlayer 
                          has be released while native error callback invoked.@**/
                    SystemClock.sleep(100);
                    /**@}**/
                    mCurrentMediaPlayer.release();
                    mCurrentMediaPlayer = mNextMediaPlayer;
                    mNextMediaPlayer = null;
                    if (mNextPlayPos != -1) {
                        Log.i(TAG, "onCompletion: mNextPlayPos is " + mNextPlayPos,Log.APP);
                        mHandler.sendMessage(mHandler.obtainMessage(TRACK_WENT_TO_NEXT, mNextPlayPos, -1));
                    }
                } else {
                    /// M:  set track complete and pre audio id
                    Log.i(TAG, "<onCompletion> send Track End",Log.APP);
                    mTrackCompleted = true;
                    mPreAudioId = getAudioId();
                    // Acquire a temporary wakelock, since when we return from
                    // this callback the MediaPlayer will release its wakelock
                    // and allow the device to go to sleep.
                    // This temp wakelock ensure MediaPlayer can acquire its wakelock if playlist
                    // is not finished. 
                    /// M: modify for bug fixed @{
                    final int acquireWakeLockTime = 3000;
                    mWakeLock.acquire(acquireWakeLockTime);
                    /// M:To deal with the situation that the song is closing to end,at the same time user do pause(),
                    // Before MediaPlayer is really paused,the MediaPlayer return onComplete.So Music can't be paused.
                    Log.i(TAG, "mIsSupposedToBePlaying:" + mIsSupposedToBePlaying,Log.APP);
                    if (mIsSupposedToBePlaying) {
                        mHandler.sendEmptyMessage(TRACK_ENDED);
                    }
                    /// @}
                    //mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
                    /// @}
                }
            }
        };

        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    Log.i(TAG, "<onError> MEDIA_ERROR_SERVER_DIED",Log.APP);
                    /// M: if media server died, notify Musicfx first.
                    sendSessionIdToAudioEffect(false);
                    mIsInitialized = false;
                    mCurrentMediaPlayer.release();
                    // Creating a new MediaPlayer and settings its wakemode does not
                    // require the media service, so it's OK to do this now, while the
                    // service is still being restarted
                    mCurrentMediaPlayer = new CompatMediaPlayer(); 
                    mCurrentMediaPlayer.setWakeMode(MediaPlaybackService.this, PowerManager.PARTIAL_WAKE_LOCK);
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(SERVER_DIED), 2000);
                    return true;
                /// M: Add for CR ALPS00238519
                // If MusicFX has been killed when Music has been playing with Audio Effect,Music
                // can not attach the reverb effect id to mediaplayer because all the reverb effect
                // source have been released. We handle the error message in mediaplayer error listenner.
                // Set the reverb effect id mAuxEffectId to 0 and send broadcast to open audio effect again,
                // then open the current song and play again  @{
                case MEDIA_ERROR_MUSICFX_DIED:
                    Log.i(TAG, "<onError> MEDIA_ERROR_MUSICFX_DIED, extra = " + extra + ", mPlayPos = " + mPlayPos,Log.APP);
                    mAuxEffectId = 0;
                    /// M: When native send the MEDIA_ERROR_MUSICFX_DIED error, we need to reset preset reverb effect.
                    sendSessionIdToAudioEffect(true);
                    /// M: Only when the position is valid we need to reopen the same song, because when open failed
                    /// we will clear the play list and position.
                    if (mPlayPos >= 0) {
			/// M: Remove to verify ALPS00654722
                        //if (mHandler.hasMessages(TRACK_WENT_TO_NEXT)) {
                        //    mHandler.removeMessages(TRACK_WENT_TO_NEXT);
                        //}
			/// @}
                        openCurrentAndNext();
                    }
                    return true;
                /// @}                
                /// M: We should have used defined  error codes as constants and handle them by 'case'.
                //  There are some constants in android.media.MediaPlayer but not enough. Because
                //  framework will return many error codes without pre-defined and there are also
                //  some operation status from JNI returned as error. As a result, we have to make
                //  a decision here: 
                //     1. For operation status INVALID_OPERATION whose code is -38, do nothing
                //       For every call to MediaPlayer, it will check its state, if the requesting 
                //       operation is not allowed in the current state, it does nothing and return
                //       INVALID_OPERATION to JNI. JNI will notify operation status to upper layer
                //       as a error with MEDIA_ERROR, which, as a result, causes onError is called.
                //       Because MediaPlayer has protected itself from this operation, we have no
                //       reason to shutdown the player.
                //     2. For other codes, notify user and shutdown player.
                default:
                    Log.i(TAG, "onError: what=" + what + ", extra=" + extra,Log.APP);
                    if (what != -38) {
                        handlePlaySongFail(mp);
                    }
                    return true;
                /// @}
                }
           }
        };

        public long duration() {
            return mCurrentMediaPlayer.getDuration();
        }

        public long position() {
            return mCurrentMediaPlayer.getCurrentPosition();
        }

        public long seek(long whereto) {
        	Log.i(TAG, "<seek> whereto:" + whereto,Log.APP);
            mCurrentMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        public void setVolume(float vol) {
        	Log.i(TAG, "<setVolume> vol:" + vol,Log.APP);
            mCurrentMediaPlayer.setVolume(vol, vol);
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.setVolume(vol, vol);
            }
        }

        public void setAudioSessionId(int sessionId) {
            mCurrentMediaPlayer.setAudioSessionId(sessionId);
        }

        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        /**
         * M: set date source and use async prepare to aviod ANR
         * 
         * @param path Data source path
         */
        public void setDataSourceAsync(String path) {
            Log.i(TAG, "<setDataSourceAsync> path:" + path,Log.APP);
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path, true);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        /**
         * M: return the current media player playing status
         * 
         * @return If is playing, return true, otherwise false
         */
        public boolean isPlaying() {
            return mCurrentMediaPlayer.isPlaying();
        }

        /**
         * M: attatch auxdio effect for Auxiliary audio effect 
         * 
         * @param effectId: the effect id.
         */
        public void attachAuxEffect(int effectId) {
        	Log.i(TAG, "<attachAuxEffect> effectId:" + effectId,Log.APP);
            mCurrentMediaPlayer.attachAuxEffect(effectId);
        }

        /**
         * M: set audio effect send level for Auxiliary audio effect 
         * 
         * @param level: the auxiliary effect level.
         */
        public void setAuxEffectSendLevel(float level) {
        	Log.i(TAG, "<setAuxEffectSendLevel> level:" + level,Log.APP);
            mCurrentMediaPlayer.setAuxEffectSendLevel(level);
        }

        /**
         * M: handle the play fail with error or non-support 
         * Try to jump to next song in the playlist, if there are any
         * 
         * @param mediaPlayer the error happen media player.
         */
        private void handlePlaySongFail(MediaPlayer mediaPlayer) {
        	Log.i(TAG, "<handlePlaySongFail>",Log.APP);
            /// M: If set next player onError, we should ignore this error and clear next player.Music will check
            /// the error audio when begin to play it at last song onComplete.
            if (mNextMediaPlayer != null && mNextMediaPlayer.equals(mediaPlayer) && mPlayer.isInitialized()) {
                mPlayer.setNextDataSource(null);
                Log.e(TAG, "handlePlaySongFail: set next player onError, clear next player and return!",Log.APP);
                return;
            }
            if (mOpenFailedCounter++ < OPEN_FAILED_MAX_COUNT && mPlayListLen > 1 && 
                    (mPlayPos < mPlayListLen - 1 || mRepeatMode == REPEAT_ALL)) {
                gotoNext(false);
            } else if (mPlayPos >= mPlayListLen - 1) {
                MediaPlaybackService.this.stop(true);
                notifyChange(QUIT_PLAYBACK);
            } else {
                MediaPlaybackService.this.stop(true);
            }
            if (mOpenFailedCounter != 0) {
                /// M:  need to make sure we only shows this once
                mOpenFailedCounter = 0;
                /// M:  Notify the user
                if (!mQuietMode) {
                    showToast(getString(R.string.fail_to_start_stream));
                }
                mQuietMode = false;
            }
        }

        /**
         * M: implement the listener to get the information about seek 
         * and suport
         */
        MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mediaPlayer, int what, int msg) {
                if (mediaPlayer == null) {
                    Log.e(TAG, "onInfo with null media player!",Log.APP);
                    return false;
                }
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    	Log.i(TAG, "<OnInfoListener.onInfo> MEDIA_INFO_NOT_SEEKABLE",Log.APP);
                        if (mediaPlayer.equals(mCurrentMediaPlayer)) {
                            mMediaSeekable = false;
                        } else {
                            mNextMediaSeekable = false;
                        }
                        Log.w(TAG, "onInfo, Disable the seeking for this media",Log.APP);
                        return true;
                     //update by zjw
                     //MediaPlayer.MEDIA_INFO_SEEKABLE --> 804
                    case 804:
                    	Log.i(TAG, "<OnInfoListener.onInfo> MEDIA_INFO_SEEKABLE",Log.APP);
                        if (mediaPlayer.equals(mCurrentMediaPlayer)) {
                            mMediaSeekable = true;
                        } else {
                            mNextMediaSeekable = true;
                        }
                        Log.i(TAG, "onInfo, current track is seekable now!",Log.APP);
                        return true;
                      //update by zjw
                      //MediaPlayer.MEDIA_INFO_AUDIO_NOT_SUPPORTED --> 862   
                    case 862:
                        Log.i(TAG, "<OnInfoListener.onInfo> MEDIA_INFO_AUDIO_NOT_SUPPORTED, Don't support the audio Format!",Log.APP);
                        handlePlaySongFail(mediaPlayer);
                        return true;
                        
                   default:
                        break;
                }
                return false;
            }
        };

        /**
         * M: implement the listener to update duration to mediaProvider
         */
        //update by zjw
//        MediaPlayer.OnDurationUpdateListener durationListener = new MediaPlayer.OnDurationUpdateListener() {
//            public void onDurationUpdate(MediaPlayer mediaPlayer, int duration) {
//                if (mediaPlayer == null || duration <= 0) {
//                    MusicLogUtils.e(TAG, "onDurationUpdate with null media player or 0 duration!");
//                    return;
//                }
//                MusicLogUtils.i(TAG, "<onDurationUpdate> duration:" + duration);
//                if (mIsPlayerReady) {
//                    /// M: workaround for duration update issue
//                    mediaPlayer.getDuration();
//                }
//                int position = (mediaPlayer.equals(mCurrentMediaPlayer) ? mPlayPos : mNextPlayPos);
//                long currentTrackId = position >= 0 ? mPlayList[position] : -1;
//                if (currentTrackId < 0 || mCursor == null) {
//                    /// M: unknown track, so return directly
//                    MusicLogUtils.e(TAG, "onDurationUpdate with unknown track!");
//                    return;
//                }
//                int oldDuration = mCursor.getInt(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
//                if (!mediaPlayer.equals(mCurrentMediaPlayer)) {
//                    Cursor c = getCursorForId(mPlayList[position]);
//                    if (c == null) {
//                        MusicLogUtils.e(TAG, "onDurationUpdate: Next Player track not found!");
//                        return;
//                    }
//                    oldDuration = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
//                    c.close();
//                    c = null;
//                }
//                
//                if (oldDuration != duration) {
//                    ContentValues value = new ContentValues(1);
//                    MusicLogUtils.i(TAG, "Old Duration is " + oldDuration);
//                    value.put(MediaStore.Audio.Media.DURATION, duration);
//                    /**
//                     * M: Changed to catch the potential exception while remove the SD card. @{
//                     */
//                    try {
//                        getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                                value, MediaStore.Audio.Media._ID + " = " + currentTrackId, null);
//                        /// M: notify the playlist cursor to change the duration display accordingly
//                        getContentResolver().notifyChange(MediaStore.Audio.Playlists.getContentUri("external"), null);
//                        MusicLogUtils.i(TAG, "duration updated to DB with new duration " + duration);
//                    } catch (UnsupportedOperationException uoe) {
//                        MusicLogUtils.i(TAG, "UnsupportedOperationException while update new duration");
//                    } catch (IllegalStateException ise) {
//                        MusicLogUtils.i(TAG, "IllegalStateException while update new duration");
//                    } finally {
//                        /// M:  re-query for mCursor
//                        if (mediaPlayer.equals(mCurrentMediaPlayer)) {
//                            if (mCursor != null) {
//                                mCursor.close();
//                                mCursor = null;
//                            }
//                            synchronized (MediaPlaybackService.this) {
//                                mCursor = getCursorForId(mPlayList[position]);
//                            }
//                        }
//                    }
//                    /**
//                     * @}
//                     */
//                }
//            }
//        };

        /**
         * M: implement the listener to play the prepared audio, if needed
         */
        MediaPlayer.OnPreparedListener preparedlistener = new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mp) {
                synchronized (MediaPlaybackService.this) {
                    /// M: Only when current player prepared need to set next track.
                    if (!mp.equals(mCurrentMediaPlayer)) {
                        Log.w(TAG, "preparedlistener finish for next player!",Log.APP);
                        return;
                    }
                    Log.i(TAG, ">>> onPrepared doseek=" + mDoSeekWhenPrepared
                            + ", mediaseekable=" + mMediaSeekable + ", quietmode=" + mQuietMode,Log.APP);
                    mIsPlayerReady = true;
                    if (duration() == 0) {
                        Log.e(TAG, "onPrepared, bad media: duration is 0",Log.APP);
                        final boolean old = mQuietMode;
                        if (mShuffleMode == SHUFFLE_NONE && mRepeatMode != REPEAT_ALL
                                && !mDoSeekWhenPrepared && mPlayPos >= mPlayListLen - 1) {
                            Toast.makeText(MediaPlaybackService.this, R.string.fail_to_start_stream,
                                    Toast.LENGTH_SHORT).show();
                        }
                        
                        mQuietMode = true;
                        errorListener.onError(mp, 0, 0);
                        mQuietMode = old;
                        Log.i(TAG, "onPrepared >>>, bad media..",Log.APP);
                        return;
                    }
                    if (mDoSeekWhenPrepared && mMediaSeekable) {
                        long seekpos = mPreferences.getLong("seekpos", 0);
                        /// M:  AVRCP and Android Music AP supports the FF/REWIND
                        if (mSeekPositionForAnotherSong != 0) {
                            seekpos = mSeekPositionForAnotherSong;
                            mSeekPositionForAnotherSong = 0;
                        }
                        Log.i(TAG, "seekpos=" + seekpos,Log.APP); 
                        seek(seekpos >= 0 && seekpos <= duration() ? seekpos : 0);
                        Log.i(TAG, "restored queue, currently at position "
                                + position() + "/" + duration()
                                + " (requested " + seekpos + ")",Log.APP);
                        mDoSeekWhenPrepared = false;
                    } else if (!mMediaSeekable) {
                        Log.e(TAG, "onPrepared: media NOT seekable, so skip seek!",Log.APP);
                        mDoSeekWhenPrepared = false;
                    }
                    
                    /// M: After prepared,seek to bookmark if needed.
                    if (isPodcast()) {
                        long bookmark = getBookmark();
                        // Start playing a little bit before the bookmark,
                        // so it's easier to get back in to the narrative.
                        if (bookmark > 5000) {
                            seek(bookmark - 5000);
                        }
                        Log.i(TAG, "onPrepared: seek to bookmark: " + bookmark,Log.APP);
                    }
                    
                    /// M: We should not call play if detonate from reloadQueue() or delete current
                    /// paused track, and let music can be played at next time detonated.@{
                    if (!mQuietMode) {
                        play();
                        notifyChange(META_CHANGED);
                    } else {
                        mQuietMode = false;
                    }
                    /// @}
                    setNextTrack();
                    Log.i(TAG, "onPrepared >>> mQuietMode:" + mQuietMode,Log.APP);
                }
            }
        };
    }

    static class CompatMediaPlayer extends MediaPlayer implements OnCompletionListener {

        private boolean mCompatMode = true;
        private MediaPlayer mNextPlayer;
        private OnCompletionListener mCompletion;

        public CompatMediaPlayer() {
            try {
                MediaPlayer.class.getMethod("setNextMediaPlayer", MediaPlayer.class);
                mCompatMode = false;
            } catch (NoSuchMethodException e) {
                mCompatMode = true;
                super.setOnCompletionListener(this);
            }
        }

        @SuppressLint("NewApi")
		public void setNextMediaPlayer(MediaPlayer next) {
            if (mCompatMode) {
                mNextPlayer = next;
            } else {
                super.setNextMediaPlayer(next);
            }
        }

        @Override
        public void setOnCompletionListener(OnCompletionListener listener) {
            if (mCompatMode) {
                mCompletion = listener;
            } else {
                super.setOnCompletionListener(listener);
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            if (mNextPlayer != null) {
                // as it turns out, starting a new MediaPlayer on the completion
                // of a previous player ends up slightly overlapping the two
                // playbacks, so slightly delaying the start of the next player
                // gives a better user experience
                SystemClock.sleep(50);
                mNextPlayer.start();
            }
            mCompletion.onCompletion(this);
        }
    }

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still
     * has a remote reference to the stub.
     * 
     * M: Use softreference instead weakreference to avoid JE when reference has been GCd
     */
    static class ServiceStub extends IMediaPlaybackService.Stub {
        SoftReference<MediaPlaybackService> mService;
        
        ServiceStub(MediaPlaybackService service) {
            mService = new SoftReference<MediaPlaybackService>(service);
        }

        public void openFile(String path)
        {
            mService.get().open(path);
        }
        public void open(long [] list, int position) {
            mService.get().open(list, position);
        }
        public int getQueuePosition() {
            return mService.get().getQueuePosition();
        }
        public void setQueuePosition(int index) {
            mService.get().setQueuePosition(index);
        }
        public boolean isPlaying() {
            return mService.get().isPlaying();
        }
        public void stop() {
            mService.get().stop();
        }
        public void pause() {
            mService.get().pause();
        }
        public void play() {
            mService.get().play();
        }
        public void prev() {
            mService.get().prev();
        }
        public void next() {
            mService.get().gotoNext(true);
        }
        public String getTrackName() {
            return mService.get().getTrackName();
        }
        public String getAlbumName() {
            return mService.get().getAlbumName();
        }
        public long getAlbumId() {
            return mService.get().getAlbumId();
        }
        public String getArtistName() {
            return mService.get().getArtistName();
        }
        public long getArtistId() {
            return mService.get().getArtistId();
        }
        public void enqueue(long [] list , int action) {
            mService.get().enqueue(list, action);
        }
        public long [] getQueue() {
            return mService.get().getQueue();
        }
        public void moveQueueItem(int from, int to) {
            mService.get().moveQueueItem(from, to);
        }
        public String getPath() {
            return mService.get().getPath();
        }
        public long getAudioId() {
            return mService.get().getAudioId();
        }
        public long position() {
            return mService.get().position();
        }
        public long duration() {
            return mService.get().duration();
        }
        public long seek(long pos) {
            return mService.get().seek(pos);
        }
        public void setShuffleMode(int shufflemode) {
            mService.get().setShuffleMode(shufflemode);
        }
        public int getShuffleMode() {
            return mService.get().getShuffleMode();
        }
        public int removeTracks(int first, int last) {
            return mService.get().removeTracks(first, last);
        }
        public int removeTrack(long id) {
            return mService.get().removeTrack(id);
        }
        public void setRepeatMode(int repeatmode) {
            mService.get().setRepeatMode(repeatmode);
        }
        public int getRepeatMode() {
            return mService.get().getRepeatMode();
        }
        public int getMediaMountedCount() {
            return mService.get().getMediaMountedCount();
        }
        public int getAudioSessionId() {
            return mService.get().getAudioSessionId();
        }
        public String getMIMEType() {
            return mService.get().getMIMEType();
        }
        public boolean canUseAsRingtone() {
            return mService.get().canUseAsRingtone();
        }

		@Override
		public Map<Long,Long> getAlbumArt() throws RemoteException {
			return mService.get().getAlbumArt();
		}
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.println("" + mPlayListLen + " items in queue, currently at index " + mPlayPos);
        writer.println("Currently loaded:");
        writer.println(getArtistName());
        writer.println(getAlbumName());
        writer.println(getTrackName());
        writer.println(getPath());
        writer.println("playing: " + mIsSupposedToBePlaying);
        writer.println("actual: " + mPlayer.mCurrentMediaPlayer.isPlaying());
        writer.println("shuffle mode: " + mShuffleMode);
        MusicUtils.debugDump(writer);
    }

    private final IBinder mBinder = new ServiceStub(this);

    /// M: AVRCP
    private final ServiceAvrcpStub mBinderAvrcp = new ServiceAvrcpStub(this);

    /**
     * M: Returns the cursor status for AVRCP
     * 
     * @return If cursor is null, return true, otherwise false
     */
    public boolean isCursorNull() {
        return mCursor == null;
    }

    /**
     * M: notify meta, queue and playstate change for BT AVRCP
     * 
     * @param s notify message.
     */
    public void notifyBTAvrcp(String s) {
        mBinderAvrcp.notifyBTAvrcp(s);
    }

    /**
     * M: the class use to get the album art for notification
     */
    private class AlbumArtWorker extends AsyncTask<Long, Void, Bitmap> {
        /**
         * M: get the album art
         * 
         * @param albumId The album id
         * @return Return the album art bitmap 
         */
        protected Bitmap doInBackground(Long... albumId) {
            Bitmap bm = null;
            try {
                long id = albumId[0].longValue();
                bm = MusicUtils.getArtwork(MediaPlaybackService.this, -1, id, true);
                /// M: for special file whose decode data is null
                if (bm == null) {
                    bm = MusicUtils.getDefaultArtwork(MediaPlaybackService.this);
                }
            } catch (IllegalArgumentException ex) {
                Log.e(TAG, "AlbumArtWorker called with wrong parameters",Log.APP);
                return null;
            }
            Log.i(TAG, "AlbumArtWorker: getArtwork returns " + bm,Log.APP);
            return bm;
        }

        /**
         * M: update the notification if got the bitmap
         * 
         * @param bm album art bitmap
         */
        protected void onPostExecute(Bitmap bm) {
            Log.i(TAG, ">>> AlbumArtWorker.onPostExecute",Log.APP);
            if (mIsSupposedToBePlaying) {
                updateNotification(MediaPlaybackService.this, bm);
            }
            Log.i(TAG, "AlbumArtWorker.onPostExecute >>>",Log.APP);
        }
    }

    /**
     * M: AVRCP and Android Music AP supports the FF/REWIND
     * the method will make rewind fastly
     * 
     * @param delta Fast backward scan lenth
     */
    private void scanBackward(long delta) {
        long startSeekPos = position();
        long scanDelta = 0;
        Log.i(TAG,">>> scanBackward startSeekPos:" + startSeekPos,Log.APP);
        if (delta < POSITION_FOR_SPEED_FAST) {
            /// M: seek at 10x speed for the first 5 seconds
            scanDelta = delta * SPEED_NORMAL; 
        } else {
            /// M: seek at 40x after that
            scanDelta = POSITION_FOR_SPEED_FAST + (delta - POSITION_FOR_SPEED_FAST) * SPEED_FAST;
        }
        long newpos = startSeekPos - scanDelta;
        if (newpos < 0) {
            /// M: move to previous track
            prev();
            long duration = duration();
            Log.i(TAG,"duration: " + duration,Log.APP);
            newpos += duration;

            if (newpos > 0) {
                mDoSeekWhenPrepared = true;
                mSeekPositionForAnotherSong = newpos;
            }
        } else {
            seek(newpos);
        }
        Log.i(TAG, "scanBackward >>>", Log.APP);
    }

    /**
     * M: AVRCP and Android Music AP supports the FF/REWIND
     * the method will make the palying song fast forword
     * 
     * @param delta Fast forward scan lenth
     */
    private void scanForward(long delta) {
        long scanDelta = 0;
        long startSeekPos = position();
        if (delta < POSITION_FOR_SPEED_FAST) {
            /// M: seek at 10x speed for the first 5 seconds
            scanDelta = delta * SPEED_NORMAL; 
        } else {
            /// M: seek at 40x after that
            scanDelta = POSITION_FOR_SPEED_FAST + (delta - POSITION_FOR_SPEED_FAST) * SPEED_FAST;
        }
        long newpos = startSeekPos + scanDelta;
        long duration = duration();
        if (newpos >= duration) {
            /// M: move to next track
            gotoNext(true);
            newpos -= duration;
            duration = duration();
            if (newpos > duration) {
                newpos = duration;
            }
            mDoSeekWhenPrepared = true;
            mSeekPositionForAnotherSong = newpos;
        } else {
            seek(newpos);
        }
        
        Log.i(TAG,"<scanForward> newpos:" + newpos,Log.APP);
    }

    /**
     * M: Return false if the current playing file is endless imy
     * use to aviod endless imy seek fail issue
     * 
     * @return If can seek, return true, otherwise false
     */
    private boolean mediaCanSeek() {
    	Log.i(TAG,"<mediaCanSeek>",Log.APP);
        synchronized (this) {
            if (mCursor == null) {
                return true;
            }
            final String path = mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
            final long maxDuration = 0x7fffffffL;
            final String imySuffix = ".imy";
            return (path != null && !(path.toLowerCase(Locale.ENGLISH).endsWith(imySuffix) && duration() == maxDuration));
        }
    }

    /**
     * M: send audio session id to Audio Effect App
     * @param resetReverb Whether need to reset the preset reverb effect, because when native reverb effect died, music
     * want to attach to it will cause error.
     */
    private void sendSessionIdToAudioEffect(boolean resetReverb) {
    	Log.i(TAG,"<sendSessionIdToAudioEffect> resetReverb:" + resetReverb,Log.APP);
        Intent i = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        i.putExtra("reset_reverb", resetReverb);
        sendBroadcast(i);
    }

    /**
     * M: Return the MIME type of the current playing song
     * 
     * @return Return mime type in string
     */
    public String getMIMEType() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
        }
    }

    /**
     * M: Check the current audio whether can use as ringtone.
     * 
     * @return When the current audio is drm and it's method is not FL, return false, otherwise return true.
     */
    public boolean canUseAsRingtone() {
        synchronized (this) {
            return isDrmCanPlay(mCursor);
        }
    }
    
    public Map<Long,Long> getAlbumArt()
    {
    	if(mPlayListLen == 0 || mPlayList == null)
    	{
    		return null;
    	}
    	Map<Long,Long> albumSongIdMap = null;
    	Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    	StringBuffer sb = new StringBuffer();
    	sb.append("_id in(");
    	for(int i = 0;i < mPlayListLen;i++)
    	{
    		sb.append(mPlayList[i]);
    		if(i != (mPlayListLen - 1))
    		{
    			sb.append(",");
    		}
    	}
    	sb.append(")");
    	Cursor cursor = 
    			MusicUtils.query(
    					MediaPlaybackService.this, 
    					uri, mCursorCols, 
    					sb.toString(), 
    					null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    	if(cursor != null)
    	{
    		try {
                int len = cursor.getCount();
                if(len != 0)
                {
                	albumSongIdMap = new HashMap<Long, Long>();
                    for (int i = 0; i < len; i++) {
                        cursor.moveToNext();
                        albumSongIdMap.put(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)), 
                        		cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
                    }
                }
            } catch (SQLiteException ex) {
            } finally {
                cursor.close();
            }
    	}
    	
    	Map<Long,Long> albumSongIdSortMap = null;
    	if(albumSongIdMap != null && albumSongIdMap.size() != 0)
    	{
    		albumSongIdSortMap = new LinkedHashMap<Long, Long>();
    		for(int i = 0;i< albumSongIdMap.size();i++)
    		{
    			Long key = mPlayList[i];
    			Long value = albumSongIdMap.get(mPlayList[i]);
    			albumSongIdSortMap.put(key, value);
    		}
    	}
    	return albumSongIdSortMap;
    }

    /**
     * M: When open current track, we should check whether it is drm or not if support drm feature.
     * 
     * @param cursor The cursor query from current track
     */
    private void checkDrmWhenOpenTrack(Cursor cursor) {
    	Log.i(TAG,"<checkDrmWhenOpenTrack>",Log.APP);
        if (cursor != null) {
        	//update by zjw
            int isDrm = cursor.getInt(cursor.getColumnIndexOrThrow("is_drm"));
            int drmMethod = cursor.getInt(cursor.getColumnIndexOrThrow("drm_method"));
            Log.i(TAG, "isDrm=" + isDrm + ", drmMethod=" + drmMethod,Log.APP);
            //update by zjw
            if (isDrm != 1 || (isDrm == 1 && drmMethod == DrmUtils.DRMMETHOD_METHOD_FL)) {
                mQuietMode = true;
                openCurrentAndNext();
                mIsReloadSuccess = true;
            }
        }
    }
    
    /**
     * Reload queue after scan finish if need.
     */
    private void reloadQueueAfterScan() {
        boolean hasCard = MusicUtils.hasMountedSDcard(getApplicationContext());
        Log.i(TAG, "<reloadQueueAfterScan> hasCard = " + hasCard + ", isReloadSuccess = " + mIsReloadSuccess,Log.APP);
        if (hasCard && !mIsReloadSuccess
                && (mPlayList == null || mPlayList.length == 0 || mPlayListLen == 0)) {
            reloadQueue();
            notifyChange(QUEUE_CHANGED);
            notifyChange(META_CHANGED);
        }
    }
    
    /**
     * M: Handle setting mode change action.
     * 
     * @param setting shuffle or repeat
     * @param newMode new mode to set
     */
    private void handleSettingModeChange(int setting, int newMode) {
        Log.i(TAG, "handleSettingModeChange" + String.format("[AVRCP] CHANGE_SETTING_MODE setting:%d newMode:%d", setting, newMode),Log.APP);
        int oldMode = 0;
        switch (setting) {
            case SETTING_MODE_SHUFFLE:
                oldMode = getShuffleMode();
                if (oldMode != newMode) {
                    setShuffleMode(newMode);
                }
                break;

            case SETTING_MODE_REPEAT:
                oldMode = getRepeatMode();
                if (oldMode != newMode) {
                    setRepeatMode(newMode);
                }
                break;

            default:
                Log.e(TAG, "Unsupport AVRCP setting mode!",Log.APP);
                break;
        }
    }

    /**
     * M: If the event send by monkey, we don't re-try failed track to avoid ANR in CTS monkey test.
     * 
     * @return true if from monkey.
     */
    private boolean isEventFromMonkey() {
        boolean isMonkey = ActivityManager.isUserAMonkey();
        Log.i(TAG, "isEventFromMonkey " + isMonkey,Log.APP);
        return isMonkey;
    }

    /**
     * M: When in normal shuffle mode, add the played track to history list, so that user can click
     * prev button to play them again.
     * 
     * @param addSameTrack Whether need to add the same track to history. When call gotoNext(true)
     * and playlist has been shuffle played we need add same to history, otherwise need not.
     */
    private void addPlayedTrackToHistory(boolean addSameTrack) {
    	Log.i(TAG,"<addPlayedTrackToHistory> addSameTrack:" + addSameTrack,Log.APP);
        if (mShuffleMode == SHUFFLE_NORMAL) {
            /// M: If the track has been added to history, we need not to add again except we want
            /// to add same track to history, such as user click next button.
            if (mPlayPos >= 0 && (addSameTrack || !mHistory.contains(mPlayPos))) {
                mHistory.add(mPlayPos);
                Log.i(TAG, "addPlayedTrackToHistory: mPlayPos = " + mPlayPos + ", mHistory = " + mHistory,Log.APP);
            }
            /// M: We only hold max size played history.
            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.removeElementAt(0);
            }
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

    /**
     * M: Check the given audio whether drm, if it's drm and with a FL method, we can play it, otherwise we need not
     * play this drm audio.
     * 
     * @param cursor Given audio's cursor
     * @return Only when audio is drm and method is not FL return false, otherwise return ture.
     */
    private boolean isDrmCanPlay(Cursor cursor) {
        if (cursor == null) {
            Log.w(TAG, "isDrmCanPlay to Check given drm with null cursor.",Log.APP);
            return false;
        }
        boolean isDrmCanPlay = true;
        //update by zjw
        int isDrm = cursor.getInt(cursor.getColumnIndexOrThrow("is_drm"));
        int drmMethod = cursor.getInt(cursor.getColumnIndexOrThrow("drm_method"));
        Log.i(TAG, "isDrmCanPlay: isDrm = " + isDrm + ", drmMethod = " + drmMethod,Log.APP);
        if (isDrm == 1 && drmMethod != DrmUtils.DRMMETHOD_METHOD_FL) {
            isDrmCanPlay = false;
        }
        Log.i(TAG, "<isDrmCanPlay> isDrmCanPlay:" + isDrmCanPlay,Log.APP);
        return isDrmCanPlay;
    }
    
    /**
     * M : For the song not in the last position of playlist ,when the songe
     * play completed or nearly completed or the songe is 0s, we should go to
     * the next song.When the songe is at the last of playlist, the songe play
     * completed or nearly completed,we click play again,we should go to the
     * next song.
     * 
     * @param duration
     * @param short_songe_length
     * @return
     */
    private boolean canGoToNext(long duration, long short_songe_length) {
    	Log.i(TAG, "<canGoToNext> duration:" + duration + " short_songe_length:" + short_songe_length,Log.APP);
        return mRepeatMode != REPEAT_CURRENT
                && (mIsPlaylistCompleted
                        || (duration == 0)
                        || (duration > 0 && duration <= 1000 && (mPlayer.position() > 0))
                        || (duration > 1000 && duration <= short_songe_length && ((duration - position()) < (duration / 10))) 
                        || (duration > short_songe_length && ((duration - position()) <= 1000)));
    }
    
    /**
     * M : For the OM project,we should clear queue when shutdown phone
     * @param needClearQueue
     */
    private void clearQueue(boolean needClearQueue) {
        if (needClearQueue) {
            Editor ed = mPreferences.edit();
            ed.putString("queue", null);
            ed.putString("history", null);
            ed.putInt("curpos", -1);
            ed.putLong("seekpos", 0);
            ed.putInt("repeatmode", REPEAT_NONE);
            ed.putInt("shufflemode", SHUFFLE_NONE);
            SharedPreferencesCompat.apply(ed);
        }
        Log.i(TAG, "<clearQueue> needClearQueue " + needClearQueue,Log.APP);
    }
    
    /**
     * M: Need quit ContentObserverThread when service will be destroy.
     */
    void quitContentObserverThread() {
        boolean isQuitSuccuss = false;
        if (mContentObserverThread != null) {
            isQuitSuccuss = mContentObserverThread.quit();
        }
        Log.i(TAG, "Quit ContentObserverThread when service will be destroy: isQuitSuccuss = " + isQuitSuccuss,Log.APP);
    }

    /**
     * M: When open track, we should check whether it is drm or not if support drm feature.
     * 
     * @param cursor The cursor query from current track
     */
    private boolean checkDrm(Cursor cursor) {
    	Log.i(TAG, "<checkDrm>",Log.APP);
    	//update by zjw
        if (!DrmUtils.isSupportDrm()) {
            Log.w(TAG, "checkDrm(): MusicFeatureOption.IS_SUPPORT_DRM was disabled",Log.APP);
            return true;
        }
        //update by zjw
        if (DrmUtils.isSupportDrm() && cursor != null) {
            int isDrm = cursor.getInt(cursor.getColumnIndexOrThrow("is_drm"));
            int drmMethod = cursor.getInt(cursor.getColumnIndexOrThrow("drm_method"));
            Log.i(TAG, "checkDrm(),isDrm=" + isDrm + ", drmMethod=" + drmMethod,Log.APP);
            // make fl file which changes suffix cann't be opened
            if (!(isDrm == 1 && drmMethod == -1)) {
                return true;
            }
        }
        return false;
    }
    
}

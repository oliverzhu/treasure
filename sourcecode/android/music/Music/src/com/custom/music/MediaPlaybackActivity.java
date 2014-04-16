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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateBeamUrisCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils.TruncateAt;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.custom.music.adapter.AlbumArtAdapter;
import com.custom.music.bean.AlbumSongIdWrapper;
import com.custom.music.util.Constants;
import com.custom.music.util.DrmUtils;
import com.custom.music.util.Log;
import com.custom.music.util.MusicUtils;
import com.custom.music.util.MusicUtils.ServiceToken;
import com.custom.music.util.thread.Future;
import com.custom.music.util.thread.FutureListener;
import com.custom.music.view.CoverFlow;
import com.custom.music.view.MusicGallery;
import com.custom.music.view.RepeatingImageButton;

@SuppressLint("NewApi")
public class MediaPlaybackActivity extends Activity implements MusicUtils.Defs,
    CreateBeamUrisCallback
{
    private static final String TAG = "MediaPlaybackActivity";

    private static final int USE_AS_RINGTONE = CHILD_MENU_BASE;

    private boolean mSeeking = false;
    private boolean mDeviceHasDpad;
    private long mStartSeekPos = 0;
    private long mLastSeekEventTime;
    private IMediaPlaybackService mService = null;
    private RepeatingImageButton mPrevButton;
    private ImageButton mPauseButton;
    private RepeatingImageButton mNextButton;
    private ImageButton mRepeatButton;
    private ImageButton mShuffleButton;
    private TextView mQueueButton;
    private Worker mAlbumArtWorker;
    private AlbumArtHandler mAlbumArtHandler;
    private Toast mToast;
    private int mTouchSlop;
    private ServiceToken mToken;

    /// M: specific performace test case.
    private static final String PLAY_TEST = "play song";
    private static final String NEXT_TEST = "next song";
    private static final String PREV_TEST = "prev song";

    /// M: FM Tx package and activity information.
    private static final String FM_TX_PACKAGE = "com.mediatek.FMTransmitter";
    private static final String FM_TX_ACTIVITY = FM_TX_PACKAGE + ".FMTransmitterActivity";

    /// M: show album art again when configuration change
    private boolean mIsShowAlbumArt = false;
    private Bitmap mArtBitmap = null;
    private long mArtSongId = -1;

    /// M: Add queue, repeat and shuffle to action bar when in landscape
    private boolean mIsLandscape;
    private MenuItem mQueueMenuItem;
    private MenuItem mRepeatMenuItem;
    private MenuItem mShuffleMenuItem;
    /// M: Add search button in actionbar when nowplaying not exist
    MenuItem mSearchItem;

    /// M: Add playlist sub menu to music
    private SubMenu mAddToPlaylistSubmenu;

    /// M: Music performance test string which is current runing
    private String mPerformanceTestString = null;

    /// M: use to make current playing time aways showing when seeking
    private int mRepeatCount = -1;

    /// M: Some music's durations can only be obtained when playing the media.
    // As a result we must know whether to update the durations.
    private boolean mNeedUpdateDuration = true;

    /// M: aviod Navigation button respond JE if Activity is background
    private boolean mIsInBackgroud = false;

    /// M: marked in onStop(), when get  phone call  from this activity,
    // if screen off to on, this activity will call onStart() to bind service,
    // the pause button may update in onResume() and onServiceConnected(), but
    // the service is not ready in onResume(), so need to discard the update.
    private boolean mIsCallOnStop = false;
    /// M: save the input of SearchView
    private CharSequence mQueryText;

    private NotificationManager mNotificationManager;
    private AudioManager mAudioManager;
    private boolean isRegistered = false;
    /// M: NFC feature
    NfcAdapter mNfcAdapter;
    /// M:identify whether the OptionMenu is opened
    private boolean mIsOptionMenuOpen = false;
    
    public static int width;
	public static int height;

    public MediaPlaybackActivity()
    {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;
		
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mAlbumArtWorker = new Worker("album art worker");
        mAlbumArtHandler = new AlbumArtHandler(mAlbumArtWorker.getLooper());

        /// M: Get the current orientation and enable action bar to add more function to it.
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        mIsLandscape = (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
        /// M: move UI component init and update to updateUI().
        updateUI();
        /// M: Set the action bar on the right to be up navigation
        /// M: Get Nfc adapter and set callback available. @{
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
        if (mNfcAdapter == null) {
            Log.w(TAG, "NFC not available!",Log.APP);
            return;
        }
        mNfcAdapter.setBeamPushUrisCallback(this, this);
        /// @}
    }
    
    int mInitialX = -1;
    int mLastX = -1;
    int mTextWidth = 0;
    int mViewWidth = 0;
    boolean mDraggingLabel = false;
    
    TextView textViewForContainer(View v) {
        View vv = v.findViewById(R.id.artistname);
        if (vv != null) return (TextView) vv;
        vv = v.findViewById(R.id.albumname);
        if (vv != null) return (TextView) vv;
        vv = v.findViewById(R.id.trackname);
        if (vv != null) return (TextView) vv;
        return null;
    }

    Handler mLabelScroller = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            TextView tv = (TextView) msg.obj;
            int x = tv.getScrollX();
            x = x * 3 / 4;
            tv.scrollTo(x, 0);
            if (x == 0) {
                tv.setEllipsize(TruncateAt.END);
            } else {
                Message newmsg = obtainMessage(0, tv);
                mLabelScroller.sendMessageDelayed(newmsg, 15);
            }
        }
    };
    

    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            /// M: only respond when progress bar don't change from touch
            //mLastSeekEventTime = 0;
            mFromTouch = true;
        }
        public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
        	Log.i(TAG, "<onProgressChanged> progress:" + progress + " fromuser:" + fromuser,Log.APP);
            if (!fromuser || (mService == null)) return;
            /// M: only respond when progress bar don't change from touch  {@
            //long now = SystemClock.elapsedRealtime();
            //if ((now - mLastSeekEventTime) > 250) {
            //    mLastSeekEventTime = now;
            //    mPosOverride = mDuration * progress / 1000;
            //
            //    try {
            //        mService.seek(mPosOverride);
            //    } catch (RemoteException ex) {
            //    }
            //}

            // trackball event, allow progress updates
            if (!mFromTouch) {
                mPosOverride = mDuration * progress / 1000;
                try {
                    mService.seek(mPosOverride);
                } catch (RemoteException ex) {
                    Log.e(TAG, "Error:" + ex,Log.APP);
                }
            /// @}
                
                refreshNow();
                mPosOverride = -1;
            }
        }
        public void onStopTrackingTouch(SeekBar bar) {
        	Log.i(TAG, "<onStopTrackingTouch>",Log.APP);
           /// M: Save the seek position, seek and update UI. @{
           if (mService != null) {
                try {
                    mPosOverride = bar.getProgress() * mDuration / 1000;
                    mService.seek(mPosOverride);
                    refreshNow();
                } catch (RemoteException ex) {
                    Log.e(TAG, "Error:" + ex,Log.APP);
                }
           }
           /// @}
            mPosOverride = -1;
            mFromTouch = false;
        }
    };
    
    private View.OnClickListener mMusicLibListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Log.i(TAG, "<mQueueListener.onClick()>",Log.APP);
        	/// M: Navigation button press back,
            /// aviod Navigation button respond JE if Activity is background
            if (!mIsInBackgroud) {
                Intent parentIntent = new Intent(MediaPlaybackActivity.this, MusicBrowserActivity.class);
                parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(parentIntent);
            }
        }
    };
    
    private View.OnClickListener mQueueListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Log.i(TAG, "<mQueueListener.onClick()>",Log.APP);
            startActivity(
                    new Intent(Intent.ACTION_EDIT)
                    .setDataAndType(Uri.EMPTY, Constants.TRACK_MIME_TYPE)
                    .putExtra("playlist", "nowplaying")
            );
        }
    };
    
    private View.OnClickListener mShuffleListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Log.i(TAG, "<mShuffleListener.onClick()>",Log.APP);
            toggleShuffle();
        }
    };

    private View.OnClickListener mRepeatListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Log.i(TAG, "<mRepeatListener.onClick()>",Log.APP);
            cycleRepeat();
        }
    };

    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
        	Log.i(TAG, "<mPauseListener.onClick()>",Log.APP);
            doPauseResume();
        }
    };

    private View.OnClickListener mPrevListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i(TAG,"<mPrevListener.onClick()> prev song start ["
                                + System.currentTimeMillis() + "]",Log.APP);
            mPerformanceTestString = PREV_TEST;

            /// M: Handle click event in handler to avoid ANR for continuous
            // press @{
            Message msg = mHandler.obtainMessage(PREV_BUTTON, null);
            mHandler.removeMessages(PREV_BUTTON);
            mHandler.sendMessage(msg);
            /// @}
        }
    };

    private View.OnClickListener mNextListener = new View.OnClickListener() {
        public void onClick(View v) {
            Log.i("TAG", "<mNextListener.onClick()>[Music] next song start ["
                                + System.currentTimeMillis() + "]",Log.APP);
            mPerformanceTestString = NEXT_TEST;

            /// M: Handle click event in handler to avoid ANR for continuous
            // press @{
            Message msg = mHandler.obtainMessage(NEXT_BUTTON, null);
            mHandler.removeMessages(NEXT_BUTTON);
            mHandler.sendMessage(msg);
            /// @}
        }
    };

    private RepeatingImageButton.RepeatListener mRewListener =
        new RepeatingImageButton.RepeatListener() {
        public void onRepeat(View v, long howlong, int repcnt) {
        	Log.i(TAG, "<mRewListener.onRepeat()> music backward howlong:" + howlong + " repcnt:" + repcnt,Log.APP);
            /// M: use to make current playing time aways showing when seeking
            mRepeatCount = repcnt;
            scanBackward(repcnt, howlong);
        }
    };
    
    private RepeatingImageButton.RepeatListener mFfwdListener =
        new RepeatingImageButton.RepeatListener() {
        public void onRepeat(View v, long howlong, int repcnt) {
        	Log.i(TAG, "<mFfwdListener.onRepeat()>  music forward howlong:" + howlong + " repcnt:" + repcnt,Log.APP);
            /// M: use to make current playing time aways showing when seeking
            mRepeatCount = repcnt;
            scanForward(repcnt, howlong);
        }
    };
   
    @Override
    public void onStop() {
        paused = true;
        Log.i(TAG, "<onStop>",Log.APP);
        /// M: so mark mIsCallOnStop is true
        mIsCallOnStop = true;
        mHandler.removeMessages(REFRESH);
        unregisterReceiver(mStatusListener);
        MusicUtils.unbindFromService(mToken);
        mService = null;
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "<onStart>",Log.APP);
        paused = false;

        mToken = MusicUtils.bindToService(this, osc);
        if (mToken == null) {
            // something went wrong
            mHandler.sendEmptyMessage(QUIT);
        }
        
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        /// M: listen more status to update UI @{
        f.addAction(MediaPlaybackService.QUIT_PLAYBACK);
        f.addAction(Intent.ACTION_SCREEN_ON);
        f.addAction(Intent.ACTION_SCREEN_OFF);
        /// @}
        registerReceiver(mStatusListener, new IntentFilter(f));
        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
    }
    
    @Override
    public void onNewIntent(Intent intent) {
    	Log.i(TAG, "<onNewIntent>",Log.APP);
        setIntent(intent);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        /// M: when it launch from status bar, collapse status ba first. @{
        Intent intent = getIntent();
        boolean collapseStatusBar = intent.getBooleanExtra("collapse_statusbar", false);
        Log.i(TAG, ">>> onResume: collapseStatusBar=" + collapseStatusBar,Log.APP);
        if (collapseStatusBar) {
//            StatusBarManager statusBar = (StatusBarManager)getSystemService(Context.STATUS_BAR_SERVICE);
//            statusBar.collapsePanels();
        	//update by zjw
        	try {
				Object service = getSystemService("statusbar");
				Class cls = Class.forName("android.app.StatusBarManager");
				Method collapse = cls.getMethod("collapsePanels");
				collapse.invoke(service);
			} catch (IllegalArgumentException e) {
				Log.w(TAG, "StatusBarManager reflect IllegalArgumentException", Log.APP);
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				Log.w(TAG, "StatusBarManager reflect ClassNotFoundException", Log.APP);
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				Log.w(TAG, "StatusBarManager reflect NoSuchMethodException", Log.APP);
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				Log.w(TAG, "StatusBarManager reflect IllegalAccessException", Log.APP);
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				Log.w(TAG, "StatusBarManager reflect InvocationTargetException", Log.APP);
				e.printStackTrace();
			}
        }
        ///@}
        updateTrackInfo();
        /// M: if it doesn't come from onStop(),we should update pause button. @{
        if (!mIsCallOnStop) {
            setPauseButtonImage();
        }
        mIsCallOnStop = false;
        /// @}

        /// M: When back to this activity, ask service for right position
        mPosOverride = -1;
        invalidateOptionsMenu();
        
        /// M: performance default test, response time for Play button
        mPerformanceTestString = PLAY_TEST;
        /// M: aviod Navigation button respond JE if Activity is background
        mIsInBackgroud = false;
        
        Log.i(TAG, "onResume >>>",Log.APP);
    }


    @Override
    public void onDestroy()
    {
    	Log.i(TAG, "<onDestroy>",Log.APP);
        mAlbumArtWorker.quit();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Don't show the menu items if we got launched by path/filedescriptor, or
        // if we're in one shot mode. In most cases, these menu items are not
        // useful in those modes, so for consistency we never show them in these
        // modes, instead of tailoring them to the specific file being played.
        long currentAudioId = MusicUtils.getCurrentAudioId();
        Log.i(TAG, "<onCreateOptionsMenu> currentAudioId:" + currentAudioId,Log.APP);
        if (currentAudioId >= 0) {
            /// M: adjust menu sequence
            // menu.add(0, GOTO_START, 0, R.string.goto_start)
            //            .setIcon(R.drawable.ic_menu_music_library);
            /// M: get the object for method onPrepareOptionsMenu to keep playlist menu up-to-date
            mAddToPlaylistSubmenu = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0,
                    R.string.add_to_playlist).setIcon(android.R.drawable.ic_menu_add);
            // these next two are in a separate group, so they can be shown/hidden as needed
            // based on the keyguard state

            menu.add(0, USE_AS_RINGTONE, 0, R.string.ringtone_menu_short)
                    .setIcon(R.drawable.ic_menu_set_as_ringtone);

            menu.add(0, DELETE_ITEM, 0, R.string.delete_item)
                    .setIcon(R.drawable.ic_menu_delete);
            /// M: move to prepare option menu to disable menu when MusicFX is disable
            menu.add(0, EFFECTS_PANEL, 0, R.string.effects_list_title)
                    .setIcon(R.drawable.ic_menu_eq);

            menu.add(0, GOTO_START, 0, R.string.goto_start)
            .setIcon(R.drawable.ic_menu_music_library);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	Log.i(TAG, "<onPrepareOptionsMenu>",Log.APP);
        if (mService == null) return false;
        /// M: DRM feature, when track is drm and not FL type, it can not set as ringtone. {@
        //update by zjw
        if (DrmUtils.isSupportDrm()) {
            try {
                menu.findItem(USE_AS_RINGTONE).setVisible(mService.canUseAsRingtone());
            } catch (RemoteException e) {
                Log.e(TAG, "onPrepareOptionsMenu with RemoteException " + e,Log.APP);
            }
        }

        /// M: Set effect menu visible depend the effect class whether disable or enable. {@
        MusicUtils.setEffectPanelMenu(getApplicationContext(), menu);
        /// @}

        /// M: Keep the playlist menu up-to-date.
        MusicUtils.makePlaylistMenu(this, mAddToPlaylistSubmenu);
        mAddToPlaylistSubmenu.removeItem(MusicUtils.Defs.QUEUE);
        KeyguardManager km = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        menu.setGroupVisible(1, !km.inKeyguardRestrictedInputMode());

        setRepeatButtonImage();
        setShuffleButtonImage();
        /// @}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        try {
            switch (item.getItemId()) {
                case GOTO_START:
                    intent = new Intent();
                    intent.setClass(this, TrackBrowserActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case USE_AS_RINGTONE: {
                    // Set the system setting to make this the current ringtone
                    if (mService != null) {
                        MusicUtils.setRingtone(this, mService.getAudioId());
                    }
                    return true;
                }
                case NEW_PLAYLIST: {
                    intent = new Intent();
                    intent.setClass(this, CreatePlaylist.class);
                    /// M: Add to indicate the save_as_playlist and new_playlist
                    intent.putExtra(MusicUtils.SAVE_PLAYLIST_FLAG, MusicUtils.NEW_PLAYLIST);
                    startActivityForResult(intent, NEW_PLAYLIST);
                    return true;
                }

                case PLAYLIST_SELECTED: {
                    long [] list = new long[1];
                    list[0] = MusicUtils.getCurrentAudioId();
                    long playlist = item.getIntent().getLongExtra("playlist", 0);
                    MusicUtils.addToPlaylist(this, list, playlist);
                    return true;
                }
                
                case DELETE_ITEM: {
                    if (mService != null) {
                        long [] list = new long[1];
                        list[0] = MusicUtils.getCurrentAudioId();
                        Bundle b = new Bundle();
                        String f;
                        /// M: Get string in DeleteItems Activity to get current language string. @{
                        //if (android.os.Environment.isExternalStorageRemovable()) {
                        //f = getString(R.string.delete_song_desc, mService.getTrackName());
                        //} else {
                        //    f = getString(R.string.delete_song_desc_nosdcard, mService.getTrackName());
                        //}
                        //b.putString("description", f);
                        b.putInt(MusicUtils.DELETE_DESC_STRING_ID, R.string.delete_song_desc);
                        b.putString(MusicUtils.DELETE_DESC_TRACK_INFO, mService.getTrackName());
                        /// @}
                        b.putLongArray("items", list);
                        intent = new Intent();
                        intent.setClass(this, DeleteItems.class);
                        intent.putExtras(b);
                        startActivityForResult(intent, -1);
                    }
                    return true;
                }

                /// M: Show effect panel and call the same method as other activities.
                case EFFECTS_PANEL:
                    return MusicUtils.startEffectPanel(this);

                /// M: Open FMTransmitter and Search view. {@
                case FM_TRANSMITTER:
                    Intent intentFMTx = new Intent(FM_TX_ACTIVITY);
                    intentFMTx.setClassName(FM_TX_PACKAGE, FM_TX_ACTIVITY);

                    try {
                        startActivity(intentFMTx);
                    } catch (ActivityNotFoundException anfe) {
                        Log.i(TAG, "FMTx activity isn't found!!",Log.APP);
                    }
                
                    return true;
                /// @}

                /// M: handle action bar and navigation up button. {@
                case android.R.id.home:
                    /// M: Navigation button press back,
                    /// aviod Navigation button respond JE if Activity is background
                    if (!mIsInBackgroud) {
                        Intent parentIntent = new Intent(this, MusicBrowserActivity.class);
                        parentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        finish();
                        startActivity(parentIntent);
                    }
                    return true;
                default:
                    return true;
                /// @}
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "onOptionsItemSelected with RemoteException " + ex,Log.APP);
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu) {
        // TODO Auto-generated method stub
        Log.i(TAG, "<onOptionsMenuClosed>",Log.APP);
        mIsOptionMenuOpen = false;
        super.onOptionsMenuClosed(menu);
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        // TODO Auto-generated method stub
        Log.i(TAG, "<onMenuOpened>",Log.APP);
        mIsOptionMenuOpen = true;
        invalidateOptionsMenu();
        return super.onMenuOpened(featureId, menu);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case NEW_PLAYLIST:
                Uri uri = intent.getData();
                if (uri != null) {
                    long [] list = new long[1];
                    list[0] = MusicUtils.getCurrentAudioId();
                    int playlist = Integer.parseInt(uri.getLastPathSegment());
                    MusicUtils.addToPlaylist(this, list, playlist);
                }
                break;
        }
    }
    private final int keyboard[][] = {
        {
            KeyEvent.KEYCODE_Q,
            KeyEvent.KEYCODE_W,
            KeyEvent.KEYCODE_E,
            KeyEvent.KEYCODE_R,
            KeyEvent.KEYCODE_T,
            KeyEvent.KEYCODE_Y,
            KeyEvent.KEYCODE_U,
            KeyEvent.KEYCODE_I,
            KeyEvent.KEYCODE_O,
            KeyEvent.KEYCODE_P,
        },
        {
            KeyEvent.KEYCODE_A,
            KeyEvent.KEYCODE_S,
            KeyEvent.KEYCODE_D,
            KeyEvent.KEYCODE_F,
            KeyEvent.KEYCODE_G,
            KeyEvent.KEYCODE_H,
            KeyEvent.KEYCODE_J,
            KeyEvent.KEYCODE_K,
            KeyEvent.KEYCODE_L,
            KeyEvent.KEYCODE_DEL,
        },
        {
            KeyEvent.KEYCODE_Z,
            KeyEvent.KEYCODE_X,
            KeyEvent.KEYCODE_C,
            KeyEvent.KEYCODE_V,
            KeyEvent.KEYCODE_B,
            KeyEvent.KEYCODE_N,
            KeyEvent.KEYCODE_M,
            KeyEvent.KEYCODE_COMMA,
            KeyEvent.KEYCODE_PERIOD,
            KeyEvent.KEYCODE_ENTER
        }

    };

    private int lastX;
    private int lastY;

    private boolean seekMethod1(int keyCode)
    {
    	Log.i(TAG, "<seekMethod1> keyCode:" + keyCode,Log.APP);
        if (mService == null) return false;
        for(int x=0;x<10;x++) {
            for(int y=0;y<3;y++) {
                if(keyboard[y][x] == keyCode) {
                    int dir = 0;
                    // top row
                    if(x == lastX && y == lastY) dir = 0;
                    else if (y == 0 && lastY == 0 && x > lastX) dir = 1;
                    else if (y == 0 && lastY == 0 && x < lastX) dir = -1;
                    // bottom row
                    else if (y == 2 && lastY == 2 && x > lastX) dir = -1;
                    else if (y == 2 && lastY == 2 && x < lastX) dir = 1;
                    // moving up
                    else if (y < lastY && x <= 4) dir = 1; 
                    else if (y < lastY && x >= 5) dir = -1; 
                    // moving down
                    else if (y > lastY && x <= 4) dir = -1; 
                    else if (y > lastY && x >= 5) dir = 1; 
                    lastX = x;
                    lastY = y;
                    try {
                        mService.seek(mService.position() + dir * 5);
                    } catch (RemoteException ex) {
                    }
                    refreshNow();
                    return true;
                }
            }
        }
        lastX = -1;
        lastY = -1;
        return false;
    }

    private boolean seekMethod2(int keyCode)
    {
    	Log.i(TAG, "<seekMethod2> keyCode:" + keyCode,Log.APP);
        if (mService == null) return false;
        for(int i=0;i<10;i++) {
            if(keyboard[0][i] == keyCode) {
                int seekpercentage = 100*i/10;
                try {
                    mService.seek(mService.duration() * seekpercentage / 100);
                } catch (RemoteException ex) {
                }
                refreshNow();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        try {
            switch(keyCode)
            {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (!useDpadMusicControl()) {
                        break;
                    }
                    if (mService != null) {
                        if (!mSeeking && mStartSeekPos >= 0) {
                            mPauseButton.requestFocus();
                            if (mStartSeekPos < 1000) {
                                mService.prev();
                            } else {
                                mService.seek(0);
                            }
                        } else {
                            scanBackward(-1, event.getEventTime() - event.getDownTime());
                            mPauseButton.requestFocus();
                            mStartSeekPos = -1;
                        }
                    }
                    mSeeking = false;
                    mPosOverride = -1;
                    return true;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (!useDpadMusicControl()) {
                        break;
                    }
                    if (mService != null) {
                        if (!mSeeking && mStartSeekPos >= 0) {
                            mPauseButton.requestFocus();
                            mService.next();
                        } else {
                            scanForward(-1, event.getEventTime() - event.getDownTime());
                            mPauseButton.requestFocus();
                            mStartSeekPos = -1;
                        }
                    }
                    mSeeking = false;
                    mPosOverride = -1;
                    return true;
                    
                /// M: handle key code center. {@
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    View curSel = getCurrentFocus();
                    if ((curSel != null && R.id.pause == curSel.getId()) || 
                            (curSel == null)) {
                        doPauseResume();
                    }
                    return true;
                /// @}
            }
        } catch (RemoteException ex) {
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean useDpadMusicControl() {
    	Log.i(TAG, "<useDpadMusicControl>",Log.APP);
        if (mDeviceHasDpad && (mPrevButton.isFocused() ||
                mNextButton.isFocused() ||
                mPauseButton.isFocused())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        int direction = -1;
        int repcnt = event.getRepeatCount();

        if((seekmethod==0)?seekMethod1(keyCode):seekMethod2(keyCode))
            return true;

        switch(keyCode)
        {
/*
            // image scale
            case KeyEvent.KEYCODE_Q: av.adjustParams(-0.05, 0.0, 0.0, 0.0, 0.0,-1.0); break;
            case KeyEvent.KEYCODE_E: av.adjustParams( 0.05, 0.0, 0.0, 0.0, 0.0, 1.0); break;
            // image translate
            case KeyEvent.KEYCODE_W: av.adjustParams(    0.0, 0.0,-1.0, 0.0, 0.0, 0.0); break;
            case KeyEvent.KEYCODE_X: av.adjustParams(    0.0, 0.0, 1.0, 0.0, 0.0, 0.0); break;
            case KeyEvent.KEYCODE_A: av.adjustParams(    0.0,-1.0, 0.0, 0.0, 0.0, 0.0); break;
            case KeyEvent.KEYCODE_D: av.adjustParams(    0.0, 1.0, 0.0, 0.0, 0.0, 0.0); break;
            // camera rotation
            case KeyEvent.KEYCODE_R: av.adjustParams(    0.0, 0.0, 0.0, 0.0, 0.0,-1.0); break;
            case KeyEvent.KEYCODE_U: av.adjustParams(    0.0, 0.0, 0.0, 0.0, 0.0, 1.0); break;
            // camera translate
            case KeyEvent.KEYCODE_Y: av.adjustParams(    0.0, 0.0, 0.0, 0.0,-1.0, 0.0); break;
            case KeyEvent.KEYCODE_N: av.adjustParams(    0.0, 0.0, 0.0, 0.0, 1.0, 0.0); break;
            case KeyEvent.KEYCODE_G: av.adjustParams(    0.0, 0.0, 0.0,-1.0, 0.0, 0.0); break;
            case KeyEvent.KEYCODE_J: av.adjustParams(    0.0, 0.0, 0.0, 1.0, 0.0, 0.0); break;

*/

            case KeyEvent.KEYCODE_SLASH:
                seekmethod = 1 - seekmethod;
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (!useDpadMusicControl()) {
                    break;
                }
                if (!mPrevButton.hasFocus()) {
                    mPrevButton.requestFocus();
                }
                scanBackward(repcnt, event.getEventTime() - event.getDownTime());
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (!useDpadMusicControl()) {
                    break;
                }
                if (!mNextButton.hasFocus()) {
                    mNextButton.requestFocus();
                }
                scanForward(repcnt, event.getEventTime() - event.getDownTime());
                return true;

            case KeyEvent.KEYCODE_S:
                toggleShuffle();
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
                /// M: handle key code center.
                 return true;
                 
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_ENTER:
                doPauseResume();
                return true;
            case KeyEvent.KEYCODE_MENU:
                if (mSearchItem != null) {
                    if (mSearchItem.isActionViewExpanded()) {
                        return true;
                    }
                }
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    private void scanBackward(int repcnt, long delta) {
    	Log.i(TAG, "<scanBackward> repcnt:" + repcnt + " delta:" + delta,Log.APP);
        if(mService == null) return;
        try {
            if(repcnt == 0) {
                mStartSeekPos = mService.position();
                mLastSeekEventTime = 0;
                mSeeking = false;
            } else {
                mSeeking = true;
                if (delta < 5000) {
                    // seek at 10x speed for the first 5 seconds
                    delta = delta * 10; 
                } else {
                    // seek at 40x after that
                    delta = 50000 + (delta - 5000) * 40;
                }
                long newpos = mStartSeekPos - delta;
                if (newpos < 0) {
                    // move to previous track
                    mService.prev();
                    long duration = mService.duration();
                    mStartSeekPos += duration;
                    newpos += duration;
                }
                if (((delta - mLastSeekEventTime) > 250) || repcnt < 0){
                    mService.seek(newpos);
                    mLastSeekEventTime = delta;
                }
                if (repcnt >= 0) {
                    mPosOverride = newpos;
                } else {
                    mPosOverride = -1;
                }
                refreshNow();
            }
        } catch (RemoteException ex) {
        }
    }

    private void scanForward(int repcnt, long delta) {
    	Log.i(TAG, "<scanForward> repcnt:" + repcnt + " delta:" + delta,Log.APP);
        if(mService == null) return;
        try {
            if(repcnt == 0) {
                mStartSeekPos = mService.position();
                mLastSeekEventTime = 0;
                mSeeking = false;
            } else {
                mSeeking = true;
                if (delta < 5000) {
                    // seek at 10x speed for the first 5 seconds
                    delta = delta * 10; 
                } else {
                    // seek at 40x after that
                    delta = 50000 + (delta - 5000) * 40;
                }
                long newpos = mStartSeekPos + delta;
                long duration = mService.duration();
                if (newpos >= duration) {
                    // move to next track
                    mService.next();
                    mStartSeekPos -= duration; // is OK to go negative
                    newpos -= duration;
                }
                if (((delta - mLastSeekEventTime) > 250) || repcnt < 0){
                    mService.seek(newpos);
                    mLastSeekEventTime = delta;
                }
                if (repcnt >= 0) {
                    mPosOverride = newpos;
                } else {
                    mPosOverride = -1;
                }
                refreshNow();
            }
        } catch (RemoteException ex) {
        }
    }
    
    private void doPauseResume() {
        try {
            if(mService != null) {
                Boolean isPlaying = mService.isPlaying();
                Log.i(TAG, "<doPauseResume> isPlaying=" + isPlaying,Log.APP);
                /// M: AVRCP and Android Music AP supports the FF/REWIND
                //   aways get position from service if user press pause button
                mPosOverride = -1;
                if (isPlaying) {
                    mService.pause();
                } else {
                    mService.play();
                }
                setPauseButtonImage();
                refreshNow();
            }
        } catch (RemoteException ex) {
        }
    }
    
    private void toggleShuffle() {
        if (mService == null) {
            return;
        }
        try {
            int shuffle = mService.getShuffleMode();
            if (shuffle == MediaPlaybackService.SHUFFLE_NONE) {
                mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
                if (mService.getRepeatMode() == MediaPlaybackService.REPEAT_CURRENT) {
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                }
                /// M: need to refresh repeat button when we modify rpeate mode.
                setRepeatButtonImage();
                showToast(R.string.shuffle_on_notif);
            } else if (shuffle == MediaPlaybackService.SHUFFLE_NORMAL ||
                    shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                /// M: After turn off party shuffle, we should to refresh option menu to avoid user click fast to show
                /// party shuffle off when has turned off.
                //invalidateOptionsMenu();
                showToast(R.string.shuffle_off_notif);
            } else {
                Log.w(TAG, "Invalid shuffle mode: " + shuffle,Log.APP);
            }
            setShuffleButtonImage();
        } catch (RemoteException ex) {
        }
    }
    
    private void cycleRepeat() {
        if (mService == null) {
            return;
        }
        try {
            int mode = mService.getRepeatMode();
            if (mode == MediaPlaybackService.REPEAT_NONE) {
                mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                showToast(R.string.repeat_all_notif);
            } else if (mode == MediaPlaybackService.REPEAT_ALL) {
                mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
                if (mService.getShuffleMode() != MediaPlaybackService.SHUFFLE_NONE) {
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
                    /// M: After turn off party shuffle, we should to refresh option menu to avoid user click fast to show
                    /// party shuffle off when has turned off.
                    //invalidateOptionsMenu();
                    setShuffleButtonImage();
                }
                showToast(R.string.repeat_current_notif);
            } else {
                mService.setRepeatMode(MediaPlaybackService.REPEAT_NONE);
                showToast(R.string.repeat_off_notif);
            }
            setRepeatButtonImage();
        } catch (RemoteException ex) {
        }
        
    }
    
    private void showToast(int resid) {
        if (mToast == null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        mToast.setText(resid);
        mToast.show();
    }

    private void startPlayback() {
    	Log.i(TAG, "<startPlayback>",Log.APP);

        if(mService == null)
            return;
        Intent intent = getIntent();
        String filename = "";
        Uri uri = intent.getData();
        if (uri != null && uri.toString().length() > 0) {
            // If this is a file:// URI, just use the path directly instead
            // of going through the open-from-filedescriptor codepath.
            String scheme = uri.getScheme();
            if ("file".equals(scheme)) {
                filename = uri.getPath();
            } else {
                filename = uri.toString();
            }
            try {
                mService.stop();
                mService.openFile(filename);
                mService.play();
                setIntent(new Intent());
            } catch (Exception ex) {
                Log.e(TAG, "couldn't start playback: " + ex,Log.APP);
            }
        }

        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
    }

    private ServiceConnection osc = new ServiceConnection() {
            public void onServiceConnected(ComponentName classname, IBinder obj) {
                mService = IMediaPlaybackService.Stub.asInterface(obj);
                /// M: Call this to invalidate option menu to install action bar
                invalidateOptionsMenu();
                startPlayback();
                try {
                    // Assume something is playing when the service says it is,
                    // but also if the audio ID is valid but the service is paused.
                    if (mService.getAudioId() >= 0 || mService.isPlaying() ||
                            mService.getPath() != null) {
                        // something is playing now, we're done
                        /// M: Only in portrait we need to set them to be 
                        // visible {@
                        if (!mIsLandscape) {
                            mRepeatButton.setVisibility(View.VISIBLE);
                            mShuffleButton.setVisibility(View.VISIBLE);
                            mQueueButton.setVisibility(View.VISIBLE);
                        }
                        /// @}
                        setRepeatButtonImage();
                        setShuffleButtonImage();
                        setPauseButtonImage();
                        return;
                    }
                } catch (RemoteException ex) {
                }
                // Service is dead or not playing anything. If we got here as part
                // of a "play this file" Intent, exit. Otherwise go to the Music
                // app start screen.
                
                /// M: MTK Mark for PlayAll timing issue, if play many error file, it will back to
                /// last screen.if play one or two error file, it will go to start screen, So we
                /// unify the behavior. {@
                //if (getIntent().getData() == null) {
                //    Intent intent = new Intent(Intent.ACTION_MAIN);
                //    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //    intent.setClass(MediaPlaybackActivity.this, MusicBrowserActivity.class);
                //    startActivity(intent);
                //}
                /// @}
                
                finish();
            }
            public void onServiceDisconnected(ComponentName classname) {
                mService = null;
                /// M: Close the activity when service not exsit
                finish();
            }
    };

    private void setRepeatButtonImage() {
        if (mService == null) return;
        try {
            /// M: Set drawable to action bar in landscape and set it to button in 
            // portrait {@
            int drawable;
            switch (mService.getRepeatMode()) {
                case MediaPlaybackService.REPEAT_ALL:
                    drawable = R.drawable.ic_mp_repeat_all_btn;
                    break;
                    
                case MediaPlaybackService.REPEAT_CURRENT:
                    drawable = R.drawable.ic_mp_repeat_once_btn;
                    break;
                    
                default:
                    drawable = R.drawable.ic_mp_repeat_off_btn;
                    break;
                    
            }
            if (mIsLandscape) {
                if (mRepeatMenuItem != null) {   
                    mRepeatMenuItem.setIcon(drawable);
                }
            } else {
                mRepeatButton.setImageResource(drawable);
            }
            /// @}
        } catch (RemoteException ex) {
        }
    }
    
    private void setShuffleButtonImage() {
        if (mService == null) return;
        try {
            /// M: Set drawable to action bar in landscape and set it to button in 
            // portrait  {@
            int drawable;
            switch (mService.getShuffleMode()) {
                case MediaPlaybackService.SHUFFLE_NONE:
                    drawable = R.drawable.ic_mp_shuffle_off_btn;
                    break;
                    
                case MediaPlaybackService.SHUFFLE_AUTO:
                    drawable = R.drawable.ic_mp_partyshuffle_on_btn;
                    break;
                    
                default:
                    drawable = R.drawable.ic_mp_shuffle_on_btn;
                    break;
                    
            }
            if (mIsLandscape) {
                if (mShuffleMenuItem != null) {
                    mShuffleMenuItem.setIcon(drawable);
                }   
            } else {
                mShuffleButton.setImageResource(drawable);
            }
            /// @}
        } catch (RemoteException ex) {
        }
    }
    
    private void setPauseButtonImage() {
        try {
            if (mService != null && mService.isPlaying()) {
            	mPauseButton.setImageResource(R.drawable.pause_selector);
                /// M: When not seeking, aways get position from service to 
                // update current playing time.
                if (!mSeeking) {
                    mPosOverride = -1;
                }
            } else {
                mPauseButton.setImageResource(R.drawable.play_selector);
            }
        } catch (RemoteException ex) {
        }
    }
    
    private RelativeLayout coverFlowParenet;
    private CoverFlow coverFlow;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private TextView mArtistName;
    private TextView mTrackName;
    private ProgressBar mProgress;
    private long mPosOverride = -1;
    private boolean mFromTouch = false;
    private long mDuration;
    private int seekmethod;
    private boolean paused;

    private static final int REFRESH = 1;
    private static final int QUIT = 2;
    private static final int GET_ALBUM_ART = 3;
    private static final int ALBUM_ART_DECODED = 4;

    /// M: Define next and prev button.
    private static final int NEXT_BUTTON = 6;
    private static final int PREV_BUTTON = 7;

    private void queueNextRefresh(long delay) {
    	Log.i(TAG, "<queueNextRefresh> delay:" + delay,Log.APP);
        if (!paused) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    private long refreshNow() {
    	Log.i(TAG, "<refreshNow>",Log.APP);
        /// M: duration for position correction for play complete
        final int positionCorrection = 300;
        if(mService == null)
            return 500;
        try {
            long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
            /// M: position correction for play complete @{
            if (pos + positionCorrection > mDuration) {
                Log.i(TAG, "refreshNow, do a workaround for position",Log.APP);
                pos = mDuration;
            }
            /// @}
            if ((pos >= 0) && (mDuration > 0)) {
                mCurrentTime.setText(MusicUtils.makeTimeString(this, pos / 1000));
                /// M: Don't need to update from touch @{
                if (!mFromTouch) {
                    int progress = (int) (1000 * pos / mDuration);
                    mProgress.setProgress(progress);
                }
                /// @}
                /// M: use to make current playing time aways showing when seeking
                if (mService.isPlaying() || mRepeatCount > -1) {
                    mCurrentTime.setVisibility(View.VISIBLE);
                } else {
                    // blink the counter
                    int vis = mCurrentTime.getVisibility();
                    mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE : View.INVISIBLE);
                    return 500;
                }
            } else {
                /// M: adjust the UI for error file  @{
                mCurrentTime.setVisibility(View.VISIBLE);
                mCurrentTime.setText("0:00");
                mTotalTime.setText("--:--");
                if (!mFromTouch) {
                    mProgress.setProgress(0);
                }
                /// @}
            }
            /// M: update duration for specific formats
            updateDuration(pos);
            // calculate the number of milliseconds until the next full second, so
            // the counter can be updated at just the right time
            long remaining = 1000 - (pos % 1000);

            // approximate how often we would need to refresh the slider to
            // move it smoothly
            int width = mProgress.getWidth();
            if (width == 0) width = 320;
            long smoothrefreshtime = mDuration / width;

            if (smoothrefreshtime > remaining) return remaining;
            if (smoothrefreshtime < 20) return 20;
            return smoothrefreshtime;
        } catch (RemoteException ex) {
        }
        return 500;
    }
    
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case GOTO_POSITION:{
	        		if(mService != null){
	        			try{
	            			long[] mSongList = mService.getQueue();
	            			if (mSongList != null 
	            					&& mSongList.length > 0 
	            					&& (Long)msg.obj != mService.getAudioId())
	            			{
	            				for(int i = 0;i < mSongList.length;i++)
		            			{
		            				if(mSongList[i] ==  (Long)msg.obj)
		            				{
		            					mService.open(mSongList, i);
			            				mService.play();
		            					break;
		            				}
		            			}
	            			}
	        			}catch(RemoteException re){}
	        		}
	        	}
	        	break;
                case REFRESH:
                	Log.i(TAG, "<handleMessage> REFRESH",Log.APP);
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;
                    
                case QUIT:
                	Log.i(TAG, "<handleMessage> QUIT",Log.APP);
                    // This can be moved back to onCreate once the bug that prevents
                    // Dialogs from being started from onCreate/onResume is fixed.
                    new AlertDialog.Builder(MediaPlaybackActivity.this)
                            .setTitle(R.string.service_start_error_title)
                            .setMessage(R.string.service_start_error_msg)
                            .setPositiveButton(R.string.service_start_error_button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            finish();
                                        }
                                    })
                            .setCancelable(false)
                            .show();
                    break;

                /// M: Handle next and prev button. {@
                case NEXT_BUTTON:
                	Log.i(TAG, "<handleMessage> NEXT_BUTTON",Log.APP);
                    if (mService == null) {
                        return;
                    }
                    mNextButton.setEnabled(false);
                    mNextButton.setFocusable(false);
                    try {
                        mService.next();
                        mPosOverride = -1;
                    } catch (RemoteException ex) {
                        Log.e(TAG,"Error:" + ex,Log.APP);
                    }                
                    mNextButton.setEnabled(true);
                    mNextButton.setFocusable(true);
                    break;
                    
                case PREV_BUTTON:
                	Log.i(TAG, "<handleMessage> PREV_BUTTON",Log.APP);
                    if (mService == null) {
                        return;
                    }
                    mPrevButton.setEnabled(false);
                    mPrevButton.setFocusable(false);
                    try {
                        mPosOverride = -1;
                        mService.prev();
                    } catch (RemoteException ex) {
                    	Log.e(TAG, "Error:" + ex,Log.APP);
                    }
                    mPrevButton.setEnabled(true);
                    mPrevButton.setFocusable(true);
                    break;
                /// @}
                case MSG_ALBUM_ART_LOADING_FINISHED:
                	mAlbumArtMap = mAlbumArtLoadJob.get();
                	if(mAlbumArtMap != null && mAlbumArtMap.size() != 0)
                	{
                		updateCoverFlow();
                	}
                	break;

                default:
                    break;
            }
        }
    };

    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MediaPlaybackService.META_CHANGED)) {
            	Log.i(TAG, "<mStatusListener.onReceive> action:MediaPlaybackService.META_CHANGED",Log.APP);
                /// M: Refresh option menu when meta change
                invalidateOptionsMenu();
                // redraw the artist/title info and
                // set new max for progress bar
                updateTrackInfo();
                setPauseButtonImage();
                queueNextRefresh(1);
                
                //add by zjw
                getAlbumArtData();
            } else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
            	Log.i(TAG, "<mStatusListener.onReceive> action:MediaPlaybackService.PLAYSTATE_CHANGED",Log.APP);
                setPauseButtonImage();
            /// M: Handle more status. {@
            } else if (action.equals(MediaPlaybackService.QUIT_PLAYBACK)) {
            	Log.i(TAG, "<mStatusListener.onReceive> action:MediaPlaybackService.QUIT_PLAYBACK",Log.APP);
                mHandler.removeMessages(REFRESH);
                finish();
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                /// M: stop refreshing
            	Log.i(TAG, "<mStatusListener.onReceive> action:MediaPlaybackService.ACTION_SCREEN_OFF",Log.APP);
                mHandler.removeMessages(REFRESH);
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                /// M: restore refreshing
            	Log.i(TAG, "<mStatusListener.onReceive> action:MediaPlaybackService.ACTION_SCREEN_ON",Log.APP);
                long next = refreshNow();
                queueNextRefresh(next);
            }
            /// @}
        }
    };
    
    private void updateTrackInfo() {
    	Log.i(TAG, ">>> updateTrackInfo",Log.APP);
        if (mService == null) {
            return;
        }
        try {
            String path = mService.getPath();
            if (path == null) {
                finish();
                return;
            }
            
            long songid = mService.getAudioId(); 
            if (songid < 0 && path.toLowerCase().startsWith("http://")) {
                // Once we can get album art and meta data from MediaPlayer, we
                // can show that info again when streaming.
            	mArtistName.setText("");
                mTrackName.setText(path);
            } else {
                ((View) mArtistName.getParent()).setVisibility(View.VISIBLE);
                String artistName = mService.getArtistName();
                if (MediaStore.UNKNOWN_STRING.equals(artistName)) {
                    artistName = getString(R.string.unknown_artist_name);
                }
                mArtistName.setText(artistName);
                String albumName = mService.getAlbumName();
                long albumid = mService.getAlbumId();
                if (MediaStore.UNKNOWN_STRING.equals(albumName)) {
                    albumName = getString(R.string.unknown_album_name);
                    albumid = -1;
                }
                mTrackName.setText(mService.getTrackName());
            }
            mDuration = mService.duration();
            mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / 1000));
            /// M: For specific file, its duration need to be updated when playing. 
            recordDurationUpdateStatus();
        } catch (RemoteException ex) {
            finish();
        }
        Log.i(TAG, "updateTrackInfo >>>",Log.APP);
    }
    
    @SuppressWarnings("deprecation")
	private void updateCoverFlow()
    {
    	Log.i(TAG, "<updateCoverFlow>",Log.APP);
    	albumSongIdWrapperList = new ArrayList<AlbumSongIdWrapper>();
    	for(Map.Entry<Long, Long> item : mAlbumArtMap.entrySet())
    	{
    		albumSongIdWrapperList.add(new AlbumSongIdWrapper(item.getValue(),item.getKey()));
    	}
    	
    	AlbumArtAdapter adapter = new AlbumArtAdapter(this, albumSongIdWrapperList, coverFlow);
    	coverFlow.setAdapter(adapter);
    	coverFlow.setMaxZoom(MusicGallery.MAX_ZOOM_OUT);
        coverFlow.setEmptyView(null);
    	coverFlow.setSpacing(-50); // -65
    	
    	RelativeLayout.LayoutParams homePageGalleryParams = 
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 
						RelativeLayout.LayoutParams.FILL_PARENT);
		coverFlow.setLayoutParams(homePageGalleryParams);
		coverFlow.setPadding(2, 0, 2, 0);
		coverFlowParenet.removeAllViews();
    	coverFlowParenet.addView(coverFlow);
    	
    	coverFlow.setCallbackDuringFling(false);
    	coverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				long songId = (Long) arg1.getTag(R.id.songId);
				if(mCurrentPos != position){
	    			mHandler.removeMessages(GOTO_POSITION);
	    			Message msgSelected = mHandler.obtainMessage(GOTO_POSITION, songId);
	    			mHandler.sendMessageDelayed(msgSelected, TIME_OUT_DISPLAY); 
	    			mCurrentPos = position;
	    		}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
    	
    	updateCoverFlowPosition();
    }

    public class AlbumArtHandler extends Handler {
        private long mAlbumId = -1;
        
        public AlbumArtHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg)
        {
        	Log.i(TAG, "<handleMessage> GET_ALBUM_ART",Log.APP);
            /// M: Keep album art in mArtBitmap to improve loading speed when config changed.
            long albumid = ((AlbumSongIdWrapper) msg.obj).albumid;
            long songid = ((AlbumSongIdWrapper) msg.obj).songid;
            if (msg.what == GET_ALBUM_ART && (mAlbumId != albumid || albumid < 0 || mIsShowAlbumArt)) {
                Message numsg = null;
                // while decoding the new image, show the default album art
                if (mArtBitmap == null || mArtSongId != songid) {
                    numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
                    mHandler.removeMessages(ALBUM_ART_DECODED);
                    mHandler.sendMessageDelayed(numsg, 300);

                    // Don't allow default artwork here, because we want to fall back to song-specific
                    // album art if we can't find anything for the album.
                    /// M: if don't get album art from file,or the album art is not the same 
                    /// as the song ,we should get the album art again
                    mArtBitmap = MusicUtils.getArtwork(MediaPlaybackActivity.this,
                                                        songid, albumid, false);
                    Log.i(TAG, "get art. mArtSongId = " + mArtSongId 
                                            + " ,songid = " + songid + " ",Log.APP);
                    mArtSongId = songid;
                }
                
                if (mArtBitmap == null) {
                    mArtBitmap = MusicUtils.getDefaultArtwork(MediaPlaybackActivity.this);
                    albumid = -1;
                }
                if (mArtBitmap != null) {
                    numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, mArtBitmap);
                    mHandler.removeMessages(ALBUM_ART_DECODED);
                    mHandler.sendMessage(numsg);
                }
                mAlbumId = albumid;
                mIsShowAlbumArt = false;
            }
        }
    }
    
    private static class Worker implements Runnable {
        private final Object mLock = new Object();
        private Looper mLooper;
        
        /**
         * Creates a worker thread with the given name. The thread
         * then runs a {@link android.os.Looper}.
         * @param name A name for the new thread
         */
        Worker(String name) {
            Thread t = new Thread(null, this, name);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
            synchronized (mLock) {
                while (mLooper == null) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
        
        public Looper getLooper() {
            return mLooper;
        }
        
        public void run() {
            synchronized (mLock) {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mLock.notifyAll();
            }
            Looper.loop();
        }
        
        public void quit() {
            mLooper.quit();
        }
    }

    /**
     * M: move from onCreat, Update media playback activity ui. call this method
     * when activity oncreate or on configuration changed.
     */
    private void updateUI() {
    	Log.i(TAG, ">>> updateUI",Log.APP);
        setContentView(R.layout.media_play);
        
        coverFlowParenet = (RelativeLayout) findViewById(R.id.curPlayMiddle);
        coverFlow = new CoverFlow(this);
        
        findViewById(R.id.musiclibtab).setOnClickListener(mMusicLibListener);
        mCurrentTime = (TextView) findViewById(R.id.currenttime);
        mTotalTime = (TextView) findViewById(R.id.totaltime);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);

        mArtistName = (TextView) findViewById(R.id.artistname);
        mTrackName = (TextView) findViewById(R.id.trackname);

        mPrevButton = (RepeatingImageButton) findViewById(R.id.prev);
        mPrevButton.setOnClickListener(mPrevListener);
        mPrevButton.setRepeatListener(mRewListener, 260);
        mPauseButton = (ImageButton) findViewById(R.id.pause);
        mPauseButton.requestFocus();
        mPauseButton.setOnClickListener(mPauseListener);
        mNextButton = (RepeatingImageButton) findViewById(R.id.next);
        mNextButton.setOnClickListener(mNextListener);
        mNextButton.setRepeatListener(mFfwdListener, 260);
        seekmethod = 1;

        mDeviceHasDpad = (getResources().getConfiguration().navigation ==
            Configuration.NAVIGATION_DPAD);

        /// M: Only when in PORTRAIT we use button, otherwise we use action bar
        if (!mIsLandscape) {
            mQueueButton = (TextView) findViewById(R.id.curplaylist);
            mQueueButton.setOnClickListener(mQueueListener);
            mShuffleButton = ((ImageButton) findViewById(R.id.shuffle));
            mShuffleButton.setOnClickListener(mShuffleListener);
            mRepeatButton = ((ImageButton) findViewById(R.id.repeat));
            mRepeatButton.setOnClickListener(mRepeatListener);
        }

        if (mProgress instanceof SeekBar) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
        }
        mProgress.setMax(1000);

        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        
        Log.i(TAG, "updateUI >>>",Log.APP);
    }

    /**
     *  M: save the activity is in background.
     */
    @Override
    protected void onPause() {
        /// M: aviod Navigation button respond JE if Activity is background
    	super.onPause();
        mIsInBackgroud = true;
        Log.i(TAG, "<onPause> set mIsInBackgroud true",Log.APP);
    }

    /**
     *  M: handle config change.
     *
     * @param newConfig The new device configuration.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	Log.i(TAG, ">>> onConfigurationChanged",Log.APP);
        super.onConfigurationChanged(newConfig);
        /// M: When configuration change, get the current orientation
        mIsLandscape = (getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE);
        /// M: when configuration changed ,set mIsShowAlbumArt = true to update album art
        mIsShowAlbumArt = true;
        updateUI();
        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
        setRepeatButtonImage();
        setPauseButtonImage();
        setShuffleButtonImage();
        /// M: When back to this activity, ask service for right position
        mPosOverride = -1;
        /// M: Before invalidateOptionsMenu,save the input of SearchView @{
        if (mSearchItem != null) {
            SearchView searchView = (SearchView) mSearchItem.getActionView();
            mQueryText = searchView.getQuery();
            Log.i(TAG, "searchText:" + mQueryText,Log.APP);
        }
        /// @}
        /// M: Refresh action bar menu item
        invalidateOptionsMenu();
        Log.i(TAG, "onConfigurationChanged >>>",Log.APP);
    }

    /**
     * M: Search view query text listener.
     */
    SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        public boolean onQueryTextSubmit(String query) {
        	Log.i(TAG, "<onQueryTextSubmit> query:" + query,Log.APP);
            Intent intent = new Intent();
            intent.setClass(MediaPlaybackActivity.this, QueryBrowserActivity.class);
            intent.putExtra(SearchManager.QUERY, query);
            startActivity(intent);
            mSearchItem.collapseActionView();
            return true;
        }

        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    /**
     * M: get the background color when touched, it may get from thememager.
     * 
     * @return Return background color
     */
    private int getBackgroundColor() {
        /// M: default background color for ICS.
        final int defaultBackgroundColor = 0xcc0099cc;
        /// M: For ICS style and support for theme manager {@
        int ret = defaultBackgroundColor;
        //update by zjw
//        if (MusicFeatureOption.IS_SUPPORT_THEMEMANAGER) {
//            Resources res = getResources();
//            ret = res.getThemeMainColor();
//            if (ret == 0) {
//                ret = defaultBackgroundColor;
//            }
//        }
        return ret;
    }

    /**
     * M: update duration for MP3/AMR/AWB/AAC/FLAC formats.
     *
     * @param position The current positon for error check.
     */
    private void updateDuration(long position) {
    	Log.i(TAG, "<updateDuration> position:" + position,Log.APP);
        final int soundToMs = 1000;
        try {
            if (mNeedUpdateDuration && mService.isPlaying()) {
                long newDuration = mService.duration();

                if (newDuration > 0L && newDuration != mDuration) {
                    mDuration = newDuration;
                    mNeedUpdateDuration = false;
                    /// M: Update UI with new duration.
                    mTotalTime.setText(MusicUtils.makeTimeString(this, mDuration / soundToMs));
                    Log.i(TAG, "new duration updated!!",Log.APP);
                }
            } else if (position < 0 || position >= mDuration) {
                mNeedUpdateDuration = false;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "Error:" + ex,Log.APP);
        }
    }

    /**
     * M: record duration update status when playing,
     * if play mp3/aac/amr/awb/flac file, set mNeedUpdateDuration to update
     * layter in updateDuration().
     */
    private void recordDurationUpdateStatus() {
    	Log.i(TAG, "<recordDurationUpdateStatus>",Log.APP);
        final String mimeTypeMpeg = "audio/mpeg";
        final String mimeTypeAmr = "audio/amr";
        final String mimeTypeAmrWb = "audio/amr-wb";
        final String mimeTypeAac = "audio/aac";
        final String mimeTypeFlac = "audio/flac";
        String mimeType;
        mNeedUpdateDuration = false;
        try {
            mimeType = mService.getMIMEType();
        } catch (RemoteException ex) {
            Log.e(TAG, "Error:" + ex,Log.APP);
            mimeType = null;
        }
        if (mimeType != null) {
            Log.i(TAG, "mimeType=" + mimeType,Log.APP);
            if (mimeType.equals(mimeTypeMpeg) 
                || mimeType.equals(mimeTypeAmr) 
                || mimeType.equals(mimeTypeAmrWb) 
                || mimeType.equals(mimeTypeAac)
                || mimeType.equals(mimeTypeFlac)) {
                mNeedUpdateDuration = true;
            }
        }
    }

    /**
     * M: Add NFC callback to provide the uri.
     */
    @Override
    public Uri[] createBeamUris(NfcEvent event) {
        Uri currentUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, MusicUtils.getCurrentAudioId());;
        Log.i(TAG, "<createBeamUris> currentUri" + currentUri,Log.APP);
        return new Uri[] {currentUri};
    }
    /**
     * M: Call when search request and expand search action view.
     */
    @Override
    public boolean onSearchRequested() {
        if (mSearchItem != null) {
            mSearchItem.expandActionView();
        }
        return true;
    }
    
    //add by zjw
    private Future<Map<Long,Long>> mAlbumArtLoadJob;
    private Map<Long,Long> mAlbumArtMap;
    private ArrayList<AlbumSongIdWrapper> albumSongIdWrapperList;
    private static int mCurrentPos = -1;
    private static final int GOTO_POSITION = 12;
    private static final int MSG_ALBUM_ART_LOADING_FINISHED = 8;
    private static final int TIME_OUT_DISPLAY = 500;
    
    private FutureListener<Map<Long,Long>> mAlbumArtLoadListener = 
    		new FutureListener<Map<Long,Long>>() {
		@Override
		public synchronized void onFutureDone(Future<Map<Long,Long>> future) {
			if (future != mAlbumArtLoadJob) {
				return;
			}
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_ALBUM_ART_LOADING_FINISHED;
			mHandler.sendMessage(msg);
		}
	};
    
    private void getAlbumArtData()
	{
    	if(mService == null)
    	{
    		return;
    	}
    	try {
			if(mAlbumArtMap == null || mAlbumArtMap.size() == 0 || mAlbumArtMap.size() != mService.getQueue().length)
			{
				mAlbumArtLoadJob = MusicUtils.submitLoadPlayListJob(mService, mAlbumArtLoadListener);
			}else
			{
				updateCoverFlowPosition();
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
    
    private void updateCoverFlowPosition()
    {
    	if(mService != null)
		{
			try {
				long songId = mService.getAudioId();
				int i = 0;
				for(Map.Entry<Long, Long> item : mAlbumArtMap.entrySet())
				{
					if(songId == item.getKey())
					{
						break;
					}
					i++;
				}
				coverFlow.setSelection(i, true);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
    }
}

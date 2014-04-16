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

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.custom.music.util.Constants;
import com.custom.music.util.Log;
import com.custom.music.util.MusicUtils;
import com.custom.music.util.MusicUtils.ServiceToken;

public class QueryBrowserActivity extends ListActivity
implements MusicUtils.Defs, ServiceConnection
{
    private static final String TAG = "QueryBrowserActivity";
    private final static int PLAY_NOW = 0;
    private final static int ADD_TO_QUEUE = 1;
    private final static int PLAY_NEXT = 2;
    private final static int PLAY_ARTIST = 3;
    private final static int EXPLORE_ARTIST = 4;
    private final static int PLAY_ALBUM = 5;
    private final static int EXPLORE_ALBUM = 6;
    private final static int REQUERY = 3;
    private QueryListAdapter mAdapter;
    private boolean mAdapterSent;
    private String mFilterString = "";
    private ServiceToken mToken;
    
    /// M: reset SD card status to refresh database error UI @{
    private final static int LIST_SHOW = 7;
    private final static int EMPTY_SHOW = 8;
    private final static int RESCAN = 9;
    private boolean mResetSdStatus = false;
    private boolean mIsMounted = true;
    private TextView mEmptyView = null;
    /// @}

    /// M: Query cursor columns
    private final String[] mCursorCols = new String[] {
            BaseColumns._ID,   // this will be the artist, album or track ID
            MediaStore.Audio.Media.MIME_TYPE, // mimetype of audio file, or "artist" or "album"
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Media.TITLE,
            "data1",
            "data2"
    };
    public QueryBrowserActivity()
    {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mAdapter = (QueryListAdapter) getLastNonConfigurationInstance();
        mToken = MusicUtils.bindToService(this, this);
        // defer the real work until we're bound to the service
    }


    public void onServiceConnected(ComponentName name, IBinder service) {
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        f.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        /// M: handle when sdcard eject and mounted action. {@
        f.addAction(Intent.ACTION_MEDIA_MOUNTED);
        /// @}
        f.addDataScheme("file");
        registerReceiver(mScanListener, f);
        
        Intent intent = getIntent();
        String action = intent != null ? intent.getAction() : null;
        
        if (Intent.ACTION_VIEW.equals(action)) {
            // this is something we got from the search bar
            Uri uri = intent.getData();
            String path = uri.toString();
            if (path.startsWith("content://media/external/audio/media/")) {
                // This is a specific file
                String id = uri.getLastPathSegment();
                long [] list = new long[] { Long.valueOf(id) };
                MusicUtils.playAll(this, list, 0);
                finish();
                return;
            } else if (path.startsWith("content://media/external/audio/albums/")) {
                // This is an album, show the songs on it
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setDataAndType(Uri.EMPTY, Constants.TRACK_MIME_TYPE);
                i.putExtra("album", uri.getLastPathSegment());
                startActivity(i);
                finish();
                return;
            } else if (path.startsWith("content://media/external/audio/artists/")) {
                // This is an artist, show the albums for that artist
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setDataAndType(Uri.EMPTY, Constants.ALBUM_MIME_TYPE);
                i.putExtra("artist", uri.getLastPathSegment());
                startActivity(i);
                finish();
                return;
            }
        }

        mFilterString = intent.getStringExtra(SearchManager.QUERY);
        if (MediaStore.INTENT_ACTION_MEDIA_SEARCH.equals(action)) {
            String focus = intent.getStringExtra(MediaStore.EXTRA_MEDIA_FOCUS);
            String artist = intent.getStringExtra(MediaStore.EXTRA_MEDIA_ARTIST);
            String album = intent.getStringExtra(MediaStore.EXTRA_MEDIA_ALBUM);
            String title = intent.getStringExtra(MediaStore.EXTRA_MEDIA_TITLE);
            if (focus != null) {
                if (focus.startsWith("audio/") && title != null) {
                    mFilterString = title;
                } else if (focus.equals(MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE)) {
                    if (album != null) {
                        mFilterString = album;
                        if (artist != null) {
                            mFilterString = mFilterString + " " + artist;
                        }
                    }
                } else if (focus.equals(MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE)) {
                    if (artist != null) {
                        mFilterString = artist;
                    }
                }
            }
        }

        setContentView(R.layout.query_activity);
        mTrackList = getListView();
        mTrackList.setTextFilterEnabled(true);
        mEmptyView = (TextView)findViewById(R.id.empty_show);
        if (mAdapter == null) {
            mAdapter = new QueryListAdapter(
                    getApplication(),
                    this,
                    R.layout.track_list_item,
                    null, // cursor
                    new String[] {},
                    new int[] {});
            setListAdapter(mAdapter);
            if (TextUtils.isEmpty(mFilterString)) {
                getQueryCursor(mAdapter.getQueryHandler(), null);
            } else {
                mTrackList.setFilterText(mFilterString);
                mFilterString = null;
            }
        } else {
            mAdapter.setActivity(this);
            setListAdapter(mAdapter);
            mQueryCursor = mAdapter.getCursor();
            if (mQueryCursor != null) {
                init(mQueryCursor);
            } else {
                getQueryCursor(mAdapter.getQueryHandler(), mFilterString);
            }
        }
    }

    public void onServiceDisconnected(ComponentName name) {
        
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        mAdapterSent = true;
        return mAdapter;
    }
    
    @Override
    public void onPause() {
        Log.i(TAG, "<onPause>",Log.APP);
        mReScanHandler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MusicUtils.unbindFromService(mToken);
        unregisterReceiver(mScanListener);
        // If we have an adapter and didn't send it off to another activity yet, we should
        // close its cursor, which we do by assigning a null cursor to it. Doing this
        // instead of closing the cursor directly keeps the framework from accessing
        // the closed cursor later.
        if (!mAdapterSent && mAdapter != null) {
            mAdapter.changeCursor(null);
        }
        // Because we pass the adapter to the next activity, we need to make
        // sure it doesn't keep a reference to this activity. We can do this
        // by clearing its DatasetObservers, which setListAdapter(null) does.
        if (getListView() != null) {
            setListAdapter(null);
        }
        mAdapter = null;
        super.onDestroy();
    }
    
    /*
     * This listener gets called when the media scanner starts up, and when the
     * sd card is unmounted.
     * M: Listen more status changed about eject and mounted.
     */
    private BroadcastReceiver mScanListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String status = Environment.getExternalStorageState();
            Log.i(TAG, "mScanListener.onReceive:" + action + ", status = " + status,Log.APP);
            if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
                MusicUtils.setSpinnerState(QueryBrowserActivity.this);
                mResetSdStatus = true;
                mReScanHandler.sendMessage(mReScanHandler.obtainMessage(RESCAN));
                mReScanHandler.sendMessageDelayed(mReScanHandler.obtainMessage(RESCAN), 1000);
            } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
                mIsMounted = false;
                mResetSdStatus = true;
                mReScanHandler.sendMessage(mReScanHandler.obtainMessage(RESCAN));
                closeContextMenu();
                closeOptionsMenu();
            mReScanHandler.sendMessageDelayed(mReScanHandler.obtainMessage(RESCAN), 1000);
            } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                mIsMounted = true;
                mReScanHandler.sendMessage(mReScanHandler.obtainMessage(RESCAN));
            }
        }
    };
    /**
     * M: use Handler update UI to avoid update UI in background thread,show empty view or listview.
     */
    private Handler mReScanHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case EMPTY_SHOW :
                    mTrackList.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                    break;
                case LIST_SHOW :
                    mTrackList.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                    break;
                case RESCAN :
                    if (mAdapter != null) {
                        getQueryCursor(mAdapter.getQueryHandler(), null);
                    }
                    break;
                // if the query results in a null cursor, onQueryComplete() will
                // call init(), which will post a delayed message to this
                // handler
                // in order to try again.
                default :
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case SCAN_DONE:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else {
                    getQueryCursor(mAdapter.getQueryHandler(), null);
                }
                break;
        }
    }
    
    public void init(Cursor c) {

        if (mAdapter == null) {
            return;
        }
        mAdapter.changeCursor(c);

        if (mQueryCursor == null) {
            /// M: Reset sd status to make sure show sdcard error UI at right time.
            if (mResetSdStatus) {
                MusicUtils.resetSdStatus();
                mResetSdStatus = false;
            }

            if(mEmptyView.getVisibility() == View.VISIBLE) {
                mEmptyView.setVisibility(View.GONE);
            }

            MusicUtils.displayDatabaseError(this,mIsMounted);
            setListAdapter(null);
            mReScanHandler.sendMessageDelayed(mReScanHandler.obtainMessage(RESCAN), 1000);
            return;
        }
        MusicUtils.hideDatabaseError(this);
        Log.e(TAG, "c.getCount() " + c.getCount() + "getListView().getCount()"+ getListView().getCount(),Log.APP);
        /// M: when cursor's count be 0;means no matches media files,so show 
        // empty view,otherwise show seached listview. @{
        int what = (c.getCount() == 0) ? EMPTY_SHOW : LIST_SHOW;
        mReScanHandler.sendMessage(mReScanHandler.obtainMessage(what));
        /// @}
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id)
    {
        // Dialog doesn't allow us to wait for a result, so we need to store
        // the info we need for when the dialog posts its result
        mQueryCursor.moveToPosition(position);
        if (mQueryCursor.isBeforeFirst() || mQueryCursor.isAfterLast()) {
            return;
        }
        String selectedType = mQueryCursor.getString(mQueryCursor.getColumnIndexOrThrow(
                MediaStore.Audio.Media.MIME_TYPE));
        
        if ("artist".equals(selectedType)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            /// M: Marked for start a new activity.
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setDataAndType(Uri.EMPTY, Constants.ALBUM_MIME_TYPE);
            intent.putExtra("artist", Long.valueOf(id).toString());
            startActivity(intent);
        } else if ("album".equals(selectedType)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            /// M: Marked for start a new activity.
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //update by zjw
            intent.setDataAndType(Uri.parse("custom"), Constants.TRACK_MIME_TYPE);
            intent.putExtra("album", Long.valueOf(id).toString());
            startActivity(intent);
        } else if (position >= 0 && id >= 0) {
            long [] list = new long[] { id };
            MusicUtils.playAll(this, list, 0);
        } else {
            Log.e(TAG, "invalid position/id: " + position + "/" + id,Log.APP);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case USE_AS_RINGTONE: {
                // Set the system setting to make this the current ringtone
                MusicUtils.setRingtone(this, mTrackList.getSelectedItemId());
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Cursor getQueryCursor(AsyncQueryHandler async, String filter) {
        if (filter == null) {
            filter = "";
        }
        

        Uri search = Uri.parse("content://media/external/audio/search/fancy/" +
                Uri.encode(filter));
        
        Cursor ret = null;
        if (async != null) {
            async.startQuery(0, null, search, mCursorCols, null, null, null);
        } else {
            ret = MusicUtils.query(this, search, mCursorCols, null, null, null);
        }
        if (ret != null) {
            Log.e(TAG, " ret != null  getQueryCursor: Count=" + ret.getCount() + 
                " search=" + filter,Log.APP);
            /// M: when cursor's count be 0;means no matches media files,so show 
            // empty view,otherwise show seached listview. @{
            int what = (ret.getCount() == 0) ? EMPTY_SHOW : LIST_SHOW;
            mReScanHandler.sendMessage(mReScanHandler.obtainMessage(what));
            /// @}
        }
        return ret;
    }
    
    static class QueryListAdapter extends SimpleCursorAdapter {
        private QueryBrowserActivity mActivity = null;
        private AsyncQueryHandler mQueryHandler;
        private String mConstraint = null;
        private boolean mConstraintIsValid = false;

        class QueryHandler extends AsyncQueryHandler {
            QueryHandler(ContentResolver res) {
                super(res);
            }

            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                if (cursor != null) {
                    Log.e(TAG, "onQueryComplete: Conut=" + cursor.getCount(),Log.APP);
                }
                mActivity.init(cursor);
            }
        }

        QueryListAdapter(Context context, QueryBrowserActivity currentactivity,
                int layout, Cursor cursor, String[] from, int[] to) {
            super(context, layout, cursor, from, to);
            mActivity = currentactivity;
            mQueryHandler = new QueryHandler(context.getContentResolver());
        }

        public void setActivity(QueryBrowserActivity newactivity) {
            mActivity = newactivity;
        }
        
        public AsyncQueryHandler getQueryHandler() {
            return mQueryHandler;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            
            TextView tv1 = (TextView) view.findViewById(R.id.line1);
            TextView tv2 = (TextView) view.findViewById(R.id.line2);
            ImageView iv = (ImageView) view.findViewById(R.id.icon);
            ViewGroup.LayoutParams p = iv.getLayoutParams();
            if (p == null) {
                // seen this happen, not sure why
                DatabaseUtils.dumpCursor(cursor);
                return;
            }
            p.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            p.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            
            String mimetype = cursor.getString(cursor.getColumnIndexOrThrow(
                    MediaStore.Audio.Media.MIME_TYPE));
            Log.i(TAG, "bindView: mimetype=" + mimetype,Log.APP);
            if (mimetype == null) {
                mimetype = "audio/";
            }
            if (mimetype.equals("artist")) {
                iv.setImageResource(R.drawable.ic_mp_artist_list);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Artists.ARTIST));
                String displayname = name;
                boolean isunknown = false;
                if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                    displayname = context.getString(R.string.unknown_artist_name);
                    isunknown = true;
                }
                tv1.setText(displayname);

                int numalbums = cursor.getInt(cursor.getColumnIndexOrThrow("data1"));
                int numsongs = cursor.getInt(cursor.getColumnIndexOrThrow("data2"));
                
                String songs_albums = MusicUtils.makeAlbumsSongsLabel(context,
                        numalbums, numsongs, isunknown);
                
                tv2.setText(songs_albums);
            
            } else if (mimetype.equals("album")) {
                iv.setImageResource(R.drawable.albumart_mp_unknown_list);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Albums.ALBUM));
                String displayname = name;
                if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                    displayname = context.getString(R.string.unknown_album_name);
                }
                tv1.setText(displayname);
                
                name = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Artists.ARTIST));
                displayname = name;
                if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                    displayname = context.getString(R.string.unknown_artist_name);
                }
                tv2.setText(displayname);
                
            } else if (mimetype.startsWith("audio/") ||
                    mimetype.equals("application/ogg") ||
                    mimetype.equals("application/x-ogg")) {
                iv.setImageResource(R.drawable.ic_mp_song_list);
                String name = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Media.TITLE));
                tv1.setText(name);

                String displayname = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Artists.ARTIST));
                if (displayname == null || displayname.equals(MediaStore.UNKNOWN_STRING)) {
                    displayname = context.getString(R.string.unknown_artist_name);
                }
                name = cursor.getString(cursor.getColumnIndexOrThrow(
                        MediaStore.Audio.Albums.ALBUM));
                if (name == null || name.equals(MediaStore.UNKNOWN_STRING)) {
                    name = context.getString(R.string.unknown_album_name);
                }
                tv2.setText(displayname + " - " + name);
            }
        }
        @Override
        public void changeCursor(Cursor cursor) {
            if (mActivity.isFinishing() && cursor != null) {
                cursor.close();
                cursor = null;
            }
            if (cursor != mActivity.mQueryCursor) {
                mActivity.mQueryCursor = cursor;
                super.changeCursor(cursor);
            }
        }
        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            String s = constraint.toString();
            if (mConstraintIsValid && (
                    (s == null && mConstraint == null) ||
                    (s != null && s.equals(mConstraint)))) {
                return getCursor();
            }
            Cursor c = mActivity.getQueryCursor(null, s);
            mConstraint = s;
            mConstraintIsValid = true;
            return c;
        }
    }

    private ListView mTrackList;
    private Cursor mQueryCursor;
}


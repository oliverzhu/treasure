package com.custom.music.bean;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import com.custom.music.util.Constants;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * @author jianwen.zhu
 * 2014/1/2
 */
public class AlbumSongIdWrapper {
	private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    public long albumid;
    public long songid;
    
    public AlbumSongIdWrapper(){}
    
    public AlbumSongIdWrapper(long aid, long sid) {
        albumid = aid;
        songid = sid;
    }
    
    public String getUniqueName() throws IllegalArgumentException
    {
    	if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
    	return Constants.TRACK_MIME_TYPE + "/" + songid;
    }
    
    public FileDescriptor getAlbumArtFileDescriptor (Context context)
    {
    	ParcelFileDescriptor pfd = null;
    	if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
    	try {
			if (albumid < 0) {
			    Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
			    pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			    if (pfd != null) {
			        return  pfd.getFileDescriptor();
			    }
			} else {
			    Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
			    pfd = context.getContentResolver().openFileDescriptor(uri, "r");
			    if (pfd != null) {
			        return pfd.getFileDescriptor();
			    }
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	return null;
    }
}

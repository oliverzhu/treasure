package com.custom.music.job;

import java.util.Map;

import com.custom.music.IMediaPlaybackService;
import com.custom.music.util.Log;
import com.custom.music.util.thread.ThreadPool;
import com.custom.music.util.thread.ThreadPool.JobContext;

/**
 * @author jianwen.zhu
 * 2014/1/2
 */
public class LoadPlaylistJob implements ThreadPool.Job<Map<Long,Long>>{
	private static final String TAG = "LoadPlaylistJob";
	private IMediaPlaybackService mService;
	
	public LoadPlaylistJob(IMediaPlaybackService mService){
		this.mService = mService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<Long,Long> run(JobContext jc) {
		try {
			if(mService == null)
			{
				return null;
			}
			return mService.getAlbumArt();
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "load playlist error", Log.APP);
		} 
		return null;
	}

}

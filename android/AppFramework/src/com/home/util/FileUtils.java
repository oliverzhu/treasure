package com.home.util;

import java.io.File;

import com.home.util.log.Log;

/**
 * IO操作...
 * @author jianwen.zhu
 * 2013/12/6
 */
public class FileUtils {
	private static final String TAG = "FileUtils";
	
	/**
	 * @param f
	 * @throws Exception
	 */
	public static void createNewFile(File f) throws Exception {
		Log.i(TAG, "create file:" + f, Log.APP);
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		if (!f.exists()) {
			f.createNewFile();
		}
	}
}

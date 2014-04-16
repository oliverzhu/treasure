package com.home.url;
import android.os.Environment;

/**
 * 
 * @author Oliverzhu
 * 2013/4/14
 */
public class FileDirProvider {

	public static String root = Environment.getExternalStorageDirectory() + "/myapp";

	public static String apk = root + "/apk";

}

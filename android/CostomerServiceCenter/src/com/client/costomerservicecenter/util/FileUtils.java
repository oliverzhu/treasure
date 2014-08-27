package com.client.costomerservicecenter.util;

import java.io.File;

public class FileUtils {
	/**
	 * create new file
	 * 
	 * @param f
	 * @throws Exception
	 */
	public static void createNewFile(File f) throws Exception {
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		if (!f.exists()) {
			f.createNewFile();
		}
	}

	/**
	 * 删除指定目录下的文件
	 * 
	 * @param dir
	 */
	public static void deleteFile(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			File[] childs = dir.listFiles();
			for (File f : childs) {
				if (f.exists() && f.isDirectory()) {
					continue;
				} else if (f.exists() && f.isFile()) {
					f.delete();
				}
			}
			dir.delete();
		} else if (dir.exists() && dir.isFile()) {
			dir.delete();
		}
	}

	/**
	 * 递归删除目录
	 * 
	 * @param dir
	 */
	public static void deleteDir(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			File[] childs = dir.listFiles();
			for (File f : childs) {
				if (f.exists() && f.isDirectory()) {
					deleteDir(f);
				} else if (f.exists()) {
					f.delete();
				}
			}
			dir.delete();
		} else if (dir.exists()) {
			dir.delete();
		}
	}

}

package com.home.io;
import java.io.File;


public class DeleteFiles {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filePath = "D:\\wendao";
		File f = new File(filePath);
		
		deleteDir(f);
	}
	
	public static void deleteDir(File dir) {
		if (dir.exists() && dir.isDirectory()) {
			File[] childs = dir.listFiles();
			for (File f : childs) {
				if (f.exists() && f.isDirectory()) {
					deleteDir(f);
				} else if (f.exists()) {
					deleteFile(f);
				}
			}
			if(dir.getName().startsWith("._"))
			{
				dir.delete();
			}
//			dir.delete();
		} else if (dir.exists()) {
			deleteFile(dir);
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
				} else if (f.exists() && f.isFile() && f.getName().startsWith(".")) {
					f.delete();
				}
			}
			if(dir.getName().startsWith("."))
			{
				dir.delete();
			}
//			dir.delete();
		} else if (dir.exists() && dir.isFile() && dir.getName().startsWith(".")) {
			dir.delete();
		}
	}

}

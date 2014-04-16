package com.home.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipCompress {
	/**
	 * 压缩
	 * @param args
	 * @throws IOException
	 */
	public static void zip(String ...args) throws IOException
	{
		FileOutputStream f = new FileOutputStream("./test.zip");
		
		ZipOutputStream zos = new ZipOutputStream(f);
		
		BufferedOutputStream out = new BufferedOutputStream(zos);
		
		zos.setComment("A test of Java Zipping");
		
		for(String arg : args)
		{
			File file = new File(arg);
//          使用字符级别的会产生乱码内容,使用字节api虽然可以避免内容的乱码 但是避免不了名字的乱码，（ant.jar）可以解决问题
//			BufferedReader in = 
//					new BufferedReader(
//							new FileReader(
//									file.getAbsoluteFile()));
			
			FileInputStream fos = new FileInputStream(file); 
			BufferedInputStream in = new BufferedInputStream(fos);
			zos.putNextEntry(new ZipEntry(file.getName()));
			int c;
			byte[] bytes = new byte[1024];
			while((c = in.read(bytes)) != -1)
			{
				out.write(bytes,0,c);
			}
			in.close();
			fos.close();
			out.flush();
		}
		out.close();
	}
	
	public static void unzip() throws IOException
	{
		FileInputStream fi = new FileInputStream("./test.zip");
		ZipInputStream zis = new ZipInputStream(fi);
		BufferedInputStream bis = new BufferedInputStream(zis);
		
		ZipEntry ze;
		
		while((ze = zis.getNextEntry()) != null)
		{
			String filename = ze.getName();
			File destFile = new File("./" + filename);
			FileOutputStream out = new FileOutputStream(destFile);
			byte[] buffer = new byte[1024];
			int count;
			while ((count = zis.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
			if (out != null) {
				out.close();
			}
		}
		
		bis.close();
		zis.close();
		fi.close();
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException{
//		zip(args);
		
//		unzip();
		
		File file = new File("../..", "java");
		System.out.println(file.isDirectory());
	}

}

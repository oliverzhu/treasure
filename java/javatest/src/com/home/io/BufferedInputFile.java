package com.home.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class BufferedInputFile {
	public static String read(String filename) throws IOException
	{
		BufferedReader in = new BufferedReader(
				new FileReader(filename));
		String s;
		StringBuilder sb = new StringBuilder();
		while((s = in.readLine()) != null)
		{
			sb.append(s + "\n");
		}
		in.close();
		return sb.toString();
	}

	public static void main(String[] args) {
		File f = new File(".");
		System.out.println(f.getAbsolutePath());
		try {
			System.out.println(read(
					"./src/com/home/io/BufferedInputFile.java"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

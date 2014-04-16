package com.home.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * 流一定要记得关，否则产生意想不到的后果
 * 将文件处理成行
 * @author think in java
 *
 */
public class TextFile extends ArrayList<String> {

	private static final long serialVersionUID = -8854023807342264041L;
	
	public static String read(String fileName)
	{
		StringBuilder sb = new StringBuilder();
		
		try {
			BufferedReader in = new BufferedReader(
					new FileReader(
							new File(fileName).getAbsoluteFile()));
			
			try {
				String s;
				while((s = in.readLine()) != null)
				{
					sb.append(s);
					sb.append("\n");
				}
			} finally{
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
		
		return sb.toString();
	}
	
	public static void write(String fileName,String text)
	{
		try {
			PrintWriter out = 
					new PrintWriter(
							new File(fileName).getAbsoluteFile());
			try {
				out.print(text);
			} finally{
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public TextFile(String fileName,String splitter)
	{
		super(Arrays.asList(read(fileName).split(splitter)));
		
		if(get(0).equals(""))
			remove(0);
	}
	
	public TextFile(String fileName)
	{
		this(fileName,"\n");
	}
	
	public void write(String fileName)
	{
		try {
			PrintWriter out = 
					new PrintWriter(
							new File(fileName).getAbsoluteFile());
			try {
				for(String item : this)
				{
					out.println(item);
				}
			} finally{
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static void main(String[] args) {
		String file = read("./src/com/home/io/HexStringTest.java");
		
		write("./test.txt",file);
		
		TextFile text = new TextFile("./test.txt");
		
		text.write("./test2.txt");
	}
}

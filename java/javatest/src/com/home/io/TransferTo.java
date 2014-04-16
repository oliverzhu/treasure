package com.home.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class TransferTo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		File 
			file1 = new File("./test.txt"),
			file2 = new File("./test3.txt");
		FileChannel 
			in = new FileInputStream(file1).getChannel(),
			out = new FileOutputStream(file2).getChannel();
		
		in.transferTo(0, in.size(), out);
		
		in.close();
		
		out.close();
		
	}

}

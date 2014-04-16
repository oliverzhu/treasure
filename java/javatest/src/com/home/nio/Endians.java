package com.home.nio;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import static com.home.common.Print.*;

public class Endians {
	 public static void main(String[] args) {
		    ByteBuffer bb = ByteBuffer.wrap(new byte[12]);
		    bb.asCharBuffer().put("abcdef");
		    print(Arrays.toString(bb.array()));
		    bb.rewind();
		    bb.order(ByteOrder.BIG_ENDIAN);
		    bb.asCharBuffer().put("abcdef");
		    print(Arrays.toString(bb.array()));
		    bb.rewind();
		    bb.order(ByteOrder.LITTLE_ENDIAN);
		    bb.asCharBuffer().put("abcdef");
		    print(Arrays.toString(bb.array()));
		    
		    
		    String str = "adb";
		    char[] chars = str.toCharArray();
		    MappedByteBuffer out = null;
			try {
				out = new RandomAccessFile("./map_char_endian.dat", "rw").getChannel()
		  	      .map(FileChannel.MapMode.READ_WRITE, 0, chars.length * 2);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
//			out.order(ByteOrder.LITTLE_ENDIAN);
		    CharBuffer charBuffer = out.asCharBuffer();
		    
		    for(int i = 0;i < chars.length;i++)
		    {
		    	charBuffer.put(chars[i]);
		    }
		    
		    charBuffer.rewind();
		    
		    while(charBuffer.hasRemaining())
		    {
		    	print(charBuffer.get());
		    }
//		    for(int i = 0;i < chars.length;i++)
//		    {
//		    	print((byte)charBuffer.get());
//		    }
		    
		    int i = 1233333;
		    MappedByteBuffer out_int = null;
			try {
				out_int = new RandomAccessFile("./map_int_endian.dat", "rw").getChannel()
		  	      .map(FileChannel.MapMode.READ_WRITE, 0, 4);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			IntBuffer intBuffer = out_int.asIntBuffer();
			intBuffer.put(i);
			
			intBuffer.rewind();
			
			while(intBuffer.hasRemaining())
		    {
		    	print(intBuffer.get());
		    }
	 }
}

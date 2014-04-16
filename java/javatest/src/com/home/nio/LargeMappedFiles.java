package com.home.nio;

import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import static com.home.common.Print.*;

/**
 * 对内存映射文件写入数据
 * @author jianwen.zhu
 *
 */
public class LargeMappedFiles {
	//规定映射内存的大小
	static int length = 0x0000002; // 字节长度：1024B
	  public static void main(String[] args) throws Exception {
	    MappedByteBuffer out =
	      new RandomAccessFile("./map_file_test.dat", "rw").getChannel()
	      .map(FileChannel.MapMode.READ_WRITE, 0, length);
	    //默认是覆盖的方式
	    for(int i = 0; i < length; i++)
	      out.put((byte)'s');
	    print("Finished writing");
	    
	    //映射的内存只有两个字节，虽然文件中的字符有很多，但是在内存中的大小是由两个自己，所以在取的时候会数组越界
	    for(int i = length/2; i < length/2 + 6; i++)
	      printnb((char)out.get(i));
	  }
}

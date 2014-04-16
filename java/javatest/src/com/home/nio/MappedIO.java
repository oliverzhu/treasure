package com.home.nio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.IntBuffer;
import java.nio.channels.FileChannel;

public class MappedIO {
	private static int numOfInts = 100;
	  private static int numOfUbuffInts = 10;
	  private abstract static class Tester {
	    private String name;
	    public Tester(String name) { this.name = name; }
	    public void runTest() {
	      System.out.print(name + ": ");
	      try {
	        long start = System.nanoTime();
	        test();
	        double duration = System.nanoTime() - start;
	        System.out.format("%.2f\n", duration/1.0e9);
	      } catch(IOException e) {
	        throw new RuntimeException(e);
	      }
	    }
	    public abstract void test() throws IOException;
	  }
	  private static Tester[] tests = {
	    new Tester("Stream Write") {
	      public void test() throws IOException {
	        DataOutputStream dos = new DataOutputStream(
	          new BufferedOutputStream(
	            new FileOutputStream(new File("./map_io.txt"))));
	        for(int i = 0; i < numOfInts; i++)
	          dos.writeInt(i);
	        dos.close();
	      }
	    },
	    new Tester("Mapped Write") {
	      public void test() throws IOException {
	        FileChannel fc =
	          new RandomAccessFile("./map_io.txt", "rw")
	          .getChannel();
	        IntBuffer ib = fc.map(
	          FileChannel.MapMode.READ_WRITE, 0, fc.size())
	          .asIntBuffer();
	        for(int i = 0; i < numOfInts; i++)
	          ib.put(i);
	        fc.close();
	      }
	    },
	    new Tester("Stream Read") {
	      public void test() throws IOException {
	        DataInputStream dis = new DataInputStream(
	          new BufferedInputStream(
	            new FileInputStream("./map_io.txt")));
	        int value = -1;
	        for(int i = 0; i < numOfInts; i++)
	        	value = dis.readInt();
	        dis.close();
	      }
	    },
	    new Tester("Mapped Read") {
	      public void test() throws IOException {
	        FileChannel fc = new FileInputStream(
	          new File("./map_io.txt")).getChannel();
	        IntBuffer ib = fc.map(
	          FileChannel.MapMode.READ_ONLY, 0, fc.size())
	          .asIntBuffer();
	        int value = -1;
	        while(ib.hasRemaining())
	        	value = ib.get();
	        fc.close();
	      }
	    },
	    new Tester("Stream Read/Write") {
	      public void test() throws IOException {
	        RandomAccessFile raf = new RandomAccessFile(
	          new File("./map_io.txt"), "rw");
	        raf.writeInt(1);
	        long pointer = -1;
	        int value = -1;
	        for(int i = 0; i < numOfUbuffInts; i++) {
	          raf.seek(raf.length() - 4);//移动4个字节的位置刚好一个 int
	          value = raf.readInt();
	          pointer = raf.getFilePointer();
	          raf.writeInt(value);
	        }
	        raf.close();
	      }
	    },
	    new Tester("Mapped Read/Write") {
	      public void test() throws IOException {
	        FileChannel fc = new RandomAccessFile(
	          new File("./map_io.txt"), "rw").getChannel();
	        IntBuffer ib = fc.map(
	          FileChannel.MapMode.READ_WRITE, 0, fc.size())
	          .asIntBuffer();
	        ib.put(0);
	        int pointer = -1;
	        int value = -1;
	        for(int i = 1; i < numOfUbuffInts; i++)
	        {
	        	//返回当前位置的值，但是位置并不会变化
	        	 value = ib.get(i - 1);
	        	 //返回当前位置并把位置+1
		          pointer = ib.get();
		          ib.put(value);
	        }
	        fc.close();
	      }
	    }
	  };
	  public static void main(String[] args) {
	    for(Tester test : tests)
	      test.runTest();
	  }

}

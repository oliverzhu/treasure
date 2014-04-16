package com.home.thread;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

class SleepBlocked implements Runnable
{
	public void run() 
	{
		try {
			TimeUnit.SECONDS.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Exiting SleepBlocked.run()");
	};
}

class IOBlocked implements Runnable
{
	private InputStream in;
	
	public IOBlocked(InputStream is)
	{
		in = is;
	}
	@Override
	public void run() {
		try {
			System.out.println("Waiting for read():");
			in.read();
		} catch (IOException e) {
			if(Thread.currentThread().isInterrupted())
			{
				System.out.println("Interrupted from blocked I/O");
			}else
			{
				throw new RuntimeException(e);
			}
		}
		System.out.println("Exiting IOBlocked.run()");
	}
}

class SynchronizedBlocked implements Runnable
{
	public synchronized void f()
	{
		while(true)
		{
			Thread.yield();
		}
	}
	
	public SynchronizedBlocked()
	{
		new Thread()
		{
			public void run()
			{
				f();
			}
		}.start();
	}
	
	@Override
	public void run() {
		System.out.println("Trying to call f()");
		f();
		System.out.println("Exiting SynchronizedBlocked.run()");
	}
}
public class Interrupting {
	private static ExecutorService exec 
		= Executors.newCachedThreadPool();
	
	static void test(Runnable r) throws InterruptedException
	{
		Future<?> f = exec.submit(r);
		TimeUnit.MILLISECONDS.sleep(100);
		System.out.println("Interrunpting " + r.getClass().getName());
		f.cancel(true);
		System.out.println("Interrupt sent to " + r.getClass().getName());
	}
	
	public static void main(String[] args) throws Exception{
		test(new SleepBlocked());
		test(new IOBlocked(System.in));
		test(new SynchronizedBlocked());
		TimeUnit.SECONDS.sleep(3);
		System.out.println("Aborting with System.exit(0)");
		System.exit(0);
	}

}

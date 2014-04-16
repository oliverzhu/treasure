package com.home.thread;

import java.io.IOException;

public class ResponsiveUi extends Thread {
	private static volatile double d = 1;
	
	public ResponsiveUi()
	{
		setDaemon(true);
		start();
	}
	
	@Override
	public void run() {
		while (true) {
			d = d + (Math.PI + Math.E) / d;
		}
	}
	
	public static void main(String[] args) {
		new ResponsiveUi();
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(d);
	}

}

package com.home.queue;

import java.util.concurrent.LinkedBlockingQueue;

public class InQueue<T>{

	protected LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>();
	
	
	public InQueue() {
	}
	
	public LinkedBlockingQueue<T> getQueue() {
		return this.queue;
	}

	public T take() {
		try {
			T packet = queue.take();
			return packet;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}

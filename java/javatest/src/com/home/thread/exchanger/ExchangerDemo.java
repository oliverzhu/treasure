package com.home.thread.exchanger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.home.common.BasicGenerator;
import com.home.common.Generator;
import com.home.thread.semaphore.Fat;

class ExchangerProducer<T> implements Runnable
{
	private Generator<T> generator;
	private Exchanger<List<T>> exchanger;
	private List<T> holder;
	
	public ExchangerProducer(Exchanger<List<T>> exchg,
			Generator<T> gen,List<T> holder) {
		exchanger = exchg;
		generator = gen;
		this.holder = holder;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				for(int i = 0;i < ExchangerDemo.size;i++)
				{
					holder.add(generator.next());
				}
				//生产好了等待交给消费者消费，并将当前线程挂起，等待消费者线程唤起，消费者消费完了返回一个空列表
				holder = exchanger.exchange(holder);
			}
		} catch (InterruptedException e) {
		}
	}
}

class ExchangerConsumer<T> implements Runnable
{
	private Exchanger<List<T>> exchanger;
	private List<T> holder;
	private volatile T value;
	
	public ExchangerConsumer(Exchanger<List<T>> ex,List<T> holder) {
		exchanger = ex;
		this.holder = holder;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				//等待生产者生产出来的东西,如果holder是空的就将当前线程挂起
				holder = exchanger.exchange(holder);
				for(T x : holder)
				{
					value = x;
					holder.remove(x);
				}
			}
		} catch (InterruptedException e) {
		}
		System.out.println("Final value:" + value);
	}
}

public class ExchangerDemo {
	static int size = 10;
	static int delay = 5;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ExecutorService exec = Executors.newCachedThreadPool();
		Exchanger<List<Fat>> xc = new Exchanger<List<Fat>>();
		
		List<Fat> 
			producerList = new CopyOnWriteArrayList<Fat>(),
			consumerList = new CopyOnWriteArrayList<Fat>();
			
		exec.execute(
				new ExchangerProducer<Fat>(
						xc, 
						BasicGenerator.create(Fat.class), 
						producerList));
		exec.execute(new ExchangerConsumer<Fat>(xc, consumerList));
		TimeUnit.SECONDS.sleep(delay);
		exec.shutdownNow();
	}

}

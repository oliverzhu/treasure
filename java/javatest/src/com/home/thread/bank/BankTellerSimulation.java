package com.home.thread.bank;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class Customer
{
	private final int serviceTime;
	public Customer(int tm)
	{
		serviceTime = tm;
	}
	public int getServiceTime()
	{
		return serviceTime;
	}
	public String toString()
	{
		return "[" + serviceTime + "]";
	}
}

class CustomerLine extends ArrayBlockingQueue<Customer>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8413057815287497120L;
	public CustomerLine(int maxLineSize)
	{
		super(maxLineSize);
	}
	public String toString()
	{
		if(this.size() == 0)
		{
			return "[Empty]";
		}
		StringBuilder result = new StringBuilder();
		for(Customer customer : this)
			result.append(customer);
		return result.toString();
	}
}

class CustomerGenerator implements Runnable
{
	private CustomerLine customers;
	private static Random rand = new Random(47);
	public CustomerGenerator(CustomerLine cq)
	{
		customers = cq;
	}
	@Override
	public void run() {
		while(!Thread.interrupted())
		{
			try {
				TimeUnit.MILLISECONDS.sleep(rand.nextInt(300));
				customers.put(new Customer(rand.nextInt(1000)));
			} catch (InterruptedException e) {
				System.out.println("CustomerGenerator interrupted");
			}
		}
		System.out.println("CustomerGenerator terminating");
	}
}

class Teller implements Runnable,Comparable<Teller>
{
	private static int counter = 0;
	private final int id = counter++;
	private int customersServed = 0;
	private CustomerLine customers;
	private boolean servingCustomerLine = true;
	public Teller(CustomerLine cq)
	{
		customers = cq;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				Customer customer = customers.take();
				TimeUnit.MILLISECONDS.sleep(customer.getServiceTime());
				synchronized (this) {
					customersServed++;
					while(!servingCustomerLine)
						wait();
				}
			}
		} catch (InterruptedException e) {
			System.out.println(this + "interrupted");
		}
		System.out.println(this + "terminating");
	}
	
	public synchronized void doSomethingElse()
	{
		customersServed = 0;
		servingCustomerLine = false;
	}
	
	public synchronized void serveCustomerLine()
	{
		assert !servingCustomerLine :"already serving:" + this;
		servingCustomerLine = true;
		notifyAll();
	}
	
	public String toString()
	{
		return "Teller " + id + " ";
	}
	
	public String shortString()
	{
		return "T" + id;
	}
	
	@Override
	public int compareTo(Teller other) {
		return customersServed < other.customersServed ? 
				-1 : (customersServed == other.customersServed ? 0 : 1);
	}
	
}

class TellerManager implements Runnable
{
	private ExecutorService exec;
	private CustomerLine customers;
	private PriorityQueue<Teller> workingTellers = 
			new PriorityQueue<Teller>();
	private Queue<Teller> tellerDoingOtherThings = 
			new LinkedList<Teller>();
	private int adjustmentPeriod;
	private static Random rand = new Random(47);
	public TellerManager(ExecutorService e,
			CustomerLine customers,int adjustmentPeroid)
	{
		exec = e;
		this.customers = customers;
		this.adjustmentPeriod = adjustmentPeroid;
		Teller teller = new Teller(customers);
		exec.execute(teller);
		workingTellers.add(teller);
	}
	
	public void adjustTellerNumber()
	{
		if(customers.size() / workingTellers.size() > 2)
		{
			if(tellerDoingOtherThings.size() > 0)
			{
				Teller teller = tellerDoingOtherThings.remove();
				teller.serveCustomerLine();
				workingTellers.offer(teller);
				return;
			}
			Teller teller = new Teller(customers);
			exec.execute(teller);
			workingTellers.add(teller);
			return;
		}
		
		if(workingTellers.size() > 1 && 
				customers.size() / workingTellers.size() < 2)
			reassignOneTeller();
		if(customers.size() == 0)
		{
			while(workingTellers.size() > 1)
				reassignOneTeller();
		}
	}
	
	private void reassignOneTeller()
	{
		Teller teller = workingTellers.poll();
		teller.doSomethingElse();
		tellerDoingOtherThings.offer(teller);
	}
	
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				TimeUnit.MILLISECONDS.sleep(adjustmentPeriod);
				adjustTellerNumber();
				System.out.print(customers + "{ ");
				for(Teller teller : workingTellers)
				{
					System.out.print(teller.shortString() + " ");
				}
				System.out.println("}");
			}
		} catch (InterruptedException e) {
			System.out.println(this + "interrupted");
		}
		System.out.println(this + "terminating");
	}
	
	public String toString()
	{
		return "TellerManager";
	}
}

/**
 * 线程池管理
 * @author Administrator
 *
 */
public class BankTellerSimulation {
	static final int MAX_LINE_SIZE = 50;
	static final int ADJUSTMENT_PEROID = 1000;
	

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("Press 'Enter' to quit");
		
		ExecutorService exec = Executors.newCachedThreadPool();
		
		CustomerLine customers = new CustomerLine(MAX_LINE_SIZE);
		exec.execute(new CustomerGenerator(customers));
		exec.execute(new TellerManager(exec, customers, ADJUSTMENT_PEROID));
		
		System.in.read();
		
		exec.shutdownNow();
	}

}

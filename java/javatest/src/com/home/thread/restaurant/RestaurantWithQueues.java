package com.home.thread.restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

class Order
{
	private static int counter = 0;
	private final int id = counter++;
	private final Customer customer;
	private final WaitPerson waitPerson;
	private final Food food;
	public Order(Customer cust,WaitPerson wp,Food f)
	{
		customer = cust;
		waitPerson = wp;
		food = f;
	}
	
	public Food item()
	{
		return food;
	}
	
	public Customer getCustomer()
	{
		return customer;
	}
	
	public WaitPerson getWaitPerson()
	{
		return waitPerson;
	}
	
	public String toString()
	{
		return "Order: " + id + "item: " + food +
				" for:" + customer + 
				" served by: " + waitPerson;
	}
}

class Plate
{
	private final Order order;
	private final Food food;
	public Plate(Order ord,Food f)
	{
		order = ord;
		food = f;
	}
	public Order getOrder()
	{
		return order;
	}
	
	public Food getFood()
	{
		return food;
	}
	
	public String toString()
	{
		return food.toString();
	}
}

class Customer implements Runnable
{
	private static int counter = 0;
	private final int id = counter++;
	private final WaitPerson waitPerson;
	
	//类似于放置盘子的桌子，一个顾客一个桌子
	private SynchronousQueue<Plate> placeSetting = 
			new SynchronousQueue<Plate>();
	public Customer(WaitPerson w)
	{
		waitPerson = w;
	}
	public void deliver(Plate p) throws InterruptedException
	{
		placeSetting.put(p);
	}
	@Override
	public void run() {
		for(Course course : Course.values())
		{
			Food food = course.randomSelection();
			try {
				waitPerson.placeOrder(this,food);
				System.out.println(this + "eating " + placeSetting.take());
			} catch (InterruptedException e) {
				System.out.println(this + "waiting for " + 
						course + "intertuptted");
				break;
			}
		}
		System.out.println(this + "finished meal,leaving");
	}
	
	public String toString()
	{
		return "Customer " + id + " ";
	}
}

class WaitPerson implements Runnable
{
	private static int counter = 0;
	private final int id = counter++;
	private final Restaurant restaurant;
	//完成
	BlockingQueue<Plate> filledOrders = 
			new LinkedBlockingQueue<Plate>();
	
	public WaitPerson(Restaurant rest)
	{
		restaurant = rest;
	}
	public void placeOrder(Customer cust,Food food)
	{
		try {
			restaurant.orders.put(new Order(cust,this,food));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				Plate plate = filledOrders.take();
				System.out.println(this + "received " + plate + 
						" delivering to " + 
						plate.getOrder().getCustomer());
				plate.getOrder().getCustomer().deliver(plate);
			}
		} catch (InterruptedException e) {
			System.out.println(this + " interrupted");
		}
		System.out.println(this + " off duty");
	}
	
	public String toString()
	{
		return "WaitPerson " + id + " ";
	}
}

class Chef implements Runnable
{
	private static int counter = 0;
	private final int id = counter++;
	private final Restaurant restaurant;
	private static Random rand = new Random(47);
	public Chef(Restaurant rest)
	{
		restaurant = rest;
	}
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				Order order = restaurant.orders.take();
				Food requestedItem = order.item();
				
				TimeUnit.MILLISECONDS.sleep(rand.nextInt(500));
				Plate plate = new Plate(order,requestedItem);
				order.getWaitPerson().filledOrders.put(plate);
			}
		} catch (InterruptedException e) {
			System.out.println(this + " intertupted");
		}
		System.out.println(this + " off duty");
	}
	
	public String toString()
	{
		return "Chef " + id + " ";
	}
}

class Restaurant implements Runnable
{
	private List<WaitPerson> waitPersons = 
			new ArrayList<WaitPerson>();
	private List<Chef> chefs = new ArrayList<Chef>();
	private ExecutorService exec;
	private static Random rand = new Random(47);
	BlockingQueue<Order> orders = new LinkedBlockingQueue<Order>();
	
	//服务员和厨师的数目可以确定下来
	public Restaurant(ExecutorService e,int nWaitPersons,int nChefs)
	{
		exec = e;
		for(int i = 0;i < nWaitPersons;i++)
		{
			WaitPerson waitPerson = new WaitPerson(this);
			waitPersons.add(waitPerson);
			exec.execute(waitPerson);
		}
		for(int i = 0;i < nChefs;i++)
		{
			Chef chef = new Chef(this);
			chefs.add(chef);
			exec.execute(chef);
		}
	}
	
	@Override
	public void run() {
		try {
			while(!Thread.interrupted())
			{
				//一个服务员可能服务于多个顾客
				WaitPerson wp = 
						waitPersons.get(rand.nextInt(waitPersons.size()));
				Customer c = new Customer(wp);
				exec.execute(c);
				TimeUnit.MILLISECONDS.sleep(100);
			}
		} catch (InterruptedException e) {
			System.out.println("Resturant interrupted");
		}
		System.out.println("Restaurang closing");
	}
}

public class RestaurantWithQueues {

	public static void main(String[] args) throws Exception{
		ExecutorService exec = Executors.newCachedThreadPool();
		Restaurant restaurant = new Restaurant(exec, 5, 2);
		exec.execute(restaurant);
		
		System.out.println("Press 'Enter' to quit");
		System.in.read();
		
		exec.shutdownNow();
	}

}

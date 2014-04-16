package com.home.refactor;

import java.util.Enumeration;
import java.util.Vector;

public class Customer
{
	private String _name;
	private Vector<Rental> _rentals = new Vector<Rental>();
	
	public Customer(String name)
	{
		_name = name;
	}
	
	public void addRental(Rental arg)
	{
		_rentals.addElement(arg);
	}
	
	public String getName()
	{
		return _name;
	}
	
	public String statement()
	{
		Enumeration<Rental> rentals = _rentals.elements();
		String result = "Rental Record for " + getName() + "\n";
		while(rentals.hasMoreElements())
		{
			Rental each = rentals.nextElement();
			
			result += "\t" + each.getMovie().getTitle() + "\t" + 
					String.valueOf(each.getCharge()) + "\n";
		}
		
		result += "Amount owed is" + String.valueOf(getToalCharge()) + "\n";
		result += "You earned " + String.valueOf(getTotalFrequentRenterPoints()) + 
				" frequent renter points";
		return result;
	}
	
	/**
	 * 将统计总价格的零时变量重构成方法
	 * @return
	 */
	private double getToalCharge()
	{
		double result = 0;
		Enumeration<Rental> rentals = _rentals.elements();
		while(rentals.hasMoreElements())
		{
			Rental each = rentals.nextElement();
			result += each.getCharge();
		}
		return result;
	}
	
	/**
	 * 将统计总积分的零时变量重构成方法
	 * @return
	 */
	private int getTotalFrequentRenterPoints()
	{
		int result = 0;
		Enumeration<Rental> rentals = _rentals.elements();
		while(rentals.hasMoreElements())
		{
			Rental each = rentals.nextElement();
			result += each.getFrequentRenterPoints();
		}
		return result;
	}
}

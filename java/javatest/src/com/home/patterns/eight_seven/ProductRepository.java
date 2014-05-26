package com.home.patterns.eight_seven;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProductRepository {
	private List<Product> products = new ArrayList<Product>();
	
	public Iterator<Product> iterator()
	{
		return products.iterator();
	}
	
	
	
}

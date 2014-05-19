package com.home.patterns.eight_three.improved;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProductRepository {
	private List<Product> products = new ArrayList<Product>();
	
	public Iterator<Product> iterator()
	{
		return products.iterator();
	}
	
	public List<Product> selectBy(Spec spec)
	{
		List<Product> foundProducts = new ArrayList<Product>();
		Iterator<Product> products = iterator();
		while(products.hasNext())
		{
			Product product = products.next();
			if(spec.isSatisfiedBy(product))
			{
				foundProducts.add(product);
			}
		}
		return foundProducts;
	}
	
	public List<Product> selectBy(List<Spec> specs)
	{
		return selectBy(new CompositeSpec(specs));
	}
}

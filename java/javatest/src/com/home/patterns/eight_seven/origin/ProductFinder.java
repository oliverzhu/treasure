package com.home.patterns.eight_seven.origin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.home.patterns.eight_seven.Color;
import com.home.patterns.eight_seven.Product;
import com.home.patterns.eight_seven.ProductRepository;

public class ProductFinder {
	private ProductRepository repository;
	
	public ProductFinder(ProductRepository repository)
	{
		this.repository = repository;
	}
	
	public List<Product> byColor(Color colorOfProductToFind)
	{
		List<Product> foundProducts = new ArrayList<Product>();
		Iterator<Product> products = repository.iterator();
		while(products.hasNext())
		{
			Product product = products.next();
			if(product.getColor().equals(colorOfProductToFind))
			{
				foundProducts.add(product);
			}
		}
		return foundProducts;
	}
	
	public List<Product> byPrice(float priceLimit)
	{
		List<Product> foundProducts = new ArrayList<Product>();
		Iterator<Product> products = repository.iterator();
		while(products.hasNext())
		{
			Product product = products.next();
			if(product.getPrice() == priceLimit)
			{
				foundProducts.add(product);
			}
		}
		return foundProducts;
	}
	
	public List<Product> byColorSizeAndBelowPrice(Color color,int size,float price)
	{
		List<Product> foundProducts = new ArrayList<Product>();
		Iterator<Product> products = repository.iterator();
		while(products.hasNext())
		{
			Product product = products.next();
			if(product.getColor() == color 
					&& product.getSize() == size 
					&& product.getPrice() < price)
			{
				foundProducts.add(product);
			}
		}
		return foundProducts;
	}
	
	public List<Product> byColorSizeAndBelowPrice(float price,Color color)
	{
		List<Product> foundProducts = new ArrayList<Product>();
		Iterator<Product> products = repository.iterator();
		while(products.hasNext())
		{
			Product product = products.next();
			if(product.getColor() != color 
					&& product.getPrice() < price)
			{
				foundProducts.add(product);
			}
		}
		return foundProducts;
	}
}

package com.home.patterns.eight_seven.improved;

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
		ColorSpec colorSpec = new ColorSpec(colorOfProductToFind);
		return selectBy(colorSpec);
	}
	
	public List<Product> byPrice(float priceLimit)
	{
		PriceSpec priceSpec = new PriceSpec(priceLimit);
		return selectBy(priceSpec);
	}
	
	
	public List<Product> byColorSizeAndBelowPrice(float price,Color color)
	{
		ColorSpec colorSpec = new ColorSpec(color);
		NotSpec notColorSpec = new NotSpec(colorSpec);
		BelowPriceSpec spec = new BelowPriceSpec(price);
		AndSpec andSpec = new AndSpec(notColorSpec, spec);
		return selectBy(andSpec);
	}
	
	private List<Product> selectBy(Spec spec)
	{
		List<Product> foundProducts = new ArrayList<Product>();
		Iterator<Product> products = repository.iterator();
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
}

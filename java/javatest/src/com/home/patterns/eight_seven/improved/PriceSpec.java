package com.home.patterns.eight_seven.improved;

import com.home.patterns.eight_seven.Product;

public class PriceSpec extends Spec {
	private float price;
	
	public PriceSpec(float price)
	{
		this.price = price;
	}
	
	@Override
	public boolean isSatisfiedBy(Product product) {
		return product.getPrice() == getPrice();
	}
	
	public float getPrice()
	{
		return price;
	}
}

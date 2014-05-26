package com.home.patterns.eight_seven.improved;

import com.home.patterns.eight_seven.Product;

public class BelowPriceSpec extends Spec {
	private float priceThreshold;
	
	public BelowPriceSpec(float priceThreshold)
	{
		this.priceThreshold = priceThreshold;
	}
	
	@Override
	public boolean isSatisfiedBy(Product product) {
		return product.getPrice() < getPriceThreshold();
	}
	
	public float getPriceThreshold()
	{
		return priceThreshold;
	}
}

package com.home.patterns.eight_seven.improved;

import com.home.patterns.eight_seven.Product;

public class SizeSpec extends Spec {
	private float size;
	
	public SizeSpec(float size)
	{
		this.size = size;
	}
	
	@Override
	public boolean isSatisfiedBy(Product product) {
		return product.getSize() == getSize();
	}
	
	public float getSize()
	{
		return size;
	}
}

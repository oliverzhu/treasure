package com.home.patterns.eight_seven.improved;

import com.home.patterns.eight_seven.Product;

public class NotSpec extends Spec {
	private Spec specToNegate;
	
	public NotSpec(Spec specToNegate)
	{
		this.specToNegate = specToNegate;
	}

	@Override
	public boolean isSatisfiedBy(Product product) {
		return !specToNegate.isSatisfiedBy(product);
	}

}

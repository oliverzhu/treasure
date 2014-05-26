package com.home.patterns.eight_seven.improved;

import com.home.patterns.eight_seven.Product;

public class AndSpec extends Spec {
	private Spec augend,addend;
	
	public AndSpec(Spec augend,Spec addend)
	{
		this.augend = augend;
		this.addend = addend;
	}

	@Override
	public boolean isSatisfiedBy(Product product) {
		return getAugend().isSatisfiedBy(product) 
				&& getAddend().isSatisfiedBy(product);
	}
	
	public Spec getAddend()
	{
		return addend;
	}
	
	public Spec getAugend()
	{
		return augend;
	}

}

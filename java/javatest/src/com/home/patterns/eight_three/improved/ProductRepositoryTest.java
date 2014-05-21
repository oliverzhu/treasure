package com.home.patterns.eight_three.improved;

import java.util.List;

import junit.framework.TestCase;

public class ProductRepositoryTest extends TestCase {
	
	//多与多种条件组合，CompositeSpec如开关式将
	public void testFindByColorSizeAndBelowPrice()
	{
		ProductRepository repository = new ProductRepository();
		CompositeSpec specs = new CompositeSpec();
		specs.add(new ColorSpec());
		specs.add(new SizeSpec());
		specs.add(new BelowPriceSpec());
		
		List<Product> foundProducts = repository.selectBy(specs);
	}

}

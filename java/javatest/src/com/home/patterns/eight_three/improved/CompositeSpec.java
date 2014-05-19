package com.home.patterns.eight_three.improved;

import java.util.Iterator;
import java.util.List;

public class CompositeSpec extends Spec{
	private List<Spec> specs;
	
	public CompositeSpec(List<Spec> specs)
	{
		this.specs = specs;
	}
	
	public List<Spec> getSpecs()
	{
		return specs;
	}

	@Override
	public boolean isSatisfiedBy(Product product) {
		Iterator<Spec> specifications = specs.iterator();
		boolean satisfiesAllSpecs = true;
		while(specifications.hasNext())
		{
			Spec productSpec = specifications.next();
			satisfiesAllSpecs &= productSpec.isSatisfiedBy(product);
		}
		return satisfiesAllSpecs;
	}

}

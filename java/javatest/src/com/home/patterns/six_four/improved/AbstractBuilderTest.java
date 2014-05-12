package com.home.patterns.six_four.improved;

import com.home.patterns.six_four.OutputBuilder;
import com.home.patterns.six_four.TestCase;

public abstract class AbstractBuilderTest extends TestCase {
	protected OutputBuilder outputBuilder;

	protected abstract OutputBuilder createBuilder(String rootName);
	
	public void testAddAboveRoot()
	{
		outputBuilder = createBuilder("orders");
		outputBuilder.addAbove("xxx");
	}
	
}

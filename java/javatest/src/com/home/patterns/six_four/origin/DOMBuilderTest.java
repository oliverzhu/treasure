package com.home.patterns.six_four.origin;

import com.home.patterns.six_four.DOMBuilder;
import com.home.patterns.six_four.OutputBuilder;
import com.home.patterns.six_four.TestCase;

public class DOMBuilderTest extends TestCase {
	private OutputBuilder outputBuilder;
	private OutputBuilder createBuilder(String rootName)
	{
		return new DOMBuilder(rootName);
	}
	
	public void testAddAboveRoot()
	{
		outputBuilder = createBuilder("orders");
		outputBuilder.addAbove("xxx");
	}
}

package com.home.patterns.six_four.origin;

import com.home.patterns.six_four.OutputBuilder;
import com.home.patterns.six_four.TestCase;
import com.home.patterns.six_four.XMLBuilder;

public class XMLBuilderTest extends TestCase {
	private OutputBuilder outputBuilder;
	private OutputBuilder createBuilder(String rootName)
	{
		return new XMLBuilder(rootName);
	}
	
	public void testAddAboveRoot()
	{
		outputBuilder = createBuilder("orders");
		outputBuilder.addAbove("xxx");
	}
}

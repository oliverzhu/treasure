package com.home.patterns.six_two.improved;

import junit.framework.TestCase;

public class StringNodeTest extends TestCase {
	public void testDeocdeAmpersand()
	{
		NodeFactory nodeFactory = new NodeFactory();
		nodeFactory.setDecodeStringNodes(true);
		StringParser parser = new StringParser();
		parser.setNodeFactory(nodeFactory);
	}

}

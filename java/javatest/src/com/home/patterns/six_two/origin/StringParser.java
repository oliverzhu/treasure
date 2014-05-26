package com.home.patterns.six_two.origin;

import com.home.patterns.six_two.Node;
import com.home.patterns.six_two.StringNode;

public class StringParser {
	private boolean shouldDecodeNode;
	public Node find(StringBuffer textBuffer,int textBegin,int textEnd)
	{
		return StringNode.createStringNode(textBuffer, textBegin, textEnd, shouldDecodeNodes());
	}
	
	private boolean shouldDecodeNodes()
	{
		return shouldDecodeNode;
	}

}

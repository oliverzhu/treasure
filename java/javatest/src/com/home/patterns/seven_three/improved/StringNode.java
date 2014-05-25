package com.home.patterns.seven_three.improved;

import com.home.patterns.seven_three.AbstractNode;
import com.home.patterns.seven_three.Node;

public class StringNode extends AbstractNode {
	private StringNode(StringBuffer textBuffer,int textBegin,int textEnd)
	{
		
	}
	
	public static Node createStringNode(StringBuffer textBuffer,int textBegin,int textEnd,boolean shouldDecode)
	{
		if(shouldDecode)
		{
			return new DecodingNode(new StringNode(textBuffer,textBegin,textEnd));
		}
		return new StringNode(textBuffer,textBegin,textEnd);
	}
	
	@Override
	public String toPlainTextString()
	{
		String result = "";
		return result;
	}

}

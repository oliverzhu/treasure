package com.home.patterns.six_two;

import com.home.patterns.six_two.AbstractNode;
import com.home.patterns.six_two.Node;


public class StringNode extends AbstractNode {
	public StringNode(StringBuffer textBuffer,int textBegin,int textEnd)
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

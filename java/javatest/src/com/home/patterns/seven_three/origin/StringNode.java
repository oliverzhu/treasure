package com.home.patterns.seven_three.origin;

import com.home.patterns.seven_three.AbstractNode;
import com.home.patterns.seven_three.Node;
import com.home.patterns.seven_three.Tranlsate;

public class StringNode extends AbstractNode {
	private boolean shouldDecode;
	private StringNode(StringBuffer textBuffer,int textBegin,int textEnd)
	{
		
	}
	private StringNode(StringBuffer textBuffer,int textBegin,int textEnd,boolean shouldDecode)
	{
		this(textBuffer,textBegin,textEnd);
		setShouldDecode(shouldDecode);
	}
	
	public static Node createStringNode(StringBuffer textBuffer,int textBegin,int textEnd,boolean shouldDecode)
	{
		return new StringNode(textBuffer,textBegin,textEnd,shouldDecode);
	}
	
	@Override
	public String toPlainTextString()
	{
		String result = "";
		if(shouldDecode())
		{
			result = Tranlsate.decode(result);
		}
		return result;
	}
	private void setShouldDecode(boolean shouldDecode) {
		this.shouldDecode = shouldDecode;
	}
	
	private boolean shouldDecode()
	{
		return shouldDecode;
	}

}

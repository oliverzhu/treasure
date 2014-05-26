package com.home.patterns.six_two.improved;

import com.home.patterns.six_two.DecodingNode;
import com.home.patterns.six_two.Node;
import com.home.patterns.six_two.StringNode;

public class NodeFactory {
	private  boolean decodeStringNode;
	
	public  Node createStringNode(StringBuffer textBuffer,int textBegin,int textEnd)
	{
		if(decodeStringNode)
		{
			return new DecodingNode(new StringNode(textBuffer,textBegin,textEnd));
		}
		return new StringNode(textBuffer,textBegin,textEnd);
	}
	
	public boolean shouldDecodeStringNodes()
	{
		return decodeStringNode;
	}
	
	public void setDecodeStringNodes(boolean decodeStringNodes)
	{
		this.decodeStringNode = decodeStringNodes;
	}

}

package com.home.patterns.six_two.improved;

import com.home.patterns.six_two.Node;


public class StringParser {
	private NodeFactory nodeFactory = new NodeFactory();
	
	public Node find(StringBuffer textBuffer,int textBegin,int textEnd)
	{
		return nodeFactory.createStringNode(textBuffer, textBegin, textEnd);
	}

	public NodeFactory getNodeFactory() {
		return nodeFactory;
	}

	public void setNodeFactory(NodeFactory nodeFactory) {
		this.nodeFactory = nodeFactory;
	}

}

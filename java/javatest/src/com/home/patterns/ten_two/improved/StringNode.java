package com.home.patterns.ten_two.improved;


public class StringNode implements Node {

	public String getText() {
		return "";
	}
	
	public void accept(NodeVisitor nodeVisitor)
	{
		nodeVisitor.visitStringNode(this);
	}

}

package com.home.patterns.ten_two.improved;


public class Tag implements Node {

	public String getTagName() {
		return null;
	}
	
	public void accept(NodeVisitor nodeVisitor)
	{
		nodeVisitor.visitTag(this);
	}

}

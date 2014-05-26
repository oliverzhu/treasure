package com.home.patterns.ten_two.improved;


public class LinkTag implements Node {

	public String getLinkText() {
		return null;
	}
	
	public void accept(NodeVisitor nodeVisitor)
	{
		nodeVisitor.visitLinkTag(this);
	}

}

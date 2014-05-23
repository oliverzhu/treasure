package com.home.patterns.eight_five.origin;

public class XMLBuilder extends AbstractBuilder {
	private TagNode rootNode;
	private TagNode currentNode;
	
	public void addChild(String childTagName)
	{
		addTo(currentNode,childTagName);
	}
	
	public void addSibling(String siblingTagName)
	{
		addTo(currentNode.getParent(), siblingTagName);
	}

	private void addTo(TagNode parenetNode, String tagName) {
		currentNode = new TagNode(tagName);
		parenetNode.add(currentNode);
	}
	
	public void addAttribute(String name,String value)
	{
		currentNode.addAttribute(name,value);
	}
	
	public void addValue(String value)
	{
		currentNode.addValue(value);
	}
}

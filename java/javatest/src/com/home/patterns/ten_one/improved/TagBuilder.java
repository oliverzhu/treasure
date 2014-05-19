package com.home.patterns.ten_one.improved;

public class TagBuilder {
	private TagNode rootNode;
	private TagNode currentNode;
	
	public TagBuilder(String rootTagName)
	{
		rootNode = new TagNode(rootTagName);
		currentNode = rootNode;
	}
	
	public void addToParent(String parentTagName,String childTagName)
	{
		TagNode parentNode = findParentBy(parentTagName);
		if(parentNode == null)
		{
			throw new RuntimeException("miss parent tag" + parentTagName);
		}
		addTo(parentNode,childTagName);
	}
	
	public void addChild(String childTagName)
	{
		addTo(currentNode, childTagName);
	}
	
	public void addSibling(String siblingTagName)
	{
		addTo(currentNode.getParent(), siblingTagName);
	}
	
	public void addTo(TagNode parentNode,String tagName)
	{
		currentNode = new TagNode(tagName);
		parentNode.add(currentNode);
	}
	
	public void addAttribute(String name,String value)
	{
		currentNode.addAttribute(name, value);
	}
	
	public void addValue(String value)
	{
		currentNode.addValue(value);
	}
	
	private TagNode findParentBy(String parentName)
	{
		TagNode parentNode = currentNode;
		while(parentNode != null)
		{
			if(parentName.equals(parentNode.getName()))
			{
				return parentNode;
			}
			parentNode = parentNode.getParent();
		}
		return null;
	}
	
	public String toXML()
	{
		return rootNode.toString();
	}

}

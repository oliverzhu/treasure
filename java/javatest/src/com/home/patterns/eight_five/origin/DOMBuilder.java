package com.home.patterns.eight_five.origin;

public class DOMBuilder extends AbstractBuilder{
	private Document document;
	private Element root;
	private Element parent;
	private Element current;
	
	public void addAttribute(String name,String value)
	{
		current.setAttribute(name,value);
	}
	
	public void addBelow(String child)
	{
		Element childNode = document.createElement(child);
		current.appendChild(childNode);
		parent = current;
		current = childNode;
		history.push(current);
	}
	
	public void addBeside(String sibling)
	{
		if(current == root)
		{
			throw new RuntimeException();
		}
		Element siblingNode = document.createElement(sibling);
		parent.appendChild(siblingNode);
		current = siblingNode;
		history.pop();
		history.push(current);
	}
	
	public void addValue(String value)
	{
		current.appendChild(document.createTextNode(value));
	}
}

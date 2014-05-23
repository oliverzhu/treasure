package com.home.patterns.eight_five.improved;

public class ElementAdapter implements XMLNode{
	Element element;
	Document document;
	
	public ElementAdapter(Element element,Document document)
	{
		this.element = element;
		this.document = document;
	}
	
	public Element getElement()
	{
		return element;
	}
	
	@Override
	public void addAttribute(String name,String value)
	{
		getElement().setAttribute(name,value);
	}
	
	@Override
	public void add(XMLNode childNode)
	{
		ElementAdapter childElement = (ElementAdapter) childNode;
		getElement().appendChild(childElement.getElement());
	}
	
	@Override
	public void addValue(String value)
	{
		getElement().appendChild(document.createTextNode(value));
	}


}

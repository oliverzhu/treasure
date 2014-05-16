package com.home.patterns.seven_five.improved;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagNode {
	private String name = "";
	private String value = "";
	private StringBuffer attributes;
	private List<TagNode> children;
	
	public TagNode(String name)
	{
		this.name = name;
		attributes = new StringBuffer("");
	}
	
	public void addAttribute(String attribute,String value)
	{
		attributes.append(" ");
		attributes.append(attribute);
		attributes.append("='");
		attributes.append(value);
		attributes.append("'");
	}
	
	public void addValue(String value)
	{
		this.value = value;
	}
	
	private List<TagNode> children()
	{
		if(children == null)
		{
			children = new ArrayList<TagNode>();
		}
		return children;
	}
	
	public void add(TagNode child)
	{
		children().add(child);
	}
	
	public String toString()
	{
		String result;
		result = 
			"<" + name + attributes + ">";
		Iterator<TagNode> it = children().iterator();
		while(it.hasNext())
		{
			TagNode node = it.next();
			result += node.toString();
		}
		result += value;
		result += "</" + name + ">";
		return result;
	}

}

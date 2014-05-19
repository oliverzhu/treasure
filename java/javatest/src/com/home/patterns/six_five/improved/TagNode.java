package com.home.patterns.six_five.improved;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TagNode {
	private TagNode parent;
	
	private String name = "";
	private String value = "";
	private StringBuffer attributes;
	private List<TagNode> children;
	
	//每一个TagNode都会new一个StringBuffer会造成性能问题
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
		child.setParent(this);
		children().add(child);
	}
	
	public TagNode getParent()
	{
		return parent;
	}
	
	public void setParent(TagNode parent)
	{
		this.parent = parent;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String toString()
	{
		if(children().size() == 0 && "".equals(value))
		{
			return "<" + name + attributes + "/>";
		}
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

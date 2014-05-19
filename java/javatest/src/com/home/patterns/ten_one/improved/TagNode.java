package com.home.patterns.ten_one.improved;

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
	
	//不在递归调用toString,而是在appendContentsTo发生递归
	public String toString()
	{
		if(children().size() == 0 && "".equals(value))
		{
			return "<" + name + attributes + "/>";
		}
		StringBuffer result = new StringBuffer("");
		appendContentsTo(result);
		return result.toString();
	}
	
	
	
	private void appendContentsTo(StringBuffer result) {
		writeOpenTagTo(result);
		writeChildrenTo(result);
		writeValueTo(result);
		writeEndTagTo(result);
	}

	private void writeOpenTagTo(StringBuffer result)
	{
		result.append("<");
		result.append(name);
		result.append(attributes.toString());
		result.append(">");
	}
	
	private void writeChildrenTo(StringBuffer result) {
		Iterator<TagNode> it = children().iterator();
		while(it.hasNext())
		{
			TagNode node = it.next();
			node.appendContentsTo(result);
		}
	}
	
	private void writeValueTo(StringBuffer result) {
		if(!value.equals(""))
		{
			result.append(value);
		}
	}
	
	private void writeEndTagTo(StringBuffer result) {
		result.append("</");
		result.append(name);
		result.append(">");
	}
}

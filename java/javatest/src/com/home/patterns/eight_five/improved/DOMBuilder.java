package com.home.patterns.eight_five.improved;

/**
 * 按最小的粒度来适配,不是适配builder，而是基本元素Element
 * @author jianwen.zhu
 *
 */
public class DOMBuilder extends AbstractBuilder{
	private Document document;
	private XMLNode parent;
	
	public void addAttribute(String name,String value)
	{
		current.addAttribute(name,value);
	}
	
	public void addBelow(String child)
	{
		XMLNode childNode = 
				new ElementAdapter(document.createElement(child),document);
		current.add(childNode);
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
		XMLNode siblingNode = 
				new ElementAdapter(document.createElement(sibling),document);
		parent.add(siblingNode);
		current = siblingNode;
		history.pop();
		history.push(current);
	}
	
	public void addValue(String value)
	{
		current.addValue(value);
	}

	@Override
	protected XMLNode createNode(String nodeName) {
		return new ElementAdapter(document.createElement(nodeName),document);
	}
	
}

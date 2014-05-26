package com.home.patterns.ten_two.improved;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.home.patterns.ten_two.Translate;

public class TextExtrator implements NodeVisitor{
	private List<Node> elements = new ArrayList<Node>();
	Node node;
	boolean isPreTag = false;
	boolean isScriptTag = false;
	StringBuffer results = new StringBuffer();
	
	public String extractText()
	{
		Node node;
		for(Iterator<Node> e = elements().iterator();e.hasNext();)
		{
			node = e.next();
			node.accept(this);
		}
		return results.toString();
	}
	
	public void visitTag(Tag tag) {
		String tagName = tag.getTagName();
		if(tagName.equalsIgnoreCase("PRE"))
		{
			isPreTag = true;
		}else if(tagName.equalsIgnoreCase("SCRIPT"))
		{
			isScriptTag = true;
		}
	}
	

	public void visitEndTag(EndTag endTag) {
		String tagName = endTag.getTagName();
		if(tagName.equalsIgnoreCase("PRE"))
		{
			isPreTag = false;
		}else if(tagName.equalsIgnoreCase("SCRIPT"))
		{
			isScriptTag = false;
		}
	}
	

	public void visitLinkTag(LinkTag linkTag) {
		if(isPreTag)
		{
			results.append(linkTag.getLinkText());
		}else
		{
			collapse(results, Translate.decode(linkTag.getLinkText()));
		}
		if(getLinks())
		{
			results.append("<");
			results.append(linkTag.getLinkText());
			results.append(">");
		}
	}

	public void visitStringNode(StringNode stringNode) {
		if(!isScriptTag)
		{
			if(isPreTag)
			{
				results.append(stringNode.getText());
			}else
			{
				String text = Translate.decode(stringNode.getText());
				if(getReplaceNonBreakingSpace())
				{
					text = text.replace('\u00a0', ' ');
				}
				if(getCollapse())
				{
					collapse(results,text);
				}else
				{
					results.append(text);
				}
			}
		}
	}

	private boolean getLinks() {
		return false;
	}

	private void collapse(StringBuffer results, String text) {
		
	}

	private boolean getCollapse() {
		return false;
	}

	private boolean getReplaceNonBreakingSpace() {
		return false;
	}

	public List<Node> elements()
	{
		return elements;
	}
	

}

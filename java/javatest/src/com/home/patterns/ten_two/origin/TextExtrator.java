package com.home.patterns.ten_two.origin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.home.patterns.ten_two.Translate;

public class TextExtrator {
	private List<Node> elements = new ArrayList<Node>();
	public String extractText()
	{
		Node node;
		boolean isPreTag = false;
		boolean isScriptTag = false;
		StringBuffer results = new StringBuffer();
		for(Iterator<Node> e = elements().iterator();e.hasNext();)
		{
			node = e.next();
			if(node instanceof StringNode)
			{
				if(!isScriptTag)
				{
					StringNode stringNode = (StringNode) node;
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
			}else if(node instanceof LinkTag)
			{
				LinkTag link = (LinkTag) node;
				if(isPreTag)
				{
					results.append(link.getLinkText());
				}else
				{
					collapse(results, Translate.decode(link.getLinkText()));
				}
				if(getLinks())
				{
					results.append("<");
					results.append(link.getLinkText());
					results.append(">");
				}
			}else if(node instanceof EndTag)
			{
				EndTag endTag = (EndTag) node;
				String tagName = endTag.getTagName();
				if(tagName.equalsIgnoreCase("PRE"))
				{
					isPreTag = false;
				}else if(tagName.equalsIgnoreCase("SCRIPT"))
				{
					isScriptTag = false;
				}
			}else if(node instanceof Tag)
			{
				Tag tag = (Tag) node;
				String tagName = tag.getTagName();
				if(tagName.equalsIgnoreCase("PRE"))
				{
					isPreTag = true;
				}else if(tagName.equalsIgnoreCase("SCRIPT"))
				{
					isScriptTag = true;
				}
			}
		}
		return results.toString();
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

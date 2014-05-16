package com.home.patterns.seven_five;

import com.home.patterns.seven_five.improved.TagNode;

import junit.framework.TestCase;

public class TagTests extends TestCase {
	public void testSimpleTagWithOneAttributeAndValue()
	{
		TagNode priceTag = new TagNode("price");
		priceTag.addAttribute("currency", "USD");
		priceTag.addValue("100");
		String expected = 
			"<price currency='USD'>" +
		    100 + "</price>";
		assertEquals("price XML ",expected, priceTag.toString());
	}
	
	public void testCompositeTagOneChild()
	{
		TagNode productTag = new TagNode("product");
		productTag.add(new TagNode("price"));
		String expected = 
			"<product>" +
			"<price>" + 
			"</price>" +
			"</product>";
		assertEquals("price XML ",expected, productTag.toString());
	}
}

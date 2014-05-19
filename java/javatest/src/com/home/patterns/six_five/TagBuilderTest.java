package com.home.patterns.six_five;

import com.home.patterns.six_five.improved.TagBuilder;

import junit.framework.TestCase;

public class TagBuilderTest extends TestCase {
	public void testRepeatingChildrenAndGrandchildren()
	{
		String expectedXml = 
			"<flavors>" + 
				"<flavor>" + 
					"<requirements>" + 
						"<requirement/>" + 
					"</requirements>" + 
				"</flavor>" + 
				"<flavor>" + 
				"<requirements>" + 
					"<requirement/>" + 
				"</requirements>" + 
				"</flavor>" + 
			"</flavors>";
		TagBuilder builder = new TagBuilder("flavors");
		for(int i = 0;i < 2;i++)
		{
			//builder.addChild("flavor");
			builder.addToParent("flavors", "flavor");
			builder.addChild("requirements");
			builder.addChild("requirement");
		}
		
		assertEquals(expectedXml, builder.toXML());
	}
	
	public void testAttributeAndValues()
	{
		String expectedXml = 
			"<flavor name='Test-Driven Developent'>" + 
			 "<requirements>" + 
			   "<requirement type='hardware'>" + 
			   "1 computer for every 2 participants" + 
			   "</requirement>" + 
			   "<requirement type='software'>" + 
			   "IDE" + 
			   "</requirement>" + 
			  "</requirements>" + 
			  "</flavor>";
		TagBuilder builder = new TagBuilder("flavor");
		builder.addAttribute("name", "Test-Driven Developent");
		builder.addChild("requirements");
		builder.addToParent("requirements", "requirement");
		builder.addAttribute("type", "hardware");
		builder.addValue("1 computer for every 2 participants");
		builder.addToParent("requirements", "requirement");
		builder.addAttribute("type", "software");
		builder.addValue("IDE");
		
		assertEquals(expectedXml, builder.toXML());
	}
}

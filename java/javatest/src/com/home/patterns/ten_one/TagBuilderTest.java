package com.home.patterns.ten_one;

import com.home.patterns.ten_one.improved.TagBuilder;

import junit.framework.TestCase;

public class TagBuilderTest extends TestCase {
	public void testRepeatingChildrenAndGrandchildren()
	{
		String expectedXml = 
			"<flavors>" + 
				"<flavor>" + 
					"<requirements>" + 
						"<requirement></requirement>" + 
					"</requirements>" + 
				"</flavor>" + 
				"<flavor>" + 
				"<requirements>" + 
					"<requirement></requirement>" + 
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
}

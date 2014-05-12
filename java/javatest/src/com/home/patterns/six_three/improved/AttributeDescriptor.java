package com.home.patterns.six_three.improved;

import java.util.Date;


public abstract class AttributeDescriptor {
	protected AttributeDescriptor()
	{
		
	}
	
	public static AttributeDescriptor forInteger(String operation)
	{
		return new DefaultDescriptor(operation, Integer.TYPE);
	}
	
	public static AttributeDescriptor forDate(String operation)
	{
		return new DefaultDescriptor(operation, Date.class);
	}
	
	public static AttributeDescriptor forString(String operation)
	{
		return new DefaultDescriptor(operation, String.class);
	}

}

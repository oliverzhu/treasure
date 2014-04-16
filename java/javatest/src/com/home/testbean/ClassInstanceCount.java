package com.home.testbean;

import java.util.HashMap;

/** 
 * 
 * @author zjw
 *
 */
public class ClassInstanceCount {
	static class Counter extends HashMap<String,Integer>
	{
		public void count(String type)
		{
			Integer quantity = get(type);
			if(quantity == null)
				put(type,1);
			else
				put(type,quantity + 1);
		}
		
	}
}

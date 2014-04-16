package com.home.valuepassed;

public class ValuePassedTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		People p = new People();
		p.setName("before");
		changeValue(p);
		System.out.println(p.getName());
	}
	
	public static void changeValue(People p)
	{
		p.setName("after");
	}

}

package com.home.patterns.seven_one.improved;

public class List {
	private static final int GROWTH_INCREMENT = 10;
	private boolean readOnly;
	private int size;
	private Object[] elements;
	
	public void add(Object element)
	{
		if(readOnly)
		{
			return;
		}
		if(atCapacity())
		{
			grow();
		}
		addElement(element);
	}
	
	private boolean atCapacity()
	{
		return (size + 1) > elements.length;
	}
	
	private void grow()
	{
		Object[] newElements = new Object[elements.length + GROWTH_INCREMENT];
		for(int i = 0;i < size;i++)
		{
			newElements[i] = elements[i];
		}
		elements = newElements;
	}
	
	private void addElement(Object element)
	{
		elements[size++] = element;
	}
}

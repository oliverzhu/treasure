package com.home.patterns.seven_one.origin;

public class List {
	private boolean readOnly;
	private int size;
	private Object[] elements;
	
	public void add(Object element)
	{
		if(!readOnly)
		{
			int newSize = size + 1;
			if(newSize > elements.length)
			{
				Object[] newElements = new Object[elements.length + 10];
				for(int i = 0;i < size;i++)
				{
					newElements[i] = elements[i];
				}
				elements = newElements;
			}
			elements[size++] = element;
		}
	}
}

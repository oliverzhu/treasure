package com.home.patterns.seven_three.improved;

import com.home.patterns.seven_three.Node;
import com.home.patterns.seven_three.Tranlsate;

public class DecodingNode implements Node {
	private Node delegate;
	
	public DecodingNode(Node newDelegate)
	{
		delegate = newDelegate;
	}

	@Override
	public String toPlainTextString() {
		return Tranlsate.decode(delegate.toPlainTextString());
	}

	@Override
	public String toHtml() {
		return delegate.toHtml();
	}

	@Override
	public int elementBegin() {
		return delegate.elementBegin();
	}

	@Override
	public int elementEnd() {
		return delegate.elementEnd();
	}

}

package com.home.patterns.six_four.improved;

import com.home.patterns.six_four.DOMBuilder;
import com.home.patterns.six_four.OutputBuilder;

public class DOMBuilderTest extends AbstractBuilderTest {

	@Override
	protected OutputBuilder createBuilder(String rootName) {
		return new DOMBuilder(rootName);
	}
}

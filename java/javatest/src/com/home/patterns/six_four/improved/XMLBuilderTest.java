package com.home.patterns.six_four.improved;

import com.home.patterns.six_four.OutputBuilder;
import com.home.patterns.six_four.XMLBuilder;

public class XMLBuilderTest extends AbstractBuilderTest {

	@Override
	protected OutputBuilder createBuilder(String rootName) {
		return new XMLBuilder(rootName);
	}
}

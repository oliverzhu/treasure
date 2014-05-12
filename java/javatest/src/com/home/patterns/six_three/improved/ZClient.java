package com.home.patterns.six_three.improved;

import java.util.ArrayList;
import java.util.List;

public class ZClient {
	protected List	createAttributeDescriptor() {
		List result = new ArrayList();
		result.add(AttributeDescriptor.forInteger("remoteId"));
		result.add(AttributeDescriptor.forDate("lastChangedDate"));
		return result;
	}
}

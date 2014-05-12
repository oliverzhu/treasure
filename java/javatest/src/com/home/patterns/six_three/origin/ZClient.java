package com.home.patterns.six_three.origin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ZClient {
	protected List	createAttributeDescriptor() {
		List result = new ArrayList();
		result.add(new DefaultDescriptor("remoteId",Integer.TYPE));
		result.add(new DefaultDescriptor("lastChangedDate", Date.class));
		return result;
	}
}

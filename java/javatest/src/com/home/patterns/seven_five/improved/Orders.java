package com.home.patterns.seven_five.improved;

import java.util.List;

public class Orders {
	private List<Order> orders;
	public int getOrderCount() {
		return orders.size();
	}

	public Order getOrder(int i) {
		return orders.get(i);
	}
}

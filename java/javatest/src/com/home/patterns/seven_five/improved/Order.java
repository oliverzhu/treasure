package com.home.patterns.seven_five.improved;

import java.util.List;

public class Order {
	private int id;
	private List<Product> products;

	public Object getOrderId() {
		return id;
	}

	public int getProductCount() {
		return products.size();
	}

	public Product getProduct(int j) {
		return products.get(j);
	}

}

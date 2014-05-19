package com.home.patterns.seven_five.improved;

public class OrderWriter {
	private Orders orders;
	public OrderWriter(Orders orders)
	{
		this.orders = orders;
	}
	
	public String getContents()
	{
		StringBuffer xml = new StringBuffer();
		writeOrderTo(xml);
		return xml.toString();
	}

	private void writeOrderTo(StringBuffer xml) {
		TagNode ordersTag = new TagNode("orders");
		for(int i = 0;i < orders.getOrderCount();i++)
		{
			Order order = orders.getOrder(i);
			TagNode orderTag = new TagNode("order");
			orderTag.addAttribute("id", order.getOrderId() + "");
			writeProductsTo(orderTag, order);
			ordersTag.add(orderTag);
		}
		xml.append(ordersTag.toString());
	}

	private void writeProductsTo(TagNode orderTag, Order order) {
		for(int j = 0;j < order.getProductCount();j++)
		{
			Product product = order.getProduct(j);
			TagNode productTag = new TagNode("product");
			productTag.addAttribute("id", product.getId() + "");
			productTag.addAttribute("color", colorFor(product));
			if(product.getSize() != 0)
			{
				productTag.addAttribute("size", sizeFor(product));
			}
			writePriceTo(productTag, product);
			productTag.addValue(product.getName());
			orderTag.add(productTag);
		}
	}

	private void writePriceTo(TagNode productTag, Product product) {
		TagNode priceNode = new TagNode("price");
		priceNode.addAttribute("currency", currencyFor(product));
		priceNode.addValue(priceFor(product));
		productTag.add(priceNode);
	}

	private String priceFor(Product product) {
		return "";
	}

	private String currencyFor(Product product) {
		return "";
	}

	private String sizeFor(Product product) {
		return "";
	}

	private String colorFor(Product product) {
		return "";
	}
}

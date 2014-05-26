package com.home.patterns.eight_seven.improved;

import com.home.patterns.eight_seven.Color;
import com.home.patterns.eight_seven.Product;

public class ColorSpec extends Spec {
	private Color colorOfProductToFind;
	
	public ColorSpec(Color colorOfProductToFind)
	{
		this.colorOfProductToFind = colorOfProductToFind;
	}
	
	@Override
	public boolean isSatisfiedBy(Product product) {
		return product.getColor().equals(getColorOfProductToFind());
	}
	
	public Color getColorOfProductToFind()
	{
		return colorOfProductToFind;
	}
}

package com.home.refactor;


public class Movie {
	public static final int CHILDRES = 2;
	public static final int REGULAR = 0;
	public static final int NEW_RELEASE = 1;
	
	private String _title;
	
	private Price _price;
	
	public Movie(String title,int priceCode)
	{
		_title = title;
		setPriceCode(priceCode);
	}

	public String getTitle() {
		return _title;
	}

	public void setTitle(String _title) {
		this._title = _title;
	}

	public int getPriceCode() {
		return _price.getPriceCode();
	}

	public void setPriceCode(int _priceCode) {
		switch(_priceCode)
		{
			case REGULAR:
				_price = new RegularPrice();
				break;
			case CHILDRES:
				_price = new ChildrendsPrice();
				break;
			case NEW_RELEASE:
				_price = new NewReleasePrice();
				break;
			default:
				throw new IllegalArgumentException("Incorrect Price Code");
		}
	}
	
	double getCharge(int daysRented)
	{
		return _price.getCharge(daysRented);
	}
	
	int getFrequentRenterPoints(int daysRented)
	{
		return _price.getFrequentRenterPoints(daysRented);
	}
}


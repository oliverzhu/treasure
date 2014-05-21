package com.home.patterns.seven_six.improved;

import java.util.Map;

import com.home.patterns.seven_six.origin.HandlerResponse;
import com.home.patterns.seven_six.origin.WorkshopManager;

public abstract class Handler {
	protected CatalogApp catalogApp;
	
	public Handler(CatalogApp catalogApp)
	{
		this.catalogApp = catalogApp;
	}
	
	protected WorkshopManager workshopManager()
	{
		return catalogApp.getWorkshopManager();
	}
	
	public abstract HandlerResponse execute(Map<String, String> parameters) throws Exception;

}

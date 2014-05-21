package com.home.patterns.seven_six.improved;

import java.util.Map;

import com.home.patterns.seven_six.origin.HandlerResponse;

public class NewWorkshopHandler extends Handler{
	private CatalogApp catalogApp;
	
	public NewWorkshopHandler(CatalogApp catalogApp)
	{
		super(catalogApp);
	}
	
	public HandlerResponse execute(Map<String, String> parameters) throws Exception
	{
		createNewWorkshop(parameters);
		return catalogApp.executeActionAndGetResponse(CatalogApp.ALL_WORKSHOPS,parameters);
	}
	
	private void createNewWorkshop(Map<String, String> parameters) throws Exception
	{
		String nextWorkshopID = workshopManager().getNextWorkshopID();
		workshopManager().addWorkshop(newWorkshopContents(nextWorkshopID));
		parameters.put("id",nextWorkshopID);
	}
	
	private StringBuffer newWorkshopContents(String nextWorkshopID) throws Exception
	{
		StringBuffer newWorkshopContents = 
				workshopManager().createNewFileFromTemplate(nextWorkshopID,
						workshopManager().getWorkshopDir(),
						workshopManager().getWorkshopTemplate());
		return newWorkshopContents;
	}
	

}

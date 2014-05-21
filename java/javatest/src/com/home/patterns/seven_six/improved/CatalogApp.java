package com.home.patterns.seven_six.improved;

import java.util.HashMap;
import java.util.Map;

import com.home.patterns.seven_six.origin.HandlerResponse;
import com.home.patterns.seven_six.origin.WorkshopManager;

public class CatalogApp {
	public static final String NEW_WORKSHOP = "workshop";
	public static final String ALL_WORKSHOPS = "allworkshops";
	public static final String All_WORKSHOPS_STYLESHEET = "allworkshops.xls";
	private WorkshopManager workshopManager;
	
	private Map<String,Handler> handlers;
	
	public CatalogApp()
	{
		createHandlers();
	}

	public HandlerResponse executeActionAndGetResponse(String actionName,Map<String, String> parameters) throws Exception
	{
		Handler handler = lookupHandlerBy(actionName);
		return handler.execute(parameters);
	}
	
	public void createHandlers()
	{
		handlers = new HashMap<String, Handler>();
		handlers.put(NEW_WORKSHOP, new NewWorkshopHandler(this));
		handlers.put(ALL_WORKSHOPS, new AllWorkshopHandler(this));
	}
	
	private Handler lookupHandlerBy(String handlerName)
	{
		return handlers.get(handlerName);
	}

	public String getFormattedData(String string) {
		return "";
	}

	protected WorkshopManager getWorkshopManager() {
		return workshopManager;
	}

}

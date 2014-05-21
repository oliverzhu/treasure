package com.home.patterns.seven_six.origin;

import java.util.Iterator;
import java.util.Map;

public class CatalogApp {
	private static final String NEW_WORKSHOP = "workshop";
	private static final String ALL_WORKSHOPS = "allworkshops";
	private static final String All_WORKSHOPS_STYLESHEET = "all_workshops_stylesheet";
	private WorkshopManager workshopManager;

	public HandlerResponse executeActionAndGetResponse(String actionName,Map<String, String> parameters)
	{
		if(actionName.equals(NEW_WORKSHOP))
		{
			String nextWorkshopID = workshopManager.getNextWorkshopID();
			StringBuffer newWorkshopContents = 
					workshopManager.createNewFileFromTemplate(nextWorkshopID,
							workshopManager.getWorkshopDir(),
							workshopManager.getWorkshopTemplate());
			workshopManager.addWorkshop(newWorkshopContents);
			parameters.put("id",nextWorkshopID);
			executeActionAndGetResponse(ALL_WORKSHOPS,parameters);
		}else if(actionName.equals(ALL_WORKSHOPS))
		{
			XMLBuilder allWorkshopsXml = new XMLBuilder("workshops");
			WorkshopRepository repository = 
					workshopManager.getWorkshopRepository();
			Iterator<String> ids = repository.KeyIterator();
			while(ids.hasNext())
			{
				String id = ids.next();
				Workshop workshop = repository.getWorkshop(id);
				allWorkshopsXml.addBelowParent("workshop");
				allWorkshopsXml.addAttribute("id",workshop.getID());
				allWorkshopsXml.addAttribute("name",workshop.getName());
				allWorkshopsXml.addAttribute("status",workshop.getStatus());
				allWorkshopsXml.addAttribute("duration",workshop.getDurationAsString());
			}
			String formattedXml = getFormattedData(allWorkshopsXml.toString());
			return new HandlerResponse(
					new StringBuffer(formattedXml),
					All_WORKSHOPS_STYLESHEET);
		}
		return null;
	}

	private String getFormattedData(String string) {
		return "";
	}

}

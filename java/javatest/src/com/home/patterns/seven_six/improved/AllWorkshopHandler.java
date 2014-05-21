package com.home.patterns.seven_six.improved;

import java.util.Iterator;
import java.util.Map;

import com.home.patterns.seven_six.origin.HandlerResponse;
import com.home.patterns.seven_six.origin.Workshop;
import com.home.patterns.seven_six.origin.WorkshopRepository;
import com.home.patterns.seven_six.origin.XMLBuilder;

public class AllWorkshopHandler extends Handler{
	public static final String All_WORKSHOPS_STYLESHEET = "allworkshops.xls";
	
	public AllWorkshopHandler(CatalogApp catalogApp)
	{
		super(catalogApp);
	}
	
	public HandlerResponse execute(Map<String, String> parameters) throws Exception
	{
		return new HandlerResponse(
				new StringBuffer(allWorkshopsData()),
				All_WORKSHOPS_STYLESHEET);
	}
	
	private String allWorkshopsData()
	{
		XMLBuilder allWorkshopsXml = new XMLBuilder("workshops");
		WorkshopRepository repository = 
				workshopManager().getWorkshopRepository();
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
		return catalogApp.getFormattedData(allWorkshopsXml.toString());
	}
	

}

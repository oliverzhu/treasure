package com.home.patterns.eight_six.origin;

public class Query {
	private SDLogin sdLogin;//need for SD version 5.1
	private SDSession sdSession;//need for SD version 5.1
	
	private SDLoginSession sdLoginSession;//need for SD version 5.2
	private boolean sd52;//tells if we're running under SD 5.2
	private SDQuery sdQuery;//this is need for SD versions 5.1 & 5.2
	
	//this is a login for SD 5.1
	public void login(String server,String user,String password)
	{
		sd52 = false;
		sdSession = sdLogin.loginSession(server,user,password);
	}
	
	//5.2 login
	public void login(String server,String user,String password,String sdConfigFileName)
	{
		sd52 = true;
		sdLoginSession = new SDLoginSession(sdConfigFileName,false);
		sdLoginSession.loginSession(server,user,password);
	}
	
	public void doQuery()
	{
		if(sdQuery != null)
		{
			sdQuery.clearResultSet();
		}
		if(sd52)
		{
			sdQuery = sdLoginSession.createQuery(SDQuery.OPEN_FOR_QUERY);
		}else
		{
			sdQuery = sdSession.createQuery(SDQuery.OPEN_FOR_QUERY);
		}
		executeQuery();
	}

	private void executeQuery() {
		
	}

}

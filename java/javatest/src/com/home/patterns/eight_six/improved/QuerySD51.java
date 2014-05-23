package com.home.patterns.eight_six.improved;

public class QuerySD51 extends AbstractQuery {
	private SDLogin sdLogin;//need for SD version 5.1
	private SDSession sdSession;//need for SD version 5.1
	
	public QuerySD51()
	{
		super();
	}
	
	//this is a login for SD 5.1
	public void login(String server,String user,String password)
	{
		sdSession = sdLogin.loginSession(server,user,password);
	}
	
	public SDQuery createQuery()
	{
		return sdSession.createQuery(SDQuery.OPEN_FOR_QUERY);
	}
		

}

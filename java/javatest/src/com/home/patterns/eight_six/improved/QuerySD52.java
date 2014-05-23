package com.home.patterns.eight_six.improved;

public class QuerySD52 extends AbstractQuery {
	private SDLoginSession sdLoginSession;//need for SD version 5.2
	private String sdConfigFileName;
	
	public QuerySD52(String sdConfigFileName)
	{
		super();
		this.sdConfigFileName = sdConfigFileName;
	}
	
	//5.2 login
	public void login(String server,String user,String password)
	{
		sdLoginSession = new SDLoginSession(sdConfigFileName,false);
		sdLoginSession.loginSession(server,user,password);
	}
		
	public SDQuery createQuery()
	{
		return sdLoginSession.createQuery(SDQuery.OPEN_FOR_QUERY);
	}
		

}

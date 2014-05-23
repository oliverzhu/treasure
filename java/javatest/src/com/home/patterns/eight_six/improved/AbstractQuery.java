package com.home.patterns.eight_six.improved;

public abstract class AbstractQuery implements Query{
	protected SDQuery sdQuery;//this is need for SD versions 5.1 & 5.2
	
	public void doQuery()
	{
		if(sdQuery != null)
		{
			sdQuery.clearResultSet();
		}
		sdQuery = createQuery();
		executeQuery();
	}
	
	public abstract SDQuery createQuery();
	

	protected void executeQuery() {
		
	}

}

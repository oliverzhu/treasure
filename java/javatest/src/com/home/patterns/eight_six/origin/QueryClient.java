package com.home.patterns.eight_six.origin;

public class QueryClient {
	private Query query;
	
	public void loginToDatabase(String db,String user,String password)
	{
		query = new Query();
		if(usingSDVersion52())
		{
			query.login(db, user, password, "");//Login to SD 5.2
		}else
		{
			query.login(db, user, password);//Login to SD 5.1
		}
	}

	private boolean usingSDVersion52() {
		return false;
	}

}

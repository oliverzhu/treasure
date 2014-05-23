package com.home.patterns.eight_six.improved;

public class QueryClient {
	private Query query;
	
	public void loginToDatabase(String db,String user,String password)
	{
		if(usingSDVersion52())
		{
			query = new QuerySD52("");
		}else
		{
			query = new QuerySD51();
		}
		query.login(db, user, password);
	}

	private boolean usingSDVersion52() {
		return false;
	}

}

package com.home.patterns.nine_one.origin;

public class SystemPermission {
	private String state;
	
	private boolean granted;
	
	private final static String REQUESTED = "REQUESTED";
	private final static String CLAIMED = "CLAIMED";
	private final static String DENIED = "DENIED";
	private final static String GRANTED = "GRANTED";
	
	public SystemPermission()
	{
		state = REQUESTED;
		granted = false;
	}
	
	public void claimed()
	{
		if(state.equals(REQUESTED))
		{
			state = CLAIMED;
		}
	}
	
	public void denied()
	{
		if(state.equals(CLAIMED))
		{
			state = DENIED;
		}
	}
	
	public void granted()
	{
		if(!state.equals(CLAIMED)) return;
		state = GRANTED;
		granted = true;
	}
	
	public boolean isGranted()
	{
		return granted;
	}
	
	public String getState()
	{
		return state;
	}

}

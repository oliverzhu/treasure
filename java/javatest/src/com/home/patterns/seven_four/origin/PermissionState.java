package com.home.patterns.seven_four.origin;

public class PermissionState {
	private final String name;
	
	private PermissionState(String name)
	{
		this.name = name;
	}
	
	public String toString()
	{
		return name;
	}
	
	public final static PermissionState REQUESTED = new PermissionState("REQUESTED");
	public final static PermissionState CLAIMED = new PermissionState("CLAIMED");
	public final static PermissionState GRANTED = new PermissionState("GRANTED");
	public final static PermissionState DENIED = new PermissionState("DENIED");
	public final static PermissionState UNIX_REQUESTED = new PermissionState("UNIX_REQUESTED");
	public final static PermissionState UNIX_CLAIMED = new PermissionState("UNIX_CLAIMED");

	
}

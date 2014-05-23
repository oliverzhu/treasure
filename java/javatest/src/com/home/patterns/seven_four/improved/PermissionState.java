package com.home.patterns.seven_four.improved;

public abstract class PermissionState {
	private final String name;
	
	protected PermissionState(String name)
	{
		this.name = name;
	}
	
	public String toString()
	{
		return name;
	}
	
	public final static PermissionState REQUESTED = new PermissionRequested();
	public final static PermissionState CLAIMED = new PermissionClaimed();
	public final static PermissionState GRANTED = new PermissionGranted();
	public final static PermissionState DENIED = new PermissionDenied();
	public final static PermissionState UNIX_REQUESTED = new UnixPermissionRequested();
	public final static PermissionState UNIX_CLAIMED = new UnixPermissionClaimed();
	
	public void claimedBy(SystemAdmin admin,SystemPermission permmission){}

	public void deniedBy(SystemAdmin admin,SystemPermission permmission){}

	public void grantedBy(SystemAdmin admin,SystemPermission permmission){}
	
}

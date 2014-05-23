package com.home.patterns.seven_four.improved;

public class SystemPermission {
	public SystemProfile getProfile() {
		return profile;
	}

	private SystemProfile profile;
	private SystemAdmin admin;
	private boolean granted;
	private boolean isUnixPermissionGranted;
	private PermissionState permission;
	
	public SystemPermission()
	{
		setState(PermissionState.REQUESTED);
		granted = false;
	}
	
	public void claimedBy(SystemAdmin admin)
	{
		permission.claimedBy(admin,this);
	}
	

	public void deniedBy(SystemAdmin admin)
	{
		permission.deniedBy(admin,this);
	}
	

	public void grantedBy(SystemAdmin admin)
	{
		permission.grantedBy(admin,this);
	}
	

	public boolean isUnixPermissionGranted() {
		return isUnixPermissionGranted;
	}

	public boolean isGranted()
	{
		return granted;
	}
	
	public PermissionState getState()
	{
		return permission;
	}
	
	void setState(PermissionState state)
	{
		this.permission = state;
	}
	
	public SystemAdmin getAdmin()
	{
		return admin;
	}
	
	public void willBeHandledBy(SystemAdmin admin) {
		this.admin = admin;
	}
	
	public void notifyUserOfPermissionRequestResult() {
		
	}
	
	public void notifyUnixAdminsOfPermissionRequest() {
		
	}

	public void setGranted(boolean granted) {
		this.granted = granted;
	}

	public void setUnixPermissionGranted(boolean isUnixPermissionGranted) {
		this.isUnixPermissionGranted = isUnixPermissionGranted;
	}

	public void setProfile(SystemProfile profile) {
		this.profile = profile;
	}
	
	

}

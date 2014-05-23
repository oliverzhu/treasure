package com.home.patterns.seven_four.origin;

public class SystemPermission {
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
		if(!getState().equals(PermissionState.REQUESTED) 
				&& !getState().equals(PermissionState.UNIX_REQUESTED))
			return;
		willBeHandledBy(admin);
		if(getState().equals(PermissionState.REQUESTED))
		{
			setState(PermissionState.CLAIMED);
		}else if(getState().equals(PermissionState.UNIX_REQUESTED))
		{
			setState(PermissionState.UNIX_CLAIMED);
		}
	}
	

	public void deniedBy(SystemAdmin admin)
	{
		if(!getState().equals(PermissionState.CLAIMED) 
				&& !getState().equals(PermissionState.UNIX_CLAIMED))
			return;
		if(!this.admin.equals(admin))
			return;
		granted = false;
		isUnixPermissionGranted = false;
		setState(PermissionState.DENIED);
		notifyUserOfPermissionRequestResult();
	}
	

	public void grantedBy(SystemAdmin admin)
	{
		if(!getState().equals(PermissionState.CLAIMED) 
				&& !getState().equals(PermissionState.UNIX_CLAIMED))
			return;
		if(!this.admin.equals(admin))
			return;
		
		if(profile.isUnixPermissionRequired() && getState().equals(PermissionState.UNIX_CLAIMED))
		{
			isUnixPermissionGranted = true;
		}else if(profile.isUnixPermissionRequired() && !isUnixPermissionGranted())
		{
			setState(PermissionState.UNIX_REQUESTED);
			notifyUnixAdminsOfPermissionRequest();
			return;
		}
		setState(PermissionState.GRANTED);
		granted = true;
		notifyUserOfPermissionRequestResult();
	}
	

	private boolean isUnixPermissionGranted() {
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
	
	private void setState(PermissionState permission)
	{
		this.permission = permission;
	}
	
	private void willBeHandledBy(SystemAdmin admin2) {
		
	}
	
	private void notifyUserOfPermissionRequestResult() {
		
	}
	
	private void notifyUnixAdminsOfPermissionRequest() {
		
	}

}

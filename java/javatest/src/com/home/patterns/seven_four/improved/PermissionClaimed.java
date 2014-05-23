package com.home.patterns.seven_four.improved;

public class PermissionClaimed extends PermissionState {

	protected PermissionClaimed() {
		super("CLAIMED");
	}
	
	public void deniedBy(SystemAdmin admin,SystemPermission systemPermission)
	{
		if(!systemPermission.getAdmin().equals(admin))
			return;
		systemPermission.setGranted(false);
		systemPermission.setUnixPermissionGranted(false);
		systemPermission.setState(PermissionState.DENIED);
		systemPermission.notifyUserOfPermissionRequestResult();
	}
	

	public void grantedBy(SystemAdmin admin,SystemPermission systemPermission)
	{
		if(!systemPermission.getAdmin().equals(admin))
			return;
		
		if(systemPermission.getProfile().isUnixPermissionRequired() 
				&& !systemPermission.isUnixPermissionGranted())
		{
			systemPermission.setState(PermissionState.UNIX_REQUESTED);
			systemPermission.notifyUnixAdminsOfPermissionRequest();
			return;
		}
		systemPermission.setState(PermissionState.GRANTED);
		systemPermission.setGranted(true);
		systemPermission.notifyUserOfPermissionRequestResult();
	}

}

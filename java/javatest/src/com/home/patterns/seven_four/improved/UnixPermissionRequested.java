package com.home.patterns.seven_four.improved;

public class UnixPermissionRequested extends PermissionState {

	protected UnixPermissionRequested() {
		super("UNIX_REQUESTED");
	}
	
	public void claimedBy(SystemAdmin admin,SystemPermission permission)
	{
		permission.willBeHandledBy(admin);
		permission.setState(PermissionState.UNIX_CLAIMED);
	}

}

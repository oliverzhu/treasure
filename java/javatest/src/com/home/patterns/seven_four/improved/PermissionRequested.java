package com.home.patterns.seven_four.improved;

public class PermissionRequested extends PermissionState {

	protected PermissionRequested() {
		super("REQUESTED");
	}
	
	//任何与Unix_REQUESTED状态有关的东西都是不需要的，因为我们只关心PermissionRequested类中的Requested状态
	//也需要检查当前状态是否REQUESTED,因为PermissionRequested类本身就保证了这一点
	public void claimedBy(SystemAdmin admin,SystemPermission permission)
	{
		permission.willBeHandledBy(admin);
		permission.setState(PermissionState.CLAIMED);
	}

}

package com.home.activityinfo;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.TextView;

import com.android.otacheck.IApplicationService;
import com.home.R;

public class CheckActivityInfo extends Activity {
	protected static IApplicationService mService = null;
	private TextView tv;
	private ServiceConnection serviceConn  = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/**
	 * @author Oliverzhu
	 */
	public class MyServiceConnection implements ServiceConnection
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = (IApplicationService) IApplicationService.Stub.asInterface(service);
			
			if(mService != null)
			{
				try {
					boolean isRoot = mService.checkRoot();
					if(isRoot)
	                {
	                	tv.setText("root");
	                }else
	                {
	                	tv.setText("ok");
	                }
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cmd);
		tv = (TextView) findViewById(R.id.content);
		
		PackageManager pm = getPackageManager();
		Intent intent = new Intent();
		ComponentName compoentName = 
				new ComponentName("com.tinno.otacheck","com.tinno.otacheck.OtaCheckActivity");
		intent.setComponent(compoentName);
        ResolveInfo ri = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if(ri != null)
        {
        	startActivityForResult(intent, 888);
        }
		serviceConn = new MyServiceConnection();
		Intent service = new Intent("com.android.otacheck.OtaCheckService");
		this.bindService(service, serviceConn, Service.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		while(true) {
//			if(mService != null) {
//				try {
//					boolean isRoot=mService.checkRoot();
//					tv.setText("root");
//					break;
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				
//			}
//		}
//		if(mService != null)
//		{
//			this.unbindService(serviceConn);
//		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mService != null && serviceConn != null)
		{
			this.unbindService(serviceConn);
			mService = null;
			serviceConn = null;
		}
	}
	

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 888)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Bundle bundle = data.getExtras();
                boolean isRoot = bundle.getBoolean("isRoot");
                if(isRoot)
                {
                	tv.setText("root");
                }else
                {
                	tv.setText("ok");
                }
            }
        }
    }
}

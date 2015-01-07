package com.client.customerservicecenter.hub;

import java.util.Map;

import android.content.Context;

import com.cloud.client.CloudClientService;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/9/22
 */
public class CloudLoginHandler extends CloudClientService{
	private static CloudLoginHandler cloudLoginHandler;
	private static final String PACKAGE_NAME = "com.login.sdk";
	
	public CloudLoginHandler(Context context, String askerAppKey) {
		super(context, askerAppKey);
	}
	
	public static CloudLoginHandler getInstance(Context context)
	{
		if(cloudLoginHandler == null)
		{
			cloudLoginHandler = new CloudLoginHandler(context,"");
		}
		return cloudLoginHandler;
	}

	@Override
	protected String initOwnerKey() {
		return "";
	}

	@Override
	protected String initPackageName() {
		return PACKAGE_NAME;
	}
	
	public int allocFileClient(Map userInfoMap) {
		return super.allocFileClient(userInfoMap); 
	}

	
	public MissionObject initMultipartUpload(String filePath, String key) {
        return super.initMultipartUpload(filePath, key);
    }
    
    public CloudFileResult multipartUploadFile(MissionObject missionObject,
            MissionListener listener) {
        return super.multipartUpload(missionObject, listener);
    }
    
    public MissionObject initDownload(String localFile, String key, boolean overwrite) {
        return super.initDownload(key, localFile, overwrite);
    }
    
    public CloudFileResult download(MissionObject missionObject,
            MissionListener listener) {
        return super.downloadFile(missionObject, listener);
    }
	
}

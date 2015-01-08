package com.ape.cloudfile;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.ape.filemanager.Util;
import com.cloud.client.CloudClientService;
import com.cloud.client.CloudObject;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;
import com.cloud.client.sql.SqlResultListener;
import com.cloud.client.sql.SqlResultMultiListener;
import com.cloud.client.sql.SqlResultSingleListener;

public class CloudFileService extends CloudClientService{

    private static final String TAG = "CloudFileSerivce";

    private boolean mIsFileClientValid = false;

    static private CloudFileService sCloudFileSerivce;
    static private Context mContext;

    static public synchronized CloudFileService getInstance(Context context) {
        Context appContext = context.getApplicationContext();
        if (sCloudFileSerivce == null || appContext != mContext) {
            mContext = appContext;
            sCloudFileSerivce = new CloudFileService(context, CloudFileUtil.ASKER_APPKEY);
        }
        Log.i(TAG, "CloudFileService.getInstance");

        return sCloudFileSerivce;
    }

    public void setUserId(String userId) {
        this.mUserId = userId;
    }
    
    public String getUserId()
    {
        return mUserId;
    }
    
    public int allocFileClient(Map userInfo)
    {
    	int result = super.allocFileClient(userInfo);
    	mIsFileClientValid = (result == CloudUtil.CLOUDCLIENT_RESULT_OK);

    	return result;
    }

    public boolean isFileClientValid()
    {
        return mIsFileClientValid;
    }

    private CloudFileService(Context context,String askerAppKey) {
        super(context, askerAppKey);
    }

    public CloudFileResult initFileService() {
        return super.createBucket();
    }
    
    public CloudFileResult listDirectory(String key) {
        return super.listDirectory(key);
    }
    
    public long getDirectorySize(String key)
    {
        return super.getDirectorySize(key);
    }
    
    public CloudFileResult getDirectoryList(String key)
    {
        return super.getDirectoryList(key);
    }
    
    public MissionObject initMultipartUpload(String filePath, String key)
    {
        return super.initMultipartUpload(filePath, key);
    }
    
    public CloudFileResult multipartUploadFile(MissionObject missionObject,
            MissionListener listener) {
        CloudFileResult result = null;
        if (Util.isSDCardReady())
        {
            try
            {
                result = super.multipartUpload(missionObject, listener);
            } catch (Exception e)
            {
            }
        }
        if (result == null)
        {
            result = new CloudFileResult();
            result.setResultCode(CloudUtil.CLOUD_FILE_FILE_NOT_EXIST);
        }
        
        return result;
    }

    @Override
    protected String initOwnerKey() {
        return CloudFileUtil.OWNER_APPKEY;
    }
    
    public MissionObject initDownload(String key, String localFile)
    {
        return super.initDownload(key, localFile, false);
    }
    
    public CloudFileResult downloadfile(MissionObject missionObject,
            MissionListener listener) {
        return super.downloadFile(missionObject, listener);
    }

    
    public CloudFileResult deleteFile(String fileKey) {
        return super.deleteFile(fileKey);
    }
    
    public CloudFileResult createDir(String dir) {
        return super.createDir(dir);
    }

    public CloudFileResult deleteDir(String dir) {
        return super.deleteDir(dir);
    }
    
    public List<MissionObject> listDownloadMissionObject() {
        return super.listDownloadMissionObject();
    }
    
    public List<MissionObject> listUploadMissionObject() {
        return super.listUploadMissionObject();
    }
    
    public void deleteUploadOneMission(String key) {
        super.deleteUploadMission(key);
    }
    
    public void deleteUploadMissionUploadId(String uploadId) {
        super.deleteUploadMissionbyUploadId(uploadId);
    }
    
    public void deleteUploadAllMission() {
        super.deleteAllUploadMission();
    }
    
    // flag is true delete local file, false only delete record.
    public void deleteDownloadOneMission(String key, boolean flag) {
        super.deleteDownloadMission(key, flag);
    }

    // flag is true delete local file, false only delete record.
    public void deleteDownloadAllMission(boolean flag) {
        super.deleteDownloadMission(mUserId, flag);
    }

    @Override
    protected String initPackageName() {
        return CloudFileUtil.PACKAGE_NAME;
    }
    
    
    // for module data used, begin
    public <T extends CloudObject> void createModuleTable(Class<T> clazz, SqlResultListener listener)
    {
        super.create(clazz, listener);
    }
    
    public <T extends CloudObject> void addModuleData(T data, SqlResultSingleListener<T> listener)
    {
        super.insert(data, listener);
    }
    
    public <T extends CloudObject> void updateModuleData(T data, final SqlResultListener listener) {
        super.update(data, listener);
    }
    
    public <T extends CloudObject> void getModuleData(Class<T> clazz, 
            String where, SqlResultMultiListener<T> listener)
    {
        super.getByWhere(clazz, where, listener);
    }

    public <T extends CloudObject> void getModuleDataList(Class<T> clazz,
            SqlResultMultiListener<T> listener)
    {
        super.get(clazz, listener);
    }
    
    public <T extends CloudObject> void deleteModuleData(T entity,
            final SqlResultListener listener)
    {
        super.delete(entity, listener);
    }
    
    public <T extends CloudObject> void deleteModuleData(final Class<T> clazz, 
            String where, final SqlResultListener listener)
    {
        super.delete(clazz, where, listener);
    }
    // for module data used, end.
}

package com.cloud.client.file;

import com.cloud.client.file.MissionObject.MissionType;
import com.cloud.client.file.database.DatabaseAccessManager;

import java.io.File;
import java.util.List;

public abstract class BaseClient implements IFileClient {

    private DatabaseAccessManager mDatabaseManager;
    
    public BaseClient(DatabaseAccessManager databaseManager) {
        this.mDatabaseManager = databaseManager;
    }
    
    @Override
    public abstract CloudFileResult createBucket();

    @Override
    public abstract CloudFileResult uploadFile(String userId, String filePath, String key,
            MissionListener listener);

    @Override
    public abstract CloudFileResult uploadFile(String userId, String filePath, String contentType,
            String key, MissionListener listener);

    @Override
    public abstract CloudFileResult uploadFiles(String userId, List<String> fileList, String key,
            MissionListener listener);

    @Override
    public abstract CloudFileResult uploadDirectory(String userId, String dir, String key,
            MissionListener listener);

    @Override
    public abstract MissionObject initMultipartUpload(String userId, String filePath, String key);

    @Override
    public abstract CloudFileResult multipartUpload(MissionObject missionObject, 
            MissionListener listener);

    @Override
    public abstract CloudFileResult listmultipartUploads(String userId);

    @Override
    public abstract MissionObject initDownload(String userId, String key,
            String localFile, boolean overwrite);

    @Override
    public abstract CloudFileResult downloadFile(MissionObject missionObject,
            MissionListener listener);

    @Override
    public abstract CloudFileResult downloadFiles(String userId, List<String> keyList, String dir,
            MissionListener listener);

    @Override
    public abstract CloudFileResult downloadDirectory(String userId, String key, String dir,
            MissionListener listener);

    @Override
    public abstract CloudFileResult deleteFile(String userId, String key);

    @Override
    public abstract CloudFileResult deleteFiles(String userId, List<String> keyList);

    @Override
    public abstract CloudFileResult deleteDirectory(String userId, String key);

    @Override
    public abstract CloudFileResult listDirectory(String userId, String key);

    @Override
    public abstract CloudFileResult getDirectoryList(String userId, String key);

    @Override
    public abstract long getDirectorySize(String userId, String key);

    @Override
    public abstract CloudFileResult moveFile(String userId, String key, String target);

    @Override
    public abstract CloudFileResult copyFile(String userId, String key, String target);

    @Override
    public abstract CloudFileResult moveFolder(String userId, String srcPath, String destPath);

    @Override
    public abstract CloudFileResult copyFolder(String userId, String srcPath, String destPath);

    @Override
    public abstract CloudFileResult createDir(String userId, String key);

    @Override
    public List<MissionObject> getUploadList(String userId) {
        List<MissionObject> missionObjects = null;

        missionObjects = mDatabaseManager.getUploadMissionObject(userId);
        return missionObjects;
    }

    @Override
    public List<MissionObject> getDownloadList(String userId) {
        List<MissionObject> missionObjects = null;

        missionObjects = mDatabaseManager.getDownloadMissionObject(userId);
        return missionObjects;
    }

    @Override
    public abstract void deleteUploadMission(String userId, String key);

    @Override
    public abstract void deleteUploadMissionbyUploadId(String uploadId);

    @Override
    public abstract void deleteUploadMission(String userId);

    @Override
    public abstract void deleteUploadMission(MissionObject missionObject);

    @Override
    public void deleteDownloadMission(String userId, String key, boolean flag) {
        MissionObject missionObject = null;

        if (flag) {
            missionObject = mDatabaseManager.getDownloadMissionObject(userId, key);
            if (missionObject != null) {
                File file = new File(missionObject.getLocalFile());
                if (file.exists()) {
                    file.delete();
                }
            }
        }

        mDatabaseManager.deleteDownloadFile(userId, key);
    }

    @Override
    public void deleteDownloadMission(String userId, boolean flag) {
        List<MissionObject> missionObjects = null;

        if (flag) {
            missionObjects = mDatabaseManager.getDownloadMissionObject(userId);
            if (missionObjects != null) {
                for (MissionObject object : missionObjects) {
                    File file = new File(object.getLocalFile());
                    if (file.exists()) {
                        file.delete();
                    }
                }
            }
        }

        mDatabaseManager.deleteDownloadFile(userId);
    }

    @Override
    public void deleteDownloadMission(MissionObject missionObject, boolean flag) {
        if (missionObject.getMissionType() == MissionType.DOWNLOAD) {
            deleteDownloadMission(missionObject.getUserId(), missionObject.getKey(), flag);
        }
    }

}

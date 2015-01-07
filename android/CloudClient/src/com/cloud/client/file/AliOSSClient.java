
package com.cloud.client.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.core.FileNameGenerator;

import android.annotation.SuppressLint;

import com.aliyun.android.oss.OSSClient;
import com.aliyun.android.oss.OSSException;
import com.aliyun.android.oss.model.MultipartUploadSummary;
import com.aliyun.android.oss.model.OSSObjectSummary;
import com.aliyun.android.oss.model.Part;
import com.aliyun.android.oss.model.Range;
import com.aliyun.android.oss.task.CustomInputStreamEntity;
import com.aliyun.android.util.MultipartUploadMission;
import com.aliyun.android.util.Pagination;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject.MissionType;
import com.cloud.client.file.database.DatabaseAccessManager;
import com.cloud.util.log.LogCloud;
import com.cloud.util.log.LogMission;

public class AliOSSClient extends BaseClient {
    private OSSClient mOSSClient;
    private String mBucketName = null;
    private static final String TAG = "AliOSSClient";

    private FileNameUtil mFileNameUtil;
    private DatabaseAccessManager mDatabaseManager;
    private LogMission mLogMission;
    private LogCloud mLogCloud;
    
    public AliOSSClient(Credentials credentials, FileNameUtil fileNameUtil, 
            DatabaseAccessManager databaseManager) {
        super(databaseManager);
        
        if (credentials.getAccessId() == null || credentials.getAccessId().equals("")) {
            throw new IllegalArgumentException("accessId must be set");
        }
        if (credentials.getSecretKey() == null || credentials.getSecretKey().equals("")) {
            throw new IllegalArgumentException("secret key must be set");
        }
        if (credentials.getBucketName() == null || credentials.getBucketName().equals("")) {
            throw new IllegalArgumentException("bucketname must be set");
        }
        
        this.mOSSClient = new OSSClient();
        this.mOSSClient.setAccessId(credentials.getAccessId());
        this.mOSSClient.setAccessKey(credentials.getSecretKey());
        this.mBucketName = credentials.getBucketName();
        this.mFileNameUtil = fileNameUtil;
        this.mDatabaseManager = databaseManager;
        this.mOSSClient.setmDatabaseManager(mDatabaseManager);
        this.mLogMission = new LogMission(TAG);
        this.mLogCloud = new LogCloud(TAG);
    }

    @Override
    public String getAccessId() {
        return mOSSClient.getAccessId();
    }

    @Override
    public String getSecretKey() {
        return mOSSClient.getAccessKey();
    }

    @Override
    public String getBuckerName() {
        return mBucketName;
    }

    public OSSClient getOssClient() {
        return mOSSClient;
    }
    
    @Override
    public CloudFileResult createBucket() {
        CloudFileResult result = new CloudFileResult();

        try {
            boolean flag = mOSSClient.createBucket(mBucketName);
            if (!flag) {
                result.setResultCode(CloudUtil.CLOUD_FILE_CREATE_BUCKER_ERROR);
            } else {
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            }
        } catch (OSSException osse) {
            throw osse;
        }

        return result;
    }

    @Override
    public CloudFileResult uploadFile(String userId, String filePath, String key,
            MissionListener listener) {
        return uploadFile(userId, filePath, null, key, listener);
    }

    @Override
    public CloudFileResult uploadFile(String userId, String filePath, String contentType,
            String key, MissionListener listener) {
        CloudFileResult result = new CloudFileResult();

        MissionObject missionObject;
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        File file = new File(filePath);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            missionObject = new MissionObject(MissionType.UPLOAD, userId, key, filePath);
        } catch (FileNotFoundException e) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_FIND);
            result.setMessage(filePath + " not found!");
            return result;
        }

        String md5Hash = null;
        String resultMD5Hash = null;
        try {
            md5Hash = FileNameGenerator.computeMD5Hash(fileInputStream);
            missionObject.setMd5(md5Hash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        if (contentType == null) {
            contentType = Mimetypes.getInstance().getMimetype(file);
        }

        InputStream instream = null;
        CustomInputStreamEntity entity = null;
        try {
            instream = new FileInputStream(file);
            entity = new CustomInputStreamEntity(instream, file.length());
            missionObject.setFileLength(entity.getContentLength());
            if (listener != null) {
                listener.setTotalSize(entity.getContentLength());
                entity.setProgressListener(listener);
            }
            resultMD5Hash = mOSSClient.uploadObject(mBucketName, fullKey, contentType, entity,
                    missionObject);
            if (!md5Hash.equals(resultMD5Hash)) {
                // TODO
            }
            missionObject.setFinished(true);
        } catch (FileNotFoundException fe) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_FIND);
            result.setMessage(filePath + " not found!");
        } catch (OSSException osse) {
            parserException(result, osse);
        } finally {
            if (missionObject.isFinished()) {
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
                missionObject.setTransferredLength(missionObject.getFileLength());
            } else {
                missionObject.setTransferredLength(0);
            }
            missionObject.setLastTime(System.currentTimeMillis());
            mDatabaseManager.coordinateInsertUploadMission(missionObject);
        }

        return result;
    }

    @Override
    public CloudFileResult uploadFiles(String userId, List<String> fileList, String key,
            MissionListener listener) {
        CloudFileResult result = new CloudFileResult();

        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        // TODO
        return result;
    }

    @Override
    public CloudFileResult uploadDirectory(String userId, String dir, String key,
            MissionListener listener) {
        CloudFileResult result = new CloudFileResult();

        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        // TODO
        return result;
    }

    @Override
    public MissionObject initMultipartUpload(String userId, String filePath, String key) {
        MissionObject missionObject = null;
        FileInputStream fileInputStream = null;
        String md5Hash = null;
        
        mLogMission.i("initMultipartUpload", 
                String.format("userId:%s, filePath:%s, key:%s", userId, filePath, key),
                null);
        
        File file = new File(filePath);
        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new OSSException(filePath + " not found!", e);
        }

        try {
            md5Hash = FileNameGenerator.computeMD5Hash(fileInputStream);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {
                fileInputStream.close();
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        missionObject = mDatabaseManager.getUploadMissionObject(userId, key);
        if (missionObject != null) {
            mLogMission.i("initMultipartUpload", "missionObject already in db.",
                    missionObject);
            /* if (missionObject.getMd5().equals(md5Hash)) */{
                return missionObject;
            }
        }

        missionObject = new MissionObject(MissionType.UPLOAD_PART, userId, key, filePath);
        missionObject.setFileLength(file.length());
        missionObject.setMd5(md5Hash);
        long row = mDatabaseManager.insertUploadFile(missionObject);
        mLogMission.i("initMultipartUpload", "create missionObject and insert into db. row:" + row,
                missionObject);
        if (row > 0) {
            missionObject.setId(row);
        } else {
            return null;
        }

        return missionObject;
    }

    @SuppressLint("UseSparseArrays")
    @Override
    public CloudFileResult multipartUpload(MissionObject missionObject, MissionListener listener) {
        CloudFileResult result = new CloudFileResult();
        String contentType = null;
        String fullKey = null;
        File file = null;
        List<Part> partList = null;
        List<Part> finishList = null;
        MultipartUploadMission mission = null;
        long transferred = 0L;
        int currPartNumber = 0;

        if (missionObject.isFinished()) {
            mLogMission.i("multipartUpload", "the finished missionObject.", missionObject);
            result.setResultCode(CloudUtil.CLOUD_FILE_FILE_EXIST);
            return result;
        }
        mLogMission.i("multipartUpload", "BEGIN upload.", missionObject);
        file = new File(missionObject.getLocalFile());
        contentType = Mimetypes.getInstance().getMimetype(file);
        fullKey = mFileNameUtil.getUserFullPath(missionObject.getUserId(), missionObject.getKey());

        if (listener != null) {
            listener.setTotalSize(missionObject.getFileLength());
        }
        try {
            mission = mOSSClient.createMultipartUploadMission(mBucketName, fullKey, file, CloudUtil.ALI_PART_SIZE);
            mission.setMissionListener(listener);
            mission.setContentType(contentType);
            if (missionObject.getUploadId() == null) {
                mission.init();
                missionObject.setUploadId(mission.getUploadId());
                mLogMission.i("multipartUpload", "new missionObject, request uploadId from server!", missionObject);
                if (missionObject.getId() > 0) {
                    missionObject.setPaused(false);
                    mLogMission.i("multipartUpload", "write uploadId into db.", missionObject);
                    mDatabaseManager.updateUploadFile(missionObject);
                } else {
                    long row = mDatabaseManager.insertUploadFile(missionObject);
                    if (row > 0) {
                        missionObject.setId(row);
                        mLogMission.i("multipartUpload", "the uploadMission not in db, insert.", missionObject);
                    } else {
                        result.setResultCode(CloudUtil.CLOUD_FILE_DB_INSERT_ERROR);
                        return result;
                    }

                }
            } else {
                mLogMission.i("multipartUpload", "missionObject already in db.", missionObject);
                mission.setUploadId(missionObject.getUploadId());
            }

            partList = mission.splitFile();
            finishList = missionObject.getFinishPartList();
            missionObject.setTotalParts(mission.getTotalPart());
            mLogMission.i(TAG, "totalPart:" + missionObject.getTotalParts()
                    + " splitPart:" + partList.size(), missionObject);
            Map<Integer, Part> finishMap = new HashMap<Integer, Part>();
            if (finishList != null) {
                mLogMission.i("multipartUpload", "finishedParts:" + finishList.size(), missionObject);
                for (Part part : finishList) {
                    transferred += part.getSize();
                    finishMap.put(part.getPartNumber(), part);
                }
                currPartNumber = finishList.size();
            } else {
                finishList = new ArrayList<Part>();
                missionObject.setFinishPartList(finishList);
            }
            mLogMission.i("multipartUpload", 
                    String.format("Begin upload parts to server, begin number:%d, transferred:%d", currPartNumber, transferred),
                    missionObject);
            for (int i = 0; i < partList.size(); i++) {
                Part part = partList.get(i);
                if (finishMap.get(part.getPartNumber()) != null) {
                    mLogMission.i("multipartUpload", "skip partNumber:" + part.getPartNumber(), missionObject);
                    continue;
                }
                if (listener != null && listener.isCancel()) {
                    missionObject.setPaused(true);
                    result.setResultCode(CloudUtil.CLOUD_FILE_MISSION_CANCEL);
                    result.setMessage("mission cancel!");
                    break;
                }
                mLogMission.i("multipartUpload", "upload a part, partNumber:"
                                + part.getPartNumber(), missionObject);
                mission.uploadPart(part);
                mLogMission.i("multipartUpload", "finish a part, partNumber:"
                                + part.getPartNumber() + " Etag:" + part.getEtag(), missionObject);
                if (part.getStream() != null) {
                    try {
                        part.getStream().close();
                        part.setStream(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                transferred += part.getSize();
                currPartNumber = i + 1;
                mLogMission.i("multipartUpload", "currPartNumber:" + currPartNumber
                        + " transferred:" + transferred, missionObject);
                if (listener != null) {
                    listener.transferred(transferred);
                }
                finishList.add(part);
                missionObject.setTransferredLength(transferred);
                missionObject.setTransferredParts(currPartNumber);
                missionObject.setLastTime(System.currentTimeMillis());
                long row = mDatabaseManager.insertPart(part, missionObject.getId());
                mLogMission.i("multipartUpload", "insert part into db, partNumber:"
                        + part.getPartNumber() + " row:" + row, missionObject);
                if (row <= 0) {
                    result.setResultCode(CloudUtil.CLOUD_FILE_DB_INSERT_ERROR);
                    return result;
                }
                mDatabaseManager.updateUploadFile(missionObject);
                mLogMission.i("multipartUpload", "update missionObject into db.",
                        missionObject);
            }

            if (finishList.size() == partList.size()) {
                mLogMission.i("multipartUpload", "END upload.", missionObject);
                mission.complete(finishList);
                missionObject.setFinished(true);
                mDatabaseManager.deletePart(missionObject.getId());
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            }
        } catch (OSSException osse) {
            parserException(result, osse);
        } finally {
            mDatabaseManager.updateUploadFile(missionObject);
            if (partList != null) {
                for (Part part : partList) {
                    if (part.getStream() != null) {
                        try {
                            part.getStream().close();
                            part.setStream(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return result;
    }

    @Override
    public CloudFileResult listmultipartUploads(String userId) {
        CloudFileResult result = new CloudFileResult();

        mLogCloud.i("listmultipartUploads", "begin to list mulitipartUploads");
        String rootKey = mFileNameUtil.getUserFolder(userId);
        Pagination<MultipartUploadSummary> pagination = null;
        List<MultipartUploadSummary> summaryList = null;
        List<MissionObject> missions = new ArrayList<MissionObject>();
        List<MissionObject> unKnowMissions = new ArrayList<MissionObject>();
        try {
            pagination = mOSSClient.listMultipartUploadMissions(mBucketName, rootKey);
            if (pagination != null) {
                mLogCloud.i("listmultipartUploads", "server response, current marker:%s", 
                        pagination.getCurMarker().toString());
            } else {
                mLogCloud.i("listmultipartUploads", "server response, pagination is null");
            }
            summaryList = pagination.getContents();

            Map<String, MultipartUploadSummary> summaryMap = new HashMap<String, MultipartUploadSummary>();
            for (MultipartUploadSummary summary : summaryList) {
                mLogCloud.i("listmultipartUploads", "SERVER: uploadID:%s", summary.getUploadId());
                summaryMap.put(summary.getUploadId(), summary);
            }

            List<MissionObject> missionObjects = mDatabaseManager.getUploadMissionObject(userId);
            if (missionObjects == null || missionObjects.size() == 0) {
                mLogCloud.i("listmultipartUploads", "There is no multipartUpload in local database!");
            }
            Map<String, MissionObject> multiPartMap = new HashMap<String, MissionObject>();
            for (MissionObject missionObject : missionObjects) {
                mLogCloud.i("listmultipartUploads", "LOCAL DB: uploadID:%s, index:%d",
                        missionObject.getUploadId(), missionObject.getId());
                if (summaryMap.containsKey(missionObject.getUploadId())) {
                    mLogCloud.i("listmultipartUploads", "VALID MISSION: uploadID:%s, index:%d",
                            missionObject.getUploadId(), missionObject.getId());
                    multiPartMap.put(missionObject.getUploadId(), missionObject);
                }
            }

            for (MultipartUploadSummary summary : summaryList) {
                MissionObject object = multiPartMap.get(summary.getUploadId());
                if (object != null) {
                    mLogCloud.i("listmultipartUploads", "MISSION List to start: uploadID:%s, index:%d",
                            object.getUploadId(), object.getId());
                    missions.add(object);
                } else {
                    MissionObject mission = new MissionObject(MissionType.UPLOAD_PART, userId,
                            summary.getKey(), null);
                    mission.setUploadId(summary.getUploadId());
                    mission.setInitiatedTime(summary.getInitiated().getTime());
                    mLogCloud.i("listmultipartUploads", "Unkonwn MISSION List to delete from server: uploadID:%s",
                            summary.getUploadId());
                    unKnowMissions.add(mission);
                }
            }
            result.setMissionList(missions);
            result.setUnKnownMission(unKnowMissions);
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        
        mLogCloud.i("listmultipartUploads", "finish to list mulitipartUploads");
        result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        return result;
    }

    @Override
    public MissionObject initDownload(String userId, String key, String localFile, boolean overwrite) {
        MissionObject missionObject = null;
        FileInputStream fileInputStream = null;
        String md5Hash = null;
        String tmpFilePath = null;
        String tmpFileSuffix = ".cloud";
        StringBuffer tmpFileName = null;

        mLogMission.i("initDownload", "userId:" + userId + " localFile:" + localFile
                + " key:" + key + " overwrite:" + overwrite, null);
        // 临时下载文件<.filename.cloud>，此文件为隐藏文件
        tmpFilePath = FileNameUtil.parseParentPath(localFile);
        tmpFileName = new StringBuffer(tmpFilePath);
        tmpFileName.append(".");
        tmpFileName.append(FileNameUtil.parseName(localFile)).append(tmpFileSuffix);
        mLogMission.i("initDownload", "tmpFileName:" + tmpFileName, null);

        File file = new File(localFile);
        File tmpFile = new File(tmpFileName.toString());
        if (!tmpFile.getParentFile().exists()) {
            tmpFile.getParentFile().mkdirs();
        }
        missionObject = mDatabaseManager.getDownloadMissionObject(userId, key);
        if (missionObject == null) {
            missionObject = new MissionObject(MissionType.DOWNLOAD, userId, key, localFile);
            mLogMission.i("initDownload", "missionObject not in db, create new missionObject.", missionObject);
            missionObject.setTempFile(tmpFileName.toString());
            if (tmpFile.exists()) {
                tmpFile.delete();
                missionObject.setTransferredLength(0);
                mLogMission.i("initDownload", "tmpFile already exist, delete and set transferred 0.", missionObject);
            }
        } else {
            mLogMission.i("initDownload", "missionObject already exist in db.", missionObject);
            if (missionObject.isFinished()) {
                if (tmpFile.exists()) {
                    tmpFile.delete();
                }
                missionObject.setFinished(false);
                missionObject.setTransferredLength(0);
                mLogMission.i("initDownload", "missionObject finished but tmpFile exist still, "
                                + "delete and set transferred 0.", missionObject);
            } else {
                if (!tmpFile.exists()) {
                    missionObject.setTransferredLength(0);
                    mLogMission.i("initDownload", "missionObject not finished and tmpFile not exist, "
                                    + "download again.", missionObject);
                } else {
                    try {
                        missionObject.setTransferredLength(tmpFile.length());
                        fileInputStream = new FileInputStream(tmpFile);
                        mLogMission.i("initDownload", "missionObject not finished and tmpFile exist, "
                                        + "download continue. transferred:"
                                        + missionObject.getTransferredLength(), missionObject);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    try {
                        md5Hash = FileNameGenerator.computeMD5Hash(fileInputStream);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    } finally {
                        try {
                            fileInputStream.close();
                        } catch (Exception ee) {
                            ee.printStackTrace();
                        }
                    }

                    if (!md5Hash.equals(missionObject.getMd5())) {
                        // TODO
                    }
                }
            }
        }

        if (file.exists()) {
            if (overwrite) {
                mLogMission.i("initDownload", "localFile exist, delete.", missionObject);
                file.delete();
            } else {
                mLogMission.i("initDownload", "localFile exist, do nothing.",
                        missionObject);
                return null;
            }
        }

        if (missionObject.getId() > 0) {
            mDatabaseManager.updateDownloadFile(missionObject);
            mLogMission.i("initDownload", "init finish, update downloadMission into db.",
                    missionObject);
        } else {
            long row = mDatabaseManager.insertDownloadFile(missionObject);
            missionObject.setId(row);
            mLogMission.i("initDownload", "init finish, insert downloadMission into db.",
                    missionObject);
            if (row <= 0) {
                return null;
            }
        }
        return missionObject;
    }

    @Override
    public CloudFileResult downloadFile(MissionObject missionObject, MissionListener listener) {
        CloudFileResult result = new CloudFileResult();
        int offset = 100;
        long transferred = 0L;
        Range range = null;
        
        if (missionObject.isFinished()) {
            mLogMission.i("downloadFile", "the finished missionObject.", missionObject);
            result.setResultCode(CloudUtil.CLOUD_FILE_FILE_EXIST);
            return result;
        }
        String fullKey = mFileNameUtil.getUserFullPath(missionObject.getUserId(),
                missionObject.getKey());

        File tmpFile = new File(missionObject.getTempFile());
        mLogMission.i("downloadFile", "local temp file.", missionObject);
        if (tmpFile.exists()) {
            transferred = tmpFile.length();
            missionObject.setTransferredLength(transferred);
            mLogMission.i("downloadFile", "tmpFile exist, set transferred:" + transferred,
                    missionObject);
        }

        if (transferred != 0) {
            if (transferred > offset) {
                transferred -= offset;
            } else {
                transferred = 0;
            }
            range = new Range(transferred, -1L);
            mLogMission.i("downloadFile", "set range:" + range.toString(), missionObject);
        }

        try {
            mLogMission.i("downloadFile", "ready to download file.", missionObject);
            int code = mOSSClient.downloadObject(mBucketName, fullKey, missionObject.getTempFile(),
                    listener, missionObject, range);
            
            if (code == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                // 下载完成，将临时文件重命名为本地文件
                tmpFile.renameTo(new File(missionObject.getLocalFile()));
                mLogMission.i("downloadFile", "download finish, rename localFile:"
                        + missionObject.getLocalFile(), missionObject);
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            } else {
                mLogMission.w("downloadFile", "download file error.", missionObject);
            }
            result.setResultCode(code);
        } catch (OSSException osse) {
            parserException(result, osse);
        } finally {
            // do nothing
        }
        return result;
    }

    @Override
    public CloudFileResult downloadFiles(String userId, List<String> keyList, String dir,
            MissionListener listener) {
        CloudFileResult result = new CloudFileResult();

        // TODO
        return result;
    }

    @Override
    public CloudFileResult downloadDirectory(String userId, String key, String dir,
            MissionListener listener) {
        CloudFileResult result = new CloudFileResult();

        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        // TODO
        return result;
    }

    @Override
    public CloudFileResult deleteFile(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("deleteFile", "delete file from server, userID:%s, key:%s", userId, key);
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        try {
            boolean flag = mOSSClient.deleteObject(mBucketName, fullKey);
            if (flag) {
                mLogCloud.i("deleteFile", "delete file OK, userID:%s, key:%s", userId, key);
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            } else {
                mLogCloud.i("deleteFile", "delete file failure, userID:%s, key:%s", userId, key);
                result.setResultCode(CloudUtil.CLOUD_FILE_DELETE_FILE_ERROR);
            }
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        return result;
    }

    @Override
    public CloudFileResult deleteFiles(String userId, List<String> keyList) {
        CloudFileResult result = new CloudFileResult();
        // TODO
        return result;
    }

    @Override
    public CloudFileResult deleteDirectory(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("deleteDirectory", "delete directory from server, userID:%s, key:%s", userId, key);
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        try {
            boolean flag = mOSSClient.deleteFolder(mBucketName, fullKey);
            if (flag) {
                mLogCloud.i("deleteDirectory", "delete directory OK, userID:%s, key:%s", userId, key);
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            } else {
                mLogCloud.i("deleteDirectory", "delete directory failure, userID:%s, key:%s", userId, key);
                result.setResultCode(CloudUtil.CLOUD_FILE_DELETE_FILE_ERROR);
            }
        } catch (OSSException osse) {
            parserException(result, osse);
        }

        return result;
    }

    @Override
    public CloudFileResult listDirectory(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("listDirectory", "not recursion list server directory, userID:%s, key:%s", userId, key);
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        Pagination<OSSObjectSummary> pagination = null;
        List<OSSObjectSummary> list = null;
        List<CloudFile> fileList = new ArrayList<CloudFile>();
        try {
            pagination = mOSSClient.viewFolder(mBucketName, fullKey);
            mLogCloud.i("listDirectory", "response from server");
            list = pagination.getContents();
            for (OSSObjectSummary oss : list) {
                fileList.add(mFileNameUtil.parseAliOSSFile(userId, oss));
            }
            
            int page = 0;
            int count = 0;
            for (CloudFile file : fileList) {
                mLogCloud.i("listDirectory", "page:%d, count:%d, FILE:%s", page, count++, file.getKey());
            }
            
            while (pagination.hasNext()) {
                mLogCloud.i("listDirectory", "has next page, page:%d", page ++);
//                try {
                    pagination = pagination.next();
//                } catch (OSSException osse) {
//                    result.setResultCode(CloudUtil.CLOUD_FILE_NETWORK_ERROR);
//                }
                list = pagination.getContents();
                for (OSSObjectSummary oss : list) {
                    fileList.add(mFileNameUtil.parseAliOSSFile(userId, oss));
                }
                
                count = 0;
                for (CloudFile file : fileList) {
                    mLogCloud.i("listDirectory", "page:%d, count:%d, FILE:%s", page, count++, file.getKey());
                }
            }
            result.setFileList(fileList);
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        
        mLogCloud.i("listDirectory", "not recursion list server directory finish!");
        return result;
    }

    @Override
    public CloudFileResult getDirectoryList(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("getDirectoryList", "recursion list server directory, userID:%s, key:%s", userId, key);
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        List<CloudFile> fileList = new ArrayList<CloudFile>();
        List<OSSObjectSummary> ossObjects = null;
        try {
            ossObjects = mOSSClient.getFolder(mBucketName, fullKey);
            mLogCloud.i("getDirectoryList", "response from server");
            for (OSSObjectSummary object : ossObjects) {
                fileList.add(mFileNameUtil.parseAliOSSFile(userId, object));
            }
            
            int count = 0;
            for (OSSObjectSummary object : ossObjects) {
                mLogCloud.i("getDirectoryList", "count:%d, FILE:%s", count++, object.getKey());
            }
            result.setFileList(fileList);
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        
        mLogCloud.i("getDirectoryList", "recursion list server directory finish!");
        return result;
    }

    @Override
    public long getDirectorySize(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("getDirectorySize", "recursion list server directory, userID:%s, key:%s", userId, key);
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        List<OSSObjectSummary> ossObjects = null;
        long size = 0L;
        try {
            ossObjects = mOSSClient.getFolder(mBucketName, fullKey);
            mLogCloud.i("getDirectorySize", "response from server");
            
            int count = 0;
            for (OSSObjectSummary object : ossObjects) {
                size += Long.valueOf(object.getSize());
                mLogCloud.i("getDirectorySize", "count:%d, FILE:%s, total size:%d",
                        count++, object.getKey(), size);
            }
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (OSSException osse) {
            parserException(result, osse);
            return result.getResultCode();
        }
        
        mLogCloud.i("getDirectorySize", "recursion list server directory size finish! final size:%d", size);
        return size;
    }

    @Override
    public CloudFileResult moveFile(String userId, String key, String target) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("moveFile", "move server file, userID:%s, original:%s, target:%s",
                userId, key, target);
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        String targetKey = mFileNameUtil.getUserFullPath(userId, target);
        try {
            mOSSClient.moveObject(mBucketName, fullKey, mBucketName, targetKey);
            mLogCloud.i("moveFile", "response from server, move finish!");
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        return result;
    }

    @Override
    public CloudFileResult copyFile(String userId, String key, String target) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("copyFile", "copy server file, userID:%s, original:%s, target:%s",
                userId, key, target);
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        String targetKey = mFileNameUtil.getUserFullPath(userId, target);
        try {
            mOSSClient.copyObject(mBucketName, fullKey, mBucketName, targetKey);
            mLogCloud.i("moveFile", "response from server, copy finish!");
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        return result;
    }

    @Override
    public CloudFileResult moveFolder(String userId, String srcPath, String destPath) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("moveFolder", "move server folder, userID:%s, original:%s, target:%s",
                userId, srcPath, destPath);
        
        String srcPathKey = mFileNameUtil.getUserFullPath(userId, srcPath);
        String destPathKey = mFileNameUtil.getUserFullPath(userId, destPath);
        try {
            boolean flag = mOSSClient.moveFolder(mBucketName, srcPathKey, mBucketName, destPathKey);
            if (!flag) {
                mLogCloud.i("moveFolder", "response from server, move failure!");
                result.setResultCode(CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR);
            } else {
                mLogCloud.i("moveFolder", "response from server, move finish!");
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            }
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        return result;
    }

    @Override
    public CloudFileResult copyFolder(String userId, String srcPath, String destPath) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("copyFolder", "copy server folder, userID:%s, original:%s, target:%s",
                userId, srcPath, destPath);
        
        String srcPathKey = mFileNameUtil.getUserFullPath(userId, srcPath);
        String destPathKey = mFileNameUtil.getUserFullPath(userId, destPath);
        try {
            boolean flag = mOSSClient.copyFolder(mBucketName, srcPathKey, mBucketName, destPathKey);
            if (!flag) {
                mLogCloud.i("copyFolder", "response from server, copy failure!");
                result.setResultCode(CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR);
            } else {
                mLogCloud.i("copyFolder", "response from server, copy finish!");
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            }
        } catch (OSSException osse) {
            parserException(result, osse);
        }
        return result;
    }

    @Override
    public CloudFileResult createDir(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        mLogCloud.i("createDir", "create directory in server, userID:%s, key:%s", userId, key);
        
        String resultMD5Hash = null;
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        try {
            resultMD5Hash = mOSSClient.createFolder(mBucketName, fullKey);
            mLogCloud.i("createDir", "response from server, create directory finish!");
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (OSSException osse) {
            parserException(result, osse);
        }

        return result;
    }

//    @Override
//    public List<MissionObject> getUploadList(String userId) {
//        List<MissionObject> missionObjects = null;
//
//        missionObjects = mDatabaseManager.getUploadMissionObject(userId);
//        return missionObjects;
//    }

//    @Override
//    public List<MissionObject> getDownloadList(String userId) {
//        List<MissionObject> missionObjects = null;
//
//        missionObjects = mDatabaseManager.getDownloadMissionObject(userId);
//        return missionObjects;
//    }

    @Override
    public void deleteUploadMission(String userId, String key) {
        MissionObject missionObject = null;
        
        mLogCloud.i("deleteUploadMission", "delete upload mission, userID:%s, key:%s", userId, key);
        
        missionObject = mDatabaseManager.getUploadMissionObject(userId, key);
        if (missionObject == null) {
            mLogCloud.i("deleteUploadMission", "not has upload mission in local DB");
            return;
        }

        if (missionObject.getMissionType() == MissionType.UPLOAD_PART) {
            if (missionObject.isFinished()|| missionObject.getUploadId() == null || missionObject.getUploadId().equals("")) {
                mLogCloud.i("deleteUploadMission", 
                        "multipartUpload Mission only delete from local DB, index:%d, uploadID:%s",
                        missionObject.getId(), missionObject.getUploadId());
                
                mDatabaseManager.coordinateDeleteUploadMission(userId, key);
                
                mLogCloud.i("deleteUploadMission", "delete finish from local DB, index:%d", missionObject.getId());
            } else {
                mLogCloud.i("deleteUploadMission", 
                        "multipartUpload Mission must delete from server first, index:%d, uploadID:%s",
                        missionObject.getId(), missionObject.getUploadId());
                String fullKey = mFileNameUtil.getUserFullPath(userId, key);
                boolean result = mOSSClient.abortMultipartUpload(mBucketName, fullKey,
                        missionObject.getUploadId());
                if (result) {
                    mLogCloud.i("deleteUploadMission",
                            "delete from server finish, and begin delete from local DB, index:%d",
                            missionObject.getId());
                    
                    mDatabaseManager.coordinateDeleteUploadMission(userId, key);
                    
                    mLogCloud.i("deleteUploadMission", "delete finish from local DB, index:%d", missionObject.getId());
                } else {
                    mLogCloud.i("deleteUploadMission",
                            "delete from server failure, index:%d, uploadID:%s",
                            missionObject.getId(), missionObject.getUploadId());
                }
            }
        } else {
            mLogCloud.i("deleteUploadMission", 
                    "not multipartUpload Mission only delete from local DB, index:%d",
                    missionObject.getId());
            
            mDatabaseManager.deleteUploadFile(userId, key);
            
            mLogCloud.i("deleteUploadMission", "delete finish from local DB, index:%d", missionObject.getId());
        }
    }

    @Override
    public void deleteUploadMission(String userId) {
        List<MissionObject> missionObjects = null;
        
        mLogCloud.i("deleteUploadMission", "<BEGIN> delete specify user's upload mission, userID:%s", userId);
        
        missionObjects = mDatabaseManager.getUploadMissionObject(userId);
        if (missionObjects == null) {
            mLogCloud.i("deleteUploadMission", "the user %d has no upload mission in local DB", userId);
            return;
        }
        int count = 0;
        for (MissionObject object : missionObjects) {
            mLogCloud.i("deleteUploadMission", 
                    "count:%d, multipartUpload Mission to delete, index:%d, uploadID:%s",
                    count ++, object.getId(), object.getUploadId());
            
            if (object.getMissionType() == MissionType.UPLOAD_PART) {
                if (object.isFinished() || object.getUploadId() == null || object.getUploadId().equals("")) {
                    mLogCloud.i("deleteUploadMission", 
                            "multipartUpload Mission only delete from local DB, index:%d, uploadID:%s",
                            object.getId(), object.getUploadId());
                    
                    mDatabaseManager.coordinateDeleteUploadMission(userId, object.getKey());
                    
                    mLogCloud.i("deleteUploadMission", "delete finish from local DB, index:%d", object.getId());
                } else {
                    mLogCloud.i("deleteUploadMission", 
                            "multipartUpload Mission must delete from server first, index:%d, uploadID:%s",
                            object.getId(), object.getUploadId());
                    
                    String fullKey = mFileNameUtil.getUserFullPath(userId, object.getKey());
                    boolean result = mOSSClient.abortMultipartUpload(mBucketName, fullKey,
                            object.getUploadId());
                    if (result) {
                        
                        mLogCloud.i("deleteUploadMission",
                                "delete from server finish, and begin delete from local DB, index:%d",
                                object.getId());
                        
                        mDatabaseManager.coordinateDeleteUploadMission(userId, object.getKey());
                        
                        mLogCloud.i("deleteUploadMission", "delete finish from local DB, index:%d", object.getId());
                    } else {
                        mLogCloud.i("deleteUploadMission",
                                "delete from server failure, index:%d, uploadID:%s",
                                object.getId(), object.getUploadId());
                    }
                }
            } else {
                mLogCloud.i("deleteUploadMission", 
                        "not multipartUpload Mission only delete from local DB, index:%d",
                        object.getId());
                
                mDatabaseManager.deleteUploadFile(userId, object.getKey());
                
                mLogCloud.i("deleteUploadMission", "delete finish from local DB, index:%d", object.getId());
            }
        }
        
        mLogCloud.i("deleteUploadMission", "<ALL FINISH> delete specify user's upload mission, userID:%s", userId);
    }

    @Override
    public void deleteUploadMissionbyUploadId(String uploadId) {
        MissionObject missionObject = null;
        
        mLogCloud.i("deleteUploadMissionbyUploadId", "delete specify upload mission, uploadId:%s", uploadId);
        
        missionObject = mDatabaseManager.getUploadMissionObjectbyUploadId(uploadId);
        if (missionObject == null) {
            mLogCloud.i("deleteUploadMission", "not has upload mission in local DB");
            return;
        }
        String fullKey = mFileNameUtil.getUserFullPath(missionObject.getUserId(),
                missionObject.getKey());
        
        mLogCloud.i("deleteUploadMission", 
                "multipartUpload Mission must delete from server, uploadID:%s",uploadId);
        
        boolean result = mOSSClient.abortMultipartUpload(mBucketName, fullKey, uploadId);
        if (result) {
            mLogCloud.i("deleteUploadMission",
                    "delete from server finish, and begin delete from local DB, uploadId:%d",
                    uploadId);
            
            mDatabaseManager.coordinateDeleteUploadMissionbyUploadId(uploadId);
            
            mLogCloud.i("deleteUploadMission", "delete finish from local DB");
        }
    }

    @Override
    public void deleteUploadMission(MissionObject missionObject) {
        if (missionObject.getMissionType() == MissionType.UPLOAD) {
            deleteUploadMission(missionObject.getUserId(), missionObject.getKey());
        } else if (missionObject.getMissionType() == MissionType.UPLOAD_PART) {
            deleteUploadMissionbyUploadId(missionObject.getUploadId());
        }

    }
//
//    @Override
//    public void deleteDownloadMission(String userId, boolean flag) {
//        List<MissionObject> missionObjects = null;
//
//        if (flag) {
//            missionObjects = mDatabaseManager.getDownloadMissionObject(userId);
//            if (missionObjects != null) {
//                for (MissionObject object : missionObjects) {
//                    File file = new File(object.getLocalFile());
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                }
//            }
//        }
//
//        mDatabaseManager.deleteDownloadFile(userId);
//    }
//
//    @Override
//    public void deleteDownloadMission(String userId, String key, boolean flag) {
//        MissionObject missionObject = null;
//
//        if (flag) {
//            missionObject = mDatabaseManager.getDownloadMissionObject(userId, key);
//            if (missionObject != null) {
//                File file = new File(missionObject.getLocalFile());
//                if (file.exists()) {
//                    file.delete();
//                }
//            }
//        }
//
//        mDatabaseManager.deleteDownloadFile(userId, key);
//    }
//
//    @Override
//    public void deleteDownloadMission(MissionObject missionObject, boolean flag) {
//        if (missionObject.getMissionType() == MissionType.DOWNLOAD) {
//            deleteDownloadMission(missionObject.getUserId(), missionObject.getKey(), flag);
//        }
//    }

    private CloudFileResult parserException(CloudFileResult result, OSSException osse) {
        
//        if (osse.getErrorType().equals(OSSException.NET_OSS_ERROR)) {
//            result.setResultCode(CloudUtil.CLOUD_FILE_NETWORK_ERROR);
//        } else {
//            result.setResultCode(FileResultParser.getResultCode(osse.getErrorCode()));
//        }
        result.setResultCode(FileResultParser.getResultCode(osse.getErrorCode()));
        if (result.getResultCode() == CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR) {
            if (osse.getErrorType().equals(OSSException.NET_OSS_ERROR)) {
                result.setResultCode(CloudUtil.CLOUD_FILE_NETWORK_ERROR);
            }
        }
        result.setMessage(osse.getMessage());
        mLogCloud.e("Error:%d, message:%s", result.getResultCode(), result.getMessage());
        return result;
    }
//    @SuppressLint("UseSparseArrays")
//    @Override
//    public int continueUploadMission(MissionObject missionObject, MissionListener listener) {
//        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
//        long row = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
//        MissionType missionType = missionObject.getMissionType();
//        
//        if (missionType == MissionType.UPLOAD) {
//            row = mDatabaseManager.getUploadFileId(missionObject.getUserId(), missionObject.getKey());
//        } else if (missionType == MissionType.UPLOAD_PART) {
//            row = mDatabaseManager.getUploadFileIdbyUploadId(missionObject.getUploadId());
//        }
//        
//        if (row == CloudUtil.CLOUD_FILE_DB_WRONG_ID) {
//            return CloudUtil.CLOUD_FILE_DB_OPERATE_ERROR;
//        }
//        
//        if (row == 0) {
//            return CloudUtil.CLOUD_FILE_DB_DATA_NOT_FOUND;
//        }
//        
//        if (missionObject.getLocalFile() == null || 
//                missionObject.getLocalFile().equals("")) {
//            return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
//        }
//        
//        File file = new File(missionObject.getLocalFile());
//        if (!file.exists()) {
//            return CloudUtil.CLOUD_FILE_NOT_FIND;
//        }
//        
//        String contentType = Mimetypes.getInstance().getMimetype(file);
//        
//        try {
//    /*        if (missionType == MissionType.UPLOAD) {
//                uploadFile(missionObject.getUserId(), 
//                        missionObject.getLocalFile(), missionObject.getKey(), listener);
//            } else*/ if (missionType == MissionType.UPLOAD_PART) {
//                List<Part> partList = mDatabaseManager.getPartObject(row);
//                String objectKey = mFileNameUtil.getUserFullPath(missionObject.getUserId(),
//                        missionObject.getKey());
//                MultipartUploadMission mission = mOSSClient.createMultipartUploadMission(
//                        BUCKET_NAME, objectKey, file, mPartSize, missionObject);
//                mission.setMissionListener(listener);
//                mission.setContentType(contentType);
//                if (partList == null || partList.size() <= 0) {
//                    result = mission.uploadFile();
//                    partList = mission.getPartList();
//                } else {
//                    Map<Integer, Part> partMap = new HashMap<Integer, Part>();
//                    for (Part part : partList) {
//                        partMap.put(part.getPartNumber(), part);
//                    }
//                    List<Part> wholeList = mission.splitFile();
//                    for (Part part : wholeList) {
//                        if (partMap.get(part.getPartNumber()) != null) {
//                            continue;
//                        }
//                        mission.uploadPart(part);
//                        partList.add(part);
//                    }
//                }
//                mission.complete(partList);
//            }
//        } catch (OSSException osse) {
//            throw osse;
//        }
//        return result;
//    }

//    @Override
//    public int continueDownloadMission(MissionObject missionObject, MissionListener listener) {
//        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
//        return result;
//    }
}

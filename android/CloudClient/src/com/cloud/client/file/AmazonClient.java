package com.cloud.client.file;

import com.aliyun.android.oss.OSSException;
import com.aliyun.android.oss.model.Part;
import com.aliyun.android.oss.model.Range;
import com.aliyun.android.util.Helper;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListMultipartUploadsRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.MultipartUpload;
import com.amazonaws.services.s3.model.MultipartUploadListing;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject.MissionType;
import com.cloud.client.file.database.DatabaseAccessManager;
import com.cloud.util.log.LogMission;

import net.tsz.afinal.core.FileNameGenerator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmazonClient extends BaseClient {

    private static final String TAG = "AmazonClient";
    private String mAccessId = null;
    private String mSecretKey = null;
    private String mBucketName = null;
    private AmazonS3Client mAmazonS3Client = null;
    private DatabaseAccessManager mDatabaseManager = null;
    private FileNameUtil mFileNameUtil = null;
    private LogMission mLogMission;
    
    public AmazonClient(Credentials credentials, FileNameUtil fileNameUtil,
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
        this.mAccessId = credentials.getAccessId();
        this.mSecretKey = credentials.getSecretKey();
        this.mBucketName = credentials.getBucketName();
        this.mAmazonS3Client = new AmazonS3Client(new BasicAWSCredentials(mAccessId, mSecretKey));
        this.mDatabaseManager = databaseManager;
        this.mFileNameUtil = fileNameUtil;
        this.mLogMission = new LogMission(TAG);
    }
    
    @Override
    public String getAccessId() {
        return mAccessId;
    }

    @Override
    public String getSecretKey() {
        return mSecretKey;
    }

    @Override
    public String getBuckerName() {
        return mBucketName;
    }

    @Override
    public CloudFileResult createBucket() {
        try {
            mAmazonS3Client.createBucket(mBucketName);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
        return null;
    }

    @Override
    public CloudFileResult uploadFile(String userId, String filePath, String key,
            MissionListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult uploadFile(String userId, String filePath, String contentType,
            String key, MissionListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult uploadFiles(String userId, List<String> fileList, String key,
            MissionListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult uploadDirectory(String userId, String dir, String key,
            MissionListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MissionObject initMultipartUpload(String userId, String filePath, String key) {
        MissionObject missionObject = null;
        FileInputStream fileInputStream = null;
        String md5Hash = null;

        mLogMission.i("initMultipartUpload", "userId:" + userId + " filePath:" + filePath
                + " key:" + key, null);
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

    @Override
    public CloudFileResult multipartUpload(MissionObject missionObject, MissionListener listener) {
        CloudFileResult result = new CloudFileResult();
        File file = null;
        String contentType = null;
        String fullKey = null;
        List<Part> partList = null;
        List<Part> finishList = null;
        List<PartETag> partETags = null;
        long transferred = 0L;
        int currPartNumber = 0;
        
        if (missionObject.isFinished()) {
            mLogMission.i("multipartUpload", "the finished missionObject.", missionObject);
            result.setResultCode(CloudUtil.CLOUD_FILE_FILE_EXIST);
            return result;
        }
        mLogMission.i("multipartUpload", "BEGIN upload.", missionObject);
        partETags = new ArrayList<PartETag>();
        file = new File(missionObject.getLocalFile());
        contentType = Mimetypes.getInstance().getMimetype(file);
        fullKey = mFileNameUtil.getUserFullPath(missionObject.getUserId(), missionObject.getKey());
        
        if (listener != null) {
            listener.setTotalSize(missionObject.getFileLength());
        }
        try {
            if (missionObject.getUploadId() == null) {
                InitiateMultipartUploadResult initiateMultipartUploadResult = 
                        mAmazonS3Client.initiateMultipartUpload(new InitiateMultipartUploadRequest(mBucketName, fullKey));
                missionObject.setUploadId(initiateMultipartUploadResult.getUploadId());
                if (missionObject.getId() > 0) {
                    missionObject.setPaused(false);
                    mDatabaseManager.updateUploadFile(missionObject);
                    mLogMission.i("multipartUpload", "write uploadId into db. ", missionObject);
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
            }
            
            partList = splitFile(missionObject.getLocalFile(), CloudUtil.AMAZON_PART_SIZE);
            finishList = missionObject.getFinishPartList();
            missionObject.setTotalParts(partList.size());
            mLogMission.i("multipartUpload", "totalPart:" + missionObject.getTotalParts()
                    + " splitPart:" + partList.size(), missionObject);
            Map<Integer, Part> finishMap = new HashMap<Integer, Part>();
            if (finishList != null) {
                mLogMission.i("multipartUpload", "finishedParts:" + finishList.size(),
                        missionObject);
                for (Part part : finishList) {
                    transferred += part.getSize();
                    finishMap.put(part.getPartNumber(), part);
                }
                currPartNumber = finishList.size();
            } else {
                finishList = new ArrayList<Part>();
                missionObject.setFinishPartList(finishList);
            }
            mLogMission.i("multipartUpload", "Begin upload parts to server, "
                    + "begin number:" + currPartNumber + " transferred:" + transferred,
                    missionObject);
            
            for (int i = 0; i < partList.size(); i ++) {
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
                UploadPartRequest uploadPartRequest = new UploadPartRequest();
                uploadPartRequest.setBucketName(mBucketName);
                uploadPartRequest.setUploadId(missionObject.getUploadId());
                uploadPartRequest.setKey(fullKey);
                uploadPartRequest.setPartNumber(part.getPartNumber());
                uploadPartRequest.setPartSize(part.getSize());
                uploadPartRequest.setInputStream(part.getStream());
                UploadPartResult uploadPartResult = mAmazonS3Client.uploadPart(uploadPartRequest);
                part.setEtag(uploadPartResult.getETag());
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
                Part part = null;
                for (int i = 0; i < finishList.size(); i ++) {
                    part = finishList.get(i);
                    PartETag partETag = new PartETag(part.getPartNumber(), part.getEtag());
                    partETags.add(partETag);
                }
                mLogMission.i("multipartUpload", "END upload.", missionObject);
                CompleteMultipartUploadRequest completeMultipartUploadRequest = 
                        new CompleteMultipartUploadRequest(mBucketName, fullKey, missionObject.getUploadId(), partETags);
                mAmazonS3Client.completeMultipartUpload(completeMultipartUploadRequest);
                missionObject.setFinished(true);
                mDatabaseManager.deletePart(missionObject.getId());
                result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
            }
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (AmazonClientException ace) {
            ace.printStackTrace();
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
        List<MultipartUpload> multipartUploads = null;
        List<MissionObject> missions = new ArrayList<MissionObject>();
        List<MissionObject> unKnowMissions = new ArrayList<MissionObject>();

        String rootKey = mFileNameUtil.getUserFolder(userId);
        
        try {
            ListMultipartUploadsRequest listMultipartUploadsRequest = new ListMultipartUploadsRequest(mBucketName);
            listMultipartUploadsRequest.setKeyMarker(rootKey);
            
            MultipartUploadListing multipartUploadListing = mAmazonS3Client.listMultipartUploads(listMultipartUploadsRequest);
            multipartUploads = multipartUploadListing.getMultipartUploads();
            
            Map<String, MultipartUpload> multipartUploadMap = new HashMap<String, MultipartUpload>();
            for (MultipartUpload multipartUpload : multipartUploads) {
                multipartUploadMap.put(multipartUpload.getUploadId(), multipartUpload);
            }
            
            List<MissionObject> missionObjects = mDatabaseManager.getUploadMissionObject(userId);
            Map<String, MissionObject> multiPartMap = new HashMap<String, MissionObject>();
            for (MissionObject missionObject : missionObjects) {
                if (multipartUploadMap.containsKey(missionObject.getUploadId())) {
                    multiPartMap.put(missionObject.getUploadId(), missionObject);
                }
            }
            
            for (MultipartUpload multipartUpload : multipartUploads) {
                MissionObject object = multiPartMap.get(multipartUpload.getUploadId());
                if (object != null) {
                    missions.add(object);
                } else {
                    MissionObject mission = new MissionObject(MissionType.UPLOAD_PART, userId,
                            multipartUpload.getKey(), null);
                    mission.setUploadId(multipartUpload.getUploadId());
                    mission.setInitiatedTime(multipartUpload.getInitiated().getTime());
                    unKnowMissions.add(mission);
                }
            }
            
            result.setMissionList(missions);
            result.setUnKnownMission(unKnowMissions);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
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
    public CloudFileResult downloadFile(MissionObject missionObject, final MissionListener listener) {
        CloudFileResult result = new CloudFileResult();
        RandomAccessFile randomAccessFile = null;
        InputStream inputStream = null;
        ObjectMetadata objectMetadata = null;
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
        if (tmpFile.exists()) {
            transferred = tmpFile.length();
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

        missionObject.setTransferredLength(transferred);
        mLogMission.i("downloadFile", "tmpFile exist, set transferred:" + transferred,
                missionObject);
        
        try {
            objectMetadata = mAmazonS3Client.getObjectMetadata(new GetObjectMetadataRequest(mBucketName, fullKey));
            if (listener != null) {
                listener.setTotalSize(objectMetadata.getContentLength());
            }
            if (missionObject.getFileLength() <= 0L) {
                missionObject.setFileLength(objectMetadata.getContentLength());
                mLogMission.i("downloadFile", "get content length and set file length. fileLength:" + 
                        missionObject.getFileLength(), missionObject);
            }
            
            GetObjectRequest getObjectRequest = new GetObjectRequest(mBucketName, fullKey);
            if (range != null) {
                getObjectRequest.setRange(range.getStart(), missionObject.getFileLength() - 1);
            }
            
            S3Object objectPortion = mAmazonS3Client.getObject(getObjectRequest);
            
            missionObject.setPaused(false);
            if (listener != null) {
                listener.setTotalSize(missionObject.getFileLength());
            }
            inputStream = objectPortion.getObjectContent();
            
            int resultCode = mDatabaseManager.updateDownloadFile(missionObject);
            if (resultCode != CloudUtil.CLOUDCLIENT_RESULT_OK) {
                result.setResultCode(resultCode);
                return result;
            }
            
            randomAccessFile = new RandomAccessFile(missionObject.getTempFile(), "rwd");
            long readoffset = 0;
            if (range != null) {
                readoffset = range.getStart();
                mLogMission.i("downloadFile", "set offset before write file, offset:" + readoffset, missionObject);
            }
            randomAccessFile.seek(readoffset);
            byte[] buffer = new byte[1024 * 4];
            long readTotal = readoffset;
            int readLength = 0;
            int readResult = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
            while (true) {
                if (readTotal == missionObject.getFileLength()) {
                    readResult = CloudUtil.CLOUDCLIENT_RESULT_OK;
                    mLogMission.i("downloadFile", "readTotal == missionObject's fileLength, readTotal:" + readTotal, missionObject);
                    break;
                }
                
                if (listener != null && listener.isCancel()) {
                    missionObject.setPaused(true);
                    readResult = CloudUtil.CLOUD_FILE_MISSION_CANCEL;
                    mLogMission.i("downloadFile", "cancel download missionObject", missionObject);
                    break;
                }
                
                readLength = inputStream.read(buffer);
                mLogMission.i("downloadFile", "read buffer length:" + readLength, missionObject);
                if (readLength == -1) {
                    readResult = CloudUtil.CLOUDCLIENT_RESULT_OK;
                    mLogMission.i("downloadFile", "buffer read finish, readLength:" + readLength, missionObject);
                    break;
                }
                
                randomAccessFile.write(buffer, 0, readLength);
                readTotal += readLength;
                mLogMission.i("downloadFile", "readTotal:" + readTotal, missionObject);
                missionObject.setTransferredLength(readTotal);
                missionObject.setLastTime(System.currentTimeMillis());
                if (listener != null) {
                    listener.transferred(readTotal);
                }
            }
            if (readResult == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                tmpFile.renameTo(new File(missionObject.getLocalFile()));
                mLogMission.i("downloadFile", "download finish, rename localFile:"
                        + missionObject.getLocalFile(), missionObject);
                missionObject.setFinished(true);
                result.setResultCode(readResult);
            }
            result.setResultCode(readResult);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        } catch (IOException ioe) {
            
        } finally {
            mDatabaseManager.updateDownloadFile(missionObject);
            mLogMission.i("downloadFile", "finally, update db", missionObject);
            if (randomAccessFile != null) {
                try {
                    inputStream.close();
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public CloudFileResult downloadFiles(String userId, List<String> keyList, String dir,
            MissionListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult downloadDirectory(String userId, String key, String dir,
            MissionListener listener) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult deleteFile(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        try {
            mAmazonS3Client.deleteObject(new DeleteObjectRequest(mBucketName, fullKey));
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (AmazonClientException ace) {
            ace.printStackTrace();
        }
        return result;
    }

    @Override
    public CloudFileResult deleteFiles(String userId, List<String> keyList) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult deleteDirectory(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        fullKey = Helper.formatPath(fullKey);
        deleteFolder(mBucketName, fullKey);
        result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        
        return result;
    }

    @Override
    public CloudFileResult listDirectory(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(mBucketName);
        listObjectsRequest.setMarker(fullKey);
        listObjectsRequest.setPrefix(fullKey);
        listObjectsRequest.setDelimiter("/");
        
        ObjectListing objectListing = null;
        List<CloudFile> fileList = new ArrayList<CloudFile>();
        String userPath = null;
        try {
            do {
                objectListing = mAmazonS3Client.listObjects(listObjectsRequest);
                for (S3ObjectSummary object : objectListing.getObjectSummaries()) {
                    fileList.add(mFileNameUtil.parseAmazonFile(userId, object));
                }
                for (String commonPrefix : objectListing.getCommonPrefixes()) {
                    userPath = mFileNameUtil.parseKeytoUserPath(userId, commonPrefix);
                    fileList.add(new CloudFile(userPath, 
                            FileNameUtil.parseParentPath(userPath), 
                            FileNameUtil.parseName(userPath), 0, 0, false));
                }
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
        } catch (AmazonServiceException ase) {
            ase.printStackTrace();
        } catch (AmazonClientException ace) {
            ace.printStackTrace();
        }
        
        result.setFileList(fileList);
        result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        return result;
    }

    @Override
    public CloudFileResult getDirectoryList(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        fullKey = Helper.formatPath(fullKey);
        List<CloudFile> fileList = getFolder(userId, mBucketName, fullKey);
        result.setFileList(fileList);
        result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        return result;
    }

    @Override
    public long getDirectorySize(String userId, String key) {
        long size = 0L;
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        fullKey = Helper.formatPath(fullKey);
        List<CloudFile> fileList = getFolder(userId, mBucketName, fullKey);
        for (CloudFile cloudFile : fileList) {
            size += cloudFile.getLength();
        }
        return size;
    }

    @Override
    public CloudFileResult moveFile(String userId, String key, String target) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult copyFile(String userId, String key, String target) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult moveFolder(String userId, String srcPath, String destPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult copyFolder(String userId, String srcPath, String destPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CloudFileResult createDir(String userId, String key) {
        CloudFileResult result = new CloudFileResult();
        
        String fullKey = mFileNameUtil.getUserFullPath(userId, key);
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(0);
        objectMetadata.setContentType("application/x-director");
        PutObjectRequest putObjectRequest = 
                new PutObjectRequest(mBucketName, fullKey, inputStream, objectMetadata);
        try {
            mAmazonS3Client.putObject(putObjectRequest);
            result.setResultCode(CloudUtil.CLOUDCLIENT_RESULT_OK);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
        return result;
    }

    @Override
    public void deleteUploadMission(String userId, String key) {
        MissionObject missionObject = null;

        missionObject = mDatabaseManager.getUploadMissionObject(userId, key);
        if (missionObject == null) {
            return;
        }

        if (missionObject.getMissionType() == MissionType.UPLOAD_PART) {
            if (missionObject.isFinished()|| missionObject.getUploadId() == null || missionObject.getUploadId().equals("")) {
                mDatabaseManager.coordinateDeleteUploadMission(userId, key);
            } else {
                String fullKey = mFileNameUtil.getUserFullPath(userId, key);
                AbortMultipartUploadRequest abortMultipartUploadRequest = 
                        new AbortMultipartUploadRequest(mBucketName, fullKey, missionObject.getUploadId());
                try {
                    mAmazonS3Client.abortMultipartUpload(abortMultipartUploadRequest);
                } catch (AmazonServiceException ase) {
                    
                } catch (AmazonClientException ace) {
                    
                }
                mDatabaseManager.coordinateDeleteUploadMission(userId, key);
            }
        } else {
            mDatabaseManager.deleteUploadFile(userId, key);
        }
    }

    @Override
    public void deleteUploadMissionbyUploadId(String uploadId) {
        MissionObject missionObject = null;

        missionObject = mDatabaseManager.getUploadMissionObjectbyUploadId(uploadId);
        if (missionObject == null) {
            return;
        }
        String fullKey = mFileNameUtil.getUserFullPath(missionObject.getUserId(),
                missionObject.getKey());
        AbortMultipartUploadRequest abortMultipartUploadRequest = 
                new AbortMultipartUploadRequest(mBucketName, fullKey, missionObject.getUploadId());
        try {
            mAmazonS3Client.abortMultipartUpload(abortMultipartUploadRequest);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
        mDatabaseManager.coordinateDeleteUploadMissionbyUploadId(uploadId);
    }

    @Override
    public void deleteUploadMission(String userId) {
        List<MissionObject> missionObjects = null;

        missionObjects = mDatabaseManager.getUploadMissionObject(userId);
        if (missionObjects == null) {
            return;
        }
        for (MissionObject object : missionObjects) {
            if (object.getMissionType() == MissionType.UPLOAD_PART) {
                if (object.isFinished() || object.getUploadId() == null || object.getUploadId().equals("")) {
                    mDatabaseManager.coordinateDeleteUploadMission(userId, object.getKey());
                } else {
                    String fullKey = mFileNameUtil.getUserFullPath(userId, object.getKey());
                    AbortMultipartUploadRequest abortMultipartUploadRequest = 
                            new AbortMultipartUploadRequest(mBucketName, fullKey, object.getUploadId());
                    try {
                        mAmazonS3Client.abortMultipartUpload(abortMultipartUploadRequest);
                    } catch (AmazonServiceException ase) {
                        
                    } catch (AmazonClientException ace) {
                        
                    }
                    mDatabaseManager.coordinateDeleteUploadMission(userId, object.getKey());
                }
            } else {
                mDatabaseManager.deleteUploadFile(userId, object.getKey());
            }
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

    /**
     * 删除文件夹，并递归删除文件夹中的所有文件
     * 
     * @param bucketName
     * @param serverPath
     */
    private void deleteFolder(String bucketName, String serverPath) {
        try {
            serverPath = Helper.formatPath(serverPath);
            deleteSubContents(bucketName, serverPath);
            mAmazonS3Client.deleteObject(bucketName, serverPath);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
    }
    
    /**
     * 删除文件夹下的所有内容，但保留该文件夹
     * 
     * @param bucketName
     * @param serverPath
     */
    private void deleteSubContents(String bucketName, String serverPath) {
        serverPath = Helper.formatPath(serverPath);
        deleteSubFolders(bucketName, serverPath);
        deleteSubObjects(bucketName, serverPath);
    }
    
    /**
     * 删除文件夹下的所有子文件夹
     * 
     * @param bucketName
     * @param serverPath
     */
    private void deleteSubFolders(String bucketName, String serverPath) {
        serverPath = Helper.formatPath(serverPath);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(serverPath);
        listObjectsRequest.setMarker(serverPath);
        listObjectsRequest.setDelimiter("/");
        
        try {
            ObjectListing objectListing = mAmazonS3Client.listObjects(listObjectsRequest);
            for (String object : objectListing.getCommonPrefixes()) {
                deleteFolder(bucketName, object);
            }
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
    }
    
    /**
     * 删除文件夹下的所有子文件
     * 
     * @param bucketName
     * @param serverPath
     */
    private void deleteSubObjects(String bucketName, String serverPath) {
        serverPath = Helper.formatPath(serverPath);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(serverPath);
        listObjectsRequest.setMarker(serverPath);
        listObjectsRequest.setDelimiter("/");
        
        try {
            ObjectListing objectListing = mAmazonS3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                mAmazonS3Client.deleteObject(bucketName, objectSummary.getKey());
            }
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
    }
    
    /**
     * 递归获取文件夹下子文件及子文件夹
     * 
     * @param userId
     * @param bucketName
     * @param serverPath
     * @return
     */
    private List<CloudFile> getFolder(String userId, String bucketName, String serverPath) {
        serverPath = Helper.formatPath(serverPath);
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(serverPath);
        listObjectsRequest.setMarker(serverPath);
        listObjectsRequest.setDelimiter("/");
        
        List<CloudFile> currentList = new ArrayList<CloudFile>();
        List<CloudFile> subFolderList = null;
        String userPath;
        try {
            ObjectListing objectListing = mAmazonS3Client.listObjects(listObjectsRequest);
            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
                currentList.add(mFileNameUtil.parseAmazonFile(userId, objectSummary));
            }
            
            for (String commonPrefix : objectListing.getCommonPrefixes()) {
                userPath = mFileNameUtil.parseKeytoUserPath(userId, commonPrefix);
                currentList.add(new CloudFile(userPath, 
                        FileNameUtil.parseParentPath(userPath), 
                        FileNameUtil.parseName(userPath), 0, 0, false));
                subFolderList = getFolder(userId, bucketName, commonPrefix);
                if (subFolderList != null) {
                    currentList.addAll(subFolderList);
                }
            }
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
        return currentList;
    }
    
    private void moveObject(String srcBucketName, String srcObject, 
            String destBucketName, String destObject) {
        try {
            CopyObjectRequest copyObjectRequest = 
                    new CopyObjectRequest(srcBucketName, srcObject, destBucketName, destObject);
            mAmazonS3Client.copyObject(copyObjectRequest);
            DeleteObjectRequest deleteObjectsRequest = 
                    new DeleteObjectRequest(srcBucketName, srcObject);
            mAmazonS3Client.deleteObject(deleteObjectsRequest);
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
    }
    
    private void copyFolder(String srcBucketName, String srcPath, 
            String destBucketName, String destPath) {
        try {
            srcPath = Helper.formatPath(srcPath);
            destPath = Helper.formatPath(destPath);
            
            mAmazonS3Client.copyObject(srcBucketName, srcPath, destBucketName, destPath);
            ObjectListing objectListing = mAmazonS3Client.listObjects(srcBucketName, srcPath);
            for (S3ObjectSummary object : objectListing.getObjectSummaries()) {
                if (object.getKey().endsWith("/")) {
                    copyFolder(srcBucketName, object.getKey(), destBucketName,
                            object.getKey().replaceFirst(srcPath, destPath));
                } else {
                    mAmazonS3Client.copyObject(srcBucketName, object.getKey(), 
                            destBucketName, object.getKey().replaceFirst(srcPath, destPath));
                }
            }
        } catch (AmazonServiceException ase) {
            
        } catch (AmazonClientException ace) {
            
        }
    }
    
    private void moveFolder(String srcBucketName, String srcPath, 
            String destBucketName, String destPath) {
        copyFolder(srcBucketName, srcPath, destBucketName, destPath);
        deleteFolder(srcBucketName, srcPath);
    }
    
    private List<Part> splitFile(String localFile, int partSize) {
        int totalPart = 0;
        int currPartNumber = 1;
        long offset = 0L; 
        long currPartSize = 0L;
        
        File file = new File(localFile);
        if (!file.exists()) {
            return null;
        }

        totalPart = (int) (file.length() / partSize);
        if (file.length() % partSize != 0) {
            totalPart ++;
        }
        
        if (totalPart > 10000) {
            throw new AmazonClientException("Part number exeeded!");
        }
        
        List<Part> partList = new ArrayList<Part>();
        for (int i = 0; i < totalPart; i ++) {
            currPartNumber = i + 1;
            offset = partSize * i;
            currPartSize = partSize < file.length() - offset ? partSize 
                    : file.length() - offset;
            
            Part part = new Part(currPartNumber);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            
            try {
                inputStream.skip(offset);
            } catch (IOException e) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            
            part.setStream(inputStream);
            part.setSize(currPartSize);
            partList.add(part);
        }
        return partList;
    }
}

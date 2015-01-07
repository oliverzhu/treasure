package com.cloud.client.file.database;

import java.util.ArrayList;
import java.util.List;

import com.aliyun.android.oss.model.Part;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject;
import com.cloud.client.file.MissionObject.MissionType;
import com.cloud.client.file.database.CloudFileColumns.DownloadAllCloumns;
import com.cloud.client.file.database.CloudFileColumns.MultiPartAllCloumns;
import com.cloud.client.file.database.CloudFileColumns.MultiPartBaseCloumns;
import com.cloud.client.file.database.CloudFileColumns.UploadAllColumns;
import com.cloud.client.file.database.CloudFileColumns.UploadBaseColumns;
import com.cloud.client.file.database.CloudFileDatabaseField.Download;
import com.cloud.client.file.database.CloudFileDatabaseField.MultiPart;
import com.cloud.client.file.database.CloudFileDatabaseField.Upload;
import com.cloud.util.log.LogCloud;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseAccessManager {
    
    private static final String TAG = "DatabaseAssceeManager";
    
    private CloudFileDatabaseHepler mCloudFileDatabaseHepler;
    private SQLiteDatabase mSqLiteDatabase;
    private LogCloud mLogCloud;
    
    private static final String UPLOAD_TABLE = CloudFileDatabaseHepler.TABLE_UPLOAD_FILE;
    private static final String MULTIPART_TABLE = CloudFileDatabaseHepler.TABLE_MULTI_PART;
    private static final String DOWNLOAD_TABLE = CloudFileDatabaseHepler.TABLE_DOWNLOAD_FILE;
    
    public DatabaseAccessManager(Context context) {
        mCloudFileDatabaseHepler = new CloudFileDatabaseHepler(context);
        mSqLiteDatabase = mCloudFileDatabaseHepler.getWritableDatabase();
        mLogCloud = new LogCloud(TAG);
    }
    
    /**
     * 将上传任务写入到{UPLOAD_TABLE}表中，任务包含分块上传与普通上传，上传成功与上传失败。
     * 
     * @param uploadObject MissionObject对象
     * @return 所插入数据在{UPLOAD_TABLE}表中的索引值
     */
    public long insertUploadFile(MissionObject uploadObject) {
        long row = mSqLiteDatabase.insert(UPLOAD_TABLE, 
                null, convertMissionValues(uploadObject));
        
        mLogCloud.i("insertUploadFile", formatLogMessage("insertUploadFile", 
                "insert", UPLOAD_TABLE, null, String.valueOf(row)));
        mLogCloud.i("insertUploadFile", "userId:" + uploadObject.getUserId()
                + "key" + uploadObject.getKey() + "type:" + uploadObject.getMissionType());
        
        return row;
    }
    
    /**
     * 将下载任务写入到{DOWNLOAD_TABLE}表中，任务包含下载成功与下载失败。
     * 
     * @param uploadObject MissionObject对象
     * @return 所插入数据在{DOWNLOAD_TABLE}表中的索引值
     */
    public long insertDownloadFile(MissionObject uploadObject) {
        long row = mSqLiteDatabase.insert(DOWNLOAD_TABLE, 
                null, convertMissionValues(uploadObject));
        
        mLogCloud.i("insertDownloadFile", formatLogMessage("insertDownloadFile", 
                "insert", DOWNLOAD_TABLE, null, String.valueOf(row)));
        mLogCloud.i("insertDownloadFile", "userId:" + uploadObject.getUserId()
                + "key" + uploadObject.getKey() + "localFile:" + uploadObject.getLocalFile());
        
        return row;
    }
    
    /**
     * 将{UPLOAD_TABLE}表中{Upload._ID}为{id}的分块上传任务的{part}分块信息写入{MULTIPART_TABLE}中。
     * 
     * @param part 分块对象
     * @param id   分块上传任务在{UPLOAD_TABLE}表中{Upload._ID}字段的值
     * @return 所插入数据在{MULTIPART_TABLE}表中的索引值
     */
    public long insertPart(Part part, long id) {
        long row = mSqLiteDatabase.insert(MULTIPART_TABLE, 
                null, ConvertPartValues(part, id));
        
        mLogCloud.i("insertPart", formatLogMessage("insetPart", 
                "insert", MULTIPART_TABLE, "upload._ID=" + id, String.valueOf(row)));
        mLogCloud.i("insertPart", "part_NO:" + part.getPartNumber() + " part_MD5:" + part.getEtag());

        return row;
    }
    
    /**
     * 更新{UPLOAD_TABLE}表中的上传任务。
     * 
     * @param uploadObject MissionObject对象
     * @return 成功则返回CloudUtil.CLOUDCLIENT_RESULT_OK，失败返回错误码
     */
    public int updateUploadFile(MissionObject uploadObject) {
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        String logMessage = null;
        
//        if (uploadObject.getMissionType() == MissionType.UPLOAD) {
            // 通过user_id与key保证数据的唯一性
            where.append(Upload._USER_ID).append(" =? ");
            where.append("and ").append(Upload.FIELD_KEY).append(" =? ");
            
            whereArgs = new String[] {uploadObject.getUserId(), uploadObject.getKey()};
            
            if (CloudUtil.DEBUG) {
                logMessage = String.format("%s=%s and %s=%s", 
                        Upload._USER_ID, uploadObject.getUserId(), Upload.FIELD_KEY, uploadObject.getKey());
            }
//        } else if (uploadObject.getMissionType() == MissionType.UPLOAD_PART) {
//            // uploadId便可以保证数据的唯一性
//            where.append(Upload.FIELD_UPLOADID).append(" =? ");
//            
//            whereArgs = new String[] {uploadObject.getUploadId()};
//            
//            if (LogFile.DEBUG) {
//                logMessage = String.format("%s=%s", 
//                        Upload.FIELD_UPLOADID, uploadObject.getUploadId());
//            }
//        }
        
        if (CloudUtil.DEBUG) {
            StringBuffer logBuffer = new StringBuffer();
            for (String arg : whereArgs) {
                logBuffer.append(arg).append(",");
            }
            logBuffer.deleteCharAt(logBuffer.length() - 1);
            mLogCloud.i("updateUploadFile", "where:" + where.toString() + "whereArgs:" + logBuffer.toString());
        }
        
        int row = mSqLiteDatabase.update(UPLOAD_TABLE, 
                convertMissionValues(uploadObject), where.toString(), whereArgs);
        
        if (CloudUtil.DEBUG) {
            mLogCloud.i("updateUploadFile", formatLogMessage("updateDownloadFile", 
                    "update", UPLOAD_TABLE, logMessage, String.valueOf(row)));
        }
        if (row > 1) {
            result = CloudUtil.CLOUD_FILE_DB_DATA_NOT_UNIQUENESS;
            return result;
        }
        
        return result;
    }
    
    /**
     * 更新{DOWNLOAD_TABLE}表中的下载任务。
     * 
     * @param uploadObject MissionObject对象
     * @return 成功则返回CloudUtil.CLOUDCLIENT_RESULT_OK，失败返回错误码
     */
    public int updateDownloadFile(MissionObject uploadObject) {
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        
        where.append(Download._USER_ID).append(" =? ");
        where.append("and ").append(Download.FIELD_KEY).append(" =? ");
        whereArgs = new String[] {uploadObject.getUserId(), uploadObject.getKey()};
        
        int row = mSqLiteDatabase.update(DOWNLOAD_TABLE, 
                convertMissionValues(uploadObject), where.toString(), whereArgs);
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    Download._USER_ID, uploadObject.getUserId(), Download.FIELD_KEY, uploadObject.getKey());
            mLogCloud.i("updateDownloadFile", formatLogMessage("updateDownloadFile", 
                    "update", DOWNLOAD_TABLE, logMessage, String.valueOf(row)));
        }
        
        if (row > 1) {
            result = CloudUtil.CLOUD_FILE_DB_DATA_NOT_UNIQUENESS;
        }
        
        return result;
    }
    
    /**
     *  更新表{MULTIPART_TABLE}的分块{part}，此分块为表{UPLOAD_TABLE}中{Upload._ID}为{id}的分块上传任务所有。
     *  
     * @param part 分块对象
     * @param id 分块上传任务在{UPLOAD_TABLE}表中{Upload._ID}字段的值
     * @return 成功则返回CloudUtil.CLOUDCLIENT_RESULT_OK，失败返回错误码
     */
    public int updatePart(Part part, long id) {
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        
        where.append(MultiPart.FIELD_UPLOAD_ID).append(" =? ");
        where.append(" and ").append(MultiPart.FIELD_PART_NO).append(" =? ");
        whereArgs = new String[] {String.valueOf(id), String.valueOf(part.getPartNumber())};
        
        int row = mSqLiteDatabase.update(MULTIPART_TABLE, 
                ConvertPartValues(part, id), where.toString(), whereArgs);
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    MultiPart.FIELD_UPLOAD_ID, id, MultiPart.FIELD_PART_NO, part.getPartNumber());
            mLogCloud.i("updatePart", formatLogMessage("updatePart", 
                    "update", MULTIPART_TABLE, logMessage, String.valueOf(row)));
        }
        if (row > 1) {
            result = CloudUtil.CLOUD_FILE_DB_DATA_NOT_UNIQUENESS;
        }
        
        return result;
    }
    
    /**
     * 删除{UPLOAD_TABLE}表中{Upload.FIELD_UPLOADID}为{uploadId}的上传任务，此任务为分块上传任务。
     * 
     * @param uploadId 字段{Upload.FIELD_UPLOADID}，普通上传任务无此值
     */
    public void deleteUploadFilebyUploadId(String uploadId) {
        int result = mSqLiteDatabase.delete(UPLOAD_TABLE, 
                Upload.FIELD_UPLOADID + " =? ", new String[] {uploadId});
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s", 
                    Upload.FIELD_UPLOADID, uploadId);
            mLogCloud.i("deleteUploadFilebyUploadId", formatLogMessage("deleteUploadFilebyUploadId", 
                    "delete", UPLOAD_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除{UPLOAD_TABLE}表中用户ID为{userId}，key为{key}的上传任务，适用于普通上传任务与分块上传任务。
     * 
     * @param userId 字段{Upload._USER_ID}
     * @param key 字段{Upload.FIELD_KEY}
     */
    public void deleteUploadFile(String userId, String key) {
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        
        where.append(Upload._USER_ID).append(" =? ");
        where.append("and ").append(Upload.FIELD_KEY).append(" =? ");
        whereArgs = new String[] {userId, key};
        
        int result = mSqLiteDatabase.delete(UPLOAD_TABLE, where.toString(), whereArgs);
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    Upload._USER_ID, userId, Upload.FIELD_KEY, key);
            mLogCloud.i("deleteUploadFile", formatLogMessage("deleteUploadFile", 
                    "delete", UPLOAD_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除{UPLOAD_TABLE}表中{userId}的所有上传任务。
     * 
     * @param userId 字段{Upload._USER_ID}
     */
    public void deleteUploadFile(String userId) {
        int result = mSqLiteDatabase.delete(UPLOAD_TABLE, 
                Upload._USER_ID + " =? ", new String[] {userId});
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s", 
                    Upload._USER_ID, userId);
            mLogCloud.i("deleteUploadFile", formatLogMessage("deleteUploadFile", 
                    "delete", UPLOAD_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除{UPLOAD_TABLE}表中{Upload._ID}为{id}的所有上传任务。
     * 
     * @param id 字段{Upload._ID}
     */
    public void deleteUploadFile(long id) {
        int result = mSqLiteDatabase.delete(UPLOAD_TABLE, 
                Upload._ID + " =? ", new String[] {String.valueOf(id)});
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s", 
                    Upload._ID, id);
            mLogCloud.i("deleteUploadFile", formatLogMessage("deleteUploadFile", 
                    "delete", UPLOAD_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除{DOWNLOAD_TABLE}表中{Download._USER_ID}为{userId}，{Download.FIELD_KEY}为{key}的下载任务。
     * 
     * @param userId 字段{Download._USER_ID}
     * @param key 字段{Download.FIELD_KEY}
     */
    public void deleteDownloadFile(String userId, String key) {
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        
        where.append(Download._USER_ID).append(" =? ");
        where.append("and ").append(Download.FIELD_KEY).append(" =? ");
        whereArgs = new String[] {userId, key};
        
        int result = mSqLiteDatabase.delete(DOWNLOAD_TABLE, where.toString(), whereArgs);
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    Download._USER_ID, Download.FIELD_KEY, userId, key);
            mLogCloud.i("deleteDownloadFile", formatLogMessage("deleteDownloadFile", 
                    "delete", DOWNLOAD_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除{DOWNLOAD_TABLE}表中{userId}的所有下载任务。
     * 
     * @param userId 字段{Download._USER_ID}
     */
    public void deleteDownloadFile(String userId) {
        int result = mSqLiteDatabase.delete(DOWNLOAD_TABLE, 
                Download._USER_ID + " =? ", new String[] {userId});
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s", 
                    Download._USER_ID, userId);
            mLogCloud.i("deleteDownloadFile", formatLogMessage("deleteDownloadFile", 
                    "delete", DOWNLOAD_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除表{MULTIPART_TABLE}中{MultiPart.FIELD_PART_NO}为{partNum}的分块，
     * 此分块为表{UPLOAD_TABLE}中{Upload._ID}为{id}的分块上传任务所有。
     * 
     * @param id 分块上传任务在{UPLOAD_TABLE}表中{Upload._ID}字段的值
     * @param partNum 分块编号{MultiPart.FIELD_PART_NO}
     */
    public void deletePart(long id, int partNum) {
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        
        where.append(MultiPart.FIELD_UPLOAD_ID).append(" =? ");
        where.append("and ").append(MultiPart.FIELD_PART_NO).append(" =? ");
        whereArgs = new String[] {String.valueOf(id), String.valueOf(partNum)};
        
        int result = mSqLiteDatabase.delete(MULTIPART_TABLE, where.toString(), whereArgs);
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    MultiPart.FIELD_UPLOAD_ID, MultiPart.FIELD_PART_NO, id, partNum);
            mLogCloud.i("deletePart", formatLogMessage("deletePart", 
                    "delete", MULTIPART_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 删除表{MULTIPART_TABLE}中任务{Upload._ID}为{id}的所有分块
     * 
     * @param id 分块上传任务在{UPLOAD_TABLE}表中{Upload._ID}字段的值
     */
    public void deletePart(long id) {
        int result = mSqLiteDatabase.delete(MULTIPART_TABLE, 
                MultiPart.FIELD_UPLOAD_ID + " =? ", new String[] {String.valueOf(id)});
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s", 
                    MultiPart.FIELD_UPLOAD_ID, id);
            mLogCloud.i("deletePart", formatLogMessage("deletePart", 
                    "delete", MULTIPART_TABLE, logMessage, String.valueOf(result)));
        }
    }
    
    /**
     * 获取表{MULTIPART_TABLE}中{MultiPart.FIELD_PART_NO}为{partNum}的分块索引值
     * 此分块为表{UPLOAD_TABLE}中{Upload._ID}为{id}的分块上传任务所有。
     * 
     * @param id 分块上传任务在{UPLOAD_TABLE}表中{Upload._ID}字段的值
     * @param partNum 分块编号{MultiPart.FIELD_PART_NO}
     * @return {MultiPart.FIELD_PART_NO}为{partNum}，{UPLOAD_TABLE}中{Upload._ID}为{id}分块的索引值
     */
    public long getPartId(long id, int partNum) {
        long row = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(MultiPart.FIELD_UPLOAD_ID).append(" =? ");
        selection.append("and ").append(MultiPart.FIELD_PART_NO).append(" =? ");
        selectionArgs = new String[] {String.valueOf(id), String.valueOf(partNum)};
        
        Cursor cursor = mSqLiteDatabase.query(MULTIPART_TABLE, 
                MultiPartBaseCloumns.MULTIPART_BASE_CLOUMNS, selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[MULTIPART_TABLE]the wrong database, part not uniqueness");
            }
            
            while (cursor.moveToNext()) {
                row = cursor.getLong(MultiPartBaseCloumns.CLOUMNUS_ID);
            }
            
            cursor.close();
        }
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    MultiPart.FIELD_UPLOAD_ID, MultiPart.FIELD_PART_NO, id, partNum);
            mLogCloud.i("getPartId", formatLogMessage("deletePart", 
                    "delete", MULTIPART_TABLE, logMessage, String.valueOf(row)));
        }
        return row;
    }
    
    /**
     * 获取{UPLOAD_TABLE}表中{Download._USER_ID}为{userId}，{Download.FIELD_KEY}为{key}的上传任务索引值
     * 
     * @param userId 字段{Upload._USER_ID}
     * @param key 字段{Upload.FIELD_KEY}
     * @return {UPLOAD_TABLE}中的索引值
     */
    public long getUploadFileId(String userId, String key) {
        long row = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
        
        StringBuffer selection = new StringBuffer();
        selection.append(Upload._USER_ID).append("=? and ");
        selection.append(Upload.FIELD_KEY).append("=?");
        
        Cursor cursor = mSqLiteDatabase.query(UPLOAD_TABLE, 
                UploadBaseColumns.UPLOAD_BASE_COLUMNS, 
                selection.toString(),
                new String[]{userId, key}, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[UPLOAD_TABLE]the wrong database, uploadId not uniqueness");
            }
            
            while (cursor.moveToNext()) {
                row = cursor.getLong(UploadBaseColumns.CLOUMNS_ID);
            }
            
            cursor.close();
        }
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s and %s=%s", 
                    Upload._USER_ID, Upload.FIELD_KEY, userId, key);
            mLogCloud.i("getUploadFileId", formatLogMessage("getUploadFileId", 
                    "select", UPLOAD_TABLE, logMessage, String.valueOf(row)));
        }
        
        return row;
    }
    
    /**
     * 获取{UPLOAD_TABLE}表中{Upload.FIELD_UPLOADID}为{uploadId}的分块上传任务索引值
     * 
     * @param uploadId 字段{Upload.FIELD_UPLOADID}
     * @return {UPLOAD_TABLE}中的索引值
     */
    public long getUploadFileIdbyUploadId(String uploadId) {
        long id = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
        
        if (uploadId == null) {
            return id;
        }
        
        Cursor cursor = mSqLiteDatabase.query(UPLOAD_TABLE,
                UploadBaseColumns.UPLOAD_BASE_COLUMNS,
                Upload.FIELD_UPLOADID + "=?",
                new String[]{uploadId}, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[UPLOAD_TABLE]the wrong database, uploadId not uniqueness");
            }
            
            while (cursor.moveToNext()) {
                id = cursor.getLong(UploadBaseColumns.CLOUMNS_ID);
            }
            
            cursor.close();
        }
        
        if (CloudUtil.DEBUG) {
            String logMessage = String.format("%s=%s", 
                    Upload.FIELD_UPLOADID, uploadId);
            mLogCloud.i("getUploadFileIdbyUploadId", formatLogMessage("getUploadFileId", 
                    "select", UPLOAD_TABLE, logMessage, String.valueOf(id)));
        }
        
        return id;
    }
    
    /**
     * 从数据表{UPLOAD_TABLE}中获取{Upload.FIELD_UPLOADID}为{uploadId}的上传任务对象
     * 
     * @param uploadId 字段{Upload.FIELD_UPLOADID}
     * @return MissionObject对象
     */
    public MissionObject getUploadMissionObjectbyUploadId(String uploadId) {
        MissionObject object = null;
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(Upload.FIELD_UPLOADID).append(" =? ");
        selectionArgs = new String[] {uploadId};
        
        Cursor cursor = mSqLiteDatabase.query(UPLOAD_TABLE, UploadAllColumns.UPLOAD_ALL_COLUMNS,
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[UPLOAD_TABLE]the wrong database, uploadId not uniqueness");
            }
            while (cursor.moveToNext()) {
                object = converUploadMissionObject(cursor);
            }
            
            cursor.close();
        }
        return object;
    }
    
    /**
     * 从数据表{UPLOAD_TABLE}中获取{Upload._USER_ID}为{userId}，{Upload.FIELD_KEY}为{key}的上传任务对象
     * 
     * @param userId 字段{Upload._USER_ID}
     * @param key 字段{Upload.FIELD_KEY}
     * @return MissionObject对象
     */
    public MissionObject getUploadMissionObject(String userId, String key) {
        MissionObject object = null;
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(Upload._USER_ID).append(" =? and ");
        selection.append(Upload.FIELD_KEY).append(" =? ");
        selectionArgs = new String[] {userId, key};
        
        Cursor cursor = mSqLiteDatabase.query(UPLOAD_TABLE, UploadAllColumns.UPLOAD_ALL_COLUMNS,
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[UPLOAD_TABLE]the wrong database, the mission not uniqueness");
            }
            
            while (cursor.moveToNext()) {
                object = converUploadMissionObject(cursor);
            }
            
            cursor.close();
        }
        return object;
    }
    
    /**
     * 从数据表{UPLOAD_TABLE}中获取{Upload._USER_ID}为{userId}的所有上传任务列表
     * 
     * @param userId 字段{Upload._USER_ID}
     * @return MissionObject对象列表
     */
    public List<MissionObject> getUploadMissionObject(String userId) {
        List<MissionObject> objectList = new ArrayList<MissionObject>();
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(Upload._USER_ID).append(" =? ");
        selectionArgs = new String[] {userId};
        
        Cursor cursor = mSqLiteDatabase.query(UPLOAD_TABLE, UploadAllColumns.UPLOAD_ALL_COLUMNS,
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            MissionObject object = null;
            while (cursor.moveToNext()) {
                object = converUploadMissionObject(cursor);
                objectList.add(object);
            }
            cursor.close();
        }
        
        return objectList;
    }
    
    /**
     * 从数据表{DOWNLOAD_TABLE}中获取{Upload._USER_ID}为{userId}，{Upload.FIELD_KEY}为{key}的下载任务Id
     * 
     * @param MissionObject对象
     * @return {DOWNLOAD_TABLE}中的索引值
     */
    public long getDownloadMissionId(MissionObject object) {
        long id = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
        
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(Download._USER_ID).append(" =? and ");
        selection.append(Download.FIELD_KEY).append(" =? ");
        selectionArgs = new String[] {object.getUserId(), object.getKey()};
        
        Cursor cursor = mSqLiteDatabase.query(DOWNLOAD_TABLE, 
                DownloadAllCloumns.DOWNLOAD_ALL_CLOUMNS, 
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[DOWNLOAD_TABLE]the wrong database, the mission not uniqueness");
            }
            
            while (cursor.moveToNext()) {
                id = cursor.getLong(DownloadAllCloumns.CLOUMNS_ID);
            }
            cursor.close();
        }
        return id;
    }
    /**
     * 从数据表{DOWNLOAD_TABLE}中获取{Upload._USER_ID}为{userId}，{Upload.FIELD_KEY}为{key}的下载任务对象
     * 
     * @param userId 字段{Upload._USER_ID}
     * @param key 字段{Upload.FIELD_KEY}
     * @return MissionObject对象
     */
    public MissionObject getDownloadMissionObject(String userId, String key) {
        MissionObject object = null;
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(Download._USER_ID).append(" =? and ");
        selection.append(Download.FIELD_KEY).append(" =? ");
        selectionArgs = new String[] {userId, key};
        
        Cursor cursor = mSqLiteDatabase.query(DOWNLOAD_TABLE, 
                DownloadAllCloumns.DOWNLOAD_ALL_CLOUMNS, 
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[DOWNLOAD_TABLE]the wrong database, the mission not uniqueness");
            }
            
            while (cursor.moveToNext()) {
                object = converDownloadMissionObject(cursor);
            }
            cursor.close();
        }
        return object;
    }
    
    /**
     * 从数据表{DOWNLOAD_TABLE}中获取{Upload._USER_ID}为{userId}的所有下载任务列表
     * 
     * @param userId 字段{Upload._USER_ID}
     * @return MissionObject对象列表
     */
    public List<MissionObject> getDownloadMissionObject(String userId) {
        List<MissionObject> objectList = new ArrayList<MissionObject>();
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(Download._USER_ID).append(" =? ");
        selectionArgs = new String[] {userId};
        
        Cursor cursor = mSqLiteDatabase.query(DOWNLOAD_TABLE, 
                DownloadAllCloumns.DOWNLOAD_ALL_CLOUMNS, 
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            MissionObject object = null;
            while (cursor.moveToNext()) {
                object = converDownloadMissionObject(cursor);
                objectList.add(object);
            }
            cursor.close();
        }
        return objectList;
    }
    
    /**
     * 从数据表{MULTIPART_TABLE}中获取{MultiPart.FIELD_UPLOAD_ID}为{id}，{MultiPart.FIELD_PART_NO}为{partNum}的分块，
     * 
     * @param id 字段{MultiPart.FIELD_UPLOAD_ID}
     * @param partNum 字段{MultiPart.FIELD_PART_NO}
     * @return Part对象
     */
    public Part getPartObject(long id, int partNum) {
        Part part = null;
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(MultiPart.FIELD_UPLOAD_ID).append(" =? and ");
        selection.append(MultiPart.FIELD_PART_NAME).append(" =? ");
        selectionArgs = new String[] {String.valueOf(id)};
        
        Cursor cursor = mSqLiteDatabase.query(MULTIPART_TABLE, 
                MultiPartAllCloumns.MULTIPART_ALL_CLOUMNS, 
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            if (cursor.getCount() > 1) {
                cursor.close();
                throw new IllegalAccessError("[MULTIPART_TABLE]the wrong database, the part not uniqueness");
            }
            while (cursor.moveToNext()) {
                part = converPartObject(cursor);
            }
            cursor.close();
        }
        return part;
    }
    
    /**
     * 从数据表{MULTIPART_TABLE}中获取{MultiPart.FIELD_UPLOAD_ID}为{id}的分块列表，
     * 
     * @param id 字段{MultiPart.FIELD_UPLOAD_ID}
     * @return Part对象列表
     */
    public List<Part> getPartObject(long id) {
        List<Part> partList = new ArrayList<Part>();
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;
        
        selection.append(MultiPart.FIELD_UPLOAD_ID).append(" =? ");
        selectionArgs = new String[] {String.valueOf(id)};
        
        Cursor cursor = mSqLiteDatabase.query(MULTIPART_TABLE, 
                MultiPartAllCloumns.MULTIPART_ALL_CLOUMNS,
                selection.toString(), selectionArgs, null, null, null);
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                partList.add(converPartObject(cursor));
            }
            cursor.close();
        }
        return partList;
    }
    
    /**
     * 同时操作表{UPLOAD_TABLE}与表{MULTIPART_TABLE}
     * 1.如果{UPLOAD_TABLE}表中不存在{uploadObject}则插入，否则退出
     * 2.如果{uploadObject}为分块上传任务且{Upload.FIELD_FINISH}为{false}，则在插入{UPLOAD_TABLE}成功后，
     *   将更新{MULTIPART_TABLE}表中分块信息：如果分块信息存在于{MULTIPART_TABLE}表中则更新，否则插入
     * 3.如果{uploadObject}为分块上传任务且{Upload.FIELD_FINISH}为{true}，则在插入{UPLOAD_TABLE}成功后，则返回
     * 
     * @param uploadObject MissionObject对象
     * @return 成功返回CloudUtil.CLOUDCLIENT_RESULT_OK，否则返回错误码
     */
    public int coordinateInsertUploadMission(MissionObject uploadObject) {
        MissionType missionType = uploadObject.getMissionType();
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        long row = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
        
        if (missionType == MissionType.UPLOAD_PART) {
            row = getUploadFileIdbyUploadId(uploadObject.getUploadId());
        } else {
            row = getUploadFileId(uploadObject.getUserId(), uploadObject.getKey());
        }
        
        if (row > 0){
            result = CloudUtil.CLOUD_FILE_DB_DATA_EXIST;
            return result;
        }
        
        row = insertUploadFile(uploadObject);
        if (row > 0 && missionType == MissionType.UPLOAD_PART) {
            uploadObject.setId(row);
            if (!uploadObject.isFinished()) {
                long part_row = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
                for (Part part : uploadObject.getFinishPartList()) {
                    part_row = getPartId(row, part.getPartNumber());
                    if (part_row > 0) {
                        updatePart(part, row);
                    } else {
                        insertPart(part, row);
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 同时操作表{UPLOAD_TABLE}与表{MULTIPART_TABLE}
     * 1.如果{uploadObject}为分块上传任务且{Upload.FIELD_FINISH}为{false}，则在更新{UPLOAD_TABLE}成功后，
     *   将更新{MULTIPART_TABLE}表中分块信息：如果分块信息存在于{MULTIPART_TABLE}表中则更新，否则插入
     * 2.如果{uploadObject}为分块上传任务且{Upload.FIELD_FINISH}为{true}，则在更新{UPLOAD_TABLE}成功后，
     *   删除此任务在{MULTIPART_TABLE}表中的所有分块信息。
     * 
     * @param uploadObject MissionObject对象
     * @return 成功返回CloudUtil.CLOUDCLIENT_RESULT_OK，否则返回错误码
     */
    public int coordinateUpdateUploadMission(MissionObject uploadObject) {
        MissionType missionType = uploadObject.getMissionType();
        int result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        
        result = updateUploadFile(uploadObject);
        if (result != CloudUtil.CLOUDCLIENT_RESULT_OK) {
            return result;
        }
        
        if (missionType == MissionType.UPLOAD_PART) {
            long row = getUploadFileIdbyUploadId(uploadObject.getUploadId());
            if (row > 0) {
                if (uploadObject.isFinished()) {
                    deletePart(row);
                } else {
                    long part_row = CloudUtil.CLOUD_FILE_DB_WRONG_ID;
                    for (Part part : uploadObject.getFinishPartList()) {
                        part_row = getPartId(row, part.getPartNumber());
                        if (part_row > 0) {
                            updatePart(part, row);
                        } else {
                            insertPart(part, row);
                        }
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * 同时操作表{UPLOAD_TABLE}与表{MULTIPART_TABLE}
     * 1.从表{UPLOAD_TABLE}中删除{Upload.FIELD_UPLOADID}为{uploadId}的记录
     * 2.如果{uploadId}为分块上传记录，则先从{MULTIPART_TABLE}表中删除此记录的分块信息，然后再删除此记录
     * 
     * @param uploadId 字段{Upload.FIELD_UPLOADID}
     */
    public void coordinateDeleteUploadMissionbyUploadId(String uploadId) {
        long row = getUploadFileIdbyUploadId(uploadId);
        
        if(row > 0) {
            deletePart(row);
        }
        deleteUploadFilebyUploadId(uploadId);
    }
    
    /**
     * 同时操作表{UPLOAD_TABLE}与表{MULTIPART_TABLE}
     * 1.从表{UPLOAD_TABLE}中删除{Upload._USER_ID}为{userId}，{Upload.FIELD_KEY}为{key}的记录
     * 2.如果为分块上传记录，则先从{MULTIPART_TABLE}表中删除此记录的分块信息，然后再删除此记录
     * 
     * @param userId 字段{Upload.userId}
     * @param key 字段{Upload.FIELD_KEY}
     */
    public void coordinateDeleteUploadMission(String userId, String key) {
        long row = getUploadFileId(userId, key);
        
        if (row > 0) {
            deletePart(row);
        }
        deleteUploadFile(userId, key);
    }
    
    /**
     * 同时操作表{UPLOAD_TABLE}与表{MULTIPART_TABLE}
     * 1.从表{UPLOAD_TABLE}中删除{Upload._USER_ID}为{userId}的所有记录
     * 2.如果为分块上传记录，则先从{MULTIPART_TABLE}表中删除此记录的分块信息，然后再删除此记录
     * 
     * @param userId 字段{Upload._USER_ID}
     */
    public void coordinateDeleteUploadMission(String userId) {
        List<MissionObject> objectList = null;
        
        objectList = getUploadMissionObject(userId);
        for (MissionObject mission : objectList) {
            if (mission.getMissionType() == MissionType.UPLOAD_PART) {
                deletePart(mission.getId());
                deleteUploadFile(mission.getId());
            }
        }
    }
    
    private MissionObject converUploadMissionObject(Cursor cursor) {
        long id = cursor.getLong(UploadAllColumns.CLOUMNS_ID);
        String userId = cursor.getString(UploadAllColumns.CLOUMNS_USER_ID);
        String key = cursor.getString(UploadAllColumns.CLOUMNS_KEY);
        String localFile = cursor.getString(UploadAllColumns.CLOUMNS_LOCALFILE);
        String md5 = cursor.getString(UploadAllColumns.CLOUMNS_MD5);
        String uploadId = cursor.getString(UploadAllColumns.CLOUMNS_UPLOADID);
        long length = cursor.getLong(UploadAllColumns.CLOUMNS_LENGTH);
        long transferredLength = cursor.getLong(UploadAllColumns.CLOUMNS_TRANSFERRED_LENGTH);
        boolean finish = cursor.getInt(UploadAllColumns.CLOUMNS_FINISH) == 1 ? true : false;
        boolean paused = cursor.getInt(UploadAllColumns.CLOUMNS_PAUSED) == 1 ? true : false;
        long initiatedTime = cursor.getLong(UploadAllColumns.CLOUMNS_INITIATED_TIME);
        long lastTime = cursor.getLong(UploadAllColumns.CLOUMNS_LAST_TIME);
        boolean multiPart = cursor.getInt(UploadAllColumns.CLOUMNS_MULTIPART) == 1 ? true : false;
        int totalPart = cursor.getInt(UploadAllColumns.CLOUMNS_TOTAL_PART);
        int transferredParts = cursor.getInt(UploadAllColumns.CLOUMNS_TRANSFERRED_PARTS);
        
        MissionObject object = new MissionObject(userId, key, localFile);
        object.setId(id);
        object.setLocalFile(localFile);
        object.setMd5(md5);
        object.setFileLength(length);
        object.setTransferredLength(transferredLength);
        object.setFinished(finish);
        object.setPaused(paused);
        object.setInitiatedTime(initiatedTime);
        object.setLastTime(lastTime);
        object.setTotalParts(totalPart);
        object.setTransferredParts(transferredParts);
        object.setUploadId(uploadId);
        if (multiPart) {
            List<Part> list = getPartObject(id);
            object.setFinishPartList(list);
            object.setMissionType(MissionType.UPLOAD_PART);
        } else {
            object.setMissionType(MissionType.UPLOAD);
        }
        
        return object;
    }
    
    private MissionObject converDownloadMissionObject(Cursor cursor) {
        long id = cursor.getLong(DownloadAllCloumns.CLOUMNS_ID);
        String userId = cursor.getString(DownloadAllCloumns.CLOUMNS_USER_ID);
        String key = cursor.getString(DownloadAllCloumns.CLOUMNS_KEY);
        String localFile = cursor.getString(DownloadAllCloumns.CLOUMNS_LOCALFILE);
        String tempFile = cursor.getString(DownloadAllCloumns.CLOUMNS_TEMP_FILE);
        String md5 = cursor.getString(DownloadAllCloumns.CLOUMNS_MD5);
        long length = cursor.getLong(DownloadAllCloumns.CLOUMNS_LENGTH);
        long transferredLength = cursor.getLong(DownloadAllCloumns.CLOUMNS_TRANSFERRED_LENGTH);
        boolean finish = cursor.getInt(DownloadAllCloumns.CLOUMNS_FINISH) == 1 ? true : false;
        boolean paused = cursor.getInt(DownloadAllCloumns.CLOUMNS_PAUSED) == 1 ? true : false;
        long initiatedTime = cursor.getLong(DownloadAllCloumns.CLOUMNS_INITIATED_TIME);
        long lastTime = cursor.getLong(DownloadAllCloumns.CLOUMNS_LAST_TIME);
        boolean existInServer = cursor.getInt(DownloadAllCloumns.CLOUMNS_EXIST_IN_SERVER) == 1 ? true : false;
        
        MissionObject object = new MissionObject(userId, key, localFile);
        object.setId(id);
        object.setLocalFile(localFile);
        object.setTempFile(tempFile);
        object.setMd5(md5);
        object.setFileLength(length);
        object.setTransferredLength(transferredLength);
        object.setFinished(finish);
        object.setPaused(paused);
        object.setInitiatedTime(initiatedTime);
        object.setLastTime(lastTime);
        object.setTotalParts(0);
        object.setFinishPartList(null);
        object.setMissionType(MissionType.DOWNLOAD);
        object.setExistInServer(existInServer);
        
        return object;
    }
    
    private Part converPartObject(Cursor cursor) {
        int partNumber = cursor.getInt(MultiPartAllCloumns.CLOUMNS_PART_NO);
        String md5 = cursor.getString(MultiPartAllCloumns.CLOUMNS_PART_MD5);
        String name = cursor.getString(MultiPartAllCloumns.CLOUMNS_PART_NAME);
        long modified = cursor.getLong(MultiPartAllCloumns.CLOUMNS_LAST_MODIFIED);
        long size = cursor.getLong(MultiPartAllCloumns.CLOUMNS_SIZE);
        
        Part part = new Part(partNumber);
        part.setEtag(md5);
        part.setPartName(name);
        part.setLastModified(modified);
        part.setSize(size);
        return part;
    }
    
    private ContentValues convertMissionValues(MissionObject uploadObject) {
        ContentValues contentValues = new ContentValues();
        
        contentValues.put(Upload._USER_ID, uploadObject.getUserId());
        contentValues.put(Upload.FIELD_KEY, uploadObject.getKey());
        contentValues.put(Upload.FIELD_LOCALFILE, uploadObject.getLocalFile());
        contentValues.put(Upload.FIELD_MD5, uploadObject.getMd5());
        contentValues.put(Upload.FIELD_LENGTH, uploadObject.getFileLength());
        contentValues.put(Upload.FIELD_TRANSFERRED_LENGTH, uploadObject.getTransferredLength());
        contentValues.put(Upload.FIELD_FINISH, uploadObject.isFinished() ? 1 : 0);
        contentValues.put(Upload.FIELD_PAUSED, uploadObject.isPaused() ? 1 : 0);
        contentValues.put(Upload.FIELD_INITIATED_TIME, uploadObject.getInitiatedTime());
        contentValues.put(Upload.FIELD_LAST_TIME, uploadObject.getLastTime());
        if (uploadObject.getMissionType() == MissionType.UPLOAD_PART) {
            contentValues.put(Upload.FIELD_MULTIPART, true);
            contentValues.put(Upload.FIELD_UPLOADID, uploadObject.getUploadId());
            contentValues.put(Upload.FIELD_TOTAL_PART, uploadObject.getTotalParts());
            contentValues.put(Upload.FIELD_TRANSFERRED_PARTS, uploadObject.getTransferredParts());
        } else if (uploadObject.getMissionType() == MissionType.UPLOAD_PART){
            contentValues.put(Upload.FIELD_MULTIPART, false);
            contentValues.put(Upload.FIELD_UPLOADID, "");
            contentValues.put(Upload.FIELD_TOTAL_PART, 0);
            contentValues.put(Upload.FIELD_TRANSFERRED_PARTS, 0);
        } else if (uploadObject.getMissionType() == MissionType.DOWNLOAD) {
            contentValues.put(Download.FIELD_TEMP_FILE, uploadObject.getTempFile());
            contentValues.put(Download.FIELD_EXIST_IN_SERVER, uploadObject.isExistInServer());
        }
        return contentValues;
    }
    
    private ContentValues ConvertPartValues(Part part, long id) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MultiPart.FIELD_UPLOAD_ID, id);
        contentValues.put(MultiPart.FIELD_PART_MD5, part.getEtag());
        contentValues.put(MultiPart.FIELD_PART_NO, part.getPartNumber());
        contentValues.put(MultiPart.FIELD_PART_NAME, part.getPartName());
        contentValues.put(MultiPart.FIELD_LAST_MODIFIED, part.getLastModified());
        contentValues.put(MultiPart.FIELD_SIZE, part.getSize());
        
        return contentValues;
    }
    
    @SuppressLint("DefaultLocale")
    private String formatLogMessage(String method, 
            String action, String table, String where, String result) {
        StringBuffer message = new StringBuffer();
//        message.append("[").append(method).append("]:");
        if (action.toLowerCase().equals("select")) {
            message.append("select from ");
        } else if (action.toLowerCase().equals("insert")) {
            message.append("insert into ");
        } else if (action.toLowerCase().equals("delete")) {
            message.append("delete from ");
        } else if (action.toLowerCase().equals("update")) {
            message.append("update ");
        }
        message.append("[").append(table.toUpperCase()).append("]");
        if (where != null) {
            if (!where.equals("")) {
                message.append("\nwhere:").append(where);
            }
        }
        message.append(" result:").append(result);
        
        return message.toString();
    }
}

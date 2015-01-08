package com.ape.cloudfile.accessDAO;

import com.cloud.client.file.MissionObject;
import com.cloud.client.file.MissionObject.MissionType;
import com.cloud.client.file.database.CloudFileColumns.DownloadAllCloumns;
import com.cloud.client.file.database.CloudFileDatabaseField.Download;
import com.cloud.client.file.database.CloudFileDatabaseHepler;
import com.cloud.client.file.database.CloudFileDatabaseField.Upload;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CloudTransferListDAO
{
    private static final String UPLOAD_TABLE_NAME = CloudFileDatabaseHepler.TABLE_UPLOAD_FILE;
    private static final String DOWNLOAD_TABLE_NAME = CloudFileDatabaseHepler.TABLE_DOWNLOAD_FILE;

    private Context mContext;
    private CloudFileDatabaseHepler mDbHelper;
    private SQLiteDatabase mDB;
    private static CloudTransferListDAO instance = null;

    public static synchronized CloudTransferListDAO getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new CloudTransferListDAO(context);
        }

        return instance;
    }

    private CloudTransferListDAO(Context context)
    {
        mContext = context;
        mDbHelper = new CloudFileDatabaseHepler(mContext);
        mDB = mDbHelper.getWritableDatabase();
    }

    public int deleteUploadRecordByDir(String userKey, String dir)
    {
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;
        
        where.append(Upload._USER_ID).append(" =? ");
        where.append("and ").append(Upload.FIELD_KEY).append(" like ? ");
        whereArgs = new String[] {userKey, dir+"%"};

        return mDB.delete(UPLOAD_TABLE_NAME, where.toString(), whereArgs);
    }

    public int deleteUploadRecordByKey(String userKey, String fileKey)
    {
        StringBuffer where = new StringBuffer();
        String[] whereArgs = null;

        where.append(Upload._USER_ID).append(" =? ");
        where.append("and ").append(Upload.FIELD_KEY).append(" =? ");
        whereArgs = new String[] {userKey, fileKey};

        return mDB.delete(UPLOAD_TABLE_NAME, where.toString(), whereArgs);
    }

    public MissionObject getDownLoadRecord(String userKey, String fileKey)
    {
        MissionObject object = null;
        StringBuffer selection = new StringBuffer();
        String[] selectionArgs = null;

        selection.append(Download._USER_ID).append(" =? and ");
        selection.append(Download.FIELD_KEY).append(" =? ");
        selectionArgs = new String[] {userKey, fileKey};

        Cursor cursor = mDB.query(DOWNLOAD_TABLE_NAME,
                DownloadAllCloumns.DOWNLOAD_ALL_CLOUMNS,
                selection.toString(), selectionArgs, null, null, null);
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
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

                object = new MissionObject(userId, key, localFile);
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
            }
            cursor.close();
        }

        return object;
    }
}

package com.cloud.client.file.database;

import com.cloud.client.CloudUtil;
import com.cloud.client.file.database.CloudFileDatabaseField.Download;
import com.cloud.client.file.database.CloudFileDatabaseField.MultiPart;
import com.cloud.client.file.database.CloudFileDatabaseField.Upload;
import com.cloud.util.log.LogCloud;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CloudFileDatabaseHepler extends SQLiteOpenHelper {

    private static final String TAG = "CloudFileDatabaseHepler";
    
    private static final int DATABASE_VERSION = CloudUtil.CLOUD_FILE_DATABASE_VERSION;
    private static final String CLOUDFILE_DATABASE_NAME = "cloud_mission.db";
    
    public static final String TABLE_UPLOAD_FILE = "upload_files";
    public static final String TABLE_DOWNLOAD_FILE = "download_files";
    public static final String TABLE_MULTI_PART = "multiparts";

    private LogCloud mLog;
    public CloudFileDatabaseHepler(Context context) {
        super(context, CLOUDFILE_DATABASE_NAME, null, DATABASE_VERSION);
        mLog = new LogCloud(TAG);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuffer uploadSQL = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
        uploadSQL.append(TABLE_UPLOAD_FILE).append(" (");
        uploadSQL.append(Upload._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        uploadSQL.append(Upload._USER_ID).append(" TEXT, ");
        uploadSQL.append(Upload.FIELD_KEY).append(" TEXT, ");
        uploadSQL.append(Upload.FIELD_LOCALFILE).append(" TEXT, ");
        uploadSQL.append(Upload.FIELD_MD5).append(" TEXT, ");
        uploadSQL.append(Upload.FIELD_LENGTH).append(" LONG, ");
        uploadSQL.append(Upload.FIELD_MULTIPART).append(" INTEGER DEFAULT 0, ");
        uploadSQL.append(Upload.FIELD_UPLOADID).append(" TEXT DEFAULT NULL, ");
        uploadSQL.append(Upload.FIELD_TOTAL_PART).append(" INTEGER DEFAULT 0, ");
        uploadSQL.append(Upload.FIELD_TRANSFERRED_PARTS).append(" INTEGER DEFAULT 0, ");
        uploadSQL.append(Upload.FIELD_TRANSFERRED_LENGTH).append(" LONG, ");
        uploadSQL.append(Upload.FIELD_FINISH).append(" INTEGER DEFAULT 0, ");
        uploadSQL.append(Upload.FIELD_PAUSED).append(" INTEGER DEFAULT 0, ");
        uploadSQL.append(Upload.FIELD_INITIATED_TIME).append(" LONG, ");
        uploadSQL.append(Upload.FIELD_LAST_TIME).append(" LONG");
        uploadSQL.append(")");
        mLog.i("onCreate", "uploadSql:%s", uploadSQL.toString());
        
        StringBuffer downloadSql = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
        downloadSql.append(TABLE_DOWNLOAD_FILE).append(" (");
        downloadSql.append(Download._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        downloadSql.append(Download._USER_ID).append(" TEXT, ");
        downloadSql.append(Download.FIELD_KEY).append(" TEXT, ");
        downloadSql.append(Download.FIELD_LOCALFILE).append(" TEXT, ");
        downloadSql.append(Download.FIELD_TEMP_FILE).append(" TEXT, ");
        downloadSql.append(Download.FIELD_MD5).append(" TEXT, ");
        downloadSql.append(Download.FIELD_LENGTH).append(" LONG, ");
        downloadSql.append(Download.FIELD_TRANSFERRED_LENGTH).append(" LONG, ");
        downloadSql.append(Download.FIELD_FINISH).append(" INTEGER DEFAULT 0, ");
        downloadSql.append(Download.FIELD_PAUSED).append(" INTEGER DEFAULT 0, ");
        downloadSql.append(Download.FIELD_INITIATED_TIME).append(" LONG, ");
        downloadSql.append(Download.FIELD_LAST_TIME).append(" LONG, ");
        downloadSql.append(Download.FIELD_EXIST_IN_SERVER).append(" INTEGER");
        downloadSql.append(")");
        mLog.i("onCreate", "downloadSql:%s", downloadSql.toString());
        
        StringBuffer multiPartSql = new StringBuffer("CREATE TABLE IF NOT EXISTS ");
        multiPartSql.append(TABLE_MULTI_PART).append(" (");
        multiPartSql.append(MultiPart._ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ");
        multiPartSql.append(MultiPart.FIELD_UPLOAD_ID).append(" INTEGER, ");
        multiPartSql.append(MultiPart.FIELD_PART_NO).append(" INTEGER, ");
        multiPartSql.append(MultiPart.FIELD_PART_MD5).append(" TEXT, ");
        multiPartSql.append(MultiPart.FIELD_PART_NAME).append(" TEXT, ");
        multiPartSql.append(MultiPart.FIELD_LAST_MODIFIED).append(" LONG, ");
        multiPartSql.append(MultiPart.FIELD_SIZE).append(" INTEGER");
         // 外键
        multiPartSql.append(", FOREIGN KEY (").append(MultiPart.FIELD_UPLOAD_ID).append(") REFERENCES ");
        multiPartSql.append(TABLE_UPLOAD_FILE).append(" (").append(Upload._ID).append(")");
        multiPartSql.append(")");
        mLog.i("onCreate", "multiPartSql:%s", multiPartSql.toString());
        
        db.execSQL(uploadSQL.toString());
        db.execSQL(downloadSql.toString());
        db.execSQL(multiPartSql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mLog.i("onUpgrade", "nothing to do!");
    }
}

package com.ape.cloudfile.accessDAO;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CloudFileDBHelper extends SQLiteOpenHelper
{
    private final static int DATABASE_VERSION = 1001;
    private final static String DATABASE_NAME = "cloud_file.db";
    private final static String ACCESS_TABLE = "access_files";
    
    public final static String FIELD_ID = "_id";
    public final static String FIELD_IS_FILE = "is_file";
    public final static String FIELD_KEY = "key";
    public final static String FIELD_NAME = "name";
    public final static String FIELD_PARENT = "parent";
    public final static String FIELD_MIME_TYPE = "mime_type";
    public final static String FIELD_LENGTH = "length";
    public final static String FIELD_MODIFY_TIME = "modify_time";
    public final static String FIELD_ACCESS_TIME = "access_time";
    public final static String FIELD_FILE_TYPE = "file_type";

    
    public CloudFileDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String textType = " TEXT, ";
        String integarType = " INTEGER, ";

        StringBuffer sql = new StringBuffer("Create table ");
        sql.append(ACCESS_TABLE).append("(");
        sql.append(FIELD_ID).append(" INTEGER primary key autoincrement,");
        sql.append(FIELD_IS_FILE).append(integarType);
        sql.append(FIELD_KEY).append(textType);
        sql.append(FIELD_NAME).append(textType);
        sql.append(FIELD_PARENT).append(textType);
        sql.append(FIELD_MIME_TYPE).append(textType);
        sql.append(FIELD_LENGTH).append(integarType);
        sql.append(FIELD_MODIFY_TIME).append(integarType);
        sql.append(FIELD_ACCESS_TIME).append(integarType);
        sql.append(FIELD_FILE_TYPE).append(" INTEGER);");

        db.execSQL(sql.toString());
        
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        if (oldVersion == 1000 && newVersion >= 1001)
        {
            db.execSQL("alter table access_files add column file_type INTEGER;");
        }
        
    }

    public String getTableName()
    {
        return ACCESS_TABLE;
    }
}

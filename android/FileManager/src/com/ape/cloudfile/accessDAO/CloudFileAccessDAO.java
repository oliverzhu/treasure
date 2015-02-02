package com.ape.cloudfile.accessDAO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ape.filemanager.MyLog;
import com.ape.filemanager.Util;
import com.cloud.client.file.CloudFile;

public class CloudFileAccessDAO
{
    private CloudFileDBHelper mDbHelper;
    private SQLiteDatabase mDB;

    private static final String CF_DB_COLUMNS[] =
    {
        CloudFileDBHelper.FIELD_ID,
        CloudFileDBHelper.FIELD_IS_FILE,
        CloudFileDBHelper.FIELD_KEY,
        CloudFileDBHelper.FIELD_NAME,
        CloudFileDBHelper.FIELD_PARENT,
        CloudFileDBHelper.FIELD_MIME_TYPE,
        CloudFileDBHelper.FIELD_LENGTH,
        CloudFileDBHelper.FIELD_MODIFY_TIME,
        CloudFileDBHelper.FIELD_ACCESS_TIME
    };
    private static final int COLUMN_ID_INDEX = 0;
    private static final int COLUMN_IS_FILE_INDEX = 1;
    private static final int CLOUMN_KEY_INDEX = 2;
    private static final int CLOUMN_NAME_INDEX = 3;
    private static final int CLOUMN_PARENT_INDEX = 4;
    private static final int CLOUMN_MIME_INDEX = 5;
    private static final int CLOUMN_LENGTH_INDEX = 6;
    private static final int CLOUMN_MODIFY_TIME_INDEX = 7;
    private static final int CLOUMN_ACCESS_TIME_INDEX = 8;

    private static final String TAG = "CloudFileDAO";
    
    private static CloudFileAccessDAO instance = null;

    static public synchronized CloudFileAccessDAO getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new CloudFileAccessDAO(context);
        }
        
        return instance;
    }

    private CloudFileAccessDAO(Context context)
    {
        mDbHelper = new CloudFileDBHelper(context);
        mDB = mDbHelper.getWritableDatabase();
    }
    
    public boolean isExist(CloudFile file)
    {
        boolean retValue = false;
        String selection = CloudFileDBHelper.FIELD_KEY + "=?";
        String[] selectionArgs = new String[] {file.getKey()};
        
        Cursor cursor = mDB.query(mDbHelper.getTableName(),
                new String[]{"_id"}, selection, selectionArgs, null, null, null);
        
        if (cursor != null)
        {
            if (cursor.getCount() > 0)
            {
                retValue = true;
            }
            cursor.close();
        }

        return retValue;
    }

    public List<CloudFile> getCloudFiles(String path, boolean isDir)
    {
        String selection;
        String[] selectionArgs = {path};
        
        if (isDir)
        {
            selection = CloudFileDBHelper.FIELD_PARENT + "=?";
        } else // is file.
        {
            selection = CloudFileDBHelper.FIELD_KEY + "=?";
        }

        return query(selection, selectionArgs, null);
    }
    
    public List<CloudFile> query(String selection, String[] selectionArgs, String orderBy)
    {
        List<CloudFile> list = null;
        Cursor cursor = mDB.query(mDbHelper.getTableName(),
                CF_DB_COLUMNS, selection, selectionArgs, null, null, null);

        if (cursor != null)
        {
            list = new ArrayList<CloudFile>(cursor.getCount());

            while (cursor.moveToNext())
            {
                String key = cursor.getString(CLOUMN_KEY_INDEX);
                String name = cursor.getString(CLOUMN_NAME_INDEX);
                String parent = cursor.getString(CLOUMN_PARENT_INDEX);
                long length = cursor.getLong(CLOUMN_LENGTH_INDEX);
                long modifyTime = cursor.getLong(CLOUMN_MODIFY_TIME_INDEX);
                long lastAccess = cursor.getLong(CLOUMN_ACCESS_TIME_INDEX);
                boolean isFile = cursor.getInt(COLUMN_IS_FILE_INDEX) == 0 ? false : true;

                CloudFile file = new CloudFile(key, parent, name, length, modifyTime, isFile);
                file.setAccessTime(lastAccess);
                list.add(file);
            }

            cursor.close();
        }

        return list;
    }
    
    public synchronized long insert(CloudFile file)
    {
        long result = 0;

        if (isExist(file))
        {
            result = update(file);
        } else
        {
            result = mDB.insert(mDbHelper.getTableName(), null, createValues(file));
        }

        return result;
    }
    
    public long insertWithoutCheck(CloudFile file)
    {
        return mDB.insert(mDbHelper.getTableName(), null, createValues(file));
    }
    
    public void insertAll(List<CloudFile> list)
    {
        for (CloudFile file : list)
        {
            insert(file);
        }
    }
    
    public int update(CloudFile file)
    {
        String where = CloudFileDBHelper.FIELD_KEY + "=?";
        String[] whereValue = { file.getKey() };
        
        int ret = mDB.update(mDbHelper.getTableName(), createValues(file),
                where, whereValue);

        return ret;
    }
    
    public boolean updatePathAccessTime(String dir)
    {
        boolean retValue = false;
        String where = CloudFileDBHelper.FIELD_PARENT + "=?";
        String[] whereValue = { dir };

        ContentValues cv = new ContentValues();
        cv.put(CloudFileDBHelper.FIELD_ACCESS_TIME, System.currentTimeMillis());
        
        int ret = mDB.update(mDbHelper.getTableName(), cv,
                where, whereValue);
        retValue = ret > 0 ? true : false;

        if (retValue)
        {
            where = CloudFileDBHelper.FIELD_KEY + "=?";
            ret = mDB.update(mDbHelper.getTableName(), cv,
                    where, whereValue);
            retValue = ret > 0 ? true : false;
        }

        return retValue;
    }
    
    public boolean delete(CloudFile file)
    {
        boolean retValue = false;
        String where;
        String[] whereValue;
        
        if (file.isFile())
        {
            where = CloudFileDBHelper.FIELD_KEY + "=?";
            whereValue = new String [] {file.getKey()};
        } else 
        {
            where = CloudFileDBHelper.FIELD_KEY + " like ? ";
            whereValue = new String [] {file.getKey() + "%"};
        }
        
        int ret = mDB.delete(mDbHelper.getTableName(), where, whereValue);
        retValue = ret > 0 ? true : false;

        MyLog.i(TAG, "delete file, where:" + where + ", whereValue:" + whereValue[0]);
        return retValue;
    }
    
    public boolean deleteFromParent(String dir)
    {
        CloudFile dirFile = new CloudFile(dir, null, null, 0, 0, false);
        return delete(dirFile);
    }
    
    public boolean deleteUnderParent(String dir)
    {
        boolean retValue = false;
        String where;
        String[] whereValue;

        where = CloudFileDBHelper.FIELD_PARENT + " like ? ";
        whereValue = new String [] {dir + "%"};
    
        int ret = mDB.delete(mDbHelper.getTableName(), where, whereValue);
        retValue = ret > 0 ? true : false;

        MyLog.i(TAG, "delete file, where:" + where + ", whereValue:" + whereValue[0]);
        return retValue;
    }
    
    public int deleteAll()
    {
        int ret = mDB.delete(mDbHelper.getTableName(), null, null);

        return ret;
    }

    private ContentValues createValues(CloudFile file)
    {
        String key = file.getKey();
        ContentValues cv = new ContentValues();

        cv.put(CloudFileDBHelper.FIELD_IS_FILE, file.isFile() ? 1 : 0);
        cv.put(CloudFileDBHelper.FIELD_KEY, key);
        cv.put(CloudFileDBHelper.FIELD_NAME, file.getName());
        cv.put(CloudFileDBHelper.FIELD_LENGTH, file.getLength());

        String parent = file.getPath();
        if (parent == null)
        {
            String filePath = key.endsWith(File.separator) ?
                    key.substring(0, key.length()-1): key;
            parent = Util.getPathFromFilepath(filePath);
        }
        if (!parent.endsWith(File.separator))
        {
            parent = parent + File.separator;
        }
        cv.put(CloudFileDBHelper.FIELD_PARENT, parent);

        cv.put(CloudFileDBHelper.FIELD_FILE_TYPE, CloudFileType.getCloudFileType(key));
        cv.put(CloudFileDBHelper.FIELD_MODIFY_TIME, file.getModifyTime());
        cv.put(CloudFileDBHelper.FIELD_ACCESS_TIME, System.currentTimeMillis());

        return cv;
    }

}

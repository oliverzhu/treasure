package com.client.customerservicecenter.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.Utils;
import com.client.customerservicecenter.wrapper.CustomDatabasePathContext;

/**
 * 操作服务网点数据库
 * @author jianwen.zhu
 * @since 2014/10/16
 */
public class ServiceOutletsDBManager extends SQLiteOpenHelper implements DBHelper{
	private Context mContext;
	private final int BUFFER_SIZE = 400000;
    public static final String DB_NAME = "servicesoutlets.db";
    private static String dbPath;
    private String dbFile;
    
    private SQLiteDatabase database;
 
    private Integer lock = 0;
    /**
	 * constructor the db helper
	 */
	public ServiceOutletsDBManager(Context context) {
		super(new CustomDatabasePathContext(context, getDirPath(context)), DB_NAME, null, 1);
		this.mContext = context;
    	dbFile = dbPath + "/" + DB_NAME;
    	importDatabase();
	}
    
    private void importDatabase()
    {
    	File file = new File(dbFile);
    	
    	int newDBVersion = mContext.getResources().getInteger(R.integer.db_version);
    	int oldDBVersion = Preferences.getDatabaseVersion(AppApplication.mPrefs);
    	
    	if(newDBVersion > oldDBVersion)
    	{
    		if(file.exists())
    		{
    			file.delete();
    		}
    		createDatabaseFile(file,newDBVersion);
    	}else if(!file.exists())
    	{
    		createDatabaseFile(file,newDBVersion);
    	}
    }

    @Override
	public void executeSQL(String sql) {
		synchronized (lock) {
			database = getWritableDatabase();
			database.execSQL(sql);
		}
	}
	
	@Override
	public void executeSQL(String sql, List<String> params) {
		executeSQL(Utils.combinaStr(sql, params));
	}
	
	@Override
	public Cursor query(String sql) {
		synchronized (lock) {
			database = getReadableDatabase();
			Cursor result = database.rawQuery(sql, null);
			return result;
		}
	}

	@Override
	public Cursor query(String sql, List<String> params) {
		return query(Utils.combinaStr(sql, params));
	}

	@Override
	public void batchExecSQL(List<String> addSQL, List<String> updateSQL, List<String> deleteSQL) {
		database = getWritableDatabase();
		database.beginTransaction();
		for(String item : addSQL) {
			database.execSQL(item);
		}
		database.setTransactionSuccessful();
		database.endTransaction();
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
		
	}
	
	private static String getDirPath(Context context)
	{
		dbPath = context.getCacheDir().getPath();
		return dbPath;
	}
	
	private void createDatabaseFile(File file,int newVersion)
	{
		InputStream is = null;
		FileOutputStream fos = null;
		try {
			is = mContext.getResources().openRawResource(
			        R.raw.servicesoutlets);
			fos = new FileOutputStream(file);
			byte[] buffer = new byte[BUFFER_SIZE];
			int count = 0;
			while ((count = is.read(buffer)) > 0) {
			    fos.write(buffer, 0, count);
			}
			
			Preferences.setDatabaseVersion(AppApplication.mPrefs, newVersion);
		}  catch (Exception e) {
			e.printStackTrace();
		}  finally
		{
			if(fos != null)
			{
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(is != null)
			{
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
            
		}
	}
}

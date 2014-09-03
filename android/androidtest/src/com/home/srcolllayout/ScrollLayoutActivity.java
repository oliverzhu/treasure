package com.home.srcolllayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.widget.LinearLayout;

import com.home.R;
import com.home.util.ContextUtils;
import com.home.util.FileUtils;

public class ScrollLayoutActivity extends Activity{
	private Context mContext;
	private LinearLayout parent;
	private ArrayList<Image> dataSource = new ArrayList<Image>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scrolllayout);
		
		mContext = this;
		
		parent = (LinearLayout) findViewById(R.id.scrollParent);
		
		initUploadDataSource();
		
		createScrollLayout(dataSource);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK && null != data){
			String sdState=Environment.getExternalStorageState();
			   String dstPath = "";
			   String picPath = null;
			   if(!sdState.equals(Environment.MEDIA_MOUNTED)){
				   File dir = mContext.getDir("pic", Context.MODE_PRIVATE);
				   dstPath = dir.getAbsolutePath();
			   }else
			   {
				   dstPath = mContext.getExternalCacheDir() + "/pic";
			   }
			   String name= DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance(Locale.CHINA)) + "";
			   Bitmap bmp = null;
			switch (requestCode) {
			case GridViewHelper.PICTURE:
				Uri selectedImage = data.getData();
			    String[] filePathColumns={MediaStore.Images.Media.DATA,MediaStore.Images.Media.ORIENTATION};
			    Cursor c = this.getContentResolver().query(selectedImage, filePathColumns, null,null, null);
			    c.moveToFirst();
			    int columnIndex = c.getColumnIndex(filePathColumns[0]);
			    String tmpPath = c.getString(columnIndex);
			    String orientation = c.getString(c.getColumnIndex(filePathColumns[1]));// 获取旋转的角度 
			    int angle = 0;  
	            if (orientation != null && !"".equals(orientation)) {  
	                angle = Integer.parseInt(orientation);  
	            }  
			    bmp = ContextUtils.scaleBitmap(tmpPath, 300, 300,angle);
			    
			    if (bmp != null) {
					FileOutputStream fout = null;
					   picPath= dstPath+ File.separator + name;
					   File file = new File(picPath);
					   try {
						FileUtils.createNewFile(file);
					   } catch (Exception e1) {
						e1.printStackTrace();
					   }
					   try {
					    fout = new FileOutputStream(file);
					    if(tmpPath.endsWith("jpg") || tmpPath.endsWith("jpeg"))
					    {
					    	bmp.compress(Bitmap.CompressFormat.JPEG, 100, fout);
					    }else
					    {
					    	bmp.compress(Bitmap.CompressFormat.PNG, 100, fout);
					    }
					    
					   } catch (FileNotFoundException e) {
					    e.printStackTrace();
					   }finally{
					    try {
					     fout.flush();
					     fout.close();
					    } catch (IOException e) {
					     e.printStackTrace();
					    }
					   }
				}
			    c.close();
				break;

			default:
				break;
			}
			
			if(picPath != null && picPath.trim().length() != 0)
			{
				if(bmp != null)
				{
					setDataSource(bmp,name);
					createScrollLayout(dataSource);
				}
			}
			   
		}
	}
	
	public void createScrollLayout(ArrayList<Image> dataSource)
	{
		//删除之后避免View之间的重叠
		parent.removeAllViews();
		
		GridViewHelper gridViewHelper = GridViewHelper.getInstance();
		gridViewHelper.setColumns(3);
		gridViewHelper.setRows(1);
		gridViewHelper.setWhichScreen(getWhichScreen(dataSource.size()));
		gridViewHelper.createGridView(
				mContext, parent, 
				dataSource, 
				ScrollLayoutGridViewAdapter.class);
	}
	
	private void setDataSource(Bitmap bitmap,String key)
	{
		if(dataSource.size() <= 0)
		{
			throw new RuntimeException("datasource need to be init");
		}
		Image image = new Image();
		image.bitmap = bitmap; 
		image.key = key;
		image.type = 0;
		dataSource.add(dataSource.size() - 1,image);
	}
	
	private void initUploadDataSource()
	{
		BitmapDrawable mBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.more_pic);
    	Bitmap mBitmap = mBitmapDrawable.getBitmap();
		Image image = new Image();
		image.bitmap = mBitmap;
		image.type = 1;
		dataSource.add(image);
	}
	
	private int getWhichScreen(int dataSourceSize)
	{
		int pageNum = 3;
		int whichScreen = 0;
		
		if(dataSourceSize % pageNum == 0)
		{
			whichScreen = dataSourceSize / pageNum - 1;
		}else
		{
			if(dataSourceSize % pageNum == 1)
			{
				whichScreen = dataSourceSize / pageNum - 1;
			}else if(dataSourceSize % pageNum == 2)
			{
				whichScreen = dataSourceSize / pageNum;
			}
		}
		
		if(whichScreen < 0)
		{
			whichScreen = 0;
		}
		return whichScreen;
	}

}

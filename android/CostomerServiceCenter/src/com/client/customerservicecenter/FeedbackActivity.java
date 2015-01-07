package com.client.customerservicecenter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.client.customerservicecenter.adapter.ScrollLayoutGridViewAdapter;
import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.hub.SessionServiceHub;
import com.client.customerservicecenter.job.DeleteUploadImgJob;
import com.client.customerservicecenter.job.LoadNetFeedbackDataJob;
import com.client.customerservicecenter.job.LoadSessionStateJob;
import com.client.customerservicecenter.job.LoadUserKeyJob;
import com.client.customerservicecenter.job.SubmitFeedbackJob;
import com.client.customerservicecenter.job.SubmitPictureJob;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.Constants.OnDatasourceChangeListener;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.util.FileUtils;
import com.client.customerservicecenter.util.NetUtils;
import com.client.customerservicecenter.util.Preferences;
import com.client.customerservicecenter.util.bitmap.BitmapUtils;
import com.client.customerservicecenter.util.thread.Future;
import com.client.customerservicecenter.util.thread.FutureListener;
import com.client.customerservicecenter.util.thread.ThreadPool;
import com.client.customerservicecenter.widget.scroll.GridViewHelper;

/**
 * @author jianwen.zhu
 * @since 2014/8/28
 */
public class FeedbackActivity extends BaseActivity implements OnDatasourceChangeListener{
	private LinearLayout parent;
	private RelativeLayout uploadPicParent;
	private TextView feedBackType;
	private EditText editText;
	private EditText phone;
	private EditText email;
	private Button submit;
	
	private String errorType;
	private ProgressDialog mProgressDialog;
	
	private SessionServiceHub mSessionServiceHub;
	
	private ArrayList<Picture> dataSource = new ArrayList<Picture>();
	
	private FutureListener<Boolean> mSubmitFeedbakListener = 
    		new FutureListener<Boolean>() {
		@Override
		public synchronized void onFutureDone(Future<Boolean> count) {
			Message message = mHandler.obtainMessage();
			message.what = Constants.MSG_SUBMIT_COMMENT_FAIL_KEY;
			Bundle data = new Bundle();
			data.putBoolean("submitState", count.get());
			message.setData(data);
			mHandler.sendMessage(message);
		}
	};
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case Constants.MSG_SHOWPROGRESSDIALOG:
				Bundle data = msg.getData();
				int titleResId = data.getInt("title");
				int messageResId = data.getInt("message");
				if(mProgressDialog != null)
				{
					mProgressDialog.setTitle(titleResId);
					mProgressDialog.setMessage(mContext.getResources().getString(messageResId));
					mProgressDialog.show();
				}
				break;
			case Constants.MSG_USER_KEY_FAIL:
				ContextUtils.closeProgressDialog(mProgressDialog);
				ContextUtils.hideKeyboard(mContext, editText);
				ContextUtils.showToast(mContext, R.string.submitFail, Toast.LENGTH_SHORT);
				break;
			case Constants.MSG_SUBMIT_COMMENT_FAIL_KEY:
				ContextUtils.closeProgressDialog(mProgressDialog);
				ContextUtils.hideKeyboard(mContext, editText);
				Bundle stateData = msg.getData();
				boolean state = stateData.getBoolean("submitState");
				if(state)
				{
					editText.setText("");
					if(userInfoMap != null)
					{
						String userKey = (String) userInfoMap.get("userkey");
						AppApplication.threadPool.submit(new LoadNetFeedbackDataJob(mContext,userKey), 
								null, 
								ThreadPool.MODE_NETWORK);
					}
					ContextUtils.showToast(mContext, R.string.submitSuccess, Toast.LENGTH_SHORT);
					finish();
				}else
				{
					ContextUtils.showToast(mContext, R.string.submitFail, Toast.LENGTH_SHORT);
				}
				break;
			case Constants.MSG_USER_KEY_SUCCESS:
				userInfoMap = (Map) msg.obj;
				if(dataSource.size() > 1)
				{
					SubmitPictureJob submitPicture = 
							new SubmitPictureJob(mContext, dataSource, mHandler, userInfoMap);
					AppApplication.threadPool.submit(submitPicture, null, ThreadPool.MODE_NETWORK);
				}else
				{
					String userKey = (String) userInfoMap.get("userkey");
					comment(userKey,false);
				}
				break;
			case Constants.MSG_SUBMIT_PICTURE_SUCCESS:
				String userKey = (String) userInfoMap.get("userkey");
				comment(userKey,true);
				break;
			case Constants.MSG_SUBMIT_PICTURE_FAIL:
				ContextUtils.closeProgressDialog(mProgressDialog);
				ContextUtils.showToast(mContext, R.string.submitFail, Toast.LENGTH_SHORT);
				break;
			case Constants.MSG_DELETE_UPLOAD_IMG:
				Picture p = (Picture) msg.obj;
				dataSource.remove(p);
				createScrollLayout(dataSource);
				break;
			case Constants.MSG_SESSION_VALID:
				AppApplication.threadPool.submit(
						new LoadUserKeyJob(mContext, mHandler,mServiceHub), null, ThreadPool.MODE_CPU);
				break;
			case Constants.MSG_SESSION_INVALID:
				ContextUtils.closeProgressDialog(mProgressDialog);
				Intent userActivity = new Intent();
				userActivity.setAction(Constants.ACTION_LOGIN_USER_ACCESS);
				startActivityForResult(userActivity,Constants.ACTIVITY_CODE_REQUEST_FOR_USERID);
				break;
			default:
				break;
			}
		};
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_feedback);
		
		ActionBar ab = getActionBar();
//        ab.setDisplayHomeAsUpEnabled(true);
//        ab.setHomeButtonEnabled(true);
//        ab.setLogo(new ColorDrawable(Color.TRANSPARENT));
		ab.setCustomView(R.layout.title_bar);
		ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		ab.setDisplayShowCustomEnabled(true);
		
		TextView abTitle = (TextView) ab.getCustomView().findViewById(R.id.bar_title);
		ab.getCustomView().findViewById(R.id.back).setOnClickListener(uiListener);
		
//		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		
		Bundle data = getIntent().getExtras();
		String resourceName = data.getString("operateType");
		errorType = data.getString("errorType");
		
		feedBackType = (TextView) findViewById(R.id.bar_title);
		final int operateTyepResId = getResources().getIdentifier(resourceName,
                "string", this.getPackageName());
		feedBackType.setText(operateTyepResId);
		abTitle.setText(operateTyepResId);
		
		editText = (EditText) findViewById(R.id.description);
		phone = (EditText) findViewById(R.id.phone);
		email = (EditText) findViewById(R.id.email);
		submit = (Button) findViewById(R.id.submit);
		parent = (LinearLayout) findViewById(R.id.scrollParent);
		uploadPicParent = (RelativeLayout) findViewById(R.id.uploadPicParent);
		LinearLayout.LayoutParams picParentParams = 
				new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, getPicParentHeight());
		picParentParams.topMargin = 20;
		uploadPicParent.setLayoutParams(picParentParams);
		
//		uploadPicParent.setVisibility(View.GONE);
		
		submit.setOnClickListener(uiListener);
		findViewById(R.id.back).setOnClickListener(uiListener);
		
		mProgressDialog = ContextUtils.createProgressDialog(mContext);
		
		String phoneNumber = data.getString("mobile");
		if(phoneNumber != null && phoneNumber.trim().length() != 0)
		{
			phone.setText(phoneNumber);
		}
		String emailStr = data.getString("email");
		if(emailStr != null && emailStr.trim().length() != 0)
		{
			email.setText(emailStr);
		}
		
		mSessionServiceHub = new SessionServiceHub();
		
		initUploadDataSource();
		
		createScrollLayout(dataSource);
	}
	
	private OnClickListener uiListener = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			switch (arg0.getId()) {
			case R.id.back:
				finish();
				break;
			case R.id.submit:
				if(!NetUtils.isNetWorkAvailable(mContext, null))
				{
					ContextUtils.showToast(mContext, R.string.toast_net_error, Toast.LENGTH_SHORT);
					return;
				}
				
				if (!checkCommentValid()) {
					return;
				}
				Message message = mHandler.obtainMessage();
				Bundle data = new Bundle();
				message.what = Constants.MSG_SHOWPROGRESSDIALOG;
				data.putInt("title", R.string.comment);
				data.putInt("message", R.string.submitComment);
				message.setData(data);
				mHandler.sendMessage(message);
				
				submitComment();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void submitComment()
	{
		AppApplication.threadPool.submit(
				new LoadSessionStateJob(mContext, mHandler, mSessionServiceHub),
				null, ThreadPool.MODE_CPU);
	}
	
	private void comment(String userKey,boolean uploadPic)
	{
		if (!checkCommentValid()) {
			return;
		}
		String comment = editText.getEditableText().toString();
		String phoneStr = phone.getEditableText().toString();
		String emailStr = email.getEditableText().toString();
		
		if(emailStr != null && emailStr.trim().length() != 0)
		{
			email.setText(emailStr);
		}
		
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = telephonyManager.getDeviceId();
		if(imei == null)
		{
			imei = "";
		}
		
		final CommentInfo commentInfo = new CommentInfo();
		commentInfo.setComment(comment);
		commentInfo.setImei(imei);
		commentInfo.setVersion(ContextUtils.getInnerVersion());
		commentInfo.setCustomVersion(ContextUtils.getCustomVersion());
		commentInfo.setModel(ContextUtils.getPhoneModel());
		commentInfo.setType(errorType);
		commentInfo.setArg0("NULL");
		commentInfo.setArg1("NULL");
		commentInfo.setArg2("NULL");
		commentInfo.setUserId(userKey);
		
		if(uploadPic)
		{
			commentInfo.setPicture(dataSource);
		}
		
		if(phoneStr != null && phoneStr.trim().length() != 0)
		{
			Preferences.setPhoneNumber(AppApplication.mPrefs, phoneStr);
			commentInfo.setPhone(phoneStr);
		}
		
		if(emailStr != null && emailStr.trim().length() != 0)
		{
			Preferences.setEmail(AppApplication.mPrefs, emailStr);
			commentInfo.setEmail(emailStr);
		}
		
		SubmitFeedbackJob submitFeedbackJob = new SubmitFeedbackJob(commentInfo);
		AppApplication.threadPool.submit(submitFeedbackJob, mSubmitFeedbakListener, ThreadPool.MODE_NETWORK);
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
				   dstPath = mContext.getExternalCacheDir() + Constants.DIR_UPLOAD_PIC_CACHE;
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
			    bmp = ContextUtils.scaleBitmap(tmpPath, 
			    		ContextUtils.dip2px(mContext, Constants.SIZE_UPLOAD_PIC_WEIGHT), 
			    		ContextUtils.dip2px(mContext, Constants.SIZE_UPLOAD_PIC_HEIGHT),angle);
			    
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
					setDataSource(BitmapUtils.resizeAndCropCenter(
							bmp, 
							ContextUtils.dip2px(mContext, Constants.SIZE_SHOW_THUMBNAIL_WEIGHT), 
							ContextUtils.dip2px(mContext, Constants.SIZE_SHOW_THUMBNAIL_WEIGHT), true),name,picPath);
					createScrollLayout(dataSource);
				}
			}
			   
		}
	}
	
	public void createScrollLayout(ArrayList<Picture> dataSource)
	{
		//删除之后避免View之间的重叠
		parent.removeAllViews();
		
		GridViewHelper gridViewHelper = GridViewHelper.getInstance();
		gridViewHelper.setColumns(3);
		gridViewHelper.setRows(1);
		//getWhichScreen(dataSource.size())
		gridViewHelper.setWhichScreen(0);
		gridViewHelper.setDatasourceChangeListener(this);
		gridViewHelper.createGridView(
				mContext, parent, 
				dataSource, 
				ScrollLayoutGridViewAdapter.class);
	}
	
	private void setDataSource(Bitmap bitmap,String key,String path)
	{
		if(dataSource.size() <= 0)
		{
			throw new RuntimeException("datasource need to be init");
		}
		Picture image = new Picture();
		image.bitmap = bitmap; 
		image.key = key;
		image.type = 0;
		image.path = path;
		//dataSource.size() - 1
		dataSource.add(1,image);
	}
	
	private void initUploadDataSource()
	{
		dataSource.clear();
		BitmapDrawable mBitmapDrawable = (BitmapDrawable) getResources().getDrawable(R.drawable.more_pic);
    	Bitmap mBitmap = mBitmapDrawable.getBitmap();
		Picture image = new Picture();
		image.bitmap = mBitmap;
		image.type = 1;
		dataSource.add(image);
		
	}
	
	/**
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
	}*/
	
	private int getPicParentHeight()
	{
		int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		return (int) (screenWidth/3 * Constants.THRESHOLD_GRID_ITEM + 10);
	}
	
	private boolean checkCommentValid()
	{
		String comment = editText.getEditableText().toString();
		if(comment == null || "".equals(comment))
		{
			ContextUtils.showToast(mContext, R.string.commentHint, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mProgressDialog = null;
	}

	@Override
	public void onDatasourceChange(Picture p) {
		AppApplication.threadPool.submit(
				new DeleteUploadImgJob(mContext, mHandler, p), null, ThreadPool.MODE_CPU);
	}

}

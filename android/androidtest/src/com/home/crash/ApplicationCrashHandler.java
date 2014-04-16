package com.home.crash;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;

/**
 * 捕获程序的运行时异常(没做处理的，包括在线程里面抛出的)
 * @author Oliverzhu
 * 2012/8/24
 */
public class ApplicationCrashHandler implements UncaughtExceptionHandler {
	private Context context;
	
	public ApplicationCrashHandler(Context context)
	{
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable e) {
		
		//！！！！一定要放在线程里面构造Dialog
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				
		        builder.setTitle("提示");
				
		        builder.setMessage("程序崩溃了 >_<");
				builder.setPositiveButton("退出", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						android.os.Process.killProcess(android.os.Process.myPid());
					}
				});
				
				builder.setNegativeButton("报告错误",new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				
				builder.create().show();
				
				Looper.loop();
			}
		}.start();

		e.printStackTrace();
	}

}

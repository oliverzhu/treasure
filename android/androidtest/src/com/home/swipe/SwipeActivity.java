package com.home.swipe;

import java.util.ArrayList;

import com.home.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;

public class SwipeActivity extends Activity {
	private Context mContext;
	private ScrollView dismissableContainer;
	private ArrayList<Teacher> teachers;
	
	private static final int MSG_DATASOURCE = 1;
	
	private Handler mHanler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case MSG_DATASOURCE:
				FeedbackLayout feedbackLayout = new FeedbackLayout(mContext, teachers);
				dismissableContainer.addView(feedbackLayout,
						new LayoutParams(LayoutParams.MATCH_PARENT,
								LayoutParams.WRAP_CONTENT));
				break;

			default:
				break;
			}
		};
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_swipe);
		mContext = this;
		dismissableContainer = (ScrollView) findViewById(R.id.dismissable_container);
		
		new Thread()
		{
			public void run() 
			{
				initDataSource();
				
				Message msg = mHanler.obtainMessage();
				msg.what = MSG_DATASOURCE;
				mHanler.sendMessage(msg);
			};
		}.start();
	}
	
	private void initDataSource()
	{
		teachers = new ArrayList<Teacher>();
		for(int i = 0;i < 10;i++)
		{
			ArrayList<Student> students = new ArrayList<Student>();
			Teacher teacher = new Teacher();
			teacher.name = "teacher" + i;
			for(int j = 0;j < 5;j++)
			{
				Student stu = new Student();
				stu.name = "student" + j;
				students.add(stu);
			}
			teacher.students = students;
			teachers.add(teacher);
		}
	}

}

package com.home.swipe;

import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.R;

public class FeedbackDetailLayout extends LinearLayout {
	private Context mContext;
	private ArrayList<Student> mDataSource;
	public FeedbackDetailLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FeedbackDetailLayout(Context context, ArrayList<Student> datasource) {
		super(context);
		this.mContext = context;
		this.mDataSource = datasource;
		init();
	}
	
	private void init()
	{
		if(mDataSource == null || mDataSource.size() == 0) {
			return;
		}
		setGravity(Gravity.CENTER_HORIZONTAL);
		setOrientation(LinearLayout.VERTICAL);
		for(int i = 0;i < mDataSource.size();i++)
		{
			Student student =  mDataSource.get(i);
			
			String name = student.name;
			
			final RelativeLayout itemLayoutParent = 
					(RelativeLayout) View.inflate(mContext, R.layout.item_feedback_detail, null);
			
			TextView tv = (TextView) itemLayoutParent.findViewById(R.id.tv);
			tv.setText(name);
			
			addView(itemLayoutParent,
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		}
	}

}

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

public class FeedbackLayout extends LinearLayout {
	private Context mContext;
	private ArrayList<Teacher> mDataSource;
	public FeedbackLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FeedbackLayout(Context context, ArrayList<Teacher> datasource) {
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
			Teacher teacher =  mDataSource.get(i);
			
			String name = teacher.name;
			
			final RelativeLayout itemLayoutParent = 
					(RelativeLayout) View.inflate(mContext, R.layout.item_feedback, null);
			
			LinearLayout feedbackDetailContainer = 
					(LinearLayout) itemLayoutParent.findViewById(R.id.feedbackDetailContainer);
			final RelativeLayout feedbackContainer = 
					(RelativeLayout) itemLayoutParent.findViewById(R.id.feedbackContainer);
			
			TextView tv = (TextView) feedbackContainer.findViewById(R.id.tv);
			tv.setText(name);
			
			
			
			final FeedbackDetailLayout detailLayout = new FeedbackDetailLayout(mContext, teacher.students);
			detailLayout.setVisibility(View.GONE);
			feedbackDetailContainer.addView(detailLayout);
			
			feedbackContainer.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(detailLayout.getVisibility() == View.GONE)
					{
						detailLayout.setVisibility(View.VISIBLE);
					}else if(detailLayout.getVisibility() == View.VISIBLE)
					{
						detailLayout.setVisibility(View.GONE);
					}
				}
			});
			
			// Create a generic swipe-to-dismiss touch listener.
			feedbackContainer.setOnTouchListener(new SwipeDismissTouchListener(
					feedbackContainer,
                    null,
                    new SwipeDismissTouchListener.OnDismissCallback() {
                        @Override
                        public void onDismiss(View view, Object token) {
                            removeView(itemLayoutParent);
                        }
                    }));
			
			addView(itemLayoutParent,
					new LayoutParams(LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
		}	
	}

}

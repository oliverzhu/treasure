package com.client.customerservicecenter.view;

import com.client.customerservicecenter.adapter.HSVAdapter;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class HSVLayout extends LinearLayout {
	private HSVAdapter adapter;  
    private Context context;  
  
    public HSVLayout(Context context, AttributeSet attrs) {  
        super(context, attrs);  
        this.context = context;  
    }
    
    public void setAdapter(HSVAdapter adapter) {  
        this.adapter = adapter;
        removeAllViews();
    	for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, null);  
            view.setPadding(0, 0, 10, 0);  
            this.setOrientation(HORIZONTAL);
            this.addView(view, new LinearLayout.LayoutParams(
            		ContextUtils.dip2px(context, Constants.SIZE_SHOW_THUMBNAIL_WEIGHT), 
            		ContextUtils.dip2px(context, Constants.SIZE_SHOW_THUMBNAIL_HEIGHT)));
        } 
    }
    
    public HSVAdapter getAdapter()
    {
    	return adapter;
    }

}

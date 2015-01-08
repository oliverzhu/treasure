package com.ape.cloudfile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class CloudListView extends ListView
{
    private boolean mIsInMeasure = false;

    public CloudListView(Context context)
    {
        super(context);
    }
    
    public CloudListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    
    public CloudListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public boolean isInMeasure()
    {
        return mIsInMeasure;
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        mIsInMeasure = true;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mIsInMeasure = false;
    }
}

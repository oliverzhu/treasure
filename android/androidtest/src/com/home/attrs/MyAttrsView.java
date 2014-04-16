package com.home.attrs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.home.R;

/**
 * 自定义样式组件
 * @author Oliverzhu
 * 在布局文件中一定要加上命名空间  xmlns:test="http://schemas.android.com/apk/res/com.home"  才能使用test:textSize
 *
 */
public class MyAttrsView extends View {
	private Paint mPaint;     
	private Context mContext;     
	private static final String mString = "Welcome to Mr Wei's blog"; 

	public MyAttrsView(Context context) {     
	    super(context);     
	    mPaint = new Paint();     
	}  
	public MyAttrsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaint = new Paint();     
        
        TypedArray a = context.obtainStyledAttributes(attrs,     
                R.styleable.PullToRefresh);
             
        int textColor = a.getColor(R.styleable.PullToRefresh_textColor,     
                0XFFFFFFFF);     
        float textSize = a.getDimension(R.styleable.PullToRefresh_textSize, 36);
             
        mPaint.setTextSize(textSize);
        mPaint.setColor(textColor);
             
        a.recycle();     
	}
	
	@Override    
    protected void onDraw(Canvas canvas) {     
        // TODO Auto-generated method stub     
        super.onDraw(canvas);     
        //设置填充     
        mPaint.setStyle(Style.FILL);     
             
        //画一个矩形,前俩个是矩形左上角坐标，后面俩个是右下角坐标     
        canvas.drawRect(new Rect(10, 10, 100, 100), mPaint);
             
        mPaint.setColor(Color.BLUE);     
        //绘制文字     
        canvas.drawText(mString, 10, 110, mPaint);
    } 

}

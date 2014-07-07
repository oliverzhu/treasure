package com.home.density;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

//自定义View对象
public class MyView extends View{

	private Paint paint  = new Paint() ;
	
	public MyView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	public MyView(Context context , AttributeSet attrs){
		super(context,attrs);
	}
	
	//存在canvas对象，即存在默认的显示区域
	//此时传进来的Canvas是经过平移后的Canvas  -------------------->?
	@Override
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		//加粗
		paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		paint.setColor(Color.RED);
		canvas.drawColor(Color.BLUE) ;
		
		//此时绘制的坐标是是相对于画布（0,0）点的坐标
		canvas.drawRect(0, 0, 30, 30, paint);
		canvas.drawText("MyView", 10, 40, paint);
	}
}

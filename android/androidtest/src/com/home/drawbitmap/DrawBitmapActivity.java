package com.home.drawbitmap;

import java.lang.ref.SoftReference;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.home.R;

/**
 * 通过Canvas绘制自己的bitmap
 * @author Oliverzhu
 * 2013/3/4
 */
public class DrawBitmapActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_drawbitmap);
		
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 1;
		Bitmap b = createBigBmps(BitmapFactory.decodeResource(getResources(),R.drawable.icon),
				BitmapFactory.decodeResource(getResources(),R.drawable.m_paper_frame_bg));
		
		SoftReference<Bitmap> bitmap = new SoftReference<Bitmap>(b);
		Drawable drawable = new BitmapDrawable(bitmap.get());
		
		ImageView ig = (ImageView) findViewById(R.id.imageView);
		ig.setBackgroundDrawable(drawable);
	}
	
	 /**
     * 画大图
     */
    public Bitmap createBigBmps(Bitmap srcBmp,Bitmap srcBgBmp) {
    	//原图和倒影图的间隙
    	int gap = 4;
    	
      //取得原图的宽度和高度
      int srcWidth = srcBmp.getWidth();
      int srcHeight = srcBmp.getHeight();
      
      int bgWidth = srcBgBmp.getWidth();
      int bgHeight = srcBgBmp.getHeight();
      
      float scaleWidth = ((float)(srcWidth + 4) /  bgWidth);
      float scaleHeight = ((float) (srcHeight + 4) / bgHeight); 
      
      //创建大图
      Bitmap bigBmp = Bitmap.createBitmap(srcWidth + 4, srcHeight + 4, Bitmap.Config.ARGB_8888);
      
      
   // 取得想要缩放的matrix参数
      Matrix matrix = new Matrix();
      matrix.postScale(scaleWidth, scaleHeight);
    //创建背景图
      Bitmap bgBmp = Bitmap.createBitmap(srcBgBmp, 0, 0, bgWidth, bgHeight, matrix, false);
      
      //实例化画板
      Canvas canvas = new Canvas(bigBmp);
      
      /********************* 画背景图像*****************************/
      canvas.drawBitmap(bgBmp, 0, 0, null);
      
      /********************* 画原始图像*****************************/
      canvas.drawBitmap(srcBmp, 2, 2, null);
      
      srcBmp.recycle();
//      System.gc();
      return bigBmp;
      
    }

}

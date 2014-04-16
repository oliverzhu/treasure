package com.home.anim.picturemove;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout.LayoutParams;

import com.home.R;

public class PictureMoveActivity extends Activity{
	private static final String TAG = "PictureDemoActivity";
	
//	private static final int FLING_MIN_DISTANCE = 140;
//    private static final int FLING_MIN_VELOCITY = 100;
	private ViewGroup targetParent;
	
	private GestureDetector mGestureDetector;
	
	private int indexOfPage = 0;
	
	/** 屏幕宽度 */
	private int animFromXDelta = 480;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_move);
        
        targetParent = (ViewGroup) findViewById(R.id.img_parent);
        
        
        AnimationImageView target_t_car = new AnimationImageView(this);
//        target_t_car.setLayoutParams(new LayoutParams(80, 100));
        target_t_car.setTag("汽车");
        
        AnimationImageView target_t_food = new AnimationImageView(this);
//        target_t_food.setLayoutParams(new LayoutParams(120, 120));
        target_t_food.setTag("美食");
        
        AnimationImageView target_t_home = new AnimationImageView(this);
//        target_t_home.setLayoutParams(new LayoutParams(180, 100));
        target_t_home.setTag("家居");
        
        ArrayList<AnimationImageView> first_page = new ArrayList<AnimationImageView>();
        
        first_page.add(target_t_home);
        first_page.add(target_t_food);
        first_page.add(target_t_car); 
        
        
        
        Drawable t_car = getImageFromAssetFile(this,"t_car.png");
        
        Drawable t_food = getImageFromAssetFile(this,"t_food.png");
        
        Drawable t_home = getImageFromAssetFile(this,"t_home.png");
        
        SoftReference<Drawable> t_home_ref = new SoftReference<Drawable>(t_home);
        
		Drawable t_home_image = t_home_ref.get();
		target_t_home.setBackgroundDrawable(t_home_image);
		
		SoftReference<Drawable> t_food_ref = new SoftReference<Drawable>(t_food);
		
		Drawable t_food_image = t_food_ref.get();
		target_t_food.setBackgroundDrawable(t_food_image);
		
		
		SoftReference<Drawable> t_car_ref = new SoftReference<Drawable>(t_car);
		
		Drawable t_car_image = t_car_ref.get();
		target_t_car.setBackgroundDrawable(t_car_image);
		
		
		Animation t_car_animation = new TranslateAnimation(animFromXDelta,
        		0.0f, 
        		0.0f, 
        		0.0f);
		//设置动画开始时机
		t_car_animation.setStartOffset(500);
		
		target_t_car.setEnterAnimation(t_car_animation);
		
		target_t_car.startAnimation(target_t_car.getEnterAnimation());
		
		Animation t_food_animation = new TranslateAnimation(animFromXDelta,
        		0.0f, 
        		0.0f, 
        		0.0f);
		t_food_animation.setStartOffset(600);
		
		target_t_food.setEnterAnimation(t_food_animation);
		
		target_t_food.startAnimation(target_t_food.getEnterAnimation());
		
		Animation t_home_animation = new TranslateAnimation(animFromXDelta,
        		0.0f, 
        		0.0f, 
        		0.0f);
		t_home_animation.setStartOffset(800);
		
		target_t_home.setEnterAnimation(t_home_animation);
		
		target_t_home.startAnimation(target_t_home.getEnterAnimation());
		
		android.widget.AbsoluteLayout.LayoutParams 
			target_t_food_params = new LayoutParams(120, 120, 80, 240);
		
		android.widget.AbsoluteLayout.LayoutParams 
		target_t_car_params = new LayoutParams(80, 100, 20, 200);
		
		android.widget.AbsoluteLayout.LayoutParams 
		target_t_home_params = new LayoutParams(180, 100, 80, 140);
		
		targetParent.addView(target_t_food,target_t_food_params);
		
		targetParent.addView(target_t_car,target_t_car_params);
		
		targetParent.addView(target_t_home,target_t_home_params);
		
    }
    
    /**
	 * 从assets中读取图片
	 * @param context
	 * @param fileName
	 * @return
	 */
	 public static  Drawable  getImageFromAssetFile(Context context,String fileName){
		 Drawable image = null;   
		 try{   
			AssetManager am = context.getAssets();   
		    InputStream is = am.open(fileName);   
		    image = new BitmapDrawable(is); 
		    is.close();   
		 }catch(Exception e){   
		       e.printStackTrace();
		 }   
		 return image;
	 }
}
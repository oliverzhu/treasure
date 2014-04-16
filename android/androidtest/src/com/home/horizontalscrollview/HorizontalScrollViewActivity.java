package com.home.horizontalscrollview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.home.R;

/**
 * view水平滑动动作效果
 * @author Oliverzhu
 *
 */
public class HorizontalScrollViewActivity extends Activity{
	private Context context;
	private int screen_width;
	private int screen_height;
	
	private RelativeLayout topBar;
	
	private RelativeLayout titleContainer;
	
	private HorizontalScrollView hsrollView;
	
	private LinearLayout content;
	
	//view动画布局的物理起始坐标
	private static int firstLeft;
		
	//做完动画view所停留的坐标
	private static int startLeft; 
	
	private String[] titles = new String[]{"汽车","国内","美女","文学","国际","傻逼","2货"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.horizontalscrollview);
		
		context = this;
		
		screen_width = getWindowManager().getDefaultDisplay().getWidth();
		screen_height = getWindowManager().getDefaultDisplay().getHeight();
		
		topBar = (RelativeLayout) findViewById(R.id.topbar);
		
		titleContainer = (RelativeLayout) topBar.findViewById(R.id.title_container);
		
		hsrollView = (HorizontalScrollView) findViewById(R.id.scrollView);
		
		content = (LinearLayout) findViewById(R.id.content);
		
		//底部内容布局参数
		RelativeLayout.LayoutParams contentParams = 
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, 
						(int)(screen_height * 0.8));
		contentParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		content.setLayoutParams(contentParams);
		
		//------->ImageView:left_guide
		ImageView left_guide = (ImageView) topBar.findViewById(R.id.left_guide);
		
		//左边导航内容布局参数
		RelativeLayout.LayoutParams leftGuideParams = 
				new RelativeLayout.LayoutParams(
						(int)(screen_width * 0.05), 
						RelativeLayout.LayoutParams.FILL_PARENT);
		leftGuideParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		left_guide.setLayoutParams(leftGuideParams);
		
		//------->ImageView:right_guide
		ImageView right_guide = (ImageView) topBar.findViewById(R.id.right_guide);
		
		//右边导航内容布局参数
		RelativeLayout.LayoutParams rightGuideParams = 
				new RelativeLayout.LayoutParams(
						(int)(screen_width * 0.05), 
						RelativeLayout.LayoutParams.FILL_PARENT);
		rightGuideParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		right_guide.setLayoutParams(rightGuideParams);
		
		//用于动画效果的view
		final RelativeLayout animViewParent = new RelativeLayout(this);
		
		animViewParent.setTag("animview");
		
		RelativeLayout.LayoutParams animViewParentParams = 
				new RelativeLayout.LayoutParams(
						(int)(screen_width * 0.15), 
						RelativeLayout.LayoutParams.FILL_PARENT);
		animViewParent.setGravity(Gravity.CENTER);
		animViewParentParams.leftMargin = 0;
		
		animViewParent.setLayoutParams(animViewParentParams);
		
		ImageView animView = new ImageView(this);
		
		animView.setImageResource(R.drawable.slidebar);
		
		animViewParent.addView(animView);
		
		titleContainer.addView(animViewParent);
		
		//添加标题分类item
		for(int i = 0;i < titles.length;i++)
		{
			final String text = titles[i];
			final RelativeLayout titleItem = new RelativeLayout(this);
			
			titleItem.setTag(text);
			RelativeLayout.LayoutParams titleItemParams = 
					new RelativeLayout.LayoutParams(
							(int)(screen_width * 0.15), 
							RelativeLayout.LayoutParams.FILL_PARENT);
			titleItem.setGravity(Gravity.CENTER);
			titleItemParams.leftMargin = i * (int)(screen_width * 0.15);
			
			titleItem.setLayoutParams(titleItemParams);
			
			TextView item = new TextView(this);
			item.setText(text);
			
			titleItem.addView(item);
			
			titleContainer.addView(titleItem);
			
			firstLeft = (int)(screen_width * 0.05);
			
			titleItem.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					RelativeLayout oldAnimViewParent = 
							(RelativeLayout) titleContainer.findViewWithTag("animview");
					if(oldAnimViewParent != null)
					{
						titleContainer.removeView(oldAnimViewParent);
					}
					
					//用于动画效果的view
					RelativeLayout newAnimViewParent = new RelativeLayout(context);
					
					newAnimViewParent.setTag("animview");
					
					RelativeLayout.LayoutParams newAnimViewParentParams = 
							new RelativeLayout.LayoutParams(
									(int)(screen_width * 0.15), 
									RelativeLayout.LayoutParams.FILL_PARENT);
					newAnimViewParent.setGravity(Gravity.CENTER);
					newAnimViewParentParams.leftMargin = 0;
					
					newAnimViewParent.setLayoutParams(newAnimViewParentParams);
					
					ImageView newanimView = new ImageView(context);
					
					newanimView.setImageResource(R.drawable.slidebar);
					
					newAnimViewParent.addView(newanimView);
					
					TextView newTextitem = new TextView(context);
					newTextitem.setText(titleItem.getTag().toString());
					newTextitem.setTextColor(Color.WHITE);
					
					
					RelativeLayout.LayoutParams newTextitemParams = 
							new RelativeLayout.LayoutParams(
									RelativeLayout.LayoutParams.WRAP_CONTENT, 
									RelativeLayout.LayoutParams.WRAP_CONTENT);
					newTextitemParams.addRule(RelativeLayout.CENTER_IN_PARENT);
					
					newTextitem.setLayoutParams(newTextitemParams);
					newAnimViewParent.addView(newTextitem);
					
					titleContainer.addView(newAnimViewParent);
					
					//此时按下这个item的坐标
					final int endLeft = titleItem.getLeft() + 
							((View)titleItem.getParent()).getLeft() + 
							((View)titleItem.getParent().getParent()).getLeft();
					//执行动画效果
					//4个参数的参照坐标都是原始这个View在页面布局的物理位置
					//view在做动画效果的时候真正布局上的物理没有改变
					
					
					TranslateAnimation animation = new TranslateAnimation(
							startLeft, endLeft - firstLeft, 0, 0);
					
					animation.setAnimationListener(new AnimationListener() {
						
						@Override
						public void onAnimationStart(Animation animation) {
						}
						
						@Override
						public void onAnimationRepeat(Animation animation) {
						}
						
						@Override
						public void onAnimationEnd(Animation animation) {
							startLeft = endLeft - firstLeft;
						}
					});
					animation.setDuration(100);
					animation.setFillAfter(true);
					//animViewParent.bringToFront();
					newAnimViewParent.startAnimation(animation);
				}
			});
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		finish();
		System.exit(0);
		return true;
	}
	
}

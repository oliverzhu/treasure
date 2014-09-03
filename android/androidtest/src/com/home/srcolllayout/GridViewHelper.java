package com.home.srcolllayout;

import java.lang.reflect.Constructor;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout.LayoutParams;

import com.home.R;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/9/2
 */
public class GridViewHelper {
	private Context context;
	
	/** 一个grid view上的图片数 */
	private int icons;
	
	/** grid数 */
	private int num;
	
	/** 列数 */
	private int columns;
	
	/** 行数 */
	private int rows;
	
	/** 默认停留在第一屏 */
	public int whichScreen = 0;
	
	private static final float GRID_ITEM_THRESHOLD = 0.9f;
	private static final float GRID_ITEM_VERTICAL_SPACING_THRESHOLD = 0.021f;
	private static final float GRID_ITEM_HORIZONTAL_SPACING_THRESHOLD = 0.0f;
	
	public static final int PICTURE = 1;

	/** 设置列数 */
	public void setColumns(int columns) {
		this.columns = columns;
	}

	/** 设置行数 */
	public void setRows(int rows) {
		this.rows = rows;
	}
	
	/** 设置当前默认显示到第几屏幕 */
	public void setWhichScreen(int whichScreen)
	{
		this.whichScreen = whichScreen;
	}

	private GridViewHelper() {
	}

	private static GridViewHelper instance = new GridViewHelper();

	public static GridViewHelper getInstance() {
		return instance;
	}
	
	public void createGridView(final Context context,
			ViewGroup group,List datas,Class clazz)
	{
		this.context = context;
		int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
		int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
		int itemWidth = (int) (screenWidth/columns * GRID_ITEM_THRESHOLD);
		int itemHeight = itemWidth;
		//一个view上的项数
		icons = rows * columns;	
		if(datas.size() % icons != 0) {
			num = datas.size() / icons + 1;
		} else {
			num = datas.size() / icons;
		}
		group.removeAllViews();
		
		ScrollLayout layout = new ScrollLayout(context);
		
		group.addView(layout, 
				new LayoutParams(
						ScrollLayout.LayoutParams.MATCH_PARENT, 
						ScrollLayout.LayoutParams.MATCH_PARENT));
		
		for(int i = 0;i<num;i++) {
			GridView item = new GridView(context);
			item.setGravity(Gravity.CENTER);
			item.setNumColumns(columns);
			item.setColumnWidth(itemWidth / columns);
			item.setVerticalScrollBarEnabled(false);
			item.setSelector(new ColorDrawable(Color.TRANSPARENT));
			layout.addView(item, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		
		
		int childCount = layout.getChildCount();
		for(int i = 0;i<childCount;i++) {
			GridView item = (GridView) layout.getChildAt(i);
			item.setVerticalSpacing((int) (screenWidth * GRID_ITEM_VERTICAL_SPACING_THRESHOLD));
			item.setHorizontalSpacing((int) (screenHeight * GRID_ITEM_HORIZONTAL_SPACING_THRESHOLD));
			int a = i*icons;
			int b = (i+1)*icons;
			if(b >= datas.size()) {
				b = datas.size();
			}
			List data = datas.subList(a, b);
			ScrollLayoutGridViewAdapter adapter = null;
			try {
				//zjw 获取实例
				Class[] types = new Class[]{Context.class,int.class};
				
				Object[] values = new Object[]{context,R.layout.item_scroll_grid};
				Constructor constructor = clazz.getDeclaredConstructor(types);
				constructor.setAccessible(true);
				adapter = (ScrollLayoutGridViewAdapter) constructor.newInstance(values);
				adapter.setItemWeight(itemWidth);
				adapter.setItemHeight(itemHeight);
				adapter.setDatas(data);
				adapter.setView(item);
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			item.setAdapter(adapter);
			
			//zjw 让屏幕显示出来默认停留在哪个屏幕
			((ScrollLayout)layout).setToScreen(whichScreen);
			
			//zjw 设置代理
			//((ScrollLayout)layout).setDelegate(srollDelegate);
		}
		
	}
	
	
}

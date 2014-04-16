package com.home.turnpage;

import com.home.turnpage.PageContainer.IPageContainer;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;

public class TurnPageActivity extends Activity{
	Button button1;
	Button button2;
	Button button3;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		button1 = new Button(TurnPageActivity.this);
		button2 = new Button(TurnPageActivity.this);
		button3 = new Button(TurnPageActivity.this);
		MagicBookView b = new MagicBookView(this);
		b.initBookView(50, 0, new IPageContainer() {
			
			@Override
			public void onTurnReload(boolean isTurnBack, int currentPage,
					int needReloadPage, PageContainer container) {
				button1.setText("" + needReloadPage);
			}
			
			@Override
			public void onSetPage(int page, PageContainer container) {
				
			}
			
			@Override
			public void onInit(int page, PageContainer container) {
				button1.setText("" + page);
				button1.setTextSize(20);
				
				container.setBackgroundColor(Color.WHITE);
				
				container.setContent(button1, 
						new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
			}
		}, new IPageContainer() {
			
			@Override
			public void onTurnReload(boolean isTurnBack, int currentPage,
					int needReloadPage, PageContainer container) {
				button2.setText("" + needReloadPage);
			}
			
			@Override
			public void onSetPage(int page, PageContainer container) {
				
			}
			
			@Override
			public void onInit(int page, PageContainer container) {
				button2.setText("" + page);
				button2.setTextSize(20);
				
				container.setBackgroundColor(Color.WHITE);
				container.setContent(button2, 
						new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
			}
		}, new IPageContainer() {
			
			@Override
			public void onTurnReload(boolean isTurnBack, int currentPage,
					int needReloadPage, PageContainer container) {
				button3.setText("" + needReloadPage);
			}
			
			@Override
			public void onSetPage(int page, PageContainer container) {
				
			}
			
			@Override
			public void onInit(int page, PageContainer container) {
				button3.setText("" + page);
				button3.setTextSize(20);
				container.setBackgroundColor(Color.WHITE);
				container.setContent(button3, 
						new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER));
			}
		});
		setContentView(b);
	}

}

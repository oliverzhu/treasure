/***********************************************************************
* Copyright (C) 2007, TINNO Corporation.
* Project Name: S9201_PK
* File Name: PageContainer
* Description: the container of one page for book turn effect 
* Author: jia.liu
* Date: 2013-04-28
* Major change history:
**********************************************************************/
package com.home.turnpage;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

public class PageContainer extends FrameLayout {
	private View mContent;
	private IPageContainer mIPageContainter;
	private int mPageInBook = -1;
	private int mPageCount = -1;
	public PageContainer(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public void setIPageContainter(IPageContainer ipc){
		mIPageContainter = ipc;
	}
	
    public IPageContainer getIPageContainter(){
    	return mIPageContainter;
	}
	public interface IPageContainer{
		public abstract void onInit(int page,PageContainer container);
		public abstract void onTurnReload(boolean isTurnBack,int currentPage,int needReloadPage,PageContainer container);
		public abstract void onSetPage(int page,PageContainer container);
	}
	void doTurnReload(boolean isTurnBack,int currentPage){
		if(mIPageContainter == null){
			if(MagicBookView.DEBUG)Log.e(MagicBookView.TAG,"PageContainter IPageContainter is null");
			return;
		}else if(mPageInBook < -1){
			if(MagicBookView.DEBUG)Log.i(MagicBookView.TAG,"PageContainter PageInBook is -1 will not reload");
			return;		
		}else if(mPageInBook >= mPageCount){
			if(MagicBookView.DEBUG)Log.i(MagicBookView.TAG,"PageContainter PageInBook >= PageCount will not reload");
			return;
		}else{
			mIPageContainter.onTurnReload(isTurnBack,currentPage,mPageInBook,this);
		}
	}
	public void doInit(){
		if(mIPageContainter == null){
			if(MagicBookView.DEBUG)Log.e(MagicBookView.TAG,"PageContainter IPageContainter is null");
			return;
		}else if(mPageInBook < 0){
			if(MagicBookView.DEBUG)Log.i(MagicBookView.TAG,"PageContainter PageInBook is -1 will not reload");
			return;		
		}else{
			mIPageContainter.onInit(mPageInBook,this);
		}
	}
	public void doReInit(){
//		removeAllViews();
		if(mIPageContainter == null){
			if(MagicBookView.DEBUG)Log.e(MagicBookView.TAG,"PageContainter IPageContainter is null");
			return;
		}else if(mPageInBook < 0){
			if(MagicBookView.DEBUG)Log.i(MagicBookView.TAG,"PageContainter PageInBook is -1 will not reload");
			return;		
		}else{
			mIPageContainter.onSetPage(mPageInBook,this);
		}
	}
	void setCurPageInBook(int page){
		if(page < -1){
			return;
		}else{
			mPageInBook = page;
		}
	}
	void setPageCount(int count){
		if(count < 0){
			return;
		}else{
			mPageCount = count;
		}
	}
	public int getCurPageInBook(){
		return mPageInBook;
	}
	public void setContent(View content,LayoutParams lp){
		mContent = content;
		if(mContent != null){
			addView(content, lp);
		}
	}
	public View getContent(){
		return mContent;
	} 
}

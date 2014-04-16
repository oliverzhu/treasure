/***********************************************************************
* Copyright (C) 2007, TINNO Corporation.
* Project Name: S9201_PK
* File Name: MagicBookView
* Description: the view to show book turn effect  
* Author: jia.liu
* Date: 2013-03-027
* Major change history:
**********************************************************************/
package com.home.turnpage;

import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.home.turnpage.PageContainer.IPageContainer;

public class MagicBookView extends FrameLayout {
	public static final boolean DEBUG = true;
	public static final String TAG = "MonthFragment";
	
	private Context mContext;

	private PageContainer mPrePageContainter;
	private PageContainer mCurPageContainter;
	private PageContainer mNextPageContainter;
	private PageContainer mActiveContaiter;

	private Bitmap mPrePageBitmap;
	private Bitmap mCurPageBitmap;
	private Bitmap mNextPageBitmap;
	
	private static final int MOTION_SLOT = 4;
	private static final boolean INTERACT_MODE = true;
	private static final boolean DRAG_MODE = false;
	private boolean mIsInterActMode = true;
	private boolean mIsTurnBack = false;
	
	private float mMotionX = -1;
	private float mMotionY = -1;
	private float mDownX = -1;
	private float mDownY = -1;

	private static final boolean TURN = true;
	private static final boolean RESET = false;
	private boolean mTurnOrReset = RESET;

	private int mCurrentPage = -1;
	private int mPageCount = -1;
	private boolean mIsAnimating = false;

	private DrawingHelper mHelper;

	Scroller mScroller;
	Logger l;
	public MagicBookView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public MagicBookView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public MagicBookView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	private void onTurn(boolean isTurnBack) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ onTurn START ~~~~~~~~~~~~~~");
		PageContainer tmp = null;
		int width = MeasureSpec.makeMeasureSpec(mHelper.mWidth, MeasureSpec.EXACTLY);
		int height = MeasureSpec.makeMeasureSpec(mHelper.mHeight, MeasureSpec.EXACTLY);
		if (isTurnBack) {
			if(DEBUG)Log.i(TAG,"turn back");
			tmp = mNextPageContainter;
			mNextPageContainter = mCurPageContainter;
			mCurPageContainter = mPrePageContainter;
			mPrePageContainter = tmp;

			mCurrentPage--;
			mPrePageContainter.setCurPageInBook(mCurrentPage - 1);
			mPrePageContainter.doTurnReload(isTurnBack,mCurrentPage);			
			mPrePageContainter.measure(width,height);
			mPrePageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
			
		} else {
			if(DEBUG)Log.i(TAG,"not turn back");
			tmp = mPrePageContainter;
			mPrePageContainter = mCurPageContainter;
			mCurPageContainter = mNextPageContainter;
			mNextPageContainter = tmp;

			mCurrentPage++;
			mNextPageContainter.setCurPageInBook(mCurrentPage + 1);
			mNextPageContainter.doTurnReload(isTurnBack,mCurrentPage);
			mNextPageContainter.measure(width, height);
			mNextPageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
		}
		mActiveContaiter = mCurPageContainter;
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ onTurn End ~~~~~~~~~~~~~~");
	}
	

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onSizeChanged");
		super.onSizeChanged(w, h, oldw, oldh);
		mHelper = new DrawingHelper(w,h);
		mHelper.init();
		int width = MeasureSpec.makeMeasureSpec(mHelper.mWidth, MeasureSpec.EXACTLY);
		int height = MeasureSpec.makeMeasureSpec(mHelper.mHeight, MeasureSpec.EXACTLY);
		
		mPrePageContainter.measure(width, height);
		mCurPageContainter.measure(width, height);
		mNextPageContainter.measure(width, height);
		
		mPrePageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
		mCurPageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
		mNextPageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);

		mPrePageBitmap = Bitmap.createBitmap(mHelper.mWidth, mHelper.mHeight,
				Bitmap.Config.ARGB_8888);
		mCurPageBitmap = Bitmap.createBitmap(mHelper.mWidth, mHelper.mHeight,
				Bitmap.Config.ARGB_8888);
		mNextPageBitmap = Bitmap.createBitmap(mHelper.mWidth, mHelper.mHeight,
				Bitmap.Config.ARGB_8888);

		mHelper.mTouch.x = 0.01f;
		mHelper.mTouch.y = 0.01f;
		
		loadBitmaps();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ dispatchTouchEvent START ~~~~~~~~~~~~~~");
		if(!mScroller.isFinished()){
			if(DEBUG)Log.v(TAG,"scroll not finished dont accept touch event");
			if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ dispatchTouchEvent END ~~~~~~~~~~~~~~");
			return false;
		}
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ dispatchTouchEvent END ~~~~~~~~~~~~~~");
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// TODO Auto-generated method stub\
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ dispatchDraw START ~~~~~~~~~~~~~~");
		if(mIsInterActMode){
			if(DEBUG)Log.v(TAG,"is interact mode so draw children");
			if(DEBUG)Log.v(TAG,"mCurPageContainter.getCurPageInBook() = " + mCurPageContainter.getCurPageInBook());
			super.dispatchDraw(canvas);
		}else{
			if(DEBUG)Log.v(TAG,"Is not InterActMode should not draw children");
		}
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ dispatchDraw END ~~~~~~~~~~~~~~");
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ onDraw START ~~~~~~~~~~~~~~");
		if(mIsInterActMode){
			if(DEBUG)Log.v(TAG,"IsInterActMode should not draw itself");
			if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ onDraw END ~~~~~~~~~~~~~~");
			return;
		}
		canvas.drawColor(0xFFAAAAAA);
		if(DEBUG)Log.v(TAG,"isn't interact mode so draw itself");
		mHelper.calcPoints();
		if (!mIsTurnBack) {
			if(DEBUG)Log.v(TAG,"isn't turn back so draw cur and next bitmap");
			mHelper.drawCurrentPageArea(canvas, mCurPageBitmap, mHelper.mPath0);
			mHelper.drawNextPageAreaAndShadow(canvas, mNextPageBitmap);
			mHelper.drawCurrentPageShadow(canvas);
			mHelper.drawCurrentBackArea(canvas, mCurPageBitmap);
		} else {
			if(DEBUG)Log.v(TAG,"ist turn back so draw pre and cur bitmap");
			mHelper.drawCurrentPageArea(canvas, mPrePageBitmap, mHelper.mPath0);
			mHelper.drawNextPageAreaAndShadow(canvas, mCurPageBitmap);
			mHelper.drawCurrentPageShadow(canvas);
			mHelper.drawCurrentBackArea(canvas, mPrePageBitmap);
		}
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ onDraw END ~~~~~~~~~~~~~~");
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onInterceptTouchEvent START ~~~~~~~~~~~~~~");
		int action = ev.getAction();
		switch(action){
		case MotionEvent.ACTION_DOWN:
			View v = mActiveContaiter.getContent();
			Rect r = new Rect();
			mDownX = mMotionX = ev.getX();
			mDownY = mMotionY = ev.getY();
			
			if(v == null){
				if(DEBUG)Log.d(TAG,"action down content is null so we intercept touch event directly");
				if(canDrag(mDownX,mDownY)){
					changeMode(DRAG_MODE);
				}		
			} else {
				v.getHitRect(r);
				if (!r.contains((int) mMotionX, (int) mMotionY)) {
					if(DEBUG)Log.d(TAG,"action down touch point not in content so we intercept touch event");
					if (canDrag(mDownX, mDownY)) {
						changeMode(DRAG_MODE);
					}
				}else{
					return false;
				}
			}
			if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onInterceptTouchEvent END ~~~~~~~~~~~~~~");
			return true;
		case MotionEvent.ACTION_MOVE:
			float deltaX = ev.getX() - mMotionX;
			float deltaY = ev.getY() - mMotionY;
			float distance = (float) Math.pow(
					(double) (deltaX * deltaX + deltaY * deltaY), 0.5d);
			mMotionX = ev.getX();
			mMotionY = ev.getY();
			if (distance >= MOTION_SLOT) {
				if(DEBUG)Log.d(TAG,"action move moved distance > slot so intercept touch event");
				if(!canDrag(mDownX,mDownY)){
					if(DEBUG)Log.d(TAG,"action move touch can't drag so return false");
					return false;
				}
				mHelper.mTouch.x = ev.getX();
				mHelper.mTouch.y = ev.getY();
				changeMode(DRAG_MODE);
				return true;
			}
			if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onInterceptTouchEvent END ~~~~~~~~~~~~~~");
			return false;
		default:
			if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onInterceptTouchEvent END ~~~~~~~~~~~~~~");
			return false;
		}
	}

	private void loadBitmaps() {
			if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ loadBitmaps START ~~~~~~~~~~~~~~");
			mPrePageBitmap = Util.takeShort(mPrePageContainter, mPrePageBitmap);
			if(DEBUG)Log.d(TAG,"mPrePageBitmap.page =" + mPrePageContainter.getCurPageInBook());
			mCurPageBitmap = Util.takeShort(mCurPageContainter, mCurPageBitmap);
			if(DEBUG)Log.d(TAG,"mCurPageBitmap.page =" + mCurPageContainter.getCurPageInBook());
			mNextPageBitmap = Util.takeShort(mNextPageContainter, mNextPageBitmap);
			if(DEBUG)Log.d(TAG,"mNextPageBitmap.page =" + mNextPageContainter.getCurPageInBook());
			if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ loadBitmaps END ~~~~~~~~~~~~~~");
	}

	public boolean shouldDragOver() {
		if (mIsTurnBack) {
			mTurnOrReset = RESET;
			return false;
		} else if (mHelper.mTouchToCornerDis > mHelper.mWidth / 2) {
			mTurnOrReset = TURN;
			return true;
		}
		mTurnOrReset = RESET;
		return false;
	}

	private void startAnimation(boolean reset) {
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ startAnimation START ~~~~~~~~~~~~~~");
		int dx, dy;
		int duration = -1;
		if (!reset) {
			dx = mHelper.getScrollOverDisPiont().x;
			dy = mHelper.getScrollOverDisPiont().y;
			duration = DrawingHelper.ANIMATE_DURATION;
		} else {
			dx = mHelper.getScrollResetDisPiont().x;
			dy = mHelper.getScrollResetDisPiont().y;
			duration = DrawingHelper.RESET_ANIMATE_DURATION;
		}
		mScroller.startScroll((int) mHelper.mTouch.x, (int) mHelper.mTouch.y, dx, dy, duration);
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ startAnimation END ~~~~~~~~~~~~~~");
	}

	public void abortAnimation() {
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ abortAnimation START ~~~~~~~~~~~~~~");
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ abortAnimation END ~~~~~~~~~~~~~~");
	}

	public void computeScroll() {
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ computeScroll START~~~~~~~~~~~~~~");
		super.computeScroll();
		if(mIsInterActMode){
			return;
		}
		if (mScroller.computeScrollOffset()) {
			float x = mScroller.getCurrX();
			float y = mScroller.getCurrY();
			mHelper.mTouch.x = x;
			mHelper.mTouch.y = y;
			postInvalidate();
			if (!mIsAnimating) {
				mIsAnimating = true;
			}
		} else {
			if (mIsAnimating) {
				mIsAnimating = false;
				doTurnIfNeed();
				changeMode(INTERACT_MODE);
			}
		}
		if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ computeScroll END~~~~~~~~~~~~~~");
	}

	private void doTurnIfNeed() {
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ doTurnIfNeed START ~~~~~~~~~~~~~~");
		if (mIsTurnBack) {
			if (mCurrentPage <= 0) {
				if(DEBUG)Log.w(TAG,"is already first page");
				return;
			}
			if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ doTurnIfNeed Turn Back ~~~~~~~~~~~~~~");
			onTurn(mIsTurnBack);
		} else if (mTurnOrReset) {
			if (mCurrentPage >= mPageCount - 1) {
				if(DEBUG)Log.w(TAG,"is already last page");
				return;
			}
			if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ doTurnIfNeed Turn Over ~~~~~~~~~~~~~~");
			onTurn(mIsTurnBack);
		}
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ doTurnIfNeed END ~~~~~~~~~~~~~~");
	}
	
	public void initBookView(int pageCount, int curPage,
			IPageContainer pre, IPageContainer cur, IPageContainer next) {
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ initBookView START ~~~~~~~~~~~~~~");
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		setBackgroundColor(0x00000000);

		mCurrentPage = curPage;
		mPageCount = pageCount;
		mPrePageContainter = new PageContainer(mContext);
		mCurPageContainter = new PageContainer(mContext);
		mNextPageContainter = new PageContainer(mContext);

		mPrePageContainter.setIPageContainter(next);
		mCurPageContainter.setIPageContainter(cur);
		mNextPageContainter.setIPageContainter(pre);
		
		if(mPageCount <= 0){
			if(DEBUG)Log.e(TAG,"PageCount < 0");
			return;
		}
		
		if (mCurrentPage < 0 || mCurrentPage >= mPageCount) {
			if(DEBUG)Log.e(TAG,"illegal CurrentPage");
			return;
		}

		mPrePageContainter.setCurPageInBook(mCurrentPage - 1);
		mCurPageContainter.setCurPageInBook(mCurrentPage);
		mNextPageContainter.setCurPageInBook(mCurrentPage + 1);
		
		mPrePageContainter.setPageCount(mPageCount);
		mCurPageContainter.setPageCount(mPageCount);
		mNextPageContainter.setPageCount(mPageCount);
		
		mPrePageContainter.doInit();
		mCurPageContainter.doInit();
		mNextPageContainter.doInit();
		
		mActiveContaiter = mCurPageContainter;
		removeAllViews();
		addView(mActiveContaiter);		
		mScroller = new Scroller(mContext);
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ initBookView END ~~~~~~~~~~~~~~");
	}

	public void setContainterTurnReloadListeners(
			PageContainer.IPageContainer pre,
			PageContainer.IPageContainer next) {
		if (pre != mNextPageContainter.getIPageContainter()) {
			mNextPageContainter.setIPageContainter(pre);
		}

		if (next != mPrePageContainter.getIPageContainter()) {
			mPrePageContainter.setIPageContainter(next);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onTouchEvent START ~~~~~~~~~~~~~~");
		if(mIsInterActMode){
			if(DEBUG)Log.d(TAG,"is inter act mode shouldn't handle self touch event");
			if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onTouchEvent END ~~~~~~~~~~~~~~");
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			if(DEBUG)Log.v(TAG,"**** action move ****");
			mHelper.mTouch.x = event.getX();
			mHelper.mTouch.y = event.getY();
			if(DEBUG)Log.v(TAG,"mHelper.mTouch.x = " + mHelper.mTouch.x);
			if(DEBUG)Log.v(TAG,"mHelper.mTouch.y = " + mHelper.mTouch.y);
			if(mHelper.mTouch.y >= mHelper.mHeight){
				mHelper.mTouch.y =  mHelper.mHeight -1;
			}
			this.postInvalidate();
		}else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if(DEBUG)Log.v(TAG,"**** action down ****");
			if(!canDrag(event.getX(),event.getY())){
				return false;
			}
			mHelper.mTouch.x = event.getX();
			mHelper.mTouch.y = event.getY();
		}else if (event.getAction() == MotionEvent.ACTION_UP) {
			if(DEBUG)Log.v(TAG,"**** action up ****");
			if(DEBUG)Log.v(TAG,"~~~~~~~~~~~~~~ onTouchEvent ACTION_UP; ~~~~~~~~~~~~~~");
			startAnimation(!shouldDragOver());
			this.postInvalidate();
		}
		return true;
	}
	public boolean isFirstPage(){
		if(mCurrentPage == 0){
			return true;
		}
		return false;
	}
	public boolean isLastPage(){
		if(mCurrentPage == mPageCount - 1){
			return true;
		}
		return false;
	}
	private void changeMode(boolean mode){
		if(mode != mIsInterActMode){
			onModeChanged(mode);
			mIsInterActMode = mode;
			
		}
	}
	private void onModeChanged(boolean mode){
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ onModeChanged START ~~~~~~~~~~~~~~");
		if(mode == DRAG_MODE){
			if(DEBUG)Log.i(TAG,"drag mode should load bitmaps");
			loadBitmaps();
		}else{
			if(DEBUG)Log.i(TAG,"interact mode should reload containter");
			removeAllViews();
			addView(mActiveContaiter);
		}
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ onModeChanged END ~~~~~~~~~~~~~~");
	}
	private boolean canDrag(float x,float y){
		if (x < mHelper.mWidth / 3) {
			mIsTurnBack = true;
			if(isFirstPage()){
				return false;
			}
		} else {
			mIsTurnBack = false;
			if(isLastPage()){
				return false;
			}	
		}
		return true;
	}
	public PageContainer getActivePageContainer(){
		return mActiveContaiter;
	}
	public void setBookToPage(int page){
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ setBookToPage START ~~~~~~~~~~~~~~");
		if(page < 0 || page > mPageCount || page == mCurrentPage){
			return;
		}
		mCurrentPage = page;
		onReInit();
		if(DEBUG)Log.i(TAG,"~~~~~~~~~~~~~~ setBookToPage END ~~~~~~~~~~~~~~");
	}
	private void onReInit(){
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onReInit START ~~~~~~~~~~~~~~");
		int width = MeasureSpec.makeMeasureSpec(mHelper.mWidth, MeasureSpec.EXACTLY);
		int height = MeasureSpec.makeMeasureSpec(mHelper.mHeight, MeasureSpec.EXACTLY);
		
		mPrePageContainter.setCurPageInBook(mCurrentPage - 1);
		mCurPageContainter.setCurPageInBook(mCurrentPage);
		mNextPageContainter.setCurPageInBook(mCurrentPage + 1);
		
		mPrePageContainter.doReInit();
		mCurPageContainter.doReInit();
		mNextPageContainter.doReInit();
		mActiveContaiter = mCurPageContainter;
		
		mPrePageContainter.measure(width, height);
		mPrePageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
		mCurPageContainter.measure(width, height);
		mCurPageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
		mNextPageContainter.measure(width, height);
		mNextPageContainter.layout(0, 0, mHelper.mWidth, mHelper.mHeight);
		if(DEBUG)Log.d(TAG,"~~~~~~~~~~~~~~ onReInit END ~~~~~~~~~~~~~~");
	}
}

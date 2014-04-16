package com.custom.music.adapter;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.custom.music.MediaPlaybackActivity;
import com.custom.music.R;
import com.custom.music.bean.AlbumSongIdWrapper;
import com.custom.music.util.Constants;
import com.custom.music.util.bitmap.BitmapLoader;
import com.custom.music.util.loader.AlbumArtLoader;
import com.custom.music.util.loader.AlbumArtLoader.ImageCallback;
import com.custom.music.view.CoverFlow;
import com.custom.music.view.MusicGallery;

public class AlbumArtAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<AlbumSongIdWrapper> wrappers;
	private CoverFlow coverFlow;
	private Bitmap mBitmap;
	private Bitmap mDefaultTransitionDrawable;
	
	private ImageCallback imageCallback = new ImageCallback() {
		@Override
		public void imageLoaded(Bitmap bitmap, String imageUrl, int pos) {
			ImageView imageViewByTag = (ImageView) coverFlow
					.findViewWithTag(imageUrl);
			if(imageViewByTag != null)
			{
				if(isActiveSlot(pos) && bitmap != null )
				{
					synchronized (imageViewByTag) {
						boolean isLoaded = 
								(Boolean) imageViewByTag.getTag(R.id.isLoaded);
						if(!isLoaded)
						{
							setImageBitmap(imageViewByTag,createReflectedImages(bitmap));
							imageViewByTag.setTag(R.id.isLoaded, true);
						}
					}
				}
			}
		}
	};
	public AlbumArtAdapter(Context context, ArrayList<AlbumSongIdWrapper> wrappers, CoverFlow coverFlow) {
		this.context = context;
		this.wrappers = wrappers;
		this.coverFlow = coverFlow;
		setImageDispSize((int)(MediaPlaybackActivity.width * 0.5),(int)(MediaPlaybackActivity.width * 0.6));
		mDefaultTransitionDrawable = 
				createTransparentReflectedImages(BitmapFactory.decodeResource(context.getResources(), R.drawable.albumart_mp_unknown));
		mBitmap = 
				createReflectedImages(BitmapFactory.decodeResource(context.getResources(), R.drawable.albumart_mp_unknown));
	}
	
	@Override
	public int getCount() {
		return wrappers.size();
	}

	@Override
	public Object getItem(int position) {
		return wrappers.get(position);
	}
	public void removeItem(int position) {
		wrappers.remove(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = new ImageView(context);
			((ImageView) convertView).setScaleType(ImageView.ScaleType.FIT_XY);//set scale type   
			convertView.setFadingEdgeLength(10);
			convertView.setLayoutParams(new Gallery.LayoutParams(mDispWidth, mDispHeight));
		}
		final AlbumSongIdWrapper item = (AlbumSongIdWrapper)getItem(position);
		if(item != null){
			((ImageView) convertView).setImageBitmap(mBitmap);
			BitmapLoader bitmapLoader = 
					new AlbumArtLoader(
							context,
							item.getAlbumArtFileDescriptor(context),
							item.getUniqueName(), 
							Constants.TYPE_MICROTHUMBNAIL, 
							mDispWidth, mDispHeight,
							position, 
							imageCallback);
			bitmapLoader.startLoad();
			convertView.setTag(item.getUniqueName());
			convertView.setTag(R.id.isLoaded, false);
			convertView.setTag(R.id.songId, item.songid);
		}
		
		return convertView;
	}
	
	/**
	 * 图片加载动画
	 * @param imageView
	 * @param bitmap
	 */
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {         
		// Use TransitionDrawable to fade in.         
		final TransitionDrawable td = 
				new TransitionDrawable(
						new Drawable[] { new BitmapDrawable(context.getResources(), mDefaultTransitionDrawable), 
								new BitmapDrawable(context.getResources(), bitmap) });
		imageView.setImageDrawable(td);
		td.startTransition(300);
	}
	
	public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= coverFlow.getFirstVisiblePosition() && slotIndex <= coverFlow.getLastVisiblePosition();
    }
	
	private static final int DEFAULT_DISPLAY_WIDTH = 320;
    private static final int DEFAULT_DISPLAY_HEIGHT = 400;
    private static final float DEFAULT_REFLECTION = 0.30f;
    private static final int DEFAULT_REFLECTION_GAP = 20;
    private  Matrix mTransMatrix = new Matrix();
    private  Paint mNormalPaint = new Paint();
    private  Paint mGradientPaint = new Paint();
    private  int mDispWidth = DEFAULT_DISPLAY_WIDTH;
    private  int mDispHeight = DEFAULT_DISPLAY_HEIGHT;
    private  int mTotalDispHeight;
    private  float mReflection = DEFAULT_REFLECTION;
    
    /**
     * Set the displayed size of the image.
     * 
     * @param width the width of the view to display image.
     * @param height the height of the view to display image.
     */
    public void setImageDispSize(final int width, final int height) {
        mDispWidth = width;
        mDispHeight = height;
        mTotalDispHeight = (int) (mDispHeight * (1 + mReflection));

        // Create a linear gradient shader to implement transition effect.
//        mGradientPaint.setStyle(Paint.Style.FILL);
        //mGradientPaint.setAntiAlias(true);
        final LinearGradient shader = new LinearGradient(0, mDispHeight+ DEFAULT_REFLECTION_GAP, 0, mTotalDispHeight,
        		0x80000000, 0x00ffffff, TileMode.CLAMP);
        mGradientPaint.setShader(shader);

        // Set the Xfermode.
        mGradientPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }
    
    private  Bitmap createReflectedImages(Bitmap originalImage) {
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();

            // Create a bitmap by scaling the origin image to fit the view size.
        mTransMatrix.reset();
        mTransMatrix.postScale((float) mDispWidth / width, (float) mDispHeight / height);
		Bitmap scaledBitmap = Bitmap.createBitmap(originalImage, 0, 0, 
                width, height, mTransMatrix, true);
        mTransMatrix.reset();
        mTransMatrix.preScale(1, -1);
		Bitmap reflectedBitmap = Bitmap.createBitmap(scaledBitmap, 0,
	            (int) (mDispHeight * (1 - mReflection)), mDispWidth,
	            (int) (mDispHeight * mReflection), mTransMatrix, false);
	    Bitmap bitmapWithReflection = Bitmap.createBitmap(mDispWidth, mTotalDispHeight,
	            Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        // Draw the origin bitmap.
        canvas.drawBitmap(scaledBitmap, 0, 0, null);
        // Draw a rectangle to separate the origin bitmap and the reflection bitmap. 
        canvas.drawRect(0, mDispHeight, mDispWidth, mDispHeight + DEFAULT_REFLECTION_GAP,
                mNormalPaint);
        // Draw reflection bitmap.
        canvas.drawBitmap(reflectedBitmap, 0, mDispHeight + DEFAULT_REFLECTION_GAP, null);
        canvas.drawRect(0, mDispHeight+ DEFAULT_REFLECTION_GAP, mDispWidth, mTotalDispHeight,mGradientPaint);
        scaledBitmap.recycle();
        scaledBitmap = null;
        reflectedBitmap.recycle();	
        reflectedBitmap =  null;
        originalImage.recycle();
        originalImage = null;
		return bitmapWithReflection;
	}
    
    private  Bitmap createTransparentReflectedImages(Bitmap originalImage){
		int width = originalImage.getWidth();
		int height = originalImage.getHeight();
		
		//创建一个透明的图片
		Bitmap mTransparentBitmap = Bitmap.createBitmap(mDispWidth, mDispHeight, Config.ARGB_8888);
    	Canvas mCanvas = new Canvas(mTransparentBitmap);
    	mCanvas.drawColor(Color.TRANSPARENT);

            // Create a bitmap by scaling the origin image to fit the view size.
        mTransMatrix.reset();
        mTransMatrix.postScale((float) mDispWidth / width, (float) mDispHeight / height);
		Bitmap scaledBitmap = Bitmap.createBitmap(originalImage, 0, 0, 
                width, height, mTransMatrix, true);
        mTransMatrix.reset();
        mTransMatrix.preScale(1, -1);
        
        //倒影是纯透明
		Bitmap reflectedBitmap = Bitmap.createBitmap(mTransparentBitmap, 0,
	            (int) (mDispHeight * (1 - mReflection)), mDispWidth,
	            (int) (mDispHeight * mReflection), mTransMatrix, false);
	    Bitmap bitmapWithReflection = Bitmap.createBitmap(mDispWidth, mTotalDispHeight,
	            Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmapWithReflection);
        // Draw the origin bitmap.
        canvas.drawBitmap(scaledBitmap, 0, 0, null);
        // Draw a rectangle to separate the origin bitmap and the reflection bitmap. 
        canvas.drawRect(0, mDispHeight, mDispWidth, mDispHeight + DEFAULT_REFLECTION_GAP,
                mNormalPaint);
        // Draw reflection bitmap.
        canvas.drawBitmap(reflectedBitmap, 0, mDispHeight + DEFAULT_REFLECTION_GAP, null);
        canvas.drawRect(0, mDispHeight+ DEFAULT_REFLECTION_GAP, mDispWidth, mTotalDispHeight,mGradientPaint);
        scaledBitmap.recycle();
        scaledBitmap = null;
        reflectedBitmap.recycle();	
        reflectedBitmap =  null;
        originalImage.recycle();
        originalImage = null;
		return bitmapWithReflection;
	}
	

}

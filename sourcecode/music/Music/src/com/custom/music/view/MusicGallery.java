package com.custom.music.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Transformation;
import android.widget.Gallery;
import android.widget.ImageView;

public class MusicGallery extends Gallery {

    private Camera mCamera = new Camera();
    private int mMaxRotationAngle = 50;
    public static final int MAX_ZOOM_OUT = 160;
    private static float DEFAULT_MAX_ZOOM = 400.0f;
    private float mMaxZoom = DEFAULT_MAX_ZOOM;//-120;
    private int mCoveflowCenter;

    private static final String TAG = "MediaPlayback";

    public MusicGallery(Context context) {
            super(context);
            this.setStaticTransformationsEnabled(true);
    }

    public MusicGallery(Context context, AttributeSet attrs) {
            super(context, attrs);
            this.setStaticTransformationsEnabled(true);
    }

    public MusicGallery(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            this.setStaticTransformationsEnabled(true);
    }

    public int getMaxRotationAngle() {
            return mMaxRotationAngle;
    }

    public void setMaxRotationAngle(int maxRotationAngle) {
            mMaxRotationAngle = maxRotationAngle;
    }

//    public int getMaxZoom() {
//            return mMaxZoom;
//    }

    public void setMaxZoom(int maxZoom) {
            mMaxZoom = maxZoom;
    }

    private int getCenterOfCoverflow() {
            return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2
                            + getPaddingLeft();
    }

    private static int getCenterOfView(View view) {
            return view.getLeft() + view.getWidth() / 2;
    }

    protected boolean getChildStaticTransformation(View child, Transformation t) {
            final int childCenter = getCenterOfView(child);
            final int childWidth = child.getWidth();
            int rotationAngle = 0;

            t.clear();
            t.setTransformationType(Transformation.TYPE_MATRIX);
            if (childCenter == getCenterOfCoverflow()) {//mCoveflowCenter
                    transformImageBitmap((ImageView)child, t, 0);
            } else {
//                    rotationAngle = (int) (((float) (mCoveflowCenter - childCenter) / childWidth) * mMaxRotationAngle);
//                    if (Math.abs(rotationAngle) > mMaxRotationAngle) {
//                            rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle
//                                            : mMaxRotationAngle;
//                    }
            	  int distToCenter = getCenterOfCoverflow() - childCenter;               
//                rotationAngle = (int) Math.abs((((float) distToCenter / childWidth) * mMaxRotationAngle));
            	  rotationAngle = (int)(((float) distToCenter / childWidth) * mMaxRotationAngle);
//                if (rotationAngle > mMaxRotationAngle) {
//                    rotationAngle = mMaxRotationAngle;
//                   }
                if(Math.abs(rotationAngle) > mMaxRotationAngle){
                	rotationAngle = (rotationAngle < 0) ? -mMaxRotationAngle : mMaxRotationAngle;
                }
                transformImageBitmap((ImageView) child, t, rotationAngle);
            }

            return true;
    }
	
	private boolean isScrollingLeft(MotionEvent e1, MotionEvent e2) { 
        return e2.getX() > e1.getX(); 
    } 
	
//    @Override
//	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
//			float velocityY) {
//		// TODO Auto-generated method stub
//		int keyCode; 
//        if (isScrollingLeft(e1, e2)) { 
//            keyCode = KeyEvent.KEYCODE_DPAD_LEFT; 
//        } else { 
//            keyCode = KeyEvent.KEYCODE_DPAD_RIGHT; 
//        } 
//        onKeyDown(keyCode, null); 
//        return true; 
//	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mCoveflowCenter = getCenterOfCoverflow();
            super.onSizeChanged(w, h, oldw, oldh);
    }

    private void transformImageBitmap(ImageView child, Transformation t, int rotationAngle) {
//            mCamera.save();
//            final Matrix imageMatrix = t.getMatrix();
//            final int imageHeight = child.getLayoutParams().height;
//            final int imageWidth = child.getLayoutParams().width;
//            final int rotation = Math.abs(rotationAngle);
//
//            // 在Z轴上正向移动camera的视角，实际效果为放大图片。
//            // 如果在Y轴上移动，则图片上下移动；X轴上对应图片左右移动。
//            mCamera.translate(0.0f, 0.0f, 100.0f);
//
//            // As the angle of the view gets less, zoom in
//            if (rotation < mMaxRotationAngle) {
//                    float zoomAmount = (float) (mMaxZoom + (rotation * 1.5));
//                    mCamera.translate(0.0f, 0.0f, zoomAmount);
//            }
//
//            // 在Y轴上旋转，对应图片竖向向里翻转。
//            // 如果在X轴上旋转，则对应图片横向向里翻转。
//            mCamera.rotateY(rotationAngle);
//            mCamera.getMatrix(imageMatrix);
//            imageMatrix.preTranslate(-((float)imageWidth / 2), -((float)imageHeight / 2));
//            imageMatrix.postTranslate(((float)imageWidth / 2), ((float)imageHeight / 2));
//            mCamera.restore();
            
          //begin
                mCamera.save();

                final Matrix imageMatrix = t.getMatrix();
                final int imageHeight = child.getHeight();
                final int imageWidth = child.getWidth();

                final float zoomAmount = Math.abs(rotationAngle) * Math.abs((float) mMaxZoom / mMaxRotationAngle);
                final float yAngle = (float)(rotationAngle*0.1);
                mCamera.translate(0.0f, 0.0f, zoomAmount);
                mCamera.rotateY(yAngle);
                mCamera.getMatrix(imageMatrix);

                imageMatrix.preTranslate(-((imageWidth) / 2), -(imageHeight / 2));
                imageMatrix.postTranslate(((imageWidth) / 2), (imageHeight / 2));

                mCamera.restore();
    }
}

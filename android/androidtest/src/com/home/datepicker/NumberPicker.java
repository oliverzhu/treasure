
package com.home.datepicker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.home.R;

public class NumberPicker extends LinearLayout {

    private static final int SELECTOR_MIDDLE_ITEM_INDEX                         = 2;
    private static final int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT             = 8;
    private static final int SELECTOR_ADJUSTMENT_DURATION_MILLIS                = 300;//800;
    private static final float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH              = 0.9f;
    
    private static final int SELECTOR_WHEEL_STATE_NONE                          = 0;
    private static final int SELECTOR_WHEEL_STATE_SMALL                         = 1;
    private static final int SELECTOR_WHEEL_STATE_LARGE                         = 2;
    
    private static final int SELECTOR_WHEEL_BRIGHT_ALPHA                        = 255;
    private static final int SELECTOR_WHEEL_DIM_ALPHA                           = 60;
    
    private static final int SELECTOR_WHEEL_PRIMARY_TEXT_SIZE                   = 22;
    private static final int SELECTOR_WHEEL_SECONDARY_TEXT_SIZE                 = 18;
    
    private static final int SHOW_INPUT_CONTROLS_DELAY_MILLIS = 
            ViewConfiguration.getDoubleTapTimeout();
    
    private static final String PROPERTY_SELECTOR_PAINT_ALPHA = "selectorPaintAlpha";
    
    private static final int SIZE_UNSPECIFIED = -1;
    private static final String TAG = "NumberPicker";
    
    public static final NumberPicker.Formatter TWO_DIGIT_FORMATTER = new NumberPicker.Formatter() {
        final StringBuilder mBuilder = new StringBuilder();

        final java.util.Formatter mFmt = new java.util.Formatter(mBuilder, java.util.Locale.US);

        final Object[] mArgs = new Object[1];

        public String format(int value) {
            mArgs[0] = value;
            mBuilder.delete(0, mBuilder.length());
            mFmt.format("%02d", mArgs);
            return mFmt.toString();
        }
    };
    
    private final TextView mInputText;
    private final int mMinHeight;
    private final int mMaxHeight;
    private final int mMinWidth;
    private int mMaxWidth;
    private final boolean mComputeMaxWidth;
    private final int mTextSize;
    private int mSelectorTextGapHeight;
    private String[] mDisplayedValues;
    private int mMinValue;
    private int mMaxValue;
    private int mValue;
    private OnValueChangeListener mOnValueChangeListener;
    private OnScrollListener mOnScrollListener;
    private Formatter mFormatter;
    private final SparseArray<String> mSelectorIndexToStringCache = new SparseArray<String>();
    private final int[] mSelectorIndices = new int[] {
            Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE,
            Integer.MIN_VALUE
    };
    private final Paint mSelectorWheelPaint;
    private int mSelectorElementHeight;
    private int mInitialScrollOffset = Integer.MIN_VALUE;
    private int mCurrentScrollOffset;
    private final Scroller mFlingScroller;
    private final Scroller mAdjustScroller;
    private int mPreviousScrollerY;
    private AdjustScrollerCommand mAdjustScrollerCommand;
    private final AnimatorSet mShowInputControlsAnimator;
    private final Animator mDimSelectorWheelAnimator;
    private float mLastDownEventY;
    private float mLastMotionEventY;
    private boolean mCheckBeginEditOnUpEvent;
    private boolean mAdjustScrollerOnUpEvent;
    private int mSelectorWheelState;
    private VelocityTracker mVelocityTracker;
    private int mTouchSlop;
    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;
    private boolean mWrapSelectorWheel;
    private final int mSolidColor;
    private final boolean mFlingable;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;
    private final long mShowInputControlsAnimimationDuration;
    private boolean mScrollWheelAndFadingEdgesInitialized;
    private long mLastUpEventTimeMillis;
    private static final int BLACK_UNFOCUSED = 0xFF6F6F6F;
    private static final int BLACK_FOCUSED = 0xFF0BADA5;//0xFF383838;
    private static final int SHADOW_COLOR = 0XFFFFFFFF;
    
    private int mLeft;
    private int mTop;
    private int mRight;
    private int mBottom;
    
    public interface OnValueChangeListener {
        void onValueChange(NumberPicker picker, int oldVal, int newVal);
    }
    
    public interface OnScrollListener {
        public static int SCROLL_STATE_IDLE = 0;
        public static int SCROLL_STATE_TOUCH_SCROLL = 1;
        public static int SCROLL_STATE_FLING = 2;
        public void onScrollStateChange(NumberPicker view, int scrollState);
    }
    
    public interface Formatter {
        public String format(int value);
    }
    
    public NumberPicker(Context context) {
        this(context, null);
    }
    
    public NumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        TypedArray attributesArray = context.obtainStyledAttributes(attrs,
                R.styleable.NumberPicker, defStyle, 0);
        mSolidColor = attributesArray.getColor(R.styleable.NumberPicker_solidColor, 0);
        mFlingable = true;//attributesArray.getBoolean(R.styleable.NumberPicker_flingable, true);
        mMinHeight = attributesArray.getDimensionPixelSize(R.styleable.NumberPicker_internalMinHeight,
                SIZE_UNSPECIFIED);
        mMaxHeight = attributesArray.getDimensionPixelSize(R.styleable.NumberPicker_internalMaxHeight,
                SIZE_UNSPECIFIED);
        if (mMinHeight != SIZE_UNSPECIFIED && mMaxHeight != SIZE_UNSPECIFIED
                && mMinHeight > mMaxHeight) {
            throw new IllegalArgumentException("minHeight > maxHeight");
        }
        mMinWidth = attributesArray.getDimensionPixelSize(R.styleable.NumberPicker_internalMinWidth,
                SIZE_UNSPECIFIED);
        mMaxWidth = attributesArray.getDimensionPixelSize(R.styleable.NumberPicker_internalMaxWidth,
                SIZE_UNSPECIFIED);
        if (mMinWidth != SIZE_UNSPECIFIED && mMaxWidth != SIZE_UNSPECIFIED
                && mMinWidth > mMaxWidth) {
            throw new IllegalArgumentException("minWidth > maxWidth");
        }
        mComputeMaxWidth = (mMaxWidth == Integer.MAX_VALUE);
        attributesArray.recycle();

        mShowInputControlsAnimimationDuration = getResources().getInteger(
                R.integer.config_longAnimTime);
        setWillNotDraw(false);
        setSelectorWheelState(SELECTOR_WHEEL_STATE_NONE);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.number_picker, this, true);

        mInputText = (TextView) findViewById(R.id.numberpicker_input);
        mInputText.setBackground(null);
        mInputText.setShadowLayer(3.0f, 0f, 3f, SHADOW_COLOR);
        mInputText.setTextColor(BLACK_FOCUSED);
        mInputText.setTextSize(TypedValue.COMPLEX_UNIT_SP, SELECTOR_WHEEL_PRIMARY_TEXT_SIZE);
        
        // initialize constants
        mTouchSlop = ViewConfiguration.getTapTimeout();
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity()
                / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT;
        mTextSize = (int) mInputText.getTextSize();

        // create the selector wheel paint
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Align.CENTER);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 
                SELECTOR_WHEEL_SECONDARY_TEXT_SIZE, getResources().getDisplayMetrics()));
        paint.setColor(BLACK_UNFOCUSED);
        Log.d(TAG, "NumberPicker mInputText.getTextColors() is %h"+mInputText.getTextColors()
                            +", mTextSize is "+mTextSize);
        mSelectorWheelPaint = paint;

        mDimSelectorWheelAnimator = ObjectAnimator.ofInt(this, PROPERTY_SELECTOR_PAINT_ALPHA,
                SELECTOR_WHEEL_BRIGHT_ALPHA, SELECTOR_WHEEL_DIM_ALPHA);
        mShowInputControlsAnimator = new AnimatorSet();
        mShowInputControlsAnimator.addListener(new AnimatorListenerAdapter() {
            private boolean mCanceled = false;

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!mCanceled) {
                    // if canceled => we still want the wheel drawn
                   setSelectorWheelState(SELECTOR_WHEEL_STATE_SMALL);
                }
                mCanceled = false;
                
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                if (mShowInputControlsAnimator.isRunning()) {
                    mCanceled = true;
                }
            }
        });

        mFlingScroller = new Scroller(getContext(), null, true);
        mAdjustScroller = new Scroller(getContext(), new DecelerateInterpolator(2.5f));

        updateInputTextView();

        if (mFlingable) {
           if (isInEditMode()) {
               setSelectorWheelState(SELECTOR_WHEEL_STATE_SMALL);
           } else {
                setSelectorWheelState(SELECTOR_WHEEL_STATE_LARGE);
                hideInputControls();
           }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int msrdWdth = getMeasuredWidth();
        final int msrdHght = getMeasuredHeight();


        final int inptTxtMsrdWdth = mInputText.getMeasuredWidth();
        final int inptTxtMsrdHght = mInputText.getMeasuredHeight();
        final int inptTxtLeft = (msrdWdth - inptTxtMsrdWdth) / 2;
        final int inptTxtTop = (msrdHght - inptTxtMsrdHght) / 2;
        final int inptTxtRight = inptTxtLeft + inptTxtMsrdWdth;
        final int inptTxtBottom = inptTxtTop + inptTxtMsrdHght;
        mInputText.layout(inptTxtLeft, inptTxtTop, inptTxtRight, inptTxtBottom);

        mLeft = getLeft();
        mTop = getTop();
        mRight = getRight();
        mBottom = getBottom();
        Log.i(TAG, "nthpower[onLayout]mLeft:" + mLeft + ", mTop" + mTop + ", mRight" + mRight + ", mBottom" + mBottom);
        
        if (!mScrollWheelAndFadingEdgesInitialized) {
            mScrollWheelAndFadingEdgesInitialized = true;
            initializeSelectorWheel();
            initializeFadingEdges();
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig){
        mScrollWheelAndFadingEdgesInitialized = false;
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int newWidthMeasureSpec = makeMeasureSpec(widthMeasureSpec, mMaxWidth);
        final int newHeightMeasureSpec = makeMeasureSpec(heightMeasureSpec, mMaxHeight);
        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec);
        final int widthSize = resolveSizeAndStateRespectingMinSize(mMinWidth, getMeasuredWidth(),
                widthMeasureSpec);
        final int heightSize = resolveSizeAndStateRespectingMinSize(mMinHeight, getMeasuredHeight(),
                heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || !mFlingable) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionEventY = mLastDownEventY = event.getY();
                mShowInputControlsAnimator.cancel();
                mDimSelectorWheelAnimator.cancel();
                mCheckBeginEditOnUpEvent = false;
                mAdjustScrollerOnUpEvent = true;
                if (mSelectorWheelState == SELECTOR_WHEEL_STATE_LARGE) {
                    mSelectorWheelPaint.setAlpha(SELECTOR_WHEEL_BRIGHT_ALPHA);
                    boolean scrollersFinished = mFlingScroller.isFinished()
                            && mAdjustScroller.isFinished();
                    if (!scrollersFinished) {
                        mFlingScroller.forceFinished(true);
                        mAdjustScroller.forceFinished(true);
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
                    }
                    mCheckBeginEditOnUpEvent = scrollersFinished;
                    mAdjustScrollerOnUpEvent = true;
                    hideInputControls();
                    return true;
                }
                mAdjustScrollerOnUpEvent = false;
                setSelectorWheelState(SELECTOR_WHEEL_STATE_LARGE);
                hideInputControls();
                return true;
            case MotionEvent.ACTION_MOVE:
                mSelectorWheelPaint.setColor(BLACK_FOCUSED);
                float currentMoveY = event.getY();
                int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                if (deltaDownY > mTouchSlop) {
                    mCheckBeginEditOnUpEvent = false;
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    setSelectorWheelState(SELECTOR_WHEEL_STATE_LARGE);
                    hideInputControls();
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled()) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                float currentMoveY = ev.getY();
                if (mCheckBeginEditOnUpEvent
                        || mScrollState != OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    int deltaDownY = (int) Math.abs(currentMoveY - mLastDownEventY);
                    if (deltaDownY > mTouchSlop) {
                        mCheckBeginEditOnUpEvent = false;
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    }
                }
                int deltaMoveY = (int) (currentMoveY - mLastMotionEventY);
                scrollBy(0, deltaMoveY);
                invalidate();
                mLastMotionEventY = currentMoveY;
                break;
            case MotionEvent.ACTION_UP:
                if (mCheckBeginEditOnUpEvent) {
                    mCheckBeginEditOnUpEvent = false;
                    final long deltaTapTimeMillis = ev.getEventTime() - mLastUpEventTimeMillis;
                    if (deltaTapTimeMillis < ViewConfiguration.getDoubleTapTimeout()) {
                        setSelectorWheelState(SELECTOR_WHEEL_STATE_SMALL);
                        showInputControls(mShowInputControlsAnimimationDuration);
                        mLastUpEventTimeMillis = ev.getEventTime();
                    return true;
                }
                }
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
                int initialVelocity = (int) velocityTracker.getYVelocity();
                if (Math.abs(initialVelocity) > mMinimumFlingVelocity) {
                    fling(initialVelocity);
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                } else {
                    if (mAdjustScrollerOnUpEvent) {
                        if (mFlingScroller.isFinished() && mAdjustScroller.isFinished()) {
                            postAdjustScrollerCommand(0);
                        }
                    } else {
                        postAdjustScrollerCommand(SHOW_INPUT_CONTROLS_DELAY_MILLIS);
                    }
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mLastUpEventTimeMillis = ev.getEventTime();
                break;
        }
        return true;
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (mSelectorWheelState == SELECTOR_WHEEL_STATE_LARGE) {
                    forceCompleteChangeCurrentByOneViaScroll();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void computeScroll() {
        if (mSelectorWheelState == SELECTOR_WHEEL_STATE_NONE) {
            return;
        }
        Scroller scroller = mFlingScroller;
        if (scroller.isFinished()) {
            scroller = mAdjustScroller;
            if (scroller.isFinished()) {
                return;
            }
        }
        scroller.computeScrollOffset();
        int currentScrollerY = scroller.getCurrY();
        if (mPreviousScrollerY == 0) {
            mPreviousScrollerY = scroller.getStartY();
        }
        scrollBy(0, currentScrollerY - mPreviousScrollerY);
        mPreviousScrollerY = currentScrollerY;
        if (scroller.isFinished()) {
            onScrollerFinished(scroller);
        } else {
            invalidate();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mInputText.setEnabled(enabled);
    }

    @Override
    public void scrollBy(int x, int y) {
        if (mSelectorWheelState == SELECTOR_WHEEL_STATE_NONE) {
            return;
        }
        int[] selectorIndices = mSelectorIndices;
        if (!mWrapSelectorWheel && y > 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        if (!mWrapSelectorWheel && y < 0
                && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
            mCurrentScrollOffset = mInitialScrollOffset;
            return;
        }
        mCurrentScrollOffset += y;
        while (mCurrentScrollOffset - mInitialScrollOffset > mSelectorTextGapHeight) {
            mCurrentScrollOffset -= mSelectorElementHeight;
            decrementSelectorIndices(selectorIndices);
            changeCurrent(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX]);
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] <= mMinValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
        while (mCurrentScrollOffset - mInitialScrollOffset < -mSelectorTextGapHeight) {
            mCurrentScrollOffset += mSelectorElementHeight;
            incrementSelectorIndices(selectorIndices);
            changeCurrent(selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX]);
            if (!mWrapSelectorWheel && selectorIndices[SELECTOR_MIDDLE_ITEM_INDEX] >= mMaxValue) {
                mCurrentScrollOffset = mInitialScrollOffset;
            }
        }
    }

    @Override
    public int getSolidColor() {
        return mSolidColor;
    }
    
    public void setOnValueChangedListener(OnValueChangeListener onValueChangedListener) {
        mOnValueChangeListener = onValueChangedListener;
    }
    
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    public void setFormatter(Formatter formatter) {
        if (formatter == mFormatter) {
            return;
        }
        mFormatter = formatter;
        initializeSelectorWheelIndices();
        updateInputTextView();
    }

    public void setValue(int value) {
        if (mValue == value) {
            return;
        }
        if (value < mMinValue) {
            value = mWrapSelectorWheel ? mMaxValue : mMinValue;
        }
        if (value > mMaxValue) {
            value = mWrapSelectorWheel ? mMinValue : mMaxValue;
        }
        mValue = value;
        initializeSelectorWheelIndices();
        updateInputTextView();
        invalidate();
    }
    
    private void tryComputeMaxWidth() {
        if (!mComputeMaxWidth) {
            return;
        }
        int maxTextWidth = 0;
        if (mDisplayedValues == null) {
            float maxDigitWidth = 0;
            for (int i = 0; i <= 9; i++) {
                final float digitWidth = mSelectorWheelPaint.measureText(String.valueOf(i));
                if (digitWidth > maxDigitWidth) {
                    maxDigitWidth = digitWidth;
                }
            }
            int numberOfDigits = 0;
            int current = mMaxValue;
            while (current > 0) {
                numberOfDigits++;
                current = current / 10;
            }
            maxTextWidth = (int) (numberOfDigits * maxDigitWidth);
        } else {
            final int valueCount = mDisplayedValues.length;
            for (int i = 0; i < valueCount; i++) {
                final float textWidth = mSelectorWheelPaint.measureText(mDisplayedValues[i]);
                if (textWidth > maxTextWidth) {
                    maxTextWidth = (int) textWidth;
                }
            }
        }
        maxTextWidth += mInputText.getPaddingLeft() + mInputText.getPaddingRight();
        if (mMaxWidth != maxTextWidth) {
            if (maxTextWidth > mMinWidth) {
                mMaxWidth = maxTextWidth;
            } else {
                mMaxWidth = mMinWidth;
            }
            invalidate();
        }
    }
    
    public boolean getWrapSelectorWheel() {
        return mWrapSelectorWheel;
    }
    
    public void setWrapSelectorWheel(boolean wrapSelectorWheel) {
        if (wrapSelectorWheel && (mMaxValue - mMinValue) < mSelectorIndices.length) {
            throw new IllegalStateException("Range less than selector items count.");
        }
        if (wrapSelectorWheel != mWrapSelectorWheel) {
            mWrapSelectorWheel = wrapSelectorWheel;
        }
    }

    public int getValue() {
        return mValue;
    }
    
    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        if (mMinValue == minValue) {
            return;
        }
        if (minValue < 0) {
            throw new IllegalArgumentException("minValue must be >= 0");
        }
        mMinValue = minValue;
        if (mMinValue > mValue) {
            mValue = mMinValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        if (mMaxValue == maxValue) {
            return;
        }
        if (maxValue < 0) {
            throw new IllegalArgumentException("maxValue must be >= 0");
        }
        mMaxValue = maxValue;
        if (mMaxValue < mValue) {
            mValue = mMaxValue;
        }
        boolean wrapSelectorWheel = mMaxValue - mMinValue > mSelectorIndices.length;
        setWrapSelectorWheel(wrapSelectorWheel);
        initializeSelectorWheelIndices();
        updateInputTextView();
        tryComputeMaxWidth();
    }
    
    public String[] getDisplayedValues() {
        return mDisplayedValues;
    }
    
    public void setDisplayedValues(String[] displayedValues) {
        if (mDisplayedValues == displayedValues) {
            return;
        }
        mDisplayedValues = displayedValues;
        updateInputTextView();
        initializeSelectorWheelIndices();
        tryComputeMaxWidth();
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mFlingable && !isInEditMode()) {
            showInputControls(mShowInputControlsAnimimationDuration * 2);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (mShowInputControlsAnimator.isRunning()
                || mSelectorWheelState != SELECTOR_WHEEL_STATE_LARGE) {
            long drawTime = getDrawingTime();
            for (int i = 0, count = getChildCount(); i < count; i++) {
                View child = getChildAt(i);
                if (!child.isShown()) {
                    continue;
                }
                drawChild(canvas, getChildAt(i), drawTime);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSelectorWheelState == SELECTOR_WHEEL_STATE_NONE) {
            return;
        }

        float x = (mRight - mLeft) / 2;
        float y = mCurrentScrollOffset;

        final int restoreCount = canvas.save();

        int[] selectorIndices = mSelectorIndices;
        for (int i = 0; i < selectorIndices.length; i++) {
            int selectorIndex = selectorIndices[i];
            String scrollSelectorValue = mSelectorIndexToStringCache.get(selectorIndex);
            if (i != SELECTOR_MIDDLE_ITEM_INDEX || mInputText.getVisibility() != VISIBLE) {
                canvas.drawText(scrollSelectorValue, x, y, mSelectorWheelPaint);
            }
            y += mSelectorElementHeight;
        }
        canvas.restoreToCount(restoreCount);
    }

    private int makeMeasureSpec(int measureSpec, int maxSize) {
        if (maxSize == SIZE_UNSPECIFIED) {
            return measureSpec;
        }
        final int size = MeasureSpec.getSize(measureSpec);
        final int mode = MeasureSpec.getMode(measureSpec);
        switch (mode) {
            case MeasureSpec.EXACTLY:
                return measureSpec;
            case MeasureSpec.AT_MOST:
                return MeasureSpec.makeMeasureSpec(Math.min(size, maxSize), MeasureSpec.EXACTLY);
            case MeasureSpec.UNSPECIFIED:
                return MeasureSpec.makeMeasureSpec(maxSize, MeasureSpec.EXACTLY);
            default:
                throw new IllegalArgumentException("Unknown measure mode: " + mode);
        }
    }
    
    private int resolveSizeAndStateRespectingMinSize(int minSize, int measuredSize,
            int measureSpec) {
        if (minSize != SIZE_UNSPECIFIED) {
            final int desiredWidth = Math.max(minSize, measuredSize);
            return resolveSizeAndState(desiredWidth, measureSpec, 0);
        } else {
            return measuredSize;
        }
    }
    
    private void initializeSelectorWheelIndices() {
        mSelectorIndexToStringCache.clear();
        int current = getValue();
        for (int i = 0; i < mSelectorIndices.length; i++) {
            int selectorIndex = current + (i - SELECTOR_MIDDLE_ITEM_INDEX);
            if (mWrapSelectorWheel) {
                selectorIndex = getWrappedSelectorIndex(selectorIndex);
            }
            mSelectorIndices[i] = selectorIndex;
            ensureCachedScrollSelectorValue(mSelectorIndices[i]);
        }
    }

    private void changeCurrent(int current) {
        if (mValue == current) {
            return;
        }
        if (mWrapSelectorWheel) {
            current = getWrappedSelectorIndex(current);
        }
        int previous = mValue;
        setValue(current);
        notifyChange(previous, current);
    }

    private void forceCompleteChangeCurrentByOneViaScroll() {
        Scroller scroller = mFlingScroller;
        if (!scroller.isFinished()) {
            final int yBeforeAbort = scroller.getCurrY();
            scroller.abortAnimation();
            final int yDelta = scroller.getCurrY() - yBeforeAbort;
            scrollBy(0, yDelta);
        }
    }
    
    @SuppressWarnings("unused")
    private void setSelectorPaintAlpha(int alpha) {
        mSelectorWheelPaint.setAlpha(alpha);
        invalidate();
    }
    
    private void setSelectorWheelState(int selectorWheelState) {
        mSelectorWheelState = selectorWheelState;
        if (selectorWheelState == SELECTOR_WHEEL_STATE_LARGE) {
            mSelectorWheelPaint.setAlpha(SELECTOR_WHEEL_BRIGHT_ALPHA);
        }
    }

    private void initializeSelectorWheel() {
        initializeSelectorWheelIndices();
        int[] selectorIndices = mSelectorIndices;
        int totalTextHeight = selectorIndices.length * mTextSize;
        float totalTextGapHeight = (mBottom - mTop) - totalTextHeight;
        float textGapCount = selectorIndices.length - 1;
        mSelectorTextGapHeight = (int) (totalTextGapHeight / textGapCount + 0.5f) + 20;
        mSelectorElementHeight = mTextSize + mSelectorTextGapHeight;
        int editTextTextPosition = mInputText.getBaseline() + mInputText.getTop();
        mInitialScrollOffset = editTextTextPosition -
                (mSelectorElementHeight * SELECTOR_MIDDLE_ITEM_INDEX);
        mCurrentScrollOffset = mInitialScrollOffset;
        updateInputTextView();
    }

    private void initializeFadingEdges() {
        setVerticalFadingEdgeEnabled(true);
        setFadingEdgeLength((mBottom - mTop - mTextSize) / 2);
    }
    
    private void onScrollerFinished(Scroller scroller) {
        if (scroller == mFlingScroller) {
            if (mSelectorWheelState == SELECTOR_WHEEL_STATE_LARGE) {
                postAdjustScrollerCommand(0);
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            } else {
                updateInputTextView();
                fadeSelectorWheel(mShowInputControlsAnimimationDuration);
            }
        } else {
            updateInputTextView();
            showInputControls(mShowInputControlsAnimimationDuration);
        }
    }
    
    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }
    
    private void fling(int velocityY) {
        mPreviousScrollerY = 0;

        if (velocityY > 0) {
            mFlingScroller.fling(0, 0, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        } else {
            mFlingScroller.fling(0, Integer.MAX_VALUE, 0, velocityY, 0, 0, 0, Integer.MAX_VALUE);
        }

        invalidate();
    }

    private void hideInputControls() {
        mShowInputControlsAnimator.cancel();
        mInputText.setVisibility(INVISIBLE);
    }
    
    private void showInputControls(long animationDuration) {
        mInputText.setVisibility(VISIBLE);
        mShowInputControlsAnimator.setDuration(animationDuration);
        mShowInputControlsAnimator.start();
    }
    
    private void fadeSelectorWheel(long animationDuration) {
        mInputText.setVisibility(VISIBLE);
        mDimSelectorWheelAnimator.setDuration(animationDuration);
        mDimSelectorWheelAnimator.start();
    }
    
    private int getWrappedSelectorIndex(int selectorIndex) {
        if (selectorIndex > mMaxValue) {
            return mMinValue + (selectorIndex - mMaxValue) % (mMaxValue - mMinValue) - 1;
        } else if (selectorIndex < mMinValue) {
            return mMaxValue - (mMinValue - selectorIndex) % (mMaxValue - mMinValue) + 1;
        }
        return selectorIndex;
    }
    
    private void incrementSelectorIndices(int[] selectorIndices) {
        for (int i = 0; i < selectorIndices.length - 1; i++) {
            selectorIndices[i] = selectorIndices[i + 1];
        }
        int nextScrollSelectorIndex = selectorIndices[selectorIndices.length - 2] + 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex > mMaxValue) {
            nextScrollSelectorIndex = mMinValue;
        }
        selectorIndices[selectorIndices.length - 1] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }
    
    private void decrementSelectorIndices(int[] selectorIndices) {
        for (int i = selectorIndices.length - 1; i > 0; i--) {
            selectorIndices[i] = selectorIndices[i - 1];
        }
        int nextScrollSelectorIndex = selectorIndices[1] - 1;
        if (mWrapSelectorWheel && nextScrollSelectorIndex < mMinValue) {
            nextScrollSelectorIndex = mMaxValue;
        }
        selectorIndices[0] = nextScrollSelectorIndex;
        ensureCachedScrollSelectorValue(nextScrollSelectorIndex);
    }
    
    private void ensureCachedScrollSelectorValue(int selectorIndex) {
        SparseArray<String> cache = mSelectorIndexToStringCache;
        String scrollSelectorValue = cache.get(selectorIndex);
        if (scrollSelectorValue != null) {
            return;
        }
        if (selectorIndex < mMinValue || selectorIndex > mMaxValue) {
            scrollSelectorValue = "";
        } else {
            if (mDisplayedValues != null) {
                int displayedValueIndex = selectorIndex - mMinValue;
                scrollSelectorValue = mDisplayedValues[displayedValueIndex];
            } else {
                scrollSelectorValue = formatNumber(selectorIndex);
            }
        }
        cache.put(selectorIndex, scrollSelectorValue);
    }

    private String formatNumber(int value) {
        return (mFormatter != null) ? mFormatter.format(value) : String.valueOf(value);
    }
    
    private void updateInputTextView() {
        if (mDisplayedValues == null) {
            mInputText.setText(formatNumber(mValue));
        } else {
            mInputText.setText(mDisplayedValues[mValue - mMinValue]);
        }
    }
    
    private void notifyChange(int previous, int current) {
        if (mOnValueChangeListener != null) {
            mOnValueChangeListener.onValueChange(this, previous, mValue);
        }
    }

    private void postAdjustScrollerCommand(int delayMillis) {
        if (mAdjustScrollerCommand == null) {
            mAdjustScrollerCommand = new AdjustScrollerCommand();
        } else {
            removeCallbacks(mAdjustScrollerCommand);
        }
        postDelayed(mAdjustScrollerCommand, delayMillis);
    }
    
    class AdjustScrollerCommand implements Runnable {
        public void run() {
            mPreviousScrollerY = 0;
            if (mInitialScrollOffset == mCurrentScrollOffset) {
                updateInputTextView();
                showInputControls(mShowInputControlsAnimimationDuration);
                return;
            }
            int deltaY = mInitialScrollOffset - mCurrentScrollOffset;
            if (Math.abs(deltaY) > mSelectorElementHeight / 2) {
                deltaY += (deltaY > 0) ? -mSelectorElementHeight : mSelectorElementHeight;
            }
            mAdjustScroller.startScroll(0, 0, 0, deltaY, SELECTOR_ADJUSTMENT_DURATION_MILLIS);
            invalidate();
        }
    }
}

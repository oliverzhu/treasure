package com.home.ninepng;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.home.R;

/**
 * A simple extension of a regular linear layout that supports the divider API
 * of Android 4.0+. The dividers are added adjacent to the children by changing
 * their layout params. If you need to rely on the margins which fall in the
 * same orientation as the layout you should wrap the child in a simple
 * {@link android.widget.FrameLayout} so it can receive the margin.
 */
public class IcsLinearLayout extends LinearLayout {
    /**
     * Don't show any dividers.
     */
    public static final int SHOW_DIVIDER_NONE = 0;
    /**
     * Show a divider at the beginning of the group.
     */
    public static final int SHOW_DIVIDER_BEGINNING = 1;
    /**
     * Show dividers between each item in the group.
     */
    public static final int SHOW_DIVIDER_MIDDLE = 2;
    /**
     * Show a divider at the end of the group.
     */
    public static final int SHOW_DIVIDER_END = 4;


    private Drawable mDivider;
    private int mDividerWidth;
    private int mDividerHeight;
    private int mShowDividers;
    private int mDividerPadding;

    private boolean mUseLargestChild;

    public IcsLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.short_divider);
        mSetDividerDrawable(drawable);
        
        //用来设置divider距离上下的边距
        mSetDividerPadding(20);
    }

    /**
     * Set a drawable to be used as a divider between items.
     * @param divider Drawable that will divide each item.
     * @see #setShowDividers(int)
     */
    public void mSetDividerDrawable(Drawable divider) {
        if (divider == mDivider) {
            return;
        }
        mDivider = divider;
        if (divider != null) {
            mDividerWidth = 1;
            		//divider.getIntrinsicWidth();
            mDividerHeight = 1;
            //divider.getIntrinsicHeight();
        } 
        setWillNotDraw(divider == null);
        requestLayout();
    }

    /**
     * Set padding displayed on both ends of dividers.
     *
     * @param padding Padding value in pixels that will be applied to each end
     *
     * @see #setShowDividers(int)
     * @see #setDividerDrawable(Drawable)
     * @see #getDividerPadding()
     */
    public void mSetDividerPadding(int padding) {
        mDividerPadding = padding;
    }

    /**
     * Get the padding size used to inset dividers in pixels
     *
     * @see #setShowDividers(int)
     * @see #setDividerDrawable(Drawable)
     * @see #setDividerPadding(int)
     */
    public int getDividerPadding() {
        return mDividerPadding;
    }

    /**
     * Get the width of the current divider drawable.
     *
     * @hide Used internally by framework.
     */
    public int getDividerWidth() {
        return mDividerWidth;
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final int index = indexOfChild(child);
        final int orientation = getOrientation();
        final LayoutParams params = (LayoutParams) child.getLayoutParams();
        if (hasDividerBeforeChildAt(index)) {
            if (orientation == VERTICAL) {
                //Account for the divider by pushing everything up
                params.topMargin = mDividerHeight;
            } else {
                //Account for the divider by pushing everything left
                params.leftMargin = mDividerWidth;
            }
        }

        final int count = getChildCount();
        if (index == count - 1) {
            if (hasDividerBeforeChildAt(count)) {
                if (orientation == VERTICAL) {
                    params.bottomMargin = mDividerHeight;
                } else {
                    params.rightMargin = mDividerWidth;
                }
            }
        }
        super.measureChildWithMargins(child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDivider != null) {
            if (getOrientation() == VERTICAL) {
                drawDividersVertical(canvas);
            } else {
                drawDividersHorizontal(canvas);
            }
        }
        super.onDraw(canvas);
    }

    void drawDividersVertical(Canvas canvas) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt(i)) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int top = child.getTop() - lp.topMargin/* - mDividerHeight*/;
                    drawHorizontalDivider(canvas, top);
                }
            }
        }

        if (hasDividerBeforeChildAt(count)) {
            final View child = getChildAt(count - 1);
            int bottom = 0;
            if (child == null) {
                bottom = getHeight() - getPaddingBottom() - mDividerHeight;
            } else {
                //final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                bottom = child.getBottom()/* + lp.bottomMargin*/;
            }
            drawHorizontalDivider(canvas, bottom);
        }
    }

    void drawDividersHorizontal(Canvas canvas) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child != null && child.getVisibility() != GONE) {
                if (hasDividerBeforeChildAt(i)) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int left = child.getLeft() - lp.leftMargin/* - mDividerWidth*/;
                    drawVerticalDivider(canvas, left);
                }
            }
        }

        if (hasDividerBeforeChildAt(count)) {
            final View child = getChildAt(count - 1);
            int right = 0;
            if (child == null) {
                right = getWidth() - getPaddingRight() - mDividerWidth;
            } else {
                //final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                right = child.getRight()/* + lp.rightMargin*/;
            }
            drawVerticalDivider(canvas, right);
        }
    }

    void drawHorizontalDivider(Canvas canvas, int top) {
        mDivider.setBounds(getPaddingLeft() + mDividerPadding, top,
                getWidth() - getPaddingRight() - mDividerPadding, top + mDividerHeight);
        mDivider.draw(canvas);
    }

    void drawVerticalDivider(Canvas canvas, int left) {
        mDivider.setBounds(left, getPaddingTop() + mDividerPadding,
                left + mDividerWidth, getHeight() - getPaddingBottom() - mDividerPadding);
        mDivider.draw(canvas);
    }

    /**
     * Determines where to position dividers between children.
     *
     * @param childIndex Index of child to check for preceding divider
     * @return true if there should be a divider before the child at childIndex
     * @hide Pending API consideration. Currently only used internally by the system.
     */
    protected boolean hasDividerBeforeChildAt(int childIndex) {
        if (childIndex == 0) {
            return false;
            		//(mShowDividers & SHOW_DIVIDER_BEGINNING) != 0;
        } else if (childIndex == getChildCount()) {
            return false;
            		//(mShowDividers & SHOW_DIVIDER_END) != 0;
        } else{
            boolean hasVisibleViewBefore = false;
            for (int i = childIndex - 1; i >= 0; i--) {
                if (getChildAt(i).getVisibility() != GONE) {
                    hasVisibleViewBefore = true;
                    break;
                }
            }
            return hasVisibleViewBefore;
        }
    }

    /**
     * When true, all children with a weight will be considered having
     * the minimum size of the largest child. If false, all children are
     * measured normally.
     *
     * @return True to measure children with a weight using the minimum
     *         size of the largest child, false otherwise.
     *
     * @attr ref android.R.styleable#LinearLayout_measureWithLargestChild
     */
    public boolean isMeasureWithLargestChildEnabled() {
        return mUseLargestChild;
    }

    /**
     * When set to true, all children with a weight will be considered having
     * the minimum size of the largest child. If false, all children are
     * measured normally.
     *
     * Disabled by default.
     *
     * @param enabled True to measure children with a weight using the
     *        minimum size of the largest child, false otherwise.
     *
     * @attr ref android.R.styleable#LinearLayout_measureWithLargestChild
     */
    public void setMeasureWithLargestChildEnabled(boolean enabled) {
        mUseLargestChild = enabled;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mUseLargestChild) {
            final int orientation = getOrientation();
            switch (orientation) {
                case HORIZONTAL:
                    useLargestChildHorizontal();
                    break;

                case VERTICAL:
                    useLargestChildVertical();
                    break;
            }
        }
    }

    private void useLargestChildHorizontal() {
        final int childCount = getChildCount();

        // Find largest child width
        int largestChildWidth = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            largestChildWidth = Math.max(child.getMeasuredWidth(), largestChildWidth);
        }

        int totalWidth = 0;
        // Re-measure childs
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }

            final LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) child.getLayoutParams();

            float childExtra = lp.weight;
            if (childExtra > 0) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(largestChildWidth,
                                MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(child.getMeasuredHeight(),
                                MeasureSpec.EXACTLY));
                totalWidth += largestChildWidth;

            } else {
                totalWidth += child.getMeasuredWidth();
            }

            totalWidth += lp.leftMargin + lp.rightMargin;
        }

        totalWidth += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(totalWidth, getMeasuredHeight());
    }

    private void useLargestChildVertical() {
        final int childCount = getChildCount();

        // Find largest child width
        int largestChildHeight = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            largestChildHeight = Math.max(child.getMeasuredHeight(), largestChildHeight);
        }

        int totalHeight = 0;
        // Re-measure childs
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child == null || child.getVisibility() == View.GONE) {
                continue;
            }

            final LinearLayout.LayoutParams lp =
                    (LinearLayout.LayoutParams) child.getLayoutParams();

            float childExtra = lp.weight;
            if (childExtra > 0) {
                child.measure(
                        MeasureSpec.makeMeasureSpec(child.getMeasuredWidth(),
                                MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(largestChildHeight,
                                MeasureSpec.EXACTLY));
                totalHeight += largestChildHeight;

            } else {
                totalHeight += child.getMeasuredHeight();
            }

            totalHeight += lp.leftMargin + lp.rightMargin;
        }

        totalHeight += getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(getMeasuredWidth(), totalHeight);
    }
}

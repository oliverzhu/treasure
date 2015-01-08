package com.ape.cloudfile.widget;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

import com.ape.filemanager.R;

public class BottomActionBar extends LinearLayout
{
    public static final int MORE_BUTTON_ID = -1;
    public static final int DEFAULT_MAX_CELL_COUNT = 5;

    protected LinearLayout mContainer;
    private View mPopMenuAncherView;
    private LinearLayout.LayoutParams mCellParams;

    protected ArrayList<MyMenuItem> mAllItems;
    protected ArrayList<MyMenuItem> mNormalItems;
    protected ArrayList<MyMenuItem> mMorePopMenuItems;

    private int mMaxCellCount;
    private int mNormalDisplayCount;
    private int mCellMargin;

    private int mTextColor;
    private float mTextSize;

    private static final int DEFAULT_TEXT_COLOR = 0xFF787878;
    private static final int DEFAULT_TEXT_SIZE = 10;

    public class MyMenuItem
    {
        private CharSequence title;
        private Drawable icon;
        private int itemId;
        private boolean visible;
        private boolean enable;

        public MyMenuItem(CharSequence title, Drawable icon, int itemId)
        {
            this(title, icon, itemId, true, true);
        }

        public MyMenuItem(CharSequence title, Drawable icon, int itemId, boolean visible, boolean enable)
        {
            this.title = title;
            this.icon = icon;
            this.itemId = itemId;
            this.visible = visible;
            this.enable = enable;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + itemId;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MyMenuItem other = (MyMenuItem) obj;
            if (itemId != other.itemId)
                return false;
            return true;
        }

        public Drawable getIcon()
        {
            return icon;
        }
        public void setIcon(Drawable icon)
        {
            this.icon = icon;
        }
        public void setIcon(int iconId)
        {
            this.icon = getContext().getResources().getDrawable(iconId);
        }
        public CharSequence getTitle()
        {
            return title;
        }
        public void setTitle(CharSequence title)
        {
            this.title = title;
        }
        public void setTitle(int titleId)
        {
            this.title = getContext().getString(titleId);
        }
        public int getItemId()
        {
            return itemId;
        }
        public void setItemId(int itemId)
        {
            this.itemId = itemId;
        }

        public boolean isVisible()
        {
            return visible;
        }

        public void setVisible(boolean visible)
        {
            this.visible = visible;
        }

        public boolean isEnable()
        {
            return enable;
        }

        public void setEnable(boolean enable)
        {
            this.enable = enable;
        }
    }

    public BottomActionBar(Context context)
    {
        super(context);
        mMaxCellCount = DEFAULT_MAX_CELL_COUNT;
        init();
    }

    public BottomActionBar(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public BottomActionBar(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.BottomActionBar);
        if (typedArray != null)
        {
            mTextSize = typedArray.getDimension(R.styleable.BottomActionBar_bottomTextSize, DEFAULT_TEXT_SIZE);
            mTextColor = typedArray.getColor(R.styleable.BottomActionBar_bottomTextColor, DEFAULT_TEXT_COLOR);
            mMaxCellCount = typedArray.getInt(R.styleable.BottomActionBar_maxCellCount, DEFAULT_MAX_CELL_COUNT);
            typedArray.recycle();
        } else
        {
            mMaxCellCount = DEFAULT_MAX_CELL_COUNT;
        }

        init();
    }

    private void init()
    {
        setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContainer = new LinearLayout(getContext());
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mContainer, params);

        ViewGroup.LayoutParams ancherParams = new ViewGroup.LayoutParams(1, ViewGroup.LayoutParams.MATCH_PARENT);
        mPopMenuAncherView = new View(getContext());
        addView(mPopMenuAncherView, ancherParams);

        mCellParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mCellParams.gravity = Gravity.CENTER;
        mCellMargin = 1;

        mAllItems = new ArrayList<MyMenuItem>();
        mNormalItems = new ArrayList<MyMenuItem>();
        mMorePopMenuItems = new ArrayList<MyMenuItem>();
    }

    public void addMenuItem(MyMenuItem item)
    {
        mAllItems.add(item);
        //addToDisplayArray(item); // Add when refresh.
    }
    
    private void addToDisplayArray(MyMenuItem item)
    {
        if (item.isVisible())
        {
            if (mNormalItems.size() < mNormalDisplayCount)
            {
                mNormalItems.add(item);
            } else if (mNormalItems.size() == mNormalDisplayCount
                    && mMorePopMenuItems.size() == 0)
            {
                MyMenuItem overFlowItem = mNormalItems.get(mNormalDisplayCount - 1);
                mNormalItems.remove(mNormalDisplayCount - 1);
                MyMenuItem moreItem = new MyMenuItem(
                        getContext().getString(R.string.menu_item_more),
                        getContext().getResources().getDrawable(R.drawable.operation_button_more),
                        MORE_BUTTON_ID);
                mNormalItems.add(moreItem);
                mMorePopMenuItems.add(overFlowItem);
                mMorePopMenuItems.add(item);
            } else
            {
                mMorePopMenuItems.add(item);
            }
        }
    }

    public MyMenuItem addMenuItem(CharSequence title, Drawable icon, int itemId)
    {
        MyMenuItem myItem = new MyMenuItem(title, icon, itemId);

        addMenuItem(myItem);
        return myItem;
    }
    
    public MyMenuItem addMenuItem(MenuItem item)
    {
        MyMenuItem myItem = new MyMenuItem(item.getTitle(), item.getIcon(), item.getItemId());
        myItem.setVisible(item.isVisible());
        myItem.setEnable(item.isEnabled());

        addMenuItem(myItem);
        return myItem;
    }

    public void clearAllButtons()
    {
        mAllItems.clear();
        mNormalItems.clear();
        mMorePopMenuItems.clear();
        mContainer.removeAllViews();
    }
    
    public void refresh()
    {
        mContainer.removeAllViews();

        mNormalItems.clear();
        mMorePopMenuItems.clear();
        mCellParams.width = getWidth() / mMaxCellCount;

        for (MyMenuItem item : mAllItems)
        {
            addToDisplayArray(item);
        }

        for (MyMenuItem item : mNormalItems)
        {
            View cell = createCellButton(item);
            mContainer.addView(cell, mCellParams);
        }
    }
    
    private View createCellButton(MyMenuItem item)
    {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout cellLayout = new LinearLayout(getContext());
        cellLayout.setOrientation(LinearLayout.VERTICAL);
        cellLayout.setPadding(mCellMargin, 0, mCellMargin, 0);
        cellLayout.setGravity(Gravity.CENTER_HORIZONTAL);

//        if (item.getIcon() != null)
//        {
//            ImageButton imageBtn = new ImageButton(getContext());
//            imageBtn.setBackgroundDrawable(item.getIcon());
//            imageBtn.setOnClickListener(mClickListener);
//            imageBtn.setEnabled(item.isEnable());
//            imageBtn.setTag(item);
//            imageBtn.setLayoutParams(params);
//            cellLayout.addView(imageBtn);
//        }

        Drawable top = item.getIcon();
        top.setBounds(0, 0, top.getIntrinsicWidth(), top.getIntrinsicHeight());
        TextView tv = new TextView(getContext());
        tv.setText(item.getTitle());
        tv.setTextSize(mTextSize);
        tv.setTextColor(mTextColor);
        tv.setSingleLine(true);
        tv.setCompoundDrawables(null, top, null, null);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setTag(item);
        tv.setEnabled(item.isEnable());
        tv.setOnClickListener(mClickListener);
        tv.setLayoutParams(params);

        cellLayout.addView(tv);

        return cellLayout;
    }

    public int getNormalDisplayCount()
    {
        return mNormalDisplayCount;
    }

    public void setNormalDisplayCount(int normalDisplayCount)
    {
        mNormalDisplayCount = normalDisplayCount;
        if (mNormalDisplayCount < 2)
        {
            throw new RuntimeException("mNormalDisplayCount must >=2");
        }
    }

    public void setItemEnable(int itemId, boolean enable)
    {
        for (MyMenuItem item : mAllItems)
        {
            if (item.getItemId() == itemId)
            {
                item.setEnable(enable);
                break;
            }
        }
    }

    public void setItemVisible(int itemId, boolean visible)
    {
        for (MyMenuItem item : mAllItems)
        {
            if (item.getItemId() == itemId)
            {
                item.setVisible(visible);
                break;
            }
        }
    }
    
    public MyMenuItem findItem(int itemId)
    {
        for (MyMenuItem item : mAllItems)
        {
            if (item.getItemId() == itemId)
            {
                return item;
            }
        }

        return null;
    }

    public View getPopupMenuAncherView()
    {
        return mPopMenuAncherView;
    }

    public void setTextColor(int color)
    {
        mTextColor = color;
    }

    public void setTextSize(float size)
    {
        mTextSize = size;
    }

    private OnClickListener mClickListener = new OnClickListener()
    {
        
        @Override
        public void onClick(View v)
        {
            MyMenuItem actionItem = (MyMenuItem) v.getTag();
            if (actionItem.getItemId() == MORE_BUTTON_ID)
            {
                PopupMenu moreMenu = new PopupMenu(getContext(), mPopMenuAncherView);
                moreMenu.setOnMenuItemClickListener(mMoreMenuListener);
                Menu popMenu = moreMenu.getMenu();
                int order = 0;
                for (MyMenuItem item : mMorePopMenuItems)
                {
                    MenuItem popItem = popMenu.add(0, item.getItemId(), order++, item.getTitle());
                    popItem.setEnabled(item.isEnable());
                }
                moreMenu.show();
            } else
            {
                if (mActionListener != null)
                {
                    mActionListener.onActionItemClick(actionItem.getItemId());
                }
            }
        }
    };
    
    private OnMenuItemClickListener mMoreMenuListener = new OnMenuItemClickListener()
    {

        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            if (mActionListener != null)
            {
                return mActionListener.onActionItemClick(item.getItemId());
            }
            return false;
        }
        
    };

    public interface BottomActionBarListener
    {
        public boolean onActionItemClick(int itemId);
    }
    
    private BottomActionBarListener mActionListener;
    
    public void setBottomActionBarListerner(BottomActionBarListener listener)
    {
        mActionListener = listener;
    }
    
    public BottomActionBarListener getBottomActionBarListerner()
    {
        return mActionListener;
    }
}

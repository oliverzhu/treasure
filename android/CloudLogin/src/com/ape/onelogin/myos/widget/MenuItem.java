package com.ape.onelogin.myos.widget;

public class MenuItem {
    private int itemId;
    private int titleRes;
    private int iconRes;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    
    public MenuItem(int itemId, int titleRes, int iconRes) {
        this.itemId = itemId;
        this.titleRes = titleRes;
        this.iconRes = iconRes;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public void setTitleRes(int titleRes) {
        this.titleRes = titleRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }
    
    public OnMenuItemClickListener getOnMenuItemClickListener() {
        return mOnMenuItemClickListener;
    }
    public void setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        mOnMenuItemClickListener = menuItemClickListener;
    }
    
    public interface OnMenuItemClickListener {
        public boolean onMenuItemClick(MenuItem item);
    }
}

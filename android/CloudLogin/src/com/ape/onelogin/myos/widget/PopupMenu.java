package com.ape.onelogin.myos.widget;

import java.util.ArrayList;
import java.util.List;

import com.ape.onelogin.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class PopupMenu extends Dialog {
    
    protected static final String TAG = "PopupMenu";
    private TextView mTitleView;
    private ListView mListView;
    private PopupMenuAdapter mPopupMenuAdapter;
    private int mTitleID;
    private List<MenuItem> mMenuItems = new ArrayList<MenuItem>();
    
    public PopupMenu(Context context, int titleID) {
        super(context, R.style.Theme_MyOS_PopupMenu);
        mPopupMenuAdapter = new PopupMenuAdapter(context, mMenuItems);
        mTitleID = titleID;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup_menu);
        
        mTitleView = (TextView) findViewById(R.id.title);
        mTitleView.setText(mTitleID);
        
        mListView = (ListView) findViewById(R.id.list);
        mListView.setAdapter(mPopupMenuAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                MenuItem item = (MenuItem) mListView.getItemAtPosition(position);
                item.getOnMenuItemClickListener().onMenuItemClick(item);
                dismiss();
            }
        });
    }
    
    public void show() {
        mPopupMenuAdapter.setMenuList(mMenuItems);
        mPopupMenuAdapter.notifyDataSetChanged();
        super.show();
    }
    
    public MenuItem add(int itemId, int titleRes, int iconRes) {
        MenuItem item = new MenuItem(itemId, titleRes, iconRes);
        mMenuItems.add(item);
        return item;
    }
    
    public void remove(int itemId) {
        for (MenuItem item : mMenuItems) {
            if (item.getItemId() == itemId) {
                mMenuItems.remove(item);
            }
        }
    }
    
    private class PopupMenuAdapter extends BaseAdapter {

        private List<MenuItem> mMenuItems;
        private Context mContext;
        
        public PopupMenuAdapter(Context context, List<MenuItem> items) {
            mMenuItems = items;
            mContext = context;
        }
        
        @Override
        public int getCount() {
            return mMenuItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mMenuItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MenuItem menuItem = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.popup_menu_item, null);
                ImageButton icon = (ImageButton) convertView.findViewById(R.id.icon);
                TextView name = (TextView) convertView.findViewById(R.id.name);
                menuItem = mMenuItems.get(position);
                if (menuItem.getIconRes() > 0) {
                    icon.setVisibility(View.VISIBLE);
                    icon.setBackgroundResource(menuItem.getIconRes());
                } else {
                    icon.setVisibility(View.GONE);
                }
                name.setText(menuItem.getTitleRes());
                if (position == getCount() - 1) {
                    name.setBackgroundResource(R.drawable.popup_menu_list_item_corner_selector);
                } else {
                    name.setBackgroundResource(R.drawable.popup_menu_list_item_angle_selector);
                }
                
                name.setPadding(
                        getContext().getResources().getDimensionPixelSize(R.dimen.popup_menu_item_name_paddingLeft),
                        getContext().getResources().getDimensionPixelSize(R.dimen.popup_menu_item_name_paddingTop),
                        getContext().getResources().getDimensionPixelSize(R.dimen.popup_menu_item_name_paddingRight),
                        getContext().getResources().getDimensionPixelSize(R.dimen.popup_menu_item_name_paddingBottom));
            }
            return convertView;
        }
        
        public void setMenuList(List<MenuItem> items) {
            mMenuItems = items;
        }

    }
}

package com.ape.onelogin.myos.widget;

import java.util.ArrayList;
import java.util.List;

import com.ape.onelogin.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MyOSActionBar extends RelativeLayout {
    
    private static final String TAG = "MyOSActionBar";
    
    private Context mContext;
    private ImageButton mHomeButton;
    private ImageButton mOptionButton;
    private TextView mTitleView;
    private TextView mSimpleButton;
    
    private PopupWindow mOptionMenu;
    private ListView mMenuListView;
    
    private String mTitle;
    private String mSimpleButtonName;
    private boolean mSimpleButtonEnable;
    private boolean mOptionMenuEnable;
    
    private ViewOnClickListener mViewOnClickListener;
    private PopupMenuAdapter mPopupMenuAdapter;
    private List<MenuItem> mMenuItems = new ArrayList<MenuItem>();
    
    private static final int WIDGET_INDEX_HOME              = 1;
    private static final int WIDGET_INDEX_TITLE             = 2;
    private static final int WIDGET_INDEX_SIMPLE            = 3;
    private static final int WIDGET_INDEX_OPTION            = 4;
    
    private static final int WIDGET_MARGIN_LEFT             = 0; //dip
    private static final int WIDGET_MARGIN_RIGHT            = 0; //dip
    
    private static final int OPTION_MENU_WIDTH              = 150; //dip
    private static final int SIMPLE_BUTTON_WIDTH            = 80; //dip
    
    public MyOSActionBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.MyOSActionBar, defStyle, 0);
        
        mTitle = typedArray.getString(R.styleable.MyOSActionBar_titleName);
        mSimpleButtonName = typedArray.getString(R.styleable.MyOSActionBar_simpleButtonName);
        mSimpleButtonEnable = typedArray.getBoolean(R.styleable.MyOSActionBar_simpleButton, false);
        mOptionMenuEnable = typedArray.getBoolean(R.styleable.MyOSActionBar_opetionMenu, false);
        
        typedArray.recycle();
        initialize(context);
    }

    public MyOSActionBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyOSActionBar(Context context) {
        this(context, null, 0);
    }
    
    private void initialize(Context context) {
        mContext = context;
        mPopupMenuAdapter = new PopupMenuAdapter(context, mMenuItems);
    }
    
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        
        mViewOnClickListener = new ViewOnClickListener();
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelSize(R.dimen.myos_widget_height));
        setLayoutParams(mainParams);
        setBackgroundResource(R.drawable.ic_actionbar_bg);
        
        RelativeLayout.LayoutParams homeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        homeParams.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
        homeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, TRUE);
        homeParams.setMargins(parserDipToPx(WIDGET_MARGIN_LEFT), 0, 0, 0);
        mHomeButton = new ImageButton(getContext());
        mHomeButton.setBackground(null);
        mHomeButton.setImageResource(R.drawable.myos_button_home_selector);
        mHomeButton.setLayoutParams(homeParams);
        mHomeButton.setOnClickListener(mViewOnClickListener);
        mHomeButton.setId(WIDGET_INDEX_HOME);
        
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
        titleParams.addRule(RelativeLayout.RIGHT_OF, WIDGET_INDEX_HOME);
        titleParams.setMargins(parserDipToPx(WIDGET_MARGIN_LEFT), 0, 0, 0);
        mTitleView = new TextView(getContext());
        mTitleView.setText(mTitle);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 
                getResources().getDimensionPixelSize(R.dimen.topbar_title_text_size));
        mTitleView.setLayoutParams(titleParams);
        mTitleView.setId(WIDGET_INDEX_TITLE);
        
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, TRUE);
        layoutParams.setMargins(0, 0, parserDipToPx(WIDGET_MARGIN_RIGHT), 0);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(layoutParams);
        
        LinearLayout.LayoutParams simpleParams = new LinearLayout.LayoutParams(
                parserDipToPx(SIMPLE_BUTTON_WIDTH), LinearLayout.LayoutParams.MATCH_PARENT);
        simpleParams.setMargins(0, 0, parserDipToPx(WIDGET_MARGIN_RIGHT), 0);
        mSimpleButton = new TextView(getContext());
        mSimpleButton.setGravity(Gravity.CENTER);
        mSimpleButton.setText(mSimpleButtonName);
        mSimpleButton.setSingleLine();
        mSimpleButton.setBackgroundResource(R.drawable.myos_button_selector);
        mSimpleButton.setLayoutParams(simpleParams);
        mSimpleButton.setClickable(mSimpleButtonEnable);
        mSimpleButton.setOnClickListener(mViewOnClickListener);
        mSimpleButton.setVisibility(View.GONE);
        mSimpleButton.setId(WIDGET_INDEX_SIMPLE);
        if (mSimpleButtonName != null && mSimpleButtonEnable) {
            mSimpleButton.setVisibility(View.VISIBLE);
        } else {
            mSimpleButton.setVisibility(View.GONE);
        }
        
        LinearLayout.LayoutParams optionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        optionParams.setMargins(0, 0, parserDipToPx(WIDGET_MARGIN_RIGHT), 0);
        mOptionButton = new ImageButton(getContext());
        mOptionButton.setBackground(null);
        mOptionButton.setImageResource(R.drawable.myos_button_option);
        mOptionButton.setLayoutParams(optionParams);
        mOptionButton.setOnClickListener(mViewOnClickListener);
        mOptionButton.setId(WIDGET_INDEX_OPTION);
        if (mOptionMenuEnable) {
            mOptionButton.setVisibility(View.VISIBLE);
        } else {
            mOptionButton.setVisibility(View.GONE);
        }
        linearLayout.addView(mSimpleButton);
        linearLayout.addView(mOptionButton);
        
        addView(mHomeButton);
        addView(mTitleView);
        addView(linearLayout);
    }
    
    private void showOptionMenu() {
        if (mOptionMenu == null) {
            LinearLayout optionMenuLayout = new LinearLayout(getContext());
            LinearLayout.LayoutParams optionMenuParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            optionMenuLayout.setBackgroundResource(R.drawable.ic_actionbar_popup_menu_bg);
            optionMenuLayout.setLayoutParams(optionMenuParams);
            
            LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            mMenuListView = new ListView(getContext());
            mMenuListView.setLayoutParams(listParams);
            optionMenuLayout.addView(mMenuListView);
            
            mPopupMenuAdapter.setMenuList(mMenuItems);
            mMenuListView.setAdapter(mPopupMenuAdapter);
            mMenuListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    MenuItem item = mMenuItems.get(position);
                    item.getOnMenuItemClickListener().onMenuItemClick(item);
                    mOptionMenu.dismiss();
                }
            });
            
            mOptionMenu = new PopupWindow(optionMenuLayout, 
                    parserDipToPx(OPTION_MENU_WIDTH), LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        mOptionMenu.setFocusable(true);
        mOptionMenu.setOutsideTouchable(true);
        mOptionMenu.setBackgroundDrawable(new ColorDrawable(0));
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        int xPos = size.x - parserDipToPx(OPTION_MENU_WIDTH);
        mOptionMenu.showAsDropDown(this, xPos, 0);
    }
    
    public MenuItem addMenu(int itemId, int titleRes, int iconRes) {
        MenuItem item = new MenuItem(itemId, titleRes, iconRes);
        mMenuItems.add(item);
        return item;
    }
    
    public void removeMenu(int itemId) {
        for (MenuItem item : mMenuItems) {
            if (item.getItemId() == itemId) {
                mMenuItems.remove(item);
            }
        }
    }
    
    public void setOptionVisibility(int visibility) {
        mOptionButton.setVisibility(visibility);
    }
    
    public void setSimpleVisibility(int visibility) {
        mSimpleButton.setVisibility(visibility);
    }
    
    public void setSimpleEnable(boolean enabled) {
        mSimpleButton.setEnabled(enabled);
    }
    
    public void setSimpleOnClickListener(OnClickListener l) {
        mSimpleButton.setOnClickListener(l);
    }
    
    public void setHomeOnClickListener(OnClickListener l) {
        mHomeButton.setOnClickListener(l);
    }
    
    public void setActionTitle(String title) {
        mTitle = title;
        mTitleView.setText(mTitle);
    }
    
    private int parserDipToPx(int dip) {
        Resources resources = getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics()); 
        return px;
    }
    
    private class ViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case WIDGET_INDEX_HOME:
                    
                    break;
                case WIDGET_INDEX_OPTION:
                    if (mMenuItems.size() > 0) {
                        showOptionMenu();
                    }
                    break;
            }
        }
        
    }
    
    private class PopupMenuAdapter extends BaseAdapter{
        
        private Context mContext;
        private List<MenuItem> mMenuItems;
        
        public PopupMenuAdapter(Context context, List<MenuItem> items) {
            mContext = context;
            mMenuItems = items;
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
            }
            return convertView;
        }
        
        public void setMenuList(List<MenuItem> items) {
            mMenuItems = items;
        }
    }
}

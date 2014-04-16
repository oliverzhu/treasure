package com.home;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.home.adapter.MessageListFragmentAdapter;
import com.home.bean.OperatingBean;
import com.home.library.sm.SlidingMenu;
import com.home.library.vpi.BaseSampleActivity;
import com.home.util.thread.Future;
import com.home.util.thread.FutureListener;

public class MainActivity extends BaseSampleActivity {
	private static final int MENU_ID_REFRESH = Menu.FIRST;
	private static final int MSG_MESSAGES_LOADING_FINISHED = 1;
	
	private View mLoadingContainer;
	private TextView mEmpty;
    private View mListContainer;
    private ListView lv;
    
    private MessageListFragmentAdapter adapter;
    
    private Future<ArrayList<OperatingBean>> mMessageLoadJob;
    
    private boolean mQueryDone;
    
    private FutureListener<ArrayList<OperatingBean>> mMessagesLoadListener = 
    		new FutureListener<ArrayList<OperatingBean>>() {
		@Override
		public synchronized void onFutureDone(Future<ArrayList<OperatingBean>> future) {
			if (future != mMessageLoadJob) {
				return;
			}
			Message msg = mHandler.obtainMessage();
			msg.what = MSG_MESSAGES_LOADING_FINISHED;
			mHandler.sendMessage(msg);
		}
	};
    
    @SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) 
		{
			switch (msg.what) {
			case MSG_MESSAGES_LOADING_FINISHED:
				showListContainer();
				ArrayList<OperatingBean> messages = mMessageLoadJob.get();
				if(messages != null && messages.size() != 0)
				{
					adapter.setData(messages);
					adapter.notifyDataSetChanged();
					mEmpty.setVisibility(View.INVISIBLE);
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list);
		
		mLoadingContainer = findViewById(R.id.loading_container);
        mListContainer = findViewById(R.id.list_container);
		mEmpty=(TextView)findViewById(R.id.empty);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mListContainer.setVisibility(View.INVISIBLE);
        lv = (ListView) findViewById(R.id.list);
        adapter = new MessageListFragmentAdapter(this, lv);
        lv.setAdapter(adapter);
		
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
		getSlidingMenu().setBehindWidth((int) (0.6 * width));
		getSlidingMenu().setBehindScrollScale(0.0f);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mQueryDone) {
            menu.add(0, MENU_ID_REFRESH, 0, R.string.menu_status_refresh)
                    .setIcon(R.drawable.ic_menu_refresh_holo_dark)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }
		return true;
	}
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case MENU_ID_REFRESH:
	            getData();
	            return true;
	        default:
	            return false;
	        }
	    }
	
	private void showListContainer() {
        if (View.VISIBLE == mLoadingContainer.getVisibility()) {
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,
                    android.R.anim.fade_out));
            mListContainer.startAnimation(AnimationUtils.loadAnimation(MainActivity.this,
                    android.R.anim.fade_in));
        }
        mListContainer.setVisibility(View.VISIBLE);
        mLoadingContainer.setVisibility(View.INVISIBLE);
        mQueryDone = true;
        invalidateOptionsMenu();
    }
	
	private void showLoadingContainer() {
        if (View.INVISIBLE == mLoadingContainer.getVisibility()) {
            mLoadingContainer.setVisibility(View.VISIBLE);
            mListContainer.setVisibility(View.INVISIBLE);
        }
        
        mQueryDone = false;
        invalidateOptionsMenu();
    }
	
	private void getData()
	{
		showLoadingContainer();
		mMessageLoadJob = 
				AppApplication.appInteractionHub.submitLoadMessagesJob(mMessagesLoadListener);
	}
}

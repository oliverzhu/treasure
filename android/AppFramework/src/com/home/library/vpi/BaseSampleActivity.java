package com.home.library.vpi;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;

import com.home.R;
import com.home.fragment.SetFragment;
import com.home.library.sm.SlidingFragmentActivity;
import com.home.library.sm.SlidingMenu;

public abstract class BaseSampleActivity extends SlidingFragmentActivity {
	protected SetFragment mFrag;
	protected int width;
	protected int height;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		width = dm.widthPixels;
		height = dm.heightPixels;

		// set the Behind View
		setBehindContentView(R.layout.sm_frame);
		FragmentTransaction t = this.getSupportFragmentManager().beginTransaction();
		mFrag = SetFragment.newInstance();
		t.replace(R.id.menu_frame, mFrag);
		t.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.sm_shadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}
}

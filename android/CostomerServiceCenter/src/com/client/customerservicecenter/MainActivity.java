package com.client.customerservicecenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.WindowManager;

import com.client.customerservicecenter.adapter.CustomerServiceFragmentAdapter;
import com.client.customerservicecenter.fragment.BaseFragment;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.widget.vpi.TabPageIndicator;

public class MainActivity extends FragmentActivity {
	private CustomerServiceFragmentAdapter mFragmentAdapter;
	private ViewPager mPager;
	private TabPageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        
        mFragmentAdapter = new CustomerServiceFragmentAdapter(getSupportFragmentManager(),this);
		restoreOrCreateFragment(savedInstanceState);
		
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mFragmentAdapter);
		mPager.setOffscreenPageLimit(4);
        
        mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
        int tab_item = getIntent().getIntExtra("currentItem", 0);
    	mPager.setCurrentItem(tab_item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	/* there is a bug
    	 * getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_FAQ, mFragmentAdapter.FRAGMENT[0]);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_FEEDBACK, mFragmentAdapter.FRAGMENT[1]);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_ELECTRONIC_WARRANTY_CARD, mFragmentAdapter.FRAGMENT[2]);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_MORE, mFragmentAdapter.FRAGMENT[3]);*/
    }
    
    private void restoreOrCreateFragment(Bundle savedInstanceState)
    {
    	Fragment faqFragment = null;
    	Fragment feedbackFragment = null;
    	Fragment electronicWarrantyCardFragment = null;
    	Fragment moreFragment = null;
        if(savedInstanceState != null 
        		&& getSupportFragmentManager().getFragment(
        				savedInstanceState, Constants.FRAGMENT_FAQ) != null)
        {
        	faqFragment = 
        			getSupportFragmentManager().getFragment(
        					savedInstanceState, Constants.FRAGMENT_FAQ);
        }
        		
        if(savedInstanceState != null 
        		&& getSupportFragmentManager().getFragment(
        				savedInstanceState, Constants.FRAGMENT_FEEDBACK) != null)
        {
        	feedbackFragment = 
        			getSupportFragmentManager().getFragment(
        					savedInstanceState, Constants.FRAGMENT_FEEDBACK);
        }
        if(savedInstanceState != null 
        		&& getSupportFragmentManager().getFragment(
        				savedInstanceState, Constants.FRAGMENT_ELECTRONIC_WARRANTY_CARD) != null)
        {
        	electronicWarrantyCardFragment = 
        			getSupportFragmentManager().getFragment(
        					savedInstanceState, Constants.FRAGMENT_ELECTRONIC_WARRANTY_CARD);
        }
        if(savedInstanceState != null 
        		&& getSupportFragmentManager().getFragment(
        				savedInstanceState, Constants.FRAGMENT_MORE) != null)
        {
        	moreFragment = 
        			getSupportFragmentManager().getFragment(
        					savedInstanceState, Constants.FRAGMENT_MORE);
        }
        
        if(faqFragment == null)
        {
        	faqFragment = BaseFragment.createFaqFragment();
        }
        if(feedbackFragment == null)
        {
        	feedbackFragment = BaseFragment.createFeedbackFragment();
        }
        if(electronicWarrantyCardFragment == null)
        {
        	electronicWarrantyCardFragment = 
        			BaseFragment.createElectronicWarrantyCardFragment();
        }
        if(moreFragment == null)
        {
        	moreFragment = BaseFragment.createMoreFragment();
        }
        mFragmentAdapter.FRAGMENT[0] = faqFragment;
        mFragmentAdapter.FRAGMENT[1] = feedbackFragment;
        mFragmentAdapter.FRAGMENT[2] = electronicWarrantyCardFragment;
        mFragmentAdapter.FRAGMENT[3] = moreFragment;
    }
    
    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
    	super.onActivityResult(arg0, arg1, arg2);
    }
}

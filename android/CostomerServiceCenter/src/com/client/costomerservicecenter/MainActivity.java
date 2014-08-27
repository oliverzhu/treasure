package com.client.costomerservicecenter;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.client.costomerservicecenter.adapter.CustomerServiceFragmentAdapter;
import com.client.costomerservicecenter.fragment.BaseFragment;
import com.client.costomerservicecenter.util.Constants;
import com.client.costomerservicecenter.widget.vpi.TabPageIndicator;

public class MainActivity extends FragmentActivity {
	private CustomerServiceFragmentAdapter mFragmentAdapter;
	private ViewPager mPager;
	private TabPageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	ActionBar actionBar = getActionBar();
		actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mFragmentAdapter = new CustomerServiceFragmentAdapter(getSupportFragmentManager(),this);
		restoreOrCreateFragment(savedInstanceState);
		
		mPager = (ViewPager)findViewById(R.id.pager);
		mPager.setAdapter(mFragmentAdapter);
        
        mIndicator = (TabPageIndicator)findViewById(R.id.indicator);
        mIndicator.setViewPager(mPager);
        
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_FAQ, mFragmentAdapter.FRAGMENT[0]);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_FEEDBACK, mFragmentAdapter.FRAGMENT[1]);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_ELECTRONIC_WARRANTY_CARD, mFragmentAdapter.FRAGMENT[2]);
    	getSupportFragmentManager().putFragment(outState, Constants.FRAGMENT_MORE, mFragmentAdapter.FRAGMENT[3]);
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
}

package com.client.customerservicecenter.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.widget.vpi.IconPagerAdapter;

public class CustomerServiceFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[4];
    public Fragment[] FRAGMENT = new Fragment[4];
    private static final int[] ICONS = new int[] {
        R.drawable.fragment_icon_faq_selector,
        R.drawable.fragment_icon_feedback_selector,
        R.drawable.fragment_icon_electronic_warranty_card_selector,
        R.drawable.fragment_icon_more_selector,
    };

    private int mCount = CONTENT.length;

    public CustomerServiceFragmentAdapter(FragmentManager fm,Context context) {
        super(fm);
        CONTENT[0] = context.getResources().getString(R.string.fragment_title_faq);
        CONTENT[1] = context.getResources().getString(R.string.fragment_title_feedback);
        CONTENT[2] = context.getResources().getString(R.string.fragment_title_electronic_warranty_card);
        CONTENT[3] = context.getResources().getString(R.string.fragment_title_more);
    }

    @Override
    public Fragment getItem(int position) {
        return FRAGMENT[position % CONTENT.length];
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return CustomerServiceFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }

	@Override
	public int getIconResId(int index) {
		return ICONS[index];
	}
}
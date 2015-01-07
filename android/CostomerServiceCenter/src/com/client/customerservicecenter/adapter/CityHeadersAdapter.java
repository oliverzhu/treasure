/*
 Copyright 2013 Tonic Artos

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.client.customerservicecenter.adapter;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.client.customerservicecenter.application.AppApplication;
import com.client.customerservicecenter.bean.CityInfo;
import com.client.customerservicecenter.widget.sgh.StickyGridHeadersSimpleAdapter;

/**
 * 
 * @author jianwen.zhu
 * @since 2014/10/22
 * @param <T>
 */
public class CityHeadersAdapter extends BaseAdapter implements
	StickyGridHeadersSimpleAdapter {
    protected static final String TAG = CityHeadersAdapter.class.getSimpleName();

    private int mHeaderResId;

    private LayoutInflater mInflater;

    private int mItemResId;

    private List<CityInfo> mItems;

    public CityHeadersAdapter(Context context, List<CityInfo> items, int headerResId,
            int itemResId) {
        init(context, items, headerResId, itemResId);
    }

    public CityHeadersAdapter(Context context, CityInfo[] items, int headerResId,
            int itemResId) {
        init(context, Arrays.asList(items), headerResId, itemResId);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public long getHeaderId(int position) {
    	CityInfo item = getItem(position);
        return Long.valueOf(item.getProvinceCode());
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mHeaderResId, parent, false);
            holder = new HeaderViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder)convertView.getTag();
        }

        CityInfo item = getItem(position);
        Long proviceCode = Long.valueOf(item.getProvinceCode());

        // set header text as first char in string
        holder.textView.setText(
        		AppApplication.baiduMapHub.getProvinceMap().get(proviceCode));

        return convertView;
    }

    @Override
    public CityInfo getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(mItemResId, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        CityInfo item = getItem(position);
        holder.textView.setText(item.getCity());

        return convertView;
    }

    private void init(Context context, List<CityInfo> items, int headerResId, int itemResId) {
        this.mItems = items;
        this.mHeaderResId = headerResId;
        this.mItemResId = itemResId;
        mInflater = LayoutInflater.from(context);
    }

    protected class HeaderViewHolder {
        public TextView textView;
    }

    protected class ViewHolder {
        public TextView textView;
    }
}

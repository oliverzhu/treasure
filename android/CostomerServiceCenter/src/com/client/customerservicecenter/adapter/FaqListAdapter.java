package com.client.customerservicecenter.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TextView;

import com.client.customerservicecenter.R;
import com.client.customerservicecenter.bean.CommentInfo;
import com.client.customerservicecenter.bean.LoginUserInfo;
import com.client.customerservicecenter.bean.Picture;
import com.client.customerservicecenter.util.Constants;
import com.client.customerservicecenter.util.ContextUtils;
import com.client.customerservicecenter.view.HSVLayout;

/**
 * @author jianwen.zhu
 * 2013/12/11
 */
public class FaqListAdapter extends BaseAdapter{
	private Context mContext;
	public ArrayList<CommentInfo> listOperating = new ArrayList<CommentInfo>();
	public HashSet<CommentInfo> setOperating = new HashSet<CommentInfo>();
	
	private ListView newsLv;
	
	/**
	private ImageCallback imageCallback = new ImageCallback() {
		@Override
		public void imageLoaded(Bitmap bitmap, String itemId, int pos) {
			CircleImageView imageViewByTag = (CircleImageView) newsLv
					.findViewWithTag(itemId);
			if(imageViewByTag != null && isActiveSlot(pos) 
					&& bitmap != null)
			{
				synchronized (imageViewByTag) {
					boolean isLoaded = 
							(Boolean) imageViewByTag.getTag(R.id.isLoaded);
					if(!isLoaded)
					{
						setImageBitmap(imageViewByTag,bitmap);
						imageViewByTag.setTag(R.id.isLoaded, true);
					}
				}
			}
		}
	};*/
	
	public FaqListAdapter(Context context,ListView newsLv){
		this.mContext = context;
		this.newsLv = newsLv;
	}
	
	public synchronized void setData(ArrayList<CommentInfo> data,boolean clear) {
		if(data == null)
		{
			return;
		}
		if(clear)
		{
			listOperating.clear();
			listOperating.addAll(data);
		}else
		{
            for (CommentInfo operateEntry : data) {
            	if(setOperating.add(operateEntry))
            	{
            		listOperating.add(operateEntry);
            	}
            }
		}
		notifyDataSetChanged();
    }

	
	public Context getContext() {
		return mContext;
	}


	public void setContext(Context context) {
		this.mContext = context;
	}

	@Override
	public int getCount() {
		if(listOperating!=null && listOperating.size() > 0){
			return listOperating.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if(listOperating!=null && listOperating.size() > position){
			return listOperating.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.item_faq, null);
			holder = new ViewHolder();
			holder.userName = (TextView) convertView.findViewById(R.id.userName);
			holder.date = (TextView) convertView.findViewById(R.id.date);
			holder.feedback_type = (TextView) convertView.findViewById(R.id.feedback_type);
			holder.feedback_content = (TextView) convertView.findViewById(R.id.feedback_content);
			holder.feedback_answer = (TextView) convertView.findViewById(R.id.feedback_answer);
//			holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar); 
			holder.feedback_hsv = (HorizontalScrollView) convertView.findViewById(R.id.feedback_hsv);
			holder.divider_vertical = (TextView) convertView.findViewById(R.id.divider_vertical);
			convertView.setTag(holder); 
			
			
		}else{
			holder = (ViewHolder)convertView.getTag();
		}
		
		if(position == (getCount() - 1))
		{
			holder.divider_vertical.setVisibility(View.INVISIBLE);
		}else
		{
			holder.divider_vertical.setVisibility(View.VISIBLE);
		}
		final CommentInfo item = (CommentInfo)getItem(position);
		if(item != null){
			LoginUserInfo loginUserInfo = item.getLoginUserInfo();
			String dateStr = ContextUtils.getFormatString(item.getCommentDate(), mContext);
			String feedbackTypeStr = getIssueType(item.getType());
			String feedbackContentStr = item.getComment();
			String feedbackAnswerStr = item.getReply();
			String feedbackUser = null;
//			String avatarKey = null;
			
			if(loginUserInfo != null)
			{
				feedbackUser = loginUserInfo.getNickName();
//				avatarKey = loginUserInfo.getHeadImg();
			}
			
			if(feedbackUser == null || feedbackUser.trim().length() == 0)
			{
				feedbackUser = mContext.getResources().getString(R.string.label_anonymous);
			}
			holder.userName.setText(feedbackUser);
			holder.date.setText(dateStr);
			holder.feedback_type.setText(feedbackTypeStr);
			holder.feedback_content.setText(feedbackContentStr);
			holder.feedback_answer.setText(feedbackAnswerStr);
			
			/**
			if(avatarKey != null && avatarKey.trim().length() != 0 && loginUserInfo != null)
			{
				BitmapLoader bitmapLoader = 
						new AvatarLoader(
								mContext, 
								String.valueOf(item.getId()),
								getCloudFileUserMap(loginUserInfo),
								avatarKey, 
								Constants.TYPE_MICROTHUMBNAIL, 
								Constants.SIZE_SHOW_MICROTHUMBNAIL_WEIGHT, 
								Constants.SIZE_SHOW_MICROTHUMBNAIL_HEIGHT, 
								position, 
								imageCallback);
				bitmapLoader.startLoad();
				holder.avatar.setTag(String.valueOf(item.getId()));
				holder.avatar.setTag(R.id.isLoaded, false);
			}*/
			
			ArrayList<Picture> pictures = item.getPicture();
			if(pictures != null && pictures.size() != 0 && loginUserInfo != null)
			{
				holder.feedback_hsv.setVisibility(View.VISIBLE);
				HSVLayout hsvLayout = (HSVLayout) holder.feedback_hsv.findViewById(R.id.pictureLayout);
				HSVAdapter adapter = hsvLayout.getAdapter();
				if(adapter == null)
				{
					adapter = new HSVAdapter(mContext,hsvLayout);
				}
				adapter.setDatas(pictures);
				adapter.setUserInfoMap(getCloudFileUserMap(item.getLoginUserInfo()));
				hsvLayout.setAdapter(adapter);
			}else
			{
				holder.feedback_hsv.setVisibility(View.GONE);
			}
		}
		
		return convertView;
	}
	
	
	public class ViewHolder{
		private TextView userName;
		private TextView date;
		private TextView feedback_type;
		private TextView feedback_content;
		private TextView feedback_answer;
//		private CircleImageView avatar;
		private HorizontalScrollView feedback_hsv;
		private TextView divider_vertical;
	}
	
	private String getIssueType(String type)
	{
		int typeInt = Integer.valueOf(type);
		String issueType = "";
		switch (typeInt) {
		case Constants.TYPE_SYSTEM:
			issueType = mContext.getResources().getString(R.string.error_system);
			break;
		case Constants.TYPE_BATTERY:
			issueType = mContext.getResources().getString(R.string.error_battery);
			break;
		case Constants.TYPE_PHONE:
			issueType = mContext.getResources().getString(R.string.error_phone);
			break;
		case Constants.TYPE_NET:
			issueType = mContext.getResources().getString(R.string.error_net);
			break;
		case Constants.TYPE_APPLICATION:
			issueType = mContext.getResources().getString(R.string.error_application);
			break;
		case Constants.TYPE_DATA:
			issueType = mContext.getResources().getString(R.string.error_data);
			break;
		case Constants.TYPE_OTHERS:
			issueType = mContext.getResources().getString(R.string.error_others);
			break;
		case Constants.TYPE_SUGAR_ADVISE:
			issueType = mContext.getResources().getString(R.string.sugar_advise);
			break;

		default:
			break;
		}
		return issueType;
	}
	
	/**
	 * 图片加载动画
	 * @param imageView
	 * @param bitmap
	 
	private void setImageBitmap(CircleImageView imageView, Bitmap bitmap) {
		// Use TransitionDrawable to fade in.         
		final TransitionDrawable td = 
				new TransitionDrawable(
						new Drawable[] { new ColorDrawable(android.R.color.transparent), 
								new BitmapDrawable(mContext.getResources(), bitmap) });         
		imageView.setImageDrawable(new BitmapDrawable(mContext.getResources(), bitmap));
//		td.startTransition(400);
	}*/
	
	public boolean isActiveSlot(int slotIndex) {
        return slotIndex >= newsLv.getFirstVisiblePosition() && slotIndex <= newsLv.getLastVisiblePosition();
    }
	
	public static Map<String, String> getCloudFileUserMap(LoginUserInfo loginUserInfo)
    {
		String userId = loginUserInfo.getId() + "";
		String accessId = loginUserInfo.getAccessId();
		String secretKey = loginUserInfo.getSecretKey();
		String bucketName = loginUserInfo.getBucketName();
		String ossType = loginUserInfo.getOssType();
		String ossLocal = loginUserInfo.getOssLocal();
    	Map<String, String> userMap = new HashMap<String, String>();
    	userMap.put("userkey",userId);
    	userMap.put("accessid",accessId);
    	userMap.put("secretkey",secretKey);
    	userMap.put("bucketname",bucketName);
    	userMap.put("osstype",ossType);
    	userMap.put("osslocal",ossLocal);
    	return userMap;
    }
}

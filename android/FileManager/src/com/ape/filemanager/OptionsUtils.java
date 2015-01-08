package com.ape.filemanager;

import com.ape.filemanager.R;

import android.content.Context;

public class OptionsUtils
{
	static public boolean isHaveFavorite(Context context)
	{
		return context.getResources().getBoolean(R.bool.have_favorite);
	}
	
	static public boolean isHaveSettingMenu(Context context)
	{
		return context.getResources().getBoolean(R.bool.have_setting_menu);
	}
}

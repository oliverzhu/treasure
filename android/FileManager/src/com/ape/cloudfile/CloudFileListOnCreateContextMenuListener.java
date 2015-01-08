package com.ape.cloudfile;

import com.ape.filemanager.MyLog;
import com.ape.filemanager.R;

import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;

public class CloudFileListOnCreateContextMenuListener implements OnCreateContextMenuListener
{
    private static final String TAG = "CloudFileListOnCreateContext";
    public static final int MENU_ITEM_DELETE = 100;
    public static final int MENU_ITEM_DOWNLOAD = 101;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo)
    {
        AdapterView.AdapterContextMenuInfo info = null;
        try
        {
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException exception)
        {
            MyLog.e(TAG, "Bad menuInfo." + exception);
        }
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete_str);
        menu.add(0, MENU_ITEM_DOWNLOAD, 0, R.string.menu_download_str);
    }

}

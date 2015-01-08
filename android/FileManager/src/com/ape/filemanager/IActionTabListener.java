package com.ape.filemanager;

import android.view.ActionMode;
import android.view.View;

public interface IActionTabListener {

    public void setActionMode(ActionMode actionMode);

    public ActionMode getActionMode();
    
    public ActionMode startActionMode(ActionMode.Callback callback);
    
    public ActionMode.Callback getActionModeCallback();

    public void onTabChanged();
    
    public View getActionModeCustomView();
}

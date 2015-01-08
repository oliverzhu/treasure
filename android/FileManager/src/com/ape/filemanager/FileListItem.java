/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ape.filemanager;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ape.cloudfile.widget.BottomActionBar;
import com.ape.cloudfile.widget.BottomActionBar.BottomActionBarListener;
import com.ape.filemanager.FileViewInteractionHub.Mode;

public class FileListItem {
    private static final float DEFAULT_ICON_ALPHA = 1f;
    private static final float HIDE_ICON_ALPHA = 0.3f;

    public static void setupFileListItemInfo(Context context, View view,
            FileInfo fileInfo, FileIconHelper fileIcon,
            FileViewInteractionHub fileViewInteractionHub) {

        // if in moving mode, show selected file always
//        if (fileViewInteractionHub != null && fileViewInteractionHub.isMoveState()) {
//            fileInfo.Selected = fileViewInteractionHub.isFileSelected(fileInfo.filePath);
//        }
        if (fileViewInteractionHub != null) {
            fileInfo.Selected = fileViewInteractionHub.isCheckedFile(fileInfo.filePath);
        }

        ImageView checkbox = (ImageView) view.findViewById(R.id.file_checkbox);
        if (fileViewInteractionHub == null || fileViewInteractionHub.getMode() == Mode.Pick) {
            checkbox.setVisibility(View.GONE);
        } else {
            boolean showCheckBox = fileViewInteractionHub.canShowCheckBox();
            checkbox.setImageResource(fileInfo.Selected ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            checkbox.setTag(fileInfo);
            checkbox.setVisibility(showCheckBox ? View.VISIBLE : View.GONE);
            view.setSelected(fileInfo.Selected);
        }

        if (fileInfo instanceof MountFileInfo) {
            MountFileInfo mountInfo = (MountFileInfo) fileInfo;
            Util.setText(view, R.id.file_name, mountInfo.displayName);
            view.findViewById(R.id.file_count).setVisibility(View.GONE);

            view.findViewById(R.id.modified_time).setVisibility(View.GONE);
            view.findViewById(R.id.storage_status).setVisibility(View.VISIBLE);
            
            StringBuilder storageStatus = new StringBuilder();
            storageStatus.append(context.getString(R.string.sd_card_available, Util.convertStorage(mountInfo.freeSpace)));
            storageStatus.append("/");
            storageStatus.append(context.getString(R.string.sd_card_size, Util.convertStorage(mountInfo.totalSpace)));
            Util.setText(view, R.id.free_space, storageStatus.toString());
            view.findViewById(R.id.total_space).setVisibility(View.GONE);
//            Util.setText(view, R.id.free_space, context.getString(R.string.sd_card_available, Util.convertStorage(mountInfo.freeSpace)));
//            Util.setText(view, R.id.total_space, context.getString(R.string.sd_card_size, Util.convertStorage(mountInfo.totalSpace)));
            Util.setText(view, R.id.file_size, "");

            view.findViewById(R.id.file_image_frame).setVisibility(View.GONE);
            ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
            lFileImage.setImageDrawable(mountInfo.mountIcon);
            lFileImage.setAlpha((mountInfo.isHidden) ? HIDE_ICON_ALPHA : DEFAULT_ICON_ALPHA);
        } else {
            Util.setText(view, R.id.file_name, fileInfo.fileName);

            TextView ctView = (TextView) view.findViewById(R.id.file_count);
            ctView.setVisibility(View.VISIBLE);
            String countString = "";
            if (fileInfo.IsDir) {
                countString = "(" + fileInfo.Count + ")";
            }
            ctView.setText(countString);

            if (fileInfo.ModifiedDate > 0) {
                Util.setText(view, R.id.modified_time, Util.formatDateString(context, fileInfo.ModifiedDate));
                view.findViewById(R.id.modified_time).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.modified_time).setVisibility(View.GONE);
            }
            view.findViewById(R.id.storage_status).setVisibility(View.GONE);
            
            Util.setText(view, R.id.file_size, (fileInfo.IsDir ? "" : Util.convertStorage(fileInfo.fileSize)));

            ImageView lFileImage = (ImageView) view.findViewById(R.id.file_image);
            ImageView lFileImageFrame = (ImageView) view.findViewById(R.id.file_image_frame);

            lFileImage.setAlpha((fileInfo.isHidden) ? HIDE_ICON_ALPHA : DEFAULT_ICON_ALPHA);
            if (fileInfo.IsDir) {
                fileIcon.cancelLoadFileIcon(fileInfo, lFileImage);
                lFileImageFrame.setVisibility(View.GONE);
                lFileImage.setImageResource(R.drawable.folder);
            } else {
                fileIcon.setIcon(fileInfo, lFileImage, lFileImageFrame);
            }
        }
    }

    public static class FileItemOnClickListener implements OnClickListener {
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;

        public FileItemOnClickListener(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        @Override
        public void onClick(View v) {
            ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
            assert (img != null && img.getTag() != null);

            FileInfo tag = (FileInfo) img.getTag();
            tag.Selected = !tag.Selected;

            if (mFileViewInteractionHub.onCheckItem(tag, v)) {
                img.setImageResource(tag.Selected ? R.drawable.btn_check_on_holo_light
                        : R.drawable.btn_check_off_holo_light);
            } else {
                tag.Selected = !tag.Selected;
            }

            ActionMode actionMode = ((FileExplorerTabActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileExplorerTabActivity) mContext)
                        .startActionMode(mFileViewInteractionHub.getActionModeCallback());
                ((FileExplorerTabActivity) mContext).setActionMode(actionMode);
                mFileViewInteractionHub.afterChangedToActionMode(tag);
            } else {
                actionMode.invalidate();
            }

//            Util.updateActionModeTitle(actionMode, mContext,
//                    mFileViewInteractionHub.getSelectedFileList().size());
            mFileViewInteractionHub.getActionModeCallback().updateActionModeUI();
        }
    }

    public static class ModeCallback implements ActionMode.Callback {
        private Menu mMenu;
        private Context mContext;
        private FileViewInteractionHub mFileViewInteractionHub;
        private BottomActionBar mBottomActionBar;

        private TextView mTitleCountText;
        private Button mSelectBtn;
        private Button mCancelBtn;
        private ActionMode mActionMode;

        private void initMenuItemSelectAllOrCancel() {
            if (mMenu == null)
                return;
            //boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
            mMenu.findItem(R.id.action_cancel).setVisible(false); //isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(false); //!isSelectedAll);
            mMenu.findItem(R.id.action_copy_path).setVisible(false);
            
            if (!mContext.getResources().getBoolean(R.bool.have_cloud_file))
                mMenu.findItem(R.id.action_send_to_cloud).setVisible(false);
        }

        private void scrollToSDcardTab() {
            if (mBottomActionBar != null)
            {
                FileExplorerTabActivity activity = ((FileExplorerTabActivity) mContext);
                if (activity.getSelectedPageIndex() != Util.SDCARD_TAB_INDEX)
                    activity.switchToPage(Util.SDCARD_TAB_INDEX);
            } else
            {
                ActionBar bar = ((FileExplorerTabActivity) mContext).getActionBar();
                if (bar.getSelectedNavigationIndex() != Util.SDCARD_TAB_INDEX) {
                    bar.setSelectedNavigationItem(Util.SDCARD_TAB_INDEX);
                }
            }
        }

        public ModeCallback(Context context,
                FileViewInteractionHub fileViewInteractionHub) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
        }

        public ModeCallback(Context context,
                FileViewInteractionHub fileViewInteractionHub, View myActionBar) {
            mContext = context;
            mFileViewInteractionHub = fileViewInteractionHub;
            mBottomActionBar = (BottomActionBar) myActionBar;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (menu != null)
            {
                MenuInflater inflater = ((Activity) mContext).getMenuInflater();
                mMenu = menu;
                inflater.inflate(R.menu.operation_menu, mMenu);
                initMenuItemSelectAllOrCancel();
            }

            mActionMode = mode;
            View customView = ((IActionTabListener) mContext).getActionModeCustomView();
            mode.setCustomView(customView);

            OnClickListener listener = new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (v.getId())
                    {
                        case R.id.select_unselect_all:
                            clickSelectButton();
                            break;

                        case R.id.cancel:
                            mActionMode.finish();
                        default:
                            break;
                    }
                }
            };
            mTitleCountText = (TextView) customView.findViewById(R.id.title_select_count);
            mSelectBtn = (Button) customView.findViewById(R.id.select_unselect_all);
            mSelectBtn.setOnClickListener(listener);
            mCancelBtn = (Button) customView.findViewById(R.id.cancel);
            mCancelBtn.setOnClickListener(listener);

            initBottomActionBar();
            return true;
        }

        private void initBottomActionBar()
        {
            if (mBottomActionBar != null)
            {
                mBottomActionBar.clearAllButtons();
                mBottomActionBar.setNormalDisplayCount(mContext.getResources().getInteger(R.integer.bottom_bar_edit_item_count));

                Resources rs = mContext.getResources();
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_send_to_cloud), rs.getDrawable(R.drawable.operation_button_upload), R.id.action_send_to_cloud);
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_copy), rs.getDrawable(R.drawable.operation_button_copy), R.id.action_copy);
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_move), rs.getDrawable(R.drawable.operation_button_move), R.id.action_move);
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_delete), rs.getDrawable(R.drawable.operation_button_delete), R.id.action_delete);
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_send), rs.getDrawable(R.drawable.operation_button_send), R.id.action_send);
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_info), null, R.id.action_detail);
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_rename), null, R.id.action_rename);

                mBottomActionBar.refresh();
                mBottomActionBar.setBottomActionBarListerner(mBottomActionBarListener);
                
                if (mMenu != null)
                    mMenu.clear();
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            int selectedCount = mFileViewInteractionHub.getSelectedCount();
            boolean isSingeFileChecked = (selectedCount == 1);
            boolean canOperation = (selectedCount > 0);
            boolean canSend = !mFileViewInteractionHub.isSelectedHasFolder() && canOperation;
            if (mBottomActionBar != null)
            {
                mBottomActionBar.setItemEnable(R.id.action_detail, isSingeFileChecked);
                mBottomActionBar.setItemEnable(R.id.action_rename, isSingeFileChecked);
                mBottomActionBar.setItemEnable(R.id.action_send, canSend);
                if (mContext.getResources().getBoolean(R.bool.cloud_file_can_upload_file))
                {
                    mBottomActionBar.setItemEnable(R.id.action_send_to_cloud, canSend);
                } else
                {
                    mBottomActionBar.setItemVisible(R.id.action_send_to_cloud, false);
                }

                mBottomActionBar.setItemEnable(R.id.action_copy, canOperation);
                mBottomActionBar.setItemEnable(R.id.action_move, canOperation);
                mBottomActionBar.setItemEnable(R.id.action_delete, canOperation);
                mBottomActionBar.refresh();
                return true;
            } else
            {

            //boolean isSelectedAll = mFileViewInteractionHub.isSelectedAll();
//            mMenu.findItem(R.id.action_copy_path).setVisible(
//                    mFileViewInteractionHub.getSelectedFileList().size() == 1);
            mMenu.findItem(R.id.action_cancel).setVisible(false); //isSelectedAll);
            mMenu.findItem(R.id.action_select_all).setVisible(false); //!isSelectedAll);
            
            mMenu.findItem(R.id.action_detail).setEnabled(isSingeFileChecked);
            mMenu.findItem(R.id.action_rename).setEnabled(isSingeFileChecked);
            
            mMenu.findItem(R.id.action_send).setEnabled(canOperation);
            mMenu.findItem(R.id.action_send_to_cloud).setEnabled(canOperation);
            
            return true;
            }
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return onMyItemClicked(mode, item.getItemId());
        }
        
        private boolean onMyItemClicked(ActionMode mode, int itemId)
        {
            switch (itemId) {
                case R.id.action_delete:
                    mFileViewInteractionHub.onOperationDelete();
                    mode.finish();
                    break;
                case R.id.action_copy:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .copyFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_move:
                    ((FileViewActivity) ((FileExplorerTabActivity) mContext)
                            .getFragment(Util.SDCARD_TAB_INDEX))
                            .moveToFile(mFileViewInteractionHub.getSelectedFileList());
                    mode.finish();
                    scrollToSDcardTab();
                    break;
                case R.id.action_send:
                    mFileViewInteractionHub.onOperationSend();
                    //mode.finish(); //STGOO-650
                    break;
                case R.id.action_send_to_cloud:
                    mFileViewInteractionHub.onOperationSendToCloud();
                    break;
                case R.id.action_copy_path:
                    mFileViewInteractionHub.onOperationCopyPath();
                    mode.finish();
                    break;
                case R.id.action_cancel:
                    mFileViewInteractionHub.clearSelection();
                    initMenuItemSelectAllOrCancel();
                    mode.finish();
                    break;
                case R.id.action_select_all:
                    mFileViewInteractionHub.onOperationSelectAll();
                    initMenuItemSelectAllOrCancel();
                    break;
                    
                case R.id.action_detail:
                    mFileViewInteractionHub.onOperationInfo();
                    break;
                case R.id.action_rename:
                    mFileViewInteractionHub.onOperationRename();
                    break;
            }
            //Util.updateActionModeTitle(mode, mContext, mFileViewInteractionHub.getSelectedCount());
            updateActionModeUI();
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mFileViewInteractionHub.clearSelection();
            ((FileExplorerTabActivity) mContext).setActionMode(null);
            ((FileExplorerTabActivity) mContext).invalidateOptionsMenu(); //STGOO-2064 
            
            if (mBottomActionBar != null)
            {
                mBottomActionBar.clearAllButtons();

                // To restore normal bottom action bar.
                mFileViewInteractionHub.onCreateOptionsMenu(null);
                mFileViewInteractionHub.onPrepareButtomActionBar();
            }
        }
        
        public void clickSelectButton()
        {
            boolean isAllChecked = mFileViewInteractionHub.isSelectedAll();
            if (isAllChecked)
            {
                mFileViewInteractionHub.clearSelection();
            } else
            {
                mFileViewInteractionHub.onOperationSelectAll();
            }
            if (mBottomActionBar != null)
            {
                onPrepareActionMode(mActionMode, null);
            }
            updateActionModeUI();
        }

        public void updateActionModeUI()
        {
            boolean isAllChecked = mFileViewInteractionHub.isSelectedAll();
            int count = mFileViewInteractionHub.getSelectedCount();

            mTitleCountText.setText(mContext.getString(R.string.multi_select_title, count));
            mSelectBtn.setText(isAllChecked ? R.string.operation_cancel_selectall : R.string.operation_selectall);
        }
        
        private BottomActionBarListener mBottomActionBarListener = new BottomActionBarListener()
        {
            @Override
            public boolean onActionItemClick(int itemId)
            {
                return onMyItemClicked(mActionMode, itemId);
            }
        };
    }
}

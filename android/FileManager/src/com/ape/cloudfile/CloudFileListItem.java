package com.ape.cloudfile;

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

import com.ape.cloudfile.transfer.TransferFilesActivity;
import com.ape.cloudfile.widget.BottomActionBar;
import com.ape.cloudfile.widget.BottomActionBar.BottomActionBarListener;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.filemanager.FileIconHelper;
import com.ape.filemanager.FileInfo;
import com.ape.filemanager.IActionTabListener;
import com.ape.filemanager.R;
import com.ape.filemanager.Util;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.MissionObject;

public class CloudFileListItem<T>
{
//    private static final float DEFAULT_ICON_ALPHA = 1f;
//    private static final float HIDE_ICON_ALPHA = 0.3f;
    private Context mContext;
    private CloudFileListAdapter<T> mAdapter;
    private OnClickListener mCheckBoxListener;
    
    public CloudFileListItem(Context context, CloudFileListAdapter<T> listAdapter)
    {
        mContext = context;
        mAdapter = listAdapter;
        
        mCheckBoxListener = new FileItemOnClickListener();
    }

    public void setupFileListItemInfo(View view, CloudFile fileInfo, CloudFileIconHelper iconHelper)
    {
        //CloudFile icon.
        ImageView fileImageView = (ImageView) view.findViewById(R.id.file_image);
        iconHelper.setIcon(fileInfo, fileImageView);

        //CloudFile name.
        Util.setText(view, R.id.file_name, fileInfo.getName());

        //CloudFile modify time.
        long time = fileInfo.getModifyTime();
        if (time > 0)
        {
            Util.setText(view, R.id.modified_time, Util.formatDateString(mContext, time));
            view.findViewById(R.id.modified_time).setVisibility(View.VISIBLE);
        } else
        {
            view.findViewById(R.id.modified_time).setVisibility(View.GONE);
        }

        //CloudFile size.
        String sizeStr = fileInfo.isFile() ? Util.convertStorage(fileInfo.getLength()) : "";
        Util.setText(view, R.id.file_size, sizeStr);

        //CloudFile checkbox.
        View checkView = view.findViewById(R.id.file_checkbox_area);
        ImageView checkbox = (ImageView) checkView.findViewById(R.id.file_checkbox);
        if (mAdapter.isCheckedMode())
        {
            boolean isChecked = mAdapter.isFileChecked((T)fileInfo);
            checkbox.setImageResource(isChecked ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            checkbox.setTag(fileInfo);
            checkbox.setOnClickListener(mCheckBoxListener);
            checkView.setVisibility(View.VISIBLE);
        } else
        {
            checkbox.setTag(fileInfo);
            checkView.setVisibility(View.GONE);
        }
    }
    
    public void setupMissionItemInfo(View view, MissionObject mission)
    {
        FileInfo fileInfo = Util.GetFileInfo(mission.getLocalFile());
        
        if (fileInfo == null)
        {
            fileInfo = new FileInfo();
            fileInfo.IsDir = false;
            fileInfo.fileName = Util.getNameFromFilepath(mission.getKey());
            fileInfo.fileSize = mission.getFileLength();
        }

        //File icon.
        ImageView fileImageView = (ImageView) view.findViewById(R.id.file_image);
        if (fileInfo.IsDir)
        {
            fileImageView.setImageResource(R.drawable.folder);

        } else
        {
            String ext = Util.getExtFromFilename(fileInfo.fileName);
            int iconId = FileIconHelper.getFileIcon(ext);
            fileImageView.setImageResource(iconId);
        }
        
        //File name
        Util.setText(view, R.id.file_name, fileInfo.fileName);
        
        //CloudFile transfer time and size
        TextView timeText = (TextView) view.findViewById(R.id.modified_time);
        TextView sizeText = (TextView) view.findViewById(R.id.file_size);
        RoundProgressBar progressBar = (RoundProgressBar) view.findViewById(R.id.roundProgressBar);
        //mAdapter.removeProgressBarInfo(progressBar);
        if (mission.isFinished())
        {
            long time = mission.getLastTime();
            if (time > 0)
            {
                timeText.setText(Util.formatDateString(mContext, time));
                timeText.setVisibility(View.VISIBLE);
            } else
            {
                timeText.setVisibility(View.INVISIBLE);
            }
            
            //CloudFile size.
            String sizeStr = Util.convertStorage(fileInfo.fileSize);
            sizeText.setText(sizeStr);

            progressBar.setVisibility(View.INVISIBLE);
            progressBar.setTag(R.id.tag_mission, null);
            progressBar.setTag(R.id.tag_sizeview, null);
        } else
        {
            timeText.setVisibility(View.INVISIBLE);
            
            StringBuilder fileSize = new StringBuilder();
            fileSize.append(Util.convertStorage(mission.getTransferredLength()));
            fileSize.append("/");
            fileSize.append(Util.convertStorage(mission.getFileLength()));
            sizeText.setText(fileSize.toString());
            
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setTag(R.id.tag_mission, mission);
            progressBar.setTag(R.id.tag_sizeview, sizeText);
            mAdapter.setProgressBarInfo(progressBar, mission);
        }

        //CloudFile checkbox.
        View checkView = view.findViewById(R.id.file_checkbox_area);
        ImageView checkbox = (ImageView) checkView.findViewById(R.id.file_checkbox);
        if (mAdapter.isCheckedMode())
        {
            boolean isChecked = mAdapter.isFileChecked((T)mission);
            checkbox.setImageResource(isChecked ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            checkbox.setOnClickListener(mCheckBoxListener);
            checkbox.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else
        {
            checkbox.setVisibility(View.INVISIBLE);
        }
        checkbox.setTag(mission);
    }


    public static <T> boolean onClickCheckedFileItem(View v, CloudFileListAdapter<T> adapter)
    {
        ImageView img = (ImageView) v.findViewById(R.id.file_checkbox);
        assert (img != null && img.getTag() != null);
        
        T fileInfo = (T) img.getTag();
        boolean isChecked = adapter.isFileChecked(fileInfo);

        isChecked = !isChecked;
        if (isChecked) {
            adapter.addCheckedFile(fileInfo);
        }
        else {
            adapter.removeCheckedFile(fileInfo);
        }

        img.setImageResource(isChecked ? R.drawable.btn_check_on_holo_light
                : R.drawable.btn_check_off_holo_light);
        return isChecked;
    }

    public class FileItemOnClickListener implements OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            IActionTabListener actionActity = ((IActionTabListener) mContext);
            onClickCheckedFileItem(v, mAdapter);

            ActionMode actionMode = actionActity.getActionMode();
            if (actionMode == null)
            {
//                actionMode = actionActity.startActionMode(new ModeCallback<T>(mContext, mAdapter));
//                actionActity.setActionMode(actionMode);
            } else
            {
                actionMode.invalidate();
            }

            //Util.updateActionModeTitle(actionMode, mContext, mAdapter.getCheckedFileCount());
            ModeCallback callback = (ModeCallback) actionActity.getActionModeCallback();
            callback.updateActionModeUI();
        }
        
    }
    
    public static class ModeCallback<E> implements ActionMode.Callback
    {
        private Menu mMenu;
        private Context mContext;
        private CloudFileListAdapter<E> mAdapter;
        private BottomActionBar mBottomActionBar;
        
        private TextView mTitleCountText;
        private Button mSelectBtn;
        private Button mCancelBtn;
        private ActionMode mActionMode;

        public ModeCallback(Context context, CloudFileListAdapter<E> listAdapter)
        {
            mContext = context;
            mAdapter = listAdapter;
        }

        public ModeCallback(Context context, CloudFileListAdapter<E> listAdapter, BottomActionBar bottomBar)
        {
            mContext = context;
            mAdapter = listAdapter;
            mBottomActionBar = bottomBar;
        }

        private void initMenuItemSelectAllOrCancel()
        {
            //boolean isAllChecked = mAdapter.isAllFilesChecked();
            mMenu.findItem(R.id.action_cancel).setVisible(false);
            mMenu.findItem(R.id.action_select_all).setVisible(false);
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            if (menu != null)
            {
                MenuInflater inflater = ((Activity) mContext).getMenuInflater();
                mMenu = menu;
                inflater.inflate(R.menu.cloudfile_menu, mMenu);
                initMenuItemSelectAllOrCancel();
                
                if (mContext.getClass() == TransferFilesActivity.class)
                {
                    mMenu.findItem(R.id.action_download).setVisible(false);
                }
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
                mBottomActionBar.setVisibility(View.VISIBLE);
                mBottomActionBar.clearAllButtons();
                mBottomActionBar.setNormalDisplayCount(mContext.getResources().getInteger(R.integer.bottom_bar_edit_item_count));

                Resources rs = mContext.getResources();
                mBottomActionBar.addMenuItem(rs.getString(R.string.operation_delete), rs.getDrawable(R.drawable.operation_button_delete), R.id.action_delete);
                mBottomActionBar.addMenuItem(rs.getString(R.string.menu_download_str), rs.getDrawable(R.drawable.operation_button_download), R.id.action_download);
                if (mContext.getClass() == TransferFilesActivity.class)
                {
                    mBottomActionBar.findItem(R.id.action_download).setVisible(false);
                }
                mBottomActionBar.refresh();
                mBottomActionBar.setBottomActionBarListerner(mBottomActionBarListener);
                
                if (mMenu != null)
                    mMenu.clear();
            }
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            if (mBottomActionBar != null)
            {
                boolean canOperation = (mAdapter.getCheckedFileCount() > 0);
                mBottomActionBar.setItemEnable(R.id.action_delete, canOperation);
                mBottomActionBar.setItemEnable(R.id.action_download, canOperation);
                mBottomActionBar.refresh();
            } else
            {
                initMenuItemSelectAllOrCancel();
            }

            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            return onMyItemClicked(mode, item.getItemId());
        }

        private boolean onMyItemClicked(ActionMode mode, int itemId)
        {
            switch (itemId)
            {
                case R.id.action_delete:
                    mAdapter.deleteCheckedFiles();
                    //mode.finish();
                    break;

                case R.id.action_download:
                    if (mAdapter.downloadCheckedFiles())
                    {
                        mode.finish();
                    }
                    break;
                    
                case R.id.action_cancel:
                    mAdapter.clearCheckedFiles();
                    mAdapter.notifyDataSetChanged();
                    initMenuItemSelectAllOrCancel();
                    mode.finish();
                    break;
                    
                case R.id.action_select_all:
                    mAdapter.addAllCheckedFiles();
                    mAdapter.notifyDataSetChanged();
                    initMenuItemSelectAllOrCancel();
                    break;
                    
                default:
                    return false;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            mAdapter.clearCheckedFiles();
            mAdapter.notifyDataSetChanged();

            ((IActionTabListener) mContext).setActionMode(null);
            
            if (mBottomActionBar != null)
            {
                mBottomActionBar.clearAllButtons();
                mAdapter.onActionModeFinished();
            }
        }

        public void clickSelectButton()
        {
            boolean isAllChecked = mAdapter.isAllFilesChecked();
            if (isAllChecked)
            {
                mAdapter.clearCheckedFiles();
            } else
            {
                mAdapter.addAllCheckedFiles();
            }
            if (mBottomActionBar != null)
            {
                onPrepareActionMode(mActionMode, null);
            }
            mAdapter.notifyDataSetChanged();
            updateActionModeUI();
        }
        
        public void updateActionModeUI()
        {
            boolean isAllChecked = mAdapter.isAllFilesChecked();
            int count = mAdapter.getCheckedFileCount();
            
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

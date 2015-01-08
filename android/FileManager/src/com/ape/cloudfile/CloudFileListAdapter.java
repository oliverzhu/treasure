package com.ape.cloudfile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ape.cloudfile.widget.CloudListView;
import com.ape.cloudfile.widget.RoundProgressBar;
import com.ape.filemanager.R;
import com.cloud.client.file.CloudFile;
import com.cloud.client.file.MissionObject;

public class CloudFileListAdapter<T> extends BaseAdapter
{
    private Context mContext;
    private ArrayList<T> mFileList;
    private HashSet<T> mCheckedFiles;

    private CloudFileListItem<T> mItemHelper;
    private CloudFileIconHelper mIconHelper;
    
    private CloudListView mListView;
    private int mListTitleCount;
    
    private boolean mIsPullToRefreshList = false;

    public interface ListItemOperationListener
    {
        boolean downloadFiles();
        
        void deleteFiles();

        boolean isCheckedMode();
        
        void setProgressBarInfo(RoundProgressBar bar, MissionObject mission);
        
        void removeProgressBarInfo(RoundProgressBar bar);
        
        void onActionModeFinished();
    }

    public CloudFileListAdapter(Context context, ArrayList<T> files)
    {
        mContext = context;
        mFileList = files;
        mCheckedFiles = new HashSet<T>();
        mListTitleCount = 0;
        
        mItemHelper = new CloudFileListItem<T>(mContext, this);
        mIconHelper = new CloudFileIconHelper(mContext);
    }

    public void exit()
    {
        mIconHelper.exit();
    }

    public void pauseIconLoader()
    {
        mIconHelper.pauseIconLoader();
    }

    public void resumeIconLoader()
    {
        mIconHelper.resumeIconLoader();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;
        if (convertView != null)
        {
            view = convertView;
        } else
        {
            view = LayoutInflater.from(mContext).inflate(
                    R.layout.cloud_file_item, parent, false);
        }

        Object fileInfo = mFileList.get(position);
        if (fileInfo.getClass() == CloudFile.class)
        {
            mItemHelper.setupFileListItemInfo(view, (CloudFile) fileInfo, mIconHelper);
        } else if (fileInfo.getClass() == MissionObject.class)
        {
            MissionObject mission = (MissionObject) fileInfo;
            View listTitle = view.findViewById(R.id.list_title_view);
            View listItem = view.findViewById(R.id.list_item_content_view);
            if (mission.getId() == CloudFileUtil.TRANSFER_LIST_TITLE_ID)
            {
                listTitle.setVisibility(View.VISIBLE);
                listItem.setVisibility(View.GONE);
                TextView listTitleText = (TextView) listTitle.findViewById(R.id.list_title);
                listTitleText.setText(mission.getKey());
            } else
            {
                listTitle.setVisibility(View.GONE);
                listItem.setVisibility(View.VISIBLE);
                if (mListView !=null && !mListView.isInMeasure())
                {
                    mItemHelper.setupMissionItemInfo(view, mission);
                }

            }
        }

        return view;
    }
    
    @Override
    public int getCount()
    {
        return mFileList.size();
    }

    @Override
    public T getItem(int position)
    {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    public ArrayList<T> getCheckedFiles()
    {
        ArrayList<T> retList = new ArrayList<T>();
        
        Iterator<T> it = mCheckedFiles.iterator();
        while (it.hasNext()) 
        {
            retList.add(it.next());
        }

        return retList;
    }

    public int getCheckedFileCount()
    {
        return mCheckedFiles.size();
    }

    public boolean isAllFilesChecked()
    {
        if (mFileList.size() != 0)
        {
            return (mFileList.size() <= mCheckedFiles.size() + mListTitleCount);
        }
        
        return false;
    }

    public boolean isNoCheckedFile()
    {
        return (mCheckedFiles.size() < 1);
    }
    
    public void clearCheckedFiles()
    {
        mCheckedFiles.clear();
    }
    
    public boolean addCheckedFile(T file)
    {
        if (file.getClass() == MissionObject.class)
        {
            MissionObject mission = (MissionObject) file;
            if (mission.getId() == CloudFileUtil.TRANSFER_LIST_TITLE_ID)
                return false;
        }
        return mCheckedFiles.add(file);
    }
    
    public boolean addAllCheckedFiles(ArrayList<T> files)
    {
        mCheckedFiles.clear();
        for (T file : files)
        {
            addCheckedFile(file);
        }

        return true;
    }
    
    public boolean addAllCheckedFiles()
    {
        if (mFileList.size() < 1)
            return false;

        return addAllCheckedFiles(mFileList);
    }
    
    public boolean removeCheckedFile(T file)
    {
        return mCheckedFiles.remove(file);
    }
    
    public boolean isFileChecked(T file)
    {
        return mCheckedFiles.contains(file);
    }

    private ListItemOperationListener mOperationListener;
    public void setOperationListener(ListItemOperationListener listener)
    {
        mOperationListener = listener;
    }
    
    public boolean downloadCheckedFiles()
    {
        if (mOperationListener != null)
        {
            return mOperationListener.downloadFiles();
        }
        
        return false;
    }
    
    public void deleteCheckedFiles()
    {
        if (mOperationListener != null)
        {
            mOperationListener.deleteFiles();
        }
    }
    
    public boolean isCheckedMode()
    {
        if (mOperationListener != null)
        {
            return mOperationListener.isCheckedMode();
        }
        return false;
    }
    
    public void setProgressBarInfo(RoundProgressBar bar, MissionObject mission)
    {
        if (mOperationListener != null)
        {
            mOperationListener.setProgressBarInfo(bar, mission);
        }
    }
    
    public void removeProgressBarInfo(RoundProgressBar bar)
    {
        if (mOperationListener != null)
        {
            mOperationListener.removeProgressBarInfo(bar);
        }
    }
    
    public void onActionModeFinished()
    {
        if (mOperationListener != null)
        {
            mOperationListener.onActionModeFinished();
        }
    }
    
    public int getListTitleCount()
    {
        return mListTitleCount;
    }

    public void setListTitleCount(int listTitleCount)
    {
        mListTitleCount = listTitleCount;
    }
    
    public void setListView(CloudListView listView)
    {
        mListView = listView;
    }

    public void setIsPullToRefreshList(boolean isPull)
    {
        mIsPullToRefreshList = isPull;
    }

    public boolean isPullToRefreshList()
    {
        return mIsPullToRefreshList;
    }
}

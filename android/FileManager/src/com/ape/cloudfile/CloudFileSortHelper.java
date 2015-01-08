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

package com.ape.cloudfile;

import java.util.Comparator;
import java.util.HashMap;

import com.ape.filemanager.Util;
import com.cloud.client.file.CloudFile;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class CloudFileSortHelper
{

    public enum SortMethod
    {
        name, size, date, type
    }

    private SortMethod mSort;

    private boolean mFileFirst;

    private HashMap<SortMethod, Comparator<CloudFile>> mComparatorList = 
            new HashMap<SortMethod, Comparator<CloudFile>>();

    private Context mContext;
    private static final int SORT_NAME_VALUE = 0;
    private static final int SORT_SIZE_VALUE = 1;
    private static final int SORT_DATE_VALUE = 2;
    private static final int SORT_TYPE_VALUE = 3;
    private static final String SORT_KEY = "sort_type";

    public CloudFileSortHelper(Context context)
    {
        mContext = context;

        updateSortMethod();
        mComparatorList.put(SortMethod.name, cmpName);
        mComparatorList.put(SortMethod.size, cmpSize);
        mComparatorList.put(SortMethod.date, cmpDate);
        mComparatorList.put(SortMethod.type, cmpType);
    }

    public void setSortMethod(SortMethod s)
    {
        mSort = s;
        SharedPreferences.Editor editor = PreferenceManager
                .getDefaultSharedPreferences(mContext).edit();
        editor.putInt(SORT_KEY, getValueFromMethod(mSort));
        editor.commit();
    }

    public SortMethod getSortMethod()
    {
        updateSortMethod();
        return mSort;
    }

    public void updateSortMethod()
    {
        int sortValue = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(SORT_KEY, SORT_NAME_VALUE);
        mSort = getMethodFromValue(sortValue);
    }

    public void setFileFirst(boolean f)
    {
        mFileFirst = f;
    }

    public Comparator<CloudFile> getComparator()
    {
        return mComparatorList.get(mSort);
    }

    private abstract class FileComparator implements Comparator<CloudFile>
    {

        @Override
        public int compare(CloudFile object1, CloudFile object2)
        {
            if (object1.isFile() == object2.isFile())
            {
                return doCompare(object1, object2);
            }

            if (mFileFirst)
            {
                // the files are listed before the dirs
                return (object1.isFile() ? -1 : 1);
            } else
            {
                // the dir-s are listed before the files
                return object1.isFile() ? 1 : -1;
            }
        }

        protected abstract int doCompare(CloudFile object1, CloudFile object2);
    }

    private Comparator<CloudFile> cmpName = new FileComparator()
    {
        @Override
        public int doCompare(CloudFile object1, CloudFile object2)
        {
            return object1.getName().compareToIgnoreCase(object2.getName());
        }
    };

    private Comparator<CloudFile> cmpSize = new FileComparator()
    {
        @Override
        public int doCompare(CloudFile object1, CloudFile object2)
        {
            return longToCompareInt(object2.getLength() - object1.getLength());
        }
    };

    private Comparator<CloudFile> cmpDate = new FileComparator()
    {
        @Override
        public int doCompare(CloudFile object1, CloudFile object2)
        {
            return longToCompareInt(object2.getModifyTime()
                    - object1.getModifyTime());
        }
    };

    private int longToCompareInt(long result)
    {
        return result > 0 ? 1 : (result < 0 ? -1 : 0);
    }

    private Comparator<CloudFile> cmpType = new FileComparator()
    {
        @Override
        public int doCompare(CloudFile object1, CloudFile object2)
        {
            int result = Util.getExtFromFilename(object1.getName())
                    .compareToIgnoreCase(
                            Util.getExtFromFilename(object2.getName()));
            if (result != 0)
                return result;

            return Util.getNameFromFilename(object1.getName())
                    .compareToIgnoreCase(
                            Util.getNameFromFilename(object2.getName()));
        }
    };

    private int getValueFromMethod(SortMethod s)
    {
        switch (s)
        {
            case name:
                return SORT_NAME_VALUE;
            case size:
                return SORT_SIZE_VALUE;
            case date:
                return SORT_DATE_VALUE;
            case type:
                return SORT_TYPE_VALUE;
        }
        return SORT_NAME_VALUE;
    }

    private SortMethod getMethodFromValue(int value)
    {
        switch (value)
        {
            case SORT_NAME_VALUE:
                return SortMethod.name;
            case SORT_SIZE_VALUE:
                return SortMethod.size;
            case SORT_DATE_VALUE:
                return SortMethod.date;
            case SORT_TYPE_VALUE:
                return SortMethod.type;
        }
        return SortMethod.name;
    }

    public int getSortIndex()
    {
        updateSortMethod();
        return getValueFromMethod(mSort);
    }
}

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

import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FileSortHelper {

    public enum SortMethod {
        name, size, date, type
    }

    private SortMethod mSort;

    private boolean mFileFirst;

    private HashMap<SortMethod, Comparator> mComparatorList = new HashMap<SortMethod, Comparator>();

    private Context mContext;
    private static final int SORT_NAME_VALUE = 0;
    private static final int SORT_SIZE_VALUE = 1;
    private static final int SORT_DATE_VALUE = 2;
    private static final int SORT_TYPE_VALUE = 3;
    private static final String SORT_KEY = "sort_type";
    
    static private FileSortHelper s_instance;
    
    static public FileSortHelper getInstance(Context context)
    {
        if (s_instance == null)
        {
            s_instance = new FileSortHelper(context);
        }
        
        return s_instance;
    }


    private FileSortHelper(Context context) {
        mContext = context;

        updateSortMethod();
        mComparatorList.put(SortMethod.name, cmpName);
        mComparatorList.put(SortMethod.size, cmpSize);
        mComparatorList.put(SortMethod.date, cmpDate);
        mComparatorList.put(SortMethod.type, cmpType);
    }

    public void setSortMethod(SortMethod s) {
        mSort = s;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(mContext).edit();
        editor.putInt(SORT_KEY, getValueFromMethod(mSort));
        editor.commit();
    }

    public SortMethod getSortMethod() {
        updateSortMethod();
        return mSort;
    }

    public void updateSortMethod()
    {
        int sortValue = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getInt(SORT_KEY, SORT_NAME_VALUE);
        mSort = getMethodFromValue(sortValue);
    }

    public void setFileFirst(boolean f) {
        mFileFirst = f;
    }

    public Comparator getComparator() {
        return mComparatorList.get(mSort);
    }

    private abstract class FileComparator implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo object1, FileInfo object2) {
            if (object1.IsDir == object2.IsDir) {
                return doCompare(object1, object2);
            }

            if (mFileFirst) {
                // the files are listed before the dirs
                return (object1.IsDir ? 1 : -1);
            } else {
                // the dir-s are listed before the files
                return object1.IsDir ? -1 : 1;
            }
        }

        protected abstract int doCompare(FileInfo object1, FileInfo object2);
    }

    private Comparator cmpName = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            return object1.fileName.compareToIgnoreCase(object2.fileName);
        }
    };

    private Comparator cmpSize = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            return longToCompareInt(object2.fileSize - object1.fileSize);
        }
    };

    private Comparator cmpDate = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            return longToCompareInt(object2.ModifiedDate - object1.ModifiedDate);
        }
    };

    private int longToCompareInt(long result) {
        return result > 0 ? 1 : (result < 0 ? -1 : 0);
    }

    private Comparator cmpType = new FileComparator() {
        @Override
        public int doCompare(FileInfo object1, FileInfo object2) {
            int result = Util.getExtFromFilename(object1.fileName).compareToIgnoreCase(
                    Util.getExtFromFilename(object2.fileName));
            if (result != 0)
                return result;

            return Util.getNameFromFilename(object1.fileName).compareToIgnoreCase(
                    Util.getNameFromFilename(object2.fileName));
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

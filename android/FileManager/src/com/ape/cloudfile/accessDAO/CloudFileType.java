package com.ape.cloudfile.accessDAO;

import com.ape.filemanager.FileCategoryHelper;
import com.ape.filemanager.FileCategoryHelper.FileCategory;

public class CloudFileType
{
    public static int getCloudFileType(String cloudPath)
    {
        FileCategory fc = FileCategoryHelper.getCategoryFromPath(cloudPath);

        if (cloudPath.endsWith("/"))
            return 0;

        return fc.ordinal();
    }

}

package com.ape.filemanager;

import android.graphics.drawable.Drawable;

public class MountFileInfo extends FileInfo
{
    // for SD information.
    public String displayName;
    public Drawable mountIcon;
    public long freeSpace;
    public long totalSpace;
    public boolean isExternal;
}

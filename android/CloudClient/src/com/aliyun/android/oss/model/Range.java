/**
 * Copyright (c) 2012 The Wiseserc. All rights reserved.
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 */
package com.aliyun.android.oss.model;

/**
 * 范围类
 * 
 * @author Michael
 */
public class Range {
    /**
     * 起始
     */
    private long start;
    
    /**
     * 终止
     */
    private long end;
    
    /**
     * 构造新实例
     */
    public Range (long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * to string
     */
    public String toString() {
        StringBuffer range = new StringBuffer();
        if (start >= 0L) {
            range.append(start);
        }
        range.append("-");
        if (end >= 0L) {
            range.append(end);
        }
        return range.toString();
    }
    
    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}

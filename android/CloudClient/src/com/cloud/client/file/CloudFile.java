package com.cloud.client.file;

public class CloudFile {
    private boolean isFile;
//    private boolean isChecked;
    private long length;
    private long modifyTime;
    private long accessTime;
    private String path;
    private String name;
    private String key;
    
    private long transferredLength = 0L;
    private String localPath;

    public CloudFile(String key, String path, String name, 
            long length, long modifyTime, boolean isFile) {
        this.key = key;
        this.path = path;
        this.name = name;
        this.length = length;
        this.modifyTime = modifyTime;
        this.isFile = isFile;
    }
    
    public CloudFile(String key, String localPath, 
            long initiatedTime, long length, long transferredLength) {
        this.key = key;
        this.localPath = localPath;
        this.modifyTime = initiatedTime;
        this.length = length;
        this.transferredLength = transferredLength;
        init(key);
    }
    
    private void init(String key) {
        String[] sqlit = key.split("/");
        name = sqlit[sqlit.length - 1];
        if (key.indexOf("/") >= 0) {
            path = key.substring(0, key.lastIndexOf("/"));
            if (path.equals("")) {
                path = "/";
            }
        } else {
            path = "/";
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CloudFile other = (CloudFile) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }

    public boolean isFile() {
        return this.isFile;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public long getLength() {
        return this.length;
    }

    public String getKey() {
        return this.key;
    }
    
    public long getModifyTime() {
        return this.modifyTime;
    }
    
    public long getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(long accessTime) {
        this.accessTime = accessTime;
    }
    
//    public void setChecked(boolean isChecked) {
//        this.isChecked = isChecked;
//    }
//    
//    public boolean getChecked() {
//        return this.isChecked;
//    }

    public long getTransferredLength() {
        return transferredLength;
    }

    public String getLocalPath() {
        return localPath;
    }
}

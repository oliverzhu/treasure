package com.cloud.client.file;

import com.aliyun.android.oss.model.OSSObjectSummary;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class FileNameUtil {

    private String mPrefixDir;
    private String mPackageName;

    public FileNameUtil(String prefixDir, String packageName) {
        if (packageName == null) {
            throw new RuntimeException("packageName should be set");
        }
        this.mPrefixDir = prefixDir;
        this.mPackageName = packageName;
    }
    
    /**
     * 获取用户{userId}的工作目录，此目录根据用户Id以及所在app生成。
     * 例：{other rule}/{packageName}/{userId}/
     * 
     * @param userId 用户Id
     * @return 用户{userId}的工作目录
     */
    public String getUserFolder(String userId) {
        String userFolder =
                (userId == null || userId.trim().length() == 0 
                || "/".equals(userId)) 
                ? "public": userId;
        return mPrefixDir + mPackageName + "/" + userFolder/* + "/"*/;
    }
    
    /**
     * 获取用户{userId}的在服务器上的操作对象，此对象为{@link getUserFolder()}与{key}的组合，
     * 用户可以使用此方法生成的对象做{downloadFile},{deleteFile}之类的操作
     * 例：{other rule}/{packageName}/{userId}/{key}
     * 
     * @param userId 用户Id
     * @param key 操作对象的相对地址
     * @return 用户{userId}在服务器上的对象
     */
    public String getUserFullPath(String userId, String key) {
        int index = key.indexOf('/');
        return index == 0 ? getUserFolder(userId) + key : 
            getUserFolder(userId) + "/" + key;
    }
    
    /**
     * 若{key}为文件则解析其文件名，若{key}为目录则解析其目录名
     * 
     * @param key
     * @return 文件名或目录名
     */
    public static String parseName(String key) {
        String[] sqlit = key.split("/");
        String name = sqlit[sqlit.length - 1];
        
        return name;
    }
    
    /**
     * 解析{key}的父目录
     * 
     * @param key
     * @return 目录名
     */
    public static String parseParentPath(String key) {
        if (key.endsWith("/")) {
            key = key.substring(0, key.lastIndexOf("/"));
        }
        
        String path = key.substring(0, key.lastIndexOf("/"));
        return path + "/";
    }
    
    /**
     * 由用户{userId}所在服务器上的对象，解析此用户的工作目录，
     * 即：{other rule}/{packageName}/{userId}/
     * 
     * @param userId 用户Id
     * @param fullKey 用户{userId}在服务器上的对象
     * @return 用户{userId}的工作目录
     */
    public String parseKeytoUserPath(String userId, String fullKey) {
        String userFolder = getUserFolder(userId);
        int userFolderLength = userFolder.length();
        int fullKeyLength = fullKey.length();
        
        String userPath;
        if (fullKeyLength < userFolderLength) {
            userPath = fullKey;
        } else {
            userPath = fullKey.substring(userFolderLength, fullKeyLength);
        }

        return userPath;
    }
    
    /**
     * 由{@link OSSObjectSummary}生成{@link CloudFile}对象
     * 
     * @param userId 用户Id
     * @param os {@link OSSObjectSummary}
     * @return {@link CloudFile}
     */
    public CloudFile parseAliOSSFile(String userId, OSSObjectSummary os) {
        String key = parseKeytoUserPath(userId, os.getKey());
        boolean isFile = !key.endsWith("/");
        long modifyTime = 0L;
        long length = 0L;
        String name = null;
        String path = null;

        if (os.getLastModified() != null) {
            modifyTime = os.getLastModified().getTime();
        }

        if (isFile) {
            String[] sqlit = key.split("/");
            name = sqlit[sqlit.length - 1];
            length = os.getSize();
            if (key.indexOf("/") >= 0) {
                path = key.substring(0, key.lastIndexOf("/"));
                if (path.equals("")) {
                    path = "/";
                }
            } else {
                path = "/";
            }
        } else {
            String tempKey = key.substring(0, key.length() - 1);
            String[] sqlit = tempKey.split("/");
            name = sqlit[sqlit.length - 1];
            length = 0;
            if (tempKey.indexOf("/") >= 0) {
                path = tempKey.substring(0, tempKey.lastIndexOf("/"));
                if (path.equals("")) {
                    path = "/";
                }
            } else {
                path = "/";
            }
        }

        return new CloudFile(key, path, name, length, modifyTime, isFile);
    }
    
    public CloudFile parseAmazonFile(String userId, S3ObjectSummary os) {
        String key = parseKeytoUserPath(userId, os.getKey());
        boolean isFile = !key.endsWith("/");
        long modifyTime = 0L;
        long length = 0L;
        String name = null;
        String path = null;

        if (os.getLastModified() != null) {
            modifyTime = os.getLastModified().getTime();
        }

        if (isFile) {
            String[] sqlit = key.split("/");
            name = sqlit[sqlit.length - 1];
            length = os.getSize();
            if (key.indexOf("/") >= 0) {
                path = key.substring(0, key.lastIndexOf("/"));
                if (path.equals("")) {
                    path = "/";
                }
            } else {
                path = "/";
            }
        } else {
            String tempKey = key.substring(0, key.length() - 1);
            String[] sqlit = tempKey.split("/");
            name = sqlit[sqlit.length - 1];
            length = 0;
            if (tempKey.indexOf("/") >= 0) {
                path = tempKey.substring(0, tempKey.lastIndexOf("/"));
                if (path.equals("")) {
                    path = "/";
                }
            } else {
                path = "/";
            }
        }

        return new CloudFile(key, path, name, length, modifyTime, isFile);
    }
}

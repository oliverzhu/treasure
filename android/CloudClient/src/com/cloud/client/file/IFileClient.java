package com.cloud.client.file;

import java.util.List;

public interface IFileClient {

    public String getAccessId();
    
    public String getSecretKey();
    
    public String getBuckerName();
    
    /**
     * 创建Bucket
     * 
     * @return {@link CloudFileResult}
     */
    public CloudFileResult createBucket();
    
    /**
     * 上传指定文件到服务器指定目录下
     * 
     * @param userId 用户Id
     * @param filePath 本地文件
     * @param key 用户{userId}将文件{filePath}保存到服务器的目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult uploadFile(
            String userId,
            String filePath,
            String key,
            MissionListener listener);

    /**
     * 上传指定文件到服务器指定目录下
     * 
     * @param userId 用户Id
     * @param filePath 本地文件
     * @param contentType 文件类型
     * @param key 用户{userId}将文件{filePath}保存到服务器的目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult uploadFile(
            String userId,
            String filePath,
            String contentType,
            String key,
            MissionListener listener);

    /**
     * 上传多个指定文件到服务器指定目录下
     * 
     * @param userId 用户Id
     * @param fileList 本地文件列表
     * @param key 用户{userId}将文件列表{fileList}保存到服务器的目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult uploadFiles(
            String userId,
            List<String> fileList,
            String key,
            MissionListener listener);
    
    /**
     * 递归上传指定目录到服务器指定目录下
     * 
     * @param userId 用户Id
     * @param dir 本地目录
     * @param key 用户{userId}将文件列表{fileList}保存到服务器的目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult uploadDirectory(
            String userId,
            String dir,
            String key,
            MissionListener listener);
    
    /**
     * 初始化分块上传任务
     * 
     * @param userId 用户Id
     * @param filePath 本地文件
     * @param key 用户{userId}将文件列表{fileList}保存到服务器的目录
     * @return {@link MissionObject}
     */
    public MissionObject initMultipartUpload(
            String userId,
            String filePath,
            String key);
    
    /**
     * 分块上传指定文件到服务器指定目录下
     * 
     * @param missionObject {@link MissionObject} {@link initMultipartUpload()返回值}
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult multipartUpload(
            MissionObject missionObject,
            MissionListener listener);
    
    /**
     * 获取用户{userId}未完成的分块上传任务
     * 
     * @param userId 用户Id
     * @param missionList 未完成的分块任务{@link MissionObject}列表，此类表中的任务支持继续上传
     * @param unKnownList 未知未完成的分块任务{@link MissionObject}列表，此类表中的任务无法继续上传，
     *          建议使用{deleteUploadMission}或{deleteUploadMissionbyUploadId}删除
     * @return {@link CloudFileResult}
     */
    public CloudFileResult listmultipartUploads(
            String userId/*,
            List<MissionObject> missionList,
            List<MissionObject> unKnownList*/);
    
    /**
     * 初始化下载任务
     * 1.对于新创建的下载任务，可以通过overwrite选择是否覆盖本地已存在的文件
     * 2.对于已完成的任务，并且任务存在于下载列表中，可以通过overwrite选择是否覆盖本地已存在文件
     * 3.参数overwrite不影响断点续传
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上需要下载的文件
     * @param localFile 本地存储位置
     * @param overwrite 是否覆盖本地已存在的文件
     * @return {@link MissionObject}
     */
    public MissionObject initDownload(
            String userId,
            String key,
            String localFile,
            boolean overwrite);
    
    /**
     * 下载服务器指定文件到本地
     * 
     * @param missionObject {@link MissionObject} {@link initMultipartUpload()返回值}
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult downloadFile(
            MissionObject missionObject,
            MissionListener listener);

    /**
     * 下载多个服务器指定文件到本地
     * 
     * @param userId 用户Id
     * @param keyList 用户{userId}在服务器上需要下载的文件列表
     * @param dir 本地存储目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult downloadFiles(
            String userId,
            List<String> keyList,
            String dir,
            MissionListener listener);
    
    /**
     * 递归下载服务器上指定目录到本地
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上需要下载的目录
     * @param dir 本地存储目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult downloadDirectory(
            String userId,
            String key,
            String dir,
            MissionListener listener);
    
    /**
     * 删除服务器上指定文件，也可删除空目录
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上需要删除的文件或目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult deleteFile(
            String userId,
            String key);
    
    /**
     * 删除服务器上多个指定文件，也可删除空目录
     * 
     * @param userId 用户Id
     * @param keyList 用户{userId}在服务器上需要删除的文件或目录列表
     * @return {@link CloudFileResult}
     */
    public CloudFileResult deleteFiles(
            String userId,
            List<String> keyList);
    
    /**
     * 递归的删除服务器上指定目录
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上需要删除的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult deleteDirectory(String userId, String key);
    
    /**
     * 非递归从服务器上获取指定目录下文件列表
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上所要获取的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult listDirectory(String userId, String key);
    
    /**
     * 递归获取指定目录下文件列表
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上所要获取的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult getDirectoryList(String userId, String key);
    
    /**
     * 获取目录大小
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上所要获取的目录
     * @return 成功返回目录大小，失败返回错误码
     */
    public long getDirectorySize(String userId, String key);
    
    /**
     * 重命名指定文件
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上需要重命名的文件
     * @param newName 新名称
     * @return 成功返回CloudUtil.CLOUDCLIENT_RESULT_OK，失败返回错误码
     */
//    public int renameFile(String userId, String key, String newName);
    
    /**
     * 移动文件
     * 
     * @param userid 用户Id
     * @param key 用户{userId}在服务器上的源文件
     * @param target 用户{userId}在服务器上的目标文件
     * @return {@link CloudFileResult}
     */
    public CloudFileResult moveFile(String userId, String key, String target);
    
    /**
     * 复制文件
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上的源文件
     * @param target 用户{userId}在服务器上的目标文件
     * @return {@link CloudFileResult}
     */
    public CloudFileResult copyFile(String userId, String key, String target);
    
    /**
     * 移动文件夹
     * 
     * @param userId 用户Id
     * @param srcPath 源文件夹
     * @param destPath 目标文件夹
     * @return {@link CloudFileResult}
     */
    public CloudFileResult moveFolder(String userId, String srcPath, String destPath);
    
    /**
     * 复制文件夹
     * 
     * @param userId 用户Id
     * @param srcPath 源文件夹
     * @param destPath 目标文件夹
     * @return {@link CloudFileResult}
     */
    public CloudFileResult copyFolder(String userId, String srcPath, String destPath);
    
    /**
     * 创建目录
     * 
     * @param userId 用户Id
     * @param key 用户{userId}在服务器上所要创建的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult createDir(String userId, String key);
    
    /**
     * 获取上传任务列表，包含成功任务与失败任务，普通上传任务与分块上传任务
     * 
     * @param userId 用户Id
     * @return {@link MissionObject}列表
     */
    public List<MissionObject> getUploadList(String userId);
    
    /**
     * 获取下载任务列表，包含成功任务与失败任务
     * 
     * @param userId 用户Id
     * @return {@link MissionObject}列表
     */
    public List<MissionObject> getDownloadList(String userId);
    
    /**
     * 删除指定上传任务
     * 
     * @param userId 用户Id
     * @param key 上传任务{@link MissionObject.getKey()}
     */
    public void deleteUploadMission(String userId, String key);
    
    /**
     * 删除指定上传任务
     * 
     * @param uploadId 上传任务{@link MissionObject.getUploadId()}
     */
    public void deleteUploadMissionbyUploadId(String uploadId);
    
    /**
     * 删除指定指定用户所有上传任务
     * 
     * @param userId 用户Id
     */
    public void deleteUploadMission(String userId);
    
    /**
     * 删除指定上传任务
     * 
     * @param missionObject {@link MissionObject}
     */
    public void deleteUploadMission(MissionObject missionObject);
    
    /**
     * 删除指定下载任务
     * 
     * @param userId 用户Id
     * @param key 下载任务{@link MissionObject.getKey()}
     * @param flag 是否删除本地文件
     */
    public void deleteDownloadMission(String userId, String key, boolean flag);
    
    /**
     * 删除指定指定用户所有下载任务
     * 
     * @param userId 用户Id
     * @param flag 是否删除本地文件
     */
    public void deleteDownloadMission(String userId, boolean flag);
    
    /**
     * 删除指定下载任务
     * 
     * @param missionObject {@link MissionObject}
     * @param flag 是否删除本地文件
     */
    public void deleteDownloadMission(MissionObject missionObject, boolean flag);
    
    /**
     * 继续上传任务
     * 
     * @param missionObject {@link MissionObject}
     * @param listener {@link MissionListener}
     * @return 成功返回CloudUtil.CLOUDCLIENT_RESULT_OK，失败返回错误码
     */
//    public int continueUploadMission(MissionObject missionObject, MissionListener listener);
    
    /**
     * 继续下载任务
     * 
     * @param missionObject {@link MissionObject}
     * @param listener {@link MissionListener}
     * @return 成功返回CloudUtil.CLOUDCLIENT_RESULT_OK，失败返回错误码
     */
//    public int continueDownloadMission(MissionObject missionObject, MissionListener listener);
}

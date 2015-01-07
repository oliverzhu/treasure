package com.ape.onelogin.login.core;

import java.util.Map;

import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.service.CloudSdkService;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;

public interface LoginServiceInterface {
    
    /**
     * 注册用户
     * <p>1.如果已存在匿名账户，则更新服务器数据表中的匿名账户。
     * <p>2.如果不存在匿名账户，则在服务器数据表中插入新用户
     * <p>3.会对账户名，手机号，邮箱做唯一性检验
     * 
     * @param user
     * @param resultListener
     */
    public void insertUser(User user, AuthenticListener resultListener);
    
    /**
     * 更新用户信息，不更新本地缓存与登录时间
     * 
     * @param user
     * @param apiListener
     */
    public void updateUser(User user, AuthenticListener resultListener);
    
    /**
     * 云账户登录，若登录成功则更新本地缓存与登录时间
     * 
     * @param user
     * @param resultListener
     */
    public void getUser(User user, AuthenticListener resultListener);
    
    /**
     * 匿名账户登录，不更新本地缓存与登录时间
     * 
     * @param user
     * @param service
     */
    public void getGuestUser(User user, CloudSdkService service);
    
    /**
     * 第三方账户登录，若登录成功则更新本地缓存与登录时间
     * 
     * @param user
     * @param resultListener
     */
    public void getCooperationUser(User user, AuthenticListener resultListener);
    
    /**
     * 找回密码前检查邮箱地址合法性
     * 
     * @param address
     * @param apiListener
     */
    public void findPassword(String address, AuthenticListener resultListener);
    
    /**
     * 发送邮件
     * 
     * @param address
     * @param title
     * @param body
     * @param apiListener
     */
    public void sendEmail(String address, String title, String body,
            AuthenticListener resultListener);
    
    /**
     * 分配文件服务器操作对象
     * 
     * @param userInfoMap
     * @return
     */
    public int allocFileClient(Map userInfoMap);
    
    /**
     * 删除上传任务
     * @param key
     */
    public void deleteUploadMission(String key);
    /**
     * 初始化文件分块上传任务
     * 
     * @param filePath
     * @param key
     * @return
     */
    public MissionObject initMultipartUpload(String filePath, String key);
    
    /**
     * 开始文件分块上传任务
     * 
     * @param missionObject
     * @param listener
     * @return
     */
    public CloudFileResult multipartUploadFile(MissionObject missionObject, MissionListener listener);
    
    /**
     * 初始化文件下载任务
     * 
     * @param localFile
     * @param key
     * @param overwrite
     * @return
     */
    public MissionObject initDownload(String localFile, String key, boolean overwrite);
    
    /**
     * 开始下载文件
     * 
     * @param missionObject
     * @param listener
     * @return
     */
    public CloudFileResult download(MissionObject missionObject, MissionListener listener);
    
    /**
     * 清除匿名用户缓存
     */
    public void clearGuestCache();
}

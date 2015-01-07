package com.ape.onelogin.login.core;

import android.app.Activity;

import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;


/**
 * 平台基本登录接口
 * 
 * @author jianwen.zhu
 * @since 2014-3-3
 */
public interface ILoginInterface {
    
    public void init();
    
    /**
     * 显示用户的基本信息，注册、登录、更新用户信息后都需要通过此方法来显示用户信息。
     * 此方法不与服务器交互，故不会产生网络延迟
     * 
     *  <p>如果本地无缓存则回调返回{@link LoginService.REP_LOGIN_SERVICE_CACHE_NOT_EXIST},
     *  <p>本地缓存过期回调返回{@link LoginService.REP_LOGIN_SERVICE_CACHE_TIMEOUT},
     *  <p>本地缓存有效回调返回{@link LoginService.REP_LOGIN_SERVICE_SUCCESS}
     *  
     * @param resultListener
     */
    public void showUser(AuthenticListener resultListener);
    
    /**
     * 用户登录
     * 
     * <p>1.对于云账户来说,查询服务器数据库中指定用户是否存在
     * <p>2.对于第三方账户来说,从第三方服务器获取登录授权
     * <p>3.如果上述条件执行成功,则调用{@link #onLogin(User, AuthenticListener)}
     * 
     * @param activity
     * @param user
     * @param resultListener
     */
    public void login(Activity activity, User user, AuthenticListener resultListener);
    
    /**
     * 用户登录,将{@link login}返回的数据信息更新到服务器中
     * 
     * @param user
     * @param resultListener
     */
    public void onLogin(User user, AuthenticListener resultListener);
    
    /**
     * 注册新用户
     * 
     * @param user
     * @param resultListener
     */
    public void register(User user, AuthenticListener resultListener);
    
    /**
     * 更新用户信息
     * 
     * @param user
     * @param resultListener
     */
    public void updateUser(User user, AuthenticListener resultListener);
    
    /**
     * 注销
     * 
     * @param activity
     * @param apiListener
     */
    public void logout(Activity activity, AuthenticListener resultListener);
    
    public void findPassword(String address, AuthenticListener resultListener);
    
    public void sendEmail(String address, String title, String body, AuthenticListener resultListener);
    
    public void clearGuestCache();
    
    /**
     * 初始化的时候根据Preference来登录
     */
    public void setAccessToken();
}

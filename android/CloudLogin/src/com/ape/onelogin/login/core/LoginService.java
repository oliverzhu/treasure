package com.ape.onelogin.login.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.tsz.afinal.core.FileNameGenerator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.R;
import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.util.LogUtil;
import com.cloud.client.CloudClientService;
import com.cloud.client.CloudUtil;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.MissionObject;
import com.cloud.client.sql.SqlResultListener;
import com.cloud.client.sql.SqlResultMultiListener;
import com.cloud.client.sql.SqlResultSingleListener;

/**
 * <p>1.对于{@link AuthenticListener}回调,结果返回以{@link User}对象的形式.除{@link #updateServerDB(User, AbsLoginHandler, AuthenticListener)}外,
 * 其返回为{@link #mImportantMap}对象,因此需要做特殊处理.</p>
 * <p>2.对于{@link CloudSdkService}回调,结果返回以HashMap<String, String>的形式.</p>
 * @author juanli
 *
 */
public class LoginService extends CloudClientService implements
        LoginServiceInterface {
    
    private static final String TAG = "LoginService";
    
    private static final String OWNER_KEY = "488e406315b632ad94db48672b31c5d2";
    private static final String PACKAGE_NAME = "com.ape.onelogin";
    
    private Context mContext;
    private LogUtil mLogUtil;
    private AuthenticatorManager mAuthenticatorManager;
    private SharedPreferences mGuestPreferences;
    private Map<String, Object> mImportantMap = new HashMap<String, Object>();
    
    public static final int REP_LOGIN_SERVICE_SUCCESS                  = 1000;
    public static final int REP_LOGIN_SERVICE_SEND_EMAIL_SUCCESS       = 1001;
    
    public static final int REP_LOGIN_SERVICE_UNKNOWN_ERROR            = -1001;
    public static final int REP_LOGIN_SERVICE_NETWORK_ERROR            = -1002;
    public static final int REP_LOGIN_SERVICE_USER_EXIST               = -1003;
    public static final int REP_LOGIN_SERVICE_USER_NOT_EXIST           = -1004;
    public static final int REP_LOGIN_SERVICE_PASSWORD_ERROR           = -1005;
    public static final int REP_LOGIN_SERVICE_EMAIL_EXISTS             = -1006;
    public static final int REP_LOGIN_SERVICE_MOBILE_EXISTS            = -1007;
    public static final int REP_LOGIN_SERVICE_USER_DATE_ERROR          = -1008;
    public static final int REP_LOGIN_SERVICE_UNIQUENESS_ERROR         = -1009;
    public static final int REP_LOGIN_SERVICE_INSERT_USER_ERROR        = -1010;
    public static final int REP_LOGIN_SERVICE_PASSWORD_COOSDK          = -1011;
    public static final int REP_LOGIN_SERVICE_PASSWORD_ANONYMOUS       = -1012;
    
    public static final int REP_LOGIN_SERVICE_CACHE_TIMEOUT            = -2001;
    public static final int REP_LOGIN_SERVICE_CACHE_NOT_EXIST          = -2002;
    
    public LoginService(Context context, String askerAppKey) {
        super(context, askerAppKey);
        mContext = context;
        mLogUtil = new LogUtil(TAG);
        mGuestPreferences = mContext.getSharedPreferences(
                Constants.PREFERENCE_NAME_GUEST, Context.MODE_APPEND);
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
    }
    
    @Override
    public void insertUser(final User user, final AuthenticListener resultListener) {
        StringBuffer where = new StringBuffer();
        final String checkCode = mGuestPreferences.getString(Constants.KEY_CHECK_CODE, "");
        
        mLogUtil.i("insertUser", "username:%s, checkCode:%s, user index in db:%s, sdkType:%s",
                user.getUserName(), checkCode, user.getId(), user.getSdkType());
        
        where.append("userName='").append(user.getUserName()).append("'")
            .append(" or email='").append(user.getEmail()).append("'")
            .append(" or mobile='").append(user.getMobile()).append("'")
            .append(" or (userName='").append(checkCode)
            .append("' and imei='").append(user.getImei())
            .append("' and sdkType='" + AbsLoginHandler.SDK_TYPE_ANONYMOUS + "')");
        
        mLogUtil.i("insertUser", "where:" + where.toString());
        
        super.getByWhere(User.class, where.toString(), new SqlResultMultiListener<User>() {
            
            boolean isAccountExists = false;
            boolean isEmailExists = false;
            boolean isPhoneExists = false;
            
            @Override
            public void onSuccess(List<User> entity, String msg) {
                // 账户未被注册,匿名账户不存在
                if (entity == null || entity.size() == 0) {
                    insertServerDB(user, resultListener);
                    return;
                }
                
                // 账户已被注册
                if (resultListener != null) {
                    for (User object : entity) {
                        if (object.getUserName().equals(user.getUserName()) &&
                                AbsLoginHandler.SDK_TYPE_CLOUD.equals(object.getSdkType())) {// juan.li for sdk to modify
                            isAccountExists = true;
                            msg = mContext.getResources().getString(R.string.user_signin_fail_by_duplicate);
                            resultListener.onComplete(REP_LOGIN_SERVICE_USER_EXIST, null, msg);
                        }
                        
                        if (object.getEmail().equals(user.getEmail())) {
                            isEmailExists = true;
                            msg = mContext.getResources().getString(R.string.user_signin_fail_by_email);
                            resultListener.onComplete(REP_LOGIN_SERVICE_EMAIL_EXISTS, null, msg);
                        }
                        
                        if (object.getMobile().equals(user.getMobile())) {
                            isPhoneExists = true;
                            msg = mContext.getResources().getString(R.string.user_signin_fail_by_mobile);
                            resultListener.onComplete(REP_LOGIN_SERVICE_MOBILE_EXISTS, null, msg);
                        }
                    }
                }
                
                // 账户未被注册,存在匿名账户
                if (!isAccountExists && !isEmailExists && !isPhoneExists) {
                    for (User object : entity) {
                        if (object.getUserName().equals(checkCode) ||
                            (object.getUserName().equals(user.getUserName()) &&
                                    !AbsLoginHandler.SDK_TYPE_CLOUD.equals(object.getSdkType()))) { // juan.li for sdk to modify
                            user.setId(object.getId());
                            // TODO
                            updateServerDB(user, new AuthenticListener() {
                                
                                @Override
                                public void onComplete(int result, Object obj, String message) {
                                    if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                                        // 删除匿名账户缓存
                                        mLogUtil.i("insertUser", "register account, clear guest cache");
                                        clearGuestCache();
                                    }
                                    if (resultListener != null) {
                                        resultListener.onComplete(result, obj, message);
                                    }
                                }
                                
                                @Override
                                public void onCancel() {
                                    if (resultListener != null) {
                                        resultListener.onCancel();
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(int errorNo, String msg) {
                if (resultListener != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    }
                    resultListener.onComplete(result, null, msg);
                }
            }
            
        });
    }
    
    @Override
    public void updateUser(final User user, final AuthenticListener resultListener) {
        StringBuffer where = new StringBuffer();
        boolean isEmailEmpty = false;
        boolean isMobileEmpty = false;
        where.append("id <> ").append(user.getId());
        if (user.getEmail() != null && user.getEmail().trim().length() != 0) {
            isEmailEmpty = false;
            where.append(" and (email='").append(user.getEmail()).append("')");
        } else {
            isEmailEmpty = true;
        }
        
        if (user.getMobile() != null && user.getMobile().trim().length() != 0) {
            isMobileEmpty = false;
            where.deleteCharAt(where.length() - 1);
            where.append(" or mobile='").append(user.getMobile()).append("')");
        } else {
            isMobileEmpty = true;
        }
        
        mLogUtil.i("updateUser", "username:%s, user index in db:%s, sdkType:%s",
                user.getUserName(), user.getId(), user.getSdkType());
        
        if (isEmailEmpty && isMobileEmpty) {
            // 对于未设置手机号与邮箱的账户,重新登录时更新相关信息
            updateServerDB(user, resultListener);
        } else {
            super.getByWhere(User.class, where.toString(), new SqlResultMultiListener<User>() {
    
                @Override
                public void onSuccess(List<User> entity, String msg) {
                    if (entity == null || entity.size() == 0) {
                        // 没有与此用户冲突的数据,可以进行更新操作
                        updateServerDB(user, resultListener);
                    } else if (resultListener != null) {
                        for (User object : entity) {
                            if (object.getEmail().equals(user.getEmail())) {
                                msg = mContext.getResources().getString(R.string.user_signin_fail_by_email);
                                resultListener.onComplete(REP_LOGIN_SERVICE_EMAIL_EXISTS, null, msg);
                            }
                            
                            if (object.getMobile().equals(user.getMobile())) {
                                msg = mContext.getResources().getString(R.string.user_signin_fail_by_mobile);
                                resultListener.onComplete(REP_LOGIN_SERVICE_MOBILE_EXISTS, null, msg);
                            }
                        }
                    }
                }
    
                @Override
                public void onFailure(int errorNo, String msg) {
                    if(resultListener != null) {
                        int result = errorNo;
                        if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                            msg = mContext.getResources().getString(R.string.ex_network_error);
                            result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                        }
                        resultListener.onComplete(result, null, msg);
                    }
                }
                
            });
        }
    }
    
    @Override
    public void getUser(User user, AuthenticListener resultListener) {
        String where = "userName='" + user.getUserName() + "'" 
                + " and password='" + user.getPassword() + "'"
                + "and sdkType='" + user.getSdkType() + "'";
        mLogUtil.i("getUser", "username:%s, user index in db:%s, sdkType:%s",
                user.getUserName(), user.getId(), user.getSdkType());
        
        queryServerDB(where, resultListener, null);
    }
    
    @Override
    public void getGuestUser(User user, final CloudSdkService service) {
        final String where = "userName='" + user.getUserName() + "'"
                + " and imei='" + user.getImei() + "'"
                + "and sdkType='" + user.getSdkType() + "'";
        mLogUtil.i("getGuestUser", "username:%s, user index in db:%s, sdkType:%s",
                user.getUserName(), user.getId(), user.getSdkType());
        
        super.insertOrUpdate(user, where, new SqlResultSingleListener<User>() {

            @Override
            public void onSuccess(User entity, String msg) {
                if (entity != null && service != null) {
                    queryServerDB(where, new AuthenticListener() {
                        
                        @Override
                        public void onComplete(int result, Object obj, String message) {
                            if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                                User entity = (User) obj;
                                Editor editor = mGuestPreferences.edit();
                                editor.putString(Constants.KEY_CHECK_CODE, entity.getUserName());
                                editor.putString(Constants.KEY_USER_KEY, entity.getId().toString());
                                editor.commit();
                            }
                        }
                        
                        @Override
                        public void onCancel() {
                            // TODO Auto-generated method stub
                            
                        }
                    }, service);
                } else {
                    HashMap<String, String> userMap = new HashMap<String, String>();
                    userMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
                    service.beginBroadcastForQuery(userMap);
                }
            }

            @Override
            public void onFailure(int errorNo, String msg) {
                if(service != null) {
                    HashMap<String, String> userMap = new HashMap<String, String>();
                    userMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
                    service.beginBroadcastForQuery(userMap);
                }
            }
            
        });
    }
    
    @Override
    public void getCooperationUser(User user, AuthenticListener resultListener) {
        String where = "userName='" + user.getUserName() + "'"
                + "and sdkType='" + user.getSdkType() + "'";
        mLogUtil.i("getCooperationUser", "username:%s, user index in db:%s, sdkType:%s",
                user.getUserName(), user.getId(), user.getSdkType());
        
        queryServerDB(where, resultListener, null);
    }
    
    @Override
    public void findPassword(String address, final AuthenticListener resultListener) {
        StringBuffer where = new StringBuffer();
        where.append("email='").append(address).append("'");
        mLogUtil.i("findPassword", "address:%s", address);
        
        super.getByWhere(User.class, where.toString(), new SqlResultMultiListener<User>() {
            
            @Override
            public void onSuccess(List<User> entity, String msg) {
                if (entity == null || entity.size() == 0) {
                    if (resultListener != null) {
                        msg = mContext.getResources().getString(R.string.ex_forget_password_not_exist);
                        resultListener.onComplete(REP_LOGIN_SERVICE_USER_NOT_EXIST, null, msg);
                    }
                } else {
                    User user = entity.get(0);
                    if (AbsLoginHandler.SDK_TYPE_ANONYMOUS.equals(user.getSdkType())) {
                        msg = mContext.getResources().getString(R.string.ex_forget_password_anonymous);
                        resultListener.onComplete(REP_LOGIN_SERVICE_PASSWORD_ANONYMOUS, null, msg);
                    } else if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(user.getSdkType())) {
                        String important = generateRandomPassword();
                        mImportantMap.put(Constants.REQ_SEND_MAIL_PARAM_1, important);
                        user.setPassword(FileNameGenerator.generator(important + Constants.PASSWORD_MASK));
                        updateServerDBforPassword(user, resultListener);
                    } else {
                        msg = mContext.getResources().getString(R.string.ex_forget_password_coosdk);
                        resultListener.onComplete(REP_LOGIN_SERVICE_PASSWORD_COOSDK, null, msg);
                    }
                }
            }
            
            @Override
            public void onFailure(int errorNo, String msg) {
                if (resultListener != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    }
                    resultListener.onComplete(result, null, msg);
                }
            }
            
        });
    }

    @Override
    public void sendEmail(String address, String title, String body,
            final AuthenticListener resultListener) {
        mLogUtil.i("sendEmail", "address:%s, title:%s", address, title);
        super.sendEmail(address, title, body, new SqlResultListener() {
            
            @Override
            public void onSuccess(String msg) {
                if (resultListener != null) {
                    resultListener.onComplete(REP_LOGIN_SERVICE_SEND_EMAIL_SUCCESS, null, msg);
                }
            }
            
            @Override
            public void onFailure(int errorNo, String msg) {
                if(resultListener != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    }
                    resultListener.onComplete(result, null, msg);
                }
            }
        });

    }
    
    /**
     * 将user插入到服务器数据表中，此操作更新本地缓存
     * 
     * @param user
     * @param loginHandler
     * @param resultListener
     */
    private void insertServerDB(final User user, final AuthenticListener resultListener) {
        mLogUtil.i("insertServerDB", "user information:\n%s", LogUtil.formatUserMessage(user));
        
        super.insert(user, new SqlResultSingleListener<User>() {

            @Override
            public void onSuccess(User entity, String msg) {
                if (resultListener != null) {
                    if (entity != null) {
                        // TODO
                        mLogUtil.i("insertServerDB", "save user into local cache");
                        mAuthenticatorManager.handleUser(entity);
                        resultListener.onComplete(REP_LOGIN_SERVICE_SUCCESS, entity, msg);
                    } else {
                        resultListener.onComplete(REP_LOGIN_SERVICE_INSERT_USER_ERROR, null, msg);
                    }
                }
            }

            @Override
            public void onFailure(int errorNo, String msg) {
                if(resultListener != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    }
                    resultListener.onComplete(result, null, msg);
                }
            }
            
        });
    }
    
    private void updateServerDBforPassword(final User user, final AuthenticListener resultListener) {
        
        mLogUtil.i("updateServerDBforPassword", "user information:\n%s", LogUtil.formatUserMessage(user));
        
        String tableName = super.getTableName(User.class);
        StringBuffer sql = new StringBuffer("UPDATE ").append(tableName).append(" SET ");

        if (user.getPassword() != null) {
            sql.append("password=").append("'" + user.getPassword() + "'");
        }
        if (user.getExpiresIn() != null) {
            sql.append(", ").append("expiresIn=").append("'" + user.getExpiresIn() + "'");
        }
        
        sql.append(" where id=").append(user.getId());
        
        mLogUtil.i("updateServerDBforPassword", "SQL:%s", sql.toString());
        
        super.updateCustom(sql.toString(), tableName, new SqlResultListener() {
            
            @Override
            public void onSuccess(String msg) {
                if (resultListener != null) {
                    mLogUtil.i("updateServerDBforPassword", "save user into local cache");
                    mImportantMap.put(Constants.REQ_SEND_MAIL_PARAM_2, user);
                    resultListener.onComplete(REP_LOGIN_SERVICE_SUCCESS, mImportantMap, null);
                }
            }
            
            @Override
            public void onFailure(int errorNo, String msg) {
                if(resultListener != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    }
                    resultListener.onComplete(result, null, msg);
                }
            }
        });
    }
    
    /**
     * 更新服务器数据库中相应记录，此操作更新本地缓存
     * 
     * @param user
     * @param resultListener
     */
    private void updateServerDB(final User user, final AuthenticListener resultListener) {
        
        mLogUtil.i("updateServerDB", "user information:\n%s", LogUtil.formatUserMessage(user));
        
        String tableName = super.getTableName(User.class);
        StringBuffer sql = new StringBuffer("UPDATE ").append(tableName).append(" SET ");
        if (user.getUserName() != null) {
            sql.append("userName=").append("'" + user.getUserName() + "'");
        }
        if (user.getPassword() != null) {
            sql.append(", ").append("password=").append("'" + user.getPassword() + "'");
        }
        if (user.getExpiresIn() != null) {
            sql.append(", ").append("expiresIn=").append("'" + user.getExpiresIn() + "'");
        }
        if (user.getMobile() != null) {
            sql.append(", ").append("mobile=").append("'" + user.getMobile() + "'");
        }
        if (user.getEmail() != null) {
            sql.append(", ").append("email=").append("'" + user.getEmail() + "'");
        }
        if (user.getRegisterDate() != null) {
            sql.append(", ").append("registerDate=").append("'" + user.getRegisterDate() + "'");
        }
        if (user.getNickName() != null) {
            sql.append(", ").append("nickName=").append("'" + user.getNickName() + "'");
        }
        if (user.getGender() != null) {
            sql.append(", ").append("gender=").append("'" + user.getGender() + "'");
        }
        if (user.getBirthday() != null) {
            sql.append(", ").append("birthday=").append("'" + user.getBirthday() + "'");
        }
        if (user.getAddress() != null) {
            sql.append(", ").append("address=").append("'" + user.getAddress() + "'");
        }
        if (user.getAvatar() != null) {
            sql.append(", ").append("avatar=").append("'" + user.getAvatar() + "'");
        }
        
        if (user.getHomeTown() != null) {
            sql.append(", ").append("homeTown=").append("'" + user.getHomeTown() + "'");
        }
        if (user.getCountry() != null) {
            sql.append(", ").append("country=").append("'" + user.getCountry() + "'");
        }
        if (user.getProvince() != null) {
            sql.append(", ").append("province=").append("'" + user.getProvince() + "'");
        }
        if (user.getCity() != null) {
            sql.append(", ").append("city=").append("'" + user.getCity() + "'");
        }
        if (user.getProfession() != null) {
            sql.append(", ").append("profession=").append("'" + user.getProfession() + "'");
        }
        
        if (user.getSdkType() != null) {
            sql.append(", ").append("sdkType=").append("'" + user.getSdkType() + "'");
        }
        
        if (user.getField1() != null) {
            sql.append(", ").append("field1=").append("'" + user.getField1() + "'");
        }
        if (user.getField2() != null) {
            sql.append(", ").append("field2=").append("'" + user.getField2() + "'");
        }
        if (user.getField3() != null) {
            sql.append(", ").append("field3=").append("'" + user.getField3() + "'");
        }
        if (user.getField4() != null) {
            sql.append(", ").append("field4=").append("'" + user.getField4() + "'");
        }
        if (user.getField5() != null) {
            sql.append(", ").append("field5=").append("'" + user.getField5() + "'");
        }
        sql.append(" where id=").append(user.getId());
        
        mLogUtil.i("updateServerDB", "SQL:%s", sql.toString());
        
        super.updateCustom(sql.toString(), tableName, new SqlResultListener() {
            
            @Override
            public void onSuccess(String msg) {
                if (resultListener != null) {
                    // TODO 保存本地缓存
                    mLogUtil.i("updateServerDB", "save user into local cache");
                    mAuthenticatorManager.handleUser(user);
                    mImportantMap.put(Constants.REQ_SEND_MAIL_PARAM_2, user);
                    resultListener.onComplete(REP_LOGIN_SERVICE_SUCCESS, mImportantMap, null);
                }
            }
            
            @Override
            public void onFailure(int errorNo, String msg) {
                if(resultListener != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    }
                    resultListener.onComplete(result, null, msg);
                }
            }
        });
    }
    
    /**
     * 从服务器数据库，根据where获取相应数据，此操作更新本地缓存
     * 
     * @param where
     * @param resultListener
     * @param service
     */
    private void queryServerDB(String where, final AuthenticListener resultListener,
            final CloudSdkService service) {
        mLogUtil.i("queryServerDB", "where:%s", where);
        
        super.getByWhere(User.class, where, new SqlResultMultiListener<User>() {

            @Override
            public void onSuccess(List<User> entity, String msg) {
                if (mContext != null) {
                    HashMap<String, String> userMap = new HashMap<String, String>();
                    userMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
                    if (entity == null || entity.size() == 0) {
                        msg = mContext.getResources().getString(R.string.login_fail_by_authorize);
                        if (resultListener != null) {
                            resultListener.onComplete(REP_LOGIN_SERVICE_USER_NOT_EXIST, null, msg);
                        }
                        if (service != null) {
                            service.beginBroadcastForQuery(userMap);
                        }
                    } else if (entity != null && entity.size() > 1) {
                        if (resultListener != null) {
                            resultListener.onComplete(REP_LOGIN_SERVICE_UNIQUENESS_ERROR, null, msg);
                        }
                        if (service != null) {
                            service.beginBroadcastForQuery(userMap);
                        }
                    } else {
                        User user = entity.get(0);
                        // TODO 保存本地缓存
                        // 必须保存本地,否则上传头像无法获取服务器参数
                        mLogUtil.i("queryServerDB", "save user into local cache");
                        mAuthenticatorManager.handleUser(user);
                        if (resultListener != null) {
                            resultListener.onComplete(REP_LOGIN_SERVICE_SUCCESS, user, msg);
                        }
                        
                        if (service != null) {
                            userMap = mAuthenticatorManager.getUserMapData();
                            service.beginBroadcastForQuery(userMap);
                        }
                    }
                }
            }

            @Override
            public void onFailure(int errorNo, String msg) {
                if (mContext != null) {
                    int result = errorNo;
                    if (errorNo == CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR) {
                        msg = mContext.getResources().getString(R.string.ex_network_error);
                        result = REP_LOGIN_SERVICE_NETWORK_ERROR;
                    } else if (errorNo == CloudUtil.CLOUDCLIENT_SQL_LOCAL_DATE_ERROR){
                        msg = mContext.getResources().getString(R.string.login_fail_by_date);
                        result = REP_LOGIN_SERVICE_USER_DATE_ERROR;
                    } else {
                        msg = mContext.getResources().getString(R.string.login_fail);
                    }
                    
                    if (resultListener != null) {
                        resultListener.onComplete(result, null, msg);
                    }
                    
                    if (service != null) {
                        HashMap<String, String> userMap = new HashMap<String, String>();
                        userMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
                        service.beginBroadcastForQuery(userMap);
                    }
                }
            }
            
        });
    }
    
    @Override
    public int allocFileClient(Map userInfo) {
        return super.allocFileClient(userInfo);
    }

    @Override
    public void deleteUploadMission(String key) {
        super.deleteUploadMission(key);
    }

    @Override
    public CloudFileResult multipartUploadFile(MissionObject missionObject,
            MissionListener listener) {
        return super.multipartUpload(missionObject, listener);
    }
    
    @Override
    public MissionObject initDownload(String localFile, String key, boolean overwrite) {
        return super.initDownload(key, localFile, overwrite);
    }
    
    @Override
    public CloudFileResult download(MissionObject missionObject,
            MissionListener listener) {
        return super.downloadFile(missionObject, listener);
    }

    @Override
    public void clearGuestCache() {
        Editor editor = mGuestPreferences.edit();
        editor.clear();
        editor.commit();
    }
    
    @Override
    protected String initOwnerKey() {
        return OWNER_KEY;
    }

    @Override
    protected String initPackageName() {
        return PACKAGE_NAME;
    }
    
    private String generateRandomPassword() {
        Random random = new Random();
        StringBuffer password = new StringBuffer();
//        char[] special = new char[]{'~', '`', '!', '@', '#', '$', '%', '^', '&',
//                '*', '(', ')', '-', '_', '+', '=', '\\', '|', '[', ']', '{', '}',
//                ';', ':', '\'', '\"', ',', '.', '<', '>', '?', '/'};
        
        final int LETTER_LENGTH = 4;
        final int DIGIT_LENGTH = 2;
        
        int maxLength = LETTER_LENGTH + DIGIT_LENGTH;
        int mLength = 0;
        int[] array = new int[maxLength];
        int tmp;
        
        // letter
        random.setSeed(System.currentTimeMillis());
        for (int i = 0; i < LETTER_LENGTH; i ++) {
            tmp = random.nextInt(26) + 65;
            tmp += random.nextInt(2) * 32; 
            array[mLength++] = tmp;
        }
        
        // digit
        random.setSeed(System.currentTimeMillis());
        for (int i = 0; i < DIGIT_LENGTH; i ++) {
            tmp = random.nextInt(10) + 48;
            array[mLength++] = tmp;
        }
        
//        random.setSeed(System.currentTimeMillis());
//        for (int i = 0; i < 2; i ++) {
//            tmp = random.nextInt(special.length);
//            array[mLength++] = special[tmp];
//        }
        
        int index = 0;
        int operateLength = maxLength;
        random.setSeed(System.currentTimeMillis());
        for (int i = 0; i < maxLength; i ++) {
            index = random.nextInt(operateLength);
            password.append((char)array[index]);
            for (int j = index; j < operateLength - 1; j ++) {
                int a = array[j];
                array[j] = array[j + 1];
                array[j + 1] = a;
            }
            operateLength --;
        }
        
        return password.toString();
    }
}

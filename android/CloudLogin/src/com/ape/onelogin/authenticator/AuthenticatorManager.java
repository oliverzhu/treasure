package com.ape.onelogin.authenticator;

import java.util.HashMap;

import com.ape.onelogin.R;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.myos.ui.AvatarManager;
import com.ape.onelogin.util.LogUtil;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

public class AuthenticatorManager {
    
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private AccountManager mAccountManager = null;
    private AvatarManager mAvatarManager = null;
    private Bundle mResultBundle = null;
    private Context mContext;
    
    protected boolean mRequestNewAccount = true;
    private static AuthenticatorManager mAuthenticatorManager = null;
    private LogUtil mLogUtil;
    
    public static AuthenticatorManager getInstance(Context context) {
        if (mAuthenticatorManager == null) {
            mAuthenticatorManager = new AuthenticatorManager(context);
        }
        
        return mAuthenticatorManager;
    }
    
    private AuthenticatorManager(Context context) {
        mLogUtil = new LogUtil("AuthenticatorManager");
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }
    
    public void beginRequest(Activity activity) {
        mLogUtil.i("enter beginRequest");
        mAccountAuthenticatorResponse = activity.getIntent().getParcelableExtra(
                AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        
        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
    }
    
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }
    
    public void handleUser(User user) {
        mLogUtil.i("handleUser", "enter handleUser, user id is:%d", user.getId());
        String userName = user.getUserName();
        String password = user.getPassword();
        Bundle userData = getUserBundle(user);
        boolean isExist = false;
        
        String accountName = user.getNickName();
        if (accountName == null || accountName.trim().length() == 0) {
            accountName = userName;
        }
        
        if (AbsLoginHandler.SDK_TYPE_ANONYMOUS.equals(user.getSdkType())) {
            accountName = mContext.getString(R.string.unknown_user_ex);
        }
        
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length == 0) {
            mLogUtil.i("handleUser", "not in acconts arrays and create, accountName:%s", accountName);
            Account account = new Account(accountName, Constants.ACCOUNT_TYPE);
            mAccountManager.addAccountExplicitly(account, password, userData);
        } else {
            for (Account entity : accounts) {
                if (entity.name.equals(accountName)) {
                    mLogUtil.i("handleUser", "in acconts arrays and update, userId:%d, accountName:%s",
                            user.getId(), accountName);
                    setUserData(entity, user);
                    isExist = true;
                    break;
                } else {
                    mLogUtil.i("handleUser", "unknown accont and remove, accountName:%s",
                            entity.name);
                    mAccountManager.removeAccount(entity, null, null);
                }
            }
            
            if (!isExist) {
                mLogUtil.i("handleUser", "not in acconts arrays and create, userId:%d, accountName:%s",
                        user.getId(), accountName);
                Account account = new Account(accountName, Constants.ACCOUNT_TYPE);
                mAccountManager.addAccountExplicitly(account, password, userData);
            }
        }
    }
    
    public boolean isSessionValid() {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        mLogUtil.i("isSessionValid", "enter isSessionValid and account number is %d", accounts.length);
        
        if (accounts.length == 1) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean isTimeout() {
        mLogUtil.i("isTimeout", "enter isTimeout");
        String expiresIn = getUserData(AbsLoginHandler.KEY_EXPIRES_IN);
        if (expiresIn == null || expiresIn.trim().length() == 0) {
            mLogUtil.i("isTimeout", "expiresIn is empty");
            return true;
        }
        
        long nowTime = System.currentTimeMillis();
        long time = Long.valueOf(expiresIn);
        if (nowTime > time) {
            mLogUtil.i("isTimeout", "timeout!!!");
            return true;
        }
        
        mLogUtil.i("isTimeout", "time right!!");
        return false;
    }
    
    public String getShowName() {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        mLogUtil.i("getShowName", "enter getShowName and account number is %d", accounts.length);
        
        String accountName = null;
        String sdkType = null;
        if (accounts.length == 1) {
            Account account = accounts[0];
            sdkType = getUserData(account, AbsLoginHandler.KEY_SDK_TYPE);
            if (AbsLoginHandler.SDK_TYPE_ANONYMOUS.equals(sdkType)) {
                accountName = mContext.getString(R.string.unknown_user_ex);
            } else {
                accountName = getUserData(account, AbsLoginHandler.KEY_NICKNAME);
                if (accountName == null || accountName.trim().length() == 0) {
                    accountName = getUserData(account, AbsLoginHandler.KEY_UID);
                }
            }
        }
        
        return accountName;
    }
    
    public User getUserData() {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        mLogUtil.i("getUserData", "enter getUserData and account number is %d", accounts.length);
        
        if (accounts.length == 1) {
            Account account = accounts[0];
            return getUserData(account);
        } else {
            return null;
        }
    }
    
    public HashMap<String, String> getUserMapData() {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        mLogUtil.i("getUserMapData", "enter getUserMapData and account number is %d", accounts.length);
        
        if (accounts.length == 1) {
            Account account = accounts[0];
            return getUserMapData(account);
        } else {
            return null;
        }
    }
    
    public void removeUser() {
        mLogUtil.w("WARNING: Remove acount!!!");
        mLogUtil.w("%s", new RuntimeException().getMessage());
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length != 0) {
            for (Account entity : accounts) {
                mLogUtil.i("removeUser", "remove account:%s", entity.name);
                mAccountManager.removeAccount(entity, null, null);
            }
        }
    }
    
    public Account getAccount() {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        mLogUtil.i("getAccount", "enter getAccount and account number is %d", accounts.length);
        if (accounts.length == 1) {
            return accounts[0];
        } else {
            mLogUtil.i("getAccount", "account number more than one and remove!");
            removeUser();
            return null;
        }
    }
    
    public String getAvatarPath() {
        mLogUtil.i("get avatar path from account cache");
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        String avatarPath = null;
        
        if (accounts.length == 1) {
            avatarPath = getUserData(accounts[0], AbsLoginHandler.KEY_AVATAR_PATH);
        }
        
        return avatarPath;
    }
    
    public void setAvatarPath(String avatarPath) {
        mLogUtil.i("set avatar path to account cache");
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        
        if (accounts.length == 1) {
            setUserData(accounts[0], AbsLoginHandler.KEY_AVATAR_PATH, avatarPath);
        }
    }
    
    public String getUserKey() {
        mLogUtil.i("get user key from account cache");
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        String userKey = AbsLoginHandler.INVALID_USER_KEY;
        
        if (accounts.length == 1) {
            userKey = getUserData(accounts[0], AbsLoginHandler.KEY_USER_KEY);
        }
        
        return userKey;
    }
    
    public String getUserName() {
        mLogUtil.i("get user name from account cache");
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        String userName = null;
        
        if (accounts.length == 1) {
            userName = getUserData(accounts[0], AbsLoginHandler.KEY_UID);
        }
        
        return userName;
    }
    
    public String getSdkType() {
        mLogUtil.i("get sdk type from account cache");
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        String sdkType = null;
        
        if (accounts.length == 1) {
            sdkType = getUserData(accounts[0], AbsLoginHandler.KEY_SDK_TYPE);
        } else {
            sdkType = AbsLoginHandler.SDK_TYPE_UNKNOWN;
        }
        
        return sdkType;
    }
    public HashMap<String, String> getCloudFileData() {
        HashMap<String, String> cloudData = null;
        
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        if (accounts.length == 1) {
            Account account = accounts[0];
            cloudData = new HashMap<String, String>();
            cloudData.put(AbsLoginHandler.KEY_USER_KEY, getUserData(account, AbsLoginHandler.KEY_USER_KEY));
            cloudData.put(AbsLoginHandler.KEY_ACCESSID, getUserData(account, AbsLoginHandler.KEY_ACCESSID));
            cloudData.put(AbsLoginHandler.KEY_SECRETKEY, getUserData(account, AbsLoginHandler.KEY_SECRETKEY));
            cloudData.put(AbsLoginHandler.KEY_BUCKETNAME, getUserData(account, AbsLoginHandler.KEY_BUCKETNAME));
            cloudData.put(AbsLoginHandler.KEY_OSSTYPE, getUserData(account, AbsLoginHandler.KEY_OSSTYPE));
            cloudData.put(AbsLoginHandler.KEY_OSSLOCAL, getUserData(account, AbsLoginHandler.KEY_OSSLOCAL));
        }
        
        return cloudData;
    }
    
    public String getUserData(String key) {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        String userData = null;
        
        if (accounts.length == 1) {
            userData = getUserData(accounts[0], key);
        } else {
            mLogUtil.w("[GET Account cache]account count is %d", accounts.length);
        }
        
        return userData;
    }
    
    /**
     * 从缓存中读取用户数据
     * 
     * @param account
     * @param key
     * @return
     */
    public String getUserData(Account account, String key) {
        String value = mAccountManager.getUserData(account, key);
        if (value == null || value.trim().length() == 0) {
            mLogUtil.w(String.format("[GET Account cache]%s, %s is empty", account.name, key));
            return "";
        } else {
            mLogUtil.d(String.format("[GET Account cache]%s, %s:%s", account.name, key, value));
            return value;
        }
    }
    
    public void setUserData(String key, String value) {
        Account[] accounts = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);
        
        if (accounts.length == 1) {
            setUserData(accounts[0], key, value);
        } else {
            mLogUtil.w(String.format("[SET Account cache]account count is %d", accounts.length));
        }
    }
    
    /**
     * 将用户数据写入缓存
     * 
     * @param account
     * @param key
     * @param value
     */
    public void setUserData(Account account, String key, String value) {
        mLogUtil.d(String.format("[SET Account cache]%s, %s:%s", account.name, key, value));
        mAccountManager.setUserData(account, key, value);
    }
    
    public void finish() {
        mLogUtil.i("enter finish");
        if (mAccountAuthenticatorResponse != null) {
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED, "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
    }
    
    /**
     * 列出的都是可以修改的数据,其他的都不能修改
     * 
     * @param account
     * @param userMap
     */
    private void setUserData(Account account, User user) {
        
        mLogUtil.i("setUserData", "the user id is %s", user.getId().toString());
        
        String id = getUserData(account, AbsLoginHandler.KEY_USER_KEY);
        if (id == null || id.trim().length() == 0) {
            if (user.getId() > 0) {
                mLogUtil.i("setUserData", "the cache user's id is error, user's Id:%s", id);
                setUserData(account, AbsLoginHandler.KEY_USER_KEY, user.getId().toString());
            }
        }
        
        String password = user.getPassword();
        if (password != null && password.trim().length() != 0) {
            setUserData(account, AbsLoginHandler.KEY_ACCESS_TOKEN, password);
        }
        
        String expiresIn = user.getExpiresIn();
        if (expiresIn != null && expiresIn.trim().length() != 0) {
            setUserData(account, AbsLoginHandler.KEY_EXPIRES_IN, user.getExpiresIn());
        }
        
        String mobile = user.getMobile();
        if (mobile != null && mobile.trim().length() != 0) {
            setUserData(account, AbsLoginHandler.KEY_MOBILE, user.getMobile());
        }
        
        String email = user.getEmail();
        if (email != null && email.trim().length() != 0) {
            setUserData(account, AbsLoginHandler.KEY_EMAIL, user.getEmail());
        }
        
        String imei = user.getImei();
        if (imei != null && imei.trim().length() != 0) {
            setUserData(account, AbsLoginHandler.KEY_IMEI, user.getImei());
        }
        
        setUserData(account, AbsLoginHandler.KEY_NICKNAME, user.getNickName());
        setUserData(account, AbsLoginHandler.KEY_GENDER, user.getGender());
        setUserData(account, AbsLoginHandler.KEY_BIRTHDAY, user.getBirthday());
        setUserData(account, AbsLoginHandler.KEY_ADDRESS, user.getAddress());
        setUserData(account, AbsLoginHandler.KEY_AVATAR, user.getAvatar());
        if (user.getAvatar() != null && user.getAvatar().trim().length() != 0 && 
                user.getSdkType().equals(AbsLoginHandler.SDK_TYPE_CLOUD)) {
            if (mAvatarManager == null) {
                mAvatarManager = new AvatarManager(mContext);
            }
            setUserData(account, AbsLoginHandler.KEY_AVATAR_PATH,
                    mAvatarManager.getPicturePath(user.getId().toString()));
        } else {
            setUserData(account, AbsLoginHandler.KEY_AVATAR_PATH, "");
        }
        
        setUserData(account, AbsLoginHandler.KEY_HOMETOWN, user.getHomeTown());
        setUserData(account, AbsLoginHandler.KEY_COUNTRY, user.getCountry());
        setUserData(account, AbsLoginHandler.KEY_PROVINCE, user.getProvince());
        setUserData(account, AbsLoginHandler.KEY_CITY, user.getCity());
        setUserData(account, AbsLoginHandler.KEY_PROFESSION, user.getProfession());
        
        String accessId = mAccountManager.getUserData(account, AbsLoginHandler.KEY_ACCESSID);
        if (accessId == null || accessId.trim().length() == 0) {
            if (user.getAccessId() != null && user.getAccessId().trim().length() > 0) {
                setUserData(account, AbsLoginHandler.KEY_ACCESSID, user.getAccessId());
            }
        }
        
        String sectetKey = getUserData(account, AbsLoginHandler.KEY_SECRETKEY);
        if (sectetKey == null || sectetKey.trim().length() == 0) {
            if (user.getSecretKey() != null && user.getSecretKey().trim().length() > 0) {
            setUserData(account, AbsLoginHandler.KEY_SECRETKEY, user.getSecretKey());
            }
        }
        
        String bucketName = getUserData(account, AbsLoginHandler.KEY_BUCKETNAME);
        if (bucketName == null || bucketName.trim().length() == 0) {
            if (user.getBucketName() != null && user.getBucketName().trim().length() > 0) {
                setUserData(account, AbsLoginHandler.KEY_BUCKETNAME, user.getBucketName());
            }
        }
        
        String ossType = getUserData(account, AbsLoginHandler.KEY_OSSTYPE);
        if (ossType == null || ossType.trim().length() == 0) {
            if (user.getOssType() != null && user.getOssType().trim().length() > 0) {
                setUserData(account, AbsLoginHandler.KEY_OSSTYPE, user.getOssType());
            }
        }
        
        String ossLocal = getUserData(account, AbsLoginHandler.KEY_OSSLOCAL);
        if (ossLocal == null || ossLocal.trim().length() == 0) {
            if (user.getOssLocal() != null && user.getOssLocal().trim().length() > 0) {
                setUserData(account, AbsLoginHandler.KEY_OSSLOCAL, user.getOssLocal());
            }
        }
        
        String sdkType = getUserData(account, AbsLoginHandler.KEY_SDK_TYPE);
        if (sdkType == null || sdkType.trim().length() == 0) {
            if (user.getSdkType() != null && user.getSdkType().trim().length() > 0) {
                setUserData(account, AbsLoginHandler.KEY_SDK_TYPE, user.getSdkType());
            } else {
                setUserData(account, AbsLoginHandler.KEY_SDK_TYPE, AbsLoginHandler.SDK_TYPE_ANONYMOUS);
            }
        }
        
        setUserData(account, AbsLoginHandler.KEY_FIELD1, user.getField1());
        setUserData(account, AbsLoginHandler.KEY_FIELD2, user.getField2());
        setUserData(account, AbsLoginHandler.KEY_FIELD3, user.getField3());
        setUserData(account, AbsLoginHandler.KEY_FIELD4, user.getField4());
        setUserData(account, AbsLoginHandler.KEY_FIELD5, user.getField5());
    }
    
    private HashMap<String, String> getUserMapData(Account account) {
        HashMap<String, String> userData = new HashMap<String, String>();
        
        userData.put(AbsLoginHandler.KEY_USER_KEY, getUserData(account, AbsLoginHandler.KEY_USER_KEY));
        userData.put(AbsLoginHandler.KEY_UID, getUserData(account, AbsLoginHandler.KEY_UID));
        userData.put(AbsLoginHandler.KEY_ACCESS_TOKEN, getUserData(account, AbsLoginHandler.KEY_ACCESS_TOKEN));
        userData.put(AbsLoginHandler.KEY_EXPIRES_IN, getUserData(account, AbsLoginHandler.KEY_EXPIRES_IN));
        userData.put(AbsLoginHandler.KEY_MOBILE, getUserData(account, AbsLoginHandler.KEY_MOBILE));
        userData.put(AbsLoginHandler.KEY_EMAIL, getUserData(account, AbsLoginHandler.KEY_EMAIL));
        userData.put(AbsLoginHandler.KEY_IMEI, getUserData(account, AbsLoginHandler.KEY_IMEI));
        
        userData.put(AbsLoginHandler.KEY_REGISTERDATE, getUserData(account, AbsLoginHandler.KEY_REGISTERDATE));
        
        userData.put(AbsLoginHandler.KEY_NICKNAME, getUserData(account, AbsLoginHandler.KEY_NICKNAME));
        userData.put(AbsLoginHandler.KEY_GENDER, getUserData(account, AbsLoginHandler.KEY_GENDER));
        userData.put(AbsLoginHandler.KEY_BIRTHDAY, getUserData(account, AbsLoginHandler.KEY_BIRTHDAY));
        userData.put(AbsLoginHandler.KEY_ADDRESS, getUserData(account, AbsLoginHandler.KEY_ADDRESS));
        userData.put(AbsLoginHandler.KEY_AVATAR, getUserData(account, AbsLoginHandler.KEY_AVATAR));
        userData.put(AbsLoginHandler.KEY_AVATAR_PATH, getUserData(account, AbsLoginHandler.KEY_AVATAR_PATH));
        
        userData.put(AbsLoginHandler.KEY_HOMETOWN, getUserData(account, AbsLoginHandler.KEY_HOMETOWN));
        userData.put(AbsLoginHandler.KEY_COUNTRY, getUserData(account, AbsLoginHandler.KEY_COUNTRY));
        userData.put(AbsLoginHandler.KEY_PROVINCE, getUserData(account, AbsLoginHandler.KEY_PROVINCE));
        userData.put(AbsLoginHandler.KEY_CITY, getUserData(account, AbsLoginHandler.KEY_CITY));
        userData.put(AbsLoginHandler.KEY_PROFESSION, getUserData(account, AbsLoginHandler.KEY_PROFESSION));
        
        userData.put(AbsLoginHandler.KEY_ACCESSID, getUserData(account, AbsLoginHandler.KEY_ACCESSID));
        userData.put(AbsLoginHandler.KEY_SECRETKEY, getUserData(account, AbsLoginHandler.KEY_SECRETKEY));
        userData.put(AbsLoginHandler.KEY_BUCKETNAME, getUserData(account, AbsLoginHandler.KEY_BUCKETNAME));
        userData.put(AbsLoginHandler.KEY_OSSTYPE, getUserData(account, AbsLoginHandler.KEY_OSSTYPE));
        userData.put(AbsLoginHandler.KEY_OSSLOCAL, getUserData(account, AbsLoginHandler.KEY_OSSLOCAL));
        userData.put(AbsLoginHandler.KEY_SDK_TYPE, getUserData(account, AbsLoginHandler.KEY_SDK_TYPE));
        
        userData.put(AbsLoginHandler.KEY_FIELD1, getUserData(account, AbsLoginHandler.KEY_FIELD1));
        userData.put(AbsLoginHandler.KEY_FIELD2, getUserData(account, AbsLoginHandler.KEY_FIELD2));
        userData.put(AbsLoginHandler.KEY_FIELD3, getUserData(account, AbsLoginHandler.KEY_FIELD3));
        userData.put(AbsLoginHandler.KEY_FIELD4, getUserData(account, AbsLoginHandler.KEY_FIELD4));
        userData.put(AbsLoginHandler.KEY_FIELD5, getUserData(account, AbsLoginHandler.KEY_FIELD5));
        
        return userData;
    }
    
    private User getUserData(Account account) {
        User user = new User();
        
        String userKey = getUserData(account, AbsLoginHandler.KEY_USER_KEY);
        mLogUtil.i("getUserData", "id:%s", userKey);
        if (userKey != null && userKey.trim().length() != 0) {
            user.setId(Long.valueOf(userKey));
        }
        user.setUserName(getUserData(account, AbsLoginHandler.KEY_UID));
        user.setPassword(getUserData(account, AbsLoginHandler.KEY_ACCESS_TOKEN));
        user.setExpiresIn(getUserData(account, AbsLoginHandler.KEY_EXPIRES_IN));
        user.setMobile(getUserData(account, AbsLoginHandler.KEY_MOBILE));
        user.setEmail(getUserData(account, AbsLoginHandler.KEY_EMAIL));
        user.setImei(getUserData(account, AbsLoginHandler.KEY_IMEI));
        
        user.setRegisterDate(getUserData(account, AbsLoginHandler.KEY_REGISTERDATE));
        
        user.setNickName(getUserData(account, AbsLoginHandler.KEY_NICKNAME));
        user.setGender(getUserData(account, AbsLoginHandler.KEY_GENDER));
        user.setBirthday(getUserData(account, AbsLoginHandler.KEY_BIRTHDAY));
        user.setAddress(getUserData(account, AbsLoginHandler.KEY_ADDRESS));
        user.setAvatar(getUserData(account, AbsLoginHandler.KEY_AVATAR));
        
        user.setHomeTown(getUserData(account, AbsLoginHandler.KEY_HOMETOWN));
        user.setCountry(getUserData(account, AbsLoginHandler.KEY_COUNTRY));
        user.setProvince(getUserData(account, AbsLoginHandler.KEY_PROVINCE));
        user.setCity(getUserData(account, AbsLoginHandler.KEY_CITY));
        user.setProfession(getUserData(account, AbsLoginHandler.KEY_PROFESSION));
        
        user.setAccessId(getUserData(account, AbsLoginHandler.KEY_ACCESSID));
        user.setSecretKey(getUserData(account, AbsLoginHandler.KEY_SECRETKEY));
        user.setBucketName(getUserData(account, AbsLoginHandler.KEY_BUCKETNAME));
        user.setOssType(getUserData(account, AbsLoginHandler.KEY_OSSTYPE));
        user.setOssLocal(getUserData(account, AbsLoginHandler.KEY_OSSLOCAL));
        user.setSdkType(getUserData(account, AbsLoginHandler.KEY_SDK_TYPE));
        
        user.setField1(getUserData(account, AbsLoginHandler.KEY_FIELD1));
        user.setField2(getUserData(account, AbsLoginHandler.KEY_FIELD2));
        user.setField3(getUserData(account, AbsLoginHandler.KEY_FIELD3));
        user.setField4(getUserData(account, AbsLoginHandler.KEY_FIELD4));
        user.setField5(getUserData(account, AbsLoginHandler.KEY_FIELD5));
        
        mLogUtil.i("getUserData", "the user id is %d", user.getId());
        return user;
    }
    
//    private Bundle getUserBundle(HashMap<String, String> userMap) {
//        Bundle userData = new Bundle();
//        userData.putString(AbsLoginHandler.KEY_USER_KEY, userMap.get(AbsLoginHandler.KEY_USER_KEY));
//        userData.putString(AbsLoginHandler.KEY_UID, userMap.get(AbsLoginHandler.KEY_UID));
//        userData.putString(AbsLoginHandler.KEY_ACCESS_TOKEN, userMap.get(AbsLoginHandler.KEY_ACCESS_TOKEN));
//        userData.putString(AbsLoginHandler.KEY_EXPIRES_IN, userMap.get(AbsLoginHandler.KEY_EXPIRES_IN));
//        userData.putString(AbsLoginHandler.KEY_MOBILE, userMap.get(AbsLoginHandler.KEY_MOBILE));
//        userData.putString(AbsLoginHandler.KEY_EMAIL, userMap.get(AbsLoginHandler.KEY_EMAIL));
//        userData.putString(AbsLoginHandler.KEY_IMEI, userMap.get(AbsLoginHandler.KEY_IMEI));
//        
//        userData.putString(AbsLoginHandler.KEY_REGISTERDATE, userMap.get(AbsLoginHandler.KEY_REGISTERDATE));
//        
//        userData.putString(AbsLoginHandler.KEY_NICKNAME, userMap.get(AbsLoginHandler.KEY_NICKNAME));
//        userData.putString(AbsLoginHandler.KEY_GENDER, userMap.get(AbsLoginHandler.KEY_GENDER));
//        userData.putString(AbsLoginHandler.KEY_BIRTHDAY, userMap.get(AbsLoginHandler.KEY_BIRTHDAY));
//        userData.putString(AbsLoginHandler.KEY_ADDRESS, userMap.get(AbsLoginHandler.KEY_ADDRESS));
//        userData.putString(AbsLoginHandler.KEY_AVATAR, userMap.get(AbsLoginHandler.KEY_AVATAR));
//        userData.putString(AbsLoginHandler.KEY_AVATAR_PATH, userMap.get(AbsLoginHandler.KEY_AVATAR_PATH));
//        
//        userData.putString(AbsLoginHandler.KEY_HOMETOWN, userMap.get(AbsLoginHandler.KEY_HOMETOWN));
//        userData.putString(AbsLoginHandler.KEY_COUNTRY, userMap.get(AbsLoginHandler.KEY_COUNTRY));
//        userData.putString(AbsLoginHandler.KEY_PROVINCE, userMap.get(AbsLoginHandler.KEY_PROVINCE));
//        userData.putString(AbsLoginHandler.KEY_CITY, userMap.get(AbsLoginHandler.KEY_CITY));
//        userData.putString(AbsLoginHandler.KEY_PROFESSION, userMap.get(AbsLoginHandler.KEY_PROFESSION));
//        
//        userData.putString(AbsLoginHandler.KEY_ACCESSID, userMap.get(AbsLoginHandler.KEY_ACCESSID));
//        userData.putString(AbsLoginHandler.KEY_SECRETKEY, userMap.get(AbsLoginHandler.KEY_SECRETKEY));
//        userData.putString(AbsLoginHandler.KEY_BUCKETNAME, userMap.get(AbsLoginHandler.KEY_BUCKETNAME));
//        userData.putString(AbsLoginHandler.KEY_OSSTYPE, userMap.get(AbsLoginHandler.KEY_OSSTYPE));
//        userData.putString(AbsLoginHandler.KEY_OSSLOCAL, userMap.get(AbsLoginHandler.KEY_OSSLOCAL));
//        userData.putString(AbsLoginHandler.KEY_SDK_TYPE, userMap.get(AbsLoginHandler.KEY_SDK_TYPE));
//        
//        userData.putString(AbsLoginHandler.KEY_FIELD1, userMap.get(AbsLoginHandler.KEY_FIELD1));
//        userData.putString(AbsLoginHandler.KEY_FIELD2, userMap.get(AbsLoginHandler.KEY_FIELD2));
//        userData.putString(AbsLoginHandler.KEY_FIELD3, userMap.get(AbsLoginHandler.KEY_FIELD3));
//        userData.putString(AbsLoginHandler.KEY_FIELD4, userMap.get(AbsLoginHandler.KEY_FIELD4));
//        userData.putString(AbsLoginHandler.KEY_FIELD5, userMap.get(AbsLoginHandler.KEY_FIELD5));
//        
//        return userData;
//    }
    
    private Bundle getUserBundle(User user) {
        Bundle userData = new Bundle();
        
        userData.putString(AbsLoginHandler.KEY_USER_KEY, user.getId().toString());
        userData.putString(AbsLoginHandler.KEY_UID, user.getUserName());
        userData.putString(AbsLoginHandler.KEY_ACCESS_TOKEN, user.getPassword());
        userData.putString(AbsLoginHandler.KEY_EXPIRES_IN, user.getExpiresIn());
        userData.putString(AbsLoginHandler.KEY_MOBILE, user.getMobile());
        userData.putString(AbsLoginHandler.KEY_EMAIL, user.getEmail());
        userData.putString(AbsLoginHandler.KEY_IMEI, user.getImei());
        
        userData.putString(AbsLoginHandler.KEY_REGISTERDATE, user.getRegisterDate());
        
        userData.putString(AbsLoginHandler.KEY_NICKNAME, user.getNickName());
        userData.putString(AbsLoginHandler.KEY_GENDER, user.getGender());
        userData.putString(AbsLoginHandler.KEY_BIRTHDAY, user.getBirthday());
        userData.putString(AbsLoginHandler.KEY_ADDRESS, user.getAddress());
        userData.putString(AbsLoginHandler.KEY_AVATAR, user.getAvatar());
        if (user.getAvatar() != null && user.getAvatar().trim().length() != 0 && 
                user.getSdkType().equals(AbsLoginHandler.SDK_TYPE_CLOUD)) {
            if (mAvatarManager == null) {
                mAvatarManager = new AvatarManager(mContext);
            }
            userData.putString(AbsLoginHandler.KEY_AVATAR_PATH,
                    mAvatarManager.getPicturePath(user.getId().toString()));
        } else {
            userData.putString(AbsLoginHandler.KEY_AVATAR_PATH, "");
        }
        
        userData.putString(AbsLoginHandler.KEY_HOMETOWN, user.getHomeTown());
        userData.putString(AbsLoginHandler.KEY_COUNTRY, user.getCountry());
        userData.putString(AbsLoginHandler.KEY_PROVINCE, user.getProvince());
        userData.putString(AbsLoginHandler.KEY_CITY, user.getCity());
        userData.putString(AbsLoginHandler.KEY_PROFESSION, user.getProfession());
        
        userData.putString(AbsLoginHandler.KEY_ACCESSID, user.getAccessId());
        userData.putString(AbsLoginHandler.KEY_SECRETKEY, user.getSecretKey());
        userData.putString(AbsLoginHandler.KEY_BUCKETNAME, user.getBucketName());
        userData.putString(AbsLoginHandler.KEY_OSSTYPE, user.getOssType());
        userData.putString(AbsLoginHandler.KEY_OSSLOCAL, user.getOssLocal());
        userData.putString(AbsLoginHandler.KEY_SDK_TYPE, user.getSdkType());
        
        userData.putString(AbsLoginHandler.KEY_FIELD1, user.getField1());
        userData.putString(AbsLoginHandler.KEY_FIELD2, user.getField2());
        userData.putString(AbsLoginHandler.KEY_FIELD3, user.getField3());
        userData.putString(AbsLoginHandler.KEY_FIELD4, user.getField4());
        userData.putString(AbsLoginHandler.KEY_FIELD5, user.getField5());
        
        mLogUtil.i("getUserBundle", "the user id is %s", userData.getString(AbsLoginHandler.KEY_USER_KEY));
        return userData;
    }
}

package com.ape.onelogin.service;

import java.util.HashMap;
import java.util.Map;

import net.tsz.afinal.core.FileNameGenerator;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.myos.ui.AvatarManager.AvatarDisplay;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.LogUtil;
import com.ape.onelogin.util.RegexValidate;
import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.R;

public class CloudSdkService extends Service {
    private Context mContext;
    private CloudLoginHandler mCloudLoginHandler;
    private AuthenticatorManager mAuthenticatorManager;
    private IQueryCompletionCallback mCallback;
    final RemoteCallbackList<IQueryCompletionCallback> mQueryCallbacks = new RemoteCallbackList <IQueryCompletionCallback>(); 
    final RemoteCallbackList<IModifyCompletionCallback> mModifyCallbacks = new RemoteCallbackList <IModifyCompletionCallback>();
    final RemoteCallbackList<IAvatarCompletionCallback> mAvatarCallbacks = new RemoteCallbackList<IAvatarCompletionCallback>();
    
    private LogUtil mLogUtil;
    private static final String TAG = "CloudSdkService";
    
    @Override
    public void onCreate() {
        super.onCreate();
        mLogUtil = new LogUtil(TAG);
        mContext = this;
        mCloudLoginHandler = CloudLoginHandler.getInstance(mContext);
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return stub;
    }
    
    private ICloudSdkService.Stub stub = new ICloudSdkService.Stub() {

        @Override
        public boolean isSessionValid() throws RemoteException {
            mLogUtil.i("enter isSessionValid");
            String sdkType = mAuthenticatorManager.getSdkType();
            if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(sdkType) ||
                    AbsLoginHandler.SDK_TYPE_SINA.equals(sdkType) ||
                    AbsLoginHandler.SDK_TYPE_TENCENT.equals(sdkType)) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getUserName() throws RemoteException {
            mLogUtil.i("enter getUserName");
            String userName = mAuthenticatorManager.getShowName();
            if(userName == null) {
                return mContext.getResources().getString(R.string.unknown_user_ex);
            }
            return userName;
        }
        
        @Override
        public void getUserKey() throws RemoteException {
            mLogUtil.i("enter getUserKey");
            if (mCallback == null) {
                return;
            }
            
            if (!mAuthenticatorManager.isSessionValid()) {
                mLogUtil.i("getUserKey", "the local cache is empty!!");
                SharedPreferences sharedPreferences =  mContext.getSharedPreferences(
                        Constants.PREFERENCE_NAME_GUEST, Context.MODE_APPEND);
                String userKey = sharedPreferences.getString(Constants.KEY_USER_KEY, "");
                String checkCode = sharedPreferences.getString(Constants.KEY_CHECK_CODE, "");
                if (userKey == null || userKey.trim().length() == 0 ||
                        checkCode == null || checkCode.trim().length() == 0) {
                    String imei = ContextUtils.getImei(mContext);
                    checkCode = FileNameGenerator.generator(String.valueOf(System.currentTimeMillis()) + imei);
                    final User user = new User();
                    user.setSdkType(AbsLoginHandler.SDK_TYPE_ANONYMOUS);
                    user.setUserName(checkCode);
                    user.setImei(imei);
                    
                    new Thread() {
                        public void run() {
                            Looper.prepare();
                            mLogUtil.i("getUserKey", "login by guest, userName:%s", user.getUserName());
                            mCloudLoginHandler.loginByGuest(user, CloudSdkService.this);
                            Looper.loop();
                        };
                    }.start();
                } else {
                    User user = new User();
                    user.setId(Long.valueOf(userKey));
                    user.setUserName(checkCode);
                    user.setSdkType(AbsLoginHandler.SDK_TYPE_ANONYMOUS);
                    mAuthenticatorManager.handleUser(user);
                    mLogUtil.i("getUserKey", "get guest information from cache, id:%d, userName:%s",
                            user.getId(), user.getUserName());
                    beginBroadcastForQuery(mAuthenticatorManager.getUserMapData());
                }
                return;
            } else if (mAuthenticatorManager.getUserKey() != null) {
                HashMap<String, String> userMap;
                String sdkType = mAuthenticatorManager.getUserData(AbsLoginHandler.KEY_SDK_TYPE);
                mLogUtil.i("getUserKey", "the local cache is exist!!");
                userMap = mAuthenticatorManager.getUserMapData();
                if (mAuthenticatorManager.isTimeout() && AbsLoginHandler.SDK_TYPE_CLOUD.equals(sdkType)) {
                    // 如果缓存过期的账户是云账户,则自动登录
                    new Thread() {
                        public void run() {
                            Looper.prepare();
                            mLogUtil.i("getUserKey", "the local cache is timeout, login again!!");
                            mCloudLoginHandler.login(CloudSdkService.this);
                            Looper.loop();
                        };
                    }.start();
                    return;
                }
                
                if (userMap == null) {
                    mLogUtil.i("getUserKey", "the local cache is exist, but usermap is null!!!");
                    userMap = new HashMap<String, String>();
                    userMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
                }
                beginBroadcastForQuery(userMap);
            }
        }

        @Override
        public void registerQueryCallback(IQueryCompletionCallback callback)
                throws RemoteException {
            mCallback = callback;
            if (mQueryCallbacks != null) {
                mQueryCallbacks.register(callback);
            }
        }

        @Override
        public void unRegisterQueryCallback(IQueryCompletionCallback callback)
                throws RemoteException {
            if (mQueryCallbacks != null) {
                mQueryCallbacks.unregister(callback);
            }
        }

        @Override
        public void logout() throws RemoteException {
            mLogUtil.w("logout", "<Account Logout>");
            HashMap<String,String> userMapInfo = new HashMap<String, String>();
            userMapInfo.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
            beginBroadcastForQuery(userMapInfo);
        }
        
        private String getPreferenceUserKey() {
            SharedPreferences pref =  mContext.getSharedPreferences(
                            Constants.PREFERENCE_NAME_GUEST, Context.MODE_APPEND);
            return pref.getString(Constants.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
        }
        
        @Override
        public void registerModifyCallback(IModifyCompletionCallback callback)
                throws RemoteException {
            if(mModifyCallbacks != null) {
                mModifyCallbacks.register(callback);
            }
        }

        @Override
        public void unRegisterModifyCallback(IModifyCompletionCallback callback)
                throws RemoteException {
            if(mModifyCallbacks != null) {
                mModifyCallbacks.unregister(callback);
            }
        }

        @Override
        public void modifyUserInfo(Map userInfoMap) throws RemoteException {
            modifyUser(userInfoMap);
        }

        @Override
        public void registerAvatarCallback(IAvatarCompletionCallback callback)
                throws RemoteException {
            if (mAvatarCallbacks != null) {
                mAvatarCallbacks.register(callback);
            }
        }

        @Override
        public void unRegisterAvatarCallback(IAvatarCompletionCallback callback)
                throws RemoteException {
            if (mAvatarCallbacks != null) {
                mAvatarCallbacks.unregister(callback);
            }
        }
        
        @Override
        public void getAvatar() throws RemoteException {
            AvatarDisplay avatarDisplay = new AvatarDisplay(mContext, new AuthenticListener() {
                
                @Override
                public void onComplete(int result, Object obj, String message) {
                    HashMap<String, String> avatarMap = new HashMap<String, String>();
                    avatarMap.put(AbsLoginHandler.KEY_USER_KEY, AbsLoginHandler.INVALID_USER_KEY);
                    avatarMap.put(AbsLoginHandler.KEY_AVATAR_PATH, "");
                    
                    String userKey = mAuthenticatorManager.getUserKey();
                    if (userKey != null && userKey.trim().length() > 0) {
                        avatarMap.put(AbsLoginHandler.KEY_USER_KEY, userKey);
                        if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                            avatarMap.put(AbsLoginHandler.KEY_AVATAR_PATH, mAuthenticatorManager.getAvatarPath());
                        }
                    }
                    beginBroadcastForAvatar(avatarMap);
                }
                
                @Override
                public void onCancel() {
                    // TODO Auto-generated method stub
                }
            });
            
            avatarDisplay.showAvatar(mAuthenticatorManager.getUserMapData());
        }
    };
    
    public synchronized void beginBroadcastForQuery(HashMap<String,String> userMapInfo) {
        if (userMapInfo == null) {
            mLogUtil.i("beginBroadcastForQuery", "the hash map is null!");
        } else if (userMapInfo.size() == 0) {
            mLogUtil.i("beginBroadcastForQuery", "the size of hash map is 0!");
        } else {
            mLogUtil.i("beginBroadcastForQuery", "begin broadcast user information, userMap:%s",
                    LogUtil.formatUserMessage(userMapInfo));
        }
        
//        String userKey = userMapInfo.get(AbsLoginHandler.KEY_USER_KEY);
//        Long id = Long.valueOf(AbsLoginHandler.INVALID_USER_KEY);
//        try {
//            id = Long.valueOf(userKey);
////            mLogUtil.i("beginBroadcastForQuery", "user id:%d", id);
//        } catch (Exception e) {
//            mLogUtil.i("beginBroadcastForQuery", "userKey must be decode! userKey:%s", userKey);
//            userKey = Utils.getPlainUserKey(userKey);
//            mLogUtil.i("beginBroadcastForQuery", "decode finish! userKey:%s", userKey);
//            userMapInfo.put(AbsLoginHandler.KEY_USER_KEY, userKey);
//        }
        
        int size = 0;
        try {
            size = mQueryCallbacks.beginBroadcast();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0;i < size;i++) { 
            try {
                mQueryCallbacks.getBroadcastItem(i).onQueryCompletion(userMapInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mQueryCallbacks.finishBroadcast();
    }
    
    public void beginBroadcastForAvatar(HashMap<String,String> userMapInfo) {
        if (userMapInfo == null) {
            mLogUtil.i("beginBroadcastForAvatar", "the hash map is null!");
        } else if (userMapInfo.size() == 0) {
            mLogUtil.i("beginBroadcastForAvatar", "the size of hash map is 0!");
        } else {
            mLogUtil.i("beginBroadcastForAvatar", "begin broadcast user information, userMap:%s",
                    LogUtil.formatAvatarMessage(userMapInfo));
        }
        
//        String userKey = userMapInfo.get(AbsLoginHandler.KEY_USER_KEY);
//        try {
//            Long id = Long.valueOf(userKey);
////            mLogUtil.i("beginBroadcastForAvatar", "user id:%d", id);
//        } catch (Exception e) {
//            mLogUtil.i("beginBroadcastForAvatar", "userKey must be decode! userKey:%s", userKey);
//            userKey = Utils.getPlainUserKey(userKey);
//            mLogUtil.i("beginBroadcastForAvatar", "decode finish! userKey:%s", userKey);
//            userMapInfo.put(AbsLoginHandler.KEY_USER_KEY, userKey);
//        }
        
        int size = mAvatarCallbacks.beginBroadcast();
        for (int i = 0; i < size; i ++) {
            try {
                mAvatarCallbacks.getBroadcastItem(i).onAvatarCompletion(userMapInfo);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mAvatarCallbacks.finishBroadcast();
    }
    
    /**
     * 1:修改成功
     * -1:修改失败
     * -2：电子邮件不符规则
     * -3：电话不符规则
     * @param result
     */
    public void beginBroadcastForModify(int result) {
        int size = mModifyCallbacks.beginBroadcast();
        for(int i = 0; i < size; i++) {
            try {
                mModifyCallbacks.getBroadcastItem(i).onModifyCompletion(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        mModifyCallbacks.finishBroadcast();
    }
    
    /**
     * email
     * phone
     * birthday
     * nickname
     * hometown
     * address
     * country
     * province
     * city
     * profession
     * field1
     * field2
     * field3
     * field4
     * field5
     * @param map
     */
    private void modifyUser(final Map map) {
        if(map == null || map.size() == 0 || !mAuthenticatorManager.isSessionValid()) {
            beginBroadcastForModify(-1);
            return;
        }
        final String email = (String) map.get(AbsLoginHandler.KEY_EMAIL);
        final String phone = (String) map.get(AbsLoginHandler.KEY_MOBILE);
        final String gender = (String) map.get(AbsLoginHandler.KEY_GENDER);
        final String birthday = (String) map.get(AbsLoginHandler.KEY_BIRTHDAY);
        final String nickname = (String) map.get(AbsLoginHandler.KEY_NICKNAME);
        final String hometown = (String) map.get(AbsLoginHandler.KEY_HOMETOWN);
        final String address = (String) map.get(AbsLoginHandler.KEY_ADDRESS);
        final String country = (String) map.get(AbsLoginHandler.KEY_COUNTRY);
        final String province = (String) map.get(AbsLoginHandler.KEY_PROVINCE);
        final String city = (String) map.get(AbsLoginHandler.KEY_CITY);
        final String profession = (String) map.get(AbsLoginHandler.KEY_PROFESSION);
        final String field1 = (String) map.get(AbsLoginHandler.KEY_FIELD1);
        final String field2 = (String) map.get(AbsLoginHandler.KEY_FIELD2);
        final String field3 = (String) map.get(AbsLoginHandler.KEY_FIELD3);
        final String field4 = (String) map.get(AbsLoginHandler.KEY_FIELD4);
        final String field5 = (String) map.get(AbsLoginHandler.KEY_FIELD5);
        
        if(phone != null && phone.trim().length() != 0) {
            if(!RegexValidate.isMobilPhone(phone)) {
                beginBroadcastForModify(-3);
                return;
            }
        }
        
        if(email != null && email.trim().length() != 0) {
            if(!RegexValidate.isEMail(email)) {
                beginBroadcastForModify(-2);
                return;
            }
        }
        new Thread() {
            public void run() {
                User user = mAuthenticatorManager.getUserData();
                user.setMobile(phone);
                user.setEmail(email);
                if(gender != null && gender.trim().length() != 0) {
                    user.setGender(gender);
                }
                if(birthday != null && birthday.trim().length() != 0) {
                    user.setBirthday(birthday);
                }
                if(nickname != null && nickname.trim().length() != 0) {
                    user.setNickName(nickname);
                }
                if(hometown != null && hometown.trim().length() != 0) {
                    user.setHomeTown(hometown);
                }
                if(address != null && address.trim().length() != 0) {
                    user.setAddress(address);
                }
                if(country != null && country.trim().length() != 0) {
                    user.setCountry(country);
                }
                if(province != null && province.trim().length() != 0) {
                    user.setProvince(province);
                }
                if(city != null && city.trim().length() != 0) {
                    user.setCity(city);
                }
                if(profession != null && profession.trim().length() != 0) {
                    user.setProfession(profession);
                }
                if(field1 != null && field1.trim().length() != 0) {
                    user.setField1(field1);
                }
                if(field2 != null && field2.trim().length() != 0) {
                    user.setField2(field2);
                }
                if(field3 != null && field3.trim().length() != 0) {
                    user.setField3(field3);
                }
                if(field4 != null && field4.trim().length() != 0) {
                    user.setField4(field4);
                }
                if(field5 != null && field5.trim().length() != 0) {
                    user.setField5(field5);
                }
                
                Looper.prepare();
                final User tmp = user;
                CloudLoginHandler handler = CloudLoginHandler.getInstance(mContext);
                handler.updateUser(user, new AuthenticListener() {
                    
                    @Override
                    public void onComplete(int result, Object obj, String message) {
                        if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                            mAuthenticatorManager.handleUser(tmp);
                            beginBroadcastForModify(1);
                        } else {
                            beginBroadcastForModify(-1);
                        }
                    }
                    
                    @Override
                    public void onCancel() {
                        beginBroadcastForModify(-1);
                    }
                });
                Looper.loop();
            };
        }.start();
    }
}

package com.ape.cloudfile;

import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.ape.cloudfile.module.UserCloudSpace;
import com.ape.filemanager.MyLog;
import com.cloud.client.sql.SqlResultListener;
import com.cloud.client.sql.SqlResultMultiListener;
import com.cloud.client.sql.SqlResultSingleListener;
import com.ape.onelogin.service.ICloudSdkService;
import com.ape.onelogin.service.IQueryCompletionCallback;

public class CloudUserInfomation
{
    protected static final String TAG = "CloudUserInfomation";
    private static final String PREF_USER_KEY = "user_key";
    private static final String PREF_USER_MAX_SPACE = "user_max_space";

    private static final int MSG_TO_GET_USER_KEY = 1;
    private static final int MSG_TO_GET_USER_INFO = 2;
    private static final int MSG_USE_SPACE_FINISHED = 3;
    private static final int MSG_ERROR_HAPPENED = 4;
    
    private static final int MSG_GET_USED_SPACE = 100;
    private static final int MSG_CREATE_MODULE_TABLE = 101;
    private static final int MSG_GET_MODULE_DATA = 102;
    private static final int MSG_INSERT_MODULE_DATA = 103;
    
    private static final int CLOUDE_SPACE_HANDLER = 1;

    private Map mUserInfoMap;
    private boolean mIsSessionValid;
    private String mUserKey;
    private String mUserName;
    private String mUserPortraitPath;
    
    private long mUsedSpace;
    private long mMaxSpace;
    
    private String mAccessId;
    private String mSecretKey;
    
    private Context mContext;
    private UserInformationListener mUserListener;
    private ICloudSdkService mCloudSdkService = null;

    private boolean misLoading = false;
    private boolean mIsInforComplete = false;
    
    public interface UserInformationListener
    {
        void userInformationFinish();
        void userKeyFinish(String userKey);
        void userInformationChanged(String userKey);
        void userInformationError(int errorCode);
        void userLogout();
    }
    
    public CloudUserInfomation(Context context, UserInformationListener listener)
    {
        mContext = context;
        mUserListener = listener;
        
        mUserKey = "";
        mUserName = "";
        mUsedSpace = 0;
        mMaxSpace = 0;
    }
    
    public void start()
    {
        misLoading = true;
        mIsInforComplete = false;
        bindUserService();
    }
    
    public void exit()
    {
        unBindUserService();
        if (mHandlerThread != null)
        {
            mHandlerThread.quit();
            mHandlerThread = null;
            mModuleDataHandler = null;
        }
    }

    private boolean bindUserService()
    {
        boolean retVal = false;

        if (mCloudSdkService == null && checkReferSDK(mContext))
        {
            Intent service = new Intent(CloudFileUtil.LOGIN_SERVICER_NAME);
            retVal = mContext.bindService(service, mCloudSdkServiceConn,
                    Service.BIND_AUTO_CREATE);
        }
        
        return retVal;
    }
    
    private void unBindUserService()
    {
        if (mCloudSdkService != null)
        {
            try {
                mCloudSdkService.unRegisterQueryCallback(mQueryUserKeyCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                mContext.unbindService(mCloudSdkServiceConn);
                mCloudSdkService = null;
            }
        }
    }
    
    public boolean isUserServiceBind()
    {
        return (mCloudSdkService != null);
    }

    public boolean getSessionValidFromUserMap()
    {
        boolean isSession = false;
        if (mUserInfoMap != null)
        {
            String type = (String) mUserInfoMap.get(CloudFileUtil.USER_MAP_TYPE_KEY);
            if (type != null
                    && !type.equals(CloudFileUtil.USER_MAP_TYPE_ANONYMOUS)
                    && !type.equals(CloudFileUtil.USER_MAP_TYPE_UNKNOWN))
            {
                isSession = true;
            }
        }

        return isSession;
    }

    private void getUserNameFromUserMap()
    {
        if (mCloudSdkService != null)
        {
            try
            {
                //mIsSessionValid = mCloudSdkService.isSessionValid();
                mIsSessionValid = getSessionValidFromUserMap();
                if (mIsSessionValid)
                {
                    mUserName = mCloudSdkService.getUserName();
                }
            } catch (RemoteException e)
            {
                MyLog.e(TAG, "get user infomation error, e:" + e);
            }
        }
    }

    private Handler mMainHandler = new Handler()
    {
        @Override
        public void dispatchMessage(Message msg)
        {
            switch (msg.what)
            {
                case MSG_TO_GET_USER_KEY:
                    MyLog.i(TAG, "before mCloudSdkService.getUserKey().");
                    if (mCloudSdkService != null)
                    {
                        try
                        {
                            mCloudSdkService.getUserKey();
                        } catch (RemoteException e)
                        {
                            MyLog.e(TAG, "get user key error, e:" + e);
                        }
                    }
                    break;
                    
                case MSG_TO_GET_USER_INFO:
                    getUserNameFromUserMap();

                    mUserKey = String.valueOf(msg.obj);
                    mUserPortraitPath = (String) mUserInfoMap.get("avatar_path");

                    if (!mIsSessionValid)
                    {
//                        misLoading = false;
//                        mIsInforComplete = true;
                        if (mUserListener != null)
                        {
                            mUserListener.userLogout();
                        }
//                        break;
                    }

                    initUserKey();
                    goToGetUserCloudSpace(CloudFileService.getInstance(mContext));
                    break;
                    
                case MSG_USE_SPACE_FINISHED:
                    misLoading = false;
                    mIsInforComplete = true;
                    if (mUserListener != null)
                    {
                        mUserListener.userInformationFinish();
                    }
                    break;

                case MSG_ERROR_HAPPENED:
                    misLoading = false;
                    mIsInforComplete = true;
                    if (mUserListener != null)
                    {
                        mUserListener.userInformationError(msg.arg1);
                    }
                    break;

                default:
                    break;
            }
        }
    };
    
    private void initUserKey()
    {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String oldUserKey = pref.getString(PREF_USER_KEY, "-1");

        CloudFileService service = CloudFileService.getInstance(mContext);
        service.setUserId(mUserKey);
        service.allocFileClient(mUserInfoMap);

        if (!oldUserKey.equals(mUserKey))
        {
            SharedPreferences.Editor editor = pref.edit();
            editor.putString(PREF_USER_KEY, mUserKey);
            editor.putLong(PREF_USER_MAX_SPACE, 0);
            editor.commit();
            
            if (mUserListener != null)
            {
                mUserListener.userInformationChanged(mUserKey);
            }
        } else
        {
            if (mUserListener != null)
            {
                mUserListener.userKeyFinish(mUserKey);
            }
        }
    }
    
    private ServiceConnection mCloudSdkServiceConn = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mCloudSdkService = (ICloudSdkService) ICloudSdkService.Stub
                    .asInterface(service);
            try
            {
                mCloudSdkService.registerQueryCallback(mQueryUserKeyCallBack);
            } catch (RemoteException e)
            {
                e.printStackTrace();
            }
            
            goToGetUserKey();
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            if (mCloudSdkService != null)
            {
                mCloudSdkService = null;
            }
        }
    };
    
    private IQueryCompletionCallback mQueryUserKeyCallBack = new IQueryCompletionCallback.Stub()
    {

        @Override
        public void onQueryCompletion(Map userInfoMap) throws RemoteException
        {
            mUserInfoMap = userInfoMap;

            String userKey = (String) mUserInfoMap.get("userkey");
            String portraitPath = (String) mUserInfoMap.get("avatar_path");
            boolean portraitSame = (portraitPath != null && portraitPath.equals(mUserPortraitPath));

            MyLog.i(TAG, "onQueryCompletion, userKey:" + userKey);
            if (userKey == null)
                throw new RuntimeException("onQueryCompletion, userkey is null");

            if (userKey.equals(mUserKey) && mMaxSpace > 0 && portraitSame)
            {
                getUserNameFromUserMap();
                mMainHandler.removeMessages(MSG_USE_SPACE_FINISHED);
                mMainHandler.sendEmptyMessage(MSG_USE_SPACE_FINISHED);
                return;
            }

            Message msg = mMainHandler.obtainMessage();
            msg.what = MSG_TO_GET_USER_INFO;
            msg.obj = userKey;
            mMainHandler.removeMessages(MSG_TO_GET_USER_INFO);
            mMainHandler.sendMessage(msg);
        }
    };
    
    public void goToGetUserKey()
    {
        if (mCloudSdkService != null)
        {
            misLoading = true;
            mIsInforComplete = false;
            Message msg = mMainHandler.obtainMessage();
            msg.what = MSG_TO_GET_USER_KEY;
            mMainHandler.removeMessages(MSG_TO_GET_USER_KEY);
            mMainHandler.sendMessage(msg);
        } else
        {
            start();
        }
    }
    
    // to get user cloud space begin.
    private CloudFileService mCloudFileTool;
    private HandlerThread mHandlerThread;
    private ModuleDataHandler mModuleDataHandler;
    public void goToGetUserCloudSpace(CloudFileService tool)
    {
        misLoading = true;
        mCloudFileTool = tool;
//        mCloudFileTool.setUserId(mUserKey);
//        mCloudFileTool.allocFileClient(mUserInfoMap);
        if (mHandlerThread == null)
        {
            mHandlerThread = new HandlerThread("UserCloudSpace");
            mHandlerThread.start();
            mModuleDataHandler = new ModuleDataHandler(mHandlerThread.getLooper());
        }
        
        mModuleDataHandler.removeMessages(MSG_GET_USED_SPACE);
        mModuleDataHandler.sendEmptyMessage(MSG_GET_USED_SPACE);
    }
    
    private void getCloudUsedSpaceResult(long result)
    {
        if (mModuleDataHandler == null || mHandlerThread == null)
        { // exit
            return;
        }

        if (result < 0)
        {
            mUsedSpace = -1;
            sendErrorCodeToMain((int) result);
        } else
        {
            mUsedSpace = result;
            
            // for demo.
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
//            long maxSpace = pref.getLong(PREF_USER_MAX_SPACE, 0);
            long maxSpace = CloudFileUtil.MAX_DEFAULT_USER_CLOUD_SPACE;
            if (maxSpace <= 0)
            {
                Message message = mModuleDataHandler.obtainMessage();
                message.what = MSG_CREATE_MODULE_TABLE;
                message.arg1 = CLOUDE_SPACE_HANDLER;
                mModuleDataHandler.removeMessages(MSG_CREATE_MODULE_TABLE);
                mModuleDataHandler.sendMessage(message);
            } else
            {
                mMaxSpace = maxSpace;
                mMainHandler.removeMessages(MSG_USE_SPACE_FINISHED);
                mMainHandler.sendEmptyMessage(MSG_USE_SPACE_FINISHED);
            }
        }
    }
    
    private void sendErrorCodeToMain(int errorCode)
    {
        mUsedSpace = -1;
        Message message = mMainHandler.obtainMessage();
        message.what = MSG_ERROR_HAPPENED;
        message.arg1 = errorCode;
        mMainHandler.removeMessages(MSG_ERROR_HAPPENED);
        mMainHandler.sendMessage(message);
    }
    
    private class CreateCloudSpaceTableResult implements SqlResultListener
    {
        @Override
        public void onSuccess(String msg)
        {
            if (mModuleDataHandler == null || mHandlerThread == null)
            { // exit
                return;
            }

            String where = "userKey=\'"+mUserKey+"\'";
            Message message = mModuleDataHandler.obtainMessage();
            message.what = MSG_GET_MODULE_DATA;
            message.obj = where;
            message.arg1 = CLOUDE_SPACE_HANDLER;
            mModuleDataHandler.removeMessages(MSG_GET_MODULE_DATA);
            mModuleDataHandler.sendMessage(message);
        }
        @Override
        public void onFailure(int errorNo, String msg)
        {
            sendErrorCodeToMain(errorNo);
        }
    }
    
    private class GetCloudSpaceResult implements SqlResultMultiListener<UserCloudSpace>
    {

        @Override
        public void onSuccess(List<UserCloudSpace> entities, String msg)
        {
            if (mModuleDataHandler == null || mHandlerThread == null)
            { // exit
                return;
            }

            if (entities.size() == 0)
            {
                UserCloudSpace item = new UserCloudSpace();
                item.setUserKey(mUserKey);
                item.setCloudSpace(CloudFileUtil.MAX_DEFAULT_USER_CLOUD_SPACE);
                Message message = mModuleDataHandler.obtainMessage();
                message.what = MSG_INSERT_MODULE_DATA;
                message.obj = item;
                message.arg1 = CLOUDE_SPACE_HANDLER;
                mModuleDataHandler.removeMessages(MSG_INSERT_MODULE_DATA);
                mModuleDataHandler.sendMessage(message);

                mUsedSpace = CloudFileUtil.MAX_DEFAULT_USER_CLOUD_SPACE;
            } else
            {
                UserCloudSpace item = entities.get(0);
                mUsedSpace = item.getCloudSpace();
            }

            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = pref.edit();
            editor.putLong(PREF_USER_MAX_SPACE, mUsedSpace);
            editor.commit();

            mMainHandler.removeMessages(MSG_USE_SPACE_FINISHED);
            mMainHandler.sendEmptyMessage(MSG_USE_SPACE_FINISHED);
        }

        @Override
        public void onFailure(int errorNo, String msg)
        {
            sendErrorCodeToMain(errorNo);
        }
    }
    
    private class SetCloudSpaceResult implements SqlResultSingleListener<UserCloudSpace>
    {

        @Override
        public void onSuccess(UserCloudSpace entity, String msg)
        {
        }

        @Override
        public void onFailure(int errorNo, String msg)
        {
            // TODO Auto-generated method stub
        }
    }
    
    private class ModuleDataHandler extends Handler
    {
        public ModuleDataHandler(Looper looper)
        {
            super(looper);
        }
        
        @Override
        public void handleMessage(Message msg)
        {
            if (mModuleDataHandler == null || mHandlerThread == null)
            {
                return;
            }

            switch (msg.what)
            {
                case MSG_GET_USED_SPACE:
                    try
                    {
                        long result = mCloudFileTool.getDirectorySize(CloudFileUtil.CLOUD_ROOT_DIR);
                        getCloudUsedSpaceResult(result);
                    } catch (Exception e)
                    {
                        getCloudUsedSpaceResult(-1);
                    }
                    break;

                case MSG_CREATE_MODULE_TABLE:
                    if (msg.arg1 == CLOUDE_SPACE_HANDLER)
                    {
                        mCloudFileTool.createModuleTable(UserCloudSpace.class,
                                new CreateCloudSpaceTableResult());
                    }
                    break;
                    
                case MSG_GET_MODULE_DATA:
                    String where = (String)msg.obj;
                    if (msg.arg1 == CLOUDE_SPACE_HANDLER)
                    {
                        mCloudFileTool.getModuleData(UserCloudSpace.class,
                                where, new GetCloudSpaceResult());
                    }
                    break;
                    
                case MSG_INSERT_MODULE_DATA:
                    if (msg.arg1 == CLOUDE_SPACE_HANDLER)
                    {
                        UserCloudSpace data = (UserCloudSpace) msg.obj;
                        mCloudFileTool.addModuleData(data,
                                new SetCloudSpaceResult());
                    }
                    break;

                default:
                    break;
            }
        }
    }
    // to get user cloud space end.
    
    static public boolean checkReferSDK(Context context)
    {
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> allAppInfoList = pm.getInstalledApplications(0);

        for (ApplicationInfo appInfo : allAppInfoList)
        {
            if (appInfo.packageName.equalsIgnoreCase(CloudFileUtil.REFER_APK))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isSpaceInformationValid()
    {
        return (mUsedSpace >= 0 && mMaxSpace > 0);
    }

    public boolean isInvalidUserKey()
    {
        return TextUtils.isEmpty(mUserKey) || mUserKey.equals("-1");
    }

    public boolean isSessionValid()
    {
        return mIsSessionValid;
    }

    public void setIsSessionValid(boolean isSessionValid)
    {
        this.mIsSessionValid = isSessionValid;
    }

    public String getUserPortraitPath()
    {
        return mUserPortraitPath;
    }

    public String getUserKey()
    {
        return mUserKey;
    }

    public void setUserKey(String userKey)
    {
        this.mUserKey = userKey;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public void setUserName(String userName)
    {
        this.mUserName = userName;
    }

    public long getUsedSpace()
    {
        return mUsedSpace;
    }

    public void setUsedSpace(long usedSpace)
    {
        this.mUsedSpace = usedSpace;
    }

    public long getMaxSpace()
    {
        return mMaxSpace;
    }

    public void setMaxSpace(long maxSpace)
    {
        this.mMaxSpace = maxSpace;
    }

    public String getAccessId()
    {
        return mAccessId;
    }

    public void setAccessId(String mAccessId)
    {
        this.mAccessId = mAccessId;
    }

    public String getSecretKey()
    {
        return mSecretKey;
    }

    public void setSecretKey(String mSecretKey)
    {
        this.mSecretKey = mSecretKey;
    }
    
    public boolean isInforComplete()
    {
        return mIsInforComplete;
    }

    public boolean isLoading()
    {
        return misLoading;
    }
}

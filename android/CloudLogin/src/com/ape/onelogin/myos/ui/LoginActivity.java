package com.ape.onelogin.myos.ui;

import java.util.HashMap;

import net.tsz.afinal.core.FileNameGenerator;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginManager;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.login.sina.SinaHandler;
import com.ape.onelogin.login.tencent.TencentHandler;
import com.ape.onelogin.myos.ui.AvatarManager.AvatarDisplay;
import com.ape.onelogin.myos.widget.MenuItem;
import com.ape.onelogin.myos.widget.MyOSActionBar;
import com.ape.onelogin.myos.widget.MyOSAlertDialog;
import com.ape.onelogin.myos.widget.MyOSEditText;
import com.ape.onelogin.myos.widget.ProgressDialog;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.Log;
import com.ape.onelogin.util.LogUtil;
import com.ape.onelogin.widget.CircleImageView;
import com.ape.onelogin.R;
import com.ape.onelogin.service.ICloudSdkService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 
 * @author juan.li
 *
 * <p>登录逻辑</p>
 * <p>1.无论注册,登录,更新账户,需要在界面上显示用户信息,都要通过{@link LoginManager#showUser(AuthenticListener)}方法</p>
 * <p>2.注册和登录会更新最近登录时间,编辑账户则不会更新最近登录时间</p>
 * <p>3.一次登录过程会以{@link LoginManager#login(Activity, User, AuthenticListener, String)}方法开始,
 *   以{@link #MSG_LOGIN_SUCCESS}消息返回结束</p>
 *   
 * <p>云账户登录过程</p>
 * <p>1.调用{@link LoginManager#login(Activity, User, AuthenticListener, String)}方法,登录用户若存在,将操作文件
 * 服务器的参数写在本地,否则退出</p>
 * <p>2.调用{@link AbsLoginHandler#onLogin(User, AuthenticListener)}方法,保存登录时间到服务器,并获取用户信息</p>
 * <p>3.调用{@link LoginManager#showUser(AuthenticListener)}方法,显示用户信息到界面</p>
 * 
 * <p>第三方登录过程</p>
 * <p>1.调用{@link LoginManager#login(Activity, User, AuthenticListener, String)}方法从第三方服务器获取授权</p>
 * <p>2.获取授权成功后,调用{@link AbsLoginHandler#onLogin(User, AuthenticListener)}方法,若帐号第一次登录,则通过
 * {@link AbsLoginHandler#register(User, AuthenticListener)}方法注册,以上操作成功后,将从第三方服务器重新获取用户信息,
 * 并连同登录(注册)时间一并更新到云服务器.</p>
 * <p>3.调用{@link LoginManager#showUser(AuthenticListener)}方法,显示用户信息到界面</p>
 * 
 * <p>账户注册</p>
 * <p>1.账户注册首先会检测本地是否存在匿名用户,若存在匿名用户则更新此账户,不存在则注册</p>
 * <p>2.第三方账户注册过程同登录过程</p>
 * 
 */
public class LoginActivity extends Activity {

    protected static final String TAG = "LoginActivity";
    
    private Context mContext;
    
    /**
     * ********************************************
     * <login UI>
     * ********************************************
     */
    private View mLoginLayout;
    private MyOSEditText mAccountEditText;
    private MyOSEditText mPasswordEditText;
    private CheckBox mShowPasswordBox;
    private ImageButton mTencentButton;
    private ImageButton mSinaButton;
    private TextView mForgetPassword;
    private TextView mRegister;
    private TextView mWarningView;
    private Button mLoginButton;
    
    private HasContentListener mHasContentListener;
    
    private static final int MASK_HAS_CONTENT_ALL                 = 0x0011;
    private static final int MASK_HAS_CONTENT_ACCOUNT             = 0x0001;
    private static final int MASK_HAS_CONTENT_PASSWORD            = 0x0010;
    
    /**
     * ********************************************
     * <user information UI>
     * ********************************************
     */
    private LinearLayout mUserLayout;
    private MyOSActionBar mActionBar;
    private CircleImageView mAvatar;
    private ImageButton mAvatarButton;
    private Button mLogoutButton;
    private TextView mNickName;
    private TextView mAccountName;
    private TextView mGender;
    private TextView mBirthday;
    private TextView mAddress;
    private TextView mMobile;
    private TextView mEmail;

    private LinearLayout mAccountLayout;
    private LinearLayout mGenderLayout;
    private LinearLayout mBirthdayLayout;
    private LinearLayout mAddressLayout;
    private LinearLayout mMobileLayout;
    private LinearLayout mEmaiLayout;
    
    private String userNameStr;
    private String nickNameStr;
    private String phoneStr;
    private String emailStr;
    private String genderStr;
    private String birthdayStr;
    private String addressStr;
    
    private ActionMenuItemClickListener mActionMenuItemClickListener;
    
    private static final int ACTIONBAR_MENU_EDIT_USER           = 100;
    private static final int ACTIONBAR_MENU_MODIFY_PASSWORD     = 101;
    
    /**
     * ********************************************
     * <common UI>
     * ********************************************
     */
    private ProgressDialog mProgressDialog;
    private ViewOnClickListener mViewOnClickListener;
    private CloudLoginHandler mCloudLoginHandler = null;
    private static TencentHandler mTencentHandler = null;
    private static SinaHandler mSinaHandler = null;
    
    private LoginManager mLoginManager;
    private boolean mIsCooLogin = false;
    private boolean mIsLoginCancel = false;
    private int mLoginAction = ACTION_LOGIN;
    
    private AvatarDisplay mAvatarDisplay;
    private AuthenticatorManager mAuthenticatorManager;
    private LogUtil mLogUtil;
    
    /**
     * 账户登录前会检测本地缓存是否过期,如果过期将重新登录.
     * 已现在的登录逻辑,如果不添加登录次数限制,则会陷入死循环
     */
    private int mLoginTimes = 0;
    private static final int MAX_LOGIN_TIMES = 1;
    
    private static final int MSG_LOGIN_SUCCESS                      =  1001;
    private static final int MSG_LOGOUT_SUCCESS                     =  1002;
    
    private static final int MSG_UNKNOWN_FAILURE                    = -1000;
    private static final int MSG_LOGIN_FAILURE                      = -1001;
    private static final int MSG_LOGIN_CANCEL                       = -1002;
    private static final int MSG_CACHE_INVALID                      = -1003;
    private static final int MSG_COOPERATION_LOGIN_FAILURE          = -1004;
    private static final int MSG_COOPERATION_CACHE_INVALID          = -1005;
    private static final int MSG_LOGOUT_FAILURE                     = -1006;
    
    private static final int REQ_FOR_REGISTER                       = 2001;
    
    // 显示用户信息,无需退出当前界面
    private static final int ACTION_SHOW_USER                       = 0x001;
    // 登录,登录完成后,退出登录界面
    private static final int ACTION_LOGIN                           = 0x002;
    
    private static final int PROGRESS_FORCE_CLOSE                   = -2000;
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        
        @SuppressLint("ShowToast")
        @Override
        public void handleMessage(Message msg) {
            closeProgressDialog(PROGRESS_FORCE_CLOSE);
            mLogUtil.i("handleMessage", "message.what:%d", msg.what);
            switch (msg.what) {
                case MSG_LOGIN_SUCCESS:
                    if (mIsCooLogin) {
                        mIsCooLogin = false;
                    }
                    finishLogin();
                    fillUser();
                    try {
                        if (OneLoginApplication.sCloudSdkService != null) {
                            OneLoginApplication.sCloudSdkService.getUserKey();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (mLoginAction == ACTION_LOGIN) {
                        finish();
                    }
                    break;
                    
                case MSG_LOGOUT_SUCCESS:
                    showUserContainer(false);
                    ContextUtils.showToast(mContext, R.string.user_logout_success, Toast.LENGTH_SHORT);
                    if (OneLoginApplication.sCloudSdkService != null) {
                        try {
                            OneLoginApplication.sCloudSdkService.logout();
                        } catch (RemoteException re) {
                            re.printStackTrace();
                        }
                    }
                    break;
                    
                case MSG_LOGIN_FAILURE:
                case MSG_COOPERATION_LOGIN_FAILURE:
                case MSG_UNKNOWN_FAILURE:
                    cancelLogin();
                    mAuthenticatorManager.removeUser();
                    String message;
                    if (msg.obj == null) {
                        message = mContext.getString(R.string.login_fail);
                    } else {
                        message = (String) msg.obj;
                    }
                    mWarningView.setText(message);
                    break;
                    
                case MSG_LOGOUT_FAILURE:
                    ContextUtils.showToast(mContext, R.string.user_logout_error, Toast.LENGTH_SHORT);
                    break;
                    
                case MSG_CACHE_INVALID:
                case MSG_COOPERATION_CACHE_INVALID:
                    mAuthenticatorManager.removeUser();
                    showUserContainer(false);
                    break;
                    
                case MSG_LOGIN_CANCEL:
                    cancelLogin();
                    break;
            }
        }
    };
    
    /**
     * 登录总控制监听
     * 
     * <p>1. 云账户登录成功后，则调用onLogin方法更新登录时间到服务器,并在界面上显示用户信息
     * <p>2. 第三方账户登录获取授权后，则调用相应的onLogin方法
     *   
     */
    private AuthenticListener mLoginAuthenticListener = new AuthenticListener() {
        
        @Override
        public void onComplete(int result, Object obj, String message) {
            mLogUtil.i("mLoginAuthenticListener", "Login onComplete result code:%d, message:%s",
                    result, message == null ? "" : message);
            closeProgressDialog(PROGRESS_FORCE_CLOSE);
            switch (result) {
                case LoginService.REP_LOGIN_SERVICE_SUCCESS: {
                    // 登录成功，检测user对象中的sdk，判断是否是第三方应用，若是则调用相应的onLogin方法
                    User user = (User) obj;
                    String sdk = user.getSdkType();
                    if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(sdk)) {
                        // 云账户登录成功，显示用信息到界面
                        showProgressDialog(R.string.user_login_load_user_info, PROGRESS_FORCE_CLOSE);
                        mCloudLoginHandler.onLogin(user, mUpdateAuthenticListener);
                    } else if (AbsLoginHandler.SDK_TYPE_SINA.equals(sdk)) {
                        mSinaHandler.onLogin(user, mUpdateAuthenticListener);
                    } else if (AbsLoginHandler.SDK_TYPE_TENCENT.equals(sdk)) {
                        mTencentHandler.onLogin(user, mUpdateAuthenticListener);
                    } else {
                        // 错误的sdk
                        closeProgressDialog(PROGRESS_FORCE_CLOSE);
                    }
                    break;
                }
                    
                case LoginService.REP_LOGIN_SERVICE_USER_NOT_EXIST:
                case LoginService.REP_LOGIN_SERVICE_UNIQUENESS_ERROR: {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_LOGIN_FAILURE;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                    break;
                }
                    
                default: {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_UNKNOWN_FAILURE;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                    break;
                }
            }
        }
        
        @Override
        public void onCancel() {
            // 登录取消
            mLogUtil.i("mLoginAuthenticListener", "Login cancel");
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_LOGIN_CANCEL;
            msg.obj = "login cancel";
            mHandler.sendMessage(msg);
        }
    };
    
    /**
     * onLogin登录监听
     * 
     * <p>1. 对于第三方账户,如果第一次登录，则会在云服务器上创建新用户
     * <p>2. 对于第三方账户,如果此账户已存在，则从云服务器上获取此用户信息
     * <p>3. 对于第三方账户,无论是否第一次登录，在登录成功后，则会从第三方服务器获取相关用户信息，并更新到云服务器上
     * <p>4. 对于云账户,则更新登录时间到云服务器
     * 
     */
    private AuthenticListener mUpdateAuthenticListener = new AuthenticListener() {
        
        @SuppressWarnings("unchecked")
        @Override
        public void onComplete(int result, Object obj, String message) {
            mLogUtil.i("mUpdateAuthenticListener", "onLogin onComplete result code:%d, message:%s",
                    result, message == null ? "" : message);
            switch (result) {
                case LoginService.REP_LOGIN_SERVICE_SUCCESS: {
                    // 登录成功，显示用信息到界面
                    User user = null;
                    if (obj instanceof User) {
                        user = (User)obj;
                    } else {
                        user = (User) ((HashMap<String, Object>) obj).get(Constants.REQ_SEND_MAIL_PARAM_2);
                    }
                    
                    mAuthenticatorManager.handleUser(user);
                    initUserData(ACTION_LOGIN);
                    
                    // 获取用户信息完成,加载头像
                    mAvatarDisplay.showAvatar(mAuthenticatorManager.getUserMapData());
                    break;
                }
                
                case LoginService.REP_LOGIN_SERVICE_USER_EXIST:
                case LoginService.REP_LOGIN_SERVICE_EMAIL_EXISTS: 
                case LoginService.REP_LOGIN_SERVICE_MOBILE_EXISTS: {
                    // 第三方账户登录失败
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_COOPERATION_LOGIN_FAILURE;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                    break;
                }
                
                default:
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_UNKNOWN_FAILURE;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                    break;
            }
        }
        
        @Override
        public void onCancel() {
            // 登录取消
            mLogUtil.i("mUpdateAuthenticListener", "onLogin cancel");
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_LOGIN_CANCEL;
            msg.obj = "login cancel";
            mHandler.sendMessage(msg);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myos_login_activity);
        mContext = this;
        mLogUtil = new LogUtil("LoginActivity");
        mLoginManager = OneLoginApplication.loginManager;
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
        mAuthenticatorManager.beginRequest(this);
        mViewOnClickListener = new ViewOnClickListener();
        
        mAvatarDisplay = new AvatarDisplay(mContext, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                mLogUtil.i("AvatarDisplay", "avatar display onComplete result code:%d, message:%s",
                        result, message == null ? "" : message);
                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    if (OneLoginApplication.sCloudSdkService != null) {
                        try {
                            OneLoginApplication.sCloudSdkService.getAvatar();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mAvatar.setImageBitmap((Bitmap) obj);
                }
            }
            
            @Override
            public void onCancel() {
                // TODO Auto-generated method stub
            }
        });
        
        initUserInfoUI();
        initLoginUI();
        
//        if (mCloudSdkService == null) {
//            Intent service = new Intent(this,CloudSdkService.class);
//            this.bindService(service, cloudSdkServiceConn, Service.BIND_AUTO_CREATE);
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuthenticatorManager.isSessionValid() && !mIsCooLogin) {
            // 非第三方账户登录,则加载用户数据
            initUserData(ACTION_SHOW_USER);
        } else {
            showUserContainer(false);
        }
        
        if (mIsCooLogin && !mIsLoginCancel) {
            // 第三方账户登录界面结束,跳转到LoginActivity界面,则显示进度条进行数据加载
            showProgressDialog(R.string.user_login_load_user_info, PROGRESS_FORCE_CLOSE);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mIsLoginCancel = true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQ_FOR_REGISTER:
                    Bundle resultBundle = data.getExtras();
                    String userId = resultBundle.getString("login_result");
                    if ("success".equals(userId)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(AbsLoginHandler.KEY_USER_KEY, mAuthenticatorManager.getUserKey());
                        bundle.putString(AbsLoginHandler.KEY_UID, mAuthenticatorManager.getUserName());
                        Intent intent = new Intent();
                        intent.putExtras(bundle);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    } else {
                        mIsLoginCancel = true;
                    }
                    break;
            }
        }
        
        if (mLoginManager.getSdkType() == AbsLoginHandler.SDK_TYPE_SINA &&
                SinaHandler.getInstance(mContext).getSsoHandler() != null && mIsCooLogin) {
            SinaHandler.getInstance(mContext).getSsoHandler().authorizeCallBack(requestCode, resultCode, data);
            mIsLoginCancel = false;
        }
        
        if (mLoginManager.getSdkType() == AbsLoginHandler.SDK_TYPE_TENCENT && 
                requestCode == com.tencent.connect.common.Constants.REQUEST_API && mIsCooLogin) {
            mIsLoginCancel = false;
            
        }
    }

    @Override
    protected void onDestroy() {
        closeProgressDialog(PROGRESS_FORCE_CLOSE);
        mContext = null;
        super.onDestroy();
        
//        if(mCloudSdkService != null && cloudSdkServiceConn != null) {
//            mCloudSdkService = null;
//            try {
//                unbindService(cloudSdkServiceConn);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }
    
    @Override
    public void finish() {
        mAuthenticatorManager.finish();
        super.finish();
    }

    private void finishLogin() {
        mLoginTimes = 0;
        Bundle bundle = new Bundle();
        bundle.putString(AbsLoginHandler.KEY_USER_KEY, mAuthenticatorManager.getUserKey());
        bundle.putString(AbsLoginHandler.KEY_UID, mAuthenticatorManager.getUserName());
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
    }
    
    private void initLoginUI() {
        mHasContentListener = new HasContentListener();
        
        mLoginLayout = findViewById(R.id.login_container);
        
        mAccountEditText = (MyOSEditText) mLoginLayout.findViewById(R.id.account);
        mPasswordEditText = (MyOSEditText) mLoginLayout.findViewById(R.id.password);
        mShowPasswordBox = (CheckBox) mLoginLayout.findViewById(R.id.show_password);
        
        mPasswordEditText.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        mShowPasswordBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPasswordEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    mPasswordEditText.setInputType(
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                Editable editable = mPasswordEditText.getText();
                Selection.setSelection(editable, editable.length());
            }
        });
        
        mTencentButton = (ImageButton) mLoginLayout.findViewById(R.id.tencent_login);
        mSinaButton = (ImageButton) mLoginLayout.findViewById(R.id.sina_login);
        mForgetPassword = (TextView) mLoginLayout.findViewById(R.id.forget_passwd);
        mRegister = (TextView) mLoginLayout.findViewById(R.id.register);
        mWarningView = (TextView) mLoginLayout.findViewById(R.id.warning);
        mLoginButton = (Button) mLoginLayout.findViewById(R.id.login);
        
        mTencentButton.setOnClickListener(mViewOnClickListener);
        mSinaButton.setOnClickListener(mViewOnClickListener);
        mForgetPassword.setOnClickListener(mViewOnClickListener);
        mRegister.setOnClickListener(mViewOnClickListener);
        mLoginButton.setOnClickListener(mViewOnClickListener);
        
        mAccountEditText.setOnTextChangeListener(mHasContentListener);
        mPasswordEditText.setOnTextChangeListener(mHasContentListener);
        
    }
    
    private void initUserInfoUI() {
        mUserLayout = (LinearLayout) findViewById(R.id.userinfo_container);
        
        mUserLayout.setBackgroundColor(getResources().getColor(R.color.window_background));
        mActionBar = (MyOSActionBar) mUserLayout.findViewById(R.id.actionbar);
        mActionMenuItemClickListener = new ActionMenuItemClickListener();
        mActionBar.addMenu(ACTIONBAR_MENU_EDIT_USER, R.string.user_edit, 0)
            .setOnMenuItemClickListener(mActionMenuItemClickListener);
        mActionBar.addMenu(ACTIONBAR_MENU_MODIFY_PASSWORD, R.string.pop_modify_password_title, 0)
            .setOnMenuItemClickListener(mActionMenuItemClickListener);
        
        mActionBar.setHomeOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        mAvatar = (CircleImageView) mUserLayout.findViewById(R.id.avatar);
        mAvatarButton = (ImageButton) mUserLayout.findViewById(R.id.avatar_button);
        mLogoutButton = (Button) mUserLayout.findViewById(R.id.logout);
        mNickName = (TextView) mUserLayout.findViewById(R.id.nick_name);
        mAccountName = (TextView) mUserLayout.findViewById(R.id.account);
        mGender = (TextView) mUserLayout.findViewById(R.id.gender);
        mBirthday = (TextView) mUserLayout.findViewById(R.id.birthday);
        mAddress = (TextView) mUserLayout.findViewById(R.id.address);
        mMobile = (TextView) mUserLayout.findViewById(R.id.moblie);
        mEmail = (TextView) mUserLayout.findViewById(R.id.email);
        
        mAccountLayout = (LinearLayout) mUserLayout.findViewById(R.id.account_layout);
        mGenderLayout = (LinearLayout) mUserLayout.findViewById(R.id.gender_layout);
        mBirthdayLayout = (LinearLayout) mUserLayout.findViewById(R.id.birthday_layout);
        mAddressLayout = (LinearLayout) mUserLayout.findViewById(R.id.address_layout);
        mMobileLayout = (LinearLayout) mUserLayout.findViewById(R.id.mobile_layout);
        mEmaiLayout = (LinearLayout) mUserLayout.findViewById(R.id.email_layout);
        
        mAvatarButton.setOnClickListener(mViewOnClickListener);
        mLogoutButton.setOnClickListener(mViewOnClickListener);
        
        // TODO
//        mNickName.setOnClickListener(mViewOnClickListener);
//        mGenderLayout.setOnClickListener(mViewOnClickListener);
//        mBirthdayLayout.setOnClickListener(mViewOnClickListener);
//        mAddressLayout.setOnClickListener(mViewOnClickListener);
//        mMobileLayout.setOnClickListener(mViewOnClickListener);
//        mEmaiLayout.setOnClickListener(mViewOnClickListener);
    }
    
    private void initUserData(int action) {
        mLoginAction = action;
        
        if (mAuthenticatorManager.isSessionValid()) {
            if (AbsLoginHandler.SDK_TYPE_ANONYMOUS.equals(mAuthenticatorManager.getSdkType())) {
                showUserContainer(false);
                return;
            }
        }
        
        if (mLoginTimes < MAX_LOGIN_TIMES) {
            mLoginTimes ++;
        } else {
            mLoginTimes = 0;
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_CACHE_INVALID;
            msg.obj = "login times greater than MAX_LOGIN_TIMES";
            mHandler.sendMessage(msg);
            return;
        }
        
        showProgressDialog(R.string.user_login_load_user_info, PROGRESS_FORCE_CLOSE);
        mLoginManager.showUser(new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                mLogUtil.i("showUser", "showUser onComplete result code:%d, message:%s",
                        result, message == null ? "" : message);
                switch (result) {
                    case LoginService.REP_LOGIN_SERVICE_SUCCESS: {
                        // 获取用户信息成功
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_LOGIN_SUCCESS;
                        msg.obj = obj;
                        mHandler.sendMessage(msg);
                        break;
                    }
                        
                    case LoginService.REP_LOGIN_SERVICE_CACHE_TIMEOUT:
                        // 最近登录时间超过上限，若cloud账户则静默登录，若第三方账户则弹出提示
                        timeoutHandler((User) obj);
                        break;
                        
                    default: {
                        // 获取用户信息失败，弹出提示（缓存失效）
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_CACHE_INVALID;
                        msg.obj = "cache invalid, silent login unknown error";
                        mHandler.sendMessage(msg);
                        break;
                    }
                }
            }
            
            @Override
            public void onCancel() {
                // 取消登录（缓存失效）
                mLogUtil.i("showUser", "showUser cancel!");
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_LOGIN_CANCEL;
                msg.obj = "show user cancel";
                mHandler.sendMessage(msg);
            }
        });
    }
    
    @SuppressWarnings("unchecked")
    private void timeoutHandler(User user) {
        showProgressDialog(R.string.user_login_timeout, PROGRESS_FORCE_CLOSE);
        if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(user.getSdkType())) {
            mLoginManager.login(LoginActivity.this, user, new AuthenticListener() {
                
                @Override
                public void onComplete(int result, Object obj, String message) {
                    mLogUtil.i("timeoutHandler", "timeout and login again onComplete result code:%d, message:%s",
                            result, message == null ? "" : message);
                    if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                        // 登录成功
                        User user = null;
                        if (obj instanceof User) {
                            user = (User)obj;
                        } else {
                            user = (User) ((HashMap<String, Object>) obj).get(Constants.REQ_SEND_MAIL_PARAM_2);
                        }
                        mAuthenticatorManager.handleUser(user);
                        initUserData(ACTION_LOGIN);
                    } else {
                        // 未知错误（缓存失效）
                        Message msg = mHandler.obtainMessage();
                        msg.what = MSG_CACHE_INVALID;
                        msg.obj = message;
                        mHandler.sendMessage(msg);
                    }
                }
                
                @Override
                public void onCancel() {
                    mLogUtil.i("timeoutHandler", "timeout and login again cancel!");
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_CACHE_INVALID;
                    msg.obj = "cache invalid and silent login cancel";
                    mHandler.sendMessage(msg);
                }
            }, AbsLoginHandler.SDK_TYPE_CLOUD);
        } else if (!AbsLoginHandler.SDK_TYPE_ANONYMOUS.equals(user.getSdkType())) {
            // 第三方缓存失效，提示重新登录
            Message msg = mHandler.obtainMessage();
            msg.what = MSG_COOPERATION_CACHE_INVALID;
            msg.obj = "cache invalid, cooperation must be login";
            mHandler.sendMessage(msg);
        }
    }
    
    private void fillUser() {
        showUserContainer(true);
        HashMap<String, String> userInfoMap = new HashMap<String, String>();
        if (mAuthenticatorManager.isSessionValid()) {
            userInfoMap = mAuthenticatorManager.getUserMapData();
            mAvatarDisplay.showAvatar(userInfoMap);
            userNameStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_UID));
            nickNameStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_NICKNAME));
            phoneStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_MOBILE));
            emailStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_EMAIL));
            genderStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_GENDER));
            birthdayStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_BIRTHDAY));
            addressStr = getNotNullStr(userInfoMap.get(AbsLoginHandler.KEY_ADDRESS));
            
            if (nickNameStr.equals("")) {
                if (userInfoMap.get(AbsLoginHandler.KEY_SDK_TYPE).equals(CloudLoginHandler.SDK_TYPE)) {
                    mNickName.setText(userNameStr);
                }
            } else {
                mNickName.setText(nickNameStr);
            }
            mAccountName.setText(userNameStr);
            if (genderStr != null && genderStr.equals(User.MALE)) {
                mGender.setText(getString(R.string.ex_male));
            }
            if (genderStr != null && genderStr.equals(User.FEMALE)) {
                mGender.setText(getString(R.string.ex_female));
            }
            mBirthday.setText(birthdayStr);
            mAddress.setText(addressStr);
            mMobile.setText(phoneStr);
            mEmail.setText(emailStr);
            
            String sdk = userInfoMap.get(AbsLoginHandler.KEY_SDK_TYPE);
            if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(sdk)) {
                mAccountLayout.setVisibility(View.VISIBLE);
            } else {
                mAccountLayout.setVisibility(View.GONE);
                mActionBar.removeMenu(ACTIONBAR_MENU_MODIFY_PASSWORD);
            }
        }
    }
    
    private void showUserContainer(boolean isShow) {
        if (isShow) {
            mUserLayout.setVisibility(View.VISIBLE);
            mLoginLayout.setVisibility(View.GONE);
        } else {
            mUserLayout.setVisibility(View.GONE);
            mLoginLayout.setVisibility(View.VISIBLE);
        }
    }
    
//    protected static ICloudSdkService mCloudSdkService = null;
//    private ServiceConnection cloudSdkServiceConn = new ServiceConnection() {
//        
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            mCloudSdkService = (ICloudSdkService) ICloudSdkService.Stub.asInterface(service);
//        }
//        
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            if (mCloudSdkService != null) {
//                mCloudSdkService = null;
//            }
//        }
//    };
    
    private void login(String sdk) {
        mWarningView.setText("");
        mIsLoginCancel = false;
        if (sdk.equals(AbsLoginHandler.SDK_TYPE_CLOUD)) {
            // 云账户登录
            if (!checkInputRegisterInfo(mAccountEditText.getText().toString(), R.string.username_empty))
                return;
            if (!checkInputRegisterInfo(mPasswordEditText.getText().toString(), R.string.password_empty))
                return;
            
            showProgressDialog(R.string.user_login_message, PROGRESS_FORCE_CLOSE);
            mCloudLoginHandler = CloudLoginHandler.getInstance(mContext);
            mLoginManager.login(LoginActivity.this, 
                    getEditTextUser(mAccountEditText.getText().toString(), mPasswordEditText.getText().toString()),
                    mLoginAuthenticListener, AbsLoginHandler.SDK_TYPE_CLOUD);
        } else {
            // 第三方账户登录
            if (sdk.equals(AbsLoginHandler.SDK_TYPE_SINA)) {
                mSinaHandler = SinaHandler.getInstance(mContext);
            }
            
            if (sdk.equals(AbsLoginHandler.SDK_TYPE_TENCENT)) {
                mTencentHandler = TencentHandler.getInstance(mContext);
            }
            
            mIsCooLogin = true;
            mLoginManager.login(LoginActivity.this, null, mLoginAuthenticListener, sdk);
        }
    }
    
    private void logout() {
        showProgressDialog(R.string.user_logouting, PROGRESS_FORCE_CLOSE);
        mLoginManager.logout(LoginActivity.this, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    mAuthenticatorManager.removeUser();
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_LOGOUT_SUCCESS;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                } else {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_LOGOUT_FAILURE;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
            }
            
            @Override
            public void onCancel() {
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_LOGOUT_FAILURE;
                mHandler.sendMessage(msg);
            }
        });
    }
    
    private boolean checkInputRegisterInfo(String text, int resId) {
        if(text == null || text.trim().length() == 0) {
            mWarningView.setText(resId);
            return false;
        }
        return true;
    }
    
    private User getEditTextUser(String userName, String password) {
        User longUser = new User();
        longUser.setUserName(userName);
        longUser.setPassword(FileNameGenerator.generator(password + Constants.PASSWORD_MASK));
        longUser.setImei(ContextUtils.getImei(mContext));
        longUser.setSdkType(AbsLoginHandler.SDK_TYPE_CLOUD);
        return longUser;
    }
    
    private void showProgressDialog(int messageRes, int id) {
        if (mContext != null) {
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(mContext);
                mProgressDialog.setCancelable(true);
            }
            mProgressDialog.setMessage(getResources().getString(messageRes));
            mProgressDialog.setProgressId(id);
            mProgressDialog.show();
        }
    }
    
    private void closeProgressDialog(int id) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            if (id == mProgressDialog.getProgressId() ||
                id == PROGRESS_FORCE_CLOSE) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }
    
    private String getNotNullStr(String str) {
        if(str == null || str.trim().length() == 0 || "null".equals(str)) {
            return "";
        }
        return str;
    }
    
    private void cancelLogin() {
        if (mIsCooLogin) {
            mIsCooLogin = false;
        }
        
        if (mSinaHandler != null) {
            mSinaHandler.close();
        }
        
        if (mTencentHandler != null) {
            mTencentHandler.close();
        }
        mIsLoginCancel = true;
    }
    
    public static void closeLoginHandler() {
        if (mSinaHandler != null) {
            mSinaHandler.close();
        }
        
        if (mTencentHandler != null) {
            mTencentHandler.close();
        }
    }
    
    private class ViewOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            
            if (v == mTencentButton) {
                login(AbsLoginHandler.SDK_TYPE_TENCENT);
            } else if (v == mSinaButton) {
                login(AbsLoginHandler.SDK_TYPE_SINA);
            } else if (v == mLoginButton) {
                login(AbsLoginHandler.SDK_TYPE_CLOUD);
            } else if (v == mRegister) {
                intent.setClass(mContext, RegisterActivity.class);
                startActivityForResult(intent, REQ_FOR_REGISTER);
            } else if (v == mForgetPassword) {
                intent.setClass(mContext, PasswordActivity.class);
                intent.putExtra(PasswordActivity.MODE_REQUEST, 
                        PasswordActivity.REQUEST_MODE_FORGET_PASSWORD);
                startActivity(intent);
            } else if (v == mLogoutButton) {
                MyOSAlertDialog dialog = new MyOSAlertDialog(mContext);
                dialog.setTitle(getString(R.string.ex_logout))
                    .setMessage(getString(R.string.ex_logout_content))
                    .setPositiveButton(getString(R.string.ok), new View.OnClickListener() {
                
                        @Override
                        public void onClick(View v) {
                            logout();
                        }})
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
                    
            } else if (v == mNickName) {
                intent.setClass(mContext, SmartEditActivity.class);
                intent.putExtra(SmartEditActivity.EDIT_MODE, SmartEditActivity.MODE_NICKNAME_EDIT);
                intent.putExtra(SmartEditActivity.INITIALIZE_DATA, nickNameStr);
                startActivityForResult(intent, SmartEditActivity.REQ_NICKNAME_MODE);
            } else if (v == mGenderLayout) {
                // TODO
            } else if (v == mBirthdayLayout) {
                // TODO
            } else if (v == mAddressLayout) {
                intent.setClass(mContext, SmartEditActivity.class);
                intent.putExtra(SmartEditActivity.EDIT_MODE, SmartEditActivity.MODE_ADDRESS_EDIT);
                intent.putExtra(SmartEditActivity.INITIALIZE_DATA, addressStr);
                startActivityForResult(intent, SmartEditActivity.REQ_ADDRESS_MODE);
            } else if (v == mMobileLayout) {
                intent.setClass(mContext, SmartEditActivity.class);
                intent.putExtra(SmartEditActivity.EDIT_MODE, SmartEditActivity.MODE_MOBILE_EDIT);
                intent.putExtra(SmartEditActivity.INITIALIZE_DATA, phoneStr);
                startActivityForResult(intent, SmartEditActivity.REQ_MOBILE_MODE);
            } else if (v == mEmaiLayout) {
                intent.setClass(mContext, SmartEditActivity.class);
                intent.putExtra(SmartEditActivity.EDIT_MODE, SmartEditActivity.MODE_EMAIL_EDIT);
                intent.putExtra(SmartEditActivity.INITIALIZE_DATA, emailStr);
                startActivityForResult(intent, SmartEditActivity.REQ_EMAIL_MODE);
            }
        }
    }
    
    private class ActionMenuItemClickListener implements MenuItem.OnMenuItemClickListener {

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Log.i(TAG, "nthpower[onMenuItemClick]item:" + item.getItemId(), Log.APP);
            
            Bundle data = new Bundle();
            data.putString(AbsLoginHandler.KEY_UID, mAuthenticatorManager.getUserName());
            
            switch (item.getItemId()) {
                case ACTIONBAR_MENU_EDIT_USER:
                    Intent editIntent = new Intent();
                    editIntent.setClass(mContext, UserEditActivity.class);
                    editIntent.putExtras(data);
                    startActivity(editIntent);
                    break;
                case ACTIONBAR_MENU_MODIFY_PASSWORD:
                    Intent passwordIntent = new Intent();
                    passwordIntent.putExtra(PasswordActivity.MODE_REQUEST, 
                            PasswordActivity.REQUEST_MODE_MODIFY_PASSWORD);
                    passwordIntent.setClass(mContext, PasswordActivity.class);
                    passwordIntent.putExtras(data);
                    startActivity(passwordIntent);
                    break;
            }
            return false;
        }
        
    }
    
    private class HasContentListener implements MyOSEditText.OnTextChangeListener {
        
        private int result = 0;
        
        @Override
        public void afterTextChanged(View view, String text) {
            
            if (text.length() > 0) {
                if (view == mAccountEditText) {
                    result |= MASK_HAS_CONTENT_ACCOUNT;
                } else if (view == mPasswordEditText) {
                    result |= MASK_HAS_CONTENT_PASSWORD;
                }
            } else {
                if (view == mAccountEditText) {
                    result &= ~MASK_HAS_CONTENT_ACCOUNT;
                } else if (view == mPasswordEditText) {
                    result &= ~MASK_HAS_CONTENT_PASSWORD;
                }
            }
            if (result == MASK_HAS_CONTENT_ALL) {
                mLoginButton.setEnabled(true);
            } else {
                mLoginButton.setEnabled(false);
            }
        }
    }
}

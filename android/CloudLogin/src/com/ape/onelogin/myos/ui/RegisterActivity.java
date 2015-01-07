package com.ape.onelogin.myos.ui;

import java.util.Map;

import net.tsz.afinal.core.FileNameGenerator;

import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginManager;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.myos.widget.MenuItem;
import com.ape.onelogin.myos.widget.MyOSEditText;
import com.ape.onelogin.myos.widget.ProgressDialog;
import com.ape.onelogin.myos.widget.UserInfoUtil;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.service.ICloudSdkService;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.LogUtil;
import com.ape.onelogin.widget.CircleImageView;
import com.ape.onelogin.R;

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
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class RegisterActivity extends Activity {
    
    private static final String TAG = "RegisterActivity";
    
    private Context mContext;
    private TextView mAccountView;
    private TextView mWarningView;
    private ImageButton mHomeButton;
    private ImageButton mAvatarButton;
    private Button mSigninButton;
    private CircleImageView mAvatarView;
    private CheckBox mShowPasswordBox;
    private MyOSEditText mAccountEditText;
    private MyOSEditText mEmailEditText;
    private MyOSEditText mMobileEditText;
    private MyOSEditText mPasswordEditText;
    private Bitmap mAvatarBitmap;
    
    private PopupMenuItemClickListener mPopupMenuItemClickListener;
    private ViewOnClickListener mViewOnClickListener;
    private InputLegallyListener mInputLegallyListener;
    private HasContentListener mHasContentListener;
    private ProgressDialog mProgressDialog;
    
    private AvatarManager mAvatarManager;
    private UserInfoUtil mUserInfoUtil;
    private LogUtil mLogUtil;
    private LoginManager mLoginManager;
    
    private static final int MASK_INPUT_ALL                 = 0x1111;
    private static final int MASK_INPUT_ACCOUNT             = 0x1000;
    private static final int MASK_INPUT_EMAIL               = 0x0100;
    private static final int MASK_INPUT_MOBILE              = 0x0010;
    private static final int MASK_INPUT_PASSWORD            = 0x0001;
    
    private static final int MASK_HAS_CONTENT_ALL           = 0x1111;
    private static final int MASK_HAS_CONTENT_ACCOUNT       = 0x1000;
    private static final int MASK_HAS_CONTENT_EMAIL         = 0x0100;
    private static final int MASK_HAS_CONTENT_MOBILE        = 0x0010;
    private static final int MASK_HAS_CONTENT_PASSWORD      = 0x0001;
    
    private static final int MSG_REGISTER_SUCCESS           = 1001;
    private static final int MSG_REGISTER_FAIL              = 1002;
    private static final int MSG_HEADIMG_UPLOAD_SUCCESS     = AvatarManager.MSG_AVATAR_DOWNLOAD_SUCCESS;
    private static final int MSG_HEADIMG_UPLOAD_FAIL        = AvatarManager.MSG_AVATAR_DOWNLOAD_FAIL;
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_SUCCESS:
                    mLogUtil.i("handleMessage", "MSG_REGISTER_SUCCESS");
                    OneLoginApplication.loginManager.clearGuestCache();
                    ContextUtils.showToast(RegisterActivity.this, 
                            R.string.user_signin_success, Toast.LENGTH_SHORT);
                    try {
                        if (OneLoginApplication.sCloudSdkService != null) {
                            OneLoginApplication.sCloudSdkService.getUserKey();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    if (mAvatarBitmap != null) {
                        mLogUtil.i("handleMessage", "register success begin upload avatar");
                        mAvatarManager.uploadAvatar(mHandler);
                    } else {
                        finishOnSuccess();
                    }
                    break;
                case MSG_REGISTER_FAIL:
                    mLogUtil.i("handleMessage", "MSG_REGISTER_FAIL");
                    mWarningView.setText((String)msg.obj);
                    closeProgressDialog();
                    break;
                    
                case MSG_HEADIMG_UPLOAD_SUCCESS:
                case MSG_HEADIMG_UPLOAD_FAIL:
                    closeProgressDialog();
                    mLogUtil.i("handleMessage", "upload avatar finish %d", msg.what);
                    finishOnSuccess();
                    break;
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLogUtil = new LogUtil(TAG);
//        if (mCloudSdkService == null) {
//            Intent service = new Intent(this,CloudSdkService.class);
//            this.bindService(service, cloudSdkServiceConn, Service.BIND_AUTO_CREATE);
//        }
        
        setContentView(R.layout.myos_register_activity);
        mContext = this;
        mAvatarManager = new AvatarManager(RegisterActivity.this);
        mUserInfoUtil = new UserInfoUtil(mContext);
        mLoginManager = OneLoginApplication.loginManager;
        
        mAccountView = (TextView) findViewById(R.id.name);
        mWarningView = (TextView) findViewById(R.id.warning);
        mHomeButton = (ImageButton) findViewById(R.id.home);
        mAvatarButton = (ImageButton) findViewById(R.id.avatar_button);
        mSigninButton = (Button) findViewById(R.id.signin);
        mAvatarView = (CircleImageView) findViewById(R.id.avatar);
        mShowPasswordBox = (CheckBox) findViewById(R.id.show_password);
        mAccountEditText = (MyOSEditText) findViewById(R.id.account);
        mEmailEditText = (MyOSEditText) findViewById(R.id.email);
        mMobileEditText = (MyOSEditText) findViewById(R.id.mobile);
        mPasswordEditText = (MyOSEditText) findViewById(R.id.password);
        
        mEmailEditText.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mMobileEditText.setInputType(InputType.TYPE_CLASS_PHONE);
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
                Editable password = mPasswordEditText.getText();
                Selection.setSelection(password, password.length());
            }
        });
        mAccountView.setText(getResources().getString(R.string.ex_register_title));
        mHomeButton.setVisibility(View.VISIBLE);
        
        mPopupMenuItemClickListener = new PopupMenuItemClickListener();
        mViewOnClickListener = new ViewOnClickListener();
        mInputLegallyListener = new InputLegallyListener();
        mHasContentListener = new HasContentListener();

        mHomeButton.setOnClickListener(mViewOnClickListener);
        mAvatarButton.setOnClickListener(mViewOnClickListener);
        mSigninButton.setOnClickListener(mViewOnClickListener);
        mAccountEditText.setOnInputLegallyListener(mInputLegallyListener);
        mEmailEditText.setOnInputLegallyListener(mInputLegallyListener);
        mMobileEditText.setOnInputLegallyListener(mInputLegallyListener);
        mPasswordEditText.setOnInputLegallyListener(mInputLegallyListener);
        
        mAccountEditText.setOnTextChangeListener(mHasContentListener);
        mEmailEditText.setOnTextChangeListener(mHasContentListener);
        mMobileEditText.setOnTextChangeListener(mHasContentListener);
        mPasswordEditText.setOnTextChangeListener(mHasContentListener);
        
//        if (mCloudSdkService == null) {
//            Intent service = new Intent(this,CloudSdkService.class);
//            this.bindService(service, cloudSdkServiceConn, Service.BIND_AUTO_CREATE);
//        }
        
//        debug();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case AvatarManager.REQ_PICTURE_FROM_CAMERA:
                    mAvatarBitmap = mAvatarManager.getPictureFromCamera(data);
                    break;
                case AvatarManager.REQ_PICTURE_FROM_ALBUM:
                    mAvatarBitmap = mAvatarManager.getPictureFromAlbum(data);
                    break;
            }
            if (mAvatarBitmap != null) {
                mAvatarView.setImageBitmap(mAvatarBitmap);
            }
        }
    }
    
    @Override
    protected void onDestroy() {
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
    
    private void showProgressDialog(int messageRes) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCancelable(true);
        }
        mProgressDialog.setMessage(getResources().getString(messageRes));
        mProgressDialog.show();
    }
    
    private void closeProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    
    private void signinAccount() {
        int result = 0;
        result |= mAccountEditText.inputLegallyCheck() ? MASK_INPUT_ACCOUNT : 0;
        result |= mEmailEditText.inputLegallyCheck() ? MASK_INPUT_EMAIL : 0;
        result |= mMobileEditText.inputLegallyCheck() ? MASK_INPUT_MOBILE : 0;
        result |= mPasswordEditText.inputLegallyCheck() ? MASK_INPUT_PASSWORD : 0;
        
        if (result == MASK_INPUT_ALL) {
            showProgressDialog(R.string.user_signin_message);
            
            mLogUtil.i("signinAccount", "begin to register");
            
            // 云账户注册
            mLoginManager.register(
                getEditTextUser(mAccountEditText, mPasswordEditText, mMobileEditText, mEmailEditText),
                new AuthenticListener() {
                    
                    @SuppressWarnings("unchecked")
                    @Override
                    public void onComplete(int result, Object obj, String message) {
                        mLogUtil.i("register", "register onComplete result code:%d, message:%s",
                                result, message == null ? "" : message);
                        if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                            User user = null;
                            if (obj instanceof User) {
                                user = (User) obj;
                            } else {
                                Map<String, Object> infoMap = (Map<String, Object>) obj;
                                user = (User)infoMap.get(Constants.REQ_SEND_MAIL_PARAM_2);
                            }
                            if (user == null) {
                                Message msg = mHandler.obtainMessage();
                                msg.what = MSG_REGISTER_FAIL;
                                msg.obj = message;
                                mHandler.sendMessage(msg);
                                mLogUtil.i("register.onException", "register failure, message:user entity is empty");
                            } else {
                                getUserInfo(user);
                            }
                        } else {
                            Message msg = mHandler.obtainMessage();
                            msg.what = MSG_REGISTER_FAIL;
                            msg.obj = message;
                            mHandler.sendMessage(msg);
                            mLogUtil.i("register.onException", "register failure, message:%s", msg);
                        }
                    }
                    
                    @Override
                    public void onCancel() {
                        mLogUtil.i("register.onCancel", "register cancel");
                    }
                }, AbsLoginHandler.SDK_TYPE_CLOUD);
        }
    }
    
    private void getUserInfo(User user) {
        showProgressDialog(R.string.user_login_load_user_info);
        mLoginManager.login(this, user, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                mLogUtil.i("getUserInfo", "getUserInfo onComplete result code:%d, message:%s",
                        result, message == null ? "" : message);
                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_REGISTER_SUCCESS;
                    msg.obj = obj;
                    mHandler.sendMessage(msg);
                }
            }
            
            @Override
            public void onCancel() {
                mLogUtil.i("register.onCancel", "register cancel");
            }
        }, AbsLoginHandler.SDK_TYPE_CLOUD);
    }
    
    private void finishOnSuccess() {
        Bundle bundle = new Bundle();
        bundle.putString("login_result", "success");
        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    
    private User getEditTextUser(MyOSEditText userNameEt, MyOSEditText passwordEt,
            MyOSEditText phoneEt, MyOSEditText emailEt) {
        String userName = userNameEt.getText().toString();
        String password = passwordEt.getText().toString();
        String phone = phoneEt.getText().toString();
        String email = emailEt.getText().toString();
        User user = new User();
        user.setUserName(userName);
        user.setPassword(FileNameGenerator.generator(password + Constants.PASSWORD_MASK));
        user.setImei(ContextUtils.getImei(mContext));
        user.setSdkType(AbsLoginHandler.SDK_TYPE_CLOUD);;
        user.setMobile(phone);
        user.setEmail(email);
        return user;
    }
    
    private void debug() {
        mAccountEditText.setText("nthpower");
        mEmailEditText.setText("juan.li@tinno.com");
        mMobileEditText.setText("13815870980");
        mPasswordEditText.setText("tinno@2010");
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
    
    private class ViewOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == mHomeButton) {
                finish();
            } else if (v == mAvatarButton) {
                mAvatarManager.showAvatarMenu(mPopupMenuItemClickListener);
            } else if (v == mSigninButton) {
                mWarningView.setText("");
                signinAccount();
            }
        }
        
    }
    
    private class PopupMenuItemClickListener implements MenuItem.OnMenuItemClickListener {
        
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case AvatarManager.MENU_PICTURE_FROM_CAMERA:
                    Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(camera, AvatarManager.REQ_PICTURE_FROM_CAMERA);
                    break;
                case AvatarManager.MENU_PICTURE_FROM_ALBUM:
                    Intent picture = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(picture, AvatarManager.REQ_PICTURE_FROM_ALBUM);
                    break;
            }
            return false;
        }
    }
    
    private class InputLegallyListener implements MyOSEditText.OnInputLegallyListener {
        
        @Override
        public String onInputLegallyCheck(View view, CharSequence text) {
            String editText = ((MyOSEditText)view).getText().toString();
            
            Log.i(TAG, "nthpower[inputLegallyCheck]View:" + editText);
            if (view == mAccountEditText) {
                return mUserInfoUtil.checkAccount(editText);
            } else if (view == mEmailEditText) {
                return mUserInfoUtil.checkEmail(editText);
            } else if (view == mMobileEditText) {
                return mUserInfoUtil.checkMobile(editText);
            } else if (view == mPasswordEditText) {
                return mUserInfoUtil.checkPassword(editText);
            }
            return null;
        }
        
    }
    
    private class HasContentListener implements MyOSEditText.OnTextChangeListener {
        
        private int result = 0;
        
        @Override
        public void afterTextChanged(View view, String text) {
            
            if (text.length() > 0) {
                if (view == mAccountEditText) {
                    result |= MASK_HAS_CONTENT_ACCOUNT;
                } else if (view == mEmailEditText) {
                    result |= MASK_HAS_CONTENT_EMAIL;
                } else if (view == mMobileEditText) {
                    result |= MASK_HAS_CONTENT_MOBILE;
                } else if (view == mPasswordEditText) {
                    result |= MASK_HAS_CONTENT_PASSWORD;
                }
            } else {
                if (view == mAccountEditText) {
                    result &= ~MASK_HAS_CONTENT_ACCOUNT;
                } else if (view == mEmailEditText) {
                    result &= ~MASK_HAS_CONTENT_EMAIL;
                } else if (view == mMobileEditText) {
                    result &= ~MASK_HAS_CONTENT_MOBILE;
                } else if (view == mPasswordEditText) {
                    result &= ~MASK_HAS_CONTENT_PASSWORD;
                }
            }
            if (result == MASK_HAS_CONTENT_ALL) {
                mSigninButton.setEnabled(true);
            } else {
                mSigninButton.setEnabled(false);
            }
        }
        
    }
}

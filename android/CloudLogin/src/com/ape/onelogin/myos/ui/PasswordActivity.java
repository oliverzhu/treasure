package com.ape.onelogin.myos.ui;

import java.util.Map;

import net.tsz.afinal.core.FileNameGenerator;

import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.cloudlogin.CloudLoginHandler;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.Constants;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.myos.widget.MyOSActionBar;
import com.ape.onelogin.myos.widget.MyOSEditText;
import com.ape.onelogin.myos.widget.ProgressDialog;
import com.ape.onelogin.myos.widget.UserInfoUtil;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.Log;
import com.ape.onelogin.util.RegexValidate;
import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

public class PasswordActivity extends Activity{
    
    private static final String TAG = "PasswordActivity";
    
    private Context mContext;
    
    /**
     * ********************************************
     * <modify password UI>
     * ********************************************
     */
    private View mModifyLayout;
    private MyOSEditText mOldPassword;
    private MyOSEditText mNewPassword;
    private MyOSEditText mConfirmPassword;
    private Button mOKButton;
    private TextView mModifyWarningView;
    private CheckBox mShowPasswordBox;
    private ProgressDialog mProgressDialog;
    
    private InputLegallyListener mInputLegallyListener;
    
    private static final int MASK_INPUT_ALL                 = 0x0111;
    private static final int MASK_INPUT_OLD                 = 0x0100;
    private static final int MASK_INPUT_NEW                 = 0x0010;
    private static final int MASK_INPUT_CONFIRM             = 0x0001;
    
    private static final int MASK_HAS_CONTENT_ALL           = 0x0111;
    private static final int MASK_HAS_CONTENT_OLD           = 0x0001;
    private static final int MASK_HAS_CONTENT_NEW           = 0x0010;
    private static final int MASK_HAS_CONTENT_CONFIRM       = 0x0100;
    /**
     * ********************************************
     * <forget password UI>
     * ********************************************
     */
    private View mForgetLayout;
    private View mFirstLayout;
    private View mSecondLayout;
    private MyOSEditText mEmailView;
    private TextView mForgetWarningView;
    private TextView mSecondHint;
    private Button mNextButton;
    private Button mFinishButton;
    
    private Map<String, Object> mImportantMap = null;
    
    private static final int MASK_HAS_CONTENT_EMAIL           = 0x0001;
    /**
     * ********************************************
     * <common UI>
     * ********************************************
     */
    private MyOSActionBar mActionBar;
    private static int mModeRequest;
    private UserInfoUtil mUserInfoUtil;
    private AuthenticatorManager mAuthenticatorManager;
    
    private HasContentListener mHasContentListener;
    
    public static final String MODE_REQUEST = "mode_request";
    public static final int REQUEST_MODE_MODIFY_PASSWORD    = 1000;
    public static final int REQUEST_MODE_FORGET_PASSWORD    = 1001;
    
    private static final int MSG_USER_INFO_LOAD_COMPLETE    = 2001;
    private static final int MSG_MODIFY_USER_SUCCESS        = 2002;
    private static final int MSG_MODIFY_USER_FAIL           = 2003;
    private static final int MSG_FINDPASSWORD_SUCCESS       = 2004;
    private static final int MSG_FINDPASSWORD_FAIL          = 2005;
    private static final int MSG_SEND_EMAIL_SUCCESS         = 2006;
    private static final int MSG_SEND_EMAIL_FAIL            = 2007;
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @SuppressWarnings("unchecked")
        @Override
        public void handleMessage(Message msg) {
            int response = msg.arg1;
            
            switch (msg.what) {
                case MSG_USER_INFO_LOAD_COMPLETE:
                    break;
                case MSG_MODIFY_USER_SUCCESS:
                    showSuccessTip();
                    break;
                case MSG_MODIFY_USER_FAIL:
                    closeProgressDialog();
                    if (response == LoginService.REP_LOGIN_SERVICE_NETWORK_ERROR) {
                        mModifyWarningView.setText(R.string.ex_network_error);
                    } else if (msg.obj instanceof String) {
                        mModifyWarningView.setText((String) msg.obj);
                    }
//                    ContextUtils.showToast(mContext, R.string.user_modify_fail, Toast.LENGTH_SHORT);
                    break;
                    
                case MSG_FINDPASSWORD_SUCCESS:
                    if (msg.obj instanceof String) {
                        closeProgressDialog();
//                        if (msg.obj.equals(LoginService.REP_MESSAGE_USER_NOT_EXIST)) {
//                            mForgetWarningView.setText(R.string.ex_forget_password_not_exist);
//                        } else {
//                            mForgetWarningView.setText((String)msg.obj);
//                        }
                    } else if (msg.obj instanceof Map){
                        mImportantMap = (Map<String, Object>) msg.obj;
                        sendEmail();
                    }
                    break;
                    
                case MSG_FINDPASSWORD_FAIL:
                    closeProgressDialog();
                    if (response == LoginService.REP_LOGIN_SERVICE_NETWORK_ERROR) {
                        mForgetWarningView.setText(R.string.ex_network_error);
                    } else if (msg.obj instanceof String) {
                        mForgetWarningView.setText((String) msg.obj);
                    }
                    break;
                    
                case MSG_SEND_EMAIL_SUCCESS:
                    closeProgressDialog();
                    showSecondStep((String)msg.obj);
                    break;
                    
                case MSG_SEND_EMAIL_FAIL:
                    closeProgressDialog();
                    if (response == LoginService.REP_LOGIN_SERVICE_NETWORK_ERROR) {
                        mForgetWarningView.setText(R.string.ex_network_error);
                    } else if (msg.obj instanceof String) {
                        mForgetWarningView.setText((String) msg.obj);
                    }
                    break;
            }
        }
        
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.myos_password_activity);
        
        mUserInfoUtil = new UserInfoUtil(mContext);
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
        
        mActionBar = (MyOSActionBar) findViewById(R.id.actionbar);
        mActionBar.setOptionVisibility(View.GONE);
        mActionBar.setHomeOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        mInputLegallyListener = new InputLegallyListener();
        mHasContentListener = new HasContentListener();
        
        mModeRequest = getIntent().getIntExtra(MODE_REQUEST, 0);
        switch (mModeRequest) {
            case REQUEST_MODE_MODIFY_PASSWORD:
                initModifyUI();
                break;
            case REQUEST_MODE_FORGET_PASSWORD:
                initForgetUI();
                break;
            default:
                throw new IllegalArgumentException("mode_request must be set!!");
        }
    }
    
    private void initForgetUI() {
        mForgetLayout = findViewById(R.id.forget_container);
        mForgetLayout.setVisibility(View.VISIBLE);
        mActionBar.setActionTitle(getString(R.string.ex_get_back_password));
        
        mFirstLayout = mForgetLayout.findViewById(R.id.first_step);
        mSecondLayout = mForgetLayout.findViewById(R.id.second_step);
        mEmailView = (MyOSEditText) mForgetLayout.findViewById(R.id.email);
        mForgetWarningView = (TextView) mForgetLayout.findViewById(R.id.warning);
        mSecondHint = (TextView) mForgetLayout.findViewById(R.id.second_hint);
        mNextButton = (Button) mForgetLayout.findViewById(R.id.next_step);
        mFinishButton = (Button) mForgetLayout.findViewById(R.id.finish);
        
        mEmailView.setOnInputLegallyListener(mInputLegallyListener);
        mNextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mForgetWarningView.setText("");
                String email = mEmailView.getText().toString();
                if (RegexValidate.isEMail(email)) {
                    findPassword(email.trim());
                } else {
                    mForgetWarningView.setText(getString(R.string.ex_register_email_notice));
                }
            }
        });
        
        mFinishButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mFirstLayout.setVisibility(View.VISIBLE);
        mEmailView.setOnTextChangeListener(mHasContentListener);
    }
    
    private void initModifyUI() {
//        initializeData();
        mModifyLayout = findViewById(R.id.modify_container);
        mModifyLayout.setVisibility(View.VISIBLE);
        mActionBar.setActionTitle(getString(R.string.pop_modify_password_title));
        
        mOldPassword = (MyOSEditText) mModifyLayout.findViewById(R.id.old_password);
        mNewPassword = (MyOSEditText) mModifyLayout.findViewById(R.id.new_password);
        mConfirmPassword = (MyOSEditText) mModifyLayout.findViewById(R.id.confirm_password);
        mOKButton = (Button) mModifyLayout.findViewById(R.id.ok);
        mModifyWarningView = (TextView) mModifyLayout.findViewById(R.id.warning);
        mShowPasswordBox = (CheckBox) mModifyLayout.findViewById(R.id.show_password);
        
        mOldPassword.setOnInputLegallyListener(mInputLegallyListener);
        mNewPassword.setOnInputLegallyListener(mInputLegallyListener);
        mConfirmPassword.setOnInputLegallyListener(mInputLegallyListener);
        
        mOldPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfirmPassword.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        
        mShowPasswordBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mOldPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mNewPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    mOldPassword.setInputType(
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mNewPassword.setInputType(
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    mConfirmPassword.setInputType(
                            InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                Editable oldPassword = mOldPassword.getText();
                Selection.setSelection(oldPassword, oldPassword.length());
                
                Editable newPassword = mNewPassword.getText();
                Selection.setSelection(newPassword, newPassword.length());
                
                Editable confirm = mConfirmPassword.getText();
                Selection.setSelection(confirm, confirm.length());
            }
        });
        
        mOKButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mModifyWarningView.setText("");
                modifyPassword();
            }
        });
        
        mOldPassword.setOnTextChangeListener(mHasContentListener);
        mNewPassword.setOnTextChangeListener(mHasContentListener);
        mConfirmPassword.setOnTextChangeListener(mHasContentListener);
    }
    
//    private void initializeData() {
//        Bundle data = getIntent().getExtras();
//        final String userId = data.getString(AbsLoginHandler.KEY_UID);
//        
//        if(userId != null && userId.trim().length() != 0) {
//            new Thread() {
//                public void run() {
//                    LoginInfoHandler loginHandler = LoginInfoHandler.getInstance();
//                    LoginInfo info  = new LoginInfo();
//                    info.setUid(userId);
//                    mLoginInfo = loginHandler.queryByUid(info);
//                    
//                    Message msg = mHandler.obtainMessage();
//                    msg.what = MSG_USER_INFO_LOAD_COMPLETE;
//                    mHandler.sendMessage(msg);
//                };
//            }.start();
//        }
//        
//        
//    }
    
    private void showSecondStep(String email) {
        mFirstLayout.setVisibility(View.GONE);
        mSecondLayout.setVisibility(View.VISIBLE);
        
        String result = String.format(getString(R.string.ex_forget_password_send_email), email);
        mSecondHint.setText(result);
    }
    
    private void modifyPassword() {
        int result = 0;
        result |= mOldPassword.inputLegallyCheck() ? MASK_INPUT_OLD : 0;
        result |= mNewPassword.inputLegallyCheck() ? MASK_INPUT_NEW : 0;
        result |= mConfirmPassword.inputLegallyCheck() ? MASK_INPUT_CONFIRM : 0;
        
        if (result == MASK_INPUT_ALL) {
            if (mAuthenticatorManager.isSessionValid()) {
                final String oldPassword = mOldPassword.getText().toString();
                final String newPassword = mNewPassword.getText().toString();
                
                String localMaskPassword = mAuthenticatorManager.getUserData(AbsLoginHandler.KEY_ACCESS_TOKEN);
                String oldMaskPassword = FileNameGenerator.generator(oldPassword + Constants.PASSWORD_MASK);
                if (!localMaskPassword.equals(oldMaskPassword)) {
                    mOldPassword.setNoticeMessage(getString(R.string.old_password_not_true));
                    return;
                }
                
                final String newMaskPassword = FileNameGenerator.generator(newPassword + Constants.PASSWORD_MASK);
                showProgressDialog(R.string.user_saving);
                new Thread() {
                    public void run() {
                        User user = mAuthenticatorManager.getUserData();
                        user.setPassword(newMaskPassword);
                        Looper.prepare();
                        CloudLoginHandler handler = CloudLoginHandler.getInstance(mContext);
                        handler.updateUser(user, new AuthenticListener() {
                            
                            @Override
                            public void onComplete(int result, Object obj, String message) {
                                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = MSG_MODIFY_USER_SUCCESS;
                                    msg.obj = obj;
                                    mHandler.sendMessage(msg);
                                } else {
                                    Message msg = mHandler.obtainMessage();
                                    msg.what = MSG_MODIFY_USER_FAIL;
                                    msg.obj = message;
                                    mHandler.sendMessage(msg);
                                }
                            }
                            
                            @Override
                            public void onCancel() {
                                Message msg = mHandler.obtainMessage();
                                msg.what = MSG_MODIFY_USER_FAIL;
                                mHandler.sendMessage(msg);
                            }
                        });
                        Looper.loop();
                    }
                }.start();
            }
        }
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
    
    private void showSuccessTip() {
        closeProgressDialog();
        ContextUtils.showToast(mContext, R.string.user_modify_success, Toast.LENGTH_SHORT);
        finish();
    }
    
    private void findPassword(String address) {
        
        showProgressDialog(R.string.ex_send_email);
        CloudLoginHandler handler = CloudLoginHandler.getInstance(mContext);
        handler.findPassword(address, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                if (result == LoginService.REP_LOGIN_SERVICE_SUCCESS) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_FINDPASSWORD_SUCCESS;
                    msg.obj = obj;
                    mHandler.sendMessage(msg);
                } else {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_FINDPASSWORD_FAIL;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
            }
            
            @Override
            public void onCancel() {
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_FINDPASSWORD_FAIL;
                mHandler.sendMessage(msg);
            }
        });
    }
    
    private void sendEmail() {
        String important = (String)mImportantMap.get(Constants.REQ_SEND_MAIL_PARAM_1);
        final User user = (User)mImportantMap.get(Constants.REQ_SEND_MAIL_PARAM_2);
        CloudLoginHandler handler = CloudLoginHandler.getInstance(mContext);
        String title = getString(R.string.ex_forget_password_email_title);
        String body = String.format(getString(R.string.ex_forget_password_email_body), user.getUserName(), important);
        handler.sendEmail(user.getEmail(), title, body, new AuthenticListener() {
            
            @Override
            public void onComplete(int result, Object obj, String message) {
                if (result == LoginService.REP_LOGIN_SERVICE_SEND_EMAIL_SUCCESS) {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_SEND_EMAIL_SUCCESS;
                    msg.obj = user.getEmail();
                    mHandler.sendMessage(msg);
                } else {
                    Message msg = mHandler.obtainMessage();
                    msg.what = MSG_SEND_EMAIL_FAIL;
                    msg.obj = message;
                    mHandler.sendMessage(msg);
                }
            }
            
            @Override
            public void onCancel() {
                Message msg = mHandler.obtainMessage();
                msg.what = MSG_SEND_EMAIL_FAIL;
                mHandler.sendMessage(msg);
            }
        });
    }
    
    private class InputLegallyListener implements MyOSEditText.OnInputLegallyListener {

        @Override
        public String onInputLegallyCheck(View view, CharSequence text) {
            String editText = ((MyOSEditText)view).getText().toString();
            
            Log.i(TAG, "nthpower[inputLegallyCheck]View:" + editText, Log.APP);
            if (view == mOldPassword) {
//                return mUserInfoUtil.checkPassword(editText);
                return null; // The original password without the need to meet the password rules.
            } else if (view == mNewPassword) {
                if (mOldPassword.getText().toString().equals(editText)) {
                    return getString(R.string.password_same);
                }
                return mUserInfoUtil.checkPassword(editText);
            } else if (view == mConfirmPassword) {
                if (!mNewPassword.getText().toString().equals(editText)) {
                    return getString(R.string.password_not_same);
                }
            }
            return null;
        }
    }
    
    private class HasContentListener implements MyOSEditText.OnTextChangeListener {
        
        private int result = 0;
        
        @Override
        public void afterTextChanged(View view, String text) {
            if (mModeRequest == REQUEST_MODE_MODIFY_PASSWORD) {
                if (text.length() > 0) {
                    if (view == mOldPassword) {
                        result |= MASK_HAS_CONTENT_OLD;
                    } else if (view == mNewPassword) {
                        result |= MASK_HAS_CONTENT_NEW;
                    } else if (view == mConfirmPassword) {
                        result |= MASK_HAS_CONTENT_CONFIRM;
                    }
                } else {
                    if (view == mOldPassword) {
                        result &= ~MASK_HAS_CONTENT_OLD;
                    } else if (view == mNewPassword) {
                        result &= ~MASK_HAS_CONTENT_NEW;
                    } else if (view == mConfirmPassword) {
                        result &= ~MASK_HAS_CONTENT_CONFIRM;
                    }
                }
                
                if (result == MASK_HAS_CONTENT_ALL) {
                    mOKButton.setEnabled(true);
                } else {
                    mOKButton.setEnabled(false);
                }
            } else if (mModeRequest == REQUEST_MODE_FORGET_PASSWORD) {
                if (text.length() > 0) {
                    if (view == mEmailView) {
                        result |= MASK_HAS_CONTENT_EMAIL;
                    }
                } else {
                    if (view == mEmailView) {
                        result &= ~MASK_HAS_CONTENT_EMAIL;
                    }
                }
                
                if (result == MASK_HAS_CONTENT_EMAIL) {
                    mNextButton.setEnabled(true);
                } else {
                    mNextButton.setEnabled(false);
                }
            }
        }
        
    }
}

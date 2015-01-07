package com.ape.onelogin.myos.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import com.ape.onelogin.authenticator.AuthenticatorManager;
import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.ape.onelogin.login.core.LoginService;
import com.ape.onelogin.login.core.LoginManager.AuthenticListener;
import com.ape.onelogin.myos.widget.DatePicker;
import com.ape.onelogin.myos.widget.MenuItem;
import com.ape.onelogin.myos.widget.MyOSActionBar;
import com.ape.onelogin.myos.widget.MyOSDatePickerDialog;
import com.ape.onelogin.myos.widget.MyOSEditText;
import com.ape.onelogin.myos.widget.ProgressDialog;
import com.ape.onelogin.myos.widget.UserInfoUtil;
import com.ape.onelogin.service.CloudSdkService;
import com.ape.onelogin.service.ICloudSdkService;
import com.ape.onelogin.util.ContextUtils;
import com.ape.onelogin.util.LogUtil;
import com.ape.onelogin.widget.CircleImageView;
import com.ape.onelogin.OneLoginApplication;
import com.ape.onelogin.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class UserEditActivity extends Activity {

    private Context mContext;
    
    private MyOSActionBar mActionBar;
    private CircleImageView mAvatarView;
    private ImageButton mAvatarButton;
    private MyOSEditText mNickNameView;
    private MyOSEditText mBirthdayView;
    private MyOSEditText mMobileView;
    private MyOSEditText mAddressView;
    private MyOSEditText mEmailView;
    private RadioButton mMaleRadio;
    private RadioButton mFemaleRadio;
    private TextView mWarningView;
    private Button mSaveButton;
    private Button mBirthdayButton;
    private ProgressDialog mProgressDialog;
    
    private Bitmap mAvatarBitmap;
    private AvatarManager mAvatarManager;
    private UserInfoUtil mUserInfoUtil;
    private PopupMenuItemClickListener mPopupMenuItemClickListener;
    private DatePickerSetListener mDatePickerSetListener;
    private TextChangeListener mTextChangeListener;
    private RadioCheckedChangeListener mRadioCheckedChangeListener;
    private AuthenticatorManager mAuthenticatorManager;
    private LogUtil mLogUtil;
    
    private static final int MSG_LOAD_USER_INFO             = 2001;
    private static final int MSG_MODIFY_USER_SUCCESS        = 2002;
    private static final int MSG_MODIFY_USER_FAIL           = 2003;
    private static final int MSG_AVATAR_UPLOAD_SUCCESS      = AvatarManager.MSG_AVATAR_DOWNLOAD_SUCCESS;
    private static final int MSG_AVATAR_UPLOAD_FAIL         = AvatarManager.MSG_AVATAR_DOWNLOAD_FAIL;
    
    private static final String KEY_INTENT = "source_intent";
    private static final String SOURCE_INTENT_CUSTOMERSERVICE = "com.client.customerservicecenter";

    protected static final String TAG = "UserEditActivity";
    
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_USER_INFO:
                    fillUserInfo((Bitmap) msg.obj);
                    break;
                case MSG_MODIFY_USER_SUCCESS:
                    Object object = msg.obj;
                    if (object instanceof String) {
                        if (OneLoginApplication.sCloudSdkService != null) {
                            try {
                                OneLoginApplication.sCloudSdkService.getUserKey();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        closeProgressDialog();
                        mWarningView.setText((String) object);
                    } else {
                        if (mAvatarBitmap != null) {
                            mAvatarManager.uploadAvatar(mHandler);
                        } else {
                            showSuccessTip();
                        }
                    }
                    break;
                case MSG_MODIFY_USER_FAIL:
                    closeProgressDialog();
                    int reponse = msg.arg1;
                    if (reponse == LoginService.REP_LOGIN_SERVICE_NETWORK_ERROR) {
                        mWarningView.setText(R.string.ex_network_error);
                    } else if (msg.obj instanceof String) {
                        mWarningView.setText((String) msg.obj);
                    }
                    break;
                    
                case MSG_AVATAR_UPLOAD_SUCCESS:
                    showSuccessTip();
                    if (OneLoginApplication.sCloudSdkService != null) {
                        try {
                            OneLoginApplication.sCloudSdkService.getAvatar();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case MSG_AVATAR_UPLOAD_FAIL:
                    closeProgressDialog();
                    mWarningView.setText(R.string.user_avatar_modify_fail);
                    break;
            }
        }
        
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.myos_user_edit_activity);
        mContext = this;
        mLogUtil = new LogUtil(TAG);
        mAuthenticatorManager = OneLoginApplication.authenticatorManager;
        initializeUI();
        initializeData();
        
//        if (mCloudSdkService == null) {
//            Intent service = new Intent(this,CloudSdkService.class);
//            this.bindService(service, cloudSdkServiceConn, Service.BIND_AUTO_CREATE);
//        }
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
                mSaveButton.setEnabled(true);
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
    
    private void initializeUI() {
        mUserInfoUtil = new UserInfoUtil(mContext);
        mAvatarManager = new AvatarManager(UserEditActivity.this);
        mPopupMenuItemClickListener = new PopupMenuItemClickListener();
        mDatePickerSetListener = new DatePickerSetListener();
        mTextChangeListener = new TextChangeListener();
        mRadioCheckedChangeListener = new RadioCheckedChangeListener();
        mActionBar = (MyOSActionBar) findViewById(R.id.actionbar);
        mActionBar.setOptionVisibility(View.GONE);
        mActionBar.setHomeOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        mAvatarView = (CircleImageView) findViewById(R.id.avatar);
        mAvatarButton = (ImageButton) findViewById(R.id.avatar_button);
        mNickNameView = (MyOSEditText) findViewById(R.id.nick_name);
        mBirthdayView = (MyOSEditText) findViewById(R.id.birthday);
        mBirthdayButton = (Button) findViewById(R.id.birthday_button);
        mMobileView = (MyOSEditText) findViewById(R.id.mobile);
        mAddressView = (MyOSEditText) findViewById(R.id.address);
        mEmailView = (MyOSEditText) findViewById(R.id.email);
        mMaleRadio = (RadioButton) findViewById(R.id.male);
        mFemaleRadio = (RadioButton) findViewById(R.id.female);
        mWarningView = (TextView) findViewById(R.id.warning);
        mSaveButton = (Button) findViewById(R.id.save);
        
        mMobileView.setInputType(InputType.TYPE_CLASS_PHONE);
        mEmailView.setInputType(
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        
        mAvatarButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                mAvatarManager.showAvatarMenu(mPopupMenuItemClickListener);
            }
        });
        
        mSaveButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                String result = null;
                mWarningView.setText("");
                if ((result = mUserInfoUtil.checkDate(mBirthdayView.getText().toString())) != null ||
                    (result = mUserInfoUtil.checkMobile(mMobileView.getText().toString())) != null ||
                    (result = mUserInfoUtil.checkEmail(mEmailView.getText().toString())) != null) {
                    mWarningView.setText(result);
                } else {
                    uploadUserInfo();
                }
            }
        });
        
        mBirthdayView.setEditEnable(false);
        mBirthdayButton.setOnClickListener(new OnClickListener() {
            
            @SuppressLint("SimpleDateFormat")
            @Override
            public void onClick(View v) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                Calendar calendar = Calendar.getInstance();
                if (mAuthenticatorManager.isSessionValid()) {
                    try {
                        String birthday = mAuthenticatorManager.getUserData(AbsLoginHandler.KEY_BIRTHDAY);
                        if (birthday == null || birthday.trim().length() == 0 || birthday.equals("null")) {
                            birthday = "";
                        }
                        date = format.parse(birthday);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (date != null) {
                    calendar.setTime(date);
                }
                
                MyOSDatePickerDialog datePicker = new MyOSDatePickerDialog(
                        mContext,
                        mDatePickerSetListener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                configureDatePicker(datePicker.getDatePicker());
                datePicker.show();
            }
        });
        
        mNickNameView.setOnTextChangeListener(mTextChangeListener);
        mMobileView.setOnTextChangeListener(mTextChangeListener);
        mAddressView.setOnTextChangeListener(mTextChangeListener);
        mEmailView.setOnTextChangeListener(mTextChangeListener);
        mMaleRadio.setOnCheckedChangeListener(mRadioCheckedChangeListener);
        mFemaleRadio.setOnCheckedChangeListener(mRadioCheckedChangeListener);
    }
    
    private void initializeData() {
        Bundle data = getIntent().getExtras();
        final String userId = data.getString(AbsLoginHandler.KEY_UID);
        final String fromWhere = data.getString(KEY_INTENT);
        
        if(fromWhere != null && SOURCE_INTENT_CUSTOMERSERVICE.equals(fromWhere)) {
            mActionBar.setActionTitle(getResources().getString(R.string.user_info));
            findViewById(R.id.avatar_container).setVisibility(View.GONE);
        }
        if (userId != null && userId.trim().length() != 0) {
            new Thread() {
                public void run() {
                    // 从本地map中获取本地头像保存地址
                    HashMap<String, String> userMap = mAuthenticatorManager.getUserMapData();
                    String mapUserId = userMap.get(AbsLoginHandler.KEY_UID);
                    String localAvatar = null;
                    if (!userId.equals(mapUserId)) {
                        Log.e(TAG, "error: local user data not match!!");
                    } else {
                        localAvatar = userMap.get(AbsLoginHandler.KEY_AVATAR_PATH);
                    }
                    
                    if (mAuthenticatorManager.isSessionValid()) {
                        String sdkType = mAuthenticatorManager.getUserData(AbsLoginHandler.KEY_SDK_TYPE);
                        Bitmap bitmap = showAvatar(localAvatar, sdkType);
                        
                        Message message = mHandler.obtainMessage();
                        message.what = MSG_LOAD_USER_INFO;
                        message.obj = bitmap;
                        mHandler.sendMessage(message);
                    }
                }
            }.start();
        }
    }
    
    private void fillUserInfo(Bitmap avatar) {
        if (mAuthenticatorManager.isSessionValid()) {
            User user = mAuthenticatorManager.getUserData();
            String nickname = user.getNickName();
            if (nickname == null || nickname.trim().length() == 0 || nickname.equals("null")) {
                nickname = "";
            }
            
            String birthday = user.getBirthday();
            if (birthday == null || birthday.trim().length() == 0 || birthday.equals("null")) {
                birthday = "";
            }
            
            String phone = user.getMobile();
            if (phone == null || phone.trim().length() == 0 || phone.equals("null")) {
                phone = "";
            }
            
            String address = user.getAddress();
            if (address == null || address.trim().length() == 0 || address.equals("null")) {
                address = "";
            }
            
            String email = user.getEmail();
            if (email == null || email.trim().length() == 0 || email.equals("null")) {
                email = "";
            }
            int length = getResources().getInteger(R.integer.max_nickname_length);
            if (nickname.length() > length) {
                nickname = nickname.substring(0, length - 1);
            }
            mNickNameView.setText(nickname);
            mBirthdayView.setText(birthday);
            mMobileView.setText(phone);
            mAddressView.setText(address);
            mEmailView.setText(email);
            
            mMaleRadio = (RadioButton) findViewById(R.id.male);
            mFemaleRadio = (RadioButton) findViewById(R.id.female);
            if (User.MALE.equals(user.getGender())) {
                mMaleRadio.setChecked(true);
                mFemaleRadio.setChecked(false);
            } else if (User.FEMALE.equals(user.getGender())) {
                mMaleRadio.setChecked(false);
                mFemaleRadio.setChecked(true);
            }
            
            if (AbsLoginHandler.SDK_TYPE_CLOUD.equals(user.getSdkType())) {
                mNickNameView.setEditEnable(true);
                mAvatarButton.setEnabled(true);
            } else {
                mNickNameView.setEditEnable(false);
                mAvatarButton.setEnabled(false);
            }
            
            if (avatar != null) {
                mAvatarView.setImageBitmap(avatar);
            }
        }
        
        mSaveButton.setEnabled(false);
    }
    
    private void uploadUserInfo() {
        final String nickname = mNickNameView.getText().toString();
        final String birthday = mBirthdayView.getText().toString();
        final String mobile = mMobileView.getText().toString();
        final String email = mEmailView.getText().toString();
        final String address = mAddressView.getText().toString();
        
        String genderTmp = null;
        if (mMaleRadio.isChecked()) {
            genderTmp = User.MALE;
        } else if (mFemaleRadio.isChecked()) {
            genderTmp = User.FEMALE;
        }
        final String gender = genderTmp;
        
        showProgressDialog(R.string.user_saving);
        if (mAuthenticatorManager.isSessionValid()) {
            new Thread() {
                public void run() {
                    User user = mAuthenticatorManager.getUserData();
                    user.setNickName(nickname);
                    user.setBirthday(birthday);
                    user.setMobile(mobile);
                    user.setEmail(email);
                    user.setAddress(address);
                    user.setGender(gender);
                    
                    Looper.prepare();
                    OneLoginApplication.loginManager.modify(user, new AuthenticListener() {
                        
                        @Override
                        public void onComplete(int result, Object obj, String message) {
                            mLogUtil.i("modify", "modify onComplete result code:%d, message:%s",
                                    result, message == null ? "" : message);
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
                            mLogUtil.i("modify", "modify cancel");
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
    
    static void configureDatePicker(DatePicker datePicker) {
        Calendar t = Calendar.getInstance();
        t.clear();
        t.set(1949, Calendar.OCTOBER, 1);
        datePicker.setMinDate(t.getTimeInMillis());
        t.clear();
        t.set(2037, Calendar.DECEMBER, 31);
        datePicker.setMaxDate(t.getTimeInMillis());
    }
    
    private Bitmap showAvatar(String localAvatar, String sdk) {
        Bitmap bitmap = null;
        if (localAvatar != null && localAvatar.trim().length() != 0) {
            File avatarFile = new File(localAvatar);
            if (avatarFile.exists()) {
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(localAvatar);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        return bitmap;
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
    
    private class DatePickerSetListener implements MyOSDatePickerDialog.OnDateSetListener {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            StringBuffer birthday = new StringBuffer();
            birthday.append(year).append("-").append(monthOfYear + 1).append("-").append(dayOfMonth);
            mBirthdayView.setText(birthday);
            mSaveButton.setEnabled(true);
        }
    }
    
    private class TextChangeListener implements MyOSEditText.OnTextChangeListener {
        
        @Override
        public void afterTextChanged(View view, String text) {
            mSaveButton.setEnabled(true);
        }
        
    }
    
    private class RadioCheckedChangeListener implements OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
            mSaveButton.setEnabled(true);
        }
        
    }
}

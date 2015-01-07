package com.ape.onelogin.myos.widget;

import android.content.Context;

import com.ape.onelogin.util.RegexValidate;
import com.ape.onelogin.R;

public class UserInfoUtil {
    
    private Context mContext;
    
    public UserInfoUtil(Context context) {
        mContext = context;
    }
    
    public String checkAccount(String content) {
        String result = null;
        
        if (content == null || content.trim().length() == 0) {
            return result;
        }
        
        if (!RegexValidate.isSpecialUserNameEx(content)) {
            result = mContext.getString(R.string.ex_register_account_notice);
        }
        return result;
    }
    
    public String checkEmail(String content) {
        String result = null;
        
        if (content == null || content.trim().length() == 0) {
            return mContext.getString(R.string.ex_register_email_empty_notice);
        }
        
        if (!RegexValidate.isEMail(content)) {
            return mContext.getString(R.string.ex_register_email_notice);
        }
        return result;
    }
    
    public String checkMobile(String content) {
        String result = null;
        
        if (content == null || content.trim().length() == 0) {
            return mContext.getString(R.string.ex_register_moblie_empty_notice);
        }
        
        if (!RegexValidate.isMobilPhone(content)) {
            return mContext.getString(R.string.ex_register_moblie_notice);
        }
        return result;
    }
    
    public String checkPassword(String content) {
        String result = null;
        
        if (content == null || content.trim().length() == 0) {
            return result;
        }
        
        if (!RegexValidate.isSpecialPasswordSecond(content)) {
            result = mContext.getString(R.string.ex_register_password_notice);
        }
        return result;
    }
    
    public String checkDate(String content) {
        String result = null;
        
        if (content == null || content.trim().length() == 0) {
            return result;
        }
        
        if (!RegexValidate.isDate(content)) {
            result = mContext.getString(R.string.ex_date_notice);
        }
        return result;
    }
}

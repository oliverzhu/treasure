package com.ape.onelogin.util;

import java.util.HashMap;

import com.ape.onelogin.bean.User;
import com.ape.onelogin.login.core.AbsLoginHandler;
import com.cloud.util.log.LogBase;

public class LogUtil extends LogBase {
    
    public LogUtil(String tag) {
        super("OneLoginLog", tag);
    }
    
    public void i(String method, String format, Object... args) {
        super.i("[%s()]%s", method, String.format(format, args));
    }
    
    public void d(String method, String format, Object... args) {
        super.d("[%s()]%s", method, String.format(format, args));
    }
    
    public void e(String method, String format, Object... args) {
        super.e("[%s()]%s", method, String.format(format, args));
    }
    
    public void w(String method, String format, Object... args) {
        super.w("[%s()]%s", method, String.format(format, args));
    }
    
    public void v(String method, String format, Object... args) {
        super.v("[%s()]%s", method, String.format(format, args));
    }
    
    public void i(String method, String message) {
        super.i("[%s()]%s", method, message);
    }
    
    public void d(String method, String message) {
        super.d("[%s()]%s", method, message);
    }
    
    public void e(String method, String message) {
        super.e("[%s()]%s", method, message);
    }
    
    public void w(String method, String message) {
        super.w("[%s()]%s", method, message);
    }
    
    public void v(String method, String message) {
        super.v("[%s()]%s", method, message);
    }

    public static String formatAvatarMessage(HashMap<String, String> userMap) {
        StringBuffer message = new StringBuffer("<AvatarMap Data>\n");
        
        String userKey = userMap.get(AbsLoginHandler.KEY_USER_KEY);
        message.append("==>userkey:").append(userMap.get(AbsLoginHandler.KEY_USER_KEY)).append("\n");
        
        if (AbsLoginHandler.INVALID_USER_KEY.equals(userKey)) {
            return message.toString();
        }
        
        message.append("==>avatar_path:").append(userMap.get(AbsLoginHandler.KEY_AVATAR_PATH)).append("\n");
        
        return message.toString();
    }
    
    public static String formatUserMessage(HashMap<String, String> userMap) {
        StringBuffer message = new StringBuffer("<UserMap Data>\n");
        
        if (userMap == null || userMap.size() == 0) {
            return "";
        }
        
        String userKey = userMap.get(AbsLoginHandler.KEY_USER_KEY);
        message.append("==>userkey:").append(userMap.get(AbsLoginHandler.KEY_USER_KEY)).append("\n");
        
        if (AbsLoginHandler.INVALID_USER_KEY.equals(userKey)) {
            return message.toString();
        }
        
        message.append("==>uid:").append(userMap.get(AbsLoginHandler.KEY_UID)).append("\n");
        message.append("==>access_token:").append(userMap.get(AbsLoginHandler.KEY_ACCESS_TOKEN)).append("\n");
        message.append("==>expires_in:").append(userMap.get(AbsLoginHandler.KEY_EXPIRES_IN)).append("\n");
        message.append("==>mobile:").append(userMap.get(AbsLoginHandler.KEY_MOBILE)).append("\n");
        message.append("==>email:").append(userMap.get(AbsLoginHandler.KEY_EMAIL)).append("\n");
        message.append("==>imei:").append(userMap.get(AbsLoginHandler.KEY_IMEI)).append("\n");
        
        message.append("==>registerDate:").append(userMap.get(AbsLoginHandler.KEY_REGISTERDATE)).append("\n");
        
        message.append("==>nickname:").append(userMap.get(AbsLoginHandler.KEY_NICKNAME)).append("\n");
        message.append("==>gender:").append(userMap.get(AbsLoginHandler.KEY_GENDER)).append("\n");
        message.append("==>birthday:").append(userMap.get(AbsLoginHandler.KEY_BIRTHDAY)).append("\n");
        message.append("==>address:").append(userMap.get(AbsLoginHandler.KEY_ADDRESS)).append("\n");
        message.append("==>avatar:").append(userMap.get(AbsLoginHandler.KEY_AVATAR)).append("\n");
        message.append("==>avatar_path:").append(userMap.get(AbsLoginHandler.KEY_AVATAR_PATH)).append("\n");
        
        message.append("==>hometown:").append(userMap.get(AbsLoginHandler.KEY_HOMETOWN)).append("\n");
        message.append("==>country:").append(userMap.get(AbsLoginHandler.KEY_COUNTRY)).append("\n");
        message.append("==>province:").append(userMap.get(AbsLoginHandler.KEY_PROVINCE)).append("\n");
        message.append("==>city:").append(userMap.get(AbsLoginHandler.KEY_CITY)).append("\n");
        message.append("==>profession:").append(userMap.get(AbsLoginHandler.KEY_PROFESSION)).append("\n");
        
        message.append("==>accessid:").append(userMap.get(AbsLoginHandler.KEY_ACCESSID)).append("\n");
        message.append("==>secretkey:").append(userMap.get(AbsLoginHandler.KEY_SECRETKEY)).append("\n");
        message.append("==>bucketname:").append(userMap.get(AbsLoginHandler.KEY_BUCKETNAME)).append("\n");
        message.append("==>osstype:").append(userMap.get(AbsLoginHandler.KEY_OSSTYPE)).append("\n");
        message.append("==>osslocal:").append(userMap.get(AbsLoginHandler.KEY_OSSLOCAL)).append("\n");
        message.append("==>sdk_type:").append(userMap.get(AbsLoginHandler.KEY_SDK_TYPE)).append("\n");
        
        message.append("==>field1:").append(userMap.get(AbsLoginHandler.KEY_FIELD1)).append("\n");
        message.append("==>field2:").append(userMap.get(AbsLoginHandler.KEY_FIELD2)).append("\n");
        message.append("==>field3:").append(userMap.get(AbsLoginHandler.KEY_FIELD3)).append("\n");
        message.append("==>field4:").append(userMap.get(AbsLoginHandler.KEY_FIELD4)).append("\n");
        message.append("==>field5:").append(userMap.get(AbsLoginHandler.KEY_FIELD5)).append("\n");
        
        return message.toString();
    }
    
    public static String formatUserMessage(User user) {
        StringBuffer message = new StringBuffer("<User entity Data>\n");
        
        message.append("-->id:").append(user.getId()).append("\n");
        message.append("-->userName:").append(user.getUserName()).append("\n");
        message.append("-->password:").append(user.getPassword()).append("\n");
        message.append("-->expiresIn:").append(user.getExpiresIn()).append("\n");
        message.append("-->mobile:").append(user.getMobile()).append("\n");
        message.append("-->email:").append(user.getEmail()).append("\n");
        message.append("-->imei:").append(user.getImei()).append("\n");
        
        message.append("-->registerDate:").append(user.getRegisterDate()).append("\n");
        
        message.append("-->nickname:").append(user.getNickName()).append("\n");
        message.append("-->gender:").append(user.getGender()).append("\n");
        message.append("-->birthday:").append(user.getBirthday()).append("\n");
        message.append("-->address:").append(user.getAddress()).append("\n");
        message.append("-->avatar:").append(user.getAvatar()).append("\n");
        
        message.append("-->hometown:").append(user.getHomeTown()).append("\n");
        message.append("-->country:").append(user.getCountry()).append("\n");
        message.append("-->province:").append(user.getProvince()).append("\n");
        message.append("-->city:").append(user.getCity()).append("\n");
        message.append("-->profession:").append(user.getProfession()).append("\n");
        
        message.append("-->accessid:").append(user.getAccessId()).append("\n");
        message.append("-->secretkey:").append(user.getSecretKey()).append("\n");
        message.append("-->bucketname:").append(user.getBucketName()).append("\n");
        message.append("-->osstype:").append(user.getOssType()).append("\n");
        message.append("-->osslocal:").append(user.getOssLocal()).append("\n");
        message.append("-->sdk_type:").append(user.getSdkType()).append("\n");
        
        message.append("-->field1:").append(user.getField1()).append("\n");
        message.append("-->field2:").append(user.getField2()).append("\n");
        message.append("-->field3:").append(user.getField3()).append("\n");
        message.append("-->field4:").append(user.getField4()).append("\n");
        message.append("-->field5:").append(user.getField5()).append("\n");
        
        return message.toString();
    }
}

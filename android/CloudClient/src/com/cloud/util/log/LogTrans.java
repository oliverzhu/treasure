package com.cloud.util.log;

import com.cloud.client.CloudUtil;
import com.cloud.client.sql.SQLResultParser;

import net.tsz.afinal.http.AjaxParams;

public class LogTrans extends LogBase {
    
    public LogTrans(String tag) {
        super("CloudClientService", tag);
    }

    private static String logMode(int mode) {
        String str = null;
        if (mode == ActionMode.Request) {
            str = "[Request]";
        } else if (mode == ActionMode.Response) {
            str = "[Response]";
        }
        return str;
    }
    
    public void i(int mode, int result, int action, AjaxParams params, String msg) {
        if (CloudUtil.DEBUG) {
            super.i(formatMessage(mode, result, action, params, msg));
        }
    }
    
    public void v(int mode, int result, int action, AjaxParams params, String msg) {
        if (CloudUtil.DEBUG) {
            super.v(formatMessage(mode, result, action, params, msg));
        }
    }
    
    public void w(int mode, int result, int action, AjaxParams params, String msg) {
        if (CloudUtil.DEBUG) {
            super.w(formatMessage(mode, result, action, params, msg));
        }
    }
    
    public void e(int mode, int result, int action, AjaxParams params, String msg) {
        if (CloudUtil.DEBUG) {
            super.e(formatMessage(mode, result, action, params, msg));
        }
    }
    
    public void d(int mode, int result, int action, AjaxParams params, String msg) {
        if (CloudUtil.DEBUG) {
            super.d(formatMessage(mode, result, action, params, msg));
        }
    }
    
    private String formatMessage(int mode, int result, int action, AjaxParams params, String msg) {
        StringBuffer log = new StringBuffer();
        log.append(logMode(mode)).append("\n");
        log.append("==>Table:").append(params.get(CloudUtil.SQL_PARAM_TABLE)).append("\n");
        if (mode == ActionMode.Request) {
            log.append("==>RequestAction:").append(SQLResultParser.Action(Integer.parseInt(params.get(CloudUtil.SQL_PARAM_ACTION)))).append("\n");
        }
        log.append("==>ResponseAction:").append(SQLResultParser.Action(action)).append("\n");
        if (mode != ActionMode.Request) {
            log.append("==>Result:").append(SQLResultParser.Result(result)).append("\n");
        }
        log.append("==>provider_key_plus:").append(params.get(CloudUtil.SQL_PARAM_PROVIDER_KEY_PLUS)).append("\n");
        log.append("==>provider_apk_name:").append(params.get(CloudUtil.SQL_PARAM_PROVIDER_APK_NAME)).append("\n");
        log.append("==>provider_client_time:").append(params.get(CloudUtil.SQL_PARAM_PROVIDER_CLIENT_TIME)).append("\n");
        log.append("==>user_key_plus:").append(params.get(CloudUtil.SQL_PARAM_USER_KEY_PLUS)).append("\n");
        log.append("==>user_apk_name:").append(params.get(CloudUtil.SQL_PARAM_USER_APK_NAME)).append("\n");
        log.append("==>user_client_time:").append(params.get(CloudUtil.SQL_PARAM_USER_CLIENT_TIME)).append("\n");
        log.append("==>version:").append(params.get(CloudUtil.SQL_PARAM_VERSION_NO)).append("\n");
        log.append("==>").append(msg);
        return log.toString();
    }
    
    public class ActionMode{
        public static final int Request = 0;
        public static final int Response = 1;
    }
}

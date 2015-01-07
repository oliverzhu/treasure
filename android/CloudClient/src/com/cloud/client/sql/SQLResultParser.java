package com.cloud.client.sql;

import com.cloud.client.CloudUtil;


public class SQLResultParser {
    public static String Action(int action){
        String strAction = null;
        switch (action) {
        case CloudUtil.SQL_ACTION_CREATE:
            strAction = "Create";
            break;
        case CloudUtil.SQL_ACTION_DROP:
            strAction = "Drop";
            break;
        case CloudUtil.SQL_ACTION_SELECT:
            strAction = "Select";
            break;
        case CloudUtil.SQL_ACTION_INSERT:
            strAction = "Insert";
            break;
        case CloudUtil.SQL_ACTION_UPDATE:
            strAction = "Update";
            break;
        case CloudUtil.SQL_ACTION_DELETE:
            strAction = "Delete";
            break;
        case CloudUtil.SQL_ACTION_INSERTORUPDATE:
            strAction = "Insert or Update";
            break;
        case CloudUtil.SQL_ACTION_SELECTORINSERT:
            strAction = "Select or Insert";
            break;
        case CloudUtil.SQL_ACTION_SELECTORUPDATE:
            strAction = "Select or Update";
            break;
        case CloudUtil.SQL_ACTION_SEND_EMAIL:
            strAction = "Send Email";
            break;
        default:
            strAction = "Unknown";
            break;
        }
        return strAction;
    }

    public static String Result(int result) {
        String strAction = null;
        switch (result) {
        case CloudUtil.CLOUDCLIENT_RESULT_OK:
            strAction = "result OK~~~";
            break;
        case CloudUtil.CLOUDCLIENT_OBJECT_ENTIY_IS_NULL:
            strAction = "entity is null";
            break;
        case CloudUtil.CLOUDCLIENT_OBJECT_ENTIY_IS_ERROR:
            strAction = "entity is error";
            break;
        case CloudUtil.CLOUDCLIENT_OBJECT_CLASS_UNKNOWN:
            strAction = "class unknown";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_TABLE_NO_EXIST:
            strAction = "table not exist";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_GRAMMAR_ERROR:
            strAction = "sql grammar error";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_CANNOT_EXECUTE:
            strAction = "sql cannot execute";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_ACTION_ERROR:
            strAction = "sql action error";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_AUTHENTICATION_ERROR:
            strAction = "sql authentication error";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_LOCAL_DATE_ERROR:
            strAction = "local system date error";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_DUPLICATE_ENTRY:
            strAction = "sql duplicate entry";
            break;
        case CloudUtil.CLOUDCLIENT_SQL_INTEGRITY_CONSTRAINT:
            strAction = "integrity constraint";
            break;
        case CloudUtil.RESPONSE_SEND_EMAIL_FAIL:
            strAction = "send email fail";
            break;
            
        case CloudUtil.CLOUD_NETWORK_UNKNOWN_ERROR:
            strAction = "unknown network error";
            break;
        case CloudUtil.CLOUD_NETWORK_HOST_CONNECT_REFUSED:
            strAction = "host connect refused";
            break;
        case CloudUtil.CLOUD_NETWORK_CONNECT_TIMEOUT:
            strAction = "connect timeout";
            break;
        case CloudUtil.CLOUD_NETWORK_SOCKET_TIMEOUT:
            strAction = "socket timeout";
            break;
        case CloudUtil.CLOUD_NETWORK_UNKNOWN_HOST:
            strAction = "unknown host error";
            break;
        case CloudUtil.CLOUD_NETWORK_ON_MAIN_THREAD:
            strAction = "network on main thread";
            break;
            
        case CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR:
        default:
            strAction = "Unknown error";
            break;
        }
        return strAction;
    }
}

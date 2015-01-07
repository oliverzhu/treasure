package com.cloud.util.log;

import com.cloud.client.CloudUtil;
import com.cloud.client.file.MissionObject;
import com.cloud.client.file.MissionObject.MissionType;

public class LogMission extends LogBase {
    
    public LogMission(String tag) {
        super("CloudClientService", tag);
    }

    public void i(String method, String msg, MissionObject object) {
        if (CloudUtil.DEBUG) {
            super.i(formatMessage(method, msg, object));
        }
    }
    
    public void d(String method, String msg, MissionObject object) {
        if (CloudUtil.DEBUG) {
            super.d(formatMessage(method, msg, object));
        }
    }
    
    public void e(String method, String msg, MissionObject object) {
        if (CloudUtil.DEBUG) {
            super.e(formatMessage(method, msg, object));
        }
    }
    
    public void v(String method, String msg, MissionObject object) {
        if (CloudUtil.DEBUG) {
            super.v(formatMessage(method, msg, object));
        }
    }
    
    public void w(String method, String msg, MissionObject object) {
        if (CloudUtil.DEBUG) {
            super.w(formatMessage(method, msg, object));
        }
    }
    
    private String formatMessage(String method, String msg, MissionObject object) {
        if (object == null) {
            return "";
        }
        StringBuffer log = new StringBuffer();
        log.append("[").append(method).append("()]\n");
        log.append("==>Message:").append(msg).append("\n");
        log.append("==>MissionObjectId:").append(object.getId()).append("\n");
        log.append("==>MissionType:").append(object.getMissionType()).append("\n");
        if (object.getMissionType() == MissionType.UPLOAD_PART) {
            log.append("==>UploadId:").append(object.getUploadId());
        }
        return log.toString();
    }
}

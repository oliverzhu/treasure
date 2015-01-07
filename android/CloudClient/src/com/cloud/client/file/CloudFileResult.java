package com.cloud.client.file;

import java.util.List;

import com.cloud.client.CloudUtil;

public class CloudFileResult {
    private int code;
    private String message;
    private List<CloudFile> fileList;
    private List<MissionObject> missionList;
    private List<MissionObject> unKnownMission;
    
    public CloudFileResult() {
        this.code = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
    }

    public int getResultCode() {
        return code;
    }

    public void setResultCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setFileList(List<CloudFile> fileList) {
        this.fileList = fileList;
    }
    
    public List<CloudFile> getFileList() {
        return this.fileList;
    }

    public List<MissionObject> getMissionList() {
        return missionList;
    }

    public void setMissionList(List<MissionObject> missionList) {
        this.missionList = missionList;
    }

    public List<MissionObject> getUnKnownMission() {
        return unKnownMission;
    }

    public void setUnKnownMission(List<MissionObject> unKnownMission) {
        this.unKnownMission = unKnownMission;
    }
}

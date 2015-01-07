package com.cloud.client.file;

import java.util.List;

import com.aliyun.android.oss.model.Part;

public class MissionObject {
    private long id;
    private String userId;
    private String key;
    private String localFile;
    private String tempFile;
    private String md5;
    private String uploadId;
    private int totalParts = 0;
    private long initiatedTime = 0L;
    private long lastTime = 0L;
    private long transferredLength = 0L;
    private long fileLength = 0L;
    private boolean isFinished = false;
    private boolean isPaused = false;
    private List<Part> finishPartList = null;
    private int transferredParts = 0;
    private MissionType missionType;
    private boolean existInServer;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MissionObject other = (MissionObject) obj;
        if (id != other.id)
            return false;
        return true;
    }

    public enum MissionType {
        NONE,
        UPLOAD,
        UPLOAD_PART,
        DOWNLOAD;
    }

    /**
     * {@link MissionObject}构造方法，使用此构造方法，必须调用setMissionType()设置任务类型
     * 
     * @param userId
     * @param key
     * @param localFile
     */
    public MissionObject(String userId, String key, String localFile) {
        this(MissionType.NONE, userId, key, localFile);
    }

    /**
     * {@link MissionObject}构造方法
     * 
     * @param missionType {@link MissionType}
     * @param userId
     * @param key
     * @param localFile
     */
    public MissionObject(MissionType missionType, String userId, String key, String localFile) {
        this.initiatedTime = System.currentTimeMillis();
        this.missionType = missionType;
        this.localFile = localFile;
        this.userId = userId;
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getLocalFile() {
        return localFile;
    }
    
    public void setLocalFile(String localFile) {
        this.localFile = localFile;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public long getInitiatedTime() {
        return initiatedTime;
    }
    
    public void setInitiatedTime(long initiatedTime) {
        this.initiatedTime = initiatedTime;
    }
    
    public long getTransferredLength() {
        return transferredLength;
    }
    
    public void setTransferredLength(long transferredLength) {
        this.transferredLength = transferredLength;
    }
    
    public int getTotalParts() {
        return totalParts;
    }
    
    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }
    
    public boolean isFinished() {
        return isFinished;
    }
    
    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }
    
    public MissionType getMissionType() {
        return missionType;
    }
    
    public void setMissionType(MissionType missionType) {
        this.missionType = missionType;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Part> getFinishPartList() {
        return finishPartList;
    }

    public void setFinishPartList(List<Part> finishPartList) {
        this.finishPartList = finishPartList;
    }
    
    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }
    
    public String getUploadId() {
        return uploadId;
    }

    public boolean isExistInServer() {
        return existInServer;
    }

    public void setExistInServer(boolean existInServer) {
        this.existInServer = existInServer;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getTransferredParts() {
        return transferredParts;
    }

    public void setTransferredParts(int transferredParts) {
        this.transferredParts = transferredParts;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public String getTempFile() {
        return tempFile;
    }

    public void setTempFile(String tempFile) {
        this.tempFile = tempFile;
    }
    
}

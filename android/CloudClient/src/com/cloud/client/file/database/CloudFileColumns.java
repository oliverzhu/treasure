package com.cloud.client.file.database;

import com.cloud.client.file.database.CloudFileDatabaseField.Download;
import com.cloud.client.file.database.CloudFileDatabaseField.MultiPart;
import com.cloud.client.file.database.CloudFileDatabaseField.Upload;

public class CloudFileColumns {
    // the upload table's base information
    public static class UploadBaseColumns {
        public static final String[] UPLOAD_BASE_COLUMNS = {
            Upload._ID,
            Upload._USER_ID,
            Upload.FIELD_KEY
        };
        
        public static final int CLOUMNS_ID = 0;
        public static final int CLOUMNS_USER_ID = 1;
        public static final int CLOUMNS_KEY = 2;
    }
    
    // the upload table's all information
    public static class UploadAllColumns {
        public static final String[] UPLOAD_ALL_COLUMNS = {
            Upload._ID,
            Upload._USER_ID,
            Upload.FIELD_KEY,
            Upload.FIELD_LOCALFILE,
            Upload.FIELD_MD5,
            Upload.FIELD_LENGTH,
            Upload.FIELD_TRANSFERRED_LENGTH,
            Upload.FIELD_FINISH,
            Upload.FIELD_PAUSED,
            Upload.FIELD_INITIATED_TIME,
            Upload.FIELD_LAST_TIME,
            Upload.FIELD_MULTIPART,
            Upload.FIELD_UPLOADID,
            Upload.FIELD_TOTAL_PART,
            Upload.FIELD_TRANSFERRED_PARTS
        };
        
        public static final int CLOUMNS_ID = 0;
        public static final int CLOUMNS_USER_ID = 1;
        public static final int CLOUMNS_KEY = 2;
        public static final int CLOUMNS_LOCALFILE = 3;
        public static final int CLOUMNS_MD5 = 4;
        public static final int CLOUMNS_LENGTH = 5;
        public static final int CLOUMNS_TRANSFERRED_LENGTH = 6;
        public static final int CLOUMNS_FINISH = 7;
        public static final int CLOUMNS_PAUSED = 8;
        public static final int CLOUMNS_INITIATED_TIME = 9;
        public static final int CLOUMNS_LAST_TIME = 10;
        public static final int CLOUMNS_MULTIPART = 11;
        public static final int CLOUMNS_UPLOADID = 12;
        public static final int CLOUMNS_TOTAL_PART = 13;
        public static final int CLOUMNS_TRANSFERRED_PARTS = 14;
    }
    
    // the download table's base information
    public static class DownloadBaseCloumns {
        public static final String[] DOWNLOAD_BASE_CLOUMNS = {
            Download._ID,
            Download._USER_ID,
            Download.FIELD_KEY
        };
        
        public static final int CLOUMNS_ID = 0;
        public static final int CLOUMNS_USER_ID = 1;
        public static final int CLOUMNS_KEY = 2;
    }
    
    // the download table's all information
    public static class DownloadAllCloumns {
        public static final String[] DOWNLOAD_ALL_CLOUMNS = {
            Download._ID,
            Download._USER_ID,
            Download.FIELD_KEY,
            Download.FIELD_LOCALFILE,
            Download.FIELD_TEMP_FILE,
            Download.FIELD_MD5,
            Download.FIELD_LENGTH,
            Download.FIELD_TRANSFERRED_LENGTH,
            Download.FIELD_FINISH,
            Download.FIELD_PAUSED,
            Download.FIELD_INITIATED_TIME,
            Download.FIELD_LAST_TIME,
            Download.FIELD_EXIST_IN_SERVER
        };
        
        public static final int CLOUMNS_ID = 0;
        public static final int CLOUMNS_USER_ID = 1;
        public static final int CLOUMNS_KEY = 2;
        public static final int CLOUMNS_LOCALFILE = 3;
        public static final int CLOUMNS_TEMP_FILE = 4;
        public static final int CLOUMNS_MD5 = 5;
        public static final int CLOUMNS_LENGTH = 6;
        public static final int CLOUMNS_TRANSFERRED_LENGTH = 7;
        public static final int CLOUMNS_FINISH = 8;
        public static final int CLOUMNS_PAUSED = 9;
        public static final int CLOUMNS_INITIATED_TIME = 10;
        public static final int CLOUMNS_LAST_TIME = 11;
        public static final int CLOUMNS_EXIST_IN_SERVER = 12;
        
    }
    
    // the multipart table's base information
    public static class MultiPartBaseCloumns {
        public static final String[] MULTIPART_BASE_CLOUMNS = {
            MultiPart._ID
        };
        
        public static final int CLOUMNUS_ID = 0;
    }
    
    // the multipart table's all information
    public static class MultiPartAllCloumns {
        public static final String[] MULTIPART_ALL_CLOUMNS = {
            MultiPart._ID,
            MultiPart.FIELD_UPLOAD_ID,
            MultiPart.FIELD_PART_NO,
            MultiPart.FIELD_PART_MD5,
            MultiPart.FIELD_PART_NAME,
            MultiPart.FIELD_LAST_MODIFIED,
            MultiPart.FIELD_SIZE
        };
        
        public static final int CLOUMNUS_ID = 0;
        public static final int CLOUMNS_UPLOAD_ID = 1;
        public static final int CLOUMNS_PART_NO = 2;
        public static final int CLOUMNS_PART_MD5 = 3;
        public static final int CLOUMNS_PART_NAME = 4;
        public static final int CLOUMNS_LAST_MODIFIED = 5;
        public static final int CLOUMNS_SIZE = 6;
    }
}

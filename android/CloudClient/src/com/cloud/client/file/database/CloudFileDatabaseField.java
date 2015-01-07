package com.cloud.client.file.database;

public class CloudFileDatabaseField {
    
    public interface BaseField {
        
        public static final String _ID = "_id";
        
        public static final String _USER_ID = "_user_id";
    }

    public interface BaseMissionField extends BaseField{
        
        public static final String FIELD_KEY = "key";
        
        public static final String FIELD_LOCALFILE = "local_file";
        
        public static final String FIELD_MD5 = "md5";
        
        public static final String FIELD_LENGTH = "file_length";
        
        public static final String FIELD_TRANSFERRED_LENGTH = "transferred_length";
        
        public static final String FIELD_FINISH = "is_finish";
        
        public static final String FIELD_PAUSED = "is_paused";
        
        public static final String FIELD_INITIATED_TIME = "initiated_time";
        
        public static final String FIELD_LAST_TIME = "last_time";
    }

    public static class Upload implements BaseMissionField {
        
        public static final String FIELD_MULTIPART = "is_multipart";
        
        public static final String FIELD_UPLOADID = "uploadId";
        
        public static final String FIELD_TOTAL_PART = "total_part";
        
        public static final String FIELD_TRANSFERRED_PARTS = "transferred_parts";
    }
    
    public static class Download implements BaseMissionField {
        
        public static final String FIELD_TEMP_FILE = "temp_file";
        
        public static final String FIELD_EXIST_IN_SERVER = "is_exist_in_server";
    }
    
    public static class MultiPart implements BaseField {
        
        public static final String FIELD_UPLOAD_ID = "_upload_id";
        
        public static final String FIELD_PART_MD5 = "part_md5";
        
        public static final String FIELD_PART_NO = "part_NO";
        
        public static final String FIELD_PART_NAME = "part_name";
        
        public static final String FIELD_LAST_MODIFIED = "last_modified";
        
        public static final String FIELD_SIZE = "size";
    }
}

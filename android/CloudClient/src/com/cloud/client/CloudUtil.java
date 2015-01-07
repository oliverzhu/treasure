package com.cloud.client;

public class CloudUtil {
    
//    public static final String POST_URL = "http://192.168.126.128:8080/CloudTest/index.jsp";
//    public static final String POST_URL = "http://ht093-win7.jstinno:8080/MyCloud/clientAction!sqlExecute.action";
    
    // debug
//    public static final String URL_PREFIX = "http://192.168.33.101:8080";
//    public static final String POST_EMAIL_URL = URL_PREFIX + "/cloudserver/clientAction!emailExecute.action";
//    public static final String POST_URL = URL_PREFIX + "/cloudserver/clientAction!sqlExecute.action";
    
//    public static final String URL_PREFIX = "http://ota.tinno.com";
    public static final String URL_PREFIX = "http://121.40.29.14:8080";
    public static final String POST_EMAIL_URL = URL_PREFIX + "/CloudServer/clientAction!emailExecute.action";
    public static final String POST_URL = URL_PREFIX + "/CloudServer/clientAction!sqlExecute.action";
    
    public static final String URL_ANTHORIZE = "/CloudServer/clientAction!authorize.action?provider_apk_key_plus=#&provider_apk_name=#&provider_client_time=#&user_apk_key_plus=#&user_apk_name=#&user_client_time=#";
    public static final boolean DEBUG = true;
    
    public static final String CLIENT_VERSION                               = "0.01.04";
    public static final String SQL_VERSION_NO                               = "1002";
    public static final int CLOUD_FILE_DATABASE_VERSION                     = 1000;
    
    public static final int ALI_PART_SIZE                                   = 500 * 1024;           // 阿里云上传文件分块大小
    public static final int AMAZON_PART_SIZE                                = 5 * 1024 * 1024;      // Amazon上传文件分块大小
    
    public static final int CLOUDCLIENT_RESULT_OK                           = 100;      // 正确操作
    public static final int CLOUDCLIENT_UNKNOWN_ERROR                       = -100;     // 未知错误操作
    
    /**SQL_PARAM_VERSION_NO
     * 数据服务器操作错误码
     */
    public static final int CLOUDCLIENT_OBJECT_ENTIY_IS_NULL                = -101;     // 对象不能为空
    public static final int CLOUDCLIENT_OBJECT_ENTIY_IS_ERROR               = -102;     // 对象错误
    public static final int CLOUDCLIENT_OBJECT_CLASS_UNKNOWN                = -103;     // 未知类
    public static final int CLOUDCLIENT_SQL_TABLE_NO_EXIST                  = -104;     // 服务器数据表不存在
    public static final int CLOUDCLIENT_SQL_GRAMMAR_ERROR                   = -105;     // SQL语法错误
    public static final int CLOUDCLIENT_SQL_CANNOT_EXECUTE                  = -106;     // SQL执行错误
    public static final int CLOUDCLIENT_SQL_ACTION_ERROR                    = -107;     // 错误Action
    public static final int CLOUDCLIENT_SQL_AUTHENTICATION_ERROR            = -108;     // 鉴权错误
    public static final int CLOUDCLIENT_SQL_LOCAL_DATE_ERROR                = -109;     // 本地时间错误
    public static final int CLOUDCLIENT_SQL_DUPLICATE_ENTRY                 = -110;     // 记录重复
    public static final int CLOUDCLIENT_SQL_INTEGRITY_CONSTRAINT            = -111;     // 数据健全性完整性方面的错误
    public static final int CLOUDCLIENT_DECRYPT_SERVER_ERROR                = -112;     // 解析服务器信息失败
    public static final int CLOUDCLIENT_CREATE_FILECLIENT_ERROR             = -113;     // 创建文件操作对象失败
    public static final int CLOUDCLIENT_USER_ID_ERROR                       = -114;     // 创建文件操作对象失败
    public static final int CLOUDCLIENT_ENTITY_INITIALIZATION               = -115;     // 用于对象初始化
    public static final int CLOUDCLIENT_UNKNOWN_SERVER_ERROR                = -200;     // 数据服务器未知错误

    public static final int CLOUD_NETWORK_UNKNOWN_ERROR                     = -300;     // 未知网络错误
    public static final int CLOUD_NETWORK_HOST_CONNECT_REFUSED              = -301;     // host访问拒绝
    public static final int CLOUD_NETWORK_CONNECT_TIMEOUT                   = -302;     // 连接超时
    public static final int CLOUD_NETWORK_SOCKET_TIMEOUT                    = -303;     // socket超时
    public static final int CLOUD_NETWORK_UNKNOWN_HOST                      = -304;     // 未知主机
    public static final int CLOUD_NETWORK_ON_MAIN_THREAD                    = -305;     // 网络请求线程在主线程
    
    /**
     * 文件服务器操作错误码 
     */
//    public static final int CLOUD_FILE_UNKNOWN_ERROR                      = -1000;
    public static final int CLOUD_FILE_NETWORK_ERROR                        = CLOUD_NETWORK_UNKNOWN_ERROR;    // 未知网络错误
    public static final int CLOUD_FILE_CREATE_BUCKER_ERROR                  = -1002;    // 创建bucket失败
    public static final int CLOUD_FILE_LIST_FILE_ERROR                      = -1003;    // 获取文件列表错误
    public static final int CLOUD_FILE_DOWNLOAD_FILE_ERROR                  = -1004;    // 下载文件错误
    public static final int CLOUD_FILE_DELETE_FILE_ERROR                    = -1005;    // 删除文件错误
    public static final int CLOUD_FILE_ILLEGAL_ARGUMENT                     = -1006;    // 非法参数
    public static final int CLOUD_FILE_NOT_FILE                             = -1007;    // 不是文件
    public static final int CLOUD_FILE_NOT_DIR                              = -1008;    // 不是目录
    public static final int CLOUD_FILE_NOT_FIND                             = -1009;    // 相关文件或目录不存在
    public static final int CLOUD_FILE_MISSION_CANCEL                       = -1010;    // 取消任务
    public static final int CLOUD_FILE_FILE_NOT_EXIST                       = -1011;    // 文件不存在
    public static final int CLOUD_FILE_FILE_EXIST                           = -1012;    // 文件已存在
    
    /**
     * 文件服务器第三方API操作错误码
     */
    public static final int CLOUD_FILE_ACCESS_DENIED                        = -1101;    // 拒绝访问
    public static final int CLOUD_FILE_BUCKET_ALREADY_EXISTS                = -1102;    // Bucket已经存在
    public static final int CLOUD_FILE_BUCKET_NOT_EMPTY                     = -1103;    // Bucket不为空
    public static final int CLOUD_FILE_ENTITY_TOO_LARGE                     = -1104;    // 实体过大
    public static final int CLOUD_FILE_INVALID_BUCKET_NAME                  = -1105;    // Bucket不符合命名规范
    public static final int CLOUD_FILE_TOO_MANY_BUCKETS                     = -1106;    // 超过bucket最大创建数
    public static final int CLOUD_FILE_INVALID_ACCESS_KEY_ID                = -1107;    // 不合法的AccessKey和AccessId
    public static final int CLOUD_FILE_NO_SUCH_BUCKET                       = -1108;    // Bucket不存在
    public static final int CLOUD_FILE_INVALID_ARGUMENTS                    = -1109;    // 非法参数
    public static final int CLOUD_FILE_ENCRYPT_KEY_NOT_EMPTY                = -1110;    // 加密密钥不为空
    public static final int CLOUD_FILE_ENCRYPT_ERROR                        = -1111;    // 加密错误
    public static final int CLOUD_FILE_DECRYPT_KEY_NOT_EMPTY                = -1112;    // 解密密钥不为空
    public static final int CLOUD_FILE_DECRYPT_ERROR                        = -1113;    // 解密错误
    public static final int CLOUD_FILE_ZIP_ERROR                            = -1114;    // 压缩错误
    public static final int CLOUD_FILE_UNZIP_ERROR                          = -1115;    // 解压缩错误
    public static final int CLOUD_FILE_PARSE_METADATA_ERROR                 = -1116;    // 解析对象元信息错误
    public static final int CLOUD_FILE_PARSE_XML_ERROR                      = -1117;    // 解析Xml错误
    public static final int CLOUD_FILE_GET_ENTITY_CONTENT_ERROR             = -1118;    // 获取对象内容错误
    public static final int CLOUD_FILE_ETAG_HEADER_EMPTY                    = -1119;    // ETag header为空(MD5)
    public static final int CLOUD_FILE_NO_UPLOADID_RETURN                   = -1120;    // 服务器未返回upload id
    public static final int CLOUD_FILE_PART_NUMBER_EXEEDED                  = -1121;    // 分组数据超过上限
    public static final int CLOUD_FILE_IOEXCEPTION                          = -1122;    // IO异常
    public static final int CLOUD_FILE_DATABASE_ERROR                       = -1123;    // 数据库错误
    public static final int CLOUD_FILE_NO_SUCH_KEY                          = -1124;    // key不存在
    
    /**
     * 保存在本地关于文件上传信息数据表的操作错误码
     */
    public static final int CLOUD_FILE_DB_OPERATE_ERROR                     = -2000;    // 本地数据库操作错误
    public static final int CLOUD_FILE_DB_WRONG_ID                          = -2001;    // id错误
    public static final int CLOUD_FILE_DB_DATA_EXIST                        = -2002;    // 记录已存在
    public static final int CLOUD_FILE_DB_DATA_NOT_UNIQUENESS               = -2003;    // 记录不唯一
    public static final int CLOUD_FILE_DB_DATA_NOT_FOUND                    = -2004;    // 记录不存在
    public static final int CLOUD_FILE_DB_INSERT_ERROR                      = -2005;    // 本地数据表插入错误
    public static final int CLOUD_FILE_DB_INSERT_PART_ERROR                 = -2006;    // 本地分块数据表插入错误
    public static final int CLOUD_FILE_DB_UPDATE_ERROR                      = -2007;    // 本地数据表更新错误
    public static final int CLOUD_FILE_DB_UPDATE_PART_ERROR                 = -2008;    // 本地分块数据表更新错误
    
    public static final int RESPONSE_SEND_EMAIL_FAIL                        = -3000;
    
    /**
     * 与数据服务器交互的操作码
     */
    public static final int SQL_ACTION_CREATE                               = 201;
    public static final int SQL_ACTION_DROP                                 = 202;
    public static final int SQL_ACTION_SELECT                               = 203;
    public static final int SQL_ACTION_INSERT                               = 204;
    public static final int SQL_ACTION_UPDATE                               = 205;
    public static final int SQL_ACTION_DELETE                               = 206;
    public static final int SQL_ACTION_INSERTORUPDATE                       = 207;
    public static final int SQL_ACTION_SELECTORUPDATE                       = 208;
    public static final int SQL_ACTION_SELECTORINSERT                       = 209;
    
    public static final int SQL_ACTION_SEND_EMAIL                           = 300;
    
    /**
     * 与数据服务器交互的参数
     */
    public static final String SQL_PARAM_SQL                                = "SQL";
    public static final String SQL_PARAM_SQL_PART_INSERT                    = "SQL_PART_INSERT";
    public static final String SQL_PARAM_SQL_PART_UPDATE                    = "SQL_PART_UPDATE";
    public static final String SQL_PARAM_SQL_PART_DELETE                    = "SQL_PART_DELETE";
    public static final String SQL_PARAM_ACTION                             = "ACTION";
    public static final String SQL_PARAM_TABLE                              = "TABLE_NAME";
    public static final String SQL_PARAM_PROVIDER_KEY_PLUS                  = "provider_apk_key_plus";
    public static final String SQL_PARAM_PROVIDER_APK_NAME                  = "provider_apk_name";
    public static final String SQL_PARAM_PROVIDER_CLIENT_TIME               = "provider_client_time";
    public static final String SQL_PARAM_USER_KEY_PLUS                      = "user_apk_key_plus";
    public static final String SQL_PARAM_USER_APK_NAME                      = "user_apk_name";
    public static final String SQL_PARAM_USER_CLIENT_TIME                   = "user_client_time";
    public static final String SQL_PARAM_VERSION_NO                         = "VERSION_NO";
//    public static final String SQL_PARAM_SESSION_ID                       = "SESSIONID";
    
    public static final String SQL_RESULT_CODE                              = "RESULT_CODE";
    public static final String SQL_RESULT_SQL                               = "SQL_RESULT";
    public static final String SQL_RESULT_ACTION                            = "ACTION";
    public static final String SQL_RESULT_DATA                              = "DATA";
//    public static final String SQL_RESULT_SESSIONID                       = "SESSIONID";
    public static final String SQL_RESULT_RECORD_ID                         = "RECORD_ID";
    
    /**
     * 发送Email操作参数
     */
    public static final String EMAIL_PARAM_ADDRESS                          = "email2Address";
    public static final String EMAIL_PARAM_TITLE                            = "emailTitle";
    public static final String EMAIL_PARAM_BODY                             = "emailBody";
    public static final String EMAIL_PARAM_VERSION                          = SQL_PARAM_VERSION_NO;
    public static final String EMAIL_PARAM_ACTION                           = SQL_PARAM_ACTION;
    
    public static final String KEY_CLOUD_PARAM_USER_KEY                     = "userkey";
    public static final String KEY_CLOUD_PARAM_ACCESSID                     = "accessid";
    public static final String KEY_CLOUD_PARAM_SECRETKEY                    = "secretkey";
    public static final String KEY_CLOUD_PARAM_BUCKETNAME                   = "bucketname";
    public static final String KEY_CLOUD_PARAM_OSSTYPE                      = "osstype";
    public static final String KEY_CLOUD_PARAM_OSSLOCAL                     = "osslocal";
}

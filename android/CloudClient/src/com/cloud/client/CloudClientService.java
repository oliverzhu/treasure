package com.cloud.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.core.FileNameGenerator;
import net.tsz.afinal.db.table.Property;
import net.tsz.afinal.db.table.TableInfo;
import net.tsz.afinal.http.AjaxParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cloud.client.Transmitter.TransResponse;
import com.cloud.client.file.AliOSSClient;
import com.cloud.client.file.AmazonClient;
import com.cloud.client.file.CloudFileResult;
import com.cloud.client.file.Credentials;
import com.cloud.client.file.Credentials.OSSType;
import com.cloud.client.file.FileNameUtil;
import com.cloud.client.file.IFileClient;
import com.cloud.client.file.MissionObject;
import com.cloud.client.file.MissionListener;
import com.cloud.client.file.database.DatabaseAccessManager;
import com.cloud.client.sql.ISqlGenerate;
import com.cloud.client.sql.SqlGenerate;
import com.cloud.client.sql.SqlResultListener;
import com.cloud.client.sql.SqlResultMultiListener;
import com.cloud.client.sql.SqlResultSingleListener;
import com.cloud.client.sql.SqlSyncResultSet.SqlSyncMultiResult;
import com.cloud.client.sql.SqlSyncResultSet.SqlSyncResult;
import com.cloud.client.sql.SqlSyncResultSet.SqlSyncSingleResult;
import com.cloud.util.DESPlus;
import com.cloud.util.NetUtils;
import com.cloud.util.Preferences;
import com.cloud.util.Utils;

public abstract class CloudClientService {

    private static final String TAG = "CloudClientService";
    private Context mContext;
    private static boolean init = false;
    private String mOwnerAppKey;
    private String mAskerAppKey;
    private static FinalHttp mFinalHttp;
    private static IFileClient mFileClient;
    private static AliOSSClient mAliOSSClient;
    private static AmazonClient mAmazonClient;
    private ISqlGenerate mSqlGenerate;
    private CloudParam mCloudParam;
    public SharedPreferences mPrefs;
    private static final String PREFIX_DIR = "";
    private String mPackageName;
    private FileNameUtil mFileNameUtil;
    protected String mUserId;

    public CloudClientService(Context context,String askerAppKey) {
        mContext = context;
        /*if (!init) */{
            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
            mAskerAppKey = askerAppKey;
            mOwnerAppKey = initOwnerKey();
            mPackageName = initPackageName();
            mFinalHttp = new FinalHttp();
            mSqlGenerate = new SqlGenerate(this, initPackageName());
            mCloudParam = new CloudParam();
            mFileNameUtil = new FileNameUtil(PREFIX_DIR, mPackageName);
            init = true;
        }
    }

    protected abstract String initOwnerKey();
    
    protected abstract String initPackageName();

    /**
     * 创建文件服务器操作对象
     * 
     * @param credentials
     * @return
     */
    public int createCloudFileService(Credentials credentials) {
        DESPlus desPlus2;
        Credentials decryptCredentials = null;
        int result = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
        
        try {
            desPlus2 = new DESPlus("@lzhlmcl,1hb6u7s?");
            decryptCredentials = new Credentials(
                    desPlus2.decrypt(credentials.getAccessId()),
                    desPlus2.decrypt(credentials.getSecretKey()), 
                    desPlus2.decrypt(credentials.getBucketName()), 
                    credentials.getOSSType(), credentials.getOSSLocal());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (decryptCredentials == null) {
            Log.e(TAG, "decrypt server infomation error~~~");
            return CloudUtil.CLOUDCLIENT_DECRYPT_SERVER_ERROR;
        }
        
        if (credentials.getOSSType().equals(OSSType.ALIYUN)) {
            if (mAliOSSClient == null) {
                mAliOSSClient = new AliOSSClient(decryptCredentials, mFileNameUtil, 
                        new DatabaseAccessManager(mContext));
            } else if (!mAliOSSClient.getAccessId().equals(decryptCredentials.getAccessId()) || 
                       !mAliOSSClient.getSecretKey().equals(decryptCredentials.getSecretKey()) ||
                       !mAliOSSClient.getBuckerName().equals(decryptCredentials.getBucketName())) {
                mAliOSSClient = new AliOSSClient(decryptCredentials, mFileNameUtil, 
                        new DatabaseAccessManager(mContext));
            }
            mFileClient = mAliOSSClient;
        } else if (credentials.getOSSType().equals(OSSType.AMAZON)) {
            if (mAmazonClient == null) {
                mAmazonClient = new AmazonClient(decryptCredentials, mFileNameUtil,
                        new DatabaseAccessManager(mContext));
            } else if (!mAmazonClient.getAccessId().equals(decryptCredentials.getAccessId()) || 
                       !mAmazonClient.getSecretKey().equals(decryptCredentials.getSecretKey()) ||
                       !mAmazonClient.getBuckerName().equals(decryptCredentials.getBucketName())) {
                mAmazonClient = new AmazonClient(decryptCredentials, mFileNameUtil,
                        new DatabaseAccessManager(mContext));
            }
            mFileClient = mAmazonClient;
        }
        
        if (mFileClient != null) {
            result = CloudUtil.CLOUDCLIENT_RESULT_OK;
        } else {
            result = CloudUtil.CLOUDCLIENT_CREATE_FILECLIENT_ERROR;
        }
        return result;
    }

    /**
     * 通过{@link userInfo}为用户分配合适的文件服务器操作对象
     * 
     * @param userInfo
     */
    public int allocFileClient(Map userInfo) {
        String userKey = (String)userInfo.get(CloudUtil.KEY_CLOUD_PARAM_USER_KEY);
        String accessId = (String)userInfo.get(CloudUtil.KEY_CLOUD_PARAM_ACCESSID);
        String secretKey = (String)userInfo.get(CloudUtil.KEY_CLOUD_PARAM_SECRETKEY);
        String bucketName = (String)userInfo.get(CloudUtil.KEY_CLOUD_PARAM_BUCKETNAME);
        String ossType = (String)userInfo.get(CloudUtil.KEY_CLOUD_PARAM_OSSTYPE);
        String ossLocal = (String)userInfo.get(CloudUtil.KEY_CLOUD_PARAM_OSSLOCAL);
        
        if (!checkUser(userKey)) {
            return CloudUtil.CLOUDCLIENT_USER_ID_ERROR;
        }
        this.mUserId = userKey;
        
        if (accessId == null || accessId.trim().length() == 0 ) {
            Log.e(TAG, "accessId is empty");
            return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
        }
        
        if (secretKey == null || secretKey.trim().length() == 0) {
            Log.e(TAG, "secretKey is empty");
            return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
        }
        
        if (bucketName == null || bucketName.trim().length() == 0) {
            Log.e(TAG, "bucketName is empty");
            return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
        }
        Credentials credentials = 
                new Credentials(accessId, secretKey, bucketName, ossType, ossLocal);
        
        return createCloudFileService(credentials);
    }
    
    public class CloudParam{
        public CloudParam() {
            
        }
        public String providerKey;
        public String providerPackageName;
        public String providerTime;
        public String userKey;
        public String userPackageName;
        public String userTime;
        public String version;
    }

    public CloudParam getAuthorizeParms() {
        String currentTime = String.valueOf(System.currentTimeMillis());
        mCloudParam.providerKey = FileNameGenerator.generator(mOwnerAppKey + " %sw-service%" + currentTime);
        mCloudParam.providerPackageName = initPackageName();
        mCloudParam.providerTime = currentTime;
        mCloudParam.userKey = FileNameGenerator.generator(mAskerAppKey + " %sw-service%" + currentTime);
        mCloudParam.userPackageName = mContext.getPackageName();
        mCloudParam.userTime = currentTime;
        mCloudParam.version = CloudUtil.SQL_VERSION_NO;//String.valueOf(ContextUtils.getVersionCode(mContext));
        return mCloudParam;
    }
    
    public boolean authorize() {
        if(mPrefs == null) {
            return false;
        }
        if(Preferences.getAuthorizeState(mPrefs)) {
            long lastAuthorizeTime = Preferences.getAuthorizeTime(mPrefs);
            long currenTime = System.currentTimeMillis();
            long deltaTime = currenTime - lastAuthorizeTime;
            if(deltaTime <= 30 * 60 * 1000 && deltaTime >= 0) {
                return true;
            }
        }
        boolean isValid = false;
        ArrayList<String> paramList = new ArrayList<String>();
        CloudParam param = getAuthorizeParms();
        paramList.add(param.providerKey);
        paramList.add(param.providerPackageName);
        paramList.add(param.providerTime);
        paramList.add(param.userKey);
        paramList.add(param.userPackageName);
        paramList.add(param.userTime);
        JSONObject jsonObject = 
                NetUtils.getJSONArrayByGet(Utils.combinaStr(CloudUtil.URL_PREFIX + CloudUtil.URL_ANTHORIZE, paramList));
        if(jsonObject == null) {
            isValid = false;
        } else {
            try {
                isValid = jsonObject.getBoolean("authorize");
            } catch (JSONException e) {
                isValid = false;
                e.printStackTrace();
            } finally {
                Preferences.setAuthorizeTime(mPrefs, Long.valueOf(param.providerTime));
                Preferences.setAuthorizeState(mPrefs, isValid);
            }
        }
        Preferences.setAuthorizeTime(mPrefs, Long.valueOf(param.providerTime));
        Preferences.setAuthorizeState(mPrefs, isValid);
        return isValid;
    }

    final protected <T extends CloudObject> void create(Class<T> clazz,
            final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.createTable(clazz);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }

    final protected <T extends CloudObject> void insert(final T entity,
            final SqlResultSingleListener<T> listener) {
        final Class<?> clazz = entity.getClass(); // use to create table if not exist
//        final SqlResultSingleListener<T> resultListener = listener; // listen the insert result
        final AjaxParams insertParams = mSqlGenerate.insertObject(entity); // use to insert again
        if (insertParams != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    // if the table is exist and insert success
                    if (listener != null) {
                        entity.setId((Long) object);
                        listener.onSuccess(entity, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (errorNo == CloudUtil.CLOUDCLIENT_SQL_TABLE_NO_EXIST) {
                        // if the table is not exist, create it first and insert later
                        AjaxParams createParams = mSqlGenerate.createTable(clazz);
                        if (createParams != null) {
                            new Transmitter(mFinalHttp, new TransmitterListener() {

                                @Override
                                public void onSuccess(Object object, String msg) {
                                    // create table success and insert again
                                    new Transmitter(mFinalHttp, new TransmitterListener() {
                                        
                                        @Override
                                        public void onSuccess(Object object, String msg) {
                                            if (listener != null) {
                                                entity.setId((Long) object);
                                                listener.onSuccess(entity, msg);
                                            }
                                        }
                                        
                                        @Override
                                        public void onFailure(int errorNo, String msg) {
                                            if (listener != null) {
                                                listener.onFailure(errorNo, msg);
                                            }
                                        }
                                    }).postSql(insertParams); // create success and insert again
                                }

                                @Override
                                public void onFailure(int errorNo, String msg) {
                                    // create table failed
                                    if (listener != null) {
                                        listener.onFailure(errorNo, msg);
                                    }
                                }
                            }).postSql(createParams); // insert failed and create table
                        } // if (createParams != null)
                    } else {// if (errorNo == CloudState.CLOUDCLIENT_SQL_TABLE_NO_EXIST)
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(insertParams); // first insert, if table not exist to create it
        } // if (insertParams != null)
    }

    final protected <T extends CloudObject> void update(T entity,
            final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.updateObject(entity);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }

    final protected <T extends CloudObject> void updateSpecifiedField (T entity,
            final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.updateObjectSpecifiedField(entity);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected void updateCustom(String sql, String tableName,
            final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.updateCustom(sql, tableName);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void get(final Class<T> clazz,
            final SqlResultMultiListener<T> listener) {
        AjaxParams params = mSqlGenerate.getAll(clazz);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        List<T> list = generateData(clazz, object);
                        listener.onSuccess(list, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }

    final protected <T extends CloudObject> void get(final Class<T> clazz, 
            String orderBy, final SqlResultMultiListener<T> listener) {
        AjaxParams params = mSqlGenerate.getAll(clazz, orderBy);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        List<T> list = generateData(clazz, object);
                        listener.onSuccess(list, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }

    final protected <T extends CloudObject> void getById(final Class<T> clazz,
            Object idValue, final SqlResultMultiListener<T> listener) {
        AjaxParams params = mSqlGenerate.getById(clazz, idValue);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        List<T> list = generateData(clazz, object);
                        listener.onSuccess(list, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }

    final protected <T extends CloudObject> void getByWhere(final Class<T> clazz,
            String where, final SqlResultMultiListener<T> listener) {
        AjaxParams params = mSqlGenerate.getByWhere(clazz, where);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        List<T> list = generateData(clazz, object);
                        listener.onSuccess(list, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void getByCustomSql(final Class<T> clazz,
            String sql,  String tableName, final SqlResultMultiListener<T> listener) {
        AjaxParams params = mSqlGenerate.getByCustomSql(sql, tableName);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        List<T> list = generateData(clazz, object);
                        listener.onSuccess(list, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void delete(T entity,
            final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.deleteObject(entity);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }

    final protected <T extends CloudObject> void delete(final Class<T> clazz, 
            String where, final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.deleteObject(clazz, where);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected void deleteCustom(String sql,  String tableName,
            String where, final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.deleteCustom(sql, tableName);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void drop(final Class<T> clazz, 
            final SqlResultListener listener) {
        AjaxParams params = mSqlGenerate.dropTable(clazz);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        listener.onSuccess(msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void insertOrUpdate(final T entity,
            String where, final SqlResultSingleListener<T> listener) {
        AjaxParams params = mSqlGenerate.insertOrUpdate(entity, where);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        entity.setId((Long) object);
                        listener.onSuccess(entity, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void selectOrUpdate(final T entity,
            String where, final SqlResultSingleListener<T>  listener) {
        AjaxParams params = mSqlGenerate.selectOrUpdate(entity, where);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        entity.setId((Long) object);
                        listener.onSuccess(entity, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final protected <T extends CloudObject> void selectOrInsert(final T entity,
            String where, final SqlResultSingleListener<T>  listener) {
        AjaxParams params = mSqlGenerate.selectOrInsert(entity, where);
        if (params != null) {
            new Transmitter(mFinalHttp, new TransmitterListener() {
                
                @Override
                public void onSuccess(Object object, String msg) {
                    if (listener != null) {
                        entity.setId((Long) object);
                        listener.onSuccess(entity, msg);
                    }
                }
                
                @Override
                public void onFailure(int errorNo, String msg) {
                    if (listener != null) {
                        listener.onFailure(errorNo, msg);
                    }
                }
            }).postSql(params);
        }
    }
    
    final public <T extends CloudObject> String getTableName(Class<T> clazz) {
        return mSqlGenerate.getTableName(clazz);
    }
    
    final public void sendEmail(String address, String title, String body,
            final SqlResultListener listener) {
        
        AjaxParams params = getEmailParams(address, title, body);
        
        new Transmitter(mFinalHttp, new TransmitterListener() {
            
            @Override
            public void onSuccess(Object object, String msg) {
                if (listener != null) {
                    listener.onSuccess(msg);
                }
            }
            
            @Override
            public void onFailure(int errorNo, String msg) {
                if (listener != null) {
                    listener.onFailure(errorNo, msg);
                }
            }
        }).postEmail(params);
    }
    
    private AjaxParams getEmailParams(String address, String title, String body) {
        CloudParam mCloudParam = getAuthorizeParms();
        AjaxParams params = new AjaxParams();
        params.put(CloudUtil.EMAIL_PARAM_ACTION, String.valueOf(CloudUtil.SQL_ACTION_SEND_EMAIL));
        params.put(CloudUtil.EMAIL_PARAM_ADDRESS, address);
        params.put(CloudUtil.EMAIL_PARAM_TITLE, title);
        params.put(CloudUtil.EMAIL_PARAM_BODY, body);
        params.put(CloudUtil.EMAIL_PARAM_VERSION, CloudUtil.CLIENT_VERSION);
        params.put(CloudUtil.SQL_PARAM_PROVIDER_KEY_PLUS, mCloudParam.providerKey);
        params.put(CloudUtil.SQL_PARAM_PROVIDER_APK_NAME, mCloudParam.providerPackageName);
        params.put(CloudUtil.SQL_PARAM_PROVIDER_CLIENT_TIME, mCloudParam.providerTime);
        params.put(CloudUtil.SQL_PARAM_USER_KEY_PLUS, mCloudParam.userKey);
        params.put(CloudUtil.SQL_PARAM_USER_APK_NAME, mCloudParam.userPackageName);
        params.put(CloudUtil.SQL_PARAM_USER_CLIENT_TIME, mCloudParam.userTime);
        params.put(CloudUtil.SQL_PARAM_VERSION_NO, mCloudParam.version);
        
        return params;
    }
    
    private <T extends CloudObject> List<T> generateData(Class<T> clazz, Object object) {
        List<T> resultList = new ArrayList<T>();
        try {
            JSONArray data = (JSONArray)object;
            if (data.length() <= 0) {
                return resultList;
            }
            JSONArray name = data.getJSONObject(0).names();
            TableInfo table = TableInfo.get(clazz);
            for (int i = 0; i < data.length(); i ++) {
                T entity = (T) clazz.newInstance();
                for (int j = 0; j < name.length(); j++) {
                    String fieldName = name.get(j).toString();
                    Property property = table.propertyMap.get(fieldName);
                    if(property!=null){
                        property.setValue(entity, data.getJSONObject(i).get(fieldName));
                    }
                    else{
                        if(table.getId().getColumn().equals(fieldName)){
                            entity.setId(data.getJSONObject(i).getLong(fieldName));
                        }
                    }
                }
                resultList.add(entity);
            }
        } catch (JSONException e) {
            Log.i(TAG, e.getMessage());
        } catch (InstantiationException e) {
            Log.i(TAG, e.getMessage());
        } catch (IllegalAccessException e) {
            Log.i(TAG, e.getMessage());
        }
        return resultList;
    }
    
    /**
     * 创建Bucket
     * 
     * @return {@link CloudFileResult}
     */
    final protected CloudFileResult createBucket() {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        return mFileClient.createBucket();
    }

    /**
     * 非递归从服务器上获取指定目录下文件列表
     * 
     * @param key 所要获取的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult listDirectory(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        return mFileClient.listDirectory(mUserId, key);
    }
    
    /**
     * 递归获取指定目录下文件列表
     * 
     * @param key 所要获取的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult getDirectoryList(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        return mFileClient.getDirectoryList(mUserId, key);

    }
    
    /**
     * 获取目录大小
     * 
     * @param key 所要获取的目录
     * @return 成功返回目录大小，失败返回错误码
     */
    public long getDirectorySize(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        if (key == null) {
            return CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT;
        }
        
        return mFileClient.getDirectorySize(mUserId, key);
    }
    
    /**
     * 上传指定文件到服务器指定目录下
     * 
     * @param userId 用户Id
     * @param filePath 本地文件
     * @param contentType 文件类型
     * @param key 用户{userId}将文件{filePath}保存到服务器的目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
//    final protected CloudFileResult updateFile(String userId, String filePath, 
//            String contentType, String key, MissionListener listener) {
//        if (mFileClient == null) {
//            throw new RuntimeException("create fileclient entity first");
//        }
//        
//        CloudFileResult result = new CloudFileResult();
//        if (key == null) {
//            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
//            result.setMessage("the key must be set!");
//            return result;
//        }
//        
//        if (filePath == null || filePath.equals("")) {
//            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
//            result.setMessage("the file is null!");
//            return result;
//        }
//        
//        try {
//            int resultCode = mFileClient.uploadFile(userId, filePath, contentType, key, listener);
//            result.setResultCode(resultCode);
//        } catch (OSSException osse) {
//            parserException(result, osse);
//            return result;
//        }
//        
//        return result;
//    }
    
    /**
     * 上传指定文件到服务器指定目录下
     * 
     * @param userId 用户Id
     * @param filePath 本地文件
     * @param key 用户{userId}将文件{filePath}保存到服务器的目录
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
//    final protected CloudFileResult updateFile(String userId, String filePath, String key,
//            MissionListener listener) {
//        if (mFileClient == null) {
//            throw new RuntimeException("create fileclient entity first");
//        }
//        
//        CloudFileResult result = new CloudFileResult();
//        if (key == null) {
//            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
//            result.setMessage("the key must be set!");
//            return result;
//        }
//        
//        if (filePath == null || filePath.equals("")) {
//            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
//            result.setMessage("the file is null!");
//            return result;
//        }
//        
//        try {
//            int resultCode = mFileClient.uploadFile(userId, filePath, key, listener);
//            result.setResultCode(resultCode);
//        } catch (OSSException osse) {
//            parserException(result, osse);
//            return result;
//        }
//        return result;
//    }
    
    /**
     * 初始化分块上传任务
     * 
     * @param filePath 本地文件
     * @param key 用户{userId}将文件列表{fileList}保存到服务器的目录
     * @return {@link MissionObject}
     */
    public MissionObject initMultipartUpload(String filePath, String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        return mFileClient.initMultipartUpload(mUserId, filePath, key);
    }
    /**
     * 执行分块上传任务
     * 
     * @param missionObject {@link MissionObject}
     * @param listener {@link MissionListener}
     * @return {@link CloudFileResult}
     */
    public CloudFileResult multipartUpload(MissionObject missionObject,
            MissionListener listener) {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (missionObject == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the missionObject is null!");
            return result;
        }
        
        return mFileClient.multipartUpload(missionObject, listener);
    }

    /**
     * 获取用户未完成的分块上传任务
     * 1.未完成且可以继续的任务存放在{@link CloudFileResult.missionList}中。
     * 2.未完成且无法继续的任务存放在{@link CloudFileResult.unKnownMission}中，
     *  建议使用{@link deleteUploadMission()}或{@link deleteUploadMissionbyUploadId}删除。
     *  
     * @return {@link CloudFileResult}
     */
    public CloudFileResult listMultipartUploads() {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        return mFileClient.listmultipartUploads(mUserId);
    }
    
    /**
     * 初始化下载任务
     * 1.对于新创建的下载任务，可以通过overwrite选择是否覆盖本地已存在的文件
     * 2.对于已完成的任务，并且任务存在于下载列表中，可以通过overwrite选择是否覆盖本地已存在文件
     * 3.参数overwrite不影响断点续传
     * 
     * @param key 用户{userId}在服务器上需要下载的文件
     * @param localFile 本地存储位置
     * @param overwrite 是否覆盖本地已存在的文件
     * @return {@link MissionObject}
     */
    public MissionObject initDownload(String key,
            String localFile, boolean overwrite) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        return mFileClient.initDownload(mUserId, key, localFile, overwrite);
    }
    
    /**
     * 执行下载任务
     * 
     * @param {{@link MissionObject}
     * @param listener {@link MissionListener}
     * @param overwrite 是否覆盖本地已存在的文件
     * @return {@link CloudFileResult}
     */
    public CloudFileResult downloadFile(MissionObject missionObject,
            MissionListener listener) {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (missionObject == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the missionObject is null!");
            return result;
        }
        return mFileClient.downloadFile(missionObject, listener);
    }
    
    /**
     * 删除服务器上指定文件，也可删除空目录
     * 
     * @param key 用户在服务器上需要删除的文件或目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult deleteFile(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        return mFileClient.deleteFile(mUserId, key);
    }
    
    /**
     * 创建目录
     * 
     * @param key 用户在服务器上所要创建的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult createDir(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        return mFileClient.createDir(mUserId, key);
    }
    
    /**
     * 移动文件
     * 
     * @param key 用户在服务器上所要移动的源文件
     * @param dir 目标目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult moveFile(String key, String dir) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        if (dir == null || dir.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the directory is null!");
            return result;
        }
        
        if (key.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_FILE);
            result.setMessage("the source not a file!");
            return result;
        }
        
        if (!dir.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the target not directory!");
            return result;
        }
        
        String sourceFileName = FileNameUtil.parseName(key);
        String targetKey = dir + sourceFileName;
        
        return mFileClient.moveFile(mUserId, key, targetKey);
    }
    
    /**
     * 文件重命名
     * 
     * @param key 用户在服务器上所要重命名的文件
     * @param newName 新名称
     * @return {@link CloudFileResult}
     */
    public CloudFileResult renameFile(String key, String newName) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        if (newName == null || newName.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the new name is null!");
            return result;
        }
        
        if (key.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_FILE);
            result.setMessage("the source not a file!");
            return result;
        }
        
        String sourcePath = FileNameUtil.parseParentPath(key);
        String targetKey = sourcePath + newName;
        
        return mFileClient.moveFile(mUserId, key, targetKey);
    }
    
    /**
     * 复制文件
     * 
     * @param key 用户{userId}在服务器上所要复制的源文件
     * @param dir 目标目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult copyFile(String key, String dir) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        CloudFileResult result = new CloudFileResult();
        if (key == null) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the key must be set!");
            return result;
        }
        
        if (dir == null || dir.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the directory is null!");
            return result;
        }
        
        if (key.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_FILE);
            result.setMessage("the source not a file!");
            return result;
        }
        
        if (!dir.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the target not directory!");
            return result;
        }
        
        String sourceFileName = FileNameUtil.parseName(key);
        String targetKey = dir + sourceFileName;
        
        return mFileClient.copyFile(mUserId, key, targetKey);
    }
    
    /**
     * 移动文件夹
     * 
     * @param srcPath 用户在服务器上所要移动的源文件夹
     * @param destPath 目标目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult moveFolder(String srcPath, String destPath) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (srcPath == null || srcPath.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the source directory must be set!");
            return result;
        }
        
        if (destPath == null || destPath.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the target directory must be set!");
            return result;
        }
        
        if (!srcPath.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the source not directory!");
            return result;
        }
        
        if (!destPath.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the target not directory!");
            return result;
        }
        
        return mFileClient.moveFolder(mUserId, srcPath, destPath);
    }
    
    /**
     * 重命名文件夹
     * 
     * @param srcPath 用户在服务器上所要重命名的源文件夹
     * @param newName 新名称
     * @return {@link CloudFileResult}
     */
    public CloudFileResult renameFolder(String srcPath, String newName) {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (srcPath == null || srcPath.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the source directory must be set!");
            return result;
        }
        
        if (newName == null || newName.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the new name is null!");
            return result;
        }
        
        if (!srcPath.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the source not directory!");
            return result;
        }
        
        String parentPath = FileNameUtil.parseParentPath(srcPath);
        String destPath = parentPath + newName;
        
        return moveFolder(srcPath, destPath);
    }
    
    /**
     * 复制文件夹
     * 
     * @param srcPath 用户在服务器上所要复制的源文件夹
     * @param destPath 目标目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult copyFolder(String srcPath, String destPath) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        CloudFileResult result = new CloudFileResult();
        if (srcPath == null || srcPath.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the source directory must be set!");
            return result;
        }
        
        if (destPath == null || destPath.equals("")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_ILLEGAL_ARGUMENT);
            result.setMessage("the target directory must be set!");
            return result;
        }
        
        if (!srcPath.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the source not directory!");
            return result;
        }
        
        if (!destPath.endsWith("/")) {
            result.setResultCode(CloudUtil.CLOUD_FILE_NOT_DIR);
            result.setMessage("the target not directory!");
            return result;
        }
        
        return mFileClient.copyFolder(mUserId, srcPath, destPath);
    }
    
    /**
     * 递归的删除服务器上指定目录
     * 
     * @param key 用户在服务器上需要删除的目录
     * @return {@link CloudFileResult}
     */
    public CloudFileResult deleteDir(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        return mFileClient.deleteDirectory(mUserId, key);
    }
    
    /**
     * 获取上传任务列表，包含成功任务与失败任务，普通上传任务与分块上传任务
     * 
     * @return {@link MissionObject}列表
     */
    public List<MissionObject> listUploadMissionObject() {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        return mFileClient.getUploadList(mUserId);
    }
    
    /**
     * 获取下载任务列表，包含成功任务与失败任务
     * 
     * @return {@link MissionObject}列表
     */
    public List<MissionObject> listDownloadMissionObject() {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        return mFileClient.getDownloadList(mUserId);
    }
    
    /**
     * 删除指定上传任务
     * 
     * @param key 上传任务{@link MissionObject.getKey()}
     */
    public void deleteUploadMission(String key) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteUploadMission(mUserId, key);
    }
    
    /**
     * 删除指定上传任务
     * 
     * @param uploadId 上传任务{@link MissionObject.getUploadId()}
     */
    public void deleteUploadMissionbyUploadId(String uploadId) {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteUploadMissionbyUploadId(uploadId);
    }
    
    /**
     * 删除指定指定用户所有上传任务
     * 
     */
    public void deleteAllUploadMission() {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteUploadMission(mUserId);
    }
    
    /**
     * 删除指定上传任务
     * 
     * @param missionObject {@link MissionObject}
     */
    public void deleteUploadMission(MissionObject missionObject) {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteUploadMission(missionObject);
    }
    
    /**
     * 删除指定下载任务
     * 
     * @param key 下载任务{@link MissionObject.getKey()}
     * @param flag 是否删除本地文件
     */
    public void deleteDownloadMission(String key, boolean flag) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteDownloadMission(mUserId, key, flag);
    }
    
    /**
     * 删除指定指定用户所有下载任务
     * 
     * @param flag 是否删除本地文件
     */
    public void deleteDownloadMission(boolean flag) {
        if (mUserId == null) {
            throw new RuntimeException("userId must be set"); 
        }
        
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteDownloadMission(mUserId, flag);
    }
    
    /**
     * 删除指定下载任务
     * 
     * @param missionObject {@link MissionObject}
     * @param flag 是否删除本地文件
     */
    public void deleteDownloadMission(MissionObject missionObject, boolean flag) {
        if (mFileClient == null) {
            throw new RuntimeException("create fileclient entity first");
        }
        
        mFileClient.deleteDownloadMission(missionObject, flag);
    }
    
    private boolean checkUser(String userId) {
        if (userId == null || userId.trim().length() == 0 || "-1".equals(userId)) {
            return false;
        }
        return true;
    }
    
    /************************************************
     * For Sync Operation
     ************************************************/
    final protected <T extends CloudObject> SqlSyncResult create(Class<?> clazz) {
        return handleSqlSyncResult(mSqlGenerate.createTable(clazz));
    }
    
    private static boolean isCheckTableExist = false;  // 确保只创建一次数据表,防止死循环问题
    final protected <T extends CloudObject> SqlSyncSingleResult<T> insert(T entity) {
        SqlSyncSingleResult<T> result = new SqlSyncSingleResult<T>();
        TransResponse response = null;
        
        Class<?> clazz = entity.getClass();
        AjaxParams params = mSqlGenerate.insertObject(entity);
        if (params != null) {
            Transmitter transmitter = new Transmitter(mFinalHttp, null);
            response = transmitter.postSqlSync(params);
            if (response != null) {
                result.setCode(response.code);
                result.setMessage(response.message);
                if (response.code == CloudUtil.CLOUDCLIENT_RESULT_OK &&
                        response.object != null) {
                    // 记录写入数据库成功,并返回index
                    entity.setId(Long.valueOf((String) response.object));
                    result.setEntity(entity);
                } else if (response.code == CloudUtil.CLOUDCLIENT_SQL_TABLE_NO_EXIST) {
                    // 所操作的表不存在,创建数据表
                    if (isCheckTableExist) {
                        isCheckTableExist = false;
                        result.setMessage("create table failure!!");
                        return result;
                    } else {
                        isCheckTableExist = true;
                    }
                    SqlSyncResult createResult = create(clazz);
                    if (createResult.getCode() == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                        //数据表创建成功,再次执行插入操作
                        result = insert(entity);
                    } else {
                        // 数据表创建失败
                        result.setCode(response.code);
                        result.setMessage("table not exist and create failure, " + createResult.getMessage());
                    }
                }
            }
        }
        return result;
    }
    
    final protected <T extends CloudObject> SqlSyncResult update(T entity) {
        return handleSqlSyncResult(mSqlGenerate.updateObject(entity));
    }
    
    final protected <T extends CloudObject> SqlSyncResult updateSpecifiedField(T entity) {
        return handleSqlSyncResult(mSqlGenerate.updateObjectSpecifiedField(entity));
    }
    
    final protected SqlSyncResult updateCustom(String sql, String tableName) {
        return handleSqlSyncResult(mSqlGenerate.updateCustom(sql, tableName));
    }
    
    final protected <T extends CloudObject> SqlSyncMultiResult<T> get(Class<T> clazz) {
        return handleSqlSyncMultiResult(clazz, mSqlGenerate.getAll(clazz));
    }
    
    final protected <T extends CloudObject> SqlSyncMultiResult<T> get(Class<T> clazz, String orderBy) {
        return handleSqlSyncMultiResult(clazz, mSqlGenerate.getAll(clazz, orderBy));
    }
    
    final protected <T extends CloudObject> SqlSyncMultiResult<T> getById(Class<T> clazz, Object idValue) {
        return handleSqlSyncMultiResult(clazz, mSqlGenerate.getById(clazz, idValue));
    }
    
    final protected <T extends CloudObject> SqlSyncMultiResult<T> getByWhere(Class<T> clazz, String where) {
        return handleSqlSyncMultiResult(clazz, mSqlGenerate.getByWhere(clazz, where));
    }
    
    final protected <T extends CloudObject> SqlSyncMultiResult<T> getByCustomSql(Class<T> clazz,
            String sql, String tableName) {
        return handleSqlSyncMultiResult(clazz, mSqlGenerate.getByCustomSql(sql, tableName));
    }
    
    final protected <T extends CloudObject> SqlSyncResult delete(T entity) {
        return handleSqlSyncResult(mSqlGenerate.deleteObject(entity));
    }
    
    final protected <T extends CloudObject> SqlSyncResult delete(Class<T> clazz, String where) {
        return handleSqlSyncResult(mSqlGenerate.deleteObject(clazz, where));
    }
    
    final protected <T extends CloudObject> SqlSyncResult deletecustom(String sql, String tableName) {
        return handleSqlSyncResult(mSqlGenerate.deleteCustom(sql, tableName));
    }
    
    final protected <T extends CloudObject> SqlSyncResult drop(Class<T> clazz) {
        return handleSqlSyncResult(mSqlGenerate.dropTable(clazz));
    }
    
    final protected <T extends CloudObject> Object insertOrUpdate(T entity, String where) {
        return handleSqlSyncSingleResult(entity, mSqlGenerate.insertOrUpdate(entity, where));
    }
    
    final protected <T extends CloudObject> Object selectOrUpdate(T entity, String where) {
        return handleSqlSyncSingleResult(entity, mSqlGenerate.selectOrUpdate(entity, where));
    }
    
    final protected <T extends CloudObject> Object selectOrInsert(T entity, String where) {
        return handleSqlSyncSingleResult(entity, mSqlGenerate.selectOrInsert(entity, where));
    }
    
    final public SqlSyncResult sendEmail(String address, String title, String body) {
        SqlSyncResult result = new SqlSyncResult();
        TransResponse response = null;
        
        Transmitter transmitter = new Transmitter(mFinalHttp, null);
        response = transmitter.postEmailSync(getEmailParams(address, title, body));
        if (response != null) {
            result.setCode(response.code);
            result.setMessage(response.message);
        }
        
        return result;
    }
    
    private <T extends CloudObject> SqlSyncMultiResult<T> handleSqlSyncMultiResult(
            Class<T> clazz, AjaxParams params) {
        SqlSyncMultiResult<T> result = new SqlSyncMultiResult<T>();
        TransResponse response = null;
        
        if (params != null) {
            Transmitter transmitter = new Transmitter(mFinalHttp, null);
            response = transmitter.postSqlSync(params);
            if (response != null) {
                result.setCode(response.code);
                result.setMessage(response.message);
                if (response.code == CloudUtil.CLOUDCLIENT_RESULT_OK &&
                        response.object != null) {
                    List<T> list = generateData(clazz, response.object);
                    result.setEntity(list);
                }
            }
        }
        return result;
    }
    
    private  <T extends CloudObject> SqlSyncSingleResult<T> handleSqlSyncSingleResult(
            T entity, AjaxParams params) {
        SqlSyncSingleResult<T> result = new SqlSyncSingleResult<T>();
        TransResponse response = null;
        
        if (params != null) {
            Transmitter transmitter = new Transmitter(mFinalHttp, null);
            response = transmitter.postSqlSync(params);
            if (response != null) {
                result.setCode(response.code);
                result.setMessage(response.message);
                if (response.code == CloudUtil.CLOUDCLIENT_RESULT_OK &&
                        response.object != null) {
                    entity.setId(Long.valueOf((String) response.object));
                    result.setEntity(entity);
                }
            }
        }
        
        return result;
    }
    
    private SqlSyncResult handleSqlSyncResult(AjaxParams params) {
        SqlSyncResult result = new SqlSyncResult();
        TransResponse response = null;
        
        if (params != null) {
            Transmitter transmitter = new Transmitter(mFinalHttp, null);
            response = transmitter.postSqlSync(params);
            if (response != null) {
                result.setCode(response.code);
                result.setMessage(response.message);
            }
        }
        return result;
    }
}

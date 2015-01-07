package com.cloud.client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cloud.client.CloudUtil;
import com.cloud.client.sql.SQLResultParser;
import com.cloud.util.log.LogTrans;
import com.cloud.util.log.LogTrans.ActionMode;

import android.os.NetworkOnMainThreadException;
import android.util.Log;
import net.tsz.afinal.FinalHttp;
import net.tsz.afinal.http.AjaxCallBack;
import net.tsz.afinal.http.AjaxParams;

class Transmitter{
    private static final String TAG = "Transmitter";

    private FinalHttp mFinalHttp = null;
    private TransmitterListener mListener = null;
    private LogTrans mLogTrans;
    
    public Transmitter(FinalHttp finalHttp, TransmitterListener listener) {
        this.mFinalHttp = finalHttp;
        this.mListener = listener;
        mLogTrans = new LogTrans(TAG);
    }

    public void postSql(AjaxParams params) {

        int action = Integer.valueOf(params.get(CloudUtil.SQL_PARAM_ACTION));
        StringBuffer msg = new StringBuffer();
        if (action >= CloudUtil.SQL_ACTION_INSERTORUPDATE && 
                action <= CloudUtil.SQL_ACTION_SELECTORINSERT) {
            msg.append("\n" + params.get(CloudUtil.SQL_PARAM_SQL_PART_INSERT))
            .append("\n" + params.get(CloudUtil.SQL_PARAM_SQL_PART_UPDATE))
            .append("\n" + params.get(CloudUtil.SQL_PARAM_SQL_PART_DELETE));
        }
        mLogTrans.d(ActionMode.Request, CloudUtil.CLOUDCLIENT_RESULT_OK, action, params,
                "SQL:" + params.get(CloudUtil.SQL_PARAM_SQL) + msg.toString());
        
        final AjaxParams params_tmp = params;
        mFinalHttp.post(CloudUtil.POST_URL, params, new AjaxCallBack<Object>() {
            
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                if (errorNo == 0) {
                    errorNo = CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR;
                    strMsg = "unknown server error~~~";
                }
                mLogTrans.i("post sql error! errorNo:%d, message:%s, Throwable:%s", errorNo, strMsg, t.getMessage());
                mListener.onFailure(errorNo, strMsg);
            }

            @Override
            public void onSuccess(Object t) {
                try {
                    StringBuffer logMsg = new StringBuffer();
                    String jsonString = new String(t.toString());
                    JSONObject jsonObject = new JSONObject(jsonString);
                    int result = jsonObject.getInt(CloudUtil.SQL_RESULT_CODE);
                    int action = jsonObject.getInt(CloudUtil.SQL_RESULT_ACTION);
                    
                    switch (action) {
                        case CloudUtil.SQL_ACTION_INSERTORUPDATE:
                        case CloudUtil.SQL_ACTION_SELECTORINSERT:
                        case CloudUtil.SQL_ACTION_SELECTORUPDATE:
                            if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                String id = jsonObject.getString(CloudUtil.SQL_RESULT_RECORD_ID);
                                logMsg.append("Id:").append(id);
                                mListener.onSuccess(Long.parseLong(id), null);
                            } else if (result != CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                mListener.onFailure(result, SQLResultParser.Result(result));
                            }
                            break;

                        case CloudUtil.SQL_ACTION_SELECT:
                            if (result == CloudUtil.CLOUDCLIENT_RESULT_OK &&
                                    jsonObject.has(CloudUtil.SQL_RESULT_DATA)) {
                                JSONArray data = jsonObject.getJSONArray(CloudUtil.SQL_RESULT_DATA);
                                String msg = "select OK, data length = " + data.length();
                                logMsg.append("msg:").append(msg);
                                mListener.onSuccess(data, msg);
                            } else if (result != CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                mListener.onFailure(result, SQLResultParser.Result(result));
                            }
                            break;
                        
                        case CloudUtil.SQL_ACTION_UPDATE:
                            if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                logMsg.append("Sql executed success~~");
                                mListener.onSuccess(result, SQLResultParser.Result(result));
                            } else {
                                mListener.onFailure(result, SQLResultParser.Result(result));
                            }
                            break;
                        
                        case CloudUtil.SQL_ACTION_INSERT:
                            if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                String id = jsonObject.getString(CloudUtil.SQL_RESULT_RECORD_ID);
                                logMsg.append("Id:").append(id);
                                mListener.onSuccess(Long.parseLong(id), null);
                            } else if (result == CloudUtil.CLOUDCLIENT_SQL_TABLE_NO_EXIST) {
                                logMsg.append("table not exist");
                                mListener.onFailure(result, "table not exist");
                            } else {
                                mListener.onFailure(result, SQLResultParser.Result(result));
                            }
                            break;
                        
                        case CloudUtil.SQL_ACTION_DELETE:
                            if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                mListener.onSuccess(result, SQLResultParser.Result(result));
                            } else {
                                mListener.onFailure(result, SQLResultParser.Result(result));
                            }
                            break;
                            
                        case CloudUtil.SQL_ACTION_CREATE:
                            if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                                logMsg.append("create table SUCCESS");
                                mListener.onSuccess(t, t.toString());
                            } else {
                                logMsg.append("create table FAILED");
                                mListener.onFailure(result, "create table failed");
                            }
                            break;
                        
                        default:
                            mListener.onFailure(CloudUtil.CLOUDCLIENT_SQL_ACTION_ERROR, "switch default");
                            mLogTrans.d(ActionMode.Response, result, action, params_tmp, "switch default");
                            break;
                    }
                    if (logMsg.length() == 0 ) {
                        if (result == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                            logMsg.append("Sql has be executed success~~");
                        } else {
                            logMsg.append("Sql has be executed failure~~");
                        }
                    }
                    mLogTrans.i(ActionMode.Response, result, action, params_tmp, logMsg.toString());
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public void postEmail(AjaxParams params) {
        
        if (CloudUtil.DEBUG) {
            StringBuffer reqMessage = new StringBuffer("[Email Request]\n");
            reqMessage.append("==>Address:").append(params.get(CloudUtil.EMAIL_PARAM_ADDRESS)).append("\n");
            reqMessage.append("==>Title:").append(params.get(CloudUtil.EMAIL_PARAM_TITLE)).append("\n");
            reqMessage.append("==>Body:").append(params.get(CloudUtil.EMAIL_PARAM_BODY)).append("\n");
            reqMessage.append("==>Version:").append(params.get(CloudUtil.EMAIL_PARAM_VERSION));
            mLogTrans.i(reqMessage.toString());
        }
        
        mLogTrans.d(ActionMode.Request, CloudUtil.CLOUDCLIENT_RESULT_OK, 
                CloudUtil.SQL_ACTION_SEND_EMAIL, params, "sendEmail");
        
        mFinalHttp.post(CloudUtil.POST_EMAIL_URL, params, new AjaxCallBack<Object>() {
            
            @Override
            public void onSuccess(Object t) {
                try {
                    StringBuffer logMsg = new StringBuffer("[Email Response]\n");
                    String jsonString = new String(t.toString());
                    JSONObject jsonObject = new JSONObject(jsonString);
                    int result = jsonObject.getInt(CloudUtil.SQL_RESULT_CODE);
                    if (result != CloudUtil.CLOUDCLIENT_RESULT_OK) {
                        mListener.onFailure(result, SQLResultParser.Result(result));
                        logMsg.append("==>" + SQLResultParser.Result(result));
                    } else {
                        mListener.onSuccess(t, "send email success!");
                        logMsg.append("==>send email success!");
                    }
                    if (CloudUtil.DEBUG) {
                        Log.i(TAG, logMsg.toString());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            
            @Override
            public void onFailure(Throwable t, int errorNo, String strMsg) {
                StringBuffer logMsg = new StringBuffer("[Email Response]\n");
                logMsg.append("==>send email error!\n");
                
                if (errorNo == 0) {
                    errorNo = CloudUtil.CLOUDCLIENT_UNKNOWN_SERVER_ERROR;
                    logMsg.append("==>message:unknown server error~~~");
                }
                logMsg.append("==>post email error! errorNo:" + errorNo + " errorMsg:" + strMsg + " Throwable:" + t);
                if (CloudUtil.DEBUG) {
                    mLogTrans.i(logMsg.toString());
                }
                mListener.onFailure(errorNo, strMsg);
            }
            
        });
    }
    
    public TransResponse postSqlSync(AjaxParams params) {
        int reqAction = Integer.valueOf(params.get(CloudUtil.SQL_PARAM_ACTION));
        StringBuffer message = new StringBuffer();
        if (reqAction >= CloudUtil.SQL_ACTION_INSERTORUPDATE &&
                reqAction <= CloudUtil.SQL_ACTION_SELECTORINSERT) {
            message.append("\n" + params.get(CloudUtil.SQL_PARAM_SQL_PART_INSERT))
                   .append("\n" + params.get(CloudUtil.SQL_PARAM_SQL_PART_UPDATE))
                   .append("\n" + params.get(CloudUtil.SQL_PARAM_SQL_PART_DELETE));
        }
        mLogTrans.d(ActionMode.Request, CloudUtil.CLOUDCLIENT_RESULT_OK, reqAction, params,
                "SQL:" + params.get(CloudUtil.SQL_PARAM_SQL) + message.toString());
        
        return postSyncBase(CloudUtil.POST_URL, params);
    }
    
    public TransResponse postEmailSync(AjaxParams params) {
        if (CloudUtil.DEBUG) {
            StringBuffer reqMessage = new StringBuffer("[Email Request]\n");
            reqMessage.append("==>Address:").append(params.get(CloudUtil.EMAIL_PARAM_ADDRESS)).append("\n");
            reqMessage.append("==>Title:").append(params.get(CloudUtil.EMAIL_PARAM_TITLE)).append("\n");
            reqMessage.append("==>Body:").append(params.get(CloudUtil.EMAIL_PARAM_BODY)).append("\n");
            reqMessage.append("==>Version:").append(params.get(CloudUtil.EMAIL_PARAM_VERSION));
            mLogTrans.i(reqMessage.toString());
        }
        
        mLogTrans.d(ActionMode.Request, CloudUtil.CLOUDCLIENT_RESULT_OK, 
                CloudUtil.SQL_ACTION_SEND_EMAIL, params, "sendEmail");
        
        return postSyncBase(CloudUtil.POST_EMAIL_URL, params);
    }
    
    private TransResponse postSyncBase(String url, AjaxParams params) {
        TransResponse transResponse = new TransResponse();
        
        int reqAction = Integer.valueOf(params.get(CloudUtil.SQL_PARAM_ACTION));
        StringBuffer message = new StringBuffer();
        
        try {
            Object response = null;
            JSONObject jsonObject = null;
            try {
                response = mFinalHttp.postSync(CloudUtil.POST_URL, params);
                jsonObject = new JSONObject(response.toString());
                int code = jsonObject.getInt(CloudUtil.SQL_RESULT_CODE);
                transResponse.code = code;
            } catch (NetworkOnMainThreadException nme) {
                // must stop process!!
                transResponse.code = CloudUtil.CLOUD_NETWORK_ON_MAIN_THREAD;
                throw nme;
            } catch (UnknownHostException uhe) {
                transResponse.code = CloudUtil.CLOUD_NETWORK_UNKNOWN_HOST;
                transResponse.message = uhe.getMessage();
            } catch (HttpHostConnectException hce) {
                transResponse.code = CloudUtil.CLOUD_NETWORK_HOST_CONNECT_REFUSED;
                transResponse.message = hce.getMessage();
            } catch (ConnectTimeoutException cte) {
                transResponse.code = CloudUtil.CLOUD_NETWORK_CONNECT_TIMEOUT;
                transResponse.message = cte.getMessage();
            } catch (SocketTimeoutException ste) {
                transResponse.code = CloudUtil.CLOUD_NETWORK_SOCKET_TIMEOUT;
                transResponse.message = ste.getMessage();
            } catch (IOException ioe) {
                transResponse.code = CloudUtil.CLOUD_FILE_NETWORK_ERROR;
                transResponse.message = ioe.getMessage();
            } catch (Exception e) {
                transResponse.code = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
                transResponse.message = e.getMessage();
            }
            
            int resultCode = transResponse.code;
            
            if (resultCode >= CloudUtil.CLOUD_NETWORK_ON_MAIN_THREAD && 
                    resultCode <= CloudUtil.CLOUD_NETWORK_UNKNOWN_ERROR) {
                // 网络请求错误,消息还未发出
                mLogTrans.w(ActionMode.Response, resultCode, reqAction,
                        params, "<NETWORK ERROR>" + SQLResultParser.Result(resultCode));
                transResponse.action = reqAction;
                return transResponse;
            }
            
            if (jsonObject == null) {
                Log.i(TAG, "jsonObject is null");
                return transResponse;
            }
            
            int respAction = jsonObject.getInt(CloudUtil.SQL_RESULT_ACTION);
            transResponse.action = respAction;
            if (resultCode == CloudUtil.CLOUDCLIENT_RESULT_OK) {
                switch (respAction) {
                    case CloudUtil.SQL_ACTION_INSERTORUPDATE:
                    case CloudUtil.SQL_ACTION_SELECTORINSERT:
                    case CloudUtil.SQL_ACTION_SELECTORUPDATE: {
                        String id = jsonObject.getString(CloudUtil.SQL_RESULT_RECORD_ID);
                        message.append("Id:").append(id);
                        transResponse.object = id;
                        break;
                    }
                        
                    case CloudUtil.SQL_ACTION_SELECT: {
                        if (jsonObject.has(CloudUtil.SQL_RESULT_DATA)) {
                            JSONArray data = jsonObject.getJSONArray(CloudUtil.SQL_RESULT_DATA);
                            message.append("message:select OK, data length=").append(data.length());
                            transResponse.object = data;
                        } else {
                            message.append("message:select OK, data length=").append(0);
                            transResponse.object = null;
                        }
                        break;
                    }
                        
                    case CloudUtil.SQL_ACTION_INSERT: {
                        String id = jsonObject.getString(CloudUtil.SQL_RESULT_RECORD_ID);
                        message.append("Id:").append(id);
                        transResponse.object = id;
                        break;
                    }
                    
                    case CloudUtil.SQL_ACTION_UPDATE:
                        message.append("update success~~");
                        break;
                        
                    case CloudUtil.SQL_ACTION_DELETE:
                        message.append("delete success~~");
                        break;
                        
                    case CloudUtil.SQL_ACTION_CREATE:
                        message.append("create table success~~");
                        break;
                        
                    case CloudUtil.SQL_ACTION_SEND_EMAIL:
                        message.append("==>send email success!");
                        break;
                        
                    default:
                        mListener.onFailure(CloudUtil.CLOUDCLIENT_SQL_ACTION_ERROR, "switch default");
                        mLogTrans.d(ActionMode.Response, resultCode, respAction, params, "switch default");
                        break;
                }
                mLogTrans.i(ActionMode.Response,
                        resultCode, respAction, params, message.toString());
                transResponse.message = message.toString();
            } else {
                mLogTrans.i(ActionMode.Response, resultCode,
                        respAction, params, SQLResultParser.Result(resultCode));
                transResponse.message = SQLResultParser.Result(resultCode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            transResponse.code = CloudUtil.CLOUDCLIENT_UNKNOWN_ERROR;
            transResponse.action = reqAction;
            transResponse.message = e.getMessage();
        }
        
        return transResponse;
    }
    
    public static class TransResponse {
        public int code = CloudUtil.CLOUDCLIENT_ENTITY_INITIALIZATION;
        public int action = CloudUtil.CLOUDCLIENT_ENTITY_INITIALIZATION;
        public String message;
        public Object object;
    }
}

interface TransmitterListener {
    public void onSuccess(Object object, String msg);
    public void onFailure(int errorNo, String msg);
}

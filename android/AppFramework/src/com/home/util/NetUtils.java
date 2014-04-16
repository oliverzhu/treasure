package com.home.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.home.bean.Image;
import com.home.util.log.Log;

/**
 * 网络操作
 * @author jianwen.zhu
 * 2013/12/6
 */
public class NetUtils {
	private static final String TAG = "NetUtils";
	
	private static final String NETTYPE_WIFI = "WIFI";
	
	private static String multipart_form_data = "multipart/form-data";   
    private static String twoHyphens = "--";   
    private static String boundary = java.util.UUID.randomUUID().toString();   // 数据分隔符    
    private static String lineEnd = "\r\n";    // The value is "\r\n" in Windows.
    
    enum NETWORK_STATUS {
        STATE_WIFI, STATE_GPRS, STATE_NONE_NETWORK
    }
	
	private NetUtils(){}
	
	public static NETWORK_STATUS getNetworkType(Context context) {
        NETWORK_STATUS ret = NETWORK_STATUS.STATE_NONE_NETWORK;

        ConnectivityManager connetManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connetManager == null) {
            Log.e(TAG, "isNetWorkAvailable connetManager = null",Log.APP);
            return ret;
        }
        NetworkInfo[] infos = connetManager.getAllNetworkInfo();
        if (infos == null) {
            return ret;
        }
        for (int i = 0; i < infos.length && infos[i] != null; i++) {
            if (infos[i].isConnected() && infos[i].isAvailable()) {

                if (infos[i].getTypeName().equalsIgnoreCase(NETTYPE_WIFI)) {
                    ret = NETWORK_STATUS.STATE_WIFI;
                } else {
                    ret = NETWORK_STATUS.STATE_GPRS;
                }

                break;
            }
        }

        Log.i(TAG, "get network stype is : " + ret,Log.APP);
        return ret;

    }
	
	/**
	 * 
	 * @param context
	 * @param typeName("",WIFI,MOBILE)
	 * @return
	 */
	public static boolean isNetWorkAvailable(Context context, String typeName) {

        Log.i(TAG, ">>> isNetWorkAvailable context = " + context + "typeName = " + typeName,Log.APP);

        boolean ret = false;

        ConnectivityManager connetManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connetManager == null) {
            Log.e(TAG, "isNetWorkAvailable connetManager = null",Log.APP);
            return ret;
        }
        NetworkInfo[] infos = connetManager.getAllNetworkInfo();
        if (infos == null) {
            return ret;
        }
        if ((typeName == null) || (typeName.length() <= 0)) {
            for (int i = 0; i < infos.length && infos[i] != null; i++) {
                if (infos[i].isConnected() && infos[i].isAvailable()) {
                    ret = true;
                    break;
                }
            }
        } else {
            for (int i = 0; i < infos.length && infos[i] != null; i++) {
                if (infos[i].getTypeName().equalsIgnoreCase(typeName) && infos[i].isConnected()
                        && infos[i].isAvailable()) {
                    Log.i(TAG, "isNetWorkAvailable name is : " + infos[i].getTypeName(),Log.APP);
                    ret = true;
                    break;
                }
            }
        }

        Log.i(TAG, "isNetWorkAvailable >>> result is : " + ret,Log.APP);
        return ret;
    }
	
	/**
	 * 
	 * @param url
	 * @return
	 */
	public synchronized InputStream getInputStreamByGet(String url){
		Log.i(TAG, "<getInputStreamByGet> url:" + url, Log.APP);
		HttpURLConnection httpConnection = null;
		try {
			int currentSize = 0;
			if(url == null) {
				return null;
			}
			URL uri = new URL(url);
			httpConnection = (HttpURLConnection)uri.openConnection();
            httpConnection.setRequestProperty("User-Agent", "PacificHttpClient");
            if(currentSize > 0) {
                httpConnection.setRequestProperty("RANGE", "bytes=" + currentSize + "-");
            }
			//设置超时时间
			httpConnection.setConnectTimeout(10000);// 限制连接超时5秒钟
			httpConnection.setReadTimeout(2*10000);
			httpConnection.setRequestProperty("Content-type",
					"text/html;charset=UTF-8");

			httpConnection.setDoOutput(true);
			httpConnection.setRequestMethod("GET");
			httpConnection.setUseCaches(false);
			int requestCode = httpConnection.getResponseCode();
			if (requestCode == 200) {
				InputStream in = httpConnection.getInputStream();
				return in;
			}
		} catch (ProtocolException e) {
			Log.e(TAG, "<getInputStreamByGet> fail", Log.APP);
		} catch (IOException e) {
			Log.e(TAG, "<getInputStreamByGet> fail", Log.APP);
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * post方式从服务器获取json数组
	 * @return
	 */
	public static JSONArray getJSONArrayByPost(String uri)
	{
		Log.i(TAG, "<getJSONArrayByPost> uri:" + uri, Log.APP);
		StringBuilder builder = new StringBuilder();
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);// Set the default socket timeout (SO_TIMEOUT) // in milliseconds which is the timeout for waiting for data.  
	    HttpConnectionParams.setSoTimeout(httpParameters, 10000);  
		HttpClient client = new DefaultHttpClient(httpParameters);
		HttpPost post = new HttpPost(uri);
		
		try {
			HttpResponse response = client.execute(post);
			
			BufferedReader reader = 
					new BufferedReader(
							new InputStreamReader(response.getEntity().getContent()));
			for(String s = reader.readLine();s!=null;s=reader.readLine())
			{
				builder.append(s);
			}
			
			String jsonString = new String(builder.toString());
			
			if("{}".equals(jsonString))
			{
				return null;
			}
			Log.i(TAG, "<getJSONArrayByPost> jsonString:" + jsonString, Log.DATA);
			return new JSONArray(jsonString);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * get方式从服务器获取json数组
	 * @return
	 */
	public static JSONObject getJSONArrayByGet(String uri) {
		Log.i(TAG, "<getJSONArrayByGet> uri:" + uri, Log.APP);
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(uri);

		try {
			HttpResponse response = client.execute(get);

			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			for (String s = reader.readLine(); s != null; s = reader.readLine()) {
				builder.append(s);
			}

			String jsonString = new String(builder.toString());

			if ("{}".equals(jsonString)) {
				return null;
			}
			Log.i(TAG, "<getJSONArrayByGet> jsonString:" + jsonString, Log.DATA);
			return new JSONObject(jsonString);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 使用post的方式，提交表单，不包括文件上传(新服务器)
	 * 
	 * @param actionUrl
	 * @param params
	 * @param files
	 * @return
	 * @throws IOException
	 */
	public static boolean uploadParamsByPost(String actionUrl, Map<String, String> params)
			throws IOException {
		Log.i(TAG, "<uploadParamsByPost> actionUrl:" + actionUrl + " params:" + params, Log.APP);
		String BOUNDARY = java.util.UUID.randomUUID().toString();
		String PREFIX = "--", LINEND = "\r\n";
		String MULTIPART_FROM_DATA = "multipart/form-data";
		String CHARSET = "UTF-8";

		URL uri = new URL(actionUrl);
		HttpURLConnection conn = (HttpURLConnection) uri.openConnection();

		conn.setReadTimeout(10 * 1000);
		conn.setConnectTimeout(10*1000);
		conn.setDoInput(true);// 允许输入
		conn.setDoOutput(true);// 允许输出
		conn.setUseCaches(false);
		conn.setRequestMethod("POST"); // Post方式
		conn.setRequestProperty("connection", "keep-alive");
		conn.setRequestProperty("Charsert", "UTF-8");

		conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
				+ ";boundary=" + BOUNDARY);

		// 首先组拼文本类型的参数
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, String> entry : params.entrySet()) {
			sb.append(PREFIX);
			sb.append(BOUNDARY);
			sb.append(LINEND);
			sb.append("Content-Disposition: form-data; name=\""
					+ entry.getKey() + "\"" + LINEND);
			sb.append("Content-Type: text/plain; charset=" + CHARSET + LINEND);
			sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
			sb.append(LINEND);
			sb.append(entry.getValue());
			sb.append(LINEND);
		}

		DataOutputStream outStream = new DataOutputStream(
				conn.getOutputStream());
		outStream.write(sb.toString().getBytes());

		// 请求结束标志
		byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
		outStream.write(end_data);
		outStream.flush();
		
		outStream.close();
		// 得到响应码
		int res = conn.getResponseCode();
		if (res == 200) {
			return true;
		}
		conn.disconnect();

		return false;
	}
	
	/**
	 * 使用post的方式，提交表单，不包括文件上传(老服务器写的代码使用这种方式)
	 * @param actionUrl:http://xxx/xxx.json
	 * @param query:Helpers.combinaStr("login_name=#&password=#&email=#&name=", listParams);
	 * @return
	 */
	public static JSONObject uploadParamsByPost(String actionUrl, String query) {
		Log.i(TAG, "<uploadParamsByPost> actionUrl:" + actionUrl + " query:" + query, Log.APP);
		try {
			URL uri = new URL(actionUrl);
			HttpURLConnection conn = (HttpURLConnection) uri.openConnection();

			conn.setReadTimeout(10 * 1000);
			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false);
			conn.setRequestMethod("POST"); // Post方式
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Charsert", "UTF-8");

			// query is your body
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");// 请求头,
																							// 必须设置
			conn.setRequestProperty("Content-Length", query.toString().getBytes("UTF-8").length + "");// 注意是字节长度,
																										// 不是字符长度
			conn.getOutputStream().write(query.toString().getBytes("UTF-8"));
			// 得到响应码
			int res = conn.getResponseCode();

			if (res == HttpURLConnection.HTTP_OK) {
				StringBuffer stringBuffer = new StringBuffer();
				String readLine;
				BufferedReader responseReader;
				// 处理响应流，必须与服务器响应流输出的编码一致
				responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
				while ((readLine = responseReader.readLine()) != null) {
					stringBuffer.append(readLine).append("/n");
				}
				responseReader.close();

				return new JSONObject(stringBuffer.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public int uploadFilesByPost(String actionUrl, String fileName, File file){
		Log.i(TAG, "<uploadFilesByPost> actionUrl:" + actionUrl + " fileName:" + fileName, Log.APP);
        String CHARSET = "UTF-8";

        // 得到响应码
		int res = 0;
		try {
			URL uri = new URL(actionUrl);
			HttpURLConnection conn = (HttpURLConnection) uri.openConnection();   
			conn.setReadTimeout(10 * 1000); 
			conn.setDoInput(true);// 允许输入
			conn.setDoOutput(true);// 允许输出
			conn.setUseCaches(false); 
			conn.setRequestMethod("POST");  //Post方式
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Charsert", "UTF-8");
			
			conn.setRequestProperty("Content-Type", multipart_form_data+ ";boundary=" + boundary); 
			
			//输出流
			DataOutputStream outStream = new DataOutputStream(conn.getOutputStream()); 

			// 发送文件数据
			if (file != null){ 
			        StringBuilder sb1 = new StringBuilder();
			        sb1.append(twoHyphens);
			        sb1.append(boundary);
			        sb1.append(lineEnd);
			        //actionData 是自己定义的
			        sb1.append("Content-Disposition: form-data; name=\"actionData\"; filename=\""
			                        + fileName + "\"" + lineEnd);
			        sb1.append("Content-Type: application/octet-stream; charset="
			                + CHARSET + lineEnd);
			        sb1.append(lineEnd);
			        outStream.write(sb1.toString().getBytes());

			        InputStream is = new FileInputStream(file);
			        byte[] buffer = new byte[1024];
			        int len = 0;
			        while ((len = is.read(buffer)) != -1) {
			            outStream.write(buffer, 0, len);
			        }

			        is.close();
			        outStream.write(lineEnd.getBytes());
			}   
			
			// 请求结束标志
			byte[] end_data = (twoHyphens + boundary + twoHyphens + lineEnd).getBytes();

			outStream.write(end_data);
			outStream.flush();
			res = conn.getResponseCode(); 

			outStream.close();
			conn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return  res;
    
	}
	
	
	
	/**  
     * 直接通过 HTTP 协议提交数据到服务器，实现表单提交功能。  
     * @param actionUrl 上传路径  
     * @param params 请求参数key为参数名，value为参数值  
     * @param files 上传文件信息  
     * @return 返回请求结果  
     */   
    public static String uploadImagesByPost(String actionUrl, Set<Map.Entry<String,String>> params, Image[] files) {
    	Log.i(TAG, "<uploadImagesByPost> actionUrl:" + actionUrl + " params:" + params, Log.APP);
        HttpURLConnection conn = null;   
        DataOutputStream output = null;   
        BufferedReader input = null;   
        try {   
            URL url = new URL(actionUrl);   
            conn = (HttpURLConnection) url.openConnection();   
            conn.setConnectTimeout(120000);   
            conn.setDoInput(true);        // 允许输入    
            conn.setDoOutput(true);        // 允许输出    
            conn.setUseCaches(false);    // 不使用Cache    
            conn.setRequestMethod("POST");   
            conn.setRequestProperty("Connection", "keep-alive");   
            conn.setRequestProperty("Content-Type", multipart_form_data + "; boundary=" + boundary);   
               
            conn.connect();   
            output = new DataOutputStream(conn.getOutputStream());   
               
            addImageContent(files, output);    // 添加图片内容    
               
            addFormField(params, output);    // 添加表单字段内容    
               
            output.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);// 数据结束标志    
            output.flush();   
               
            int code = conn.getResponseCode();   
            if(code == 200) {   
            	 input = new BufferedReader(new InputStreamReader(conn.getInputStream()));   
                 StringBuilder response = new StringBuilder();   
                 String oneLine;   
                 while((oneLine = input.readLine()) != null) {   
                     response.append(oneLine + lineEnd);   
                 }   
                 return response.toString();   
            }   
            return null;
        } catch (IOException e) {   
            throw new RuntimeException(e);   
        } finally {   
            // 统一释放资源    
            try {   
                if(output != null) {   
                    output.close();   
                }   
                if(input != null) {   
                    input.close();   
                }   
            } catch (IOException e) {   
                throw new RuntimeException(e);   
            }   
               
            if(conn != null) {   
                conn.disconnect();   
            }   
        }   
    }   
	
	private static void addImageContent(Image[] files, DataOutputStream output) {   
        for(Image file : files) {   
            StringBuilder split = new StringBuilder();   
            split.append(twoHyphens + boundary + lineEnd);   
            split.append("Content-Disposition: form-data; name=\"" + file.formName + "\"; filename=\"" + file.fileName + "\"" + lineEnd);   
//            split.append("Content-Type: " + file.type + lineEnd);   
            split.append(lineEnd); 
            FileInputStream fStream = null;
            try {   
                // 发送图片数据    
                output.writeBytes(split.toString());  
                
                /* 取得文件的FileInputStream */
                fStream = new FileInputStream(file.path);
                /* 设置每次写入1024bytes */
                int bufferSize = 1024;
                byte[] buffer = new byte[bufferSize];
                int length = -1;
                /* 从文件读取数据至缓冲区 */
                while((length = fStream.read(buffer)) != -1)
                {
                  /* 将资料写入DataOutputStream中 */
                	output.write(buffer, 0, length);
                }
                output.writeBytes(lineEnd);   
            } catch (IOException e) {   
                throw new RuntimeException(e);   
            }   finally
            {
            	if(fStream != null)
            	{
            		try {
						fStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
            	}
            }
        }   
    }  
	
	  private static void addFormField(Set<Map.Entry<String,String>> params, DataOutputStream output) {   
	        StringBuilder sb = new StringBuilder();   
	        for(Map.Entry<String, String> param : params) {   
	            sb.append(twoHyphens + boundary + lineEnd);   
	            sb.append("Content-Disposition: form-data; name=\"" + param.getKey() + "\"" + lineEnd);   
	            sb.append(lineEnd);   
	            sb.append(param.getValue() + lineEnd);   
	        }   
	        try {   
	            output.writeBytes(sb.toString());// 发送表单字段数据    
	        } catch (IOException e) {   
	            throw new RuntimeException(e);   
	        }   
	    }   

}

package me.gc.facerecognition.util;

import android.content.Context;
import android.util.Log;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.client.params.ClientPNames;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HttpUtil {

	private static AsyncHttpClient asyncClient = new AsyncHttpClient();
	private static long time = 0;
	private static final long TimeForReLogin = 60*1000*15;//15分钟超时，需重新登录验证

	/**
	 * 用于拦截和处理由AsyncHttpClient创建的请求
	 * @param url
	 * @param responseHandler
	 */
	public static void get(String url, AsyncHttpResponseHandler responseHandler) {

		/**
		 * RequestParams params
		 * 用于创建AsyncHttpClient实例中的请求参数(包括字符串或者文件)的集合
		 */

		RequestParams params = new RequestParams();
		asyncClient.setTimeout(15000);
		asyncClient.get(url, params, responseHandler);
	}

	public static void get(String url, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
		Log.d("HTTP GET", url + "?" + params.toString());

		System.setProperty("http.keepAlive", "false");
		if(params != null){
			Log.d("HTTP get", url + "?" + params.toString());
			StringBuilder paramstr = new StringBuilder();
			paramstr.append(params.toString());
			paramstr.append("&key=SD7B3L3P");
			String paramstr1 = paramstr.toString();
			// 使用MD5计算出token�?  strToken为参�?				
			String token11 = "";
			try{
				token11 = getMD5String(paramstr1, "UTF-8");
				params.put("token", token11);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Log.d("HTTP get =", url + "?" + params.toString());
		}
		asyncClient.setTimeout(8000);
		asyncClient.get(url, params, responseHandler);
	}

	public static void get(Context context, String url, RequestParams params,
                           AsyncHttpResponseHandler responseHandler) {
		Log.d("HTTP GET", url + "?" + params.toString());
		asyncClient.setTimeout(8000);
		asyncClient.get(context, url, params, responseHandler);
	}
	public static void post(String url, RequestParams params,
                            AsyncHttpResponseHandler responseHandler){
		System.setProperty("http.keepAlive", "false");
		if(params != null){
			Log.d("HTTP post", url + "?" + params.toString());
			StringBuilder paramstr = new StringBuilder();
			paramstr.append(params.toString());
			Log.d("HTTP post =", url + "?" + params.toString());
		}else{
			Log.d("HTTP post = ", url);
		}
		//String paramstr2 = params.toString();
		asyncClient.setTimeout(150000);
		asyncClient.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
		asyncClient.post(url, params, responseHandler);
		time = System.currentTimeMillis();
		
	}

	public static void post(Context context, String url, RequestParams params,
                            AsyncHttpResponseHandler responseHandler) {
		Log.d("HTTP post", url + "?" + params.toString());
		asyncClient.setTimeout(8000);
		asyncClient.post(context, url, params, responseHandler);
	}

	private static void initParams(RequestParams params) {
		params.put("key", "SD7B3L3P");
		String paramstr1 = params.toString();
		// 使用MD5计算出token�?  strToken为参�?				
		String token11 = "";
		try{
			token11 = getMD5String(paramstr1, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params.put("token", token11);
	}
	// MD5算法，获得token�?				
	private static String getMD5String(String plainText, String charset) throws UnsupportedEncodingException {
		StringBuffer buf = new StringBuffer("");
		try { 			
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(plainText.getBytes(charset)); 		
			byte b[] = md.digest(); 		
			int i; 		
			for (int offset = 0; offset < b.length; offset++) { 		
				i = b[offset]; 	
				if(i<0) i+= 256; 	
				if(i<16) 	
					buf.append("0"); 
				buf.append(Integer.toHexString(i));
			} 		
					
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace(); 		
		} 			
		return buf.toString();			
	}
	
	public static boolean isNeedRelogin(long curTime){
		if(curTime-time>TimeForReLogin){
			return true;
		}else{
			return false;
		}
	}
	
	public static void setTime(long curTime){
		time = curTime;
	}
}

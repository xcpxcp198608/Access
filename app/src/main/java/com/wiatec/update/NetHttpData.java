package com.wiatec.update;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class NetHttpData {
	
	//http://mac.wiatec.com/macauth.asp
	public static String dataIp="http://mac.wiatec.com/macauth.asp";
	//http://www.wiatec.com/macauth.asp
	private static AsyncHttpClient client = null ;
	private static NetHttpData nethttpdao = null ;
	
	private NetHttpData (){
		client = new AsyncHttpClient();
	}
	
	public static NetHttpData getHttpDao(){
		if (nethttpdao==null) {
			nethttpdao = new NetHttpData();
		}
		return nethttpdao;
	}

	public void matchMacAdress(String mac,AsyncHttpResponseHandler responseHandler){
		String url = dataIp;
		RequestParams params = new RequestParams();
		params.add("macadr", mac);
		client.get(url, params, responseHandler);
	}
}

package com.wiatec.update.services;


import java.io.File;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wiatec.update.MainActivity;
import com.wiatec.update.R;
import com.wiatec.update.Util;
import com.wiatec.update.utils.CacheUtils;
import com.wiatec.update.utils.Constant;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

public class CheckVersionService extends Service {
	
	private Util mUtil ;
	private IntentFilter intentFilter;
	//private boolean flag = true;
	//private int count = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("Service onStartCommand", "服务启动了");
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		mUtil = new Util(getApplicationContext());
		intentFilter = new IntentFilter(); 
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); 
		registerReceiver(NetWorkReceiver, intentFilter);
		super.onCreate();
	}
	
	
	 @Override
	public void onDestroy() {
		 if(NetWorkReceiver != null){
			 unregisterReceiver(NetWorkReceiver);
		 }
		super.onDestroy();
	}


	

	BroadcastReceiver NetWorkReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			boolean network_connect = false;
			boolean eth_connect = false;
			ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo info = connectMgr.getActiveNetworkInfo();
			
			if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2) {
				NetworkInfo eth = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
	    		eth_connect = eth.isConnected();
			}else {
				File eth_address = new File(Util.ETH_ADDRESS);
				eth_connect = eth_address.exists();
	    	}
			
			if(info == null){
	    		network_connect = false;
	    	}else{
	    		network_connect = true;
	    	}
			
			if (network_connect || eth_connect) {
				// connect network
				Log.e("connect network", "connect network");
				checkVersion(Constant.VERSIONURL);
			} else {
				// unconnect network
			}
		}
	};
	
	
	 private void checkVersion(String url1){
	    	new AsyncHttpClient().get(url1, new TextHttpResponseHandler() {
				@Override
				public void onSuccess(int arg0, Header[] arg1, String arg2) {
					String updateInfo = "Have a new update package.";
					try {
						JSONObject jobj=new JSONObject(arg2);
						String currentVersion = CacheUtils.getString(getApplicationContext(), "current_version","BTV2016030201");
						String serverVersion = "";
						if(jobj.get("version")!=null){
							serverVersion = jobj.get("version").toString().trim();
						}
						if(jobj.get("updateInfo")!=null){
							updateInfo = jobj.get("updateInfo").toString().trim();
						}
						Log.e("serverVersion", serverVersion);
						Log.e("currentVersion", currentVersion);
						if(!currentVersion.equals(serverVersion)){
							/*NotificationManager mNotificationManager  = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
							//跳转意图
					        Intent intent = new Intent(CheckVersionService.this,MainActivity.class);
					        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,intent,0);
					        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
					        //通知栏显示内容
					        builder.setTicker("BTV have a new upgrade package.");
					        //通知消息下拉是显示的文本内容
					        builder.setContentText("BTV have a new upgrade package.");
					        //通知栏消息下拉时显示的标题
					        builder.setContentTitle("BTVUpdater");
					        //接收到通知时，按手机的默认设置进行处理，声音，震动，灯
					        builder.setDefaults(Notification.DEFAULT_ALL);
					        //通知栏显示图标
					        builder.setSmallIcon(R.drawable.ic_launcher);
					        builder.setContentIntent(pendingIntent);
					        Notification notification = builder.build();
					        //点击跳转后消失
					        notification.flags |= Notification.FLAG_AUTO_CANCEL;
					        mNotificationManager.notify(1,notification);*/
							
							AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());  
					        builder.setTitle("ACCESS");  
					        //builder.setIcon(R.drawable.ic_launcher);
					        builder.setMessage(updateInfo);
					        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss(); 
									stopSelf();
								}
							});
					        builder.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
					        	
					                public void onClick(DialogInterface dialog, int whichButton) {  
					                	Intent intent = new Intent(CheckVersionService.this,MainActivity.class);
										intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										getApplicationContext().startActivity(intent);
										stopSelf();
					           }  
					        });  
					        builder.setCancelable(false);
					        AlertDialog dialog = builder.create();
					        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
					        dialog.show();
						}
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				
				@Override
				public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
					
				}
			});
	    }
}

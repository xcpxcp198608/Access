package com.wiatec.update;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.wiatec.PX.ApkCheck;
import com.wiatec.PX.ApkLaunch;
import com.wiatec.PX.FavoriteManager;
import com.wiatec.update.R;
import com.wiatec.update.utils.Constant;
import com.wiatec.update.utils.StreamUtils;
import com.wiatec.update.utils.UIUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    Util m_util;
    protected static final int SHOW_UPDATE_DIALOG = 1;
	private static final int LOAD_MAINUI = 2;
	private static final int UPDATE_ROM = 3;
    
    // 包管理器  
 	private PackageManager packageManager;
 	private int clientVersionCode;
    
    // 服务器资源的下载路径
 	private String downloadurl;
 	
 	
 	private ProgressDialog mDialog;
 	
 // 消息处理器
 	private Handler handler = new Handler() {
 		public void handleMessage(android.os.Message msg) {
 			switch (msg.what) {
 			case LOAD_MAINUI:
 				loadMainUI();
 				break;
 			case SHOW_UPDATE_DIALOG:
 				// 因为对话框是activity的一部分显示对话框 必须指定activity的环境（令牌）
 				AlertDialog.Builder builder = new Builder(MainActivity.this);
 				builder.setTitle("Upgrade Info");
				builder.setCancelable(false);
 				builder.setMessage("APP has a new version, upgrade now?");
 				// builder.setCancelable(false);
 				builder.setOnCancelListener(new OnCancelListener() {
 					@Override
 					public void onCancel(DialogInterface dialog) {
 						dialog.dismiss();
 					}
 				});
 				builder.setNegativeButton("Yes", new OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						Log.e("下载：" ,downloadurl);
 						mDialog = new ProgressDialog(MainActivity.this);
						mDialog.setTitle("Downloading...");
						mDialog.setMessage("updater.apk");
						mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						mDialog.setCancelable(false);
						mDialog.show();
 						download(downloadurl);
 					}
 				});
// 				builder.setPositiveButton("Remind me later", new OnClickListener() {
// 					@Override
// 					public void onClick(DialogInterface dialog, int which) {
// 						dialog.dismiss();
// 						loadMainUI();
// 					}
//
// 				});
 				builder.show();
 				break;
 			}
 		};
 	};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
        packageManager = getPackageManager();
        m_util = new Util(this);

		String device = Build.MODEL;
		if(device.equals("BTVi3")|| device.equals("MorphoBT E110")|| device.equals("BTV3")){
			FavoriteManager.backup();
		}else{
			AlertDialog.Builder builder = new Builder(MainActivity.this);
			builder.setTitle("Warning");
			builder.setMessage("This device is not supported");
			builder.setCancelable(false);
			builder.setNegativeButton("Confirm", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.show();
			return;
		}

        
        AlertDialog.Builder builder = new Builder(MainActivity.this);
        	builder.setTitle("Warning"); 
        	//builder.setIcon(R.drawable.ic_launcher);
			builder.setMessage("This content is provided by Internet, only for learning.Thanks.");
			builder.setCancelable(false);

			builder.setNegativeButton("Next", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					if (!m_util.CheckNetwork(true)) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								Log.e("loadMainUI", "直接loadMainUI()");
								SystemClock.sleep(2000);
								loadMainUI();
							}
						}).start();
						
					} else {
						try {
							PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
							clientVersionCode = packInfo.versionCode;
							checkVersion();
						} catch (NameNotFoundException e) {
							e.printStackTrace();
							// 不会发生 can't reach
						}
					}
				}
			});
			builder.show();
        
        
        Update.DownloadType type = m_util.DetectEnvironment();
        if (type != Update.DownloadType.NONE) {
        	if (type == Update.DownloadType.KODI)
        		m_util.KillProcess(Update.KodiPackage);
        	else if (type == Update.DownloadType.XBMC)
        		m_util.KillProcess(Update.XbmcPackage);
        }   
        
        
//        if(!m_util.CheckNetwork(true)){
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						Log.e("loadMainUI","直接loadMainUI()");
//						SystemClock.sleep(2000);
//						loadMainUI();
//					}
//				}).start();
//		}else{
//			try {
//				PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
//				clientVersionCode = packInfo.versionCode;
//				checkVersion();
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//				// 不会发生 can't reach
//			}
//		}
        
        
        
	/*	new Thread(new Runnable() {

			@Override
			public void run() {
				SystemClock.sleep(1000);
				startActivity();
			}
		}).start();*/
    }
    
    /**
	 * 连接服务器 检查版本号 是否有更新
	 */
	private void checkVersion() {
		new Thread() {
			public void run() {
				Message msg = Message.obtain();
				// 检查 代码执行的时间。如果时间少于2秒 补足2秒
				long startTime = System.currentTimeMillis();
				try {
					URL url = new URL(Constant.APPUPDATEURL);//待会需要修改
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(2000);
					int code = conn.getResponseCode();
					if (code == 200) {
						InputStream is = conn.getInputStream();// json文本
						String json = StreamUtils.readStream(is);
						if (TextUtils.isEmpty(json)) {
							// 服务器json获取失败
							// 错误2016 请联系客服
							UIUtils.showToast(MainActivity.this,"服务器json获取失败,请联系客服");
							msg.what = LOAD_MAINUI;
						} else {
							Log.e("json", json);
							JSONObject jsonObj = new JSONObject(json);
							downloadurl = jsonObj.getString("downloadurl");
							int serverVersionCode = jsonObj.getInt("version");
							boolean isNeedUpdate = serverVersionCode > clientVersionCode;
							if (isNeedUpdate) {
								// 需要更新，弹出更新提醒对话框
								msg.what = SHOW_UPDATE_DIALOG;
							} else {
								// 不需要更新，进入应用程序主界面
								msg.what = LOAD_MAINUI;
							}
						}
					} else {
						// 错误2015 请联系客服
						UIUtils.showToast(MainActivity.this,"服务器状态码错误,请联系客服");
						msg.what = LOAD_MAINUI;
					}
				} catch (MalformedURLException e) {
					e.printStackTrace();
					// 错误2011 请联系客服
					UIUtils.showToast(MainActivity.this,"APP下载路径不正确,请联系客服");
					msg.what = LOAD_MAINUI;
				} catch (NotFoundException e) {
					e.printStackTrace();
					// 错误2012 请联系客服
					UIUtils.showToast(MainActivity.this,"服务器地址找不到,请联系客服");
					msg.what = LOAD_MAINUI;
				} catch (IOException e) {
					e.printStackTrace();
					// 错误2013 请联系客服
					UIUtils.showToast(MainActivity.this, "网络错误,请检查网络后重试");
					msg.what = LOAD_MAINUI;
				} catch (JSONException e) {
					e.printStackTrace();
					// 错误2014 请联系客服
					UIUtils.showToast(MainActivity.this,"json解析错误,请联系客服");
					msg.what = LOAD_MAINUI;
				} finally {
					long endtime = System.currentTimeMillis();
					long dtime = endtime - startTime;
					if (dtime > 2000) {
						handler.sendMessage(msg);
					} else {
						try {
							Thread.sleep(2000 - dtime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						handler.sendMessage(msg);
					}
				}
			};
		}.start();

	}

	/**
	 * 多线程的下载器
	 * 
	 * @param downloadurl
	 */
	private void download(String downloadurl) {
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/updater.apk");
		if(file.exists()){
			Log.e("file已存在", "即将删除");
			file.delete();
		}
		// 多线程断点下载。
		HttpUtils http = new HttpUtils();
		http.download(downloadurl, Environment.getExternalStorageDirectory().getAbsolutePath()+"/updater.apk",
				new RequestCallBack<File>() {
					@Override
					public void onSuccess(ResponseInfo<File> arg0) {
						System.out.println("安装 /mnt/sdcard/updater.apk");
						if(mDialog!=null&&mDialog.isShowing()){
							mDialog.dismiss();
						}
						Intent intent = new Intent();
						intent.setAction("android.intent.action.VIEW");
						intent.addCategory("android.intent.category.DEFAULT");

						intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "updater.apk")),"application/vnd.android.package-archive");
						startActivityForResult(intent, 0);
					}

					@Override
					public void onFailure(HttpException arg0, String arg1) {
						Toast.makeText(MainActivity.this, "下载失败", Toast.LENGTH_SHORT).show();
						System.out.println(arg1);
						arg0.printStackTrace();
						loadMainUI();
					}

					@Override
					public void onLoading(long total, long current,boolean isUploading) {
						//下载进度条展示
						mDialog.setMax((int)total);
						mDialog.setProgress((int)current);
						super.onLoading(total, current, isUploading);
					}
				});
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadMainUI();
		super.onActivityResult(requestCode, resultCode, data);
	}
    
   
    public void loadMainUI()
    {
    	Intent intent = new Intent(this, Update.class );
    	this.startActivity(intent);
    	MainActivity.this.finish();
    }
    	
}
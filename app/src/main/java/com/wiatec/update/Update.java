package com.wiatec.update;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jude.rollviewpager.RollPagerView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.wiatec.PX.F;
import com.wiatec.PX.FavoriteManager;
import com.wiatec.PX.MarqueeView;
import com.wiatec.PX.RollViewAdapter;
import com.wiatec.PX.RollViewInfo;
import com.wiatec.entity.NewVersionInfoEntity;
import com.wiatec.update.utils.CacheUtils;
import com.wiatec.update.utils.Constant;
import com.wiatec.update.widget.DotsTextView;


public class Update extends Activity {
    /// when XBMC or Kodi is detected this will contain what
    public enum DownloadType {
        NONE, XBMC, KODI, Addon
    }
    @SuppressLint("SdCardPath")
	public static String pathSD = "/sdcard/Android/data/";
    public static String KodiPackage = "org.xbmc.kodi";
    public static String XbmcPackage = "org.xbmc.xbmc";
    public static String FavouriteFile = "favourites.xml";
    public static String SUCCESS = "success";
    public static String LABEL = "BTV";
    private final String KodiUrl = "http://gobeyondtv.co/apk/BTV.apk";
    //private final String DownUrl1 = "http://gobeyondtv.co/wizard/backup.zip";
    private final String DownUrl1 = "http://www.gobeyondtv.co/wizard/backup_test.zip";
    //private final String DownUrl2 = "http://gobeyondtv.co/wizard/roms.zip";
    private final String DownUrl2 = "http://gobeyondtv.co/wizard/roms-wiatec.zip";

	public static Update instance;
	public int m_AddonOption;
    /// setup type
    private DownloadType m_DownloadType = DownloadType.NONE;
    private boolean m_IsConnected = false;
    private boolean m_Verify = false;
    public static boolean stopdownload=false;
	private ArrayList<Integer> m_Ids = new ArrayList<Integer>();
	private ArrayList<String> m_Countries = new ArrayList<String>();
	private ArrayList<String> m_Urls = new ArrayList<String>();
	private ArrayList<Double> m_Versions = new ArrayList<Double>();
	private ArrayList<String> m_Actives = new ArrayList<String>();
    /// our kodi specific stuff
    KodiEnvironment m_kodiEnv;
	private String[] downurlarrays;
	private String[] wherefilesaves;
    public int totalprograssvalue=0;
    public int nowprograssvalue=0;
    public String filehead;
    Util m_util;
    public int index=1;
    // label state
    int m_progressType=0;
    public int finish1=0;
    
    public TasksCompletedView mTasksView;
    
    
    private TextView version_name;
    public Button m_DownloadBtn;
    private TextView m_WorkTypeTV;//提示文案
    private TextView current_version;
    public TextView download_tip; 
    public DotsTextView dots_textView;
    private boolean m_Downloading = false;
    private String currentVersion;
    public int finishcount=0;
    
    public boolean isDownloadAgain = false;
    private boolean installAll = false;
    
    public NewVersionInfoEntity entity;
    
    private PackageManager packageManager;
    
    private static int[] ErrorMsg = {
    	R.string.deploy_fail,
    	R.string.Error_1, 
    	R.string.Error_2,
    	R.string.Error_3,
    	R.string.Error_4,
    	R.string.Error_5,
    	R.string.Error_6,
    	R.string.Error_7,
    };

	//px--------------------------------------------------------------------------------------------
	private RollPagerView rollView;
	private MarqueeView tv_Marquee;
	private SharedPreferences sharedPreferences;
	private String localLanguage;
	private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main3);

		//px----------------------------------------------------------------------------------------
		rollView = (RollPagerView) findViewById(R.id.rollView);
		tv_Marquee = (MarqueeView) findViewById(R.id.tv_Marquee);
		sharedPreferences = getSharedPreferences("language" ,MODE_PRIVATE);
		localLanguage = sharedPreferences.getString("language" ,"");
		if("".equals(localLanguage)){
			downloadUrl = Constant.VERSIONURL;
		}else if(getString(R.string.english).equals(localLanguage)){
			downloadUrl = Constant.VERSIONURL;
		}else if(getString(R.string.italian).equals(localLanguage)){
			downloadUrl = Constant.ITALIAN_VERSION_URL;
		}else if(getString(R.string.spanish).equals(localLanguage)){
			downloadUrl = Constant.SPANISH_VERSION_URL;
		}else if(getString(R.string.korea).equals(localLanguage)){
			downloadUrl = Constant.KOREA_VERSION_URL;
		}else if(getString(R.string.chinese_tw).equals(localLanguage)){
			downloadUrl = Constant.CHINESE_TW_VERSION_URL;
		}else if(getString(R.string.chinese).equals(localLanguage)){
			downloadUrl = Constant.CHINESE_VERSION_URL;
		}else {
			downloadUrl = Constant.VERSIONURL;
		}
		Log.d("----px----" , downloadUrl);
		//---

        packageManager = getPackageManager();
        filehead=Util.getFileHead();
        instance = this;
        m_AddonOption = 0;
        
        m_DownloadBtn = (Button)this.findViewById(R.id.download_btn);
        m_DownloadBtn.setOnClickListener(DownloadListener);
        UpdateDownloadBtn(false);
        
        // detect XBMC/Kodi
        m_kodiEnv = new KodiEnvironment(this);
        m_util = new Util(this);
        
        version_name = (TextView) findViewById(R.id.version_name);
        mTasksView  = (TasksCompletedView) findViewById(R.id.tasks_view);
        m_WorkTypeTV = (TextView)findViewById(R.id.work_type);
        download_tip = (TextView) findViewById(R.id.download_tip);
        download_tip.setText("Checking update");
        m_WorkTypeTV.setText("");
        dots_textView = (DotsTextView) findViewById(R.id.dots_textView);
        dots_textView.showAndPlay();
        
        try {
			PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(), 0);
			String versionName = packInfo.versionName;
			version_name.setText(versionName);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
        
        if(!m_util.CheckNetwork(true)){
        	download_tip.setText(R.string.msg_nowifi);
        	dots_textView.setVisibility(View.GONE);
        }else{
        	if(Constant.MACAUTCH){
        		String macAdress = Util.getMacAddress();
            	macAdress = macAdress.replaceAll(":", "");
            	Log.e("macAdress", macAdress);
            	NetHttpData.getHttpDao().matchMacAdress(macAdress, new AsyncHttpResponseHandler() {
        			@Override
        			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        				String str = new String(responseBody);
        				str = str.substring(str.lastIndexOf("=")+1, str.length());
        				Log.e("result", str);
        				if("OK".equals(str)){
        					checkVersion(downloadUrl);
//        					NoIdentify();
//        		    		if (m_IsConnected == false) {
//        		    			AlertDialog.Builder gg = new AlertDialog.Builder(Update.this);
//        		    			gg.setMessage(R.string.msg_nowifi);
//        		    			gg.show();
//        		    		}
        				}else{
        					showDialog(Update.this,R.string.msg_nomatchmac);
        				}
        			}

        			@Override
        			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
        				Log.e("result", arg3.toString());
        					showDialog(Update.this,R.string.msg_checked_fail);
        			}
        		});
        	}else{
        		checkVersion(downloadUrl);
        	}
        }
        //BTV2016020201-a0bc123f7d
        currentVersion = CacheUtils.getString(this, "current_version", "BG5D1-9d608dc985");//BTV2016030201
        current_version = (TextView) findViewById(R.id.current_version);
        current_version.setText("Current Version:" + currentVersion.substring(0, currentVersion.lastIndexOf("-")));
        //current_version.setText("Current Version:" + currentVersion);
    }

	//px--------------------------------------------------------------------------------------------
	@Override
	protected void onStart() {
		super.onStart();
		m_DownloadBtn.requestFocus();
		showRollView();
		showMarquee();
	}

	@Override
    protected void onResume()
    {
    	if (!m_Downloading)
    		m_kodiEnv.DetectEnvironment();
    	Log.v("ButtonAnable", m_Verify+":"+m_Downloading+":"+isInstalled());
    	if (m_Downloading)
    		m_kodiEnv.StartDeployment();
        stopdownload=false;
       	super.onResume();
    }
    @Override
    protected void onPause() 
    {
    	if (m_Downloading)
    		m_kodiEnv.StopDeployment();
    	super.onPause();
    }
    
    
    @Override
    protected void onStop() {
    	super.onStop();
    }
    
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			AlertDialog.Builder builder = new AlertDialog.Builder(Update.this);  
	        builder.setTitle("Warning"); 
	       // builder.setIcon(R.drawable.ic_launcher);
	        builder.setMessage(R.string.msg_exit_update);  
	        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					if(task != null){
						Log.e("task.getStatus", task.getStatus()+"");
					}
					if(task!=null && task.getStatus() == Status.RUNNING){
			    		task.cancel(true);
			    	}
					Update.this.finish();
				}
			});
	        builder.setPositiveButton("Cancel",new DialogInterface.OnClickListener() {
	        	
	                public void onClick(DialogInterface dialog, int whichButton) {  
	                	dialog.dismiss();  
	           }  
	        });  
	        builder.setCancelable(true);
	        builder.show();
		}
		return super.onKeyUp(keyCode, event);
	}
    
    
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // close the log and cleanup
        m_kodiEnv.CancelDeployment();
        m_kodiEnv.Cleanup(false);
    }
    
    public void onLowMemory() {
    	super.onLowMemory();
    	m_kodiEnv.CancelDeployment();
    }
    /**
     * cancel our downloading.
     */
    public void onBackPressed()
    {
        // will tell the async task to exit.
        m_kodiEnv.CancelDeployment();
        // exit now
        super.onBackPressed();
    }
    
    public void onInstallKodi()
    {
    	ProgressBar pb = (ProgressBar)findViewById(R.id.work_prog);
    	pb.setVisibility(View.INVISIBLE);
    	
    }
    public void onInstallAddon()
    {
    	m_DownloadBtn.setEnabled(false);
    }
    public void onDownloadFile()
    {
    	m_DownloadBtn.setEnabled(false);
    }
    /**
     * Start the async task that will download and extract the settings zip. tie the operations to the progress bar
     */
    private void checkVersion(String url1){
    	m_DownloadBtn.setEnabled(false);
    	new AsyncHttpClient().get(url1, new TextHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, String arg2) {
				Log.e("WECAN", "DD");
				try {
					JSONObject jobj=new JSONObject(arg2);
					entity = new NewVersionInfoEntity();
					entity.setMyversion(currentVersion);
					if(jobj.get("version")!=null){
						entity.setVersion(jobj.get("version").toString());
					}
					if(jobj.get("downloadUrl")!=null){
						entity.setDownloadUrl(jobj.getString("downloadUrl").toString());
					}
					if(jobj.get("verinclude")!=null){
						entity.setVerinclude(jobj.getString("verinclude").toString());
					}
					if(jobj.get("verincludeall")!=null){
						entity.setVerincludeall(jobj.getString("verincludeall").toString());
						//Log.e("verincludeall", jobj.getString("verincludeall").toString());
					}
						
					if(entity.isLastVersion()){
						Log.v("SRXFTY", "New");
						m_WorkTypeTV.setText("The current version is the latest!");
						m_DownloadBtn.setEnabled(false);
					}else{
						m_DownloadBtn.setEnabled(true);
						m_WorkTypeTV.setText("New Version : " + entity.getVersion().substring(0, entity.getVersion().lastIndexOf("-")));
						String[] temp = entity.getVerinclude().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
						List list = Arrays.asList(temp);
						for (int i = 0; i < temp.length; i++) {
							Log.e("temp", temp[i]);
							Log.e("list", list.get(i)+"");
						}
						Log.e("版本是否包含", list.contains(entity.getMyversion())+"");
						if(!list.contains(entity.getMyversion())){//应该下载整个包
							installAll = true;
						}
					
					}
					download_tip.setText("");
					dots_textView.hideAndStop();
					dots_textView.setVisibility(View.GONE);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onFailure(int arg0, Header[] arg1, String arg2, Throwable arg3) {
				Log.e("onFailure", arg3.toString());
			}
		});
    }
    
    private void startDeployment(String url1, String url2)
    {
        // show the progress bar
        m_kodiEnv.DeploySettingsFile(url1, url2);
    }
    
    private void NoIdentify() {    	
    	m_IsConnected = m_util.CheckNetwork(false);
        
        String address = Util.getMacAddress();
        if (address.endsWith("N/A")) {
        	m_IsConnected = false;
        	return;
        }

        m_Verify = true;
        m_Ids.add(1);
        m_Countries.add("American Version");
        m_Urls.add("http://gobeyondtv.co/wizard/backup.zip");
        m_Versions.add(1.1);
        m_Actives.add("Y");
    }
 
    /**
     * First int is the type (download/extract). second is the progress itself.
     * @param progress A tuple of ints
     */
    public void onProgressUpdate(Integer... progress)
    {
        // based on the type of work, set the label text (only when changes otherwise we have lots of layout calcs.)
        int progressType = progress[0];
        if (progressType != m_progressType)
        {
            if (progressType == SettingsDownloader.PROGRESS_DOWNLOAD) {
            	m_WorkTypeTV.setText(R.string.work_type_downloading);
            } else if (progressType == SettingsDownloader.PROGRESS_EXTRACT) {
            	m_WorkTypeTV.setText(R.string.work_type_extracting);
            }
            m_progressType = progressType;
        }

        // update progress
        //UpdateProgress(progress[1]);
    }

    /**
     * Called on the UI thread when everything finishes
     * @param bSuccess True if deployment succeeded
     */
	public void DeploymentFinished(boolean bSuccess, int errorCode) {
        // set result label based on if we succeeded
		int code = errorCode;
		if (code < 0 )
			code = 0;
		else if (code > ErrorMsg.length - 1)
			code = ErrorMsg.length - 1;

		//px----------------------------------------------------------------------------------------
		FavoriteManager.restore();
		
        m_WorkTypeTV.setText(bSuccess ? R.string.deploy_success : ErrorMsg[code]);
    	m_WorkTypeTV.setTextColor(Color.parseColor(bSuccess ? "#ff77ff79" : "#ffff0000"));

        UpdateDownloadBtn(true);
        m_progressType = 0;
        m_Downloading = false;

        // cleanup log, etc.
        m_kodiEnv.Cleanup(bSuccess);

    }

	/**
     * Easily detect if anything is installed
     * @return
     */
    public boolean isInstalled()
    {
        return m_DownloadType == DownloadType.KODI || m_DownloadType == DownloadType.XBMC;
    }
    public boolean isDownloading() {
    	return m_Downloading;
    }

    /**
     * easily understood
     * @return
     */
    public DownloadType getDownloadType()
    {
        return m_DownloadType;
    }
    
    public void setDownloadType(DownloadType type)
    {
    	m_DownloadType = type;
    }
    
	private boolean Uninstall() {
		try
		{
			String apk = "";
			if (m_DownloadType == DownloadType.KODI)
				apk = Update.KodiPackage;
			else if (m_DownloadType == DownloadType.XBMC)
				apk = Update.XbmcPackage;
			
			if (apk.length() == 0)
				return false;
			
			PackageManager pm = getPackageManager();
			if (pm == null)
				return false;
			
			PackageInfo pi = pm.getPackageInfo(apk, PackageManager.GET_ACTIVITIES);
			if (pi == null)
				return false;

			String package_name = pi.packageName;
			Uri uri_path = Uri.fromParts("package", package_name, null);
			Intent intent = new Intent(Intent.ACTION_DELETE, uri_path);
			
		    Update.instance.startActivityForResult(intent, 0);
		}
		catch (Exception e)
		{
			//Log.e("Uninstall Kodi/Xbmc", "Error: "+e.toString());
		}
		
		return true;
	 }
	
	/*private void UpdateDownloadBtn() {
		m_DownloadBtn.setEnabled(m_Actives.get(0).equalsIgnoreCase("Y"));
		//检测mac是否链接 测试时可以注销不影响测试
	}*/

	/////////////////////////////////////////////////////////////
	/////////////////ButtonListener/////////////////////////////
	////////////////////////////////////////////////////////////
	private OnClickListener InstallListener = new OnClickListener() {
	    public void onClick(View v) {
	    	if (!m_IsConnected)
	    		return;
	    	
	    	if (!isInstalled()) {
	    		
	    		setDownloadType(DownloadType.KODI);
	    		
	    		startDeployment(KodiUrl, "");
	    		//testCopy(Update.pathSD + "org.xbmc.kodi/files/.kodi", true);
	    	}
	    	else
	    		Uninstall();
	    		//testCopy(Update.pathSD + "org.xbmc.kodi/files/.kodi", true);
	    }
	 };
	private OnClickListener DownloadListener = new OnClickListener() {
	    public void onClick(View v) {
	    	Log.e("aaaa","aaaaaaa");
//	    	DownloadProc();
	    	downloadFile();
	    }
	 };
	public void UpdateDownloadBtn(boolean enable) {
		m_DownloadBtn.setEnabled(enable);
	}
	
	/*private void UpdateProgress(int progress) {
		UpdateProgress(progress, true);
	}
	
	private void UpdateProgress(int progress, boolean enable) {
		m_DownloadProgress.setProgress(progress);
		UpdateProgress(enable);
	}
	
	private void UpdateProgress(boolean enable) {
		m_DownloadProgress.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
	}*/
	
	private void DownloadProc() {
		
		Log.e("bbbb", m_IsConnected+"");
		
    	if (!m_IsConnected)
    		return;

    	if (m_DownloadType == DownloadType.KODI)
    		m_util.KillProcess(KodiPackage);
    	else if (m_DownloadType == DownloadType.XBMC)
    		m_util.KillProcess(XbmcPackage);
		
    	UpdateDownloadBtn(false);
    	setDownloadType(DownloadType.Addon);
    	startDeployment(DownUrl1, DownUrl2);
	}
	
	
	private void testCopy(String destDir, boolean backup) {
		String userPath = destDir + "/userdata/";
		File userFile = new File(userPath);
		String favDir = userPath + Update.FavouriteFile;
		File favFile = new File(favDir);
		if (userFile.exists()) {
			if (backup && favFile.exists())
				m_util.CopyFile(userPath, Update.FavouriteFile, Update.pathSD);
			else 
				m_util.CopyFile(Update.pathSD, Update.FavouriteFile, userPath);
		}

	}
	
	
	DownLoaderTask task;
	
	private int getTotalSize(String[] downurls) {
		int totalSize = 0;
		HttpURLConnection connection = null;
		for(int i = 0;i<downurls.length;i++){
			try {
				URL mUrl = new URL(downurls[i]);
				Log.e("mUrl", mUrl+"");
				connection = (HttpURLConnection) mUrl.openConnection();
				connection.setRequestMethod("GET");
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(5000);
				Log.e("ResponseCode", connection.getResponseCode()+"");
				if(connection.getResponseCode() == HttpStatus.SC_OK){
					int length = connection.getContentLength();
					totalSize += length;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.e("totalSize1111", totalSize+"");
		return totalSize;
	}
	
	
	private void downloadFile(){
		UpdateDownloadBtn(false);
		
		
		if(installAll){
			downurlarrays = entity.getNeeddownloadAllUrl();
			wherefilesaves = entity.getAllWhereFileSave();
		}else{
			downurlarrays = entity.getNeeddownloadUrl();
			wherefilesaves = entity.getWhereFileSave();
		}
		
		for(int i=0;i<downurlarrays.length;i++){
			Log.e("NeeddownloadUrl["+i+"]", downurlarrays[i]);
			Log.e("WhereFileSave["+i+"]", wherefilesaves[i]);
		}
		
		//final String[] downurlarrays1 = new String[]{downurlarrays[0],downurlarrays[1]};
		//String[] wherefilesaves1 = new String[]{wherefilesaves[0],wherefilesaves[1]};
		
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				int mTotalSize = getTotalSize(downurlarrays);
				
				Message msg = handler.obtainMessage();
				msg.arg1 = mTotalSize;
				msg.sendToTarget();
			}
		}).start();
		
	}
	
	
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			// update.mTasksView.setTotalProgress(msg.arg1);
			if(task!=null && !task.isCancelled() && !isDownloadAgain){
				return;
			}
			
			//String[] downurlarrays1 = new String[]{downurlarrays[0],downurlarrays[1]};
			//String[] wherefilesaves1 = new String[]{wherefilesaves[0],wherefilesaves[1]};
			
			task = new DownLoaderTask(downurlarrays, wherefilesaves, Update.this,msg.arg1,installAll);
			task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,downurlarrays.length);
		};

	};
	
	
	private void showDialog(Context context,int msgNomatchmac) {  
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
        builder.setTitle("Warning"); 
       // builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(msgNomatchmac);  
        builder.setPositiveButton("Exit",new DialogInterface.OnClickListener() {
        	
                public void onClick(DialogInterface dialog, int whichButton) {  
                        Update.this.finish();  
           }  
        });  
        builder.setCancelable(false);
        builder.show();  
    }

	//px--------------------------------------------------------------------------------------------
	private void showRollView(){
		JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(F.url.image, new Response.Listener<JSONArray>() {
			@Override
			public void onResponse(JSONArray response) {
				try {
					if(response != null &&response.length()>0){
						List<RollViewInfo> list = new ArrayList<RollViewInfo>();
						for (int i = 0; i < response.length() ; i++) {
							JSONObject j = response.getJSONObject(i);
							RollViewInfo r = new RollViewInfo();
							r.setUrl(j.getString("url"));
							r.setLink(j.getString("link"));
							list.add(r);
						}
						RollViewAdapter adapter = new RollViewAdapter(list);
						rollView.setAdapter(adapter);
						rollView.setPlayDelay(6000);
						rollView.setHintView(null);
					}} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
		jsonArrayRequest.setTag("RollViewInfo");
		com.wiatec.PX.Application.getVolleyRequestQueue().add(jsonArrayRequest);
	}

	//px--------------------------------------------------------------------------------------------
	private void showMarquee(){
		JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(F.url.marquee, null, new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					if(response!= null){
						tv_Marquee.setBackgroundColor(Color.argb(60,0,0,0));
						tv_Marquee.setText("                                                                         " +
								"                                                                                    " +
								"                                                                                    " +
								"                         "+response.getString("content"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {

			}
		});
		jsonObjectRequest.setTag("MarqueeInfo");
		com.wiatec.PX.Application.getVolleyRequestQueue().add(jsonObjectRequest);
	}

}

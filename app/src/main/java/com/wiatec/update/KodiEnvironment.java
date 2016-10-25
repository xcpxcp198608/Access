package com.wiatec.update;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.os.Environment;
import java.io.File;

/**
 * Used to detect the environment (XBMC/Kodi or nothing) and to run the download process
 */
public class KodiEnvironment
{
    
    /// represents our context (usually the activity)
    private Context m_ctx;

    /// kodi/xbmc home dir
    private String m_kodiHome;
    private String m_downloadPath;

    /// log file. for our simple purpose we dont need a logging framework, yet.
    //private File m_log;
    //private FileWriter m_logWriter;

    /// our downloader instance. used to start and cancel
    private SettingsDownloader m_downloader;


    /**
     * main ctor
     * @param ctx Our app context
     */
    public KodiEnvironment(Context ctx)
    {
        m_ctx = ctx;
    }


    public void OpenLog()
    {
    	/*
        if (m_logWriter != null)
        {
            return;
        }

        String logPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/kd_log_" + android.os.Process.myPid() + ".txt";
        m_log = new File(logPath);
        try
        {
            m_logWriter = new FileWriter(m_log);
            //Log.i("KodiEnvironmentLogger", "Opened log file: "+logPath);
        }
        catch (IOException e)
        {
            //Log.e("KodiEnvironmentLogger", "Cannot open log for writing:  "+logPath);
        }
        */
    }

    /**
     * Detect what is installed (Kodi or XBMC) 检测环境
     */
    public void DetectEnvironment()
    {
        OpenLog();
        Update activity = (Update)m_ctx;
        activity.setDownloadType(Update.DownloadType.NONE);
        m_kodiHome = "";
        try
        {
            // attempt to detect userdata directory for Kodi
            String kodiHomePath = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/org.xbmc.kodi");
            //m_logWriter.write("Testing Kodi home at: " + kodiHomePath+"\n");
            File fKodiHome = new File(kodiHomePath);
            if (fKodiHome.exists() && fKodiHome.isDirectory()) {
                //m_logWriter.write("Found Kodi via userdata dir!\n");
                activity.setDownloadType(Update.DownloadType.KODI);
            }
            else
            {
                // attempt detection via package manager
                //m_logWriter.write("Attempting to detect Kodi via PackageManager\n");
                detectPackage(Update.KodiPackage, Update.DownloadType.KODI);
            }

            // attempt to detect XBMC
            String xbmcHomePath="";
            if (activity.getDownloadType() == Update.DownloadType.NONE)
            {
                // attempt to detect userdata directory for XBMC
                xbmcHomePath = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/org.xbmc.xbmc");
                //m_logWriter.write("Testing XBMC home at: " + xbmcHomePath +"\n");
                File fXbmcHome = new File(xbmcHomePath);
                if (fXbmcHome.exists() && fXbmcHome.isDirectory()) {
                    //m_logWriter.write("Found XBMC via userdata dir!\n");
                    activity.setDownloadType(Update.DownloadType.XBMC);
                }
                else
                {
                    // attempt to detect XBMC package
                    //m_logWriter.write("Attempting to detect XBMC via PackageManager\n");
                    detectPackage(Update.XbmcPackage, Update.DownloadType.XBMC);
                }
            }

            if (activity.getDownloadType() == Update.DownloadType.NONE)
            {
                //m_logWriter.write("Error: Could not locate XBMC or Kodi installed\n");
            }

            // set the Kodi/XBMC home. assumes we're running on a relatively new OS that has a files dir under the Kodi package storage
            if (activity.getDownloadType() == Update.DownloadType.KODI) {
                m_kodiHome = kodiHomePath+"/files/.kodi";
            } 
            else if (activity.getDownloadType() == Update.DownloadType.XBMC) 
            {
                m_kodiHome = xbmcHomePath+"/files/.xbmc";
            }
            else
            {
            	m_kodiHome = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/download"); 
            }
            	
            //m_logWriter.write("Setting Kodi/XBMC home to: "+m_kodiHome+"\n");
            //m_logWriter.flush();
        }
        catch(Exception e)
        {
            //Log.e("KodiEnvironmentLogger", e.toString());
        }
    }


    /**
     * Attempt to detect from package manager
     * @param pkgStr Package name
     * @param type Wanted setup type
     */
    private void detectPackage(String pkgStr, Update.DownloadType type)
    {
        String logName = (type == Update.DownloadType.KODI) ? "Kodi" : "XBMC";
        try
        {
            PackageManager pm = m_ctx.getPackageManager();
            try
            {
                // nested try so our entire code block doesn't end if it isn't found in PM
                PackageInfo pi = pm.getPackageInfo(pkgStr, PackageManager.GET_ACTIVITIES);
                //m_logWriter.write("Found "+logName+" package: "+pi.packageName+", ver code: "+pi.versionCode+", ver name: "+pi.versionName+"\n");
                ((Update)m_ctx).setDownloadType(type);
            }
            catch(PackageManager.NameNotFoundException e)
            {
                //m_logWriter.write("Cannot find "+logName+" package: "+e.toString()+"\n");
            }
        }
        catch(Exception e)
        {
            //Log.e("KodiEnvironmentLogger", e.toString());
        }
    }

     /**
     * used to close our log. if everything was successful we'll also delete it
     */
    public void Cleanup(boolean bSuccess)
    {
    }

    /**
     * retrieve the settings and extract them. this should only be called from the UI thread
     */
    public void DeploySettingsFile(String url1, String url2)
    {
        // simple launch, download and extract
        if (m_downloader == null)
            m_downloader= new SettingsDownloader(m_ctx, false);

        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        m_downloader.execute(url1, url2, m_kodiHome, path + "/roms");            
    }

    /**
     * notify our downloader to stop execution
     */
    public void CancelDeployment()
    {
        if (m_downloader != null)
        {
            m_downloader.cancel(true);
            m_downloader = null;
        }
    }
    public void StopDeployment() 
    {
    	if (m_downloader != null)
    		m_downloader.DownloadStop(true);
    }
    public void StartDeployment() 
    {
    	if (m_downloader != null)
    		m_downloader.DownloadStop(false);
    	
    }

}
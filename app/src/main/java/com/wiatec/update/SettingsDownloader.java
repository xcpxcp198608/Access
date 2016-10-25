package com.wiatec.update;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.Formatter;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * The downloader file. Takes a String as URL, updates with integer (0-100) and the result is the path of the downloaded file
 */
public class SettingsDownloader extends AsyncTask<String, Integer, Boolean>
{
    /// our progress types, passed in the progress update functions
    public final static int PROGRESS_DOWNLOAD = 1;
    public final static int PROGRESS_EXTRACT = 2;

    // maximum wakelock time in ms
    public final static int FOUR_MINUTES = 4 * 60 * 1000;

    /// our context
    Context m_ctx;

    int m_ErrorCode;
    /// log writer
    //FileWriter m_logWriter;
    private boolean m_DownloadStop = false;
	public static int mContentLength;
	public static long mTotalRead;
	private static int mPrecent;
    
    public SettingsDownloader(Context ctx, boolean stop)
    {
        m_ctx = ctx;
        m_DownloadStop = stop;
        //m_logWriter = logWriter;
    }
    public void DownloadStop(boolean enable) {
    	m_DownloadStop = enable;
    }

    @Override
    protected void onPreExecute()
    {
    }

    @Override
    protected Boolean doInBackground(String... strParams)
    {
        Boolean result = Boolean.FALSE;

        if (isCancelled())
        	return result;
        
        // get a wakelock to hold for the duration of the background work. downloading
        // may be slow. extraction usually isn't too slow but also takes a bit of time. limit the wakelock's time!
        PowerManager powerManager = (PowerManager)m_ctx.getSystemService(Context.POWER_SERVICE);
        WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SettingsDownloaderWakeLock");
        wakeLock.acquire(FOUR_MINUTES);

        Update update = ((Update)m_ctx);
        // download the file
        String filename1 = downloadFile(strParams[0], 0, true);
        String filename2 = downloadFile(strParams[1], 100, true);
        if (filename1 != null)
        {
            Update.DownloadType type = update.getDownloadType(); 
            
            if (type == Update.DownloadType.Addon)
            	result = InstallAddon(filename1, strParams[2], true, 0);
            //else if (type == Update.DownloadType.XBMC || type == Update.DownloadType.KODI)
            //	result = InstallKodi(filename1, strParams[2]);
        }
        
        if (filename2 != null)
        {
            Update.DownloadType type = update.getDownloadType(); 
            if (type == Update.DownloadType.Addon)
            	result = InstallAddon(filename2, strParams[3], true, 100);
        }

        // if our max time hasn't passed but work is done or an error occurred we bail out and release
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
        return result;
    }

    /**
     * Updating the UI
     * @param progress
     */
    protected void onProgressUpdate(Integer... progress)
    {
        // lovely cast we don't like. update the activity
        Update activity = (Update)m_ctx;
        activity.onProgressUpdate(progress);
        //activity.m_Precent.setText(mPrecent +"%");
        //activity.m_Current_Total.setText(formatFileSize(m_ctx,mTotalRead)+"/"+ formatFileSize(m_ctx,mContentLength));
    }

    /**
     * Called in the UI thread after the task finished
     * @param result
     */
    @Override
    protected void onPostExecute(Boolean result)
    {
        // @todo: make an interface instead of a cast
        Update activity = (Update)m_ctx;
        activity.DeploymentFinished(result, m_ErrorCode);
    }

    /**
     * called on the UI thread after cancellation and background work has stopped
     */
    protected void onCancelled()
    {
        Update activity = (Update)m_ctx;
        activity.DeploymentFinished(false, m_ErrorCode);
    }

    /**
     * Download the file and save it to our files dir
     * @param strURL URL of file to DL
     * @return Full path tyo the settings ZIP file
     */
    private String downloadFile(String strURL, int index, boolean delete)
    {
        int npos = strURL.lastIndexOf('/');
        if (npos == strURL.length()-1) { 
        	m_ErrorCode = 1;
            return null;
        }

        String filename = strURL.substring(npos+1);
        try {
            // setup streams to download and write to file
            String outputFilePath = Update.pathSD + filename;
            File fSettingsZip = new File(outputFilePath);
            if (fSettingsZip.exists()) {    // todo: do we want this configurable?
                //m_logWriter.write("Deleting old file: "+outputFilePath+"\n");
            	if (!delete)
            		return outputFilePath;
            	
            	fSettingsZip.delete();
            	//return outputFilePath;
            }

           publishProgress(PROGRESS_DOWNLOAD, (int)(0.5 * index));
           //m_logWriter.write("Trying URL: "+strURL+", filename: "+filename+"\n");
            //m_logWriter.flush();
            URL url = new URL(strURL);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0(Linux; U; Android 2.2; en-gb; LG-P500 Build/FRF91) AppleWebKit/533.0 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
            conn.connect();

            mContentLength = conn.getContentLength();

            InputStream is = new BufferedInputStream(url.openStream(), 8192);
            OutputStream os = new BufferedOutputStream(new FileOutputStream(outputFilePath));
            byte data[] = new byte[1024];
            mTotalRead = 0;
            int read=0;
            //while ((read = is.read(data)) != -1)
            while (true) {
                if (isCancelled()) {
                    outputFilePath = null;
                    m_ErrorCode = 2;
                    break;
                }
                
                if (m_DownloadStop)
                	continue;
                
                if ((read = is.read(data)) == -1)
                	break;

                mTotalRead += read;

                // progress if we know the content length
                if (mContentLength > 0)
                    publishProgress(PROGRESS_DOWNLOAD, (int)(0.5 * (index + mTotalRead*100/mContentLength)));
                	mPrecent = (int)mTotalRead*100 / mContentLength;
                // write to file
                os.write(data, 0, read);
            }

            // close and get ready to bail
            os.flush();
            os.close();
            is.close();
            filename = outputFilePath;
        }
        catch(Exception e) {
            filename = null;
            m_ErrorCode = 3;
        }

        return filename;
    }

    /**
     * Extract the settings zip to the wanted location
     * @param fSettingsZip The zip file
     * @return True if extraction was successful
     */
    private boolean extractSettingsZip(File fSettingsZip, String destDir, int index)
    {
        boolean result = false;
        try
        {
            //m_logWriter.write("Unzipping to destination: "+destDir+"\n");
            //m_logWriter.flush();
        	boolean backFavFile = false;
        	File dest = new File(destDir);
        	if (dest.exists()) {
        		String userPath = destDir + "/userdata/";
        		File userFile = new File(userPath);
        		String favDir = userPath + Update.FavouriteFile;
        		File favFile = new File(favDir);
        		if (userFile.exists() && favFile.exists()) {
        			CopyFile(userPath, Update.FavouriteFile, Update.pathSD);
        			backFavFile = true;
        		}
        		
        		DeleteDir(destDir);
        	}
        	else
        		dest.mkdirs();
        	
            publishProgress(PROGRESS_EXTRACT, (int)(0.5 * index));
            // open the zip
            ZipFile zip = new ZipFile(fSettingsZip);
            int count=0;
            int zipSize = zip.size();
            Enumeration<? extends ZipEntry> entries = zip.entries();
            //while(entries.hasMoreElements())
            while (true)
            {
                if (isCancelled()) {
                	result = false;
                	m_ErrorCode = 4;
                    break;
                }

                if (m_DownloadStop)
                	continue;
                
                if (!entries.hasMoreElements())
                	break;
                
                // todo: update progress
                ZipEntry ze = (ZipEntry)entries.nextElement();
                count++;
                String entryName = ze.getName();
                String destFullpath = destDir+"/"+entryName;
                //m_logWriter.write("Extracting: "+destFullpath+"\n");
                File fDestPath = new File(destFullpath);
                if (ze.isDirectory())
                {
                    fDestPath.mkdirs();
                    publishProgress(PROGRESS_EXTRACT, (int)(0.5 * (index + count*100/zipSize)));
                    continue;
                }
                fDestPath.getParentFile().mkdirs();

                // write file
                try {
                    InputStream is = zip.getInputStream(ze);
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destFullpath));
                    int n=0;
                    byte buf[] = new byte[4096];
                    while((n = is.read(buf, 0, 4096)) > -1)
                    {
                        bos.write(buf, 0, n);
                    }
                    // close
                    is.close();
                    bos.close();
                } catch(IOException ioe) {
                	m_ErrorCode = 5;
                    //m_logWriter.write("Could not write, error: "+ioe.toString());
                }

                // update progress
                //publishProgress(PROGRESS_EXTRACT, (count*100/zipSize));
            }

            // close zip and bail
            zip.close();
            //m_logWriter.write("Successfully extracted: "+fSettingsZip.getName()+"\n");
            //m_logWriter.flush();
            if (backFavFile) {
        		String userPath = destDir + "/userdata/";
        		File userFile = new File(userPath);
        		if (!userFile.exists())
        			userFile.mkdirs();
        		
        		File inputFile = new File(Update.pathSD + Update.FavouriteFile);
        		if (inputFile.exists())
        			CopyFile(Update.pathSD, Update.FavouriteFile, userPath);
            }
            
            result = !isCancelled();
        }
        catch(Exception e)
        {
            //Log.e("SettingsDownloader", "Error: "+e.toString());
            result = false;
            m_ErrorCode = 6;
        }

        return result;
    }
    private void DeleteDir(String path) {
    	
	   File file = new File(path);
	   File[] childFileList = file.listFiles();
	   for(File childFile : childFileList)
	   {
	       if(childFile.isDirectory())
	           DeleteDir(childFile.getAbsolutePath());      
	       else
	           childFile.delete();    
	   }
	   
	   file.delete();     
    }
    public void CopyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath); 
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);        
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;        

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
                catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private Boolean InstallAddon(String filename, String destDir, boolean delete, int index)
    {
    	boolean bSuccess = true;
        File fSettingsZip = new File(filename);
        if (fSettingsZip.exists() && !isCancelled())
        {
            try
            {
                //m_logWriter.write("Successfully downloaded to: "+filename+"\n");
                // extract to wanted directory
                bSuccess = extractSettingsZip(fSettingsZip, destDir, index);
                // delete settings zip
                if (bSuccess == false || (delete == true && fSettingsZip.exists())) {
                	fSettingsZip.delete();
                }
                
                ((Update)m_ctx).onInstallAddon();
            }
            catch(Exception e)
            {
                //Log.e("SettingsDownloader", "Error: "+e.toString());
            	m_ErrorCode = 7;
            }
        }


        return (bSuccess ? Boolean.TRUE : Boolean.FALSE);
    }
    private Boolean InstallKodi(String file, String destDir)
    {
		if(file.length() == 0)
			return Boolean.FALSE;
		
    	boolean bSuccess = true;
        File fSettingsZip = new File(file);
        if (fSettingsZip.exists() && !isCancelled())
        {
			Intent intent = new Intent(Intent.ACTION_VIEW);
		    intent.setDataAndType(Uri.fromFile(new File(file)), "application/vnd.android.package-archive");
		    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
		    Update.instance.startActivity(intent);
		    //Update.instance.startActivityForResult(intent, 0);
		    
		    ((Update)m_ctx).onInstallKodi();
        }
        return Boolean.TRUE;

    }
    
    
    private String formatFileSize(Context context,long number){
        return Formatter.formatFileSize(context, number);
    }
}



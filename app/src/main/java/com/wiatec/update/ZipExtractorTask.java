package com.wiatec.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.wiatec.entity.NewVersionInfoEntity;
import com.wiatec.update.utils.CacheUtils;
import com.wiatec.update.utils.Constant;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;

public class ZipExtractorTask extends AsyncTask<Integer, Integer, Long> {
	
	private final String TAG = "ZipExtractorTask";
	private String[] mInput;
	private String[] mOutput;
	private int mProgress = 0;
	//private boolean mReplaceAll;
	private Context mContext;
	private Update update;
	private long mExtractedSize;
	private long totalZipSize;
	private boolean installAll;
	private boolean[] backFavFile ;
	
	public ZipExtractorTask(String[] strings, String[] out, Context context,boolean installAll){
		super();
		this.mInput = strings;
		this.mOutput = out;
		// mOutput = new File(out);
		// if(!mOutput.exists()){
		// if(!mOutput.mkdirs()){
		// Log.e(TAG, "Failed to make directories:"+mOutput.getAbsolutePath());
		// }
		// }
		//this.mReplaceAll = replaceAll;
		this.mContext = context;
		this.installAll = installAll;
		update = (Update) mContext;
		backFavFile = new boolean[out.length];
	}
	
	@Override
	protected Long doInBackground(Integer... params) {
		Log.e("params", params[0]+"");
		 mExtractedSize = unzip(params[0]);
		 return mExtractedSize;
	}
	
	@Override
	protected void onPostExecute(Long result) {
		update.download_tip.setText("Installed successfully");
		update.download_tip.setTextColor(Color.parseColor("#0B9444"));
		update.dots_textView.hideAndStop();
		update.dots_textView.setVisibility(View.GONE);
		update.m_DownloadBtn.setSelected(true);
		
		//删除解压包
		for(int i =0;i<mInput.length;i++){
			if(installAll){
        		File inputFile = new File(Update.pathSD + Update.FavouriteFile);
        		if (inputFile.exists()){
        			String userPath = mOutput[i] + "userdata/";
            		File userFile = new File(userPath);
            		if (!userFile.exists())
            			userFile.mkdirs();
        			CopyFile(Update.pathSD, Update.FavouriteFile, userPath);
        		}
			}
			
			File file = new File(Util.getFileHead()+mInput[i]+".zip");
			if(file.exists()){
				file.delete();
			}
		}
		Log.e("version", update.entity.getVersion());
		CacheUtils.setString(mContext, "current_version", update.entity.getVersion());
	}
	
	@Override
	protected void onPreExecute() {
		update.download_tip.setText("Installing");
		update.download_tip.setTextColor(Color.WHITE);
		totalZipSize = getZipTotalSize(mInput);
		update.mTasksView.setTotalProgress((int)totalZipSize);
		
		if(installAll){//整包升级先删除目录
			for(int i = 0;i<mOutput.length;i++){
				File dest = new File(mOutput[i]);
				if (dest.exists()) {
					String userPath = mOutput[i] + "userdata/";
					Log.e("userPath", userPath);
					File userFile = new File(userPath);
					String favDir = userPath + Update.FavouriteFile;
					File favFile = new File(favDir);
					if (userFile.exists() && favFile.exists()) {
						CopyFile(userPath, Update.FavouriteFile, Update.pathSD);
						backFavFile[i] = true;
					}else{
						backFavFile[i] = false;
					}
					DeleteDir(mOutput[i]);
				}
				else
					dest.mkdirs();
				Log.e("setbackFavFile["+i+"]", backFavFile[i]+"");
			}
			
		}
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

	private long getZipTotalSize(String[] input) {
		int totalSize = 0;
		for(int i = 0;i<input.length;i++){
			File fileiput;
			ZipFile zip = null;
			try {
				fileiput = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/"+input[i]+".zip");
				zip = new ZipFile(fileiput);
				totalSize += getOriginalSize(zip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Log.e("totalZipSize", totalSize+"");
		return totalSize;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		update.mTasksView.setProgress(values[0]);
		update.download_tip.setText((long)values[0]*100/totalZipSize+"%Installing");
		//Log.e("values[0]", (long)values[0]*100 +"====" +totalZipSize+"==="+((long)values[0])*100 / totalZipSize);
	}
	
	private long unzip(int length){
		long extractedSize = 0L;
		for(int i=0;i<length;i++){
			Enumeration<ZipEntry> entries;
			ZipFile zip = null;
			File input;
			Log.e("mInput[" + i +"]", mInput[i]);
			try {
				input = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Android/data/"+mInput[i]+".zip");
				Log.e("input", input.getAbsolutePath()+"="+input.exists()+"");
				zip = new ZipFile(input);
				//totalSize += getOriginalSize(zip);
				entries = (Enumeration<ZipEntry>) zip.entries();
				while(entries.hasMoreElements()){
					ZipEntry entry = entries.nextElement();
					if(entry.isDirectory()){
						continue;
					}
					File mOut = new File(mOutput[i]);
					File destination = new File(mOut, entry.getName());
					if(!destination.getParentFile().exists()){
						//Log.e(TAG, "make="+destination.getParentFile().getAbsolutePath());
						destination.getParentFile().mkdirs();
					}
					ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
					extractedSize+=copy(zip.getInputStream(entry),outStream);
					outStream.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				if(zip!=null){
					zip.close();
					zip=null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//zip包已经解压完成，此处检测是否有需要删除的文件
			String[] deleteFiles = Util.getDeleteFiles(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.FILEDELTXT);
			if(deleteFiles != null){
				for(int j = 0;j<deleteFiles.length;j++){
					Log.e("需要删除的文件或者文件夹["+j+"]", deleteFiles[j].toString());
					File file = new File(deleteFiles[j]);
					if (file.isFile()) {
			            // 为文件时调用删除文件方法
			                Util.deleteFile(deleteFiles[j]);
			            } else {
			            // 为目录时调用删除目录方法
			            	Util.deleteDirectory(deleteFiles[j]);
			            }
				}
				
				File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Constant.FILEDELTXT);
				if(file.exists()){
					file.delete();
				}
			}
		}
		return extractedSize;
	}

	private long getOriginalSize(ZipFile file){
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) file.entries();
		long originalSize = 0l;
		while(entries.hasMoreElements()){
			ZipEntry entry = entries.nextElement();
			if(entry.getSize()>=0){
				originalSize+=entry.getSize();
			}
		}
		return originalSize;
	}
	
	private int copy(InputStream input, OutputStream output){
		byte[] buffer = new byte[1024*8];
		BufferedInputStream in = new BufferedInputStream(input, 1024*8);
		BufferedOutputStream out  = new BufferedOutputStream(output, 1024*8);
		int count =0,n=0;
		try {
			while((n=in.read(buffer, 0, 1024*8))!=-1){
				out.write(buffer, 0, n);
				count+=n;
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return count;
	}
	
	private final class ProgressReportingOutputStream extends FileOutputStream{

		public ProgressReportingOutputStream(File file)
				throws FileNotFoundException {
			super(file);
		}

		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount)
				throws IOException {
			super.write(buffer, byteOffset, byteCount);
		    mProgress += byteCount;
		    publishProgress(mProgress);
		}
	}
}

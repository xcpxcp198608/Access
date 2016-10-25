package com.wiatec.update;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class DownLoaderTask extends AsyncTask<Integer, Integer, Long> {
	private final String TAG = "DownLoaderTask";
	private String[] downurlarrays;
	private String[] wherefilesaves;
	private int mProgress = 0;
	private Context mContext;
	private int mTotalSize;
	private Update update;
	private int mTotalLength;
	private boolean installAll;
	
	private static final Object monitor = new Object();
	
	public DownLoaderTask(String[] downurlarrays,String[] wherefilesaves,Context context,int mTotalSize,boolean installAll){
		super();
		this.downurlarrays = downurlarrays;
		this.wherefilesaves = wherefilesaves;
		this.mTotalSize = mTotalSize;
		this.mContext = context;
		this.installAll = installAll;
		update = (Update) mContext;
	}

	@Override
	protected void onPreExecute() {
		op(mTotalSize);
		Log.e("onPreExecute", "onPreExecute执行完毕");
	}
	
	private void op(int totalSize) {
		//update.UpdateDownloadBtn(false);
		update.mTasksView.setTotalProgress(totalSize);
		mTotalLength = 0;
		for (int i = 0; i < wherefilesaves.length; i++) {
			File file = new File(Util.getFileHead() + wherefilesaves[i] + ".zip");
			if (file.exists()) {
				Log.e("hasBeanDownloadFileLength", file.length() + "");
				mTotalLength += file.length();
			}
		}

		if (mTotalLength > 0) {
			update.mTasksView.setProgress(mTotalLength);
			update.download_tip.setText((long) mTotalLength * 100 / mTotalSize + "%Downloading");
		} else {
			update.download_tip.setText("Downloading");
		}
		update.download_tip.setTextColor(Color.parseColor("#4c4c4c"));
		update.dots_textView.setVisibility(View.VISIBLE);
		update.dots_textView.showAndPlay();
	}

	@Override
	protected Long doInBackground(Integer... params) {
		Log.e("doInBackground", "doInBackground开始");
		return download(params[0]);
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		update.mTasksView.setProgress(values[0]);
		update.download_tip.setText(((long)values[0])*100/mTotalSize +"%Downloading");
		//Log.e("download values[0]", values[0]+"");
	}

	@Override
	protected void onPostExecute(Long result) {
		//执行此方法说明已经下载完成，在此处对文件进行md5校验
		String fileMD5 = "";
		boolean allFileIsComplete = true;
		boolean[] fileComplete = new boolean[wherefilesaves.length];
		for(int i=0;i<wherefilesaves.length;i++){
			File file = new File(Util.getFileHead()+wherefilesaves[i]+".zip");
			fileMD5 = Util.getMD5(file);
			Log.e(wherefilesaves[i]+"-getFileMD5",fileMD5+"=="+fileMD5.substring(0,10));
			String correctMD5 = wherefilesaves[i].substring(wherefilesaves[i].indexOf("-")+1);
			Log.e(wherefilesaves[i]+"-correctMD5", correctMD5);
			fileComplete[i] =  fileMD5.substring(0, 10).equals(correctMD5);
			if(!fileComplete[i]){
				File mFile = new File(Util.getFileHead()+wherefilesaves[i]+".zip");
				if(mFile.exists()){
					mFile.delete();
				}
			}
			Log.e("fileComplete["+i+"]", fileComplete[i]+"");
			allFileIsComplete &= fileComplete[i];
		}
		
		Log.e("allFileIsComplete", allFileIsComplete+"");
		if(allFileIsComplete){
			String[] desDirs;
			if(installAll){//整包升级
				desDirs = new String[]{Util.getFinalZipHead(),Environment.getExternalStorageDirectory().getAbsolutePath()+"/roms/"};
			}else{
				desDirs = new String[wherefilesaves.length];
				for(int i=0;i<wherefilesaves.length;i++){
					desDirs[i] = Util.getFinalZipHead();
				}
			}
			Log.e("desDirs", desDirs.toString());
			ZipExtractorTask task = new ZipExtractorTask(wherefilesaves,desDirs,mContext,installAll);
			task.execute(wherefilesaves.length);
			
		}else{
			update.UpdateDownloadBtn(true);
			update.dots_textView.setVisibility(View.INVISIBLE);
			update.download_tip.setText("Installation file is incomplete!Please download again.");
			update.download_tip.setTextColor(Color.parseColor("#ffff0000"));
			update.mTasksView.setProgress(0);
			update.isDownloadAgain = true;
		}
		
	}

	private long download(int size){
		
		//========================================
		HttpClient client = null;
		HttpGet request = null;
		//========================================
		
		
		for(int i=0;i<size;i++){
			HttpResponse response = null;
			InputStream is = null;
			RandomAccessFile fos = null;
			ProgressReportingOutputStream output = null;
			client = new DefaultHttpClient();
			try {
				request = new HttpGet(downurlarrays[i]);
				File mFile = new File(Util.getFileHead()+downurlarrays[i].substring(downurlarrays[i].lastIndexOf("/") + 1));
				Log.e("mFile", mFile.getAbsolutePath());
				
				if(!mFile.exists()){
					output = new ProgressReportingOutputStream(mFile);
					response = client.execute(request);
					is = response.getEntity().getContent();
					mFile.createNewFile();
					byte buffer [] = new byte[1024];
					int inputSize = -1;
					long total = response.getEntity().getContentLength();
					int count = 0; //已下载大小
					while((inputSize = is.read(buffer)) != -1) {
						//synchronized (monitor) {
						output.write(buffer, 0, inputSize);
							count += inputSize;
						//}
						//更新进度
						//一旦任务被取消则退出循环，否则一直执行，直到结束
						if(isCancelled()) {
							output.flush();
							return 0;
						}
					}
					output.flush();
				}else{
					long readedSize = mFile.length(); //文件大小，即已下载大小
					Log.e(mFile.getName()+"readedSize", readedSize+"");
					HttpClient tempClient = new DefaultHttpClient();
					HttpResponse tempResponse = tempClient.execute(request);
					long mFileLength = tempResponse.getEntity().getContentLength();
					Log.e(mFile.getName()+"mFileLength", mFileLength+"");
					if(readedSize == mFileLength){
						continue;
					}
					
					//设置下载的数据位置XX字节到XX字节
					//Header header_size = new BasicHeader("Range", "bytes=" + readedSize + "-");
					request.addHeader("Range","bytes=" + readedSize + "-");
					
					//执行请求获取下载输入流
					response = client.execute(request);
					is = response.getEntity().getContent();
					Log.e(mFile.getName()+"剩余长度", response.getEntity().getContentLength()+"");
//					if(response.getEntity().getContentLength() == 362){//如果剩余部分未下载的部分返回值为-1，说明上一次该文件已经下载完成，无需再次下载
//						continue;
//					}
					//文件总大小=已下载大小+未下载大小
					//long total = readedSize + response.getEntity().getContentLength();
					//创建文件输出流
					//fos = new RandomAccessFile(mFile, "rw");
					//从文件的size以后的位置开始写入，其实也不用，直接往后写就可以。有时候多线程下载需要用
					//fos.seek(readedSize);
					output = new ProgressReportingOutputStream(mFile,true);
					byte buffer [] = new byte[1024];
					int inputSize = -1;
					int count = (int)readedSize;
					while((inputSize = is.read(buffer)) != -1) {
						//fos.write(buffer, 0, inputSize);
						//synchronized (monitor) {
						output.write(buffer, 0, inputSize);
							count += inputSize;
						//}
						if(isCancelled()) {
								output.flush();
								return 0;
							}
						}
					
					output.flush();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				try {
					if(is != null){
						is.close();
					}
					if(output != null){
						output.close();
					}
					if(fos != null){
						fos.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		Log.e("doInBackground", "doInBackground结束");
		return mTotalSize;
	}
	
	/*private int copy(InputStream input, OutputStream output){
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
	}*/
	
	
	
	private final class ProgressReportingOutputStream extends FileOutputStream{

		public ProgressReportingOutputStream(File file)
				throws FileNotFoundException {
			super(file);
		}
		
		
		public ProgressReportingOutputStream(File file,boolean append) throws FileNotFoundException {
			super(file, append);
		}

		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount)
				throws IOException {
			super.write(buffer, byteOffset, byteCount);
		    mProgress += byteCount;
		    publishProgress(mProgress + mTotalLength);
		}
		
	}
	
}

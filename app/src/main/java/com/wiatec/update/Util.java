package com.wiatec.update;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.List;

import org.apache.commons.codec.binary.Hex;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class Util {

	public final static String ETH_ADDRESS = "/sys/class/net/eth0/address";
	final static String WIFI_ADDRESS = "/sys/class/net/wlan0/address";
	public static final int privatekey = 2;
	static Context m_ctx;

	public Util(Context ctx) {
		m_ctx = ctx;
	}

	public static String getDisplayDeviceName() {
		return "BTV";
	}

	public static String getFileHead() {
		String path = "";
		switch (privatekey) {
		case 1:
			path = m_ctx.getFilesDir().toString();
			break;
		case 2:
			File sdDir = null;
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED);// 判断sd卡是否存在
			if (sdCardExist) {
				path = Environment.getExternalStorageDirectory().toString();// 获取跟目录
			} else {
				path = m_ctx.getFilesDir().toString();
			}
			break;

		default:
			break;
		}
		return path + "/Android/data/";
	}

	public static String getFinalZipHead() {
		String kodiHomePath = new String(Environment
				.getExternalStorageDirectory().getAbsolutePath()
				+ "/Android/data/org.xbmc.kodi");
		String m_kodiHome = kodiHomePath + "/files/.kodi/";
		return m_kodiHome;
	}

	public static String loadFileAsString(File _file) {
		try {
			FileInputStream fis = new FileInputStream(_file);
			int count = (int) _file.length();
			byte[] buffer = new byte[count];
			fis.read(buffer);
			fis.close();
			String address = buffer.toString();
			return address;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static String loadFileAsString(String path) {
		String address = "N/A";
		try {
			StringBuffer fileData = new StringBuffer(1024);
			BufferedReader reader = new BufferedReader(new FileReader(path));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
			reader.close();
			address = fileData.toString();
			address = address.substring(0, address.length() - 1);
			// return address;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return address;
	}

	/*
	 * Get the STB MacAddress
	 */
	public static String getMacAddress() {
		// try
		{
			String address = "N/A";
			File eth_address = new File(ETH_ADDRESS);
			File wifi_address = new File(WIFI_ADDRESS);

			if (eth_address.exists())
				address = loadFileAsString(ETH_ADDRESS);

			if (wifi_address.exists())
				address = loadFileAsString(WIFI_ADDRESS);

			return address;
		}
	}

	public boolean CheckNetwork(boolean onlyCheck) {

		boolean eth_connect = false;
		boolean network_connect = false;
		ConnectivityManager mgr = (ConnectivityManager) m_ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = mgr.getActiveNetworkInfo();
		// NetworkInfo mobile =
		// mgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		// NetworkInfo wifi = mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (info == null) {
			network_connect = false;
		} else {
			network_connect = true;
		}

		if (Build.VERSION.SDK_INT == Build.VERSION_CODES.HONEYCOMB_MR2) {
			NetworkInfo eth = mgr
					.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			eth_connect = eth.isConnected();
		} else {
			File eth_address = new File(ETH_ADDRESS);
			eth_connect = eth_address.exists();
		}

		boolean result = network_connect || eth_connect;

		if (!onlyCheck && !result) {
			WifiManager wfmanager = (WifiManager) m_ctx
					.getSystemService(Context.WIFI_SERVICE);
			if (!wfmanager.isWifiEnabled()) {
				wfmanager.setWifiEnabled(true);
				result = true;
			}
		}
		return result;
	}

	public String getResponseData(String req_url) {
		String data = "N/A";
		try {
			URL url = new URL(req_url);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.connect();

			InputStream is = null;
			ByteArrayOutputStream baos = null;
			int responseCode = conn.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				is = conn.getInputStream();
				baos = new ByteArrayOutputStream();
				byte[] byteBuffer = new byte[1024];
				byte[] byteData = null;
				int nLength = 0;
				while ((nLength = is.read(byteBuffer, 0, byteBuffer.length)) != -1) {
					baos.write(byteBuffer, 0, nLength);
				}

				byteData = baos.toByteArray();
				data = new String(byteData);
			}
		} catch (Exception e) {
			// Log.e("SettingsDownloader", "Error: "+e.toString());
		}
		return data;
	}

	public void CopyFile(String inputPath, String inputFile, String outputPath) {

		InputStream in = null;
		OutputStream out = null;
		try {

			// create output directory if it doesn't exist
			File dir = new File(outputPath);
			if (!dir.exists()) {
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

		} catch (FileNotFoundException fnfe1) {
			Log.e("tag", fnfe1.getMessage());
		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}
	}

	public void deleteFile(String inputPath, String inputFile) {
		try {
			// delete the original file
			new File(inputPath + inputFile).delete();
		} catch (Exception e) {
			Log.e("tag", e.getMessage());
		}
	}

	public void KillProcess(Update.DownloadType type) {
		if (type == Update.DownloadType.KODI)
			KillProcess(Update.KodiPackage);
		else if (type == Update.DownloadType.XBMC)
			KillProcess(Update.XbmcPackage);
	}

	public void KillProcess(String packageName) {
		ActivityManager actMgr = (ActivityManager) m_ctx
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> list = actMgr.getRunningAppProcesses();
		for (RunningAppProcessInfo info : list) {
			if (info.processName.equals(packageName)) {
				android.os.Process.sendSignal(info.pid,
						android.os.Process.SIGNAL_KILL);
				actMgr.killBackgroundProcesses(packageName);
				System.gc();
				break;
			}
		}
	}

	public Update.DownloadType DetectEnvironment() {
		Update.DownloadType type = Update.DownloadType.NONE;
		try {
			// attempt to detect userdata directory for Kodi
			String kodiHomePath = new String(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/Android/data/org.xbmc.kodi");
			// m_logWriter.write("Testing Kodi home at: " + kodiHomePath+"\n");
			File fKodiHome = new File(kodiHomePath);
			if (fKodiHome.exists() && fKodiHome.isDirectory())
				type = Update.DownloadType.KODI;
			else
				type = detectPackage(Update.KodiPackage,
						Update.DownloadType.KODI);

			// attempt to detect XBMC
			String xbmcHomePath = "";
			if (type == Update.DownloadType.NONE) {
				// attempt to detect userdata directory for XBMC
				xbmcHomePath = new String(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/Android/data/org.xbmc.xbmc");
				// m_logWriter.write("Testing XBMC home at: " + xbmcHomePath
				// +"\n");
				File fXbmcHome = new File(xbmcHomePath);
				if (fXbmcHome.exists() && fXbmcHome.isDirectory())
					type = Update.DownloadType.XBMC;
				else
					type = detectPackage(Update.XbmcPackage,
							Update.DownloadType.XBMC);
			}
		} catch (Exception e) {
			// Log.e("KodiEnvironmentLogger", e.toString());
		}

		return type;
	}

	private Update.DownloadType detectPackage(String pkgStr,
			Update.DownloadType type) {
		try {
			PackageManager pm = m_ctx.getPackageManager();
			try {
				// nested try so our entire code block doesn't end if it isn't
				// found in PM
				PackageInfo pi = pm.getPackageInfo(pkgStr,
						PackageManager.GET_ACTIVITIES);
				return type;
			} catch (PackageManager.NameNotFoundException e) {
				return Update.DownloadType.NONE;
			}
		} catch (Exception e) {
			return Update.DownloadType.NONE;
		}
	}

	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	public static String getMD5(File file) {
		FileInputStream fileInputStream = null;
		try {
			MessageDigest MD5 =MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				MD5.update(buffer, 0, length);
			}

			return new String(Hex.encodeHex(MD5.digest()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}  finally {
			try {
				if (fileInputStream != null)
					fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String[] getDeleteFiles(String path) {
		String[] deleteFiles = null;
		InputStreamReader reader = null;
		StringWriter writer = new StringWriter();
		try {
			File urlFile = new File(path);
			if (urlFile.exists()) {
				reader = new InputStreamReader(new FileInputStream(urlFile),
						"UTF-8");
				// br = new BufferedReader(isr);
				// 将输入流写入输出流
				char[] buffer = new char[1024];
				int n = 0;
				while (-1 != (n = reader.read(buffer))) {
					writer.write(buffer, 0, n);
				}
				Log.e("writer", writer.toString());
				deleteFiles = writer.toString().split("\n");
			} else {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return deleteFiles;
	}

	public static void deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			file.delete();
		}
	}

	public static void deleteDirectory(String filePath) {
		// 如果filePath不以文件分隔符结尾，自动添加文件分隔符
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath.trim() + File.separator;
			Log.e("filePath", filePath);
		}
		File dirFile = new File(filePath);
		Log.e("AAAA", dirFile.getAbsolutePath()+"="+dirFile.exists()+"="+dirFile.isDirectory());
		if (dirFile.exists() || dirFile.isDirectory()) {
			File[] files = dirFile.listFiles();
			// 遍历删除文件夹下的所有文件(包括子目录)
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					// 删除子文件
					deleteFile(files[i].getAbsolutePath());
				} else {
					// 删除子目录
					deleteDirectory(files[i].getAbsolutePath());
				}
			}
		}
		// 删除当前空目录
		dirFile.delete();
	}

}

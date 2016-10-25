package com.wiatec.entity;

import java.io.Serializable;

import android.util.Log;

public class NewVersionInfoEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String myversion;
	private String version;
	private String downloadUrl;
	private String verinclude;
	private String verincludeall;
	private boolean needFull=false;
	private String needdownloadUrl[]=null;
	private String needdownloadAllUrl[] = null;
	
	
	public String getVerincludeall() {
		return verincludeall;
	}
	public void setVerincludeall(String verincludeall) {
		this.verincludeall = verincludeall;
	}
	
	public String getVersion() {
		return version;
	}
	public NewVersionInfoEntity setVersion(String version) {
		this.version = version;
		return this;
	}
	public String getMyversion() {
		return myversion;
	}
	public NewVersionInfoEntity setMyversion(String myversion) {
		this.myversion = myversion;
		return this;
	}
	public String getDownloadUrl() {
		return downloadUrl;
		
	}
	public boolean isNeedFull() {
		return needFull;
	}
	public NewVersionInfoEntity setDownloadUrl(String downloadUrl) {
		this.downloadUrl = downloadUrl;
		return this;
	}
	public String getVerinclude() {
		return verinclude;
	}
	
	public void setVerinclude(String verinclude) {
		this.verinclude = verinclude;
	}
	
	public boolean isLastVersion(){
		if(myversion.equals(version)){
			return true;
		}else{
			return false;
		}
	}
	
	
	public String[] getNeeddownloadAllUrl() {
		//["BTVALL2016040501-3e314a2bdc","ROMSALL2016040501-df93faea2d"]
		String org = verincludeall.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
		String[] orgarray = org.split(",");
		
		String[] result = new String[orgarray.length];
		for (int i = 0; i < orgarray.length; i++) {
			orgarray[i] = orgarray[i].trim();
			result[i] = downloadUrl + orgarray[i] + ".zip";
		}
		needdownloadAllUrl = result;
		return needdownloadAllUrl;
	}
	
	
	
	public String[] getNeeddownloadUrl() {

			String org=verinclude.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
			String[] orgarray=org.split(",");
			int orgarraylength=orgarray.length;
			int myindex=-1;
			for (int i = 0; i < orgarray.length; i++) {
				orgarray[i]=orgarray[i].trim();
				if(orgarray[i].equals(myversion)){
					myindex=i;
				}
				
			}
			if(myindex!=-1){
				String[] result=new String[orgarraylength-myindex];
				for (int i = 0; i < result.length-1; i++) {
					result[i]=orgarray[i+myindex+1];
				}
				result[result.length-1]=version;
				for (int i = 0; i < result.length; i++) {
					result[i]=downloadUrl+result[i]+".zip";
				}
				needdownloadUrl=result;
			}else{
				needdownloadUrl=null;
			}
 		return needdownloadUrl;
	}
	
	
	public String[] getAllWhereFileSave(){
		String org=verincludeall.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
		String[] orgarray=org.split(",");
		String[] result=new String[orgarray.length];
		for (int i = 0; i < orgarray.length; i++) {
			result[i]=orgarray[i].trim();
		}
		needdownloadAllUrl=result;
		return needdownloadAllUrl;
	}
	
	public String[] getWhereFileSave() {
		String org=verinclude.replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "");
		String[] orgarray=org.split(",");
		int orgarraylength=orgarray.length;
		int myindex=-1;
		for (int i = 0; i < orgarray.length; i++) {
			orgarray[i]=orgarray[i].trim();
			if(orgarray[i].equals(myversion)){
				myindex=i;
			}
		}
		if(myindex!=-1){
			String[] result=new String[orgarraylength-myindex];
			for (int i = 0; i < result.length-1; i++) {
				result[i]=orgarray[i+myindex+1];
			}
			result[result.length-1]=version;
			needdownloadUrl=result;
		}else{
			needdownloadUrl=null;
		}
		return needdownloadUrl;
	}

}

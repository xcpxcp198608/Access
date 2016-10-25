/*
 * @Title ThreadDAO.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description：
 * @author ZL
 * @version 1.0
 */
package com.wiatec.update.db;

import java.util.List;

import com.wiatec.entity.ThreadInfo;


/** 
 * 数据访问接口
 * @author ZL
 */
public interface ThreadDAO
{
	/** 
	 * 插入线程信息
	 * @param threadInfo
	 * @return void
	 * @author ZL
	 */ 
	public void insertThread(ThreadInfo threadInfo);
	/** 
	 * 删除线程信息
	 * @param url
	 * @param thread_id
	 * @return void
	 * @author ZL
	 */ 
	public void deleteThread(String url);
	/** 
	 * 更新线程下载进度
	 * @param url
	 * @param thread_id
	 * @return void
	 * @author ZL
	 */ 
	public void updateThread(String url, int thread_id, int finished);
	/** 
	 * 查询文件的线程信息
	 * @param url
	 * @return
	 * @return List<ThreadInfo>
	 * @author ZL
	 */ 
	public List<ThreadInfo> getThreads(String url);
	/** 
	 * 线程信息是否存在
	 * @param url
	 * @param thread_id
	 * @return
	 * @return boolean
	 * @author ZL
	 */ 
	public boolean isExists(String url, int thread_id);
}

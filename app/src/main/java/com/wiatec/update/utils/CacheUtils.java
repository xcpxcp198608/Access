package com.wiatec.update.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class CacheUtils
{
	private final static String			SP_NAME	= "version_config";
	private static SharedPreferences	mPreferences;		// SharedPreferences的实例

	private static SharedPreferences getSp(Context context)
	{
		if (mPreferences == null)
		{
			mPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
		}

		return mPreferences;
	}

	/**
	 * 通过SP获得boolean类型的数据，没有默认为false
	 * 
	 * @param context
	 *            : 上下文
	 * @param key
	 *            : 存储的key
	 * @return
	 */
	public static boolean getBoolean(Context context, String key)
	{
		SharedPreferences sp = getSp(context);
		return sp.getBoolean(key, false);
	}

	/**
	 * 通过SP获得boolean类型的数据，没有默认为false
	 * 
	 * @param context
	 *            : 上下文
	 * @param key
	 *            : 存储的key
	 * @param defValue
	 *            : 默认值
	 * @return
	 */
	public static boolean getBoolean(Context context, String key, boolean defValue)
	{
		SharedPreferences sp = getSp(context);
		return sp.getBoolean(key, defValue);
	}

	/**
	 * 设置boolean的缓存数据
	 * 
	 * @param context
	 * @param key
	 *            :缓存对应的key
	 * @param value
	 *            :缓存对应的值
	 */
	public static void setBoolean(Context context, String key, boolean value)
	{
		SharedPreferences sp = getSp(context);
		Editor edit = sp.edit();// 获取编辑器
		edit.putBoolean(key, value);
		edit.commit();
	}

	/**
	 * 通过SP获得String类型的数据，没有默认为null
	 * 
	 * @param context
	 *            : 上下文
	 * @param key
	 *            : 存储的key
	 * @return
	 */
	public static String getString(Context context, String key)
	{
		SharedPreferences sp = getSp(context);
		return sp.getString(key, null);
	}

	/**
	 * 通过SP获得String类型的数据
	 * 
	 * @param context
	 *            : 上下文
	 * @param key
	 *            : 存储的key
	 * @param defValue
	 *            : 默认值
	 * @return
	 */
	public static String getString(Context context, String key, String defValue)
	{
		SharedPreferences sp = getSp(context);
		return sp.getString(key, defValue);
	}

	/**
	 * 设置String的缓存数据
	 * 
	 * @param context
	 * @param key
	 *            :缓存对应的key
	 * @param value
	 *            :缓存对应的值
	 */
	public static void setString(Context context, String key, String value)
	{
		SharedPreferences sp = getSp(context);
		Editor edit = sp.edit();// 获取编辑器
		edit.putString(key, value);
		edit.commit();
	}

	/**
	 * 通过SP获得Long类型的数据，没有默认为-1
	 * 
	 * @param context
	 *            : 上下文
	 * @param key
	 *            : 存储的key
	 * @return
	 */
	public static long getLong(Context context, String key)
	{
		SharedPreferences sp = getSp(context);
		return sp.getLong(key, -1);
	}

	/**
	 * 通过SP获得Long类型的数据
	 * 
	 * @param context
	 *            : 上下文
	 * @param key
	 *            : 存储的key
	 * @param defValue
	 *            : 默认值
	 * @return
	 */
	public static long getLong(Context context, String key, long defValue)
	{
		SharedPreferences sp = getSp(context);
		return sp.getLong(key, defValue);
	}

	/**
	 * 设置Long的缓存数据
	 * 
	 * @param context
	 * @param key
	 *            :缓存对应的key
	 * @param value
	 *            :缓存对应的值
	 */
	public static void setLong(Context context, String key, long value)
	{
		SharedPreferences sp = getSp(context);
		Editor edit = sp.edit();// 获取编辑器
		edit.putLong(key, value);
		edit.commit();
	}
	
	
	public static int getInt(Context context, String key)
	{
		SharedPreferences sp = getSp(context);
		return sp.getInt(key, 0);
	}
	
	public static int getInt(Context context, String key, int defValue)
	{
		SharedPreferences sp = getSp(context);
		return sp.getInt(key, defValue);
	}
	
	public static void setInt(Context context, String key, int value)
	{
		SharedPreferences sp = getSp(context);
		Editor edit = sp.edit();// 获取编辑器
		edit.putInt(key, value);
		edit.commit();
	}
}

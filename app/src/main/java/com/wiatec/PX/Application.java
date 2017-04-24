package com.wiatec.PX;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by PX on 2016/9/19.
 */
public class Application extends android.app.Application {
    private static RequestQueue requestQueue;
    private static Context mContext;
    @Override
    public void onCreate() {
        super.onCreate();
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        mContext = getApplicationContext();
    }

    public static RequestQueue getVolleyRequestQueue(){
        return requestQueue;
    }

    public static Context getContext(){
        return mContext;
    }
}

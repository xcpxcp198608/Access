package com.wiatec.update;

import com.wiatec.update.exception.CrashHandler;

import android.app.Application;

public class CrashApplication extends Application {
	
	@Override  
    public void onCreate() {  
        super.onCreate();  
        CrashHandler catchHandler = CrashHandler.getInstance();  
        catchHandler.init(getApplicationContext());  
    }
}

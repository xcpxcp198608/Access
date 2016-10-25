package com.wiatec.update.receiver;

import com.wiatec.update.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OpenAPPReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){
			Log.e("PACKAGE_ADDED", intent.getAction());
			if(intent.getDataString().equals("com.wiatec.update")){
				Log.e("getDataString()", intent.getDataString());
				Intent intent1 = new Intent(context,MainActivity.class);
				intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent1);
			}
		}
	}
}

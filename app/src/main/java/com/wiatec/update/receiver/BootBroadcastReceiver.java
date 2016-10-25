package com.wiatec.update.receiver;


import com.wiatec.update.services.CheckVersionService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.e("BootBroadcastReceiver", "BootBroadcastReceiver");
		Intent intent1 = new Intent(context,CheckVersionService.class);
		context.startService(intent1);
	}
}

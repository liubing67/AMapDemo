package com.abing.amap.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by leidong on 2017/4/28.
 */

public class AutoStartBroadcastReceiver extends BroadcastReceiver {
    private static final String ACTION = "android.intent.action.BOOT_COMPLETED";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION)) {
            Intent service = new Intent(context, AutoStartService.class);
            context.startService(service);

            Intent newIntent = context.getPackageManager()
                    .getLaunchIntentForPackage("com.abing.amap");
            context.startActivity(newIntent);
        }
    }
}

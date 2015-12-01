package com.codeforgvl.trolleytracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationLoggerServiceManager extends BroadcastReceiver {
    public static final String TAG = "LocationLoggerServiceManager";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure we are getting the right intent
        List<String> intents = Arrays.asList("android.intent.action.MY_PACKAGE_REPLACED","android.intent.action.BOOT_COMPLETED");
        if(intents.contains(intent.getAction())) {
            boolean mUpdatesRequested = PreferenceManager.getInstance().getBackgroundTestsEnabled(context);

            if(mUpdatesRequested){
                Intent mi=new Intent(context, BackgroundLocationService.class);
                context.startService(mi);
            }

            if (intent.getAction() == Intent.ACTION_BOOT_COMPLETED) {
                PreferenceManager.getInstance().setUptime(System.currentTimeMillis(), context);
            }
        } else if (intent.getAction() == Intent.ACTION_POWER_DISCONNECTED && PreferenceManager.getInstance().getShutdownEnabled(context)) {
            //Shut down device (but make sure we are not currently booting)
            if (PreferenceManager.getInstance().getUptime(context) > System.currentTimeMillis() - SystemClock.uptimeMillis()) {
                new Thread(new ShutdownRunnable()).start();
            }
        }
    }

    private class ShutdownRunnable implements Runnable {
        @Override
        public void run() {
            try {
                Process proc = Runtime.getRuntime()
                        .exec(new String[]{ "su", "-c", "reboot -p" });
                proc.waitFor();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
package com.codeforgvl.trolleytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;
import java.util.Arrays;
import java.util.List;

public class LocationLoggerServiceManager extends BroadcastReceiver {
    public static final String TAG = "LocationLoggerServiceManager";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure we are getting the right intent
        List<String> intents = Arrays.asList(Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_BOOT_COMPLETED);
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
                Toast.makeText(context, "Tracker Agent: Power unplugged, shutting down", Toast.LENGTH_LONG).show();
                Intent shutdownIntent = new Intent(context, ShutdownService.class);
                context.startService(shutdownIntent);
            } {
                Toast.makeText(context, "Tracker Agent: Ignored power unplugged event, currently booting", Toast.LENGTH_LONG).show();
            }
        }
    }
}
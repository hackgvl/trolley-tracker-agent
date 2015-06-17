package com.codeforgvl.trolleytracker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class LocationLoggerServiceManager extends BroadcastReceiver {
    public static final String TAG = "LocationLoggerServiceManager";
    @Override
    public void onReceive(Context context, Intent intent) {
        // Make sure we are getting the right intent
        if( "android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            boolean mUpdatesRequested = PreferenceManager.getInstance().getBackgroundTestsEnabled(context);

            if(mUpdatesRequested){
                Intent mi=new Intent(context, BackgroundLocationService.class);
                context.startService(mi);
            }

        } else {
            //Log.e(TAG, "Received unexpected intent " + intent.toString());
        }
    }
}
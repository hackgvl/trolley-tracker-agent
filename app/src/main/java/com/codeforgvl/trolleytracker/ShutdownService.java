package com.codeforgvl.trolleytracker;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Adam Hodges on 12/7/2015.
 */

public class ShutdownService extends IntentService {
    public ShutdownService() {
        super("ShutdownService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Process proc = Runtime.getRuntime()
                    .exec(new String[]{"su", "-c", "am start -a android.intent.action.ACTION_REQUEST_SHUTDOWN"});
            proc.waitFor();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
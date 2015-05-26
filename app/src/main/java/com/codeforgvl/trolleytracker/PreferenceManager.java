package com.codeforgvl.trolleytracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.codeforgvl.trolleytracker.ui.SettingsActivity;

/**
 * A singleton class which provides accessors and mutators for the client's 
 * preferences.
 * @author Joe Maley
 */
public class PreferenceManager {
	private static PreferenceManager instance = null;

    public String getServerIP(Context context) {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_IP, "104.131.44.166");
    }

    public String getTrolleyNumber(Context context) {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(SettingsActivity.KEY_PREF_TROLLEY_ID, "5");
    }

    public String getUser(Context context) {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(SettingsActivity.KEY_PREF_USERNAME, "");
    }

    public String getPassword(Context context) {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(SettingsActivity.KEY_PREF_PASSWORD, "");
    }

    public void setBackgroundTestsEnabled(boolean backgroundTestsEnabled, Activity activity){
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(activity);
        sharedPreferences.edit().putBoolean(SettingsActivity.KEY_PREF_BACKGROUND_TESTS, backgroundTestsEnabled).commit();
    }
    public boolean getBackgroundTestsEnabled(Context context) {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_BACKGROUND_TESTS, true);
    }

	
	/**
	 * Creates a new instance of the class if this is the first time being 
	 * called. Otherwise, it is returns the previously created instance.
	 * @return An instance of the preference manager
	 */
	public static PreferenceManager getInstance() {
		
		if (instance == null) {
			instance = new PreferenceManager();
		}
		
		return instance;
	}
}

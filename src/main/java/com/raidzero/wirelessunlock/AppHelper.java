package com.raidzero.wirelessunlock;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by raidzero on 5/5/14 9:23 PM
 */
public class AppHelper extends Application {
    public SharedPreferences sharedPreferences = null;
    private static final String tag = "AppHelper";
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public boolean isPrefEnabled(String key) {
        Log.d(tag, "isPrefEnabled(" + key + ") called");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(key, false);
        } else {
            Log.d(tag, "sharedPreferences is null");
        }

        return false;
    }
}

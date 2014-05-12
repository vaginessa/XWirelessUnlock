package com.raidzero.wirelessunlock.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;
import com.raidzero.wirelessunlock.R;

/**
 * Created by raidzero on 5/5/14 6:31 PM
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String tag = "WirelessUnlock/PreferenceActivity";

    private AppDelegate appDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        this.appDelegate = Common.getAppDelegate();
    }

    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // showNotifications
        if (key.equals("showNotifications")) {
            if (appDelegate.isPrefEnabled("enableApp")) {
                if (sharedPreferences.getBoolean(key, false)) {
                    Log.d(tag, "enabled notifications");
                    appDelegate.showNotification();
                } else {
                    Log.d(tag, "disabled notifications");
                    appDelegate.dismissNotification();
                }
            }
        }

        // enableApp
        if (key.equals("enableApp")) {
            if (!sharedPreferences.getBoolean(key, false)) {
                Log.d(tag, "disabled app");
                appDelegate.stopUnlockService("Lock screen control disabled.");
            }
        }
    }
}

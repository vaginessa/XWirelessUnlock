package com.raidzero.wirelessunlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/14/14 8:31 PM
 */
public class IntentReceiver extends BroadcastReceiver {
    private static final String tag = "WirelessUnlock/IntentReceiver";

    protected AppDelegate appDelegate = Common.getAppDelegate();
    protected String action;
    SharedPreferences prefs = appDelegate.getSharedPreferences();

    public void onReceive(Context context, Intent intent) {

        action = intent.getAction();

        if (!prefs.getBoolean("enableApp", true)) {
            Log.d(tag, "RECEIVER IGNORING " + action + ". APP DISABLED");
            return;
        } else {
            Log.d(tag, "Receiver fired on intent: " + action);
        }
    }
}

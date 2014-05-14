package com.raidzero.wirelessunlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by posborn on 5/14/14.
 */

public class ScreenReceiver extends BroadcastReceiver {
    private static final String tag = "WirelessUnlock/ScreenReceiver";
    AppDelegate appDelegate = Common.getAppDelegate();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d(tag, "Receiver fired on intent: " + action);

        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            appDelegate.setScreenState(AppDelegate.ScreenPowerState.ON);
        }

        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            appDelegate.setScreenState(AppDelegate.ScreenPowerState.OFF);
        }

        appDelegate.processChanges();
    }
}

package com.raidzero.wirelessunlock.receivers;

import android.content.Context;
import android.content.Intent;

import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by posborn on 5/14/14.
 */

public class ScreenReceiver extends IntentReceiver {
    private static final String tag = "WirelessUnlock/ScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (action.equals(Intent.ACTION_SCREEN_ON)) {
            appDelegate.setScreenState(AppDelegate.ScreenPowerState.ON);
        }

        if (action.equals(Intent.ACTION_SCREEN_OFF)) {
            appDelegate.setScreenState(AppDelegate.ScreenPowerState.OFF);
        }
    }
}

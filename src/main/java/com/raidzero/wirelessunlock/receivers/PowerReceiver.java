package com.raidzero.wirelessunlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/11/14 10:41 AM
 */
public class PowerReceiver extends BroadcastReceiver {
    AppDelegate appDelegate = Common.getAppDelegate();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!appDelegate.isPrefEnabled("enableApp")) {
            // don't do anything
            return;
        }

        appDelegate.processChanges();
    }
}

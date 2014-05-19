package com.raidzero.wirelessunlock.receivers;

import android.content.Context;
import android.content.Intent;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/10/14 5:24 PM
 */
public class WifiReceiver extends IntentReceiver {
    private static final String tag = "WirelessUnlock/WifiReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        appDelegate.processChanges();
    }
}

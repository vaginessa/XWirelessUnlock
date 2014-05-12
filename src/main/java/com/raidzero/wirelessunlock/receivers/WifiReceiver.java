package com.raidzero.wirelessunlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/10/14 5:24 PM
 */
public class WifiReceiver extends BroadcastReceiver {
    AppDelegate appDelegate = Common.getAppDelegate();

    @Override
    public void onReceive(Context context, Intent intent) {
        appDelegate.processChanges();
    }
}

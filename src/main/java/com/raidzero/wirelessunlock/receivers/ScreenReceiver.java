package com.raidzero.wirelessunlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/9/14 9:31 AM
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static final String tag = "WirelessUnlock/ScreenReceiver";
    private AppHelper appHelper = Common.getAppHelper();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(tag, "Receiver fired by intent: " + intent.getAction());
        // just tell the service to refresh
        appHelper.processChanges();
    }

}
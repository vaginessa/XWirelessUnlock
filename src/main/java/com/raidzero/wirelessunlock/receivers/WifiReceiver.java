package com.raidzero.wirelessunlock.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/10/14 5:24 PM
 */
public class WifiReceiver extends BroadcastReceiver {
    AppHelper appHelper = Common.getAppHelper();

    @Override
    public void onReceive(Context context, Intent intent) {
        appHelper.processChanges();
    }
}

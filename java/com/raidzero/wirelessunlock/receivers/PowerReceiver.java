package com.raidzero.wirelessunlock.receivers;

import android.content.Context;
import android.content.Intent;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/11/14 10:41 AM
 */
public class PowerReceiver extends IntentReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        appDelegate.processChanges();
    }
}

package com.raidzero.wirelessunlock.receivers;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by posborn on 5/13/14.
 */
public class AdminReceiver  extends DeviceAdminReceiver {
    private static final String tag = "WirelessUnlock/DeviceAdminReceiver";
    AppDelegate appDelegate = Common.getAppDelegate();

    @Override
    public void onEnabled(Context context, Intent intent) {
        appDelegate.setDeviceAdminActive();
        Log.d(tag, "device admin enabled");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        appDelegate.setDeviceAdminInactive();
        Log.d(tag, "device amdin disabled");
    }
}

package com.raidzero.wirelessunlock.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/10/14 5:25 PM
 */
public class BluetoothReceiver extends BroadcastReceiver {
    private static final String tag = "WirelessUnlock/BluetoothReceiver";

    AppHelper appHelper = Common.getAppHelper();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddr = device.getAddress();

            Log.d(tag, "Bluetooth device connected: " + deviceAddr);
            appHelper.addConnectedAddress(deviceAddr);

        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddr = device.getAddress();

            Log.d(tag, "Bluetooth device disconnected: " + deviceAddr);
            appHelper.removeConnectedAddress(deviceAddr);
        }

        appHelper.processChanges();
    }
}

package com.raidzero.wirelessunlock.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;

/**
 * Created by raidzero on 5/10/14 5:25 PM
 */
public class BluetoothReceiver extends IntentReceiver {
    private static final String tag = "WirelessUnlock/BluetoothReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddr = device.getAddress();

            Log.d(tag, "Bluetooth device connected: " + deviceAddr);
            appDelegate.addConnectedAddress(deviceAddr);

        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddr = device.getAddress();

            Log.d(tag, "Bluetooth device disconnected: " + deviceAddr);
            appDelegate.removeConnectedAddress(deviceAddr);
        }

        appDelegate.processChanges();
    }
}

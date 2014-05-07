package com.raidzero.wirelessunlock.Receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.raidzero.wirelessunlock.AppHelper;
import com.raidzero.wirelessunlock.SettingsActivity;

/**
 * Created by raidzero on 5/5/14 7:33 PM
 */
public class ConnectionReceiver extends BroadcastReceiver {

    private final static String tag = "WirelessUnlock/ConnectionReceiver";
    private AppHelper appHelper = SettingsActivity.getAppHelper();

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        Log.d(tag, "Receiver fired on action: " + action);

        // handle bluetooth connection
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            String deviceAddr = device.getAddress();
            Log.d(tag, "Bluetooth device connected: " + deviceAddr);
            appHelper.addConnectedDevice(deviceAddr);

        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            String deviceAddr = device.getAddress();
            Log.d(tag, "Bluetooth device disconnected: " + deviceAddr);

            appHelper.removeConnectedDevice(deviceAddr);
        }

        appHelper.processChanges();
    }
}

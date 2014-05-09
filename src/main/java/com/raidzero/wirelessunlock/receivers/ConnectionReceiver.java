package com.raidzero.wirelessunlock.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;
import com.raidzero.wirelessunlock.LockService;

/**
 * Created by raidzero on 5/8/14 2:13 PM
 */
public class ConnectionReceiver extends BroadcastReceiver {

    private final static String tag = "WirelessUnlock/ConnectionReceiver";

    private AppHelper appHelper = Common.getAppHelper();

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        Log.d(tag, "Receiver fired by intent: " + action);

        // start service
        context.startService(new Intent(context, LockService.class));


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
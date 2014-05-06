package com.raidzero.wirelessunlock;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by raidzero on 5/5/14 7:33 PM
 */
public class ConnectionReceiver extends BroadcastReceiver {

    private final static String tag = "ConnectionReceiver";
    private AppHelper appHelper = new AppHelper();

    @Override
    public void onReceive(Context context, Intent intent) {

        final String action = intent.getAction();
        Log.d(tag, "Receiver fired on action: " + action);

        // handle wifi
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            Log.d(tag, "Wifi change detected!");

            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            NetworkInfo.State state = networkInfo.getState();

            if (state == NetworkInfo.State.CONNECTED) {
                String targetAddr = manager.getConnectionInfo().getBSSID().replace("\"", "");
                Log.d(tag, "Connected to BSSID " + targetAddr);

                // see if this device is trusted
                if (appHelper.isPrefEnabled(targetAddr)) {
                    Log.d(tag, "TRUSTED DEVICE (WIFI)");
                }

                // TODO: disable lock screen
            }
            else if (state == NetworkInfo.State.DISCONNECTED) {
                Log.d(tag, "DISCONNECTED FROM WIFI");

                // TODO: re-enable lock screen
            }
        }

        // handle bluetooth connection
        if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            String deviceAddr = device.getAddress();
            Log.d(tag, "Bluetooth device connected: " + deviceAddr);

            // see if this device is trusted
            if (appHelper.isPrefEnabled(deviceAddr)) {
                Log.d(tag, "TRUSTED DEVICE (BLUETOOTH)");
            }

            // TODO: disable lock screen
        } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            String deviceAddr = device.getAddress();
            Log.d(tag, "Bluetooth device disconnected: " + deviceAddr);

            // TODO: re-enable lock screen
        }


    }
}

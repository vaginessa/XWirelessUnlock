package com.raidzero.wirelessunlock;

import android.app.Application;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHealth;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by raidzero on 5/5/14 9:23 PM
 */
public class AppHelper extends Application {

    private static final String tag = "WirelessUnlock/AppHelper";
    private static final String kgTag = "com.raidzero.wirelessunlock.AppHelper";

    private static Context context;

    private static SharedPreferences sharedPreferences;

    // this must be a member variable
    private static KeyguardManager.KeyguardLock kgLock;

    private ArrayList<String> connectedBluetoothDevices = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        this.kgLock = keyguardManager.newKeyguardLock(kgTag);

        Log.d(tag, "onCreate(): sharedPreferences == null? " + (sharedPreferences == null));
    }

    public boolean isPrefEnabled(String key) {
        Log.d(tag, "isPrefEnabled(" + key + ") called");

        boolean rtn = false;

        if (sharedPreferences != null) {
            rtn = sharedPreferences.getBoolean(key, false);
        } else {
            Log.d(tag, "sharedPreferences is null");
        }
        Log.d(tag, "result: " + rtn);

        return rtn;
    }

    public boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        Log.d(tag, "status: " + status);
        boolean isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        Log.d(tag, "charging? " + isCharging);
        return isCharging;
    }

    private void enableLockScreen() {
        kgLock.reenableKeyguard();
    }

    private void disableLockScreen() {
        kgLock.disableKeyguard();
    }

    public ArrayList<String> getTrustedDevices() {
        ArrayList<String> result = new ArrayList<String>();

        Map<String, ?> keys = sharedPreferences.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            String keyName = entry.getKey();

            if (isPrefEnabled(keyName)) {
                result.add(keyName);
            }
        }

        return result;
    }

    public void addConnectedDevice(String address) {
        if (!connectedBluetoothDevices.contains(address)) {
            connectedBluetoothDevices.add(address);
        }
    }

    public void removeConnectedDevice(String address) {
        if (connectedBluetoothDevices.contains(address)) {
            connectedBluetoothDevices.remove(address);
        }
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    private String getConnectedWifiAddress() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        try {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getBSSID().replace("\"", "");
        } catch (Exception e) {
            return null;
        }
    }

    public void processChanges() {
        Log.d(tag, "processChanges()");

        ArrayList<String> allConnectedDevices = new ArrayList<String>();

        String currentNetworkAddr = getConnectedWifiAddress();
        allConnectedDevices.add(currentNetworkAddr);

        // now add BT
        allConnectedDevices.addAll(connectedBluetoothDevices);

        Log.d(tag, "connectedBluetoothDevices:");
        for (String device : connectedBluetoothDevices) {
            Log.d(tag, "device: " + device);
        }
        Log.d(tag, "allConnectedDevices:");
        for (String device : allConnectedDevices) {
            Log.d(tag, "device: " + device);
        }

        for (String address : getTrustedDevices()) {
            Log.d(tag, "checking address " + address);

            if (allConnectedDevices.contains(address)) {
                if (connectedBluetoothDevices.contains(address) && isPrefEnabled("onlyWhenCharging")) {
                    Log.d(tag, "onlyWhenCharging enabled.");
                    if (!isCharging()) {
                        Log.d(tag, "Lockscreen enabled (device not charging)");
                        enableLockScreen();
                        return;
                    }
                }
                // disable the lock screen, a trusted device is present
                disableLockScreen();

                Log.d(tag, "lockscreen disabled");
                // nothing else to see here
                return;
            }
        }

        // otherwise enable it
        Log.d(tag, "Lockscreen enabled");
        enableLockScreen();
    }
}

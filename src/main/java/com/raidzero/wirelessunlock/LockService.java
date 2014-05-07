package com.raidzero.wirelessunlock;

import android.app.KeyguardManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by posborn on 5/7/14.
 */
public class LockService extends Service {

    private static final String tag = "WirelessUnlock/LockService";
    private static final String kgTag = "com.raidzero.wirelessunlock.LockService";

    private static boolean isRunning = false;

    private static Context context;

    private static SharedPreferences sharedPreferences;

    // this must be a member variable
    private static KeyguardManager.KeyguardLock kgLock;

    private static enum LockState {
        ENABLED, DISABLED
    }

    private static ArrayList<String> connectedBluetoothDevices = new ArrayList<String>();

    private final IBinder myBinder = new MyLocalBinder();

    @Override

    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        LockService getService() {
            return LockService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "LockService created");
        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        this.kgLock = keyguardManager.newKeyguardLock(kgTag);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        return START_STICKY; // Run until explicitly stopped.
    }

    public static boolean isPrefEnabled(String key) {
        boolean rtn = false;

        if (sharedPreferences != null) {
            rtn = sharedPreferences.getBoolean(key, false);
        }
        return rtn;
    }

    public static boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging =
                status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    public static ArrayList<String> getTrustedDevices() {
        ArrayList<String> result = new ArrayList<String>();

        try {
            Map<String, ?> keys = sharedPreferences.getAll();

            for (Map.Entry<String, ?> entry : keys.entrySet()) {
                String keyName = entry.getKey();

                if (isPrefEnabled(keyName)) {
                    result.add(keyName);
                }
            }
        } catch (Exception e) {
            return result; // its empty
        }

        return result;
    }

    private static void addConnectedDevice(String address) {
        if (!connectedBluetoothDevices.contains(address)) {
            connectedBluetoothDevices.add(address);
        }
    }

    private static void removeConnectedDevice(String address) {
        if (connectedBluetoothDevices.contains(address)) {
            connectedBluetoothDevices.remove(address);
        }
    }

    public static void setLockScreenState(LockState state) {
        if (kgLock != null) {
            if (state == LockState.DISABLED) {
                kgLock.disableKeyguard();
            } else if (state == LockState.ENABLED) {
                kgLock.reenableKeyguard();
            }
        }
    }

    private static String getConnectedWifiAddress() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getBSSID().replace("\"", "");
        } catch (Exception e) {
            return null;
        }
    }

    public static void processChanges() {
        ArrayList<String> allConnectedDevices = new ArrayList<String>();

        String currentNetworkAddr = getConnectedWifiAddress();
        String trustedAddress = "";
        String lockReason = "No trusted devices found.";

        // only add address if it exists
        if (currentNetworkAddr != null) {
            allConnectedDevices.add(currentNetworkAddr);
        }

        // now add BT
        allConnectedDevices.addAll(connectedBluetoothDevices);

        // do we actually have anything connected?
        int numConnectedDevices = allConnectedDevices.size();
        Log.d(tag, numConnectedDevices + " device(s) connected.");

        if (numConnectedDevices > 0) {
            for (String device : allConnectedDevices) {
                Log.d(tag, "connectedDevice: " + device);
            }

            for (String address : getTrustedDevices()) {
                if (allConnectedDevices.contains(address)) {
                    if (connectedBluetoothDevices.contains(address)) {
                        // this is a bluetooth device
                        if (isPrefEnabled("onlyWhenCharging")) {
                            if (!isCharging()) {
                                lockReason = address + " is trusted but device is not charging.";
                                continue;
                            }
                        } else {
                            // bluetooth device is trusted and we don't care about charging
                            trustedAddress = address;
                            break;
                        }
                    }

                    // this is not a bluetooth device, and is trusted
                    trustedAddress = address;
                    break;
                }
            }
        }

        if (!trustedAddress.equals("")) {
            Log.d(tag, String.format("Lockscreen disabled (%s is trusted)", trustedAddress));
            setLockScreenState(LockState.DISABLED);
        } else {
            // otherwise enable it
            Log.d(tag, String.format("Lockscreen enabled. (%s)", lockReason));
            setLockScreenState(LockState.ENABLED);
        }
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static class ConnectionReceiver extends BroadcastReceiver {

        private final static String tag = "WirelessUnlock/ConnectionReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            Log.d(tag, "Receiver triggered by action: " + action);

            // start service
            if (!isRunning) {
                context.startService(new Intent(context, LockService.class));
                Log.d(tag, "Service started by action: " + action);
            }

            // handle bluetooth connection
            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceAddr = device.getAddress();

                Log.d(tag, "Bluetooth device connected: " + deviceAddr);
                addConnectedDevice(deviceAddr);

            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceAddr = device.getAddress();

                Log.d(tag, "Bluetooth device disconnected: " + deviceAddr);
                removeConnectedDevice(deviceAddr);
            }

            processChanges();
        }
    }
}


package com.raidzero.wirelessunlock;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;
import com.raidzero.wirelessunlock.receivers.ScreenReceiver;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by posborn on 5/7/14.
 */
public class LockService extends Service {

    private final String tag = "WirelessUnlock/LockService";
    private final String kgTag = "com.raidzero.wirelessunlock.LockService";

    private boolean isRunning = false;
    private boolean isLockScreenEnabled = true;

    private Context context;

    private SharedPreferences sharedPreferences;
    private AppHelper appHelper;

    private ArrayList<String> connectedBluetoothDevices = new ArrayList<String>();

    // this must be a member variable
    private KeyguardManager.KeyguardLock kgLock;

    private enum LockState {
        ENABLED, DISABLED
    }

    private final IBinder myBinder = new MyLocalBinder();

    @Override

    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public class MyLocalBinder extends Binder {
        public LockService getService() {
            return LockService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "LockService created");
        this.context = getApplicationContext();
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        appHelper = new AppHelper();

        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        this.kgLock = keyguardManager.newKeyguardLock(kgTag);

        // register the screen receiver
        IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        ScreenReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, screenFilter);

        broadcastLockState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        return START_STICKY; // Run until explicitly stopped.
    }

    private boolean isPrefEnabled(String key) {
        boolean rtn = false;

        if (sharedPreferences != null) {
            rtn = sharedPreferences.getBoolean(key, false);
        }
        return rtn;
    }

    private boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public void addConnectedDevice(String address) {
        if (!connectedBluetoothDevices.contains(address)) {
            Log.d(tag, "Adding BT device " + address);
            connectedBluetoothDevices.add(address);
        }

        Log.d(tag, "addConnectedDevice(): ");
        for (String d : connectedBluetoothDevices) {
            Log.d(tag, "device: " + d);
        }
    }

    public void removeConnectedDevice(String address) {
        Log.d(tag, "removeConnectedDevice(): ");
        for (String d : connectedBluetoothDevices) {
            Log.d(tag, "device: " + d);
        }
        if (connectedBluetoothDevices.contains(address)) {
            connectedBluetoothDevices.remove(address);
            Log.d(tag, "removed BT device " + address);
        }
    }

    private ArrayList<String> getTrustedDevices() {
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

    private void setLockScreenState(LockState state) {
        if (kgLock != null) {
            if (state == LockState.DISABLED) {
                kgLock.disableKeyguard();
                isLockScreenEnabled = false;
            } else if (state == LockState.ENABLED) {
                kgLock.reenableKeyguard();
                isLockScreenEnabled = true;
            }
            broadcastLockState();
        }
    }

    private void broadcastLockState() {
        Intent i = new Intent();
        i.setAction(Common.messageIntent);

        String dataString = "nothing to see here";

        if (isLockScreenEnabled) {
            dataString = getResources().getString(R.string.lockscreen_enabled);
        } else {
            dataString = getResources().getString(R.string.lockscreen_disabled);
        }

        i.putExtra("message", dataString);

        sendBroadcast(i);

        Log.d(tag, "broadcastLockState(): " + dataString);
    }

    private String getConnectedWifiAddress() {
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getBSSID().replace("\"", "");
        } catch (Exception e) {
            return null;
        }
    }

    public void processChanges() {
        ArrayList<String> allConnectedDevices = new ArrayList<String>();

        String currentNetworkAddr = getConnectedWifiAddress();
        String trustedAddress = "";
        String lockReason = "No trusted devices found.";

        // only add address if it exists
        if (currentNetworkAddr != null) {
            allConnectedDevices.add(currentNetworkAddr);
        }

        Log.d(tag, "connectedBluetoothDevices: " + connectedBluetoothDevices.size());
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
}


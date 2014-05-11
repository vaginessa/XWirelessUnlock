package com.raidzero.wirelessunlock.global;

import android.app.Application;
import android.content.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.raidzero.wirelessunlock.R;
import com.raidzero.wirelessunlock.UnlockService;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by raidzero on 5/8/14 2:18 PM
 */
public class AppHelper extends Application {
    private static final String tag = "WirelessUnlock/AppHelper";

    private boolean isServiceRunning = false;

    private SharedPreferences sharedPreferences;
    private ArrayList<String> connectedAddresses = new ArrayList<String>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "onCreate()");

        Common.appHelper = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public void addConnectedAddress(String address) {
        if (!connectedAddresses.contains(address)) {
            connectedAddresses.add(address);
        }
    }

    public void removeConnectedAddress(String address) {
        if (connectedAddresses.contains(address)) {
            connectedAddresses.remove(address);
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

    public void startUnlockService() {
        if (!isServiceRunning) {
            startService(new Intent(this, UnlockService.class));
            isServiceRunning = true;
        }
        broadcastServiceState();
    }

    public void stopUnlockService() {
        if (isServiceRunning) {
            stopService(new Intent(this, UnlockService.class));
            isServiceRunning = false;
        }
        broadcastServiceState();
    }

    public void broadcastServiceState() {
        Intent i = new Intent();
        i.setAction(Common.messageIntent);

        String data;

        if (isServiceRunning) {
            data = getResources().getString(R.string.main_lockscreen_disabled);
        } else {
            data = getResources().getString(R.string.main_lockscreen_enabled);
        }

        i.putExtra("message", data);
        sendBroadcast(i);
    }

    private boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    private boolean isPrefEnabled(String key) {
        boolean rtn = false;

        if (sharedPreferences != null) {
            rtn = sharedPreferences.getBoolean(key, false);
        }
        return rtn;
    }

    private String getConnectedWifiAddress() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getBSSID().replace("\"", "");
        } catch (Exception e) {
            return null;
        }
    }

    public void processChanges() {
        ArrayList<String> connectedDevices = new ArrayList<String>();

        // add wifi, if any
        connectedDevices.add(getConnectedWifiAddress());

        // add bluetooth, if any
        connectedDevices.addAll(connectedAddresses);

        boolean startService = false;
        for (String d : connectedDevices) {
            // bluetooth?
            if (connectedAddresses.contains(d)) {
                if (isPrefEnabled("onlyWhenCharging")) {
                    if (isCharging()) {
                        Log.d(tag, "Trusted BT device. charging");
                        startService = true;
                        break;
                    }
                } else {
                    Log.d(tag, "Trusted BT device");
                    startService = true;
                    return;
                }
            }

            // wifi
            if (getTrustedDevices().contains(d)) {
                Log.d(tag, "Trusted wifi device.");
                startService = true;
                break;
            }
        }

        if (startService) {
            startUnlockService();
            return;
        }

        stopUnlockService();
    }
}

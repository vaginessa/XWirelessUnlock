package com.raidzero.wirelessunlock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.util.Log;

import java.util.List;
import java.util.Set;

/**
 * Created by raidzero on 5/5/14 6:31 PM
 */
public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String tag = "WirelessUnlock/PreferenceActivity";

    private static LockService lockService;
    private boolean isBound = false;

    // wifi stuff
    private WifiManager wifiManager = null;
    private List<ScanResult> scanResults = null;
    private WifiReceiver wifiReceiver = null;
    PreferenceCategory btCategory = null;
    PreferenceCategory wifiCategory = null;

    private ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LockService.MyLocalBinder binder = (LockService.MyLocalBinder) service;
            lockService = binder.getService();
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            isBound = false;
        }
    };

    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        if (lockService != null) {
            lockService.processChanges();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unbind from it so it doesn't kill the service
        unbindService(myConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // send intent to start service
        if (!lockService.isRunning()) {
            sendBroadcast(new Intent("com.raidzero.wirelessunlock.APP_STARTED"));
        }
        else {
            Log.d(tag, "Service already running");
        }

        // bind to it
        Intent intent = new Intent(this, LockService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

        // register wifi stuff and start scan
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();

        // get list of bluetooth devices
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        btCategory = (PreferenceCategory) findPreference("key_btDevices");
        if (pairedDevices != null) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceAddr = device.getAddress();
                String deviceName = device.getName();

                CheckBoxPreference cb = new CheckBoxPreference(this);

                cb.setKey(String.format(deviceAddr));
                cb.setSummary(deviceAddr);
                cb.setTitle(deviceName);

                btCategory.addPreference(cb);
            }
        }

        // the scanresult receiver will add the wifi networks to the list
        wifiCategory = (PreferenceCategory) findPreference("key_wifiNetworks");
        wifiCategory.setTitle(getResources().getString(R.string.wifi_scanning));
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            scanResults = wifiManager.getScanResults();
            addWifiNetworks();
        }
    }

    private void addWifiNetworks() {

        wifiCategory.setTitle(getResources().getString(R.string.settings_wifiDevicesTitle));

        if (scanResults != null) {
            for (ScanResult network : scanResults) {
                String networkAddr = network.BSSID;
                String networkName = network.SSID;

                CheckBoxPreference cb = new CheckBoxPreference(this);

                // trim surrounding quotes, if any
                if (networkName.startsWith("\"") && networkName.endsWith("\"")) {
                    networkName = networkName.substring(1, networkName.length() - 1);
                }

                if (networkName == null || networkName.equals("")) {
                    networkName = getResources().getString(R.string.wifi_hiddenNetwork);
                }

                cb.setKey(String.format(networkAddr));
                cb.setSummary(networkAddr);
                cb.setTitle(networkName);

                wifiCategory.addPreference(cb);
            }
        }

        unregisterReceiver(wifiReceiver);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // just update
        if (lockService != null) {
            lockService.processChanges();
        }
    }
}

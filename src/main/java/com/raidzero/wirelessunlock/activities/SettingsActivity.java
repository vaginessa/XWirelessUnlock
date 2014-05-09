package com.raidzero.wirelessunlock.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;
import com.raidzero.wirelessunlock.R;

import java.util.List;
import java.util.Set;

/**
 * Created by raidzero on 5/5/14 6:31 PM
 */
public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String tag = "WirelessUnlock/PreferenceActivity";

    //private static LockService lockService;
    private boolean isBound = false;

    // wifi stuff
    private WifiManager wifiManager = null;
    private List<ScanResult> scanResults = null;
    private WifiReceiver wifiReceiver = null;
    PreferenceCategory btCategory = null;
    PreferenceCategory wifiCategory = null;

    private AppHelper appHelper;
    /*
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
    };*/

    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appHelper = Common.getAppHelper();
        addPreferencesFromResource(R.xml.preferences);

        /*
        // bind to it
        Intent intent = new Intent(this, LockService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
        */

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
        appHelper.processChanges();
    }
}

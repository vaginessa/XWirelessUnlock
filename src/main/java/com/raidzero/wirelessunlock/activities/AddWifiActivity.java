package com.raidzero.wirelessunlock.activities;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.adapters.DeviceListAdapter;
import com.raidzero.wirelessunlock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raidzero on 5/8/14 10:37 AM
 */
public class AddWifiActivity extends ListActivity {
    private static final String tag = "WirelessUnlock/AddWifiActivity";

    private ArrayList<AppDevice> detectedWifiNetworks = new ArrayList<AppDevice>();
    private ListView list_devices = null;
    DeviceListAdapter adapter;

    private WifiManager wifiManager = null;
    private List<ScanResult> scanResults = null;
    private WifiReceiver wifiReceiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_list);

        list_devices = getListView();

        // register wifi stuff and start scan
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();


        setTitle(getResources().getString(R.string.wifi_scanning));
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter = new DeviceListAdapter(this, detectedWifiNetworks, false);
        list_devices.setAdapter(adapter);
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            Log.d(tag, "WifiReceiver!");

            scanResults = wifiManager.getScanResults();
            detectedWifiNetworks = getWifiNetworks();

            int numNetworks = detectedWifiNetworks.size();
            Log.d(tag, "Found " + numNetworks + " networks.");

            adapter.clear();
            adapter.addAll(detectedWifiNetworks);
            adapter.notifyDataSetChanged();

            setTitle(getResources().getString(R.string.action_add_wifi));
        }
    }

    private ArrayList<AppDevice> getWifiNetworks() {
        ArrayList<AppDevice> rtnList = new ArrayList<AppDevice>();

        if (scanResults != null) {
            for (ScanResult network : scanResults) {

                String networkAddr = network.BSSID;
                String networkName = network.SSID;

                // trim surrounding quotes, if any
                if (networkName.startsWith("\"") && networkName.endsWith("\"")) {
                    networkName = networkName.substring(1, networkName.length() - 1);
                }

                if (networkName == null || networkName.equals("")) {
                    networkName = getResources().getString(R.string.wifi_hiddenNetwork);
                }

                AppDevice d = new AppDevice(AppDevice.DeviceType.WIFI, networkName, networkAddr, false, true);
                //Log.d(tag, "Added network: " + networkName);
                rtnList.add(d);
            }
        }

        unregisterReceiver(wifiReceiver);
        return rtnList;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        AppDevice device = detectedWifiNetworks.get(position);

        // pass this device back to main activity
        Intent data = new Intent();
        data.putExtra("device", device);
        setResult(RESULT_OK, data);
        finish();
    }
}


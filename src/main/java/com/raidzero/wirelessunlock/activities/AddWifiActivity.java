package com.raidzero.wirelessunlock.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.adapters.DeviceListAdapter;
import com.raidzero.wirelessunlock.R;
import com.raidzero.wirelessunlock.global.Common;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by raidzero on 5/8/14 10:37 AM
 */
public class AddWifiActivity extends Activity {
    private static final String tag = "WirelessUnlock/AddWifiActivity";

    private ArrayList<AppDevice> detectedWifiNetworks = new ArrayList<AppDevice>();
    private ArrayList<AppDevice> selectedWifiNetworks = new ArrayList<AppDevice>();
    private ArrayList<AppDevice> lastScannedNetworks = new ArrayList<AppDevice>();

    private ListView networkList = null;
    private Button scanButton = null;
    private Button saveButton = null;
    private DeviceListAdapter adapter = null;
    private ProgressBar progressBar = null;

    private WifiManager wifiManager = null;
    private List<ScanResult> scanResults = null;
    private WifiReceiver wifiReceiver = null;

    private AppDelegate appDelegate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_wifi);

        appDelegate = Common.getAppDelegate();

        // view objects
        networkList = (ListView) findViewById(R.id.list_wifi_found_networks);
        scanButton = (Button) findViewById(R.id.wifi_scan_button);
        saveButton = (Button) findViewById(R.id.wifi_save_button);
        progressBar = (ProgressBar) findViewById(R.id.wifi_progress_bar);

        // set click listeners
        scanButton.setOnClickListener(scanButtonListener);
        saveButton.setOnClickListener(saveButtonListener);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter = new DeviceListAdapter(this, detectedWifiNetworks, true);
        networkList.setAdapter(adapter);

        lastScannedNetworks = appDelegate.getLastScannedNetworks();

        // if we already have a list of scanned networks, just display it
        if (lastScannedNetworks.size() > 0) {
            Log.d(tag, "saved scanned data found.");
            detectedWifiNetworks = lastScannedNetworks;
            loadScannedData(detectedWifiNetworks);
        }
        else
        {
            startScanning();
        }
    }

    class WifiReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {
            Log.d(tag, "WifiReceiver!");

            scanResults = wifiManager.getScanResults();
            detectedWifiNetworks = getWifiNetworks();

            int numNetworks = detectedWifiNetworks.size();
            Log.d(tag, "Found " + numNetworks + " networks.");

            loadScannedData(detectedWifiNetworks);
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

                if (appDelegate.addressExists(appDelegate.getTrustedWifiNetworks(), networkAddr)) {
                    Log.d(tag, "skipping existing network " + networkName);
                    continue;
                }

                AppDevice d = new AppDevice(AppDevice.DeviceType.WIFI, networkName, networkAddr, false);
                //Log.d(tag, "Added network: " + networkName);
                rtnList.add(d);
            }
        }

        unregisterReceiver(wifiReceiver);

        // save the networks in appDelegate
        appDelegate.setLastScannedNetworks(rtnList);

        return rtnList;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    // listeners
    View.OnClickListener scanButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            startScanning();
        }
    };

    View.OnClickListener saveButtonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            Log.d(tag, "save button clicked");

            selectedWifiNetworks = adapter.getCheckedDevices();

            Intent rtnIntent = new Intent();
            rtnIntent.putParcelableArrayListExtra("devices", selectedWifiNetworks);
            setResult(RESULT_OK, rtnIntent);
            finish();
        }
    };

    private void startScanning() {
        showProgressBar();

        // register wifi stuff and start scan
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
    }

    private void loadScannedData(ArrayList<AppDevice> networks) {
        adapter.clear();
        adapter.addAll(networks);
        adapter.notifyDataSetChanged();

        scanButton.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
        hideProgressBar();
    }
}


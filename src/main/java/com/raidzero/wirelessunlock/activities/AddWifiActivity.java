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

    private ListView networkList = null;
    private Button scanButton = null;
    private Button saveButton = null;
    private DeviceListAdapter adapter;
    private ProgressBar progressBar;

    private WifiManager wifiManager = null;
    private List<ScanResult> scanResults = null;
    private WifiReceiver wifiReceiver = null;

    private AppDelegate appDelegate;

    private ProgressDialog progressDialog;
    private Context context;

    private boolean scanComplete = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_wifi);

        context = this;
        appDelegate = Common.getAppDelegate();
        networkList = (ListView) findViewById(R.id.list_wifi_found_networks);
        scanButton = (Button) findViewById(R.id.wifi_scan_button);
        saveButton = (Button) findViewById(R.id.wifi_save_button);
        progressBar = (ProgressBar) findViewById(R.id.wifi_progress_bar);

        // register wifi stuff and start scan
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // set click listeners
        scanButton.setOnClickListener(scanButtonListener);
        saveButton.setOnClickListener(saveButtonListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        scanComplete = false;
        adapter = new DeviceListAdapter(this, detectedWifiNetworks, true);
        networkList.setAdapter(adapter);
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

            scanComplete = true;
            hideProgressBar();
            saveButton.setVisibility(View.VISIBLE);
            scanButton.setText("Scan again");
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
            showProgressBar();
            wifiManager.startScan();
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
}


package com.raidzero.wirelessunlock.activities;

import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.raidzero.wirelessunlock.*;
import com.raidzero.wirelessunlock.adapters.DeviceListAdapter;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.global.AppDelegate;
import com.raidzero.wirelessunlock.global.Common;
import com.raidzero.wirelessunlock.global.DeviceListLoader;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/7/14 7:51 PM
 */
public class MainActivity extends ActionBarActivity {

    private static final String tag = "WirelessUnlock/MainActivity";

    public static AppDelegate appDelegate;

    private MessageReceiver messageReceiver = null;
    private RefreshDevicesReceiver refreshDevicesReceiver = null;

    // views
    private static TextView lockStatusView = null;
    private static ListView trustedBluetoothList = null;
    private static ListView trustedWifiList = null;

    // lists
    ArrayList<AppDevice> trustedBluetoothDevices = new ArrayList<AppDevice>();
    ArrayList<AppDevice> trustedWifiNetworks = new ArrayList<AppDevice>();

    // adapters
    DeviceListAdapter btAdapter;
    DeviceListAdapter wifiAdapter;

    // device admin
    ComponentName deviceAdmin;
    DevicePolicyManager devicePolicyManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        appDelegate = (AppDelegate) getApplicationContext();

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        deviceAdmin = appDelegate.getDeviceAdmin();

        if (!devicePolicyManager.isAdminActive(deviceAdmin)) {

            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    getResources().getString(R.string.device_admin_description));

            startActivityForResult(intent, Common.ENABLE_ADMIN_REQUEST_CODE);
        }

        lockStatusView = (TextView) findViewById(R.id.textView_lockStatus);

        trustedBluetoothList = (ListView) findViewById(R.id.list_trusted_bluetooth_devices);
        trustedWifiList = (ListView) findViewById(R.id.list_trusted_wifi_devices);

        appDelegate.loadDevices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_add_bluetooth:
                startAddBluetoothDevice();
                return true;
            case R.id.action_add_wifi:
                startAddWifiNetwork();
                return true;
            case R.id.action_log:
                openLog();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        trustedBluetoothDevices = appDelegate.getTrustedBluetoothDevices();
        trustedWifiNetworks = appDelegate.getTrustedWifiNetworks();

        btAdapter = new DeviceListAdapter(this, appDelegate.getTrustedBluetoothDevices(), false);
        wifiAdapter = new DeviceListAdapter(this, appDelegate.getTrustedWifiNetworks(), false);

        appDelegate.loadDevices();

        trustedBluetoothList.setAdapter(btAdapter);
        trustedWifiList.setAdapter(wifiAdapter);

        trustedBluetoothList.setOnItemClickListener(bluetoothClickListener);
        trustedWifiList.setOnItemClickListener(wifiClickListener);

        if (!appDelegate.isPrefEnabled("enableApp")) {
            Log.d(tag, "control disabled");
            lockStatusView.setText(getResources().getString(R.string.main_appDisabled));
        } else {
            Log.d(tag, "control enabled");

            // register message receiver if not already
            if (messageReceiver == null) {
                messageReceiver = new MessageReceiver();
            }
            IntentFilter messageIntentFilter = new IntentFilter(Common.messageIntentAction);
            registerReceiver(messageReceiver, messageIntentFilter);

            // register deviceRefreshReceiver
            if (refreshDevicesReceiver == null) {
                refreshDevicesReceiver = new RefreshDevicesReceiver();
                IntentFilter refreshIntentFilter = new IntentFilter(Common.refreshDevicesIntentAction);
                registerReceiver(refreshDevicesReceiver, refreshIntentFilter);
            }

            appDelegate.processChanges();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onPause() {
        super.onPause();

        // unregister message receiver
        if (messageReceiver != null) {
            try {
                unregisterReceiver(messageReceiver);
            } catch (IllegalArgumentException e) {
                Log.d(tag, "messageReceiver " + e.getMessage());
            }
        }

        // unregister device refresh receiver
        if (refreshDevicesReceiver != null) {
            try {
                unregisterReceiver(refreshDevicesReceiver);
            } catch (IllegalArgumentException e) {
                Log.d(tag, "refreshDevicesReceiver " + e.getMessage());
            }
        }

        appDelegate.writeDeviceFile();
    }

    private void openLog() {
        Intent i = new Intent(this, LogActivity.class);
        startActivity(i);
    }

    private void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void startAddWifiNetwork() {
        Intent i = new Intent(this, AddWifiActivity.class);
        startActivityForResult(i, Common.ADD_DEVICE_REQUEST_CODE);
    }

    private void startAddBluetoothDevice() {
        Intent i = new Intent(this, AddBluetoothActivity.class);
        startActivityForResult(i, Common.ADD_DEVICE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppDevice d;
        ArrayList<AppDevice> devices;
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Common.ADD_DEVICE_REQUEST_CODE:
                    d = data.getExtras().getParcelable("device");
                    if (d != null) {
                        appDelegate.addTrustedDevice(d);
                    }
                    devices = data.getParcelableArrayListExtra("devices");

                    if (devices != null) {
                        appDelegate.addTrustedDevices(devices);
                    }
                    break;
                case Common.CHANGE_DEVICE_REQUEST_CODE:
                    d = data.getExtras().getParcelable("device");
                    boolean remove = data.getBooleanExtra("remove", false);
                    if (remove) {
                        appDelegate.removeTrustedDevice(d);
                    } else {
                        appDelegate.updateTrustedDevice(d.getAddress(), d);
                    }
                    break;
            }
        }
    }

    // click listeners
    AdapterView.OnItemClickListener wifiClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.d(tag, "clickListener fired on " + position);
            AppDevice d = trustedWifiNetworks.get(position);

            Intent i = new Intent(getApplicationContext(), DeviceSettingsActivity.class);
            i.putExtra("device", d);
            startActivityForResult(i, Common.CHANGE_DEVICE_REQUEST_CODE);
        }
    };

    AdapterView.OnItemClickListener bluetoothClickListener = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            Log.d(tag, "clickListener fired on " + position);
            AppDevice d = trustedBluetoothDevices.get(position);

            Intent i = new Intent(getApplicationContext(), DeviceSettingsActivity.class);
            i.putExtra("device", d);
            startActivityForResult(i, Common.CHANGE_DEVICE_REQUEST_CODE);
        }
    };

    // broadcast receivers
    public class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(tag, "MessageReceiver!");

            String lockStatus = intent.getStringExtra("message");

            Log.d(tag, "lockStatus: " + lockStatus);

            if (lockStatus != null) {
                lockStatusView.setText(lockStatus);
            }
        }
    }

    public class RefreshDevicesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(tag, "RefreshDevicesReceiver!");

            trustedBluetoothDevices = appDelegate.getTrustedBluetoothDevices();
            trustedWifiNetworks = appDelegate.getTrustedWifiNetworks();

            btAdapter.notifyDataSetChanged();
            wifiAdapter.notifyDataSetChanged();
        }
    }
}

package com.raidzero.wirelessunlock.activities;

import android.content.*;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import com.raidzero.wirelessunlock.*;
import com.raidzero.wirelessunlock.adapters.DeviceListAdapter;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/7/14 7:51 PM
 */
public class MainActivity extends ActionBarActivity {

    private static final String tag = "WirelessUnlock/MainActivity";

    private SharedPreferences sharedPreferences;
    public static AppHelper appHelper;

    public static LockService lockService;
    private MessageReceiver messageReceiver;

    public ServiceConnection myConnection;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        appHelper = new AppHelper();
        myConnection = appHelper.getServiceConnection();

        // bind to service
        Intent intent = new Intent(this, LockService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);

        sendBroadcast(new Intent("com.raidzero.wirelessunlock.APP_STARTED"));

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        lockStatusView = (TextView) findViewById(R.id.textView_lockStatus);

        trustedBluetoothList = (ListView) findViewById(R.id.list_trusted_bluetooth_devices);
        trustedWifiList = (ListView) findViewById(R.id.list_trusted_wifi_devices);

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
                addBluetoothDevice();
                return true;
            case R.id.action_add_wifi:
                addWifiNetwork();
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

        btAdapter = new DeviceListAdapter(this, trustedBluetoothDevices, true);
        wifiAdapter = new DeviceListAdapter(this, trustedWifiNetworks, true);

        trustedBluetoothList.setAdapter(btAdapter);
        trustedWifiList.setAdapter(wifiAdapter);

        // register message receiver if not already
        if (messageReceiver == null) {
            messageReceiver = new MessageReceiver();
        }
        IntentFilter ifilter = new IntentFilter(Common.messageIntent);
        registerReceiver(messageReceiver, ifilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (appHelper.isServiceBound()) {
            unbindService(myConnection);
        }

        // unregister message receiver
        if (messageReceiver != null) {
            unregisterReceiver(messageReceiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

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

    private void openSettings() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void addWifiNetwork() {
        Intent i = new Intent(this, AddWifiActivity.class);
        startActivityForResult(i, Common.addWifiRequestCode);
    }

    private void addBluetoothDevice() {
        Intent i = new Intent(this, AddBluetoothActivity.class);
        startActivityForResult(i, Common.addBluetoothRequestCode);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Common.addBluetoothRequestCode:
                    String deviceAddress = data.getDataString();
                    Log.d(tag, "received BT address: " + deviceAddress);
                    // TODO: do something with this
                    break;
                case Common.addWifiRequestCode:
                    String wifiAddress = data.getDataString();
                    Log.d(tag, "received wifi address: " + wifiAddress);
                    // TODO: do something with this
                    break;
            }
        }
    }
}

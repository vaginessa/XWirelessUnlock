package com.raidzero.wirelessunlock.activities;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.adapters.DeviceListAdapter;
import com.raidzero.wirelessunlock.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by raidzero on 5/8/14 10:37 AM
 */
public class AddBluetoothActivity extends ListActivity {
    private static final String tag = "WirelessUnlock/AddBluetoothActivity";

    private ArrayList<AppDevice> pairedBluetoothDevices = null;
    private ListView list_devices = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.device_list);

        list_devices = getListView();
    }

    @Override public void onResume() {
        super.onResume();

        pairedBluetoothDevices = getPairedDevices();
        if (pairedBluetoothDevices.size() > 0) {
            DeviceListAdapter adapter = new DeviceListAdapter(this, pairedBluetoothDevices, false);
            list_devices.setAdapter(adapter);
        } else {
            // display no paired devices found
            noDevicesFound();
        }
    }

    private void noDevicesFound() {
        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setMessage(getResources().getString(R.string.bluetooth_no_devices));
        alert.setTitle("Error");
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });
        alert.create().show();
    }

    private ArrayList<AppDevice> getPairedDevices() {

        ArrayList<AppDevice> rtnList = new ArrayList<AppDevice>();

        // get list of bluetooth devices
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices != null) {
            for (BluetoothDevice d : pairedDevices) {
                String name = d.getName();
                String address = d.getAddress();

                // make AppDevice
                AppDevice device = new AppDevice(AppDevice.DeviceType.BLUETOOTH, name, address, false);

                //Log.d(tag, String.format("created device. (name: %s, address: %s)", device.getName(), device.getAddress()));
                // Add to arraylist
                rtnList.add(device);
            }
        }

        return rtnList;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        AppDevice device = pairedBluetoothDevices.get(position);

        // pass this device back to main activity
        Intent data = new Intent();
        data.putExtra("device", device);
        setResult(RESULT_OK, data);
        finish();
    }
}

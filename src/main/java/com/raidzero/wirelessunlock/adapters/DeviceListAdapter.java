package com.raidzero.wirelessunlock.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class DeviceListAdapter extends ArrayAdapter<AppDevice> {

    private static final String tag="WirelessUnlock/DeviceListAdapter";

    private boolean enableCheckbox = false;
    private boolean checkboxes[];
    private int numDevices = 0;

    public DeviceListAdapter(Context context, ArrayList<AppDevice> devices, boolean checkEnabled) {
        super(context, R.layout.device_row, devices);
        this.enableCheckbox = checkEnabled;
        this.numDevices = devices.size();

        checkboxes = new boolean[numDevices];

        for (int i = 0; i < numDevices; i++) {
            checkboxes[i] = false;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get AppDevice for this position
        AppDevice d = getItem(position);

        // if we arent reusing a view, inflate one
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.device_row, parent, false);
        }

        // get the views
        TextView txtName = (TextView) convertView.findViewById(R.id.txt_deviceName);
        TextView txtAddr = (TextView) convertView.findViewById(R.id.txt_deviceAddr);
        final CheckBox chkBox = (CheckBox) convertView.findViewById(R.id.chk_deviceEnable);

        if (enableCheckbox) {
            if (checkboxes.length > 0) {
                chkBox.setChecked(checkboxes[position]);
            }

            chkBox.setTag(Integer.valueOf(position));

            chkBox.setOnClickListener(new CompoundButton.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkboxes[(Integer) v.getTag()] = chkBox.isChecked();
                }
            });

        } else {
            // hide checkbox
            chkBox.setVisibility(View.GONE);
        }

        // set their values
        txtName.setText(d.getName());
        txtAddr.setText(d.getAddress());

        // return completed view
        return convertView;
    }

    @Override
    public void addAll(Collection<? extends AppDevice> devices) {
        super.addAll(devices);

        this.numDevices = devices.size();
        checkboxes = new boolean[numDevices];

        for (int i = 0; i < numDevices; i++) {
            Log.d(tag, "setting position " + i);
            checkboxes[i] = false;
        }
    }

    public ArrayList<AppDevice> getCheckedDevices() {
        ArrayList<AppDevice> rtn = new ArrayList<AppDevice>();

        int numCheckboxes = checkboxes.length;

        for (int i = 0; i < numCheckboxes; i++) {
            if (checkboxes[i]) {
                rtn.add(getItem(i));
            }
        }

        return rtn;
    }
}
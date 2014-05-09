package com.raidzero.wirelessunlock.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.raidzero.wirelessunlock.global.AppDevice;
import com.raidzero.wirelessunlock.R;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 1:17 PM
 */
public class DeviceListAdapter extends ArrayAdapter<AppDevice> {

    private boolean enableCheckbox = false;

    public DeviceListAdapter(Context context, ArrayList<AppDevice> devices, boolean checkEnabled) {
        super(context, R.layout.device_row, devices);
        this.enableCheckbox = checkEnabled;
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
        CheckBox chkBox = (CheckBox) convertView.findViewById(R.id.chk_deviceEnable);

        // set their values
        txtName.setText(d.getName());
        txtAddr.setText(d.getAddress());

        if (!enableCheckbox) {
            // hide checkbox
            chkBox.setVisibility(View.GONE);
        }

        // return completed view
        return convertView;
    }
}
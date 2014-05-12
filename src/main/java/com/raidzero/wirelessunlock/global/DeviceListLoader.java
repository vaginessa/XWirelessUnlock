package com.raidzero.wirelessunlock.global;

import android.util.Log;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by raidzero on 5/9/14 1:55 PM
 */
public class DeviceListLoader {
    private static final String tag = "WirelessUnlock/DeviceListLoader";

    public static void writeDeviceList(ArrayList<AppDevice> list, FileOutputStream stream) {
        OutputStreamWriter writer;
        try {
            writer = new OutputStreamWriter(stream);
            for (AppDevice device : list) {
                String dType = "";
                switch (device.getType()) {
                    case BLUETOOTH:
                        dType = "BLUETOOTH";
                        break;
                    case WIFI:
                        dType = "WIFI";
                        break;
                }

                String line = String.format(
                        "%s|%s|%s|%s|%s\n",
                        dType, device.getName(), device.getAddress(),
                        String.valueOf(device.getChargingOnly()), device.getEnabled());

                writer.write(line);
            }

            writer.close();

        } catch (Exception e) {
            // do nothing
        }
    }


    public static ArrayList<AppDevice> loadDeviceList(AppDevice.DeviceType targetType, FileInputStream stream) {
        Log.d(tag, "loadDeviceList() called");
        BufferedReader reader;
        ArrayList<AppDevice> rtn = new ArrayList<AppDevice>();

        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                //Log.d(tag, "line: " + line);

                AppDevice.DeviceType dType = null;

                String[] data = line.split("\\|");
                String strType = data[0];
                String name = data[1];
                String address = data[2];
                String strOnlyCharging = data[3];
                String strEnabled = data[4];

                if (strType.equals("BLUETOOTH")) {
                    dType = AppDevice.DeviceType.BLUETOOTH;
                }
                if (strType.equals("WIFI")) {
                    dType = AppDevice.DeviceType.WIFI;
                }

                if (targetType != dType) {
                    Log.d(tag, "Not loading device of type " + strType);
                    continue;
                }

                //Log.d(tag, String.format("strType: %s\nname: %s\naddress:%s\nstrOnlyCharging: %s\nstrEnabled: %s",
                //        strType, name, address, strOnlyCharging, strEnabled));

                Boolean enabled = Boolean.valueOf(strEnabled);
                Boolean onlyCharging = Boolean.valueOf(strOnlyCharging);

                rtn.add(new AppDevice(dType, name, address, onlyCharging, enabled));
            }

            String strType = (targetType == AppDevice.DeviceType.BLUETOOTH) ? "BLUETOOTH" : "WIFI";
            Log.d(tag, String.format("returning %d %s devices.", rtn.size(), strType));

            stream.close();
            return rtn;

        } catch (Exception e) {
            // nothing
        }

        return null;
    }
}

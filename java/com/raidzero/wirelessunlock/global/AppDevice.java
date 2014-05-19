package com.raidzero.wirelessunlock.global;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Created by raidzero on 5/8/14 10:49 AM
 */
public class AppDevice implements Parcelable {
    private static final String tag = "WirelessUnlock/AppDevice";

    public enum DeviceType {
        BLUETOOTH, WIFI
    }

    private DeviceType deviceType;
    private String deviceName;
    private String deviceAddress;
    private boolean chargingOnly;

    public AppDevice(DeviceType type, String name, String address, boolean chargingOnly) {
        this.deviceType = type;
        this.deviceName = name;
        this.deviceAddress = address;
        this.chargingOnly = chargingOnly;

        //Log.d(tag, String.format("Created device %s", deviceName));
    }

    public String getName() {
        //Log.d(tag, "getName() returning: " + deviceName);
        return deviceName;
    }

    public String getAddress() {
        //Log.d(tag, "getAddress() returning: " + deviceAddress);
        return deviceAddress;
    }

    public DeviceType getType() {
        return deviceType;
    }

    public boolean getChargingOnly() {
        return chargingOnly;
    }


    // parcelable stuff
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        String dType = "";
        switch (deviceType) {
            case BLUETOOTH:
                dType = "BLUETOOTH";
                break;
            case WIFI:
                dType = "WIFI";
                break;
        }

        dest.writeString(dType);
        dest.writeString(deviceName);
        dest.writeString(deviceAddress);
        dest.writeString(String.valueOf(chargingOnly));
    }

    // this is needed to make the parcel
    public AppDevice(Parcel source){
        //Log.d(tag, "ParcelData(Parcel source): time to put back parcel data");
        String dType;
        dType = source.readString();
        deviceName = source.readString();
        deviceAddress = source.readString();
        chargingOnly = Boolean.valueOf(source.readString());

        if (dType.equals("BLUETOOTH")) {
            deviceType = DeviceType.BLUETOOTH;
        }

        if (dType.equals("WIFI")) {
            deviceType = DeviceType.WIFI;
        }
    }

    public static final Creator CREATOR = new Creator<AppDevice>() {
        public AppDevice createFromParcel(Parcel source) {
            return new AppDevice(source);
        }
        public AppDevice[] newArray(int size) {
            return new AppDevice[size];
        }
    };
}

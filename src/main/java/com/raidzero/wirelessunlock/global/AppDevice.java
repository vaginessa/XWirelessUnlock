package com.raidzero.wirelessunlock.global;

/**
 * Created by raidzero on 5/8/14 10:49 AM
 */
public class AppDevice {
    private static final String tag = "WirelessUnlock/AppDevice";

    public enum DeviceType {
        BLUETOOTH, WIFI
    }

    private DeviceType deviceType;
    private String deviceName;
    private String deviceAddress;

    public AppDevice(DeviceType type, String name, String address) {
        this.deviceType = type;
        this.deviceName = name;
        this.deviceAddress = address;
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
}

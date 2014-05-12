package com.raidzero.wirelessunlock.global;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.util.Log;
import com.raidzero.wirelessunlock.R;
import com.raidzero.wirelessunlock.UnlockService;
import com.raidzero.wirelessunlock.receivers.BluetoothReceiver;
import com.raidzero.wirelessunlock.receivers.PowerReceiver;
import com.raidzero.wirelessunlock.receivers.WifiReceiver;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by raidzero on 5/8/14 2:18 PM
 */
public class AppDelegate extends Application {
    private static final String tag = "WirelessUnlock/AppDelegate";

    private boolean isServiceRunning = false;

    private SharedPreferences sharedPreferences;
    private ArrayList<String> connectedAddresses = new ArrayList<String>();
    private DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd @ HH:mm:ss");

    private NotificationManager notificationManager;
    private Notification notification;

    private boolean notificationDisplayed = false;
    private Bitmap largeIcon;


    // receivers
    private BluetoothReceiver bluetoothReceiver = null;
    private WifiReceiver wifiReceiver = null;
    private PowerReceiver powerReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "onCreate()");

        Common.appDelegate = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    }

    public void addConnectedAddress(String address) {
        if (!connectedAddresses.contains(address)) {
            connectedAddresses.add(address);
        }
    }

    public void removeConnectedAddress(String address) {
        if (connectedAddresses.contains(address)) {
            connectedAddresses.remove(address);
        }
    }

    public void startUnlockService(String reason) {
        if (!isServiceRunning) {
            startService(new Intent(this, UnlockService.class));
            isServiceRunning = true;
            writeLog(reason + " Started service");
            showNotification();
        }
        broadcastServiceState();
    }

    public void stopUnlockService(String reason) {
        if (isServiceRunning) {
            stopService(new Intent(this, UnlockService.class));
            isServiceRunning = false;
            writeLog(reason + " Stopped service");
        }
        dismissNotification();
        broadcastServiceState();
    }

    public void broadcastServiceState() {
        Intent i = new Intent();
        i.setAction(Common.messageIntent);

        String data;

        if (isServiceRunning) {
            data = getResources().getString(R.string.main_lockscreen_disabled);
        } else {
            data = getResources().getString(R.string.main_lockscreen_enabled);
        }

        i.putExtra("message", data);
        sendBroadcast(i);
    }

    private boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public boolean isPrefEnabled(String key) {
        boolean rtn = false;

        if (sharedPreferences != null) {
            rtn = sharedPreferences.getBoolean(key, false);
            Log.d(tag, "isPrefEnabled(" + key + ")? " + rtn);
        }
        return rtn;
    }

    private String getConnectedWifiAddress() {
        try {
            WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            return wifiInfo.getBSSID().replace("\"", "");
        } catch (Exception e) {
            return null;
        }
    }

    public ArrayList<AppDevice> getTrustedDevices(AppDevice.DeviceType type) {
        ArrayList<AppDevice> rtn;

        try {
            rtn = DeviceListLoader.loadDeviceList(type, openFileInput(Common.deviceFile));
        } catch (Exception e) {
            Log.d(tag, "getTrustedDevices() error: " + e.getMessage());
            return null;
        }

        int numDevices = 0;
        try {
            numDevices = rtn.size();
        } catch (NullPointerException e) {
            // leave it at 0
        }

        Log.d(tag, "getTrustedDevices() returning " + numDevices + " devices.");
        return rtn;
    }

    private boolean isAddressTrusted(String address) {
        Log.d(tag, "isAddressTrusted(" + address + ")?");

        ArrayList<AppDevice> trustedDevices = new ArrayList<AppDevice>();

        ArrayList<AppDevice> trustedWifi = getTrustedDevices(AppDevice.DeviceType.WIFI);
        ArrayList<AppDevice> trustedBluetooth = getTrustedDevices(AppDevice.DeviceType.BLUETOOTH);

        if (trustedBluetooth != null) {
            trustedDevices.addAll(trustedBluetooth);
        }
        if (trustedWifi != null) {
            trustedDevices.addAll(trustedWifi);
        }

        for (AppDevice device : trustedDevices) {
            if (device.getAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    private AppDevice getDeviceFromAddress(AppDevice.DeviceType type, String address) {
        ArrayList<AppDevice> trustedDevices = getTrustedDevices(type);

        for (AppDevice d : trustedDevices) {
            if (d.getAddress().equals(address)) {
                Log.d(tag, "getDeviceFromAddress() returning device: " + d.getName());
                return d;
            }
        }

        return null;
    }

    public void processChanges() {
        Log.d(tag, "processChanges()");
        ArrayList<String> connectedDevices = new ArrayList<String>();

        // add wifi, if any
        connectedDevices.add(getConnectedWifiAddress());

        // add bluetooth, if any
        connectedDevices.addAll(connectedAddresses);

        boolean startService = false;
        String reason = "";

        for (String d : connectedDevices) {

            if (isAddressTrusted(d)) {
                AppDevice device = null;
                String deviceType;

                // bluetooth?
                if (connectedAddresses.contains(d)) {
                    device = getDeviceFromAddress(AppDevice.DeviceType.BLUETOOTH, d);
                    deviceType = "BT device";
                } else {
                    // must be wifi
                    device = getDeviceFromAddress(AppDevice.DeviceType.WIFI, d);
                    deviceType = "WiFi network";
                }

                if (device.getChargingOnly()) {
                    if (isCharging()) {
                        reason = String.format("%s (%s) trusted & plugged in.", deviceType, device.getName());
                        startService = true;
                        break;
                    } else {
                        reason = String.format("%s (%s) trusted but not plugged in.", deviceType, device.getName());
                        startService = false;
                        break;
                    }
                } else {
                    reason = String.format("%s (%s) trusted.", deviceType, device.getName());
                    startService = true;
                    break;
                }
            }
        }

        if (!reason.equals("")) {
            if (startService) {
                startUnlockService(reason);
            } else {
                stopUnlockService(reason);
            }
            return;
        }

        reason = "No trusted devices found.";
        stopUnlockService(reason);
    }

    public void writeLog(String msg) {
        Date date = new Date();

        try {
            FileOutputStream fp = openFileOutput(Common.logFile, Context.MODE_APPEND);
            OutputStreamWriter osw = new OutputStreamWriter(fp , "UTF-8");
            BufferedWriter writer = new BufferedWriter(osw);

            writer.write(logDateFormat.format(date) + ": " + msg + "\n");

            writer.close();
        } catch (Exception e) {
            Log.d(tag, e.getMessage());
        }

        Log.d(tag, "writeLog(): " + msg);
    }

    public void dismissNotification() {
        if (notification != null) {
            notificationManager.cancel(R.string.service_notification_id);
            notificationDisplayed = false;
        }
    }

    public void showNotification() {
        if (isPrefEnabled("showNotifications") && isServiceRunning) {
            if (!notificationDisplayed) {
                Log.d(tag, "showNotification()");
                notification  = new Notification.Builder(this)
                        .setContentTitle(getResources().getString(R.string.service_notificationTitle))
                        .setContentText(getResources().getString(R.string.service_notificationText))
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(largeIcon)
                        .setOngoing(true)
                        .build();
                notificationManager.notify(R.string.service_notification_id, notification);
                notificationDisplayed = true;
            }
        }
    }
}

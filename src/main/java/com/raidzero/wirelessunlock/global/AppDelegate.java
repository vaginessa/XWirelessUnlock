package com.raidzero.wirelessunlock.global;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
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
import com.raidzero.wirelessunlock.activities.MainActivity;
import com.raidzero.wirelessunlock.receivers.AdminReceiver;
import com.raidzero.wirelessunlock.receivers.ScreenReceiver;

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

    // notification stuff
    private NotificationManager notificationManager;
    private Notification notification;
    private boolean notificationDisplayed = false;
    private Bitmap largeIcon;
    private Intent mainActivityIntent;

    private ArrayList<AppDevice> trustedBluetoothDevices = new ArrayList<AppDevice>();
    private ArrayList<AppDevice> trustedWifiNetworks = new ArrayList<AppDevice>();

    // device admin stuff
    private boolean deviceAdminActive = false;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName deviceAdmin;
    private int currentPasswordQuality;
    private int currentPasswordLength;

    private ScreenReceiver screenReceiver = null;
    public enum ScreenPowerState {
        ON, OFF
    };

    private ScreenPowerState screenState;

    private ArrayList<AppDevice> lastScannedNetworks = new ArrayList<AppDevice>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(tag, "onCreate()");

        Common.appDelegate = this;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        this.mainActivityIntent = new Intent(this, MainActivity.class);

        this.devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        this.deviceAdmin = new ComponentName(this, AdminReceiver.class);

        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        this.largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        // register screen receiver
        if (screenReceiver == null) {
            screenReceiver = new ScreenReceiver();

            IntentFilter screenFilter = new IntentFilter();
            screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
            screenFilter.addAction(Intent.ACTION_SCREEN_ON);

            registerReceiver(screenReceiver, screenFilter);
        }
    }

    public ArrayList<AppDevice> getTrustedBluetoothDevices() {
        return trustedBluetoothDevices;
    }

    public ArrayList<AppDevice> getTrustedWifiNetworks() {
        return trustedWifiNetworks;
    }

    public void removeTrustedDevice(AppDevice device) {
        ArrayList<AppDevice> list =
                (device.getType() == AppDevice.DeviceType.BLUETOOTH) ?
                        trustedBluetoothDevices : trustedWifiNetworks;

        removeFromList(list, device.getAddress());

        broadcastDeviceChange();
    }

    public void addTrustedDevices(ArrayList<AppDevice> devices) {
        for (AppDevice d : devices) {
            addTrustedDevice(d);
        }
    }

    public void addTrustedDevice(AppDevice newDevice) {
        ArrayList<AppDevice> list =
                (newDevice.getType() == AppDevice.DeviceType.BLUETOOTH) ?
                        trustedBluetoothDevices : trustedWifiNetworks;

        if (!addressExists(list, newDevice.getAddress())) {
            list.add(newDevice);
        }

        broadcastDeviceChange();
    }

    private void removeFromList(ArrayList<AppDevice> list, String address) {
        int i = 0;
        boolean found = false;

        for (AppDevice d : list) {
            if (d.getAddress().equals(address)) {
                found = true;
                break;
            }
            i++;
        }

        if (found) {
            list.remove(i);
        }
    }

    public boolean addressExists(ArrayList<AppDevice> list, String address) {
        for (AppDevice d : list) {
            if (d.getAddress().equals(address)) {
                return true;
            }
        }

        return false;
    }

    public void updateTrustedDevice(String address, AppDevice newDevice) {
        AppDevice.DeviceType type = newDevice.getType();

        AppDevice oldDevice = getDeviceFromAddress(type, address);

        removeTrustedDevice(oldDevice);
        addTrustedDevice(newDevice);

        broadcastDeviceChange();
        writeDeviceFile();
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

    public synchronized void startUnlockService(String reason) {
        if (!isServiceRunning) {

            if (devicePolicyManager.isAdminActive(deviceAdmin)) {
                // save password params
                currentPasswordQuality = devicePolicyManager.getPasswordQuality(deviceAdmin);
                currentPasswordLength = devicePolicyManager.getPasswordMinimumLength(deviceAdmin);

                // relax
                devicePolicyManager.setPasswordQuality(deviceAdmin, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                devicePolicyManager.setPasswordMinimumLength(deviceAdmin, 0);

                Log.d(tag, "device admin: set stuff");
            }

            startService(new Intent(this, UnlockService.class));
            isServiceRunning = true;

            writeLog(reason + " Started service");
            showNotification();
        }
        broadcastServiceState();
    }

    public synchronized void stopUnlockService(String reason) {
        if (isServiceRunning) {

            // stop device administrator
            if (devicePolicyManager.isAdminActive(deviceAdmin)) {
                // re-apply password params
                devicePolicyManager.setPasswordQuality(deviceAdmin, currentPasswordQuality);
                devicePolicyManager.setPasswordMinimumLength(deviceAdmin, currentPasswordLength);

                if (screenState == ScreenPowerState.OFF) {
                    devicePolicyManager.lockNow();
                }
                Log.d(tag, "device admin: stopped stuff");
            }

            stopService(new Intent(this, UnlockService.class));
            isServiceRunning = false;
            writeLog(reason + " Stopped service");
        }
        dismissNotification();

        broadcastServiceState();
    }

    public void broadcastServiceState() {
        Intent i = new Intent();
        i.setAction(Common.messageIntentAction);

        String data;

        if (isServiceRunning) {
            data = getResources().getString(R.string.main_lockscreen_disabled);
        } else {
            data = getResources().getString(R.string.main_lockscreen_enabled);
        }

        if (!sharedPreferences.getBoolean("enableApp", true)) {
            data = getResources().getString(R.string.main_appDisabled);
        }

        i.putExtra("message", data);
        sendBroadcast(i);
    }

    public void broadcastDeviceChange() {
        writeDeviceFile();

        Intent i = new Intent();
        i.setAction(Common.refreshDevicesIntentAction);

        i.putExtra("bluetoothDevices", trustedBluetoothDevices);
        i.putExtra("wifiNetworks", trustedWifiNetworks);

        sendBroadcast(i);
    }

    private boolean isCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
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
//            Log.d(tag, "getTrustedDevices() error: " + e.getMessage());
            return null;
        }

        int numDevices = 0;
        try {
            numDevices = rtn.size();
        } catch (NullPointerException e) {
            // leave it at 0
        }

//        Log.d(tag, "getTrustedDevices() returning " + numDevices + " devices.");
        return rtn;
    }

    private boolean isAddressTrusted(String address) {
//        Log.d(tag, "isAddressTrusted(" + address + ")?");

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
//                Log.d(tag, "getDeviceFromAddress() returning device: " + d.getName());
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
                AppDevice device;
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
        if (notificationDisplayed) {
            notificationManager.cancel(R.string.service_notification_id);
            notificationDisplayed = false;
        }
    }

    public void showNotification() {
        if (sharedPreferences.getBoolean("showNotifications", false) && isServiceRunning) {
            if (!notificationDisplayed) {
//                Log.d(tag, "showNotification()");
                notification  = new Notification.Builder(this)
                        .setContentTitle(getResources().getString(R.string.service_notificationTitle))
                        .setContentText(getResources().getString(R.string.service_notificationText))
                        .setTicker(getResources().getString(R.string.service_notificationTitle))
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(largeIcon)
                        .setOngoing(true)
                        .setContentIntent(PendingIntent.getActivity(this, 0, mainActivityIntent, 0))
                        .build();
                notificationManager.notify(R.string.service_notification_id, notification);
                notificationDisplayed = true;
            }
        }
    }

    public void writeDeviceFile() {
        ArrayList<AppDevice> allTrustedDevices = new ArrayList<AppDevice>();
        allTrustedDevices.addAll(trustedBluetoothDevices);
        allTrustedDevices.addAll(trustedWifiNetworks);

        try {
            DeviceListLoader.writeDeviceList(allTrustedDevices, openFileOutput(Common.deviceFile, Context.MODE_PRIVATE));
        } catch (Exception e) {
            // nothing
        }

        processChanges();
    }

    public void loadDevices() {
        try {
            trustedBluetoothDevices = DeviceListLoader.loadDeviceList(
                    AppDevice.DeviceType.BLUETOOTH, openFileInput(Common.deviceFile));
            trustedWifiNetworks = DeviceListLoader.loadDeviceList(
                    AppDevice.DeviceType.WIFI, openFileInput(Common.deviceFile));

//            Log.d(tag, String.format("Got %d BT devices", trustedBluetoothDevices.size()));
//            Log.d(tag, String.format("Got %d Wifi devices", trustedWifiNetworks.size()));
        } catch (Exception e) {
            // nothing
        }

//        Log.d(tag, "loadDevices() done");
    }

    // device admin stuff
    public void setDeviceAdminActive() {
        deviceAdminActive = true;
    }

    public void setDeviceAdminInactive() {
        deviceAdminActive = false;
    }

    public ComponentName getDeviceAdmin() {
        return deviceAdmin;
    }

    public void setScreenState(ScreenPowerState state) {
        if (state == ScreenPowerState.OFF) {
            screenState = ScreenPowerState.OFF;
        } else {
            screenState = ScreenPowerState.ON;
        }
    }

    public void setLastScannedNetworks(ArrayList<AppDevice> networks) {
        lastScannedNetworks = networks;
//        Log.d(tag, "added " + networks.size() + " to scanned data");
    }

    public ArrayList<AppDevice> getLastScannedNetworks() {
//        Log.d(tag, "returning " + lastScannedNetworks.size() + " scanned network data");
        return lastScannedNetworks;
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }
}

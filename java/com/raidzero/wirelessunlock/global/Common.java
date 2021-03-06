package com.raidzero.wirelessunlock.global;

import com.raidzero.wirelessunlock.activities.MainActivity;

/**
 * Created by raidzero on 5/8/14 9:42 AM
 */
public class Common {
    public static final String tag = "WirelessUnlock/Common";

    public static final String deviceFile = "devices.txt";
    public static final String logFile = "log.txt";

    // Intents
    public static final String messageIntentAction = "com.raidzero.wirelessunlock.SERVICE_MESSAGE";
    public static final String refreshDevicesIntentAction = "com.raidzero.wirelessunlock.REFRESH_DEVICES";

    public static AppDelegate appDelegate;

    // activity request codes
    public static final int ADD_DEVICE_REQUEST_CODE = 1000;
    public static final int CHANGE_DEVICE_REQUEST_CODE = 1001;
    public static final int ENABLE_ADMIN_REQUEST_CODE = 2000;

    public static AppDelegate getAppDelegate() {
        if (MainActivity.appDelegate != null) {
            return MainActivity.appDelegate;
        } else {
            return appDelegate;
        }
    }
}

package com.raidzero.wirelessunlock.global;

import android.util.Log;
import com.raidzero.wirelessunlock.activities.MainActivity;

/**
 * Created by raidzero on 5/8/14 9:42 AM
 */
public class Common {
    public static final String tag = "WirelessUnlock/Common";
    public static final String messageIntent = "com.raidzero.wirelessunlock.SERVICE_MESSAGE";
    public static final String deviceFile = "devices.txt";

    public static AppHelper appHelper;

    // activity request codes
    public static final int addDeviceRequestCode = 1000;

    public static AppHelper getAppHelper() {
        if (MainActivity.appHelper != null) {
            return MainActivity.appHelper;
        } else {
            return appHelper;
        }
    }
}

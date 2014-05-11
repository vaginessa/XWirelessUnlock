package com.raidzero.wirelessunlock;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import com.raidzero.wirelessunlock.global.AppHelper;
import com.raidzero.wirelessunlock.global.Common;


/**
 * Created by raidzero on 5/10/14 4:58 PM
 */
public class UnlockService extends Service {
    private static final String tag = "WirlessUnlock/UnlockService";
    private static final String kgTag = "com.raidzero.wirelessunlock.UnlockService";

    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock kgLock;

    private AppHelper appHelper;
    private SharedPreferences sharedPreferences;



    // this is an unbound service
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);


        this.kgLock = keyguardManager.newKeyguardLock(kgTag);
        this.appHelper = Common.getAppHelper();

        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(appHelper);


        Log.d(tag, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        kgLock.disableKeyguard();
        Log.d(tag, "Service started");

        appHelper.showNotification();
        return START_STICKY; // Run until explicitly stopped.
    }

    @Override
    public void onDestroy() {
        if (kgLock != null) {
            kgLock.reenableKeyguard();
        }

        appHelper.dismissNotification();

        Log.d(tag, "Service destroyed");
    }



}

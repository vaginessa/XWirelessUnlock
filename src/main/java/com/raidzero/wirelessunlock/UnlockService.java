package com.raidzero.wirelessunlock;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by raidzero on 5/10/14 4:58 PM
 */
public class UnlockService extends Service {
    private static final String tag = "WirlessUnlock/UnlockService";
    private static final String kgTag = "com.raidzero.wirelessunlock.UnlockService";

    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock kgLock;

    // this is an unbound service
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        this.kgLock = keyguardManager.newKeyguardLock(kgTag);
        Log.d(tag, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        kgLock.disableKeyguard();
        Log.d(tag, "Service started");
        return START_STICKY; // Run until explicitly stopped.
    }

    @Override
    public void onDestroy() {
        if (kgLock != null) {
            kgLock.reenableKeyguard();
        }
        Log.d(tag, "Service destroyed");
    }


}

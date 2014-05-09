package com.raidzero.wirelessunlock.global;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import com.raidzero.wirelessunlock.LockService;
import com.raidzero.wirelessunlock.activities.MainActivity;

import java.util.ArrayList;

/**
 * Created by raidzero on 5/8/14 2:18 PM
 */
public class AppHelper extends Application {
    private static final String tag = "WirelessUnlock/AppHelper";

    private ArrayList<String> connectedBluetoothDevices = new ArrayList<String>();

    private LockService lockService;
    private boolean isBound = false;

    private ServiceConnection myConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            LockService.MyLocalBinder binder = (LockService.MyLocalBinder) service;
            lockService = binder.getService();
            Log.d(tag, "service bound");
            isBound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(tag ,"service unbound");
            isBound = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Common.appHelper = this;
        Log.d(tag, "onCreate()");
    }


    public boolean isServiceBound() {
        return isBound;
    }

    public void addConnectedDevice(String address) {
        lockService.addConnectedDevice(address);
    }

    public void removeConnectedDevice(String address) {
        lockService.removeConnectedDevice(address);
    }

    public void processChanges() {
        Log.d(tag, "processChanges()");

        if (isBound) {
            lockService.processChanges();
        } else {
            Log.d(tag, "not bound");
        }
    }

    public ServiceConnection getServiceConnection() {
        return myConnection;
    }
}

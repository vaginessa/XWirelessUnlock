package com.raidzero.wirelessunlock.xposed;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

/**
 * Created by raidzero on 5/18/14 9:12 AM
 */
public class XposedMod implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private static final String tag = "XWirelessUnlock/XposedMod";
    private XSharedPreferences prefs;
    private void XLog(String msg) {
        XposedBridge.log(tag + ": " + msg);
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        loadPrefs();
    }

    @Override
    public void handleLoadPackage(LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.contains("com.android.keyguard")) {

            findAndHookMethod("com.android.keyguard.KeyguardViewMediator", loadPackageParam.classLoader,
                    "showLocked", "android.os.Bundle", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XLog("before lock screen");
                            if (isEnabled("disableLock")) {
                                XLog("disabling lock screen");
                                param.setResult(null);
                            }
                        }
                    });
        }
    }

    private boolean isEnabled(String key) {
        prefs.reload();
        boolean rtn = prefs.getBoolean(key, false);
        XLog("isPrefEnabled(" + key + ")? " + rtn);

        return rtn;
    }

    private void loadPrefs() {
        prefs = new XSharedPreferences("com.raidzero.xposed.wirelessunlock");
        prefs.makeWorldReadable();
        XLog("prefs loaded.");
    }
}

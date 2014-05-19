package com.raidzero.wirelessunlock.xposed;

import java.lang.reflect.Method;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodBestMatch;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;

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
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {

        if (loadPackageParam.packageName.contains("com.android.keyguard")) {

            findAndHookMethod("com.android.keyguard.KeyguardViewMediator", loadPackageParam.classLoader,
                    "showLocked", "android.os.Bundle", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XLog("before lock screen");
                            if (isEnabled("disableLock")) {
                                XLog("preventing call");
                                param.setResult(null);
                            }
                        }
                    });

            findAndHookMethod("com.android.keyguard.KeyguardViewMediator", loadPackageParam.classLoader,
                    "onScreenTurnedOff", int.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XLog("before onScreenTurnedOff");
                        }
                    });

            final Class<?> c = findClass("com.android.keyguard.KeyguardViewMediator", loadPackageParam.classLoader);
            final Method m = findMethodExact(c, "hideLocked");
            final Class<?> callback = findClass("com.android.internal.policy.IKeyguardShowCallback", loadPackageParam.classLoader);

                findAndHookMethod("com.android.keyguard.KeyguardViewMediator", loadPackageParam.classLoader,
                "onScreenTurnedOn", callback, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XLog("after onScreenTurnedOn");
                        if (isEnabled("disableLock")) {
                            // TODO: hideLocked();
                        }
                    }
                });

            findAndHookMethod("com.android.keyguard.KeyguardViewMediator", loadPackageParam.classLoader,
                    "doKeyguardLocked", "android.os.Bundle", new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XLog("before doKeyguardLocked");
                            if (isEnabled("disableLock")) {
                                XLog("Preventing call");
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
        prefs = new XSharedPreferences("com.raidzero.wirelessunlock");
        prefs.makeWorldReadable();
        XLog("prefs loaded.");
    }
}

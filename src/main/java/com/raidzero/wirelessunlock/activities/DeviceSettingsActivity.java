package com.raidzero.wirelessunlock.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Button;
import com.raidzero.wirelessunlock.R;
import com.raidzero.wirelessunlock.global.AppDevice;

/**
 * Created by raidzero on 5/10/14 12:41 AM
 */
public class DeviceSettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        addPreferencesFromResource(R.xml.device_prefs);

        AppDevice d = i.getExtras().getParcelable("device");

        String deviceName = d.getName();

        this.setTitle(deviceName);
    }
}

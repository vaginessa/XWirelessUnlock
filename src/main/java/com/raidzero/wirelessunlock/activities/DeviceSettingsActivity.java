package com.raidzero.wirelessunlock.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import com.raidzero.wirelessunlock.R;
import com.raidzero.wirelessunlock.global.AppDevice;

/**
 * Created by raidzero on 5/10/14 12:41 AM
 */
public class DeviceSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 0.80);

        setContentView(R.layout.device_settings);

        getWindow().setLayout(screenWidth, ViewGroup.LayoutParams.WRAP_CONTENT);

        Intent i = getIntent();

        final AppDevice d = i.getExtras().getParcelable("device");

        String deviceName = d.getName();
        this.setTitle(deviceName);
        boolean chargingOnly = d.getChargingOnly();

        final CheckBox chk_chargingOnly = (CheckBox) findViewById(R.id.chk_deviceSettings_chargingOnly);
        final Button button_remove = (Button) findViewById(R.id.button_deviceSettings_remove);

        chk_chargingOnly.setChecked(chargingOnly);


        chk_chargingOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppDevice newDevice = new AppDevice(d.getType(), d.getName(), d.getAddress(),
                        chk_chargingOnly.isChecked(), true);

                Intent rtn = new Intent();
                rtn.putExtra("replace", true);
                rtn.putExtra("device", newDevice);
                setResult(RESULT_OK, rtn);
                finish();
            }

        });
    }
}

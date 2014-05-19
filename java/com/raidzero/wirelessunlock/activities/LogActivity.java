package com.raidzero.wirelessunlock.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.raidzero.wirelessunlock.R;
import com.raidzero.wirelessunlock.global.Common;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by raidzero on 5/10/14 11:46 PM
 */
public class LogActivity extends ActionBarActivity {
    private static final String tag = "WirelessUnlock/LogActivity";
    private TextView logView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.service_log);
        logView = (TextView) findViewById(R.id.txt_log);

        populateLog();

    }

    private void populateLog() {
        for (String s : getLines()) {
            logView.append(s);
        }
    }

    private ArrayList<String> getLines() {
        ArrayList<String> rtn = new ArrayList<String>();

        try {
            FileInputStream stream = openFileInput(Common.logFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                rtn.add(line + "\n");
            }
        } catch (Exception e) {
            // nothing
        }

        return rtn;
    }

    private void clearLog() {
        deleteFile(Common.logFile);
        logView.setText("");
    }

    private void refreshLog() {
        logView.setText("");
        populateLog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.log_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_log_clear:
                clearLog();
                return true;
            case R.id.action_log_refresh:
                refreshLog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

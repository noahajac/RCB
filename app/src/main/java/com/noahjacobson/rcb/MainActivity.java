package com.noahjacobson.rcb;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button disableRootButton = (Button) findViewById(R.id.disable_root);
        Button enableRootButton = (Button) findViewById(R.id.enable_root);
        disableRootButton.setOnClickListener(disableRoot);
        enableRootButton.setOnClickListener(enableRoot);
    }

    public boolean rootAccessCheck() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean("rootAccess", true)) {
            rootStatusCheck();
            return true;
        }else{
            return false;
        }
    }

    public void rootStatusCheck() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        String suBinaryName = sharedPreferences.getString("su_binary_name", "su");
        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled");
        File suEnabled = new File("/system/bin/" + suBinaryName);
        File suDisabled = new File("/system/bin/" + suDisabledBinaryName);
        if (suEnabled.exists()) {
            sharedPreferencesEditor.putBoolean("rootEnabled", true);
            sharedPreferencesEditor.commit();
        }else if(suDisabled.exists()) {
            sharedPreferencesEditor.putBoolean("rootEnabled", false);
            sharedPreferencesEditor.commit();
        }
    }

    public boolean rootCheck() {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        String suBinaryName = sharedPreferences.getString("su_binary_name", "su");
        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled");
        File suEnabled = new File("/system/bin/" + suBinaryName);
        boolean suEnabledFound;
        if (suEnabled.exists()) {
            suEnabledFound = true;
        }else{
            suEnabledFound = false;
        }

        File suDisabled = new File("/system/bin/" + suDisabledBinaryName);
        boolean suDisabledFound;
        if (suDisabled.exists()) {
            suDisabledFound = true;
        }else{
            suDisabledFound = false;
        }

        if (suEnabledFound || suDisabledFound) {
            if(rootAccessCheck()) {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private View.OnClickListener disableRoot = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (rootCheck()) {
                SharedPreferences sharedPreferences = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                if (sharedPreferences.getBoolean("rootEnabled", true)) {
                    try {
                        String suBinaryName = sharedPreferences.getString("su_binary_name", "su");
                        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled");
                        Process rootProcess = Runtime.getRuntime().exec("/system/xbin/" + suBinaryName);
                        DataOutputStream rootStream = new DataOutputStream(rootProcess.getOutputStream());
                        rootStream.writeBytes("mount -o rw,remount,rw /system\n");
                        rootStream.writeBytes("mv /system/bin/" + suBinaryName + " /system/bin/" + suDisabledBinaryName + "\n");
                        rootStream.writeBytes("mv /system/xbin/" + suBinaryName + " /system/xbin/" + suDisabledBinaryName + "\n");
                        rootStream.writeBytes("mount -o ro,remount,ro /system\n");
                        rootStream.writeBytes("exit\n");
                        rootStream.flush();

                    } catch (IOException e) {

                    }
                }
            }else{
                Toast rootAlreadyDisabled = Toast.makeText(getApplicationContext(), "Root Already Disabled", Toast.LENGTH_SHORT);
                rootAlreadyDisabled.show();
            }
        }
    };
    private View.OnClickListener enableRoot = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (rootCheck()) {
                SharedPreferences sharedPreferences = MainActivity.this.getPreferences(Context.MODE_PRIVATE);
                if (sharedPreferences.getBoolean("rootEnabled", false)) {
                    try {
                        String suBinaryName = sharedPreferences.getString("su_binary_name", "su");
                        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled");
                        Process rootProcess = Runtime.getRuntime().exec("/system/xbin/" + suDisabledBinaryName);
                        DataOutputStream rootStream = new DataOutputStream(rootProcess.getOutputStream());
                        rootStream.writeBytes("mount -o rw,remount,rw /system\n");
                        rootStream.writeBytes("mv /system/xbin/" + suDisabledBinaryName + " /system/xbin/" + suBinaryName + "\n");
                        rootStream.writeBytes("mv /system/bin/" + suDisabledBinaryName + " /system/bin/" + suBinaryName + "\n");
                        rootStream.writeBytes("mount -o ro,remount,ro /system\n");
                        rootStream.writeBytes("exit\n");
                        rootStream.flush();

                    } catch (IOException e) {

                    }
                }else{
                    Toast rootAlreadyEnabled = Toast.makeText(getApplicationContext(), "Root Already Enabled", Toast.LENGTH_SHORT);
                    rootAlreadyEnabled.show();
                }
            }
        }
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            settingsIntent.putExtra( SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName());
            settingsIntent.putExtra( SettingsActivity.EXTRA_NO_HEADERS, true );
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

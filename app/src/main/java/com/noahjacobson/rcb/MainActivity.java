package com.noahjacobson.rcb;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

public class MainActivity extends AppCompatActivity { // Class for the main activity.

    @Override // Override previous functions.
    protected void onCreate(Bundle savedInstanceState) { // Function to run on start of activity.
        super.onCreate(savedInstanceState); // Run Android API code.
        setContentView(R.layout.activity_main); // Set view to the main layout via its id.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); // Find toolbar by id.
        setSupportActionBar(toolbar); // Set toolbar as the action bar.
        Button disableRootButton = (Button) findViewById(R.id.disable_root_button); // Find button to disable root by id.
        Button enableRootButton = (Button) findViewById(R.id.enable_root_button); // Find button to enable root by id.
        Button toggleRootButton = (Button) findViewById(R.id.toggle_root_button); // Find button to toggle root by id.
        disableRootButton.setOnClickListener(disableRootListener); // Set on click listener for disable root button.
        enableRootButton.setOnClickListener(enableRootListener); // Set on click listener for enable root button.
        toggleRootButton.setOnClickListener(toggleRootListener); // Set on click listener for toggle root button.
    }

    private void rootStatusCheck() { // Function to check if root is currently enabled or disabled.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declares variable for shared preferences.
        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit(); // Declare variable for shared preferences editor.
        String suBinaryName = sharedPreferences.getString("su_binary_name", "su"); // Declare variable for the name of the su binary when enabled.
        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled"); // Declare variable for the name of the su binary when disabled.
        File suBinaryEnabled = new File("/system/xbin/" + suBinaryName); // Declare variable for the enabled su binary and its path.
        File suBinaryDisabled = new File("/system/xbin/" + suDisabledBinaryName); // Declare variable for the disabled su binary and its path.
        if (suBinaryEnabled.exists()) { // Does the enabled su binary exist?
            sharedPreferencesEditor.putBoolean("root_enabled", true); // Set preference that states root is currently enabled.
            sharedPreferencesEditor.apply(); // Save changes to preferences.
        }else if(suBinaryDisabled.exists()) { // If the enabled su binary does not exist, does the disabled su binary exist?
            sharedPreferencesEditor.putBoolean("root_enabled", false); // Set preference that states root is currently disabled.
            sharedPreferencesEditor.apply(); // Save changes to preferences.
        }
    }

    private boolean rootAccessCheck() { // Function to check if RCB has root access.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
        if(sharedPreferences.getBoolean("root_access", false)) { // Does the saved preference state that RCB has root access?
            rootStatusCheck(); // Call the function rootStatusCheck to check if root is currently enabled or disabled.
            return true; // Tell previous function rootCheck that RCB has root access.
        }else{ // The saved preference states that RCB does not have root access.
            return false; // Tell previous function rootCheck that RCB does not have root access.
        }
    }

    private boolean rootCheck() { // Function to check if root binaries are present and call other root checking functions.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
        String suBinaryName = sharedPreferences.getString("su_binary_name", "su"); // Declare variable for the name of the su binary when enabled.
        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled"); // Declare variable for the name of the su binary when disabled.

        File suBinaryEnabled = new File("/system/xbin/" + suBinaryName); // Declare variable for the enabled su binary and its path.
        boolean suBinaryEnabledFound; // Declare variable for if the enabled su binary is found or not.
        suBinaryEnabledFound = suBinaryEnabled.exists(); // Set variable declaring weather the enabled su binary exists or not.

        File suBinaryDisabled = new File("/system/xbin/" + suDisabledBinaryName); // Declares variable for the disabled su binary and its path.
        boolean suBinaryDisabledFound; // Declare variable for if the disabled su binary is found or not.
        suBinaryDisabledFound = suBinaryDisabled.exists(); // Set variable declaring weather the enabled su binary exists or not.

        return (suBinaryEnabledFound || suBinaryDisabledFound) && rootAccessCheck(); // Tell the previous function weather it's safe to check if RCB has root access.
    }

    private void disableRoot() { // Function to disable root.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
        String suBinaryName = sharedPreferences.getString("su_binary_name", "su"); // Declare variable for the name of the su binary when enabled.
        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled"); // Declare variable for the name of the su binary when disabled.
        try { // Try to disable root.
            Process rootProcess = Runtime.getRuntime().exec("/system/xbin/" + suBinaryName); // Create a shell process with root privileges.
            DataOutputStream rootStream = new DataOutputStream(rootProcess.getOutputStream()); // Create stream to write commands to the shell process.
            rootStream.writeBytes("mount -o rw,remount,rw /system\n"); // Send command to re-mount the system partition as read-write.
            rootStream.writeBytes("mv /system/bin/" + suBinaryName + " /system/bin/" + suDisabledBinaryName + "\n"); // Send command to rename the enabled su binary in bin to the disabled su binary.
            rootStream.writeBytes("mv /system/xbin/" + suBinaryName + " /system/xbin/" + suDisabledBinaryName + "\n"); // Send command to rename the enabled su binary in xbin to the disabled su binary.
            rootStream.writeBytes("mount -o ro,remount,ro /system\n"); // Send command to re-mount the system partition as read-only.
            rootStream.writeBytes("exit\n"); // Send command to exit the root shell.
            rootStream.flush(); // Get rid of the root shell.
        } catch (IOException ignored) {} // Watch for IOException errors.
    }

    private void enableRoot() { // Function to enable root.
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
        String suBinaryName = sharedPreferences.getString("su_binary_name", "su"); // Declare variable for the name of the su binary when enabled.
        String suDisabledBinaryName = sharedPreferences.getString("su_disabled_binary_name", "su.disabled"); // Declare variable for the name of the su binary when disabled.
        try { // Try to enable root.
            Process rootProcess = Runtime.getRuntime().exec("/system/xbin/" + suDisabledBinaryName); // Create a shell process with root privileges.
            DataOutputStream rootStream = new DataOutputStream(rootProcess.getOutputStream()); // Create stream to write commands to the shell process.
            rootStream.writeBytes("mount -o rw,remount,rw /system\n"); // Send command to re-mount the system partition as read-write.
            rootStream.writeBytes("mv /system/xbin/" + suDisabledBinaryName + " /system/xbin/" + suBinaryName + "\n"); // Send command to rename the disabled su binary in xbin to the enabled su binary.
            rootStream.writeBytes("mv /system/bin/" + suDisabledBinaryName + " /system/bin/" + suBinaryName + "\n"); // Send command to rename the disabled su binary in bin to the enabled su binary.
            rootStream.writeBytes("mount -o ro,remount,ro /system\n"); // Send command to re-mount the system partition as read-only.
            rootStream.writeBytes("exit\n"); // Send command to exit the root shell.
            rootStream.flush(); // Get rid of the root shell.
        } catch (IOException ignored) {} // Watch for IOException errors.
    }

    private final View.OnClickListener disableRootListener = new View.OnClickListener() { // On click listener for button to disable root.
        @Override // Override previous functions.
        public void onClick(View v) { // Function states what to do when the disable root button is clicked.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
            if (rootCheck()) { // Calls root checking functions. Is it safe to continue without errors?
                if (sharedPreferences.getBoolean("root_enabled", true)) { // Is root enabled?
                    disableRoot(); // Calls function to disable root.
                }else{ // Root is not enabled.
                    Toast rootAlreadyDisabledToast = Toast.makeText(getApplicationContext(), R.string.root_already_disabled_toast, Toast.LENGTH_SHORT); // Create toast to say root is already disabled.
                    rootAlreadyDisabledToast.show(); // Show toast that root is already disabled.
                }
            }
        }
    };

    private final View.OnClickListener enableRootListener = new View.OnClickListener() { // On click listener for button to enable root.
        @Override // Override previous functions.
        public void onClick(View v) { // Function states what to do when the enable root button is clicked.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
            if (rootCheck()) { // Call root checking functions. Is it safe to continue without errors?
                if (!sharedPreferences.getBoolean("root_enabled", true)) { // Is root disabled?
                    enableRoot(); // Calls function to enable root.
                }else{ // Root is not disabled.
                    Toast rootAlreadyEnabledToast = Toast.makeText(getApplicationContext(), R.string.root_already_enabled_toast, Toast.LENGTH_SHORT); // Create toast to say root is already enabled.
                    rootAlreadyEnabledToast.show(); // Show toast that root is already enabled.
                }
            }
        }
    };

    private final View.OnClickListener toggleRootListener = new View.OnClickListener() { // Function states what to do when the toggle root button is clicked.
      @Override // Override previous functions.
      public void onClick(View v) { // Function states what to do when toggle root button is checked.
          SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()); // Declare variable for shared preferences.
          if (rootCheck()) { // Call root checking functions. Is it safe to continue without errors?
              if (sharedPreferences.getBoolean("root_enabled", true)) { // Is root enabled?
                  disableRoot(); // Calls function to disable root.
              }else if (!sharedPreferences.getBoolean("root_enabled", true)) { // If root is not enabled, is root disabled?
                  enableRoot(); // Calls function to enable root.
              }
          }
      }
    };

    @Override // Override previous functions.
    public boolean onCreateOptionsMenu(Menu menu) { // Function to create the options menu then add items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu); // Show the options menu.
        return true; // Return true to previous function.
    }

    @Override // Override previous functions.
    public boolean onOptionsItemSelected(MenuItem item) { // Function to handle clicks of items in the action bar.
        int id = item.getItemId(); // Get id of item.
        if (id == R.id.action_settings) { // Is the id the id of settings option?
            Intent settingsIntent = new Intent(this, SettingsActivity.class); // Create a new intent for the settings activity.
            settingsIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName()); // Add extra items to intent.
            settingsIntent.putExtra(SettingsActivity.EXTRA_NO_HEADERS, true ); // Add extra items to intent.
            startActivity(settingsIntent); // Start the settings activity.
            return true; // Return true to the previous function.
        }
        return super.onOptionsItemSelected(item); // Return to previous function.
    }
}
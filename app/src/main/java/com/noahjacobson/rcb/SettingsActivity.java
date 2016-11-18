package com.noahjacobson.rcb;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class SettingsActivity extends AppCompatPreferenceActivity { // Class for the settings activity.

    @Override // Override previous functions.
    public boolean onOptionsItemSelected(MenuItem item) { // Function to listen to action bar item click.
        switch (item.getItemId()) { // Find the item selected.
            case android.R.id.home: // Provides proper up navigation.
                finish(); // Provides proper up navigation.
                return true; // Returns true to previous function.
        }
        return false; // Returns false to previous function.
    }

    private static boolean isXLargeTablet(Context context) { // Function to detect if device's screen is extra large.
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE; // Returns to previous function.
    }

    @Override // Override previous functions.
    protected void onCreate(Bundle savedInstanceState) { // Function to run on start of the activity.
        super.onCreate(savedInstanceState); // Run Android API code.
        setupActionBar(); // Call the function to setup the action bar.
    }

    private void setupActionBar() { // Function to setup the action bar.
        ActionBar actionBar = getSupportActionBar(); // Declare variable for the action bar.
        actionBar.setTitle(R.string.settings_name); // Set the activity title on the action bar.
        actionBar.setDisplayHomeAsUpEnabled(true); // Show the up button in the action bar.
    }

    @Override // Override previous functions.
    public boolean onIsMultiPane() { // Function to determine if activity should run in multi-pane mode.
        return isXLargeTablet(this); // Return to the previous function/
    }

    @Override // Override previous functions.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB) // Set the target API to Honeycomb.
    public void onBuildHeaders(List<Header> target) { // Function to get preference headers.
        loadHeadersFromResource(R.xml.pref_headers, target); // Get preference headers.
    }

    protected boolean isValidFragment(String fragmentName) { // Function to stop malicious injection.
        return PreferenceFragment.class.getName().equals(fragmentName) || GeneralPreferenceFragment.class.getName().equals(fragmentName); // Return to previous function.
    }

   public static class ResetSettingsDialogFragment extends DialogFragment { // Function for the confirmation dialog box to reset settings.
        @Override  // Override previous functions.
        public Dialog onCreateDialog(Bundle savedInstanceState) { // Function to run on the creation of the dialog box.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); // Create new dialog box builder.
            builder.setMessage(R.string.reset_settings_dialog_message); // Set the message for the dialog box.
            builder.setTitle(R.string.reset_settings_dialog_title); // Set the title for the dialog box.
            builder.setPositiveButton(R.string.reset_settings_dialog_positive_button, new DialogInterface.OnClickListener() { // Set the positive button for the dialog box.
                        public void onClick(DialogInterface dialog, int id) { // Function to run when the positive button is clicked.
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity()); // Declare variable for shared preferences.
                            SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit(); // Declare variable for shared preferences editor.
                            sharedPreferencesEditor.clear(); // Clear the saved preferences to default.
                            sharedPreferencesEditor.apply(); // Save changes to preferences.
                            getActivity().finish(); // Close settings activity.
                            Toast resetSettingsToast = Toast.makeText(getActivity().getApplication(), R.string.reset_settings_toast, Toast.LENGTH_SHORT); // Create toast to say the settings were reset to default.
                            resetSettingsToast.show(); // Show toast to say the settings were reset to default.
                        }
                    });
            builder.setNegativeButton(R.string.reset_settings_dialog_negative_button, new DialogInterface.OnClickListener() { // Set the negative button for the dialog box
                        public void onClick(DialogInterface dialog, int id) { // Function to run when the negative button is clicked.
                            // Do nothing.
                        }
                    });
            return builder.create(); // Return to the previous function.
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener { // Class for the preferences fragment.
        @Override // Override previous functions.
        public void onCreate(Bundle savedInstanceState) { // Function to run on creation of the fragment.
            super.onCreate(savedInstanceState); // Run Android API code.
            addPreferencesFromResource(R.xml.pref_general); // Add the preferences to the fragment.
            PreferenceManager.setDefaultValues(getContext(), R.xml.pref_general, false); // Set the default values for the preferences.
            final Preference requestRoot = findPreference("request_root"); // Declare variable for the request root access button.
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity()); // Declare variable for shared preferences.
            if(sharedPreferences.getBoolean("root_access", false)) { // Does RCB have root access?
                requestRoot.setSummary(R.string.request_root_preference_true_summary); // Set the summary to display that RCB has root access.
            }else if(!sharedPreferences.getBoolean("root_access", false)) { // Does RCB not have root access?
                requestRoot.setSummary(R.string.request_root_preference_false_summary); // Set the summary to display that RCB does not have root access.
            }
            initSummary(getPreferenceScreen()); // Call the function to initiate the preference's summaries.

            requestRoot.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // Set the on click listener for the request root button.
                @Override // Override previous functions.
                public boolean onPreferenceClick(Preference preference) { // Function to run when the request root button is clicked.
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext()); // Declare variable for shared preferences.
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit(); // Declare variable for shared preferences editor.
                    String suBinaryName = sharedPreferences.getString("su_binary_name", "su"); // Declare variable for the name of the su binary when enabled.
                    try { // Try to request root access.
                        Process rootProcess = Runtime.getRuntime().exec("/system/xbin/" + suBinaryName); // Create a shell process with root privileges.
                        DataOutputStream rootStream = new DataOutputStream(rootProcess.getOutputStream()); // Create stream to write commands to the shell process.
                        rootStream.writeBytes("exit\n"); // Send command to exit the root shell.
                        rootStream.flush(); // Get rid of the root shell.
                        try {
                            rootProcess.waitFor(); // Wait for the shell process.
                            if (rootProcess.exitValue() == 0) { // Was the root request accepted?
                                sharedPreferencesEditor.putBoolean("root_access", true); // Set preference to state that RCB has root access.
                                sharedPreferencesEditor.apply(); //  // Save changes to preferences.
                                requestRoot.setSummary(R.string.request_root_preference_true_summary); // Set the summary for the request root button to state that RCB has root access.
                                Toast rootCheckSuccessfulToast = Toast.makeText(getActivity().getApplication(), R.string.request_root_toast_successful, Toast.LENGTH_SHORT); // Create toast to show that the root request was accepted.
                                rootCheckSuccessfulToast.show(); // Show toast to show that the root request was accepted.
                            }
                            else { // The root request was denied.
                                sharedPreferencesEditor.putBoolean("root_access", false); // Set preference to state that RCB does not have root access.
                                sharedPreferencesEditor.commit(); // Save changes to preferences.
                                requestRoot.setSummary(R.string.request_root_preference_false_summary); // Set the summary for the root request button to state that RCB does not have root access.
                                Toast rootCheckUnsuccessfulToast = Toast.makeText(getActivity().getApplication(), R.string.request_root_toast_unsuccessful_denied, Toast.LENGTH_SHORT); // Create toast to show that the root request was denied.
                                rootCheckUnsuccessfulToast.show(); // Show toast to show that the root request was denied.
                            }
                        } catch (InterruptedException e) { // Watch for InterruptedException errors.
                            sharedPreferencesEditor.putBoolean("root_access", false); // Set preference to state that RCB does not have root access.
                            sharedPreferencesEditor.commit(); // Save changes to preferences.
                            requestRoot.setSummary(R.string.request_root_preference_false_summary); // Set the summary for the root request button to state that RCB does not have root access.
                            Toast rootCheckUnsuccessfulToast = Toast.makeText(getActivity().getApplication(), R.string.request_root_toast_unsuccessful_error + ": " + e, Toast.LENGTH_SHORT); // Create toast to show that the root request had an error.
                            rootCheckUnsuccessfulToast.show(); // Create toast to show that the root request had an error.
                        }
                    } catch (IOException e) { // Watch for IOException errors showing that the su binary could not be found.
                        sharedPreferencesEditor.putBoolean("root_access", false); // Set preference to state that RCB does not have root access.
                        sharedPreferencesEditor.commit(); // Save changes to preferences.
                        requestRoot.setSummary(R.string.request_root_preference_false_summary); // Set the summary for the root request button to state that RCB does not have root access.
                        Toast rootCheckUnsuccessfulToast = Toast.makeText(getActivity().getApplication(), R.string.request_root_toast_unsuccessful_cannot_find, Toast.LENGTH_SHORT); // Create toast to show that the root request was unable to find the su binary.
                        rootCheckUnsuccessfulToast.show(); // Create toast to show that the root request was unable to find the su binary.
                    }
                    return true; // Return true to previous function.
                }
            });

            Preference resetSettings = findPreference("reset_settings"); // Declare variable for the reset settings button.
            resetSettings.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() { // Set the on click listener for the reset settings button.
                @Override // Override previous functions.
                public boolean onPreferenceClick(Preference preference) { // Function to run when the reset settings button is clicked.
                    FragmentManager fragmentManager = getActivity().getFragmentManager(); // Declare variable for the fragment manager.
                    DialogFragment resetPopup = new ResetSettingsDialogFragment(); // Create new dialog box to confirm that the user wants to reset settings.
                    resetPopup.show(fragmentManager, "settingsResetDialog"); // Show dialog box to confirm that the user wants to reset settings.
                    return true; // Return true to previous function.
                }
            });
        }

        @Override // Override previous functions.
        public void onResume() { // Function to run when the fragment is resumed.
            super.onResume(); // Run Android API code.
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this); // Create a listener for when a preference changes.
        }

        @Override // Override previous functions.
        public void onPause() { // Function to run when the fragment is paused.
            super.onPause(); // Run Android API code.
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this); // Get rid of the listener for when a preference changes.
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { // Function to run when a preference is changed.
            updatePrefSummary(findPreference(key)); // Call the function to update the summary of the preference.
        }

        private void initSummary(Preference p) { // Function to run when the fragment is first created to initiate the summaries.
            if (p instanceof PreferenceGroup) { // Is the preference part of this fragment?
                PreferenceGroup pGrp = (PreferenceGroup) p; // Declare variable for the preference.
                for (int i = 0; i < pGrp.getPreferenceCount(); i++) { // Loop until there are no more preference summaries left to initiate.
                    initSummary(pGrp.getPreference(i)); // Initiate the remaining preference summaries.
                }
            } else { // Preference isn't part of the fragment.
                updatePrefSummary(p); // Call the function to update the summary of a preference to its current value.
            }
        }

        @SuppressWarnings("ConstantConditions")
        private void updatePrefSummary(Preference p) { // Function to update the summary of a preference to its current value.
            if (p instanceof ListPreference) { // Is the preference a list preference?
                ListPreference listPref = (ListPreference) p; // Declare variable for the preference.
                p.setSummary(listPref.getEntry()); // Set the summary of the preference to its current value.
            }
            if (p instanceof EditTextPreference) { // Is the preference a text preference?
                EditTextPreference editTextPref = (EditTextPreference) p; // Declare variable for the preference.
                p.setSummary(editTextPref.getText()); // Set the summary of the preference to its current value.
            }
            if (p instanceof MultiSelectListPreference) { // Is the preference a multiple selection preference?
                EditTextPreference editTextPref = (EditTextPreference) p; // Declare variable for the preference.
                p.setSummary(editTextPref.getText()); // Set the summary of the preference to its current value.
            }
        }
    }
}

package ru.krotarnya.diasync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import java.util.Objects;

public class DiasyncSettings extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = "DiasyncSettings";
    private static final String TITLE_TAG = "DiasyncSettingsTitle";

    public static final String ROOT_FRAGMENT = "RootFragment";
    public static final String DISPLAY_FRAGMENT = "DisplayFragment";
    public static final String ALERTS_FRAGMENT = "AlertsFragment";
    public static final String CONNECTIVITY_FRAGMENT = "ConnectivityFragment";

    private static DiasyncSettings instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new RootFragment())
                .commit();
        }
        else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            ActionBar action_bar = getSupportActionBar();
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(R.string.settings_title);
                if (action_bar != null) action_bar.setDisplayHomeAsUpEnabled(false);
            }
            else {
                if (action_bar != null) action_bar.setDisplayHomeAsUpEnabled(true);
            }
        });

        ActionBar action_bar = getSupportActionBar();
        if ((action_bar != null) && (getSupportFragmentManager().getBackStackEntryCount() > 0)) {
            action_bar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra("fragment")) {
            String fragment = intent.getStringExtra("fragment");

            PreferenceFragmentCompat preference_fragment = null;

            switch(fragment) {
                case ROOT_FRAGMENT:
                    preference_fragment = new RootFragment();
                    break;
                case ALERTS_FRAGMENT:
                    preference_fragment = new AlertsFragment();
                    break;
                case CONNECTIVITY_FRAGMENT:
                    preference_fragment = new ConnectivityFragment();
                    break;
                case DISPLAY_FRAGMENT:
                    preference_fragment = new DisplayFragment();
                    break;
                default:
                    preference_fragment = new RootFragment();
            }
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, preference_fragment)
                .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        Log.d(TAG, "onPreferenceStartFragment callback");
        final Bundle args = pref.getExtras();

        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), Objects.requireNonNull(pref.getFragment()));
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();

        setTitle(pref.getTitle());
        return true;
    }

    public static Context getContext(){
        return instance;
    }

    public static class RootFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            setPreferencesFromResource(R.xml.settings_root, key);
            Preference clear_data = findPreference("clear_data");
            if (clear_data != null)
            clear_data.setOnPreferenceClickListener(preference -> {
                Log.d(TAG, "Asked for clear data and force close");
                Diasync.clearDataForceClose();
                return true;
            });

            Context context = DiasyncSettings.getContext();
            if (context != null) {
                Log.d(TAG, "Checking for battery optimization");
                String packageName = context.getPackageName();
                try {
                    PowerManager pm = (PowerManager) context.getSystemService(POWER_SERVICE);
                    if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                        intent.setData(Uri.parse("package:" + packageName));
                        context.startActivity(intent);
                    }
                }
                catch (Exception e) {
                    Log.d(TAG, "Wasn't able to check for batter optimization");
                }
            }
        }
    }

    public static class DisplayFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            setPreferencesFromResource(R.xml.settings_display, key);
            Context context = DiasyncSettings.getContext();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor prefs_editor = prefs.edit();

            EditTextPreference glucose_low_pref = findPreference("glucose_low_pref");
            if (glucose_low_pref != null) {
                glucose_low_pref.setOnBindEditTextListener(
                        editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                );
                glucose_low_pref.setOnPreferenceChangeListener((preference, value) -> {
                    String glucose_units = prefs.getString("glucose_units", "");
                    double glucose_low;
                    switch (glucose_units) {
                        case "mmol":
                            glucose_low = Glucose.mmolToMgdl((String) value);
                            break;
                        case "mgdl":
                            glucose_low = Glucose.parse((String) value);
                            break;
                        default:
                            return false;
                    }

                    prefs_editor.putString("glucose_low", Glucose.stringMgdl(glucose_low));
                    prefs_editor.apply();
                    return true;
                });
            }

            EditTextPreference glucose_high_pref = findPreference("glucose_high_pref");
            if (glucose_high_pref != null) {
                glucose_high_pref.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
                );
                glucose_high_pref.setOnPreferenceChangeListener((preference, value) -> {
                    String glucose_units = prefs.getString("glucose_units", "");
                    double glucose_high;
                    switch (glucose_units) {
                        case "mmol":
                            glucose_high = Glucose.mmolToMgdl((String) value);
                            break;
                        case "mgdl":
                            glucose_high = Glucose.parse((String) value);
                            break;
                        default:
                            return false;
                    }

                    prefs_editor.putString("glucose_high", Glucose.stringMgdl(glucose_high));
                    prefs_editor.apply();
                    return true;
                });
            }

            ListPreference glucose_units = findPreference("glucose_units");
            if (glucose_units != null) {
                glucose_units.setOnPreferenceChangeListener((preference, value) -> {
                    final String old_value = glucose_units.getValue();
                    final String new_value = (String) value;
                    Log.d(TAG, "New glucose units = " + new_value);
                    if (Objects.equals(old_value, new_value)) return false;

                    switch (new_value) {
                        case "mmol":
                            if (glucose_high_pref != null)
                                glucose_high_pref.setText(Glucose.stringMmol(Glucose.mgdlToMmol(glucose_high_pref.getText())));
                            if (glucose_low_pref  != null)
                                glucose_low_pref. setText(Glucose.stringMmol(Glucose.mgdlToMmol(glucose_low_pref. getText())));
                            break;
                        case "mgdl":
                            if (glucose_high_pref != null)
                                glucose_high_pref.setText(Glucose.stringMgdl(Glucose.mmolToMgdl(glucose_high_pref.getText())));
                            if (glucose_low_pref  != null)
                                glucose_low_pref. setText(Glucose.stringMgdl(Glucose.mmolToMgdl(glucose_low_pref. getText())));
                            break;
                        default:
                            Log.wtf(TAG, "Unknown glucose unit type set.");
                            return false;
                    }
                    return true;
                });
            }
        }
    }

    public static class ConnectivityFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            setPreferencesFromResource(R.xml.settings_connectivity, key);
        }
    }

    public static class AlertsFragment extends PreferenceFragmentCompat {
        private SeekBarPreference alerts_snooze;
        private Preference alerts_resume;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            setPreferencesFromResource(R.xml.settings_alerts, key);
            alerts_snooze = findPreference("alerts_snooze");
            alerts_resume = findPreference("alerts_resume");
            update();

            String[] alerts_snooze_values = getResources().getStringArray(R.array.alerts_snooze_values);
            String[] alerts_snooze_entries = getResources().getStringArray(R.array.alerts_snooze_entries);
            if (alerts_snooze_values.length == alerts_snooze_entries.length) {
                int size = alerts_snooze_values.length;
                alerts_snooze.setMax(size - 1);
                alerts_snooze.setSummary(alerts_snooze_entries[alerts_snooze.getValue()]);

                alerts_snooze.setOnPreferenceClickListener(preference -> {
                    Log.d(TAG, "Snoozing alerts");
                    try {
                        Alerter.snooze(System.currentTimeMillis() + Long.parseLong(alerts_snooze_values[alerts_snooze.getValue()]));
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to snooze");
                    }
                    update();
                    return true;
                });


                alerts_snooze.setOnPreferenceChangeListener((preference, newValue) -> {
                    try {
                        alerts_snooze.setSummary(alerts_snooze_entries[(int) newValue]);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to set snooze period for alerts_snooze_values[" + newValue + "]");
                    }
                    return true;
                });
            }

            alerts_resume.setOnPreferenceClickListener(preference -> {
                Log.d(TAG, "Resuming alerts");
                Alerter.resume();
                update();
                return true;
            });
        }

        public void update() {
            if (Alerter.isSnoozedExternally()) {
                alerts_snooze.setVisible(false);
                alerts_resume.setVisible(true);
                alerts_resume.setSummary("Snoozed till " + Diasync.timeFormat(Alerter.snoozedTill()));
            }
            else {
                alerts_snooze.setVisible(true);
                alerts_resume.setVisible(false);
            }
        }
    }
}

package com.krotarnya.diasync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import java.util.Objects;

public class DiasyncSettings extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = "DiasyncSettings";
    private static final String TITLE_TAG = "DiasyncSettingsTitle";
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
        Libre2Widget.update(getContext());
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
                    Libre2Widget.update(context);
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
                    Libre2Widget.update(context);
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
                    Libre2Widget.update(context);
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
}

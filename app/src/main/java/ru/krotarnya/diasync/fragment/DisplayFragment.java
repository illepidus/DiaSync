package ru.krotarnya.diasync.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import ru.krotarnya.diasync.Glucose;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.SettingsActivity;

public class DisplayFragment extends PreferenceFragmentCompat {
    private static final String TAG = "DisplayFragment";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String key) {
        setPreferencesFromResource(R.xml.settings_display, key);
        Context context = SettingsActivity.getContext();
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
                        if (glucose_low_pref != null)
                            glucose_low_pref.setText(Glucose.stringMmol(Glucose.mgdlToMmol(glucose_low_pref.getText())));
                        break;
                    case "mgdl":
                        if (glucose_high_pref != null)
                            glucose_high_pref.setText(Glucose.stringMgdl(Glucose.mmolToMgdl(glucose_high_pref.getText())));
                        if (glucose_low_pref != null)
                            glucose_low_pref.setText(Glucose.stringMgdl(Glucose.mmolToMgdl(glucose_low_pref.getText())));
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

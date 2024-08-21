package ru.krotarnya.diasync.settings;

import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import java.util.Objects;

import ru.krotarnya.diasync.Glucose;
import ru.krotarnya.diasync.R;

public final class DisplayFragment extends PreferenceFragment {
    private static final String TAG = "DisplayFragment";

    @Override
    protected int screenResource() {
        return R.xml.settings_display;
    }

    @Override
    protected void afterCreatePreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
        SharedPreferences.Editor editor = prefs.edit();

        EditTextPreference glucose_low_pref = findPreference("glucose_low_pref");
        if (glucose_low_pref != null) {
            glucose_low_pref.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
            );
            glucose_low_pref.setOnPreferenceChangeListener((preference, value) -> {
                String glucose_unit = prefs.getString("glucose_unit", "");
                double glucose_low;
                switch (glucose_unit) {
                    case "mmol":
                        glucose_low = Glucose.mmolToMgdl((String) value);
                        break;
                    case "mgdl":
                        glucose_low = Glucose.parse((String) value);
                        break;
                    default:
                        return false;
                }

                editor.putString("glucose_low", Glucose.stringMgdl(glucose_low));
                editor.apply();
                return true;
            });
        }

        EditTextPreference glucose_high_pref = findPreference("glucose_high_pref");
        if (glucose_high_pref != null) {
            glucose_high_pref.setOnBindEditTextListener(
                    editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)
            );
            glucose_high_pref.setOnPreferenceChangeListener((preference, value) -> {
                String glucose_unit = prefs.getString("glucose_unit", "");
                double glucose_high;
                switch (glucose_unit) {
                    case "mmol":
                        glucose_high = Glucose.mmolToMgdl((String) value);
                        break;
                    case "mgdl":
                        glucose_high = Glucose.parse((String) value);
                        break;
                    default:
                        return false;
                }

                editor.putString("glucose_high", Glucose.stringMgdl(glucose_high));
                editor.apply();
                return true;
            });
        }

        ListPreference glucose_unit = findPreference("glucose_unit");
        if (glucose_unit != null) {
            glucose_unit.setOnPreferenceChangeListener((preference, value) -> {
                final String old_value = glucose_unit.getValue();
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

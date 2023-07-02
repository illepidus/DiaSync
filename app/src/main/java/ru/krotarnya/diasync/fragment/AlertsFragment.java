package ru.krotarnya.diasync.fragment;

import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;

import ru.krotarnya.diasync.Alerter;
import ru.krotarnya.diasync.Diasync;
import ru.krotarnya.diasync.R;

public class AlertsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "AlertsFragment";
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
        } else {
            alerts_snooze.setVisible(true);
            alerts_resume.setVisible(false);
        }
    }
}

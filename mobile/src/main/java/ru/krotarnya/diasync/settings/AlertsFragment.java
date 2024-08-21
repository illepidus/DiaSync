package ru.krotarnya.diasync.settings;

import android.os.Handler;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.SeekBarPreference;

import java.time.Duration;
import java.time.Instant;

import ru.krotarnya.diasync.Alerter;
import ru.krotarnya.diasync.Diasync;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.model.SnoozeInterval;

public final class AlertsFragment extends PreferenceFragment {
    private static final String TAG = "AlertsFragment";
    private static final Duration UPDATE_INTERVAL = Duration.ofMillis(300);
    private final Handler eventHandler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateFragment();
            eventHandler.postDelayed(updateRunnable, UPDATE_INTERVAL.toMillis());
        }
    };

    private SeekBarPreference snoozePref;
    private Preference resumePref;

    @Override
    protected int screenResource() {
        return R.xml.settings_alerts;
    }

    @Override
    protected void afterCreatePreferences() {
        snoozePref = findPreference("alerts_snooze");
        resumePref = findPreference("alerts_resume");

        if ((snoozePref == null) || (resumePref == null))
            return;

        snoozePref.setMax(SnoozeInterval.getTotalCount() - 1);
        snoozePref.setSummary(SnoozeInterval
                .getByOrderOrDefault(snoozePref.getValue())
                .getDisplayedText());

        snoozePref.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "Snoozing alerts");
            try {
                Alerter.externalSnooze(Instant.now().plus(SnoozeInterval
                        .getByOrderOrDefault(snoozePref.getValue())
                        .getDuration()));
            } catch (Exception e) {
                Log.e(TAG, "Failed to snooze");
                return false;
            }
            return true;
        });

        snoozePref.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                snoozePref.setSummary(SnoozeInterval
                        .getByOrderOrDefault((int) newValue)
                        .getDisplayedText());
            } catch (Exception e) {
                Log.wtf(TAG, "Failed to set snooze period", e);
                return false;
            }
            return true;
        });

        resumePref.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "Resuming alerts");
            Alerter.resume();
            updateFragment();
            return true;
        });

        updateFragment();
        eventHandler.postDelayed(updateRunnable, UPDATE_INTERVAL.toMillis());
    }

    public void updateFragment() {
        if (Alerter.isSnoozedExternally()) {
            snoozePref.setVisible(false);
            resumePref.setVisible(true);
            resumePref.setSummary("["
                    + Diasync.durationFormat(Duration.between(Instant.now(), Alerter.snoozedTill()))
                    + "] Snoozed till "
                    + Diasync.timeFormat(Alerter.snoozedTill()));
        } else {
            snoozePref.setVisible(true);
            resumePref.setVisible(false);
        }
    }
}

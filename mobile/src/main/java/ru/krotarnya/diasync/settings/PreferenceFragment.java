package ru.krotarnya.diasync.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Optional;

public abstract class PreferenceFragment extends PreferenceFragmentCompat {
    protected final String TAG = this.getClass().getSimpleName();
    protected final void setOnPreferenceClickListener(String key, Runnable action) {
        Optional<Preference> preference = Optional.ofNullable(findPreference(key));
        preference.ifPresent(p -> p.setOnPreferenceClickListener(any -> {
            try {
                action.run();
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }));
    }

    @Override
    public final void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String key) {
        setPreferencesFromResource(screenResource(), key);
        afterCreatePreferences();
    }

    abstract int screenResource();
    abstract void afterCreatePreferences();
}

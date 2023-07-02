package ru.krotarnya.diasync.fragment;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import ru.krotarnya.diasync.R;

public class ConnectivityFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String key) {
        setPreferencesFromResource(R.xml.settings_connectivity, key);
    }
}

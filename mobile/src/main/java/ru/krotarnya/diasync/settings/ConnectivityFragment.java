package ru.krotarnya.diasync.settings;

import android.os.Bundle;

import ru.krotarnya.diasync.R;

public class ConnectivityFragment extends PreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String key) {
        setPreferencesFromResource(R.xml.settings_connectivity, key);
    }
}

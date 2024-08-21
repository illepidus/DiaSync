package ru.krotarnya.diasync.settings;

import ru.krotarnya.diasync.R;

public class ConnectivityFragment extends PreferenceFragment {
    @Override
    int screenResource() {
        return R.xml.settings_connectivity;
    }

    @Override
    void afterCreatePreferences() {

    }
}

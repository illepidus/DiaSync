package ru.krotarnya.diasync.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import ru.krotarnya.diasync.Diasync;
import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.activity.PipActivity;
import ru.krotarnya.diasync.activity.SettingsActivity;

@SuppressLint("BatteryLife")
public class RootFragment extends PreferenceFragmentCompat {
    private static final String TAG = "RootFragment";
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String key) {
        setPreferencesFromResource(R.xml.settings_root, key);

        Context context = SettingsActivity.getContext();
        if (context != null) {
            Log.d(TAG, "Checking for battery optimization");
            String packageName = context.getPackageName();
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    context.startActivity(intent);
                }
            } catch (Exception e) {
                Log.d(TAG, "Wasn't able to check for batter optimization");
            }
        }

        Preference clear_data = findPreference("clear_data");
        if (clear_data != null) clear_data.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "Asked for clear data and force close");
            Diasync.clearDataForceClose();
            return true;
        });

        Preference pip_activity = findPreference("pip_activity");
        if (pip_activity != null) pip_activity.setOnPreferenceClickListener(preference -> {
            Log.d(TAG, "Asked to launch PiP activity");
            Intent intent = new Intent(context, PipActivity.class);
            startActivity(intent);
            return true;
        });
    }
}

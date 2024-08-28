package ru.krotarnya.diasync.settings;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import ru.krotarnya.diasync.R;

public final class RootFragment extends PreferenceFragment {
    @Override
    protected int screenResource() {
        return R.xml.settings_root;
    }

    @Override
    protected void afterCreatePreferences() {
        requestBatteryOptimizationsIfNeeded();
        setOnPreferenceClickListener("clear_data", this::clearDataForceClose);
    }

    @SuppressLint("BatteryLife")
    private void requestBatteryOptimizationsIfNeeded() {
        Log.d(TAG, "Checking for battery optimization");
        String packageName = requireContext().getPackageName();
        PowerManager pm = (PowerManager) requireContext().getSystemService(Context.POWER_SERVICE);
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Log.d(TAG, "Asking to disable battery optimization");
            requireContext().startActivity(
                    new Intent()
                            .setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                            .setData(Uri.parse("package:" + packageName)));
        }
    }

    private void clearDataForceClose() {
        ((ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE))
                .clearApplicationUserData();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        requireContext().startActivity(homeIntent);
    }
}

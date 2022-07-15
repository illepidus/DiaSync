package com.krotarnya.diasync;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Objects;

public class DiasyncSettings extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = "DiasyncSettings";
    private static final String TITLE_TAG = "DiasyncSettingsTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new RootFragment())
                    .commit();
        }
        else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            ActionBar action_bar = getSupportActionBar();
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(R.string.settings_title);
                if (action_bar != null) action_bar.setDisplayHomeAsUpEnabled(false);
            }
            else {
                if (action_bar != null) action_bar.setDisplayHomeAsUpEnabled(true);
            }
        });

        ActionBar action_bar = getSupportActionBar();
        if ((action_bar != null) && (getSupportFragmentManager().getBackStackEntryCount() > 0)) {
            action_bar.setDisplayHomeAsUpEnabled(true);
        }

        /*THIS BLOCK IS HERE AS DiasyncSettings IS MAIN ACTIVITY REMOVE IT AS IT IS NOT TRUE*/
        Intent widget_intent = new Intent(this, Libre2Widget.class);
        widget_intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplicationContext())
                .getAppWidgetIds(new ComponentName(getApplicationContext(), Libre2Widget.class));
        if (ids.length > 0) {
            widget_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(widget_intent);
        }

        /*THIS IS TEST BLOCK*/
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        Log.d(TAG, "onPreferenceStartFragment callback");
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), Objects.requireNonNull(pref.getFragment()));
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    public static class RootFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            Log.d(TAG, "key = " + key);
            setPreferencesFromResource(R.xml.settings_root, key);
        }
    }

    public static class DisplayFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            Log.d(TAG, "key = " + key);
            setPreferencesFromResource(R.xml.settings_display, key);
        }
    }

    public static class ConnectivityFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String key) {
            Log.d(TAG, "key = " + key);
            setPreferencesFromResource(R.xml.settings_connectivity, key);
        }
    }
}
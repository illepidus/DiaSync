package ru.krotarnya.diasync.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Objects;

import ru.krotarnya.diasync.R;
import ru.krotarnya.diasync.fragment.AlertsFragment;
import ru.krotarnya.diasync.fragment.ConnectivityFragment;
import ru.krotarnya.diasync.fragment.DisplayFragment;
import ru.krotarnya.diasync.fragment.RootFragment;

public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {
    private static final String TAG = "SettingsActivity";
    private static final String TITLE_TAG = "DiasyncSettingsTitle";
    public static final String DISPLAY_FRAGMENT = "DisplayFragment";
    public static final String ALERTS_FRAGMENT = "AlertsFragment";
    public static final String CONNECTIVITY_FRAGMENT = "ConnectivityFragment";
    private static SettingsActivity instance;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new RootFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            ActionBar action_bar = getSupportActionBar();
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(R.string.settings_title);
                if (action_bar != null) action_bar.setDisplayHomeAsUpEnabled(false);
            } else {
                if (action_bar != null) action_bar.setDisplayHomeAsUpEnabled(true);
            }
        });

        ActionBar action_bar = getSupportActionBar();
        if ((action_bar != null) && (getSupportFragmentManager().getBackStackEntryCount() > 0)) {
            action_bar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra("fragment")) {
            String fragment = intent.getStringExtra("fragment");

            PreferenceFragmentCompat preference_fragment;

            switch (fragment) {
                case ALERTS_FRAGMENT:
                    preference_fragment = new AlertsFragment();
                    break;
                case CONNECTIVITY_FRAGMENT:
                    preference_fragment = new ConnectivityFragment();
                    break;
                case DISPLAY_FRAGMENT:
                    preference_fragment = new DisplayFragment();
                    break;
                default:
                    preference_fragment = new RootFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, preference_fragment)
                    .commit();
        }
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
    public boolean onPreferenceStartFragment(
            @NonNull PreferenceFragmentCompat caller,
            @NonNull Preference pref) {
        Log.d(TAG, "onPreferenceStartFragment callback");
        final Bundle args = pref.getExtras();

        Fragment fragment = getSupportFragmentManager()
                .getFragmentFactory()
                .instantiate(getClassLoader(), Objects.requireNonNull(pref.getFragment()));
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit();

        setTitle(pref.getTitle());
        return true;
    }

    public static Context getContext() {
        return instance;
    }
}

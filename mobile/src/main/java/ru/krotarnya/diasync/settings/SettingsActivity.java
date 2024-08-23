package ru.krotarnya.diasync.settings;

import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Optional;

import ru.krotarnya.diasync.R;

public final class SettingsActivity extends AppCompatActivity {
    private static final String ACTIVATE_FRAGMENT = "activate_fragment";
    private static final String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            boolean hasUp = (getSupportFragmentManager().getBackStackEntryCount() != 0);
            Optional.ofNullable(getSupportActionBar())
                    .ifPresent(bar -> bar.setDisplayHomeAsUpEnabled(hasUp));
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, getActiveFragment())
                .commit();
    }

    private Fragment getActiveFragment() {
        return getSupportFragmentManager()
                .getFragmentFactory()
                .instantiate(getClassLoader(), getActiveFragmentClassName());
    }

    private String getActiveFragmentClassName() {
        return Optional.ofNullable(getIntent().getStringExtra(ACTIVATE_FRAGMENT))
                .orElse(RootFragment.class.getName());
    }

    @Override
    public boolean onSupportNavigateUp() {
        return getSupportFragmentManager().popBackStackImmediate();
    }

    private static <T extends PreferenceFragment> Intent consIntent(
            Context context,
            @Nullable Class<T> fragmentClass) {
        Intent intent = new Intent(context, SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Optional.ofNullable(fragmentClass)
                .map(Class::getName)
                .ifPresent(className -> intent.putExtra(ACTIVATE_FRAGMENT, className));

        return intent;
    }

    public static <T extends PreferenceFragment> void pleaseStartExternally(
            Context context,
            @Nullable Class<T> fragmentClass) {
        int requestCode = 0;
        Bundle options = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                ? ActivityOptions.makeBasic().setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED).toBundle()
                : null;
        int flags = PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT;
        Intent intent = consIntent(context, fragmentClass);
        try {
            PendingIntent.getActivity(context, requestCode, intent, flags, options).send();
        } catch (PendingIntent.CanceledException e) {
            Log.e(TAG, "Was not able to start activity", e);
        }
    }
}

package ru.krotarnya.diasync.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Optional;

import ru.krotarnya.diasync.R;

public final class SettingsActivity extends AppCompatActivity {
    private static final String ACTIVATE_FRAGMENT = "activate_fragment";

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

    public static <T extends PreferenceFragment> void pleaseStart(
            Context context,
            @Nullable Class<T> fragmentClass) {
        Intent intent = new Intent(context, SettingsActivity.class);
        Optional.ofNullable(fragmentClass)
                .map(Class::getName)
                .ifPresent(className -> intent.putExtra(ACTIVATE_FRAGMENT, className));

        context.startService(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}

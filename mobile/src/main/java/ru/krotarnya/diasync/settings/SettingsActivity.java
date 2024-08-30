package ru.krotarnya.diasync.settings;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.Optional;

import ru.krotarnya.diasync.R;

public final class SettingsActivity extends AppCompatActivity {
    public static final String FRAGMENT = SettingsActivity.class.getName() + ".fragment";

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
        return Optional.ofNullable(getIntent().getStringExtra(FRAGMENT))
                .orElse(RootFragment.class.getName());
    }

    @Override
    public boolean onSupportNavigateUp() {
        return getSupportFragmentManager().popBackStackImmediate();
    }
}

package fr.vinetos.tranquille;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Predicate;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new RootSettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller, PreferenceScreen pref) {
        return applyToBaseSettingsFragment(f -> f.onPreferenceStartScreen(caller, pref));
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        return applyToBaseSettingsFragment(f -> f.onPreferenceStartFragment(caller, pref));
    }

    private boolean applyToBaseSettingsFragment(Predicate<BaseSettingsFragment> predicate) {
        return applyToFragments(f -> f instanceof BaseSettingsFragment
                && predicate.test((BaseSettingsFragment) f));
    }

    private boolean applyToFragments(Predicate<Fragment> predicate) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (predicate.test(fragment)) return true;
        }
        return false;
    }

}

package fr.vinetos.tranquille;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.XmlRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import java.util.Objects;

public abstract class BaseSettingsFragment extends PreferenceFragmentCompat
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback,
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    @Override
    public void onStart() {
        super.onStart();

        requireActivity().setTitle(getPreferenceScreen().getTitle());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        checkScreenKey(rootKey);

        switchToDeviceProtectedStorage();

        setPreferencesFromResource(rootKey);

        initScreen();

        disablePreferenceIcons();
    }

    protected void checkScreenKey(String key) {
        String screenKey = getScreenKey();
        if (!TextUtils.equals(screenKey, key)) {
            throw new IllegalArgumentException("Incorrect key: " + key
                    + ", expected: " + screenKey);
        }
    }

    protected abstract String getScreenKey();

    protected void switchToDeviceProtectedStorage() {
        getPreferenceManager().setStorageDeviceProtected();
    }

    protected void setPreferencesFromResource(String rootKey) {
        setPreferencesFromResource(getPreferencesResId(), rootKey);
    }

    @XmlRes
    protected abstract int getPreferencesResId();

    protected void initScreen() {}

    protected void disablePreferenceIcons() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            preference.setIconSpaceReserved(false);
            if (preference instanceof PreferenceGroup) {
                PreferenceGroup group = (PreferenceGroup) preference;
                int nestedCount = group.getPreferenceCount();
                for (int k = 0; k < nestedCount; k++) {
                    Preference nested = group.getPreference(k);
                    nested.setIconSpaceReserved(false);
                }
            }
        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat caller,
                                           PreferenceScreen pref) {
        return switchToFragment(getFragmentForPreferenceScreen(caller, pref), pref.getKey());
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        return switchToFragment(getFragmentForPreferenceFragment(caller, pref), pref.getKey());
    }

    protected Fragment getFragmentForPreferenceScreen(PreferenceFragmentCompat caller,
                                                      PreferenceScreen pref) {
        return null; // should be overridden if needed
    }

    protected Fragment getFragmentForPreferenceFragment(PreferenceFragmentCompat caller,
                                                        Preference pref) {
        FragmentActivity activity = requireActivity();

        Fragment fragment = activity.getSupportFragmentManager().getFragmentFactory()
                .instantiate(activity.getClassLoader(), pref.getFragment());

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, pref.getKey());
        fragment.setArguments(args);

        return fragment;
    }

    protected boolean switchToFragment(Fragment fragment, String key) {
        if (fragment == null) return false;

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.settings, fragment, key)
                .addToBackStack(key)
                .commit();

        return true;
    }

    protected void setPrefChangeListener(@NonNull CharSequence key,
                                         Preference.OnPreferenceChangeListener listener) {
        requirePreference(key).setOnPreferenceChangeListener(listener);
    }

    @NonNull
    protected <T extends Preference> T requirePreference(@NonNull CharSequence key) {
        return Objects.requireNonNull(findPreference(key));
    }

}

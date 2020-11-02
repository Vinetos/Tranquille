package dummydomain.yetanothercallblocker;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import java.util.Objects;

public abstract class BaseSettingsFragment extends PreferenceFragmentCompat
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    public void onStart() {
        super.onStart();

        requireActivity().setTitle(getPreferenceScreen().getTitle());
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        checkScreenKey(rootKey);

        getPreferenceManager().setStorageDeviceProtected();

        setPreferencesFromResource(R.xml.root_preferences, rootKey);

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
        String key = pref.getKey();

        PreferenceFragmentCompat fragment = getSubscreenFragment(key);
        if (fragment == null) return false;

        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key);
        fragment.setArguments(args);

        getParentFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.settings, fragment, key)
                .addToBackStack(key)
                .commit();

        return true;
    }

    protected PreferenceFragmentCompat getSubscreenFragment(String key) {
        return null;
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

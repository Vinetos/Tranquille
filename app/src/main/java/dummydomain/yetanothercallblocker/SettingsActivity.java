package dummydomain.yetanothercallblocker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import dummydomain.yetanothercallblocker.work.UpdateScheduler;

import static java.util.Objects.requireNonNull;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Settings settings = App.getSettings();

        PermissionHelper.handlePermissionsResult(this, requestCode, permissions, grantResults,
                settings.getIncomingCallNotifications(), settings.getBlockCalls(),
                settings.getUseContacts());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (PermissionHelper.handleCallScreeningResult(this, requestCode, resultCode)) {
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (fragment instanceof SettingsFragment) {
                    ((SettingsFragment) fragment).updateCallScreeningPreference();
                }
            }
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private static final String PREF_USE_CALL_SCREENING_SERVICE = "useCallScreeningService";
        private static final String PREF_AUTO_UPDATE_ENABLED = "autoUpdateEnabled";
        private static final String PREF_CATEGORY_NOTIFICATIONS = "categoryNotifications";

        private final UpdateScheduler updateScheduler = UpdateScheduler.get(App.getInstance());

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SwitchPreferenceCompat incomingCallNotificationPref =
                    requireNonNull(findPreference(Settings.PREF_INCOMING_CALL_NOTIFICATIONS));
            incomingCallNotificationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions((AppCompatActivity) getActivity(), true, false, false);
                }
                return true;
            });

            SwitchPreferenceCompat blockCallsPref =
                    requireNonNull(findPreference(Settings.PREF_BLOCK_CALLS));
            blockCallsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions((AppCompatActivity) getActivity(), false, true, false);
                }
                return true;
            });

            SwitchPreferenceCompat callScreeningPref =
                    requireNonNull(findPreference(PREF_USE_CALL_SCREENING_SERVICE));
            callScreeningPref.setChecked(PermissionHelper.isCallScreeningHeld(getActivity()));
            callScreeningPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.requestCallScreening(getActivity());
                } else {
                    Toast.makeText(getActivity(),
                            R.string.use_call_screening_service_disable_message,
                            Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            });
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                callScreeningPref.setVisible(false);
            }

            SwitchPreferenceCompat nonPersistentAutoUpdatePref =
                    requireNonNull(findPreference(PREF_AUTO_UPDATE_ENABLED));
            nonPersistentAutoUpdatePref.setChecked(updateScheduler.isAutoUpdateScheduled());
            nonPersistentAutoUpdatePref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    updateScheduler.scheduleAutoUpdates();
                } else {
                    updateScheduler.cancelAutoUpdateWorker();
                }
                return true;
            });

            SwitchPreferenceCompat useContactsPref =
                    requireNonNull(findPreference(Settings.PREF_USE_CONTACTS));
            useContactsPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions((AppCompatActivity) getActivity(), false, false, true);
                }
                return true;
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Preference category = requireNonNull(findPreference(PREF_CATEGORY_NOTIFICATIONS));
                category.setVisible(false);
            }

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

        public void updateCallScreeningPreference() {
            SwitchPreferenceCompat callScreeningPref =
                    requireNonNull(findPreference(PREF_USE_CALL_SCREENING_SERVICE));
            callScreeningPref.setChecked(PermissionHelper.isCallScreeningHeld(getActivity()));
        }
    }
}

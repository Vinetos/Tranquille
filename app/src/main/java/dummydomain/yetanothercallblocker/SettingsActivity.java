package dummydomain.yetanothercallblocker;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import dummydomain.yetanothercallblocker.preference.IntListPreference;
import dummydomain.yetanothercallblocker.utils.DebuggingUtils;
import dummydomain.yetanothercallblocker.utils.FileUtils;
import dummydomain.yetanothercallblocker.work.UpdateScheduler;

import static java.util.Objects.requireNonNull;

public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
                                           PreferenceScreen preferenceScreen) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, preferenceScreen.getKey());
        fragment.setArguments(args);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.settings, fragment, preferenceScreen.getKey());
        ft.addToBackStack(preferenceScreen.getKey());
        ft.commit();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Settings settings = App.getSettings();

        PermissionHelper.handlePermissionsResult(this, requestCode, permissions, grantResults,
                settings.getIncomingCallNotifications(), settings.getCallBlockingEnabled(),
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
        private static final String PREF_SCREEN_ADVANCED = "screenAdvanced";
        private static final String PREF_DB_MANAGEMENT = "dbManagement";
        private static final String PREF_COUNTRY_CODES_INFO = "countryCodesInfo";
        private static final String PREF_EXPORT_LOGCAT = "exportLogcat";

        private static final Logger LOG = LoggerFactory.getLogger(SettingsFragment.class);

        private final UpdateScheduler updateScheduler = UpdateScheduler.get(App.getInstance());

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setStorageDeviceProtected();

            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            initRootScreen(rootKey);
            initAdvancedScreen(rootKey);

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

        private void initRootScreen(String rootKey) {
            if (rootKey != null) return;

            SwitchPreferenceCompat incomingCallNotificationPref =
                    requireNonNull(findPreference(Settings.PREF_INCOMING_CALL_NOTIFICATIONS));
            incomingCallNotificationPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions((AppCompatActivity) getActivity(), true, false, false);
                }
                return true;
            });

            Preference.OnPreferenceChangeListener callBlockingListener = (preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions((AppCompatActivity) getActivity(), false, true, false);
                }
                return true;
            };
            requireNonNull((SwitchPreferenceCompat) findPreference(Settings.PREF_BLOCK_NEGATIVE_SIA_NUMBERS))
                    .setOnPreferenceChangeListener(callBlockingListener);
            requireNonNull((SwitchPreferenceCompat) findPreference(Settings.PREF_BLOCK_HIDDEN_NUMBERS))
                    .setOnPreferenceChangeListener(callBlockingListener);
            requireNonNull((SwitchPreferenceCompat) findPreference(Settings.PREF_BLOCK_BLACKLISTED))
                    .setOnPreferenceChangeListener(callBlockingListener);

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

            IntListPreference uiModePref = requireNonNull(findPreference(Settings.PREF_UI_MODE));
            uiModePref.setOnPreferenceChangeListener((preference, newValue) -> {
                App.setUiMode(Integer.parseInt((String) newValue));
                return true;
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Preference category = requireNonNull(findPreference(PREF_CATEGORY_NOTIFICATIONS));
                category.setVisible(false);
            }
        }

        private void initAdvancedScreen(String rootKey) {
            if (!PREF_SCREEN_ADVANCED.equals(rootKey)) return;

            requireNonNull((Preference) findPreference(PREF_DB_MANAGEMENT))
                    .setOnPreferenceClickListener(preference -> {
                        startActivity(new Intent(getActivity(), DbManagementActivity.class));
                        return true;
                    });

            String countryCodesExplanationSummary = getString(R.string.country_codes_info_summary)
                    + ". " + getString(R.string.country_codes_info_summary_addition,
                    App.getSettings().getCachedAutoDetectedCountryCode());

            Preference countryCodesInfoPreference
                    = requireNonNull(findPreference(PREF_COUNTRY_CODES_INFO));
            countryCodesInfoPreference.setSummary(countryCodesExplanationSummary);
            countryCodesInfoPreference.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_category_country_codes)
                        .setMessage(countryCodesExplanationSummary)
                        .setNegativeButton(R.string.back, null)
                        .show();
                return true;
            });

            Preference.OnPreferenceChangeListener countryCodeChangeListener
                    = (preference, newValue) -> {
                String value = (String) newValue;
                if (TextUtils.isEmpty(value) || Pattern.matches("^[a-zA-Z]{2}$", value)) {
                    return true;
                }

                Toast.makeText(getActivity(), R.string.country_code_incorrect_format,
                        Toast.LENGTH_SHORT).show();
                return false;
            };

            EditTextPreference countryCodePreference
                    = requireNonNull(findPreference(Settings.PREF_COUNTRY_CODE_OVERRIDE));
            countryCodePreference.setOnPreferenceChangeListener(countryCodeChangeListener);

            EditTextPreference countryCodeForReviewsPreference
                    = requireNonNull(findPreference(Settings.PREF_COUNTRY_CODE_FOR_REVIEWS_OVERRIDE));
            countryCodeForReviewsPreference.setOnPreferenceChangeListener(countryCodeChangeListener);

            requireNonNull((Preference) findPreference(PREF_EXPORT_LOGCAT))
                    .setOnPreferenceClickListener(preference -> {
                        exportLogcat();
                        return true;
                    });
        }

        public void updateCallScreeningPreference() {
            SwitchPreferenceCompat callScreeningPref =
                    requireNonNull(findPreference(PREF_USE_CALL_SCREENING_SERVICE));
            callScreeningPref.setChecked(PermissionHelper.isCallScreeningHeld(getActivity()));
        }

        private void exportLogcat() {
            Activity activity = getActivity();

            String path = null;
            try {
                path = DebuggingUtils.saveLogcatInCache(activity);
                DebuggingUtils.appendDeviceInfo(path);
            } catch (IOException | InterruptedException e) {
                LOG.warn("exportLogcat()", e);
            }

            if (path != null) {
                FileUtils.shareFile(activity, new File(path));
            }
        }
    }
}

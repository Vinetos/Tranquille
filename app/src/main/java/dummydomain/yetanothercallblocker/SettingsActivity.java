package dummydomain.yetanothercallblocker;

import android.app.Activity;
import android.content.Context;
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
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.regex.Pattern;

import dummydomain.yetanothercallblocker.utils.DebuggingUtils;
import dummydomain.yetanothercallblocker.utils.FileUtils;
import dummydomain.yetanothercallblocker.utils.PackageManagerUtils;
import dummydomain.yetanothercallblocker.work.UpdateScheduler;

public class SettingsActivity extends AppCompatActivity
        implements PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

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
    public boolean onPreferenceStartScreen(PreferenceFragmentCompat preferenceFragmentCompat,
                                           PreferenceScreen preferenceScreen) {
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            if (fragment instanceof BaseSettingsFragment) {
                if (((BaseSettingsFragment) fragment)
                        .onPreferenceStartScreen(preferenceFragmentCompat, preferenceScreen)) {
                    return true;
                }
            }
        }

        return true;
    }

    public static abstract class BaseSettingsFragment extends PreferenceFragmentCompat
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

    public static class RootSettingsFragment extends BaseSettingsFragment {

        private static final String PREF_SCREEN_ROOT = null;
        private static final String PREF_USE_CALL_SCREENING_SERVICE = "useCallScreeningService";
        private static final String PREF_AUTO_UPDATE_ENABLED = "autoUpdateEnabled";
        private static final String PREF_NOTIFICATION_CHANNEL_SETTINGS = "notificationChannelSettings";
        private static final String PREF_CATEGORY_NOTIFICATIONS = "categoryNotifications";
        private static final String PREF_CATEGORY_NOTIFICATIONS_LEGACY = "categoryNotificationsLegacy";
        private static final String PREF_NOTIFICATIONS_BLOCKED_NON_PERSISTENT = "showNotificationsForBlockedCallsNonPersistent";
        private static final String PREF_SCREEN_ADVANCED = "screenAdvanced";

        private static final String STATE_REQUEST_TOKEN = "STATE_REQUEST_TOKEN";

        private final UpdateScheduler updateScheduler = UpdateScheduler.get(App.getInstance());

        private PermissionHelper.RequestToken requestToken;

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                               @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            Settings settings = App.getSettings();

            PermissionHelper.handlePermissionsResult(requireContext(),
                    requestCode, permissions, grantResults,
                    settings.getIncomingCallNotifications(), settings.getCallBlockingEnabled(),
                    settings.getUseContacts());
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (PermissionHelper.handleCallScreeningResult(
                    requireActivity(), requestCode, resultCode, requestToken)) {
                updateCallScreeningPreference();
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            requestToken = PermissionHelper.RequestToken
                    .fromSavedInstanceState(savedInstanceState, STATE_REQUEST_TOKEN);
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);

            if (requestToken != null) {
                requestToken.onSaveInstanceState(outState, STATE_REQUEST_TOKEN);
            }
        }

        @Override
        public void onStart() {
            super.onStart();

            // may be changed externally
            updateCallScreeningPreference();

            // needs to be updated after the confirmation dialog was closed
            // due to activity recreation (orientation change, etc.)
            updateBlockedCallNotificationsPreference();
        }

        @Override
        protected String getScreenKey() {
            return PREF_SCREEN_ROOT;
        }

        @Override
        protected void initScreen() {
            setPrefChangeListener(Settings.PREF_INCOMING_CALL_NOTIFICATIONS, (pref, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions(requireContext(), this,
                            true, false, false);
                }
                return true;
            });

            Preference.OnPreferenceChangeListener callBlockingListener = (preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions(requireContext(), this,
                            false, true, false);
                }
                return true;
            };
            setPrefChangeListener(Settings.PREF_BLOCK_NEGATIVE_SIA_NUMBERS, callBlockingListener);
            setPrefChangeListener(Settings.PREF_BLOCK_HIDDEN_NUMBERS, callBlockingListener);
            setPrefChangeListener(Settings.PREF_BLOCK_BLACKLISTED, callBlockingListener);

            SwitchPreferenceCompat callScreeningPref =
                    requirePreference(PREF_USE_CALL_SCREENING_SERVICE);
            callScreeningPref.setChecked(PermissionHelper.isCallScreeningHeld(requireContext()));
            callScreeningPref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    requestToken = PermissionHelper.requestCallScreening(requireActivity(), this);
                } else {
                    PermissionHelper.disableCallScreening(requireActivity());
                    return false;
                }
                return true;
            });
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                callScreeningPref.setVisible(false);
            }

            setPrefChangeListener(Settings.PREF_USE_MONITORING_SERVICE, (pref, newValue) -> {
                boolean enabled = Boolean.TRUE.equals(newValue);
                Context context = requireContext();

                PackageManagerUtils.setComponentEnabledOrDefault(
                        context, StartupReceiver.class, enabled);
                if (enabled) {
                    CallMonitoringService.start(context);
                } else {
                    CallMonitoringService.stop(context);
                }

                return true;
            });

            SwitchPreferenceCompat nonPersistentAutoUpdatePref =
                    requirePreference(PREF_AUTO_UPDATE_ENABLED);
            nonPersistentAutoUpdatePref.setChecked(updateScheduler.isAutoUpdateScheduled());
            nonPersistentAutoUpdatePref.setOnPreferenceChangeListener((preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    updateScheduler.scheduleAutoUpdates();
                } else {
                    updateScheduler.cancelAutoUpdateWorker();
                }
                return true;
            });

            setPrefChangeListener(Settings.PREF_USE_CONTACTS, (preference, newValue) -> {
                if (Boolean.TRUE.equals(newValue)) {
                    PermissionHelper.checkPermissions(requireContext(), this,
                            false, false, true);
                }
                return true;
            });

            setPrefChangeListener(Settings.PREF_UI_MODE, (preference, newValue) -> {
                App.setUiMode(Integer.parseInt((String) newValue));
                return true;
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requirePreference(PREF_NOTIFICATION_CHANNEL_SETTINGS)
                        .setOnPreferenceClickListener(preference -> {
                            Intent intent = new Intent(
                                    android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                            intent.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,
                                    BuildConfig.APPLICATION_ID);
                            startActivity(intent);
                            return true;
                        });

                requirePreference(PREF_CATEGORY_NOTIFICATIONS_LEGACY).setVisible(false);
            } else {
                requirePreference(PREF_CATEGORY_NOTIFICATIONS).setVisible(false);

                SwitchPreferenceCompat blockedCallNotificationsPref =
                        requirePreference(PREF_NOTIFICATIONS_BLOCKED_NON_PERSISTENT);
                blockedCallNotificationsPref.setChecked(
                        App.getSettings().getNotificationsForBlockedCalls());
                blockedCallNotificationsPref.setOnPreferenceChangeListener((pref, newValue) -> {
                    if (Boolean.TRUE.equals(newValue)) {
                        App.getSettings().setNotificationsForBlockedCalls(true);
                    } else {
                        new AlertDialog.Builder(requireActivity())
                                .setTitle(R.string.are_you_sure)
                                .setMessage(R.string.blocked_call_notifications_disable_message)
                                .setPositiveButton(R.string.blocked_call_notifications_disable_confirmation,
                                        (d, w) -> App.getSettings().setNotificationsForBlockedCalls(false))
                                .setNegativeButton(android.R.string.cancel, null)
                                .setOnDismissListener(d -> updateBlockedCallNotificationsPreference())
                                .show();
                    }
                    return true;
                });
            }
        }

        private void updateCallScreeningPreference() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return;

            this.<SwitchPreferenceCompat>requirePreference(PREF_USE_CALL_SCREENING_SERVICE)
                    .setChecked(PermissionHelper.isCallScreeningHeld(requireContext()));
        }

        private void updateBlockedCallNotificationsPreference() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) return;

            this.<SwitchPreferenceCompat>requirePreference(PREF_NOTIFICATIONS_BLOCKED_NON_PERSISTENT)
                    .setChecked(App.getSettings().getNotificationsForBlockedCalls());
        }

        @Override
        protected PreferenceFragmentCompat getSubscreenFragment(String key) {
            return PREF_SCREEN_ADVANCED.equals(key) ? new AdvancedSettingsFragment() : null;
        }

    }

    public static class AdvancedSettingsFragment extends BaseSettingsFragment {

        private static final String PREF_SCREEN_ADVANCED = "screenAdvanced";
        private static final String PREF_COUNTRY_CODES_INFO = "countryCodesInfo";
        private static final String PREF_EXPORT_LOGCAT = "exportLogcat";

        private static final Logger LOG = LoggerFactory.getLogger(AdvancedSettingsFragment.class);

        @Override
        protected String getScreenKey() {
            return PREF_SCREEN_ADVANCED;
        }

        @Override
        protected void initScreen() {
            String countryCodesExplanationSummary = getString(R.string.country_codes_info_summary)
                    + ". " + getString(R.string.country_codes_info_summary_addition,
                    App.getSettings().getCachedAutoDetectedCountryCode());

            Preference countryCodesInfoPreference = requirePreference(PREF_COUNTRY_CODES_INFO);
            countryCodesInfoPreference.setSummary(countryCodesExplanationSummary);
            countryCodesInfoPreference.setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(requireActivity())
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

                Toast.makeText(requireActivity(), R.string.country_code_incorrect_format,
                        Toast.LENGTH_SHORT).show();
                return false;
            };

            setPrefChangeListener(Settings.PREF_COUNTRY_CODE_OVERRIDE, countryCodeChangeListener);
            setPrefChangeListener(Settings.PREF_COUNTRY_CODE_FOR_REVIEWS_OVERRIDE,
                    countryCodeChangeListener);

            requirePreference(PREF_EXPORT_LOGCAT)
                    .setOnPreferenceClickListener(preference -> {
                        exportLogcat();
                        return true;
                    });
        }

        private void exportLogcat() {
            Activity activity = requireActivity();

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

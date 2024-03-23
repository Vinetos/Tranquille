package fr.vinetos.tranquille;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.SwitchPreferenceCompat;

import fr.vinetos.tranquille.utils.PackageManagerUtils;
import fr.vinetos.tranquille.work.UpdateScheduler;

public class RootSettingsFragment extends BaseSettingsFragment {

    private static final String PREF_SCREEN_ROOT = null;
    private static final String PREF_USE_CALL_SCREENING_SERVICE = "useCallScreeningService";
    private static final String PREF_AUTO_UPDATE_ENABLED = "autoUpdateEnabled";
    private static final String PREF_NOTIFICATION_CHANNEL_SETTINGS = "notificationChannelSettings";
    private static final String PREF_CATEGORY_NOTIFICATIONS = "categoryNotifications";
    private static final String PREF_CATEGORY_NOTIFICATIONS_LEGACY = "categoryNotificationsLegacy";
    private static final String PREF_NOTIFICATIONS_BLOCKED_NON_PERSISTENT = "showNotificationsForBlockedCallsNonPersistent";

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
                settings.getUseContacts(), settings.getUseNotification());
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
    protected int getPreferencesResId() {
        return R.xml.root_preferences;
    }

    @Override
    protected void initScreen() {
        setPrefChangeListener(Settings.PREF_INCOMING_CALL_NOTIFICATIONS, (pref, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                PermissionHelper.checkPermissions(requireContext(), this,
                        true, false, false, false);
            }
            return true;
        });

        Preference.OnPreferenceChangeListener callBlockingListener = (preference, newValue) -> {
            if (Boolean.TRUE.equals(newValue)) {
                PermissionHelper.checkPermissions(requireContext(), this,
                        false, true, false, false);
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
                        false, false, true, false);
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
                        NotificationHelper.initNotificationChannels(requireContext());

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

}

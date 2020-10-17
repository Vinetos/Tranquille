package dummydomain.yetanothercallblocker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.settings.applications.PreferredSettingsUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static dummydomain.yetanothercallblocker.IntentHelper.startActivity;
import static java.util.Objects.requireNonNull;

public class PermissionHelper {

    private static final int REQUEST_CODE_PERMISSIONS = 128;
    private static final int REQUEST_CODE_DEFAULT_DIALER = 129;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionHelper.class);

    private static final Set<String> INFO_PERMISSIONS = new HashSet<>();
    private static final Set<String> BLOCKING_PERMISSIONS = new HashSet<>();
    private static final Set<String> CONTACTS_PERMISSIONS = new HashSet<>();

    static {
        INFO_PERMISSIONS.add(Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            INFO_PERMISSIONS.add(Manifest.permission.READ_CALL_LOG);
        }

        BLOCKING_PERMISSIONS.addAll(INFO_PERMISSIONS);

        BLOCKING_PERMISSIONS.add(Manifest.permission.CALL_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            BLOCKING_PERMISSIONS.add(Manifest.permission.ANSWER_PHONE_CALLS);
        }

        CONTACTS_PERMISSIONS.add(Manifest.permission.READ_CONTACTS);
    }

    public static void checkPermissions(Activity activity, boolean info,
                                        boolean block, boolean contacts) {
        Set<String> requiredPermissions = new HashSet<>();

        if (info) requiredPermissions.addAll(INFO_PERMISSIONS);
        if (block) requiredPermissions.addAll(BLOCKING_PERMISSIONS);
        if (contacts) requiredPermissions.addAll(CONTACTS_PERMISSIONS);

        List<String> missingPermissions = new ArrayList<>(requiredPermissions.size());

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    missingPermissions.toArray(new String[0]), REQUEST_CODE_PERMISSIONS);
        }
    }

    public static void handlePermissionsResult(@NonNull Context context, int requestCode,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults,
                                               boolean infoExpected, boolean blockingExpected,
                                               boolean contactsExpected) {
        if (requestCode != REQUEST_CODE_PERMISSIONS) return;

        boolean infoDenied = false;
        boolean blockingDenied = false;
        boolean contactsDenied = false;

        if (permissions.length == 0) {
            infoDenied = true;
            blockingDenied = true;
            contactsDenied = true;
        } else {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (INFO_PERMISSIONS.contains(permission)) {
                        infoDenied = true;
                    }
                    if (BLOCKING_PERMISSIONS.contains(permission)) {
                        blockingDenied = true;
                    }
                    if (CONTACTS_PERMISSIONS.contains(permission)) {
                        contactsDenied = true;
                    }
                }
            }
        }

        LOG.debug("onRequestPermissionsResult() infoDenied={}, blockingDenied={}, contactsDenied={}",
                infoDenied, blockingDenied, contactsDenied);

        if (!infoExpected) infoDenied = false;
        if (!blockingExpected) blockingDenied = false;
        if (!contactsExpected) contactsDenied = false;

        List<String> features = new ArrayList<>(3);

        if (infoDenied) features.add(context.getString(R.string.feature_info));
        if (blockingDenied) features.add(context.getString(R.string.feature_call_blocking));
        if (contactsDenied) features.add(context.getString(R.string.feature_contacts));

        if (!features.isEmpty()) {
            String message = context.getString(R.string.denied_permissions_message) + " "
                    + TextUtils.join(", ", features);

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean hasNumberInfoPermissions(Context context) {
        for (String permission : INFO_PERMISSIONS) {
            if (!hasPermission(context, permission)) return false;
        }
        return true;
    }

    public static boolean hasCallLogPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return hasPermission(context, Manifest.permission.READ_CALL_LOG);
        }
        return true;
    }

    public static boolean hasContactsPermission(Context context) {
        return hasPermission(context, Manifest.permission.READ_CONTACTS);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static RequestToken requestCallScreening(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManagerHelper.requestCallScreeningRole(activity);
            return null;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return setAsDefaultDialer(activity);
        }
        return null;
    }

    public static boolean isCallScreeningHeld(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (RoleManagerHelper.hasCallScreeningRole(context)) return true;
        }

        return isDefaultDialer(context);
    }

    public static void disableCallScreening(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (RoleManagerHelper.hasCallScreeningRole(context)) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.default_caller_id_app)
                        .setMessage(R.string.default_caller_id_app_unset)
                        .setPositiveButton(R.string.open_system_settings,
                                (d, w) -> openDefaultAppsSettings(context))
                        .show();
                return;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isDefaultDialer(context)) {
                showDefaultDialerDialog(context, false);
            }
        }
    }

    public static boolean handleCallScreeningResult(Context context,
                                                    int requestCode, int resultCode,
                                                    RequestToken requestToken) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (RoleManagerHelper.handleCallScreeningResult(context, requestCode, resultCode)) {
                return true;
            }
        }

        return handleDefaultDialerResult(context, requestCode, resultCode, requestToken);
    }

    public static boolean isDefaultDialer(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false;

        TelecomManager telecomManager = requireNonNull(
                (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE));

        return BuildConfig.APPLICATION_ID.equals(telecomManager.getDefaultDialerPackage());
    }

    @RequiresApi(Build.VERSION_CODES.N)
    public static RequestToken setAsDefaultDialer(Activity activity) {
        if (isDefaultDialer(activity)) return null;

        Intent intent = new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER);
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,
                BuildConfig.APPLICATION_ID);

        try {
            activity.startActivityForResult(intent, REQUEST_CODE_DEFAULT_DIALER);
            return new RequestToken(System.nanoTime());
        } catch (Exception e) {
            LOG.warn("setAsDefaultDialer()", e);

            setAsDefaultDialerFallback(activity);
        }

        return null;
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private static void setAsDefaultDialerFallback(Context context) {
        showDefaultDialerDialog(context, true);
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private static void showDefaultDialerDialog(Context context, boolean set) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.default_phone_app)
                .setMessage(set ? R.string.default_phone_app_set : R.string.default_phone_app_unset)
                .setPositiveButton(R.string.open_system_settings,
                        (d, w) -> openDefaultDialerSettings(context))
                .show();
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private static void openDefaultAppsSettings(Context context) {
        if (startActivity(context, new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))) return;
        startActivity(context, new Intent(Settings.ACTION_SETTINGS));
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private static void openDefaultDialerSettings(Context context) {
        if (openDefaultDialerSettingsEmui(context)) return;
        openDefaultAppsSettings(context);
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private static boolean openDefaultDialerSettingsEmui(Context context) {
        if (!isEmui()) return false;

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_DIAL);
        intentFilter.addDataScheme(PhoneAccount.SCHEME_TEL);

        Intent preferredAppIntent = getEmuiPreferredAppIntent(intentFilter);

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.settings",
                "com.android.settings.Settings$PreferredSettingsActivity"));

        intent.putExtra("preferred_app_intent", preferredAppIntent);
        intent.putExtra("preferred_app_intent_filter", intentFilter);
        intent.putExtra("preferred_app_type",
                PreferredSettingsUtils.PreferredApplication.PREFERRED_DAILER);
        intent.putExtra("preferred_app_label", "");
        intent.putExtra("preferred_app_package_name", "");

        if (startActivity(context, intent)) {
            return true;
        }

        return startActivity(context, new Intent("com.android.settings.PREFERRED_SETTINGS"));
    }

    private static boolean isEmui() {
        // not sure about min and max SDK versions
        if ("huawei".equalsIgnoreCase(Build.BRAND) || "honor".equalsIgnoreCase(Build.BRAND)) {
            try {
                @SuppressWarnings("JavaReflectionMemberAccess")
                Field field = Intent.class.getField("FLAG_HW_HOME_INTENT_FROM_SYSTEM");
                field.setAccessible(true);
                return field.getInt(Intent.class) != 0;
            } catch (Exception e) {
                LOG.debug("isEmui()", e);
            }
        }
        return false;
    }

    private static Intent getEmuiPreferredAppIntent(IntentFilter filter) {
        Intent intent = new Intent(filter.getAction(0));
        if (filter.countCategories() > 0 && !TextUtils.isEmpty(filter.getCategory(0))) {
            intent.addCategory(filter.getCategory(0));
        }
        if (filter.countDataSchemes() > 0 && !TextUtils.isEmpty(filter.getDataScheme(0))) {
            Uri localUri = Uri.parse(filter.getDataScheme(0) + ":");
            String str = null;
            if (filter.countDataTypes() > 0 && !TextUtils.isEmpty(filter.getDataType(0))) {
                str = filter.getDataType(0);
                if (!str.contains("\\") && !str.contains("/")) {
                    str = str + "/*";
                }
            }
            intent.setDataAndType(localUri, str);
        }
        return intent;
    }

    public static boolean handleDefaultDialerResult(Context context,
                                                    int requestCode, int resultCode,
                                                    RequestToken requestToken) {
        if (requestCode != REQUEST_CODE_DEFAULT_DIALER) return false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return true;
        if (resultCode == Activity.RESULT_OK) return true;

        if (requestToken != null && System.nanoTime() - requestToken.timestamp
                < TimeUnit.MILLISECONDS.toNanos(500)) {
            // probably the request is not supported, try workarounds
            setAsDefaultDialerFallback(context);
        } else {
            Toast.makeText(context, R.string.denied_default_dialer_message, Toast.LENGTH_LONG)
                    .show();
        }

        return true;
    }

    public static class RequestToken {
        private final long timestamp;

        private RequestToken(long timestamp) {
            this.timestamp = timestamp;
        }

        public void onSaveInstanceState(@NonNull Bundle outState, String key) {
            outState.putLong(key, timestamp);
        }

        public static RequestToken fromSavedInstanceState(@Nullable Bundle savedInstanceState,
                                                          String key) {
            return savedInstanceState != null && savedInstanceState.containsKey(key)
                    ? new RequestToken(savedInstanceState.getLong(key)) : null;
        }
    }

}

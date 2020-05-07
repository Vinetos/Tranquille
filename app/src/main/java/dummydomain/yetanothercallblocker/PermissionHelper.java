package dummydomain.yetanothercallblocker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionHelper {

    private static final int PERMISSION_REQUEST_CODE = 1;

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

    public static void checkPermissions(AppCompatActivity activity, boolean info,
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
                    missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    public static void onRequestPermissionsResult(@NonNull Context context, int requestCode,
                                                  @NonNull String[] permissions,
                                                  @NonNull int[] grantResults,
                                                  boolean infoExpected, boolean blockingExpected,
                                                  boolean contactsExpected) {
        if (requestCode != PERMISSION_REQUEST_CODE) return;

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

    public static boolean hasCallLogPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return hasPermission(context, Manifest.permission.READ_CALL_LOG);
        } else {
            return true; // TODO: check
        }
    }

    public static boolean hasContactsPermission(Context context) {
        return hasPermission(context, Manifest.permission.READ_CONTACTS);
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

}

package dummydomain.yetanothercallblocker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PermissionHelper {

    private static final int PERMISSION_REQUEST_CODE = 1;

    private static final Logger LOG = LoggerFactory.getLogger(PermissionHelper.class);

    private static final Set<String> INFO_PERMISSIONS = new HashSet<>();
    private static final Set<String> BLOCKING_PERMISSIONS = new HashSet<>();

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
    }

    public static void checkPermissions(AppCompatActivity activity, boolean info, boolean block) {
        Collection<String> requiredPermissions = block ? BLOCKING_PERMISSIONS
                : info ? INFO_PERMISSIONS : Collections.emptyList();

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
                                                  boolean infoExpected, boolean blockingExpected) {
        if (requestCode != PERMISSION_REQUEST_CODE) return;

        boolean infoDenied = false;
        boolean blockingDenied = false;

        if (permissions.length == 0) {
            infoDenied = true;
            blockingDenied = true;
        } else {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (INFO_PERMISSIONS.contains(permission)) {
                        infoDenied = true;
                        blockingDenied = true;
                        break;
                    } else if (BLOCKING_PERMISSIONS.contains(permission)) {
                        blockingDenied = true;
                    }
                }
            }
        }

        LOG.debug("onRequestPermissionsResult() infoDenied={}, blockingDenied={}",
                infoDenied, blockingDenied);

        if (!blockingExpected) blockingDenied = false;
        if (!infoExpected) infoDenied = false;

        String message;
        if (infoDenied && blockingDenied) {
            message = context.getString(R.string.denied_permissions_message_blocking_and_info);
        } else if (infoDenied) {
            message = context.getString(R.string.denied_permissions_message_info);
        } else if (blockingDenied) {
            message = context.getString(R.string.denied_permissions_message_blocking);
        } else {
            message = null;
        }

        if (message != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static boolean havePermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

}

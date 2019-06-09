package dummydomain.yetanothercallblocker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    private static final int PERMISSION_REQUEST_CODE = 1;

    public static void checkPermissions(AppCompatActivity activity) {
        List<String> requiredPermissions = new ArrayList<>();
        requiredPermissions.add(Manifest.permission.READ_PHONE_STATE);
        requiredPermissions.add(Manifest.permission.CALL_PHONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            requiredPermissions.add(Manifest.permission.READ_CALL_LOG);
        }

        List<String> missingPermissions = new ArrayList<>();

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                    missingPermissions.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

}

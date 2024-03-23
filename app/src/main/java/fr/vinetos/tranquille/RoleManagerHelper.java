package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import static android.content.Context.ROLE_SERVICE;
import static java.util.Objects.requireNonNull;

@RequiresApi(Build.VERSION_CODES.Q)
public class RoleManagerHelper {

    private static final int REQUEST_CODE_CALL_SCREENING = 130;

    public static boolean hasCallScreeningRole(Context context) {
        return getRoleManager(context).isRoleHeld(RoleManager.ROLE_CALL_SCREENING);
    }

    public static void requestCallScreeningRole(Activity activity) {
        if (hasCallScreeningRole(activity)) return;

        activity.startActivityForResult(
                getCallScreeningRequestIntent(activity), REQUEST_CODE_CALL_SCREENING);
    }

    public static void requestCallScreeningRole(Context context, Fragment fragment) {
        if (hasCallScreeningRole(context)) return;

        fragment.startActivityForResult(
                getCallScreeningRequestIntent(context), REQUEST_CODE_CALL_SCREENING);
    }

    private static Intent getCallScreeningRequestIntent(Context context) {
        return getRoleManager(context).createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
    }

    public static boolean handleCallScreeningResult(Context context,
                                                    int requestCode, int resultCode) {
        if (requestCode != REQUEST_CODE_CALL_SCREENING) return false;

        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(context, R.string.denied_call_screening_role_message, Toast.LENGTH_LONG)
                    .show();
        }

        return true;
    }

    private static RoleManager getRoleManager(Context context) {
        @SuppressLint("WrongConstant")
        RoleManager roleManager = (RoleManager) context.getSystemService(ROLE_SERVICE);
        return requireNonNull(roleManager);
    }

}

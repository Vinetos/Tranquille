package fr.vinetos.tranquille.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.os.UserManagerCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class SystemUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SystemUtils.class);

    private static Boolean fileBasedEncryptionEnabled;
    private static boolean userUnlocked;

    public static boolean isFileBasedEncryptionEnabled() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false;

        Boolean enabled = fileBasedEncryptionEnabled;
        if (enabled == null) {
            enabled = fileBasedEncryptionEnabled = isFileBasedEncryptionEnabledInternal();
        }

        return enabled;
    }

    private static Boolean isFileBasedEncryptionEnabledInternal() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> cls = Class.forName("android.os.SystemProperties");
            Method get = cls.getMethod("get", String.class);

            String type = (String) get.invoke(null, "ro.crypto.type");
            if (!TextUtils.equals(type, "file")) return false;

            String state = (String) get.invoke(null, "ro.crypto.state");
            return TextUtils.equals(state, "encrypted");
        } catch (Exception e) {
            LOG.warn("isFileBasedEncryptionEnabledInternal()", e);
        }

        return true; // *assume* it is enabled, if the check fails
    }

    public static boolean isUserUnlocked(Context context) {
        if (userUnlocked) return true;

        if (UserManagerCompat.isUserUnlocked(context)) {
            userUnlocked = true;
            return true;
        }

        return false;
    }

}

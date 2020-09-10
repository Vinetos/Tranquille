package dummydomain.yetanothercallblocker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.internal.telephony.ITelephony;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

public class PhoneUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PhoneUtils.class);

    public static boolean endCall(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                TelecomManager telecomManager = requireNonNull(
                        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE));

                telecomManagerEndCall(telecomManager);
                LOG.info("Rejected call using TelecomManager");

                return true;
            } catch (Exception e) {
                LOG.warn("Error while rejecting call on API 28+", e);
            }
        }

        try {
            TelephonyManager tm = requireNonNull(
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

            @SuppressLint("DiscouragedPrivateApi") // no choice
            Method m = tm.getClass().getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephony = requireNonNull((ITelephony) m.invoke(tm));

            telephony.endCall();
            LOG.info("Rejected call using ITelephony");

            return true;
        } catch (Exception e) {
            LOG.warn("Error while rejecting call", e);
        }

        return false;
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // no choice
    @SuppressLint("MissingPermission") // maybe shouldn't
    @RequiresApi(Build.VERSION_CODES.P)
    private static void telecomManagerEndCall(TelecomManager telecomManager) {
        telecomManager.endCall();
    }

}

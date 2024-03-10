package fr.vinetos.tranquille.utils;

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

    public static boolean endCall(@NonNull Context context, boolean offHook) {
        LOG.debug("endCall() started");

        if (offHook) {
            // According to docs, it should work with TelecomManager,
            // but it doesn't (the ongoing call is ended instead).
            LOG.warn("endCall() cannot end a new call while off-hook");
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                TelecomManager telecomManager = requireNonNull(
                        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE));

                if (telecomManagerEndCall(telecomManager)) {
                    LOG.info("endCall() ended call using TelecomManager");
                } else {
                    LOG.warn("endCall() TelecomManager returned false");
                }

                return true;
            } catch (Exception e) {
                LOG.warn("endCall() error while ending call with TelecomManager", e);
            }
        } else {
            try {
                TelephonyManager tm = requireNonNull(
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

                Method m = tm.getClass().getDeclaredMethod("getITelephony");
                m.setAccessible(true);
                ITelephony telephony = requireNonNull((ITelephony) m.invoke(tm));

                if (telephony.endCall()) {
                    LOG.info("endCall() ended call using ITelephony");
                } else {
                    LOG.warn("endCall() ITelephony returned false");
                }

                return true;
            } catch (Exception e) {
                LOG.warn("endCall() error while ending call with ITelephony", e);
            }
        }

        return false;
    }

    @SuppressWarnings({"deprecation", "RedundantSuppression"}) // no choice
    @SuppressLint("MissingPermission") // maybe shouldn't
    @RequiresApi(Build.VERSION_CODES.P)
    private static boolean telecomManagerEndCall(TelecomManager telecomManager) {
        return telecomManager.endCall();
    }

}

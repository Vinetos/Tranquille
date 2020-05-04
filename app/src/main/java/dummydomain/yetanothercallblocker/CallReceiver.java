package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.android.internal.telephony.ITelephony;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.NumberInfo;

public class CallReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CallReceiver.class);

    private static boolean isOnCall; // TODO: proper handling

    public static boolean isEnabled(Context context) {
        return context.getPackageManager()
                .getComponentEnabledSetting(new ComponentName(context, CallReceiver.class))
                != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    public static void setEnabled(Context context, boolean enable) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, CallReceiver.class),
                enable ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;

        String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        LOG.info("Received intent: action={}, extraState={}, incomingNumber={}",
                intent.getAction(), telephonyExtraState, incomingNumber);

        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(telephonyExtraState)) {
            isOnCall = true;
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(telephonyExtraState)) {
            if (incomingNumber == null) return;

            NumberInfo numberInfo = DatabaseSingleton.getNumberInfo(incomingNumber);

            boolean blocked = false;
            if (!isOnCall && numberInfo.rating == NumberInfo.Rating.NEGATIVE
                    && new Settings(context).getBlockCalls()) {
                blocked = rejectCall(context);

                if (blocked) {
                    NotificationHelper.showBlockedCallNotification(context, numberInfo);
                }
            }

            if (!blocked) NotificationHelper.showIncomingCallNotification(context, numberInfo);
        } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(telephonyExtraState)) {
            isOnCall = false;
            NotificationHelper.hideIncomingCallNotification(context, incomingNumber);
        }
    }

    @SuppressLint("MissingPermission")
    private boolean rejectCall(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            TelecomManager telecomManager = (TelecomManager)
                    context.getSystemService(Context.TELECOM_SERVICE);
            try {
                telecomManager.endCall();
                LOG.info("Rejected call using TelecomManager");

                return true;
            } catch (Exception e) {
                LOG.warn("Error while rejecting call on API 26+", e);
            }
        }

        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method m = tm.getClass().getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephony = (ITelephony)m.invoke(tm);

            telephony.endCall();
            LOG.info("Rejected call using ITelephony");

            return true;
        } catch (Exception e) {
            LOG.warn("Error while rejecting call", e);
        }

        return false;
    }

}

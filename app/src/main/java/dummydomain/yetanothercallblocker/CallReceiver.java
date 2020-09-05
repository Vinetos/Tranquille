package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.android.internal.telephony.ITelephony;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import dummydomain.yetanothercallblocker.data.NumberInfo;
import dummydomain.yetanothercallblocker.data.NumberInfoService;
import dummydomain.yetanothercallblocker.data.YacbHolder;
import dummydomain.yetanothercallblocker.event.CallEndedEvent;
import dummydomain.yetanothercallblocker.event.CallOngoingEvent;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;
import static java.util.Objects.requireNonNull;

public class CallReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CallReceiver.class);

    private static boolean isOnCall; // TODO: proper handling

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;

        String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        boolean hasNumberExtra = intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        LOG.info("Received intent: extraState={}, incomingNumber={}, hasNumberExtra={}",
                telephonyExtraState, incomingNumber, hasNumberExtra);

        extraLogging(intent); // TODO: make optional or remove

        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(telephonyExtraState)) {
            isOnCall = true;
            postEvent(new CallOngoingEvent());
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(telephonyExtraState)) {
            if (incomingNumber == null) {
                if (hasNumberExtra) {
                    incomingNumber = "";
                } else {
                    if (!PermissionHelper.hasNumberInfoPermissions(context)) {
                        LOG.warn("No info permissions");
                        return;
                    }
                    return; // TODO: check
                }
            }

            Settings settings = App.getSettings();

            boolean blockingEnabled = settings.getCallBlockingEnabled();
            boolean showNotifications = settings.getIncomingCallNotifications();

            if (blockingEnabled || showNotifications) {
                NumberInfoService numberInfoService = YacbHolder.getNumberInfoService();
                NumberInfo numberInfo = numberInfoService.getNumberInfo(incomingNumber,
                        settings.getCachedAutoDetectedCountryCode(), false);

                boolean blocked = false;
                if (blockingEnabled && !isOnCall && numberInfoService.shouldBlock(numberInfo)) {
                    blocked = rejectCall(context);

                    if (blocked) {
                        NotificationHelper.showBlockedCallNotification(context, numberInfo);

                        numberInfoService.blockedCall(numberInfo);

                        postEvent(new CallEndedEvent());
                    }
                }

                if (!blocked && showNotifications) {
                    NotificationHelper.showIncomingCallNotification(context, numberInfo);
                }
            }
        } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(telephonyExtraState)) {
            isOnCall = false;
            NotificationHelper.hideIncomingCallNotification(context);
            postEvent(new CallEndedEvent());
        }
    }

    private void extraLogging(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            LOG.trace("extraLogging() extras:");
            for (String k : extras.keySet()) {
                LOG.trace("extraLogging() key={}, value={}", k, extras.get(k));
            }

            Object subscription = extras.get("subscription"); // PhoneConstants.SUBSCRIPTION_KEY
            if (subscription != null) {
                LOG.trace("extraLogging() subscription.class={}", subscription.getClass());
                if (subscription instanceof Number) {
                    long subId = ((Number) subscription).longValue();
                    LOG.trace("extraLogging() subId={}, check={}",
                            subId, subId < Integer.MAX_VALUE);
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private boolean rejectCall(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                TelecomManager telecomManager = requireNonNull(
                        (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE));

                //noinspection deprecation
                telecomManager.endCall();
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

}

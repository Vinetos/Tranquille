package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;

import androidx.annotation.NonNull;

import com.android.internal.telephony.ITelephony;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import dummydomain.yetanothercallblocker.data.DatabaseSingleton;
import dummydomain.yetanothercallblocker.data.NumberInfo;
import dummydomain.yetanothercallblocker.event.CallEndedEvent;
import dummydomain.yetanothercallblocker.event.CallOngoingEvent;
import dummydomain.yetanothercallblocker.event.CallStartedEvent;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;

public class CallReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CallReceiver.class);

    private static boolean isOnCall; // TODO: proper handling

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) return;

        String telephonyExtraState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        LOG.info("Received intent: action={}, extraState={}, incomingNumber={}",
                intent.getAction(), telephonyExtraState, incomingNumber);

        if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(telephonyExtraState)) {
            isOnCall = true;
            postEvent(new CallOngoingEvent());
        } else if (TelephonyManager.EXTRA_STATE_RINGING.equals(telephonyExtraState)) {
            if (incomingNumber == null) return;

            postEvent(new CallStartedEvent());

            Settings settings = App.getSettings();

            boolean blockCalls = settings.getBlockCalls();
            boolean showNotifications = settings.getIncomingCallNotifications();

            if (blockCalls || showNotifications) {
                NumberInfo numberInfo = DatabaseSingleton.getNumberInfo(incomingNumber);

                boolean blocked = false;
                if (blockCalls && !isOnCall && numberInfo.rating == NumberInfo.Rating.NEGATIVE
                        && numberInfo.contactItem == null) {
                    blocked = rejectCall(context);

                    if (blocked) {
                        NotificationHelper.showBlockedCallNotification(context, numberInfo);
                        postEvent(new CallEndedEvent());
                    }
                }

                if (!blocked && showNotifications) {
                    NotificationHelper.showIncomingCallNotification(context, numberInfo);
                }
            }
        } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(telephonyExtraState)) {
            isOnCall = false;
            NotificationHelper.hideIncomingCallNotification(context, incomingNumber);
            postEvent(new CallEndedEvent());
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
                LOG.warn("Error while rejecting call on API 28+", e);
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

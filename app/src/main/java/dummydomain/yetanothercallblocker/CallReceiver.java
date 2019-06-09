package dummydomain.yetanothercallblocker;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

import dummydomain.yetanothercallblocker.sia.DatabaseSingleton;
import dummydomain.yetanothercallblocker.sia.model.NumberInfo;
import dummydomain.yetanothercallblocker.sia.model.database.CommunityDatabaseItem;
import dummydomain.yetanothercallblocker.sia.model.database.FeaturedDatabaseItem;

public class CallReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(CallReceiver.class);

    private boolean isOnCall; // TODO: check: is this object not destroyed in-between calls?

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

    // TODO: handle in-call calls

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

            NumberInfo numberInfo = getNumberInfo(incomingNumber);

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

    protected NumberInfo getNumberInfo(String number) {
        LOG.debug("getNumberInfo({}) started", number);
        // TODO: check number format

        CommunityDatabaseItem communityItem = DatabaseSingleton.getCommunityDatabase()
                .getDbItemByNumber(number);
        LOG.trace("getNumberInfo() communityItem={}", communityItem);

        FeaturedDatabaseItem featuredItem = DatabaseSingleton.getFeaturedDatabase()
                .getDbItemByNumber(number);
        LOG.trace("getNumberInfo() featuredItem={}", featuredItem);

        NumberInfo numberInfo = new NumberInfo();
        numberInfo.number = number;
        if (featuredItem != null) numberInfo.name = featuredItem.getName();

        if (communityItem != null && communityItem.hasRatings()) {
            if (communityItem.getNegativeRatingsCount() > communityItem.getPositiveRatingsCount()
                    + communityItem.getNeutralRatingsCount()) {
                numberInfo.rating = NumberInfo.Rating.NEGATIVE;
            } else if (communityItem.getPositiveRatingsCount() > communityItem.getNeutralRatingsCount()
                    + communityItem.getNegativeRatingsCount()) {
                numberInfo.rating = NumberInfo.Rating.POSITIVE;
            } else if (communityItem.getNeutralRatingsCount() > communityItem.getPositiveRatingsCount()
                    + communityItem.getNegativeRatingsCount()) {
                numberInfo.rating = NumberInfo.Rating.NEUTRAL;
            }
        }
        numberInfo.communityDatabaseItem = communityItem;

        LOG.trace("getNumberInfo() rating={}", numberInfo.rating);

        return numberInfo;
    }

    private boolean rejectCall(@NonNull Context context) {
        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method m = tm.getClass().getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephony = (ITelephony)m.invoke(tm);

            telephony.endCall();

            return true;
        } catch (Exception e) {
            LOG.warn("Error while rejecting call", e);
        }

        return false;
    }

}

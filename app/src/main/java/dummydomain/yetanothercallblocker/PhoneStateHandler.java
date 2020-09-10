package dummydomain.yetanothercallblocker;

import android.content.Context;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import dummydomain.yetanothercallblocker.data.NumberInfo;
import dummydomain.yetanothercallblocker.data.NumberInfoService;
import dummydomain.yetanothercallblocker.event.CallEndedEvent;
import dummydomain.yetanothercallblocker.event.CallOngoingEvent;
import dummydomain.yetanothercallblocker.utils.PhoneUtils;

import static dummydomain.yetanothercallblocker.EventUtils.postEvent;

public class PhoneStateHandler {

    private static final Logger LOG = LoggerFactory.getLogger(PhoneStateHandler.class);

    private final Context context;
    private final Settings settings;
    private final NumberInfoService numberInfoService;
    private final NotificationService notificationService;

    private boolean isOffHook;

    private List<CallEvent> lastEvents = new ArrayList<>();
    private long lastEventTimestamp;

    public PhoneStateHandler(Context context, Settings settings,
                             NumberInfoService numberInfoService,
                             NotificationService notificationService) {
        this.context = context;
        this.settings = settings;
        this.numberInfoService = numberInfoService;
        this.notificationService = notificationService;
    }

    public void onRinging(String phoneNumber) {
        LOG.debug("onRinging({})", phoneNumber);

        boolean ignore = false;

        if (phoneNumber == null) {
            if (!PermissionHelper.hasNumberInfoPermissions(context)) {
                LOG.warn("onRinging() no info permissions");
                return;
            }

            // TODO: check
            LOG.debug("onRinging() ignoring null");
            ignore = true;
        }

        if (!ignore && !shouldProcess(phoneNumber)) {
            LOG.debug("onRinging() ignoring repeated event");
            ignore = true;
        }

        recordEvent(phoneNumber);

        if (ignore) return;

        boolean blockingEnabled = settings.getCallBlockingEnabled();
        boolean showNotifications = settings.getIncomingCallNotifications();

        if (!blockingEnabled && !showNotifications) {
            return;
        }

        NumberInfo numberInfo = numberInfoService.getNumberInfo(phoneNumber,
                settings.getCachedAutoDetectedCountryCode(), false);

        boolean blocked = false;
        if (blockingEnabled && !isOffHook && numberInfoService.shouldBlock(numberInfo)) {
            blocked = PhoneUtils.endCall(context);

            if (blocked) {
                notificationService.notifyCallBlocked(numberInfo);

                numberInfoService.blockedCall(numberInfo);

                postEvent(new CallEndedEvent());
            }
        }

        if (!blocked && showNotifications) {
            notificationService.startCallIndication(numberInfo);
        }
    }

    public void onOffHook(String phoneNumber) {
        LOG.debug("onOffHook({})", phoneNumber);

        isOffHook = true;

        postEvent(new CallOngoingEvent());
    }

    public void onIdle(String phoneNumber) {
        LOG.debug("onIdle({})", phoneNumber);

        isOffHook = false;

        notificationService.stopAllCallsIndication();

        postEvent(new CallEndedEvent());
    }

    private boolean shouldProcess(String phoneNumber) {
        // using 1 second ago as the cutoff point - consider everything older as unrelated events
        long cutoff = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);

        if (lastEventTimestamp - cutoff < 0) { // no events in the last second
            lastEvents.clear();
            return true;
        }

        for (ListIterator<CallEvent> it = lastEvents.listIterator(); it.hasNext(); ) {
            CallEvent event = it.next();
            if (event.timestamp - cutoff < 0) { // event is older than the cutoff point
                it.remove();
            } else if (TextUtils.equals(event.number, phoneNumber)) {
                return false; // don't process same event
            }
        }

        return true;
    }

    private void recordEvent(String phoneNumber) {
        long currentTimestamp = System.nanoTime();
        lastEvents.add(new CallEvent(phoneNumber, currentTimestamp));
        lastEventTimestamp = currentTimestamp;
    }

    private static class CallEvent {
        final String number;
        final long timestamp;

        public CallEvent(String number, long timestamp) {
            this.number = number;
            this.timestamp = timestamp;
        }
    }

}

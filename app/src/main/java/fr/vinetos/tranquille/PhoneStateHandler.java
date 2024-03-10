package fr.vinetos.tranquille;

import android.content.Context;
import android.text.TextUtils;

import androidx.core.util.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import fr.vinetos.tranquille.data.NumberInfo;
import fr.vinetos.tranquille.data.NumberInfoService;
import fr.vinetos.tranquille.event.CallEndedEvent;
import fr.vinetos.tranquille.event.CallOngoingEvent;
import fr.vinetos.tranquille.utils.PhoneUtils;

import static fr.vinetos.tranquille.EventUtils.postEvent;
import static fr.vinetos.tranquille.utils.StringUtils.quote;

public class PhoneStateHandler {

    public enum Source {
        PHONE_STATE_LISTENER,
        PHONE_STATE_BROADCAST_RECEIVER_MONITORING,
        PHONE_STATE_BROADCAST_RECEIVER
    }

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

    public void onRinging(Source source, String phoneNumber) {
        LOG.debug("onRinging({}, {})", source, quote(phoneNumber));

        boolean ignore = false;

        if (phoneNumber == null) {
            if (!PermissionHelper.hasNumberInfoPermissions(context)) {
                LOG.warn("onRinging() no info permissions");
                return;
            }

            if (source == Source.PHONE_STATE_LISTENER) {
                LOG.info("onRinging() treating null from PhoneStateListener as a hidden number");
                phoneNumber = "";
            } else if ((source == Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING
                    || source == Source.PHONE_STATE_BROADCAST_RECEIVER)
                    && isEventPresent(sameSourceAndNumber(source, null))
                    && !isEventPresent(nonEmptyNumber())) {
                LOG.info("onRinging() treating repeated null from PhoneStateBroadcastReceiver" +
                        " as a hidden number");
                phoneNumber = "";
            }

            if (phoneNumber == null) {
                LOG.debug("onRinging() ignoring null");
                ignore = true;
            }
        }

        if (!ignore && isEventPresent(sameNumber(phoneNumber))) {
            LOG.debug("onRinging() ignoring repeated event");
            ignore = true;
        }

        recordEvent(source, phoneNumber);

        if (ignore) return;

        boolean blockingEnabled = settings.getCallBlockingEnabled();
        boolean showNotifications = settings.getIncomingCallNotifications();

        if (!blockingEnabled && !showNotifications) {
            return;
        }

        NumberInfo numberInfo = numberInfoService.getNumberInfo(phoneNumber,
                settings.getCachedAutoDetectedCountryCode(), false);

        boolean blocked = false;
        if (blockingEnabled && numberInfoService.shouldBlock(numberInfo)) {
            blocked = PhoneUtils.endCall(context, isOffHook);

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

    public void onOffHook(Source source, String phoneNumber) {
        LOG.debug("onOffHook({}, {})", source, quote(phoneNumber));

        isOffHook = true;

        postEvent(new CallOngoingEvent());
    }

    public void onIdle(Source source, String phoneNumber) {
        LOG.debug("onIdle({}, {})", source, quote(phoneNumber));

        isOffHook = false;

        notificationService.stopAllCallsIndication();

        postEvent(new CallEndedEvent());
    }

    private static Predicate<CallEvent> sameNumber(String number) {
        return event -> TextUtils.equals(event.number, number);
    }

    private static Predicate<CallEvent> sameSourceAndNumber(Source source, String number) {
        return event -> event.source == source && TextUtils.equals(event.number, number);
    }

    private static Predicate<CallEvent> nonEmptyNumber() {
        return event -> !TextUtils.isEmpty(event.number);
    }

    private boolean isEventPresent(Predicate<CallEvent> predicate) {
        return findEvent(predicate) != null;
    }

    private CallEvent findEvent(Predicate<CallEvent> predicate) {
        // using 1 second ago as the cutoff point - consider everything older as unrelated events
        long cutoff = System.nanoTime() - TimeUnit.SECONDS.toNanos(1);

        if (lastEventTimestamp - cutoff < 0) { // no events in the last second
            lastEvents.clear();
            return null;
        }

        for (ListIterator<CallEvent> it = lastEvents.listIterator(); it.hasNext(); ) {
            CallEvent event = it.next();
            if (event.timestamp - cutoff < 0) { // event is older than the cutoff point
                it.remove();
            } else if (predicate.test(event)) {
                return event;
            }
        }

        return null;
    }

    private void recordEvent(Source source, String phoneNumber) {
        long currentTimestamp = System.nanoTime();
        lastEvents.add(new CallEvent(source, phoneNumber, currentTimestamp));
        lastEventTimestamp = currentTimestamp;
    }

    private static class CallEvent {
        final Source source;
        final String number;
        final long timestamp;

        public CallEvent(Source source, String number, long timestamp) {
            this.source = source;
            this.number = number;
            this.timestamp = timestamp;
        }
    }

}

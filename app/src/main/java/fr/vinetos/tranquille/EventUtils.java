package fr.vinetos.tranquille;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventUtils {

    private static final Logger LOG = LoggerFactory.getLogger(EventUtils.class);

    private static EventUtils instance;

    private static class Holder {
        static final EventBus bus = createBus();
    }

    public static EventBus bus() {
        return Holder.bus;
    }

    public static void register(Object subscriber) {
        bus().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        bus().unregister(subscriber);
    }

    public static void postEvent(Object event) {
        bus().post(event);
    }

    public static void postStickyEvent(Object event) {
        bus().postSticky(event);
    }

    public static void removeStickyEvent(Object event) {
        bus().removeStickyEvent(event);
    }

    private static EventBus createBus() {
        EventBus bus = EventBus.builder()
                .throwSubscriberException(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(false)
                .addIndex(new EventBusIndex())
                .installDefaultEventBus();

        instance = new EventUtils();
        bus.register(instance);

        return bus;
    }

    private EventUtils() {}

    @Subscribe
    public void onSubscriberExceptionEvent(SubscriberExceptionEvent event) {
        LOG.warn("onSubscriberExceptionEvent()", event.throwable);
    }

}

package dummydomain.yetanothercallblocker;

import org.greenrobot.eventbus.EventBus;

public class EventUtils {

    public static void postEvent(Object event) {
        bus().post(event);
    }

    public static void postStickyEvent(Object event) {
        bus().postSticky(event);
    }

    public static void removeStickyEvent(Object event) {
        bus().removeStickyEvent(event);
    }

    private static EventBus bus() {
        return EventBus.getDefault();
    }

}

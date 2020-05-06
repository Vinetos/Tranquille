package dummydomain.yetanothercallblocker;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EventHandler.class);

    private static EventHandler instance;

    private Context context;

    public static EventHandler create(Context context) {
        EventHandler instance = new EventHandler(context);

        EventBus.getDefault().register(instance);

        return EventHandler.instance = instance;
    }

    private EventHandler(Context context) {
        this.context = context;
    }

    @Subscribe
    public void onSubscriberExceptionEvent(SubscriberExceptionEvent event) {
        LOG.warn("onSubscriberExceptionEvent()", event.throwable);
    }

}

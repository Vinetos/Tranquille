package dummydomain.yetanothercallblocker;

import android.app.Application;

import org.greenrobot.eventbus.EventBus;

public class App extends Application {

    private static App instance;

    private static Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        settings = new Settings(this);

        EventBus.builder()
                .throwSubscriberException(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(false)
                .addIndex(new EventBusIndex())
                .installDefaultEventBus();

        EventHandler.create(this);

        NotificationHelper.createNotificationChannels(this);
    }

    public static App getInstance() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }

}

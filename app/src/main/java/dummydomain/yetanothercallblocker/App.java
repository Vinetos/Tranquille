package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.app.Application;

import org.conscrypt.Conscrypt;
import org.greenrobot.eventbus.EventBus;

import java.security.Security;

import dummydomain.yetanothercallblocker.data.Config;

public class App extends Application {

    private static App instance;

    @SuppressLint("StaticFieldLeak")
    private static Settings settings;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        Security.insertProviderAt(Conscrypt.newProvider(), 1);

        settings = new Settings(this);
        settings.init();

        EventBus.builder()
                .throwSubscriberException(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(false)
                .addIndex(new EventBusIndex())
                .installDefaultEventBus();

        EventHandler.create(this);

        NotificationHelper.createNotificationChannels(this);

        Config.init(this, settings);
    }

    public static App getInstance() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }

}

package dummydomain.yetanothercallblocker;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import org.greenrobot.eventbus.EventBus;

import dummydomain.yetanothercallblocker.data.Config;

public class App extends Application {

    private static App instance;

    @SuppressLint("StaticFieldLeak")
    private static Settings settings;

    public static App getInstance() {
        return instance;
    }

    public static Settings getSettings() {
        return settings;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        new DeviceProtectedStorageMigrator().migrate(this);

        settings = new Settings(getDeviceProtectedStorageContext());
        settings.init();

        EventBus.builder()
                .throwSubscriberException(BuildConfig.DEBUG)
                .sendNoSubscriberEvent(false)
                .addIndex(new EventBusIndex())
                .installDefaultEventBus();

        EventHandler.create(this);

        Config.init(getDeviceProtectedStorageContext(), settings);
    }

    private Context getDeviceProtectedStorageContext() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createDeviceProtectedStorageContext();
        } else {
            return this;
        }
    }

}

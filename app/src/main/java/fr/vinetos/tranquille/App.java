package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

import fr.vinetos.tranquille.utils.DebuggingUtils;
import fr.vinetos.tranquille.data.Config;

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

    public static void setUiMode(int uiMode) {
        AppCompatDelegate.setDefaultNightMode(uiMode);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        DebuggingUtils.setUpCrashHandler();

        new DeviceProtectedStorageMigrator().migrate(this);

        settings = new Settings(getDeviceProtectedStorageContext());
        settings.init();

        Config.init(getDeviceProtectedStorageContext(), settings);

        setUiMode(settings.getUiMode());

        if (settings.getUseMonitoringService()) {
            CallMonitoringService.start(this);
        }
    }

    private Context getDeviceProtectedStorageContext() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createDeviceProtectedStorageContext();
        } else {
            return this;
        }
    }

}

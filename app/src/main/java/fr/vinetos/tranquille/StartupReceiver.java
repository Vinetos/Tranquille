package fr.vinetos.tranquille;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupReceiver extends BroadcastReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(StartupReceiver.class);

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        LOG.debug("onReceive({})", intent);

        // this broadcast receiver is used to start CallMonitoringService at boot or after app update
        // the actual starting happens in App
    }

}

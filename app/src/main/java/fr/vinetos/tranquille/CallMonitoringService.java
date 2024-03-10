package fr.vinetos.tranquille;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.vinetos.tranquille.data.YacbHolder;

import static fr.vinetos.tranquille.utils.StringUtils.quote;
import static java.util.Objects.requireNonNull;

public class CallMonitoringService extends Service {

    private static final String ACTION_START = "YACB_ACTION_START";
    private static final String ACTION_STOP = "YACB_ACTION_STOP";

    private static final Logger LOG = LoggerFactory.getLogger(CallMonitoringService.class);

    private final MyPhoneStateListener phoneStateListener = new MyPhoneStateListener();
    private final PhoneStateBroadcastReceiver phoneStateBroadcastReceiver
            = new PhoneStateBroadcastReceiver(
            PhoneStateHandler.Source.PHONE_STATE_BROADCAST_RECEIVER_MONITORING);

    private boolean monitoringStarted;

    public static void start(Context context) {
        ContextCompat.startForegroundService(context, getIntent(context, ACTION_START));
    }

    public static void stop(Context context) {
        context.stopService(getIntent(context, ACTION_STOP));
    }

    private static Intent getIntent(Context context, String action) {
        Intent intent = new Intent(context, CallMonitoringService.class);
        intent.setAction(action);
        return intent;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LOG.debug("onStartCommand({})", intent);

        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopMonitoring();
            stopForeground();
            stopSelf();
        } else {
            startForeground();
            startMonitoring();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LOG.debug("onBind({})", intent);
        return null;
    }

    @Override
    public void onDestroy() {
        LOG.debug("onDestroy()");
        stopMonitoring();
    }

    private void startForeground() {
        startForeground(NotificationHelper.NOTIFICATION_ID_MONITORING_SERVICE,
                NotificationHelper.createMonitoringServiceNotification(this));
    }

    private void stopForeground() {
        stopForeground(true);
    }

    private void startMonitoring() {
        if (monitoringStarted) return;
        monitoringStarted = true;

        try {
            getTelephonyManager().listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TelephonyManager.EXTRA_STATE_RINGING); // TODO: check
            intentFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            registerReceiver(phoneStateBroadcastReceiver, intentFilter);
        } catch (Exception e) {
            LOG.error("startMonitoring()", e);
        }
    }

    private void stopMonitoring() {
        if (!monitoringStarted) return;

        try {
            getTelephonyManager().listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);

            unregisterReceiver(phoneStateBroadcastReceiver);
        } catch (Exception e) {
            LOG.error("stopMonitoring()", e);
        }

        monitoringStarted = false;
    }

    private TelephonyManager getTelephonyManager() {
        return requireNonNull(
                (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
    }

    private static class MyPhoneStateListener extends PhoneStateListener {

        private static final Logger LOG = LoggerFactory.getLogger(MyPhoneStateListener.class);

        @Override
        public void onCallStateChanged(int state, String phoneNumber) {
            LOG.info("onCallStateChanged({}, {})", state, quote(phoneNumber));

            /*
             * According to docs, an empty string may be passed if the app lacks permissions.
             * The app deals with permissions in PhoneStateHandler.
             */
            if (TextUtils.isEmpty(phoneNumber)) {
                phoneNumber = null;
            }

            PhoneStateHandler phoneStateHandler = YacbHolder.getPhoneStateHandler();
            PhoneStateHandler.Source source = PhoneStateHandler.Source.PHONE_STATE_LISTENER;

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    phoneStateHandler.onIdle(source, phoneNumber);
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    phoneStateHandler.onRinging(source, phoneNumber);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    phoneStateHandler.onOffHook(source, phoneNumber);
                    break;
            }
        }
    }

}
